package com.game.manager;

import com.game.Loading;
import com.game.constant.*;
import com.game.dataMgr.StaticFirstBloodMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.define.LoadData;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.StaticFirstBloodAward;
import com.game.domain.s.StaticPvpBattleLv;
import com.game.domain.s.StaticWorldActPlan;
import com.game.log.domain.HatcheryLog;
import com.game.pb.CommonPb;
import com.game.pb.PvpBattlePb.SynPvpInfoRq;
import com.game.server.GameServer;
import com.game.service.AchievementService;
import com.game.service.WorldActPlanService;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.MapInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@LoadData(name = "皇城血战", type = Loading.LOAD_USER_DB,initSeq = 2000)
public class WorldPvpMgr extends BaseManager{

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private BattleMgr battleMgr;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;

	@Autowired
	private WorldActPlanService worldActPlanService;

	@Autowired
	private WorldPvpMgr worldPvpMgr;

	@Autowired
	private StaticFirstBloodMgr staticFirstBloodMgr;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private PersonalityManager personalityManager;

	@Autowired
	AchievementService achievementService;

	private LinkedList<PvpPkInfo> pvpPkInfos = new LinkedList<PvpPkInfo>();                   // pvp战斗信息
	private int open;
	private int lastFightDay;                                                        // 上一次pvp结束的时间[TODO存数据库]

	private Logger logger = LoggerFactory.getLogger(getClass());

	//参加血战的人 主要做推送
	private Set<Long> set = Collections.synchronizedSet(new HashSet());

	// 数据从WorldData里面取出, 所以init函数在序列化world数据之后才启动
	// 累杀数=所有英雄杀兵总和, 连杀=英雄连续杀兵总和

	@Override
	public void init() throws Exception{
		setOpen(1);

		// 初始化战斗实体
		checkPvpBattle();
		checkSameRank();
		// 三个国家挖图纸信息
		checkDigInfo();
	}

	// 皇城血战结束之后清除所有玩家的PvpHero,以及累杀
	// 国宴完成之后清除所有玩家的国宴状态
	public void checkDigInfo() {
		WorldData worldData = getWorldData();
		worldData.checkDigInfo();
	}

	public void checkPvpBattle() {
		WorldData worldData = getWorldData();
		worldData.checkPvpBattle();
	}

	public WorldData getWorldData() {
		return worldManager.getWolrdInfo();
	}

	public int getWorldPvpState() {
		WorldData worldData = getWorldData();
		return worldData.getWorldPvpState();
	}

	public void setWorldPvpState(int state) {
		WorldData worldData = worldManager.getWolrdInfo();
		worldData.setWorldPvpState(state);
	}

	public LinkedList<RankPvpHero> getRankList() {
		WorldData worldData = worldManager.getWolrdInfo();
		return worldData.getRankList();
	}

	public LinkedList<RankPvpHero> getRankListCopy() {
		LinkedList<RankPvpHero> list = new LinkedList<>();
		LinkedList<RankPvpHero> l = getRankList();
		list.addAll(l);
		return list;
	}

	public HashSet<Long> getAttenders() {
		WorldData worldData = worldManager.getWolrdInfo();
		return worldData.getAttenders();
	}

	public LinkedList<PvpHero> getDeadHeros() {
		WorldData worldData = worldManager.getWolrdInfo();
		return worldData.getDeadHeroes();
	}

	public HashMap<Integer, HashMap<Integer, DigInfo>> getDigPapers() {
		WorldData worldData = worldManager.getWolrdInfo();
		return worldData.getDigPapers();
	}


	public long getBanquetEndTime() {
		WorldData worldData = worldManager.getWolrdInfo();
		return worldData.getBanquetEndTime();
	}

	public void setBanquetEndTime(long banquetEndTime) {
		WorldData worldData = worldManager.getWolrdInfo();
		worldData.setBanquetEndTime(banquetEndTime);
	}

	public void handlePvpClear() {
		WorldData worldData = worldManager.getWolrdInfo();
		worldData.handlePvpClear();
	}

