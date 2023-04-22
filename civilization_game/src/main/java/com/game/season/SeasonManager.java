package com.game.season;

import com.game.constant.DevideFactor;
import com.game.constant.MailId;
import com.game.dao.p.WorldDao;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.CtyGovern;
import com.game.domain.p.Property;
import com.game.domain.p.World;
import com.game.manager.CountryManager;
import com.game.manager.PlayerManager;
import com.game.manager.ServerManager;
import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.grand.entity.GrandInfo;
import com.game.season.grand.entity.StaticSeasonTreasury;
import com.game.season.journey.entity.*;
import com.game.season.seasongift.entity.SeasonGIftInfo;
import com.game.season.seven.entity.DayScore;
import com.game.season.seven.entity.SevenInfo;
import com.game.season.seven.entity.StaticSeasonSevenAward;
import com.game.season.seven.entity.StaticSeasonSevenRank;
import com.game.season.talent.entity.EffectType;
import com.game.season.talent.entity.StaticCompTalentUp;
import com.game.season.talent.entity.TalentInfo;
import com.game.season.turn.entity.StaticTurnAward;
import com.game.season.turn.entity.TurnInfo;
import com.game.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class SeasonManager {
	@Autowired
	StaticSeasonMgr staticSeasonMgr;
	@Autowired
	PlayerManager playerManager;
	@Autowired
	ServerManager serverManager;
	@Autowired
	SeasonRankManager seasonRankManager;
	@Autowired
	CountryManager countryManager;

	@Autowired
	WorldDao worldDao;

	private Map<Integer, SeasonActivity> activityMap = new ConcurrentHashMap<>();

	private Map<SeasonState, BiConsumer<SeasonActivity, Long>> actions = new HashMap<>();

	private Map<Integer, Consumer<SeasonActivity>> end = new HashMap<>();

	private int season = 1;

	public Map<Integer, BiFunction<Player, SeasonActivity, Boolean>> actionMap = new HashMap<>();

	public void init() {
		initAction();
		initRedAction();
		initEndAction();
		this.season = serverManager.getServer().getMergeNum();// 当前合服
		World world = worldDao.selectWorld(1);
		byte[] seasonAct = world.getSeasonAct();
		if (seasonAct != null) {
			try {
				SeasonActivityPb.SeasonActPb seasonActPb = SeasonActivityPb.SeasonActPb.parseFrom(seasonAct);
				int mergeNum = seasonActPb.getMergeNum();
				if (mergeNum == season) {
					List<SeasonActivityPb.SeasonActInfo> infoList = seasonActPb.getInfoList();
					infoList.forEach(x -> {
						activityMap.put(x.getId(), new SeasonActivity(x));
					});
					return;
				}
			} catch (Exception e) {

			}
		}
		List<StaticCompPlan> planList = staticSeasonMgr.getPlanList(this.season);
		for (StaticCompPlan staticCompPlan : planList) {
			// if (staticCompPlan.getActivityId() == SeasonAct.ACT_10.getActId() && season < 2 ) {//&&
			// continue;
			// }
			// long openTime = TimeHelper.getTime(TimeHelper.curentTime(), staticCompPlan.getOpenWeekBegin(), staticCompPlan.getWeekDay(), 0);
			long openTime = TimeHelper.getTime(TimeHelper.curentTime(), staticCompPlan.getOpenWeekBegin() - 1, staticCompPlan.getWeekDay(), 0);
			long preheat = 0L;
			if (staticCompPlan.getPreviewDays() != 0) {
				preheat = TimeHelper.getTime(new Date(openTime), staticCompPlan.getPreviewDays() * 24);// 预热24小时
			}
			long endTime = openTime + staticCompPlan.getOpenDays() * 24 * 60 * 60 * 1000L;
			long exhibitionTime = 0L;
			if (staticCompPlan.getShowDays() != 0) {
				exhibitionTime = TimeHelper.getTimeMinute(new Date(endTime), staticCompPlan.getShowDays() * 24 * 60);
			}
			SeasonActivity activity = new SeasonActivity();
			activity.setId(staticCompPlan.getCompseasonPlanId());
			activity.setActId(staticCompPlan.getActivityId());
			activity.setOpenTime(openTime);
			activity.setEndTime(endTime);
			activity.setPreheatTime(preheat);
			activity.setExhibitionTime(exhibitionTime);
			activity.setState(SeasonState.NO_OPEN);
			activity.setAwardId(staticCompPlan.getAwardId());
			activityMap.put(staticCompPlan.getCompseasonPlanId(), activity);
			LogHelper.GAME_LOGGER.info("赛季活动{},预热时间{},开始时间{},结束时间{},展示结束时间{}", staticCompPlan.getName(), DateHelper.getDate(activity.getPreheatTime()), DateHelper.getDate(activity.getOpenTime()), DateHelper.getDate(activity.getEndTime()), DateHelper.getDate(activity.getExhibitionTime()));
		}
	}

	public void flushSeason() {
		Iterator<SeasonActivity> iterator = activityMap.values().iterator();
		while (iterator.hasNext()) {
			SeasonActivity activity = iterator.next();
			actions.get(activity.getState()).accept(activity, TimeHelper.curentTime());
			if (activity.getState() == SeasonState.CLOSE) {
				iterator.remove();
			}
		}
	}

	/**
	 * 监听状态
	 */
	public void initAction() {
		actions.put(SeasonState.NO_OPEN, this::handlerNoOpen);
		actions.put(SeasonState.PREHEAT, this::handlerPrepare);
		actions.put(SeasonState.OPEN, this::handlerOpen);
		actions.put(SeasonState.END, this::handlerDoEnd);
		actions.put(SeasonState.EXHIB, this::handlerExhibition);
		actions.put(SeasonState.CLOSE, this::handlerClose);
	}

	/**
	 * 红点
	 */
	public void initRedAction() {
		actionMap.put(SeasonAct.ACT_1.getActId(), this::grandRedHot);
		actionMap.put(SeasonAct.ACT_2.getActId(), this::journeyRedHot);
		actionMap.put(SeasonAct.ACT_3.getActId(), this::turnRedHot);
		actionMap.put(SeasonAct.ACT_4.getActId(), this::sevenRedHot);

	}

	public void initEndAction() {
		end.put(SeasonAct.ACT_2.getActId(), this::sendJourneyAward);
		end.put(SeasonAct.ACT_3.getActId(), this::sendTurnAward);
		end.put(SeasonAct.ACT_10.getActId(), this::sendTalentAward);

	}

	private void handlerNoOpen(SeasonActivity seasonActivity, long currentTime) {
		if (seasonActivity.getPreheatTime() != 0) {
			if (currentTime > seasonActivity.getPreheatTime()) {
				seasonActivity.setState(SeasonState.PREHEAT);
				synSeasonState(seasonActivity);
			}
		} else {
			if (currentTime > seasonActivity.getOpenTime()) {
				seasonActivity.setState(SeasonState.OPEN);
				synSeasonState(seasonActivity);
			}
		}

	}

	private void handlerPrepare(SeasonActivity seasonActivity, long currentTime) {
		if (currentTime > seasonActivity.getOpenTime()) {
			seasonActivity.setState(SeasonState.OPEN);
			synSeasonState(seasonActivity);
		}
	}

	private void handlerOpen(SeasonActivity seasonActivity, long currentTime) {
		if (currentTime > seasonActivity.getEndTime()) {
			seasonActivity.setState(SeasonState.END);
			synSeasonState(seasonActivity);
		}
	}

	private void handlerDoEnd(SeasonActivity seasonActivity, long currentTime) {
		if (seasonActivity.getExhibitionTime() > 0) {
			seasonActivity.setState(SeasonState.EXHIB);
		} else {
			seasonActivity.setState(SeasonState.CLOSE);
		}
		synSeasonState(seasonActivity);

		Consumer<SeasonActivity> seasonActivityConsumer = end.get(seasonActivity.getActId());
		if (seasonActivityConsumer != null) {
			seasonActivityConsumer.accept(seasonActivity);
		}
	}

	private void handlerExhibition(SeasonActivity seasonActivity, long currentTime) {
		if (currentTime > seasonActivity.getExhibitionTime()) {
			seasonActivity.setState(SeasonState.CLOSE);
			synSeasonState(seasonActivity);
		}
	}

	private void handlerClose(SeasonActivity seasonActivity, long currentTime) {
		Map<Long, Player> allPlayer = playerManager.getAllPlayer();
		allPlayer.values().forEach(player -> {

			BaseModule baseModule = player.getSeasonAct().get(seasonActivity.getActId());
			if (baseModule != null) {
				baseModule.clean();
			} else {
				SeasonGIftInfo module = player.getModule(SeasonGIftInfo.class);
				module.clean(seasonActivity.getActId());
			}
		});
	}

	public Map<Integer, SeasonActivity> getActivityMap() {
		return activityMap;
	}

	public void setActivityMap(Map<Integer, SeasonActivity> activityMap) {
		this.activityMap = activityMap;
	}

	public SeasonActivity getSeasonActivity(int actId) {
		return activityMap.values().stream().filter(x -> x.getActId() == actId && (x.getState() == SeasonState.PREHEAT || x.getState() == SeasonState.OPEN || x.getState() == SeasonState.EXHIB)).findFirst().orElse(null);
	}

	public SeasonActivity getSeasonActivityById(int id) {
		return activityMap.get(id);
	}

	public void synSeasonState(SeasonActivity seasonActivity) {
		SeasonActivityPb.SynSeasonState.Builder builder = SeasonActivityPb.SynSeasonState.newBuilder();
		SeasonActivityPb.SeasonActivity.Builder builder1 = SeasonActivityPb.SeasonActivity.newBuilder();
		builder1.setActivityId(seasonActivity.getActId());
		builder1.setState(seasonActivity.getState().getState());
		builder1.setPreTime(seasonActivity.getPreheatTime());
		builder1.setBeginTime(seasonActivity.getOpenTime());
		builder1.setEndTime(seasonActivity.getEndTime());
		builder1.setDisplayTime(seasonActivity.getExhibitionTime());
		builder1.setId(seasonActivity.getId());
		builder1.setAwardId(seasonActivity.getAwardId());
		builder.setActivity(builder1);
		List<Player> onlinePlayer = playerManager.getOnlinePlayer();
		for (Player player : onlinePlayer) {
			SynHelper.synMsgToPlayer(player, SeasonActivityPb.SynSeasonState.EXT_FIELD_NUMBER, SeasonActivityPb.SynSeasonState.ext, builder.build());
		}

	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	/**
	 * 是否有红点
	 *
	 * @param player
	 * @param seasonActivity
	 * @return
	 */
	public boolean isRed(Player player, SeasonActivity seasonActivity) {
		BiFunction<Player, SeasonActivity, Boolean> action = actionMap.get(seasonActivity.getActId());
		if (action != null) {
			return action.apply(player, seasonActivity);
		}
		return false;
	}

	public boolean grandRedHot(Player player, SeasonActivity seasonActivity) {
		GrandInfo module = player.getModule(GrandInfo.class);
		if (module.getState() != 1) {
			return true;
		}
		return false;
	}

	public boolean sevenRedHot(Player player, SeasonActivity seasonActivity) {
		SevenInfo module = player.getModule(SevenInfo.class);
		Map<Integer, Integer> total = module.getTotal();
		Iterator<Map.Entry<Integer, Integer>> iterator = total.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer> next = iterator.next();
			Integer day = next.getKey();
			Integer score = next.getValue();
			List<StaticSeasonSevenAward> sevenAwardList = staticSeasonMgr.getSevenAwardList(seasonActivity.getAwardId(), day);
			if (sevenAwardList != null) {
				for (StaticSeasonSevenAward staticSeasonSevenAward : sevenAwardList) {
					if (score >= staticSeasonSevenAward.getCond() && module.getState(staticSeasonSevenAward.getId()) == 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean journeyRedHot(Player player, SeasonActivity seasonActivity) {
		JourneyInfo module = player.getModule(JourneyInfo.class);
		Map<Integer, Integer> taskCount = module.getTaskCount();
		Iterator<Map.Entry<Integer, Integer>> iterator = taskCount.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer> next = iterator.next();
			Integer key = next.getKey();
			StaticSeasonJourney staticSeasonJourney = staticSeasonMgr.getStaticSeasonJourney(key);
			if (staticSeasonJourney != null && next.getValue() >= staticSeasonJourney.getCond() && module.getTaskState(key) == 0) {
				return true;
			}
		}
		List<StaticJourneyAward> staticJourneyAwards = staticSeasonMgr.getStaticJourneyAwards(seasonActivity.getAwardId());
		if (staticJourneyAwards != null) {
			int active = module.getActive();
			for (StaticJourneyAward staticJourneyAward : staticJourneyAwards) {
				if (active >= staticJourneyAward.getCond() && module.getActiveState(staticJourneyAward.getId()) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean turnRedHot(Player player, SeasonActivity seasonActivity) {
		TurnInfo module = player.getModule(TurnInfo.class);
		if (module.getCount() > 0) {
			return true;
		}
		List<StaticTurnAward> staticTurnAwards = staticSeasonMgr.getStaticTurnAwards(seasonActivity.getAwardId());
		if (staticTurnAwards != null) {
			for (StaticTurnAward staticTurnAward : staticTurnAwards) {
				if (module.getTotalCount() >= staticTurnAward.getCond() && module.getReciveState(staticTurnAward.getId()) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public double getSeasonBuf(Player player, EffectType... buffType) {
		if (player == null) {
			return 0d;
		}
		double seaBuf;
		TalentInfo module = player.getModule(TalentInfo.class);
		Map<Integer, Map<Integer, Integer>> map = module.getMap();
		AtomicInteger add = new AtomicInteger();
		map.values().forEach(x -> x.values().forEach(y -> {
			StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(y);
			if (staticCompTalentUp != null) {
				List<List<Integer>> effect = staticCompTalentUp.getEffect();
				for (EffectType effectType : buffType) {
					effect.forEach(z -> {
						if (z.get(0) == effectType.getEffId()) {
							add.addAndGet(z.get(2));
						}
					});
				}
			}
		}));
		seaBuf = add.get() / DevideFactor.PERCENT_NUM;
		return seaBuf;
	}

	public int getBuf(Player player, EffectType buffType, int type) {
		if (player == null) {
			return 0;
		}
		TalentInfo module = player.getModule(TalentInfo.class);
		Map<Integer, Map<Integer, Integer>> map = module.getMap();
		AtomicInteger add = new AtomicInteger();
		map.values().forEach(x -> x.values().forEach(y -> {
			StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(y);
			if (staticCompTalentUp != null) {
				List<List<Integer>> effect = staticCompTalentUp.getEffect();
				if (effect != null) {
					effect.forEach(z -> {
						if (z.get(0) == buffType.getEffId() && z.get(1) == type) {
							add.addAndGet(z.get(2));
						}
					});
				}
			}
		}));
		return add.get();
	}

	public int getBuf(Player player, EffectType buffType) {
		if (player == null) {
			return 0;
		}
		TalentInfo module = player.getModule(TalentInfo.class);
		Map<Integer, Map<Integer, Integer>> map = module.getMap();
		AtomicInteger add = new AtomicInteger();
		map.values().forEach(x -> x.values().forEach(y -> {
			StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(y);
			if (staticCompTalentUp != null) {
				List<List<Integer>> effect = staticCompTalentUp.getEffect();
				if (effect != null) {
					effect.forEach(z -> {
						if (z.get(0) == buffType.getEffId()) {
							add.addAndGet(z.get(2));
						}
					});
				}
			}
		}));
		return add.get();
	}

	/**
	 * 增加基础攻击力等等
	 * 
	 * @param player
	 * @param property
	 */
	public void addProperty(Player player, Property property) {
		if (player == null) {
			return;
		}
		TalentInfo module = player.getModule(TalentInfo.class);
		Map<Integer, Map<Integer, Integer>> map = module.getMap();
		for (Map<Integer, Integer> value : map.values()) {
			for (Integer val : value.values()) {
				StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(val);
				List<List<Integer>> effect = staticCompTalentUp.getEffect();
				for (List<Integer> list : effect) {
					if (list.get(0) == EffectType.EFFECT_TYPE3.getEffId()) {
						property.addValue(list.get(1), list.get(2));
					}
				}
			}
		}
	}

	/**
	 * 赛季旅程 结束后 邮件发放 奖励
	 * 
	 * @param seasonActivity
	 */
	public void sendJourneyAward(SeasonActivity seasonActivity) {
		Map<Long, Player> allPlayer = playerManager.getAllPlayer();

		// 阵营
		Map<Integer, AtomicInteger> journeyCampRank = seasonRankManager.getJourneyCampRank();
		List<SeasonActivityPb.CountrySort> countrySorts = new ArrayList<>();
		for (Map.Entry<Integer, AtomicInteger> integerAtomicIntegerEntry : journeyCampRank.entrySet()) {
			SeasonActivityPb.CountrySort.Builder builder1 = SeasonActivityPb.CountrySort.newBuilder();
			builder1.setCountry(integerAtomicIntegerEntry.getKey());
			builder1.setScore(integerAtomicIntegerEntry.getValue().get());
			countrySorts.add(builder1.build());
		}
		countrySorts = countrySorts.stream().sorted(Comparator.comparing(SeasonActivityPb.CountrySort::getScore).reversed()).collect(Collectors.toList());
		for (Player player : allPlayer.values()) {
			JourneyInfo module = player.getModule(JourneyInfo.class);
			Map<Integer, Integer> taskCount = module.getTaskCount();
			Iterator<Map.Entry<Integer, Integer>> iterator = taskCount.entrySet().iterator();
			List<CommonPb.Award> list = new ArrayList<>();
			while (iterator.hasNext()) {
				Map.Entry<Integer, Integer> next = iterator.next();
				Integer key = next.getKey();
				StaticSeasonJourney staticSeasonJourney = staticSeasonMgr.getStaticSeasonJourney(key);
				if (staticSeasonJourney != null && next.getValue() >= staticSeasonJourney.getCond() && module.getTaskState(key) == 0) {
					List<List<Integer>> award = staticSeasonJourney.getAward();
					award.forEach(x -> {
						list.add(PbHelper.createAward(x.get(0), x.get(1), x.get(2)).build());
					});
				}
			}
			List<StaticJourneyAward> staticJourneyAwards = staticSeasonMgr.getStaticJourneyAwards(seasonActivity.getAwardId());
			if (staticJourneyAwards != null) {
				int active = module.getActive();
				for (StaticJourneyAward staticJourneyAward : staticJourneyAwards) {
					if (active >= staticJourneyAward.getCond() && module.getActiveState(staticJourneyAward.getId()) == 0) {
						List<List<Integer>> award = staticJourneyAward.getAward();
						award.forEach(x -> {
							list.add(PbHelper.createAward(x.get(0), x.get(1), x.get(2)).build());
						});
					}
				}
			}
			if (!list.isEmpty()) {
				playerManager.sendAttachMail(player, PbHelper.finilAward(list), MailId.SEASON_MAIL_163, null);
			}
			// 阵营排行奖励
			List<CommonPb.Award> rankAward = new ArrayList<>();
			int rank = 0;
			int type = 0;
			for (int i = 0; i < countrySorts.size(); i++) {
				SeasonActivityPb.CountrySort countrySort = countrySorts.get(i);
				if (player.getCountry() == countrySort.getCountry()) {

					CtyGovern govern = countryManager.getGovern(player);
					if (govern != null) {
						type = govern.getGovernId();
					}
					StaticJourneyRankOfficer rankOff = staticSeasonMgr.getRankOff(seasonActivity.getAwardId(), i + 1, type);
					if (rankOff != null) {
						List<List<Integer>> award = rankOff.getAward();
						award.forEach(x -> {
							rankAward.add(PbHelper.createAward(x.get(0), x.get(1), x.get(2)).build());
						});
					}
					rank = i + 1;
					break;
				}
			}
			if (!rankAward.isEmpty()) {
				playerManager.sendAttachMail(player, PbHelper.finilAward(rankAward), MailId.SEASON_MAIL_165, String.valueOf(rank), String.valueOf(type));
			}
		}
		// 个人
		Map<Long, JourneyInfo> journeyPesRank = seasonRankManager.getJourneyPesRank();
		List<JourneyInfo> journeyInfos = new ArrayList<>(journeyPesRank.values());
		List<JourneyInfo> collect = journeyInfos.stream().sorted(Comparator.comparing(JourneyInfo::getScore).reversed()).collect(Collectors.toList());
		for (int i = 0; i < collect.size(); i++) {
			StaticJourneyPerson staticJourneyPerson1 = staticSeasonMgr.getStaticJourneyPerson(seasonActivity.getAwardId(), i);
			if (staticJourneyPerson1 == null) {
				continue;
			}
			JourneyInfo journeyInfo = collect.get(i);
			List<CommonPb.Award> list = new ArrayList<>();
			List<List<Integer>> award = staticJourneyPerson1.getAward();
			award.forEach(x -> {
				list.add(PbHelper.createAward(x.get(0), x.get(1), x.get(2)).build());
			});
			playerManager.sendAttachMail(journeyInfo.getPlayer(), PbHelper.finilAward(list), MailId.SEASON_MAIL_164, String.valueOf(i + 1));
		}
	}

	/**
	 * 每日结束发 赛季七日排行奖励
	 */
	public void sendSevenRankAward() {
		SeasonActivity seasonActivity = getSeasonActivity(SeasonAct.ACT_4.getActId());
		if (seasonActivity == null) {
			return;
		}
		int day = TimeHelper.equation(seasonActivity.getOpenTime(), TimeHelper.curentTime()) + 1;// 第几天
		if (day > 7) {
			return;
		}
		List<StaticSeasonSevenAward> sevenAwardList = staticSeasonMgr.getSevenAwardList(seasonActivity.getAwardId(), day);
		Map<Long, Player> allPlayer = playerManager.getAllPlayer();
		allPlayer.values().forEach(player -> {
			SevenInfo module = player.getModule(SevenInfo.class);
			int score = module.getDayTotalScore(day);
			List<CommonPb.Award> list = new ArrayList<>();
			if (sevenAwardList != null) {
				for (StaticSeasonSevenAward staticSeasonSevenAward : sevenAwardList) {
					if (score < staticSeasonSevenAward.getCond() || module.isGet(staticSeasonSevenAward.getId())) {
						continue;
					}
					List<List<Integer>> award = staticSeasonSevenAward.getAward();
					for (List<Integer> integerList : award) {
						list.add(PbHelper.createAward(integerList.get(0), integerList.get(1), integerList.get(2)).build());
					}
					module.updateState(staticSeasonSevenAward.getId());
				}
				playerManager.sendAttachMail(player, PbHelper.finilAward(list), MailId.SEASON_MAIL_160, String.valueOf(day));
			}
		});
		List<DayScore> list = new ArrayList<>();
		Map<Long, DayScore> dayRank = seasonRankManager.getDayRank(day);
		list.addAll(dayRank.values());
		List<DayScore> collect = list.stream().filter(x -> x.getScore() >= 2000).sorted(Comparator.comparing(DayScore::getScore).reversed()).collect(Collectors.toList());
		List<StaticSeasonSevenRank> sevenRack = staticSeasonMgr.getSevenRack(seasonActivity.getAwardId(), 1);
		if (sevenRack == null) {
			return;
		}
		for (int i = 0; i < collect.size(); i++) {
			DayScore dayScore = collect.get(i);
			List<Award> awards = new ArrayList<>();
			for (StaticSeasonSevenRank staticSeasonSevenRank : sevenRack) {
				if (i < staticSeasonSevenRank.getRank()) {
					// 发每日排行榜奖励
					List<List<Integer>> award = staticSeasonSevenRank.getAward();
					award.forEach(x -> {
						awards.add(new Award(x.get(0), x.get(1), x.get(2)));
					});
					break;
				}
			}
			Player player = dayScore.getPlayer();
			playerManager.sendAttachMail(player, awards, MailId.SEASON_MAIL_161, String.valueOf(day), String.valueOf(i + 1));
		}
		if (day == 7) {
			Map<Long, DayScore> dayRank1 = seasonRankManager.getDayRank();// 总排行
			list.addAll(dayRank1.values());
			List<DayScore> collect1 = list.stream().filter(x -> x.getScore() >= 5000).sorted(Comparator.comparing(DayScore::getScore).reversed()).collect(Collectors.toList());
			List<StaticSeasonSevenRank> staticSeasonSevenRanks = staticSeasonMgr.getSevenRack(seasonActivity.getAwardId(), 2);
			for (int i = 0; i < collect1.size(); i++) {
				DayScore dayScore = collect1.get(i);
				List<Award> awards = new ArrayList<>();
				for (StaticSeasonSevenRank staticSeasonSevenRank : staticSeasonSevenRanks) {
					if (i < staticSeasonSevenRank.getRank()) {
						// 发总排行奖励
						List<List<Integer>> award = staticSeasonSevenRank.getAward();
						award.forEach(x -> {
							awards.add(new Award(x.get(0), x.get(1), x.get(2)));
						});
						break;

					}
				}
				playerManager.sendAttachMail(dayScore.getPlayer(), awards, MailId.SEASON_MAIL_162, String.valueOf(i + 1));
			}
		}
	}

	/**
	 * 宏伟宝库发奖励
	 */
	public void sendTreasuryAward() {
		SeasonActivity seasonActivity = getSeasonActivity(SeasonAct.ACT_1.getActId());
		if (seasonActivity == null) {
			return;
		}
		Map<Integer, StaticSeasonTreasury> staticSeasonTreasury = staticSeasonMgr.getStaticSeasonTreasury(seasonActivity.getAwardId());
		Map<Long, Player> allPlayer = playerManager.getAllPlayer();
		allPlayer.values().forEach(player -> {
			GrandInfo module = player.getModule(GrandInfo.class);
			if (TimeHelper.curentTime() > module.getNextTime()) {
				Map<Integer, List<CommonPb.Award>> map = new HashMap<>();
				staticSeasonTreasury.values().forEach(y -> {
					int score = module.getScore(y.getType(), y.getId());
					if (score >= y.getCond()) {
						List<CommonPb.Award> awards = map.computeIfAbsent(y.getType(), x -> new ArrayList<>());
						List<List<Integer>> award = y.getAward();
						List<Integer> list = award.get(new Random().nextInt(award.size()));
						CommonPb.Award.Builder award1 = PbHelper.createAward(list.get(0), list.get(1), list.get(2));
						awards.add(award1.build());
					}
				});
				List<CommonPb.Award> list = new ArrayList<>();
				if (!map.isEmpty()) {
					for (List<CommonPb.Award> value : map.values()) {
						list.add(value.get(new Random().nextInt(value.size())));
					}
					playerManager.sendAttachMail(player, PbHelper.finilAward(list), MailId.SEASON_MAIL_159, null);
				}

				module.clean();
			}
		});
	}

	public void sendTalentAward(SeasonActivity seasonActivity) {
		Map<Long, Player> allPlayer = playerManager.getAllPlayer();
		for (Player player : allPlayer.values()) {
			TalentInfo module = player.getModule(TalentInfo.class);
			Map<Integer, Map<Integer, Integer>> map = module.getMap();
			List<CommonPb.Award> list = new ArrayList<>();
			map.values().forEach(x -> x.values().forEach(y -> {
				StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(y);
				if (staticCompTalentUp.getTotalCost() != null) {
					List<List<Integer>> total = staticCompTalentUp.getTotalCost();
					total.forEach(totalCost -> {
						list.add(PbHelper.createAward(totalCost.get(0), totalCost.get(1), totalCost.get(2)).build());
					});
				}
			}));
			if (!list.isEmpty()) {
				playerManager.sendAttachMail(player, PbHelper.finilAward(list), MailId.SEASON_MAIL_166, null);
			}
		}
	}

	public void sendTurnAward(SeasonActivity seasonActivity) {
		Map<Long, Player> allPlayer = playerManager.getAllPlayer();
		for (Player player : allPlayer.values()) {
			TurnInfo module = player.getModule(TurnInfo.class);
			List<CommonPb.Award> list = new ArrayList<>();
			List<StaticTurnAward> staticTurnAwards = staticSeasonMgr.getStaticTurnAwards(seasonActivity.getAwardId());
			if (staticTurnAwards != null) {
				for (StaticTurnAward staticTurnAward : staticTurnAwards) {
					if (module.getTotalCount() >= staticTurnAward.getCond() && module.getReciveState(staticTurnAward.getId()) == 0) {
						List<List<Integer>> award = staticTurnAward.getAward();
						award.forEach(x -> {
							list.add(PbHelper.createAward(x.get(0), x.get(1), x.get(2)).build());
						});
					}
				}
			}
			if (!list.isEmpty()) {
				playerManager.sendAttachMail(player, PbHelper.finilAward(list), MailId.SEASON_MAIL_169, null);
			}
		}
	}
}