	// 检查皇城血战是否开启
	public void checkPvpOpen() {
		WorldData worldData = worldManager.getWolrdInfo();
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_1);
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_1);
		if (worldActPlan == null) {
			return;
		}
		WorldTargetTask worldTargetTask = worldData.getTasks().get(staticWorldActPlan.getTargetId());
		if (worldTargetTask == null) {
			if (worldActPlan != null) {
				worldData.getWorldActPlans().remove(worldActPlan.getId());
			}
			return;
		}

		if (worldActPlan.getOpenTime() == 0) {
			return;
		}
		long now = System.currentTimeMillis();
		// 首先 检查战斗是否结束!
		long endDate = TimeHelper.getHoursOfDay(staticWorldActPlan.getContinues().get(3));
		if (now > endDate) {
			if (getWorldPvpState() == WorldPvpState.START) {
				setWorldPvpState(WorldPvpState.END);
				setPvpEndTime(0L);
				logger.info("皇城血战从开始到结束!");
				//TODO 计算下一轮皇城血战的时间
				worldActPlanService.activityEnd(worldActPlan);
			}
			//LogHelper.GAME_DEBUG.error("战斗已经结束了,重新调整时间!");
			return;
		}

		//TODO  这里主要未开启装备改成预热状态
		if (worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN) {
			if (worldActPlan.getPreheatTime() != 0 && now > worldActPlan.getPreheatTime()) {
				worldActPlan.setState(WorldActPlanConsts.PREHEAT);
				worldActPlanService.syncWorldActivityPlan();
				logger.info("皇城血战从初始状态到预热状态 预热开始时间{}", DateHelper.getDate(worldActPlan.getPreheatTime()));
				return;
			}
		}
		// long startDate = TimeHelper.getHoursOfDay(startHour);
		long startDate = worldActPlan.getOpenTime();
		if (now < startDate) {
			return;
		}
		// 检查战斗是否开启!
		if (now >= startDate) {
			// 如果已经打完的时间和当前时间相同，则不能进行下去
			if (GameServer.getInstance().currentDay == lastFightDay) {
				return;
			}
			if (getWorldPvpState() == WorldPvpState.INIT) {
				setWorldPvpState(WorldPvpState.START);
				handlePvpClear();       // 清空战斗信息
				clearPlayerPvpInfo();
				long period = TimeHelper.SECOND_MS * staticLimitMgr.getNum(133);
				setPvpEndTime(now + period);
				//设置活动结束时间

				worldData.setActivityEndTime(now + staticWorldActPlan.getContinues().get(0) * TimeHelper.SECOND_MS + staticWorldActPlan.getContinues().get(2) * TimeHelper.SECOND_MS);
				worldActPlan.setState(WorldActPlanConsts.OPEN);
				// 推送活动结束时间
				synAllEndTime();
				worldActPlanService.syncWorldActivityPlan();
				logger.info("皇城血战从初始状态到开始状态 开始时间{}", DateHelper.getDate(now));
				logger.info("皇城血战从初始状态到开始状态 结束时间{}", DateHelper.getDate(getPvpEndTime()));
			}
		}
	}


	public boolean isPvpOver() {
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_1);
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_1);
		if (worldActPlan == null) {
			return true;
		}
		long now = System.currentTimeMillis();
		//long startDate = TimeHelper.getHoursOfDay(startHour);
		long startDate = worldActPlan.getOpenTime();

		long endDate = TimeHelper.getHoursOfDay(staticWorldActPlan.getContinues().get(3));

		return now < startDate || now > endDate || GameServer.getInstance().currentDay == lastFightDay;
	}


	// 战中、战斗结束、下一次战斗
	// 战斗状态需要存盘、参战队伍需要存盘
	public void pvpLogic() {
		if (getWorldPvpState() == WorldPvpState.INIT) {
			doInitLogic();
		} else if (getWorldPvpState() == WorldPvpState.START) {
			doStartLogic();
		} else if (getWorldPvpState() == WorldPvpState.END) {
			doEndLogic();
		} else if (getWorldPvpState() == WorldPvpState.BANQUET) {
			doBanquetLogic();
		}
	}


	private void doStartLogic() {
		//LogHelper.GAME_DEBUG.error("do start logic");
//        LogHelper.GAME_DEBUG.error("开始执行皇城血战的打斗逻辑！");
		warCheckLogic();
		checkCenter();
//        LogHelper.GAME_DEBUG.error("皇城血战倒计时=" + (getPvpEndTime()/1000-now/1000));

	}

	private void doBanquetLogic() {
		long now = System.currentTimeMillis();
		if (now > getBanquetEndTime()) {
			WorldData worldData = worldManager.getWolrdInfo();
			WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_1);
			if (worldActPlan == null) {
				worldActPlan = new WorldActPlan();
				worldActPlan.setId(WorldActivityConsts.ACTIVITY_1);
			}
			worldActPlan.setState(WorldActPlanConsts.END);
			worldActPlan.setEndTime(System.currentTimeMillis());
			worldActPlanService.refreshWorldActPlan(worldActPlan);
			setWorldPvpState(WorldPvpState.INIT);
			worldActPlanService.syncWorldActivityPlan();

			logger.info("doBanquetLogic : 国宴结束  计算下一次皇城血战的时间 开始时间 {}", DateHelper.getDate(worldActPlan.getOpenTime()));
			logger.info("doBanquetLogic : 国宴结束  计算下一次皇城血战的时间 预热时间 {}", DateHelper.getDate(worldActPlan.getPreheatTime()));
		}
	}


	private void doEndLogic() {
		lastFightDay = GameServer.getInstance().currentDay;
		setWorldPvpState(WorldPvpState.BANQUET);
		setFirstBlood();
		handleScore();
		doEndScoreMail();
		clearAttender();
		LogHelper.GAME_DEBUG.error("doEndLogic: 清除所有玩家的集结英雄和Pvp英雄, 以及英雄.");
		handleBanquetTime();
		handlePvpClear();       // 清空战斗信息
		clearPlayerPvpInfo();   // 清除所有玩家集结的英雄和累杀以及损兵数
		handlePvpScore();       // 清理积分
		setPvpEndTime(0L);
		setPvpPeriod(0);
		synAllEndTime();
		set.clear();
	}

	/**
	 * 母巢之战结束发送邮件，同步积分
	 */
	public void doEndScoreMail() {
		WorldData worldData = getWorldData();
		long pvpScore = worldData.getPvpBattleScore();
		int level = 1;
		if (pvpScore >= staticLimitMgr.getNum(216)) {
			level = 2;
		}
		StaticPvpBattleLv staticWorldPvpBattleLv = staticWorldMgr.getPvpBattleLvMap().get(level);
		int country = getPvpCountry();
		int num = staticLimitMgr.getNum(SimpleId.BROOD_WAR_DIE_SOLDIERS);
		for (Player player : playerManager.getPlayers().values()) {
			if (player.getCountry() == country) {
				player.getSimpleData().addPvpScore(staticWorldPvpBattleLv.getScore());
				playerManager.sendNormalMail(player, MailId.WORLD_PVP_SUCCESS, String.valueOf(level), String.valueOf(country), String.valueOf(staticWorldPvpBattleLv.getScore()));
				personalityManager.checkIconOpen(player, 3);
				SpringUtil.getBean(com.game.log.LogUser.class).hatchery_log(
					HatcheryLog.builder()
						.lordId(player.roleId)
						.nick(player.getNick())
						.lv(player.getLevel())
						.title(player.getTitle())
						.point(staticWorldPvpBattleLv.getScore())
						.source(HatcheryLog.SCORE_MAIL)
						.build()
				);
			} else {
				playerManager.sendNormalMail(player, MailId.LAST_WAR_REPORT, String.valueOf(level), String.valueOf(country));
			}
			//发送邮件，根据玩家在母巢之战中死亡的损兵数给予军功
			int dieSoldiers = player.getSimpleData().getDieSoldiers();
			int honor = dieSoldiers * num;
			if (honor > 0) {
				List<Award> awards = new ArrayList<>();
				awards.add(new Award(AwardType.LORD_PROPERTY, LordPropertyType.HONOR, honor));
				playerManager.sendAttachMail(player, awards, MailId.BROOD_WAR_DIE_SOLDIERS, Integer.toString(country), Integer.toString(player.getSimpleData().getThisTimePvpScore()), Integer.toString(dieSoldiers), Integer.toString(honor), Integer.toString(1));
			} else {
				playerManager.sendNormalMail(player, MailId.BROOD_WAR_DIE_SOLDIERS, Integer.toString(country), Integer.toString(player.getSimpleData().getThisTimePvpScore()), Integer.toString(dieSoldiers), Integer.toString(honor), Integer.toString(2));
			}
		}
	}

	/**
	 * 设置首杀
	 */
	public void setFirstBlood() {
		MapInfo mapInfo = worldManager.getWorldMapInfo().get(20);
		LinkedList<RankPvpHero> rankList = worldPvpMgr.getRankListCopy();
		List<Player> helperList = new ArrayList<>();
		Player player = null;
		int helperNum = staticLimitMgr.getNum(257) == 0 ? 9 : staticLimitMgr.getNum(257);
		Set<Long> set = new HashSet<>();
		for (int i = 0; i < rankList.size(); i++) {
			RankPvpHero rankPvpHero = rankList.get(i);
			if (rankPvpHero == null) {
				continue;
			}
			long lordId = rankPvpHero.getLordId();
			Player attacker = playerManager.getPlayer(lordId);
			if (attacker.getCountry() == getPvpCountry() && attacker != null) {
				StaticFirstBloodAward staticFirstBloodAward = staticFirstBloodMgr.getStaticFirstBloodAwardMap().get(9);
				if (staticFirstBloodAward != null && staticFirstBloodAward.getAward() != null) {
					List<List<Integer>> lists = staticFirstBloodAward.getAward();
					ArrayList<Award> awards = new ArrayList<>();
					for (List<Integer> list : lists) {
						awards.add(new Award(list.get(0), list.get(1), list.get(2)));
					}
					playerManager.sendAttachMail(attacker, awards, MailId.ACT_FIRST_BLOOD_AWARD_WEEK);
				}
				if (player == null) {
					player = attacker;
				} else if (helperList.size() < helperNum - 1) {
					if (set.contains(attacker.getLord().getLordId())) {
						continue;
					}
					set.add(player.getLord().getLordId());
					helperList.add(attacker);
				}
			}
		}
		mapInfo.setFirstBlood(CityType.WORLD_FORTRESS, player, helperList, 20);
		set.clear();
	}


	public void doGmEndLogic() {
		lastFightDay = GameServer.getInstance().currentDay;
		setWorldPvpState(WorldPvpState.INIT);
		clearAttender();
		LogHelper.GAME_DEBUG.error("doGmEndLogic: 清除所有玩家的集结英雄和Pvp英雄, 以及英雄.");
		handleScore();
		handleBanquetTime();
		handlePvpClear();       // 清空战斗信息
		clearPlayerPvpInfo();   // 清除所有玩家集结的英雄和累杀
		handlePvpScore();       // 清理积分
		setPvpEndTime(0L);
		setPvpPeriod(0);
		synAllEndTime();
		setBanquetEndTime(0L);
		lastFightDay = 0;
		set.clear();
	}

	public void clearAttender() {
		HashSet<Long> attenders = getAttenders();
		for (Long lordId : attenders) {
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				player.clearMassHeroes();
				player.clearPvpHeroes();
			}
		}

	}

	public void handlePvpScore() {
		WorldData worldData = getWorldData();
		worldData.setPvpBattleScore(0);
	}

	// 活动结束后这里进行空转
	// 到时候优化下
	private void doInitLogic() {
		//LogHelper.GAME_DEBUG.error("do init logic");
	}

	/**
	 * 国宴固定24小时
	 */
	public void handleBanquetTime() {
		long now = System.currentTimeMillis();
		setBanquetEndTime(now + TimeHelper.DAY_MS);
	}

	public void handleScore() {
		handleMutilScore();
		handleTotalScore();
	}

	// 处理连杀积分
	public void handleMutilScore() {
		LinkedList<RankPvpHero> rankList = getRankList();
		int size = rankList.size();
		for (int i = 0; i < size; i++) {
			RankPvpHero rankPvpHero = rankList.get(i);
			if (rankPvpHero == null) {
				LogHelper.CONFIG_LOGGER.error("rankPvpHero is null");
				continue;
			}

			Player player = playerManager.getPlayer(rankPvpHero.getLordId());
			if (player == null) {
				continue;
			}

			int awardScore = staticWorldMgr.getPvpScore(i + 1);
			player.addPvpScore(awardScore);
			SpringUtil.getBean(com.game.log.LogUser.class).hatchery_log(
				HatcheryLog.builder()
					.lordId(player.roleId)
					.nick(player.getNick())
					.lv(player.getLevel())
					.title(player.getTitle())
					.point(awardScore)
					.source(HatcheryLog.MULTI)
					.build()
			);
		}
	}

	// 处理累杀积分
	public void handleTotalScore() {
		HashSet<Long> attenders = getAttenders();
		for (Long lordId : attenders) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			int totalKill = player.getTotalKillNum();
			int totalScore = staticWorldMgr.getTotalScore(totalKill);
			player.addPvpScore(totalScore);
			synPvpScore(player);
			SpringUtil.getBean(com.game.log.LogUser.class).hatchery_log(
				HatcheryLog.builder()
					.lordId(player.roleId)
					.nick(player.getNick())
					.lv(player.getLevel())
					.title(player.getTitle())
					.point(totalScore)
					.source(HatcheryLog.TOTAL)
					.build()
			);
		}
	}

	// check hero in battle
	public boolean isHeroInBattle(int paramHeroId, long paramLordId) {
		for (PvpBattle pvpBattle : getPvpBattles()) {
			if (pvpBattle == null) {
				continue;
			}

			if (pvpBattle.isHeroInBattle(paramHeroId, paramLordId)) {
				return true;
			}
		}

		return false;
	}


	public LinkedList<PvpBattle> getPvpBattles() {
		WorldData worldData = getWorldData();
		return worldData.getPvpBattles();
	}

	public PvpBattle getPvpBattle(int placeId) {
		WorldData worldData = getWorldData();
		return worldData.getPvpBattle(placeId);
	}


	public CommonPb.PvpHero.Builder wrapPvpHero(PvpHero pvpHero) {
		CommonPb.PvpHero.Builder builder = CommonPb.PvpHero.newBuilder();
		builder.setHeroId(pvpHero.getHeroId());
		builder.setRebornTimes(pvpHero.getRebornTimes());
		builder.setMutilKill(pvpHero.getMutilKill());
		builder.setPlaceId(pvpHero.getPlaceId());
		long lordId = pvpHero.getLordId();
		int heroId = pvpHero.getHeroId();
		Player player = playerManager.getPlayer(lordId);
		if (player != null) {
			Hero hero = player.getHero(heroId);
			if (hero != null) {
				builder.setSoldierNum(hero.getCurrentSoliderNum());
			}
		}

		builder.setDefenceTimes(pvpHero.getDefenceTimes());
		builder.setAttackTimes(pvpHero.getAttackTimes());
		builder.setSoloTimes(pvpHero.getSoloTimes());
		builder.setDeadTime(pvpHero.getDeadTime());
		PvpBattle pvpBattle = getPvpBattle(pvpHero.getPlaceId());
		if (pvpBattle != null) {
			builder.setBattleIndex(pvpBattle.getHeroIndex(pvpHero));
		}
		builder.setFreeeRebornTimes(pvpHero.getFreeeRebornTimes());
		return builder;
	}

	public CommonPb.PvpRankInfo.Builder wrapPvpRank(RankPvpHero pvpRankInfo, int rank) {
		CommonPb.PvpRankInfo.Builder builder = CommonPb.PvpRankInfo.newBuilder();
		builder.setRank(rank);
		builder.setKillNum(pvpRankInfo.getMutilKill());
		long lordId = pvpRankInfo.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player != null) {
			builder.setCountry(player.getCountry());
			builder.setNick(player.getNick());
		}
		builder.setHeroId(pvpRankInfo.getHeroId());
		builder.setScore(staticWorldMgr.getPvpScore(rank));
		builder.setLordId(pvpRankInfo.getLordId());
		return builder;
	}

	public void cacPlayerSoldier(Team team, Player player) {
		List<BattleEntity> battleEntities = team.getAllEnities();
		for (BattleEntity battleEntity : battleEntities) {
			if (battleEntity.getLordId() != player.roleId) {
				continue;
			}

			int id = battleEntity.getEntityId();
			Hero hero = player.getHero(id);
			if (hero == null) {
				LogHelper.CONFIG_LOGGER.error("hero is null!");
				continue;
			}

			hero.setCurrentSoliderNum(battleEntity.getRealCurSoldierNum());
		}
	}

	// 战斗逻辑检查
	public void warCheckLogic() {
		pvpPkInfos.clear();
		long now = System.currentTimeMillis();
		LinkedList<PvpBattle> battles = getPvpBattles();
		for (PvpBattle pvpBattle : battles) {
			if (refreshPvpBattle(pvpBattle)) {
				continue;
			}
			checkPvpBattle(pvpBattle, now);
		}
		checkDeadHero(now);
	}

	/**
	 * 对中心位置的容错
	 *
	 * @param pvpBattle
	 * @return
	 */
	private boolean refreshPvpBattle(PvpBattle pvpBattle) {
		int pvpCountry = getPvpCountry();
		//进攻方有人 防守方没人
		if (pvpBattle.hasAttacker() && !pvpBattle.hasDefencer()) {
			pvpBattle.getDefenceTeam().addAll(pvpBattle.getAttackTeam());
			pvpBattle.getAttackTeam().clear();
			int defencerCountry = pvpBattle.getDefenceTeam().getFirst().getCountry();
			if (defencerCountry != pvpCountry && pvpBattle.getPlaceId() == 4) {
				setPvpCountry(defencerCountry);
				pvpBattle.setFlag(true);
				return true;
			}
			// 需要同步兵力信息
			pvpBattle.setLastHeroCountry(defencerCountry);
			synPvpSoldierAndExp(pvpBattle);
			//防守方有人 进攻方没人
		} else if (!pvpBattle.hasAttacker() && pvpBattle.hasDefencer()) {
			int defencerCountry = pvpBattle.getDefenceTeam().getFirst().getCountry();
			if (defencerCountry != pvpCountry && pvpBattle.getPlaceId() == 4) {
				setPvpCountry(defencerCountry);
				pvpBattle.setFlag(true);
			}
			pvpBattle.setLastHeroCountry(defencerCountry);
			synPvpSoldierAndExp(pvpBattle);
			return true;
			//进攻方跟防守方的国家一样
		} else if (pvpBattle.hasAttacker() && pvpBattle.hasDefencer()) {
			int attackCountyry = pvpBattle.getAttackTeam().getFirst().getCountry();
			int defencerCountry = pvpBattle.getDefenceTeam().getFirst().getCountry();
			if (attackCountyry == defencerCountry) {
				pvpBattle.getDefenceTeam().addAll(pvpBattle.getAttackTeam());
				pvpBattle.getAttackTeam().clear();
				if (defencerCountry != pvpCountry && pvpBattle.getPlaceId() == 4) {
					setPvpCountry(defencerCountry);
					pvpBattle.setFlag(true);
				}
				pvpBattle.setLastHeroCountry(defencerCountry);
				synPvpSoldierAndExp(pvpBattle);
				return true;
			}

		}
		return false;
	}

	public PvpBattle getCenterBattle() {
		LinkedList<PvpBattle> battles = getPvpBattles();
		int placeId = PvpPlaceId.CENTER;
		if (battles.size() < placeId) {
			return null;
		}

		return battles.get(placeId - 1);
	}

	public int getPvpCountry() {
		WorldData worldData = getWorldData();
		return worldData.getPvpCountry();
	}

	public void setPvpCountry(int pvpCountry) {
		//System.out.println("================================================================" + pvpCountry);
		WorldData worldData = getWorldData();
		worldData.setPvpCountry(pvpCountry);
	}

	public long getPvpPeriod() {
		WorldData worldData = getWorldData();
		return worldData.getPvpPeriod();
	}

	public void setPvpPeriod(long pvpPeriod) {
		WorldData worldData = getWorldData();
		worldData.setPvpPeriod(pvpPeriod);
	}

	public long getPvpEndTime() {
		WorldData worldData = getWorldData();
		Date endTime = new Date(worldData.getActivityEndTime());
		Date pvpEndTime = new Date(worldData.getPvpEndTime());
		if (pvpEndTime.before(endTime)) {
			return worldData.getPvpEndTime();
		} else {
			return worldData.getActivityEndTime();
		}

	}

	public void setPvpEndTime(long pvpEndTime) {
		WorldData worldData = getWorldData();
		worldData.setPvpEndTime(pvpEndTime);
	}


	public void checkCenter() {

		// 检查倒计时结束, 倒计时结束, 通知血战完成
		long now = System.currentTimeMillis();
		long endTime = getPvpEndTime();
		if (endTime <= now) {
			setWorldPvpState(WorldPvpState.END);
			LogHelper.GAME_DEBUG.error("倒计时到，整个血战结束!");
			return;
		}

		PvpBattle pvpBattle = getCenterBattle();
		if (pvpBattle == null) {
			return;
		}
		int country = pvpBattle.getLastHeroCountry();
		if (country == 0) {
			return;
		}

		int lastCountry = getPvpCountry();
		//System.out.println("lastCountry ="+lastCountry+"=================LastHeroCountry ="+country);
		if (!pvpBattle.isFlag()) {
			//if (lastCountry == country) {
			return;
			//}
			//setPvpCountry(country);
		} else {
			pvpBattle.setFlag(false);
		}
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_1);
		//setPvpCountry(country);
		// System.out.println(lastCountry + "==============checkCenter=============" + getPvpCountry());
		// 如果累计时间超过三小时，则不进行时间增加，只改变国家
		long pvpPeriod = getPvpPeriod();
		if (pvpPeriod < TimeHelper.SECOND_MS * staticWorldActPlan.getContinues().get(2)) {
			// 计算已经走的时间
			long goneTime = TimeHelper.SECOND_MS * staticWorldActPlan.getContinues().get(0) - (endTime - now);
			goneTime = Math.max(0, goneTime);
			LogHelper.GAME_DEBUG.error("攻防交替增加的时间=" + goneTime);
			if (goneTime < TimeHelper.SECOND_MS * staticWorldActPlan.getContinues().get(1)) {
				setPvpEndTime(endTime + goneTime);
				pvpPeriod += goneTime;
				setPvpPeriod(pvpPeriod);
			} else {
				setPvpEndTime(endTime + TimeHelper.SECOND_MS * staticWorldActPlan.getContinues().get(1));
				pvpPeriod += TimeHelper.SECOND_MS * staticWorldActPlan.getContinues().get(1);
				setPvpPeriod(pvpPeriod);
			}
		}

		LogHelper.GAME_DEBUG.error("进行攻防转换，重新记录倒计时, 当前累计倒计时时间为:" + pvpPeriod / 1000);

		//TODO ...... 每次攻防转换需要通知所有玩家的当前时间
		synAllPvpEndTime();


	}

	public void handleHeroDead(PvpHero pvpHero, PvpBattle pvpBattle) {
		addDeadHero(pvpHero);
		long timeLimit = TimeHelper.SECOND_MS * staticLimitMgr.getNum(131);
		pvpHero.setDeadTime(System.currentTimeMillis() + timeLimit);
		pvpBattle.removeHero(pvpHero);
		LogHelper.GAME_DEBUG.error("当前英雄死亡, 死亡英雄从战斗踢出 = " + pvpHero.getHeroId());
		synPvpHero(pvpHero);
		LogHelper.GAME_DEBUG.error("当前英雄死亡, 英雄Id = " + pvpHero.getHeroId());
	}

	// 战斗倒计时3秒处理
	private void checkPvpBattle(PvpBattle pvpBattle, long now) {
		if (pvpBattle.hasTwoSides()) {
//            LogHelper.GAME_DEBUG.error("双方有兵力, 开始打斗, placeId =" + pvpBattle.getPlaceId());
//            LogHelper.GAME_DEBUG.error("攻击方人数:" + pvpBattle.getAttackTeam().size());
//            LogHelper.GAME_DEBUG.error("防守方人数:" + pvpBattle.getDefenceTeam().size());
			// 如果战斗没有开始, 且战斗没有进行倒计时
			if (!pvpBattle.isStartBattle() && pvpBattle.getStartedTime() < now) {  // 战斗没有开始
				LogHelper.GAME_DEBUG.error("血战开始后, 有三秒的自由准备时间!");
				pvpBattle.setStartedTime(now + TimeHelper.SECOND_MS * 1);
				pvpBattle.setStartBattle(true);
			} else if (pvpBattle.isStartBattle() && pvpBattle.getStartedTime() >= now) {  // 自由准备时间
				// 自由准备时间
				LogHelper.GAME_DEBUG.error("血战正式开始, 进入自由准备时间!");
			} else if (pvpBattle.isStartBattle() && pvpBattle.getStartedTime() < now) {  // 开始打
				// 开始战斗
				doPvpBattleLogic(pvpBattle);
				pvpBattle.setStartBattle(false);
				LogHelper.GAME_DEBUG.error("血战正式开始, 开始战斗!");
			}
		} else {
			showSoldier(pvpBattle);
			pvpBattle.setStartBattle(false);
		}

		synPvpWarChecker();
	}

	public void showSoldier(PvpBattle pvpBattle) {
//        LogHelper.GAME_DEBUG.error("当前据点没有兵力，没有战斗!" + pvpBattle.getPlaceId());
//        LogHelper.GAME_DEBUG.error("攻击方人数:" + pvpBattle.getAttackTeam().size());
//        LogHelper.GAME_DEBUG.error("防守方人数:" + pvpBattle.getDefenceTeam().size());
//        int attackSoldier = 0;
//        LinkedList<PvpHero> attackTeam = pvpBattle.getAttackTeam();
//        for (PvpHero pvpHero : attackTeam) {
//            attackSoldier += getPvpHeroSoldier(pvpHero);
//        }
//        int defenceSoldier = 0;
//        LinkedList<PvpHero> defenceTeam = pvpBattle.getDefenceTeam();
//        for (PvpHero pvpHero : defenceTeam) {
//            defenceSoldier += getPvpHeroSoldier(pvpHero);
//        }

//        LogHelper.GAME_DEBUG.error("攻击方兵力 = " + attackSoldier + ", 防守方兵力 = " + defenceSoldier);
	}

	// 开始战斗1秒打一场, 从战斗双方
	private void doPvpBattleLogic(PvpBattle pvpBattle) {
		PvpHero attacker = pvpBattle.getAttacker();
		if (attacker == null) {
			LogHelper.CONFIG_LOGGER.error("attacker is null.");
			LogHelper.GAME_DEBUG.error("没有攻击方玩家, 战斗停止!");
			return;
		}

		PvpHero defencer = pvpBattle.getDefencer();
		if (defencer == null) {
			LogHelper.CONFIG_LOGGER.error("attacker is null.");
			LogHelper.GAME_DEBUG.error("没有防守方玩家, 战斗停止!");
			return;
		}

		Player defencePlayer = playerManager.getPlayer(defencer.getLordId());
		if (defencePlayer == null) {
			LogHelper.CONFIG_LOGGER.error("defence player is null.");
			LogHelper.GAME_DEBUG.error("找不到防守方玩家, 战斗停止!");
			return;
		}

		Hero defenceHero = defencePlayer.getHero(defencer.getHeroId());
		if (defenceHero == null) {
			LogHelper.CONFIG_LOGGER.error("defence Hero is null.");
			LogHelper.GAME_DEBUG.error("找不到防守方英雄, 战斗停止!");
			return;
		}

		if (defenceHero.getCurrentSoliderNum() <= 0) {
			LogHelper.CONFIG_LOGGER.error("target hero soldier num <= 0.");
			LogHelper.GAME_DEBUG.error("防守方兵力为0, 无法战斗!");
			return;
		}

		Player attackPlayer = playerManager.getPlayer(attacker.getLordId());
		if (attackPlayer == null) {
			LogHelper.CONFIG_LOGGER.error("attacker player is null");
			LogHelper.GAME_DEBUG.error("找不到攻击方玩家, 无法战斗!");
			return;
		}

		Hero attackHero = attackPlayer.getHero(attacker.getHeroId());
		if (attackHero == null) {
			LogHelper.CONFIG_LOGGER.error("attackHero is null.");
			LogHelper.GAME_DEBUG.error("找不到攻击方玩家的英雄, 无法战斗!");
			return;
		}

		List<Integer> myHeroIds = new ArrayList<Integer>();
		List<Integer> tragetHeroIds = new ArrayList<Integer>();
		myHeroIds.add(attacker.getHeroId());
		tragetHeroIds.add(defencer.getHeroId());

		Team attackerTeam = battleMgr.initPlayerTeam(attackPlayer, myHeroIds, BattleEntityType.HERO);
		Team defenceTeam = battleMgr.initPlayerTeam(defencePlayer, tragetHeroIds, BattleEntityType.HERO);

		SoloInfo defenceSolo = new SoloInfo();
		defenceSolo.setLordName(defencePlayer.getNick());
		defenceSolo.setHeroId(defenceHero.getHeroId());
		SoloInfo attackSolo = new SoloInfo();
		attackSolo.setLordName(attackPlayer.getNick());
		attackSolo.setHeroId(attackHero.getHeroId());

		LogHelper.GAME_DEBUG.error("战斗结束前, 攻击方英雄血量为:" + attackHero.getCurrentSoliderNum());
		LogHelper.GAME_DEBUG.error("战斗结束前, 防守方英雄血量为:" + defenceHero.getCurrentSoliderNum());

		Random random = new Random(System.currentTimeMillis());
		int beforeAttackSoldier = attackHero.getCurrentSoliderNum();
		int beforeDefenceSoldier = defenceHero.getCurrentSoliderNum();
		battleMgr.doTeamBattle(attackerTeam, defenceTeam, random, ActPassPortTaskType.IS_WORLD_WAR);
		//母巢特殊处理
		activityManager.calcuKillAll(attackerTeam, defenceTeam);

		worldManager.caculateTeamKill(attackerTeam, attacker.getLordId());
		worldManager.caculateTeamDefenceKill(defenceTeam);

		cacPlayerSoldier(attackerTeam, attackPlayer);
		cacPlayerSoldier(defenceTeam, defencePlayer);
		int afterDefenceSoldier = defenceHero.getCurrentSoliderNum();
		int afterAttackSoldier = attackHero.getCurrentSoliderNum();
		int defencerLost = beforeDefenceSoldier - afterDefenceSoldier;
		int attackerLost = beforeAttackSoldier - afterAttackSoldier;
		attackSolo.setLost(attackerLost);
		defenceSolo.setLost(defencerLost);
		//记录玩家母巢之战损兵数量
		attackPlayer.getSimpleData().addDieSoldiers(attackerLost);
		defencePlayer.getSimpleData().addDieSoldiers(defencerLost);

		if (attackerLost > 0) {
			activityManager.updActPerson(attackPlayer, ActivityConst.ACT_SOILDER_RANK, attackerLost, 0);
			achievementService.addAndUpdate(attackPlayer,AchiType.AT_25,attackerLost);
		}
		if (defencerLost > 0) {
			activityManager.updActPerson(defencePlayer, ActivityConst.ACT_SOILDER_RANK, defencerLost, 0);
			achievementService.addAndUpdate(defencePlayer,AchiType.AT_25,defencerLost);
		}

		handlePvpKill(attacker, defencerLost);
		handlePvpKill(defencer, attackerLost);

		LogHelper.GAME_DEBUG.error("战斗结束后, 攻击方英雄血量为:" + attackHero.getCurrentSoliderNum());
		LogHelper.GAME_DEBUG.error("战斗结束后, 防守方英雄血量为:" + defenceHero.getCurrentSoliderNum());

		if (defencerLost != 0 && attackerLost != 0) {
			PvpPkInfo pvpPkInfo = new PvpPkInfo();
			pvpPkInfo.setAttack(attackSolo);
			pvpPkInfo.setDefence(defenceSolo);
			addPvpBattleScore(defencerLost);
			addPvpBattleScore(attackerLost);

			pvpPkInfos.add(pvpPkInfo);
		} else {
			LogHelper.CONFIG_LOGGER.error("attacker kill num <= 0 or defencer kill num <= 0.");
		}

		// remove pvp hero
		if (attackHero.getCurrentSoliderNum() <= 0) {
			handleHeroDead(attacker, pvpBattle);
		}

		if (defenceHero.getCurrentSoliderNum() <= 0) {
			handleHeroDead(defencer, pvpBattle);
		}

		checkPvpBattle(pvpBattle);
		// 需要同步兵力信息
		synPvpSoldierAndExp(pvpBattle);
	}

	// 进行攻防转换
	public void checkPvpBattle(PvpBattle pvpBattle) {
		// 如果有防守方
		if (pvpBattle.hasDefencer()) {
			return;
		}

		// 如果没有攻击方
		if (!pvpBattle.hasAttacker()) {
			return;
		}

		// 检查第一个
		PvpHero pvpHero = pvpBattle.getAttacker();
		if (pvpHero == null) {
			return;
		}

		int country = pvpHero.getCountry();
		LinkedList<PvpHero> attackTeam = new LinkedList<>();
		LinkedList<PvpHero> defenceTeam = new LinkedList<>();
		LinkedList<PvpHero> team = pvpBattle.getAttackTeam();
		for (PvpHero hero : team) {
			if (hero.getCountry() == country) {
				defenceTeam.add(hero);
			} else {
				attackTeam.add(hero);
			}
		}

		// 先清除当前的攻击方
		pvpBattle.clearAttacker();
		pvpBattle.setAttackTeam(attackTeam);
		pvpBattle.setDefenceTeam(defenceTeam);

		if (pvpBattle.getPlaceId() == 4) {
			//System.out.println("=====================pvpBattle.getPlaceId()====================");
			pvpBattle.setLastHeroCountry(country);
			setPvpCountry(country);
			pvpBattle.setFlag(true);
		}

	}

	private int getPvpHeroSoldier(PvpHero pvpHero) {
		long lordId = pvpHero.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("player is null.");
			return 0;
		}

		Hero hero = player.getHero(pvpHero.getHeroId());
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.error("hero is null.");
			return 0;
		}

		return hero.getCurrentSoliderNum();
	}

	public CommonPb.PvpSoldier wrapPvpSoldier(PvpBattle pvpBattle) {
		CommonPb.PvpSoldier.Builder builder = CommonPb.PvpSoldier.newBuilder();
		builder.setPlaceId(pvpBattle.getPlaceId());
		int attackSoldier = 0;
		LinkedList<PvpHero> attackTeam = pvpBattle.getAttackTeam();
		for (PvpHero pvpHero : attackTeam) {
			attackSoldier += getPvpHeroSoldier(pvpHero);
		}
		builder.setAttackSoldier(attackSoldier);
		int defenceSoldier = 0;
		LinkedList<PvpHero> defenceTeam = pvpBattle.getDefenceTeam();
		for (PvpHero pvpHero : defenceTeam) {
			defenceSoldier += getPvpHeroSoldier(pvpHero);
		}
		builder.setDefenceSoldier(defenceSoldier);
		if (!defenceTeam.isEmpty()) {
			PvpHero pvpHero = defenceTeam.getFirst();
			if (pvpHero != null) {
				builder.setDefenceCountry(pvpHero.getCountry());
			}
		} else {
			builder.setDefenceCountry(pvpBattle.getLastHeroCountry());
		}
		builder.setDefenceNum(pvpBattle.getDefenceNum());
		//builder.setAttackerNum(pvpBattle.getAttackerNum());

		return builder.build();
	}

	public List<CommonPb.PvpSoldier> wrapAllSoldier() {
		List<CommonPb.PvpSoldier> builders = new ArrayList<CommonPb.PvpSoldier>();
		LinkedList<PvpBattle> battles = getPvpBattles();
		for (PvpBattle pvpBattle : battles) {
			builders.add(wrapPvpSoldier(pvpBattle));
		}
		return builders;
	}

	public LinkedList<PvpPkInfo> getPvpPkInfos() {
		return pvpPkInfos;
	}

	public void setPvpPkInfos(LinkedList<PvpPkInfo> pvpPkInfos) {
		this.pvpPkInfos = pvpPkInfos;
	}

	public void clear() {
		pvpPkInfos.clear();
	}

	// 下次再参加的时候重置
	public void addPlayer(Player player) {
		HashSet<Long> attenders = getAttenders();
		if (attenders.contains(player.roleId)) {
			return;
		}
		player.clearMassHeroes();
		player.clearPvpHeroes();
		player.setTotalKillNum(0);
		SimpleData simpleData = player.getSimpleData();
		simpleData.clearBanquetInfo();  // 清除国宴数据
		LogHelper.GAME_DEBUG.error("addPlayer: 清除所有玩家的集结英雄和Pvp英雄, 以及英雄.");
		attenders.add(player.roleId);
	}

	// 同步兵力情况
	public void synPvpSoldier(PvpBattle pvpBattle) {
		CommonPb.PvpSoldier pvpSoldier = wrapPvpSoldier(pvpBattle);
		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.addPvpSoldier(pvpSoldier);
		HashSet<Long> attenders = getAttenders();
		for (Long lordId : attenders) {
			if (lordId == null) {
				continue;
			}
			Player player = playerManager.getPlayer(lordId);
			if (player == null ||
				!player.isLogin ||
				player.getChannelId() == -1) {
				continue;
			}

			SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
				SynPvpInfoRq.ext,
				builder.build());
		}
	}


	// 同步兵力情况
	public void synPvpAllSoldier(PvpBattle pvpBattle) {
		CommonPb.PvpSoldier pvpSoldier = wrapPvpSoldier(pvpBattle);
		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.addPvpSoldier(pvpSoldier);
		builder.setEndTime(getPvpEndTime());
		Iterator<Long> it = set.iterator();
		while (it.hasNext()) {
			long playerId = it.next();
			Player player = playerManager.getPlayer(playerId);
			if (player == null ||
				!player.isLogin ||
				player.getChannelId() == -1) {
				continue;
			}
			SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
				SynPvpInfoRq.ext,
				builder.build());
		}
	}


	public long getPvpBattleScore() {
		WorldData worldData = getWorldData();
		return worldData.getPvpBattleScore();
	}

	public void synPvpSoldierAndExp(PvpBattle pvpBattle) {
		CommonPb.PvpSoldier pvpSoldier = wrapPvpSoldier(pvpBattle);
		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.addPvpSoldier(pvpSoldier);
		builder.setPvpBattleScore(getPvpBattleScore());
		HashSet<Long> attenders = getAttenders();
		for (Long lordId : attenders) {
			if (lordId == null) {
				continue;
			}
			Player player = playerManager.getPlayer(lordId);
			if (player == null ||
				!player.isLogin ||
                player.getChannelId() == -1) {
				continue;
			}

			SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
				SynPvpInfoRq.ext,
				builder.build());
		}
	}

	public void synPvpWarChecker() {
		if (pvpPkInfos.isEmpty()) {
			return;
		}
		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		HashSet<Long> attenders = getAttenders();
		for (PvpPkInfo pkInfo : pvpPkInfos) {
			builder.addPvpPkInfo(pkInfo.wrapPb());
		}

		for (Long lordId : attenders) {
			if (lordId == null) {
				continue;
			}
			Player player = playerManager.getPlayer(lordId);
			if (player == null ||
				!player.isLogin ||
                player.getChannelId() == -1) {
				continue;
			}

			SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
				SynPvpInfoRq.ext,
				builder.build());
		}
	}

	// 同步两方区域兵力
	public void synBothPvpSoldier(PvpBattle pvpBattle, PvpBattle targetBattle) {
		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.addPvpSoldier(wrapPvpSoldier(pvpBattle));
		builder.addPvpSoldier(wrapPvpSoldier(targetBattle));
		builder.setEndTime(getPvpEndTime());
		HashSet<Long> attenders = getAttenders();
		for (Long lordId : attenders) {
			if (lordId == null) {
				continue;
			}
			Player player = playerManager.getPlayer(lordId);
			if (player == null ||
				!player.isLogin ||
                player.getChannelId() == -1) {
				continue;
			}

			SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
				SynPvpInfoRq.ext,
				builder.build());
			logger.error("给玩家同步兵力....");
			logger.error("双方兵力为...." + builder.build());
		}

		if (attenders.isEmpty()) {
			logger.error("没有玩家参加血战!!!");
		}
	}


	// 同步玩家英雄信息
	private void synPvpHero(PvpHero pvpHero) {
		Player player = playerManager.getPlayer(pvpHero.getLordId());
		if (player == null || !player.isLogin ||player.getChannelId() == -1) {
			return;
		}

		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.setPvpHero(wrapPvpHero(pvpHero));
		SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
			SynPvpInfoRq.ext,
			builder.build());
	}


	private void addDeadHero(PvpHero pvpHero) {
		boolean found = false;
		LinkedList<PvpHero> deadHeros = getDeadHeros();
		for (PvpHero elem : deadHeros) {
			if (elem.isEqual(pvpHero)) {
				found = true;
				break;
			}
		}

		if (found) {
			LogHelper.CONFIG_LOGGER.error("pvpHero had added in, heroId = " + pvpHero.getHeroId() + ", lordId = " + pvpHero.getLordId());
			return;
		}

		deadHeros.add(pvpHero);
		LogHelper.GAME_DEBUG.error("当前英雄死亡后，加入死亡列表.., heroId =" + pvpHero.getHeroId());
	}

	// 检查所有英雄有没有挂掉,如果挂掉则从玩家身上删除pvpHero, soldierNum <= 0 && deadTime <= now, delete
	private void checkDeadHero(long now) {
		LinkedList<PvpHero> deadHeros = getDeadHeros();
		Iterator<PvpHero> iterator = deadHeros.iterator();
		while (iterator.hasNext()) {
			PvpHero pvpHero = iterator.next();
			if (pvpHero == null) {
				continue;
			}

			// 且血量大于0
			Player player = playerManager.getPlayer(pvpHero.getLordId());
			if (player == null) {
				continue;
			}
			Hero hero = player.getHero(pvpHero.getHeroId());
			if (hero == null) {
				continue;
			}

			if (pvpHero.getDeadTime() <= now && hero.getCurrentSoliderNum() <= 0) {
				removePlayerHero(pvpHero);
				iterator.remove();
			}
		}

	}

	// 同步玩家删除英雄信息
	private void synRemovePvpHero(PvpHero pvpHero) {
		Player player = playerManager.getPlayer(pvpHero.getLordId());
		if (player == null || !player.isLogin || player.getChannelId() == -1) {
			return;
		}

		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.setRemoveHero(wrapPvpHero(pvpHero));
		SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
			SynPvpInfoRq.ext,
			builder.build());
	}

	// 英雄回城
	private void removePlayerHero(PvpHero pvpHero) {
		Player player = playerManager.getPlayer(pvpHero.getLordId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("player is null.");
			return;
		}

		synRemovePvpHero(pvpHero);
		player.removePvpHero(pvpHero);
	}


	public boolean isBanquetOver() {
		long now = System.currentTimeMillis();
		return getBanquetEndTime() <= now;
	}

	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}


	// 排行比较器
	class ComparatorPvpHero implements Comparator<RankPvpHero> {

		@Override
		public int compare(RankPvpHero o1, RankPvpHero o2) {
			// 按照连杀来排
            if (o1.getMutilKill() < o2.getMutilKill()) {
                return 1;
            }
            if (o1.getMutilKill() > o2.getMutilKill()) {
                return -1;
            }

			return 0;
		}
	}


	// 处理连杀排行榜: 取玩家内部最高的连杀
	// HashMap<Integer, RandkPvpHero>
	public void checkRank(PvpHero pvpHero) {
		// 根据连杀数进行排行, 先更新，再排序
		// 取出最好的英雄
		// 先判断当前pvpHero是否在rankList,如果不在，则进行插入
		// 取出玩家最好的英雄, 判断bestHero在不在rankList,
		// 取出玩家最好的英雄，如果排行榜没有这个玩家，则更新当前英雄到排行榜
		// 如果有，则只需更新排行榜和玩家的英雄信息
		PvpHero bestHero = getPlayerMutiKill(pvpHero);
		if (bestHero == null) {
			LogHelper.CONFIG_LOGGER.error("bestHero is null");
			return;
		}

		RankPvpHero rankHero = getPvpHeroRank(bestHero);
		LinkedList<RankPvpHero> rankList = getRankList();
		if (rankHero == null) {
			rankList.add(new RankPvpHero(bestHero));
			Collections.sort(rankList, new ComparatorPvpHero());
		} else {
			if (rankHero.getMutilKill() < bestHero.getMutilKill()) {
				rankHero.setHeroId(bestHero.getHeroId());
				rankHero.setMutilKill(bestHero.getMutilKill());
			}
			Collections.sort(rankList, new ComparatorPvpHero());
		}
	}

	public void checkSameRank() {
		LinkedList<RankPvpHero> rankList = getRankList();
		HashMap<Long, RankPvpHero> rankMap = new HashMap<Long, RankPvpHero>();
		for (RankPvpHero rankPvpHero : rankList) {
			rankMap.put(rankPvpHero.getLordId(), rankPvpHero);
		}
		rankList.clear();
		rankList.addAll(rankMap.values());
	}

	public RankPvpHero getPvpHeroRank(PvpHero bestHero) {
		LinkedList<RankPvpHero> rankList = getRankList();
		for (RankPvpHero elem : rankList) {
			if (elem.getLordId() == bestHero.getLordId()) {
				return elem;
			}
		}
		return null;
	}

	// 获取玩家最高连杀
	public PvpHero getPlayerMutiKill(PvpHero pvpHero) {
		Player player = playerManager.getPlayer(pvpHero.getLordId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("player is null!");
			return null;
		}
		return player.getMutilKillHero();
	}

	// update
	public void handlePvpKill(PvpHero pvpHero, int killNum) {
		if (killNum <= 0) {
			LogHelper.CONFIG_LOGGER.error("killNum <= 0");
			return;
		}
		pvpHero.setMutilKill(pvpHero.getMutilKill() + killNum);
		Player player = playerManager.getPlayer(pvpHero.getLordId());
		if (player != null) {
			player.setTotalKillNum(player.getTotalKillNum() + killNum);
		}

		synPvpKill(pvpHero);
		checkRank(pvpHero);
	}

	public void synPvpKill(PvpHero pvpHero) {
		Player player = playerManager.getPlayer(pvpHero.getLordId());
		if (player == null || !player.isLogin || player.getChannelId() == -1) {
			return;
		}

		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.setPvpHero(wrapPvpHero(pvpHero));
		builder.setTotalKill(player.getTotalKillNum());
		SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
			SynPvpInfoRq.ext,
			builder.build());
	}

	public boolean isAllDig(int country, List<Integer> papers) {
		HashMap<Integer, HashMap<Integer, DigInfo>> digPapers = getDigPapers();
		HashMap<Integer, DigInfo> digInfoMap = digPapers.get(country);
		if (digInfoMap == null) {
			LogHelper.CONFIG_LOGGER.error("digInfoMap is null!");
			return false;
		}

		for (Integer itemId : papers) {
			if (!digInfoMap.containsKey(itemId)) {
				return false;
			}
		}

		return true;
	}

	public void updateDig(int country, int itemId, long lordId) {
		HashMap<Integer, HashMap<Integer, DigInfo>> digPapers = getDigPapers();
		HashMap<Integer, DigInfo> digInfoMap = digPapers.get(country);
		if (digInfoMap == null) {
			LogHelper.CONFIG_LOGGER.error("digInfoMap is null!");
			return;
		}

		if (digInfoMap.containsKey(itemId)) {
			return;
		}

		digInfoMap.put(itemId, new DigInfo(lordId, itemId));
	}

	public List<Integer> getLeftPapers(int country, List<Integer> papers) {
		List<Integer> leftPapers = new ArrayList<Integer>();
		HashMap<Integer, HashMap<Integer, DigInfo>> digPapers = getDigPapers();
		HashMap<Integer, DigInfo> digInfoMap = digPapers.get(country);
		if (digInfoMap == null) {
			LogHelper.CONFIG_LOGGER.error("digInfoMap is null!");
			return leftPapers;
		}

		for (Integer itemId : papers) {
			if (!digInfoMap.containsKey(itemId)) {
				leftPapers.add(itemId);
			}
		}

		return leftPapers;
	}

	public HashMap<Integer, DigInfo> getDigInfo(int country) {
		HashMap<Integer, HashMap<Integer, DigInfo>> digPapers = getDigPapers();
		return digPapers.get(country);
	}

	private void synPvpScore(Player player) {
		if (player == null || !player.isLogin || player.getChannelId() == -1) {
			return;
		}

		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.setPvpScore(player.getPvpScore());
		SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
			SynPvpInfoRq.ext,
			builder.build());
	}

	public void clearPlayerPvpInfo() {
		Map<Long, Player> playerMap = playerManager.getPlayers();
		for (Player player : playerMap.values()) {
			player.clearPvpBattle();
			player.getSimpleData().setDieSoldiers(0);
			player.getSimpleData().setThisTimePvpScore(0);
		}
	}

	public void synPvpEndTime(SynPvpInfoRq.Builder builder, Player player) {
		if (player == null || !player.isLogin || player.getChannelId() == -1) {
			return;
		}

		SynHelper.synMsgToPlayer(player, SynPvpInfoRq.EXT_FIELD_NUMBER,
			SynPvpInfoRq.ext,
			builder.build());
	}

	public void synAllPvpEndTime() {
		HashSet<Long> attenders = getAttenders();
		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.setEndTime(getPvpEndTime());
		for (Long lordId : attenders) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			synPvpEndTime(builder, player);
		}
	}

	public void synAllEndTime() {
		SynPvpInfoRq.Builder builder = SynPvpInfoRq.newBuilder();
		builder.setEndTime(getPvpEndTime());
		builder.setBanquetEndTime(getBanquetEndTime());
		for (Player player : playerManager.getPlayers().values()) {
			if (player == null) {
				continue;
			}
			synPvpEndTime(builder, player);
		}
	}


	public void addPvpBattleScore(int lost) {
		WorldData worldData = getWorldData();
		worldData.addBattleScore(lost);
	}

	public void checkPlayerHero(Player player) {
		Iterator<PvpHero> it = player.getPvpHeroMap().values().iterator();
		while (it.hasNext()) {
			PvpHero pvpHero = it.next();
			if (pvpHero == null) {
				continue;
			}
			int placeId = pvpHero.getPlaceId();
			PvpBattle pvpBattle = getPvpBattle(placeId);
			if (!pvpBattle.hasPvpHero(pvpHero)) {
				it.remove();
			}
		}
	}


	public Set getSet() {
		return set;
	}
}
