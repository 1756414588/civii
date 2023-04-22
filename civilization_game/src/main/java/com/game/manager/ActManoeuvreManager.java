package com.game.manager;

import com.game.constant.BattleEntityType;
import com.game.constant.GameError;
import com.game.constant.MailId;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dao.p.ManoeuvreDao;
import com.game.dataMgr.StaticManoeuvreMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.Award;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.BattleProperty;
import com.game.domain.p.Hero;
import com.game.domain.p.Manoeuvre;
import com.game.domain.p.Team;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticManoeuvreMatch;
import com.game.domain.s.StaticWorldActPlan;
import com.game.pb.ActManoeuvrePb.SynManoeuvreChangeLineRq;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.ManoeuverReport;
import com.game.pb.CommonPb.TwoInt;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.Pair;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.fight.manoeuvre.ManoeuvreConst;
import com.game.worldmap.fight.manoeuvre.ManoeuvreCourse;
import com.game.worldmap.fight.manoeuvre.ManoeuvreData;
import com.game.worldmap.fight.manoeuvre.ManoeuvreDetail;
import com.game.worldmap.fight.manoeuvre.ManoeuvreFighter;
import com.game.worldmap.fight.manoeuvre.ManoeuvreRound;
import com.game.worldmap.fight.manoeuvre.ManoeuvreScore;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @version 创建时间：2021-12-20 下午17:36:23
 * @Desc 【沙盘演武】管理类
 */
@Component
@Getter
public class ActManoeuvreManager {

	// 历史信息
	private List<ManoeuvreData> history = new ArrayList<>();

	private ManoeuvreData manoeuvreData;
	@Autowired
	private StaticManoeuvreMgr staticManoeuvreMgr;
	@Autowired
	private ManoeuvreDao manoeuvreDao;
	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;
	@Autowired
	private BattleMgr battleMgr;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private HeroManager heroManager;
	@Autowired
	private WorldTargetManager worldTargetManager;
	// 连胜记录
	private Map<Integer, Integer> victorys = new HashMap<>();
	private Map<Integer, Function<WorldActPlan, Boolean>> actions = new HashMap<>();

	public void init() {
		List<Manoeuvre> manoeuvreList = manoeuvreDao.selectTopList();

		for (int i = 0; i < manoeuvreList.size(); i++) {
			Manoeuvre e = manoeuvreList.get(i);
			ManoeuvreData top = new ManoeuvreData(e);
			if (manoeuvreData == null) {
				manoeuvreData = top;
			}
			history.add(top);
		}

		calcVictory();
		initAction();

		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_14);
		if (worldActPlan == null) {
			return;
		}

		// 合服清理沙盘演武数据,将活动设置为未开启
		if (manoeuvreList == null || manoeuvreList.isEmpty()) {
			worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
			return;
		}

		// 重启保护
		if (manoeuvreData.getStatus() == ManoeuvreConst.STATUS_APPLY) {//准备阶段
			if (worldActPlan.getState() == WorldActPlanConsts.OPEN) {
				worldActPlan.setState(WorldActPlanConsts.PREHEAT);
			}
		}

		LogHelper.GAME_LOGGER.info("开启【沙盘演武】 活动状态:{} 预热时间:{} 开启时间:{} 结束时间:{} 配置数据阶段:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()), manoeuvreData.getStatus());
	}

	private void initAction() {
		actions.put(WorldActPlanConsts.NOE_OPEN, this::handlerNotOpen);
		actions.put(WorldActPlanConsts.PREHEAT, this::handlerPrepare);
		actions.put(WorldActPlanConsts.OPEN, this::handerOpen);
		actions.put(WorldActPlanConsts.DO_END, this::handlerDoEnd);
	}

	public void initWorldActPlan(StaticWorldActPlan staticWorldActPlan) {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = new WorldActPlan();
		worldActPlan.setId(staticWorldActPlan.getId());
		worldActPlan.setTargetSuccessTime(System.currentTimeMillis());
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		worldData.getWorldActPlans().put(worldActPlan.getId(), worldActPlan);

		// 如果今天是礼拜五礼拜六礼拜天,则开启时间为下一周
		int openWeek = staticWorldActPlan.getOpenWeek();
		int curWeek = TimeHelper.getCurWeek();
		if (curWeek == 5 || curWeek == 6 || curWeek == 7) {//如果完成时间
			openWeek = 1;
		}

		long openTime = TimeHelper.getTime(worldActPlan.getTargetSuccessTime(), openWeek, staticWorldActPlan.getWeekTime(),
			staticWorldActPlan.getTime());
		long preheat = TimeHelper.getTime(new Date(openTime), staticWorldActPlan.getPreheat());
		long endTime = openTime + staticWorldActPlan.getEndTime() * 60 * 1000L;
		worldActPlan.setOpenTime(openTime);
		worldActPlan.setPreheatTime(preheat);
		worldActPlan.setEndTime(endTime);
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);

		LogHelper.GAME_LOGGER.info("初始化【沙盘演武】 预热时间:{} 开启时间:{} 结束时间:{} ", DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
	}

	/**
	 * 计算国家连胜
	 */
	private void calcVictory() {
		Pair<Integer, Integer> victory = new Pair<>(0, 0);
		for (int i = 0; i < history.size(); i++) {
			ManoeuvreData e = history.get(i);
			if (e.getWiner() == 0) {
				continue;
			}

			int country = e.getWiner();
			int left = victory.getLeft();
			if (left != country) {//与上个记录不同
				victory.setLeft(country);
				victory.setRight(1);
			} else {
				victory.setRight(victory.getRight() + 1);
			}

			if (victorys.containsKey(country)) {
				int count = victorys.get(country);
				if (count < victory.getRight()) {
					victorys.put(country, victory.getRight());
				}
			} else {
				victorys.put(country, victory.getRight());
			}
		}
	}


	public ManoeuvreData getManoeuvreData(long id) {
		return history.stream().filter(e -> e.getKeyId() == id).findFirst().get();
	}

	/**
	 * 检测活动
	 *
	 * @param worldActPlan
	 */
	public void checkActPlan(WorldActPlan worldActPlan) {
		if (worldActPlan == null) {
			return;
		}

		if (actions.containsKey(worldActPlan.getState())) {
			actions.get(worldActPlan.getState()).apply(worldActPlan);
		}
	}

	/**
	 * 活动未开启状态时刻处理
	 *
	 * @param worldActPlan
	 * @return
	 */
	private boolean handlerNotOpen(WorldActPlan worldActPlan) {
		long currentTime = System.currentTimeMillis();
		if (worldActPlan.getPreheatTime() > currentTime) {//报名尚未开始
			return false;
		}

		worldActPlan.setState(WorldActPlanConsts.PREHEAT);
		initManoeuvreFuture(worldActPlan).thenAcceptAsync(e -> {
			if (e == null) {
				return;
			}
			Manoeuvre manoeuvre = new Manoeuvre(e);
			manoeuvreDao.insertManoeuvre(manoeuvre);
			this.manoeuvreData = new ManoeuvreData(manoeuvre);
			manoeuvreData.setStatus(ManoeuvreConst.STATUS_APPLY);
			history.add(0, manoeuvreData);// 添加到首位
			synActivityOpen();
		});
		LogHelper.GAME_LOGGER.info("【沙盘演武】开启预热.报名开始. 活动状态:{} 预热时间:{} 开启时间:{} 结束时间:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
		return true;
	}

	/**
	 * 报名阶段处理
	 *
	 * @param worldActPlan
	 * @return
	 */
	private boolean handlerPrepare(WorldActPlan worldActPlan) {
		long currentTime = System.currentTimeMillis();
		if (worldActPlan.getOpenTime() > currentTime) {//报名阶段尚未结束
			return false;
		}
		worldActPlan.setState(WorldActPlanConsts.OPEN);
		if (initFighters()) {// 初始化参战队伍信息,每条线取战力最高的25人
			this.manoeuvreData.setStatus(ManoeuvreConst.STATUS_PREPARE);
			this.manoeuvreData.setStage(ManoeuvreConst.STAGE_ONE);// 准备阶段到开战属于第一轮
			manoeuvreDao.updateManoeuvre(manoeuvreData.createManoeuvre());
			synManoeuvre();

			// 报名参战选手邮件
			Map<Integer, List<CommonPb.ManoeuverApply>> applyMap = manoeuvreData.getFighterPb();
			manoeuvreData.getApplyMap().values().forEach(e -> {
				Player target = playerManager.getPlayer(e.getPlayerId());
				if (target == null) {
					return;
				}
				List<CommonPb.ManoeuverApply> applyList = applyMap.get(target.getCountry());
				playerManager.sendManoeuvreMail(target, null, applyList, MailId.MANOEUVRE_APPLY);
			});
		} else {
			manoeuvreData.setStatus(ManoeuvreConst.STATUS_END);//活动结束
			manoeuvreData.setStage(ManoeuvreConst.STAGE_END);//结算阶段
			worldActPlan.setState(WorldActPlanConsts.DO_END);// 活动进入结算阶段
			synActivityDoEnd();
		}
		return true;
	}

	/**
	 * 进行阶段处理
	 *
	 * @param worldActPlan
	 */
	private boolean handerOpen(WorldActPlan worldActPlan) {
		int status = manoeuvreData.getStatus();
		if (status != ManoeuvreConst.STATUS_PREPARE) {
			return false;
		}
		int stage = manoeuvreData.getStage();
		if (stage < ManoeuvreConst.STAGE_ONE || stage > ManoeuvreConst.STAGE_THRE) {
			return false;
		}

		long currentTime = System.currentTimeMillis();
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_14);

		long period = staticWorldActPlan.getContinues().get(0) * ManoeuvreConst.SECOND;// 每轮的间隔时间
		long stageEndTime = worldActPlan.getOpenTime() + period * stage;
		if (currentTime < stageEndTime) {// 未到回合结束时间
			return false;
		}

		manoeuvreData.setStage(stage + 1);
		processStage(stage).thenAcceptAsync(e -> {
			if (e == null) {
				return;
			}
			// 左侧积分大于右侧积分
			int battleRs = e.getBattleResult();
			ManoeuverReport manoeuverReport = e.getReportPb();
			if (battleRs == ManoeuvreConst.RESULT_QUITE) {
				String[] leftParam = {String.valueOf(e.getCountryLeft()), String.valueOf(stage), String.valueOf(e.getCountryRight()), String.valueOf(1)};
				String[] rightParam = {String.valueOf(e.getCountryRight()), String.valueOf(stage), String.valueOf(e.getCountryLeft()), String.valueOf(1)};
				playerManager.getPlayers().forEach((k, v) -> {
					if (v.getCountry() == e.getCountryLeft()) {
						playerManager.sendManoeuvreMail(v, manoeuverReport, null, MailId.MANOEUVRE_QUITE, leftParam);
					} else if (v.getCountry() == e.getCountryRight()) {
						playerManager.sendManoeuvreMail(v, manoeuverReport, null, MailId.MANOEUVRE_QUITE, rightParam);
					}
				});
			} else {
				int winCountry = battleRs == ManoeuvreConst.RESULT_LEFT_WIN ? e.getCountryLeft() : e.getCountryRight();
				int lostCountry = battleRs == ManoeuvreConst.RESULT_LEFT_WIN ? e.getCountryRight() : e.getCountryLeft();
				String[] winParam = {String.valueOf(winCountry), String.valueOf(stage), String.valueOf(lostCountry), String.valueOf(3)};
				String[] lostParam = {String.valueOf(lostCountry), String.valueOf(stage), String.valueOf(winCountry), String.valueOf(0)};
				playerManager.getPlayers().forEach((k, v) -> {
					if (v.getCountry() == winCountry) {
						playerManager.sendManoeuvreMail(v, manoeuverReport, null, MailId.MANOEUVRE_WIN, winParam);
					} else if (v.getCountry() == lostCountry) {
						playerManager.sendManoeuvreMail(v, manoeuverReport, null, MailId.MANOEUVRE_LOST, lostParam);
					}
				});
			}

			// 个人杀敌数积分奖励邮件
			Map<Long, Integer> beatMap = e.getBeatMap();
			for (List<ManoeuvreFighter> fighters : e.getFights().values()) {
				for (ManoeuvreFighter fighter : fighters) {
					// 该国本轮没有参与
					if (fighter.getCountry() != e.getCountryRight() && fighter.getCountry() != e.getCountryLeft()) {
						continue;
					}
					Player player = playerManager.getPlayer(fighter.getPlayerId());
					if (player == null) {
						continue;
					}

					if (beatMap.containsKey(fighter.getPlayerId())) {
						int beatNumber = beatMap.get(fighter.getPlayerId());
						List<Award> beatAwardList = staticManoeuvreMgr.getAward(ManoeuvreConst.TYPE_RANK_PERSON, beatNumber);
						playerManager.sendAttachMail(player, beatAwardList, MailId.MANOEUVRE_PERSON_AWARD, String.valueOf(beatNumber));
					} else {
						List<Award> beatAwardList = staticManoeuvreMgr.getAward(ManoeuvreConst.TYPE_RANK_PERSON, ManoeuvreConst.TYPE_RANK_PERSON_MAX);
						playerManager.sendAttachMail(player, beatAwardList, MailId.MANOEUVRE_EMPTY_AWARD);
					}
				}
			}
			manoeuvreDao.updateManoeuvre(manoeuvreData.createManoeuvre());
		});

		// 全部回合结束
		if (manoeuvreData.getStage() >= ManoeuvreConst.STAGE_END) {
			manoeuvreData.setStatus(ManoeuvreConst.STATUS_END);
			worldActPlan.setState(WorldActPlanConsts.DO_END);
			doCalc().thenAcceptAsync(e -> {
				if (e == null) {
					return;
				}
				manoeuvreDao.updateManoeuvre(e.createManoeuvre());
				// 发放最终排名邮件
				Map<Integer, Integer> countryRanks = new HashMap<>();
				for (int i = 1; i <= e.getScoreList().size(); i++) {
					ManoeuvreScore score = e.getScoreList().get(i - 1);
					countryRanks.put(score.getCountry(), i);
				}
				playerManager.getPlayers().forEach((k, v) -> {
					if (!countryRanks.containsKey(v.getCountry())) {
						return;
					}
					int rank = countryRanks.get(v.getCountry());
					List<Award> awardList = staticManoeuvreMgr.getAward(ManoeuvreConst.TYPE_RANK_COUNTRY, rank);
					if (awardList != null) {
						playerManager.sendAttachMail(v, awardList, MailId.MANOEUVRE_CAMP_AWARD, String.valueOf(v.getCountry()), String.valueOf(rank));
					}
				});

				// 沙盘演武参与奖励
				List<Award> applyAwardList = staticManoeuvreMgr.getAward(ManoeuvreConst.TYPE_RANK_ATTEND, 1);
				for (ManoeuvreFighter fighter : e.getApplyMap().values()) {
					Player player = playerManager.getPlayer(fighter.getPlayerId());
					if (applyAwardList != null && player != null) {
						playerManager.sendAttachMail(player, applyAwardList, MailId.MANOEUVRE_ATTEND_AWARD);
					}
				}
			});
			synActivityDoEnd();
		} else {
			synManoeuvre();
		}
		return true;
	}

	/**
	 * 结算阶段处理
	 *
	 * @param worldActPlan
	 * @return
	 */
	private boolean handlerDoEnd(WorldActPlan worldActPlan) {
		if (worldActPlan.getState() != WorldActPlanConsts.DO_END) {
			return false;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime < worldActPlan.getEndTime()) {
			return false;
		}

		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_14);
		long startTime = manoeuvreData.getStartTime();// 当前次的开启时间
		int roundType = worldTargetManager.getRoundType(worldManager.getWolrdInfo(), staticWorldActPlan);
		long date = TimeHelper.getTime(startTime, roundType, staticWorldActPlan.getWeekTime(), staticWorldActPlan.getTime());
		long preheat = TimeHelper.getTime(new Date(date), staticWorldActPlan.getPreheat());
		long endTime = date + staticWorldActPlan.getEndTime() * 60 * 1000L;
		worldActPlan.setOpenTime(date);
		worldActPlan.setPreheatTime(preheat);
		worldActPlan.setEndTime(endTime);
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);

		LogHelper.GAME_LOGGER.info("开启【沙盘演武】 活动状态:{} 预热时间:{} 开启时间:{} 结束时间:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));

		synActivityDispear();
		return true;
	}

	public CompletableFuture<Manoeuvre> initManoeuvreFuture(WorldActPlan worldActPlan) {
		if (manoeuvreData != null && manoeuvreData.getStartTime() == worldActPlan.getOpenTime()) {
			return CompletableFuture.completedFuture(null);
		}
		Manoeuvre manoeuvre = new Manoeuvre();
		manoeuvre.setStartTime(worldActPlan.getOpenTime());
		manoeuvre.setStatus(ManoeuvreConst.STATUS_APPLY);
		manoeuvre.setStage(0);
		// 对阵信息
		List<StaticManoeuvreMatch> list = staticManoeuvreMgr.getNewCourse();
		for (int i = 0; i < list.size(); i++) {
			StaticManoeuvreMatch manoeuvreMatch = list.get(i);
			int round = manoeuvreMatch.getCountryA() * 1000 + manoeuvreMatch.getCountryB() * 100;
			if (i == 0) {//
				manoeuvre.setRoundOne(round);
			} else if (i == 1) {
				manoeuvre.setRoundTwo(round);
			} else if (i == 2) {
				manoeuvre.setRoundThree(round);
			}
		}
		return CompletableFuture.completedFuture(manoeuvre);
	}

	private CompletableFuture<ManoeuvreData> doCalc() {
		List<ManoeuvreScore> list = new ArrayList<>();
		Map<Integer, ManoeuvreScore> scoreMap = new HashMap<>();
		for (int country = 1; country <= 3; country++) {
			ManoeuvreScore manoeuvreScore = new ManoeuvreScore();
			manoeuvreScore.setCountry(country);
			scoreMap.put(country, manoeuvreScore);
			list.add(manoeuvreScore);
		}

		for (ManoeuvreCourse course : manoeuvreData.getCourseMap().values()) {
			ManoeuvreScore left = scoreMap.get(course.getCountryLeft());
			ManoeuvreScore right = scoreMap.get(course.getCountryRight());
			if (course.getScoreLeft() > course.getScoreRight()) {//左侧胜利
				left.setScore(left.getScore() + 3);
			} else if (course.getScoreLeft() < course.getScoreRight()) {//右侧胜利
				right.setScore(right.getScore() + 3);
			} else if (course.getScoreLeft() == course.getScoreRight()) {//平局
				left.setScore(left.getScore() + 1);
				right.setScore(right.getScore() + 1);
			}
			left.setCaptureFlag(left.getCaptureFlag() + course.getScoreLeft());
			right.setCaptureFlag(right.getCaptureFlag() + course.getScoreRight());

			Pair<Integer, Integer> pair = course.getKillSoilder();
			left.setKillSoidler(pair.getLeft());
			right.setKillSoidler(pair.getRight());
		}

		List<ManoeuvreScore> rankList = list.stream().sorted(Comparator.comparing(ManoeuvreScore::getScore).thenComparing(ManoeuvreScore::getCaptureFlag).thenComparing(ManoeuvreScore::getKillSoidler).reversed()).collect(Collectors.toList());
		manoeuvreData.getScoreList().addAll(rankList);

		ManoeuvreScore winScore = rankList.get(0);
		manoeuvreData.setWiner(winScore.getCountry());

		// 计算连胜
		calcVictory();

		return CompletableFuture.completedFuture(manoeuvreData);
	}

	/**
	 * 筛选报名，选出参战队伍
	 *
	 * @return
	 */
	public boolean initFighters() {
		if (manoeuvreData.getApplyMap().isEmpty()) {//没有任何人报名
			return false;
		}
		// 按国家,案线路 取排名
		HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> applys = HashBasedTable.create();
		manoeuvreData.getApplyMap().values().forEach(e -> {
			List<ManoeuvreFighter> fighters = applys.get(e.getCountry(), e.getLine());
			if (fighters == null) {
				fighters = new ArrayList<>();
				applys.put(e.getCountry(), e.getLine(), fighters);
			}
			fighters.add(e);
		});

		// 将报名信息录入导战斗队伍信息
		for (int country = 1; country <= 3; country++) {
			for (int line = 1; line <= 3; line++) {
				List<ManoeuvreFighter> list = applys.get(country, line);
				if (list == null || list.isEmpty()) {
					manoeuvreData.getFights().put(country, line, new ArrayList<>());
					continue;
				}
				List<ManoeuvreFighter> sortList = list.stream().sorted(Comparator.comparing(ManoeuvreFighter::getPower).reversed().thenComparing(ManoeuvreFighter::getApplyTime)).collect(Collectors.toList());
				List<ManoeuvreFighter> fighterList = null;
				if (sortList.size() < ManoeuvreConst.LINE_FIGHTER_MAX) {
					fighterList = sortList;
				} else {
					fighterList = sortList.subList(0, ManoeuvreConst.LINE_FIGHTER_MAX);
				}
				List<ManoeuvreFighter> resultList = new ArrayList<>();
				if (!fighterList.isEmpty()) {
					resultList = fighterList.stream().sorted(Comparator.comparing(ManoeuvreFighter::getPower).thenComparing(ManoeuvreFighter::getApplyTime)).collect(Collectors.toList());
				}
				for (int pos = 1; pos <= resultList.size(); pos++) {
					ManoeuvreFighter fighter = resultList.get(pos - 1);
					fighter.setPos(pos);// 设置出战位置
				}
				manoeuvreData.getFights().put(country, line, resultList);
			}
		}
		return true;
	}

	/**
	 * 复制全体参战人员数据
	 *
	 * @return
	 */
	public HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> copyFighterList() {
		HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> result = HashBasedTable.create();

		HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> dataFights = manoeuvreData.getFights();

		for (int country = 1; country <= 3; country++) {
			for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
				List<ManoeuvreFighter> list = dataFights.get(country, line);
				List<ManoeuvreFighter> copyLine = new ArrayList<>();
				result.put(country, line, copyLine);
				if (list == null) {
					continue;
				}

				for (ManoeuvreFighter e : list) {
					copyLine.add(copyFighter(e));
				}
			}
		}
		return result;
	}

	public ManoeuvreFighter copyFighter(ManoeuvreFighter fighter) {
		ManoeuvreFighter copy = new ManoeuvreFighter();
		copy.setPlayerId(fighter.getPlayerId());
		copy.setCountry(fighter.getCountry());
		for (Hero hero : fighter.getHeroes()) {
			copy.getHeroes().add(hero.clone());
		}
		copy.setPower(fighter.getPower());
		copy.setLine(fighter.getLine());
		copy.setPos(fighter.getPos());
		copy.setApplyTime(fighter.getApplyTime());
		copy.setBlood(fighter.getBlood());
		return copy;
	}

	/**
	 * 创建战斗
	 *
	 * @param stage
	 */
	public CompletableFuture<ManoeuvreCourse> processStage(int stage) {
		if (stage >= ManoeuvreConst.STAGE_END) {
			return CompletableFuture.completedFuture(null);
		}
		ManoeuvreCourse course = manoeuvreData.getCourseMap().get(stage);
		if (course.getStatus() != ManoeuvreConst.COURSE_NONE) {
			return CompletableFuture.completedFuture(null);
		}

		if (course.isComplete()) {
			return CompletableFuture.completedFuture(null);
		}

		course.setStatus(ManoeuvreConst.COURSE_BEGIN);
		int leftId = course.getCountryLeft();
		int rightId = course.getCountryRight();

		// 国家ID,线路,报名数据
		HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> fighters = copyFighterList();
		course.setFights(fighters);

		// 线路,国家,是否还有活着的玩家
		HashBasedTable<Integer, Integer, Boolean> result = HashBasedTable.create();

		int round = 1;
		while (true) {
			Map<Integer, Pair<ManoeuvreFighter, ManoeuvreFighter>> combats = getCombat(fighters, leftId, rightId);
			for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
				if (result.containsRow(line)) {// 已经有结果了
					combats.remove(line);
					continue;
				}
				Pair<ManoeuvreFighter, ManoeuvreFighter> pair = combats.get(line);
				if (pair.getLeft() == null || pair.getRight() == null) {
					result.put(line, leftId, pair.getLeft() != null);
					result.put(line, rightId, pair.getRight() != null);
				}
			}

			if (combats.isEmpty()) {// 战斗结束
				break;
			}

			// 三路对阵
			for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
				if (!combats.containsKey(line)) {
					continue;
				}
				Pair<ManoeuvreFighter, ManoeuvreFighter> pair = combats.get(line);
				handlerWar(stage, round, line, pair.getLeft(), pair.getRight());
			}
			round++;
		}

		for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
			if (result.get(line, leftId)) {
				course.setScoreLeft(course.getScoreLeft() + 1);
			}
			if (result.get(line, rightId)) {
				course.setScoreRight(course.getScoreRight() + 1);
			}
		}
		course.setStatus(ManoeuvreConst.COURSE_NONE);
		return CompletableFuture.completedFuture(course);
	}

	/**
	 * 取三路对阵信息
	 *
	 * @param fighters
	 * @param leftId
	 * @param rightId
	 * @return
	 */
	public Map<Integer, Pair<ManoeuvreFighter, ManoeuvreFighter>> getCombat(HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> fighters, int leftId, int rightId) {
		Map<Integer, Pair<ManoeuvreFighter, ManoeuvreFighter>> result = new HashMap<>();

		for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
			ManoeuvreFighter left = getFighter(fighters, leftId, line);
			ManoeuvreFighter right = getFighter(fighters, rightId, line);
			result.put(line, new Pair<>(left, right));
		}
		return result;
	}

	/**
	 * 取国家一路阵容排头兵
	 *
	 * @param fighters
	 * @param country
	 * @param line
	 * @return
	 */
	public ManoeuvreFighter getFighter(HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> fighters, int country, int line) {
		List<ManoeuvreFighter> list = fighters.get(country, line);
		Optional<ManoeuvreFighter> optional = list.stream().filter(e -> e.alive()).findFirst();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	/**
	 * 处理战斗
	 *
	 * @param stage
	 * @param round
	 * @param line
	 * @param left
	 * @param right
	 */
	public void handlerWar(int stage, int round, int line, ManoeuvreFighter left, ManoeuvreFighter right) {
		if (left == null || right == null) {
			return;
		}

		Player playerLeft = playerManager.getPlayer(left.getPlayerId());
		Player playerRight = playerManager.getPlayer(right.getPlayerId());

		Team attacker = createTeam(left, true);
		Team defencer = createTeam(right, false);

		Random rand = new Random(System.currentTimeMillis());
		battleMgr.doTeamBattle(attacker, defencer, rand, false);

		if (attacker.isWin()) {//左侧胜利
			left.setCount(left.getCount() + 1);
			left.setBlood(attacker.getCurSoldier());
			left.setBeat(left.getBeat() + 1);
			right.setBlood(0);
		} else {
			left.setBlood(0);
			right.setBlood(defencer.getCurSoldier());
			right.setBeat(right.getBeat());
			right.setCount(right.getCount() + 1);
		}

		ManoeuvreCourse course = manoeuvreData.getCourseMap().get(stage);

		// 战报信息
		ManoeuvreRound manoeuvreRound = new ManoeuvreRound();
		manoeuvreRound.setStage(stage);
		manoeuvreRound.setRound(round);
		manoeuvreRound.setLine(line);
		manoeuvreRound.setPostLeft(left.getPos());
		manoeuvreRound.setPlayerIdLeft(left.getPlayerId());
		manoeuvreRound.setNickLeft(playerLeft.getNick());
		manoeuvreRound.setBloodLeft(left.getBlood());
		manoeuvreRound.setPostRight(right.getPos());
		manoeuvreRound.setPlayerIdRight(right.getPlayerId());
		manoeuvreRound.setNickRight(playerRight.getNick());
		manoeuvreRound.setBloodRight(right.getBlood());
		course.getRoundList().add(manoeuvreRound);// 添加回合信息

		// 武将杀敌和损兵信息
		addRoundDetail(manoeuvreRound, attacker, left);
		addRoundDetail(manoeuvreRound, defencer, right);
	}

	/**
	 * 武将的击杀损兵详情
	 *
	 * @param manoeuvreRound
	 * @param team
	 * @param fighter
	 */
	private void addRoundDetail(ManoeuvreRound manoeuvreRound, Team team, ManoeuvreFighter fighter) {
		for (BattleEntity entity : team.getGameEntities()) {
			ManoeuvreDetail detail = new ManoeuvreDetail();
			detail.setStage(manoeuvreRound.getStage());
			detail.setRound(manoeuvreRound.getRound());
			detail.setPlayerId(team.getLordId());
			detail.setHeroId(entity.getEntityId());
			detail.setLostSoilder(entity.getLost());
			detail.setKillSoilder(entity.getKillNum());
			detail.setMaxSoilder(entity.getMaxSoldierNum());
			Hero hero = fighter.getHero(entity.getEntityId());
			if (hero != null) {
				detail.setDiviNum(hero.getDiviNum());
			}
			manoeuvreRound.getDetailList().add(detail);
		}
	}


	/**
	 * 创建队伍信息
	 *
	 * @param fighter
	 * @param isAttacker
	 * @return
	 */
	private Team createTeam(ManoeuvreFighter fighter, boolean isAttacker) {
		Team team = new Team();
		Player player = playerManager.getPlayer(fighter.getPlayerId());
		Team marchTeam = initManoeuvreTeam(player, fighter.getHeroes(), BattleEntityType.HERO, false, 0, isAttacker);
		team.addTeam(marchTeam);
		team.setLordId(fighter.getPlayerId());
		return team;
	}


	// 1.初始化战队[玩家英雄], world, 损兵模式
	public Team initManoeuvreTeam(Player player, List<Hero> heroList, int entityType, boolean isCityMode, int fatherEntityType, boolean isAttacker) {
		if (player == null) {
			return null;
		}

		Team team = new Team();
		// 应该用玩家出战的英雄
		BattleProperty playerBp = battleMgr.getBattleProperty(player.getLevel());
		for (Hero hero : heroList) {
			if (hero.getCurrentSoliderNum() <= 0) {
				continue;
			}
			BattleEntity battleEntity = battleMgr.createBattleEntity(hero, player, playerBp, entityType, true, isCityMode, fatherEntityType, isAttacker);
			if (battleEntity != null) {
				team.add(battleEntity);
			}
		}

		team.setCountry(player.getCountry());
		team.setPortrait(player.getPortrait());
		team.setLordId(player.getRoleId());

		battleMgr.clearTeam(team);

		return team;
	}

	public int getVictorr(int country) {
		if (victorys.containsKey(country)) {
			return victorys.get(country);
		}
		return 0;
	}


	public CompletableFuture<ManoeuvreData> applyFight(Player player, int line, int maxSoilder, List<Hero> heroList) {
		if (maxSoilder == 0 || heroList.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}
		ManoeuvreFighter fighter = manoeuvreData.getApplyMap().get(player.getRoleId());
		if (fighter == null) {
			fighter = new ManoeuvreFighter();
			fighter.setCountry(player.getCountry());
			fighter.setPlayerId(player.getRoleId());
			manoeuvreData.getApplyMap().put(player.getRoleId(), fighter);
		}

		fighter.setLine(line);
		int power = heroManager.calcHeroBattleScore(player, heroList);
		fighter.setHeroes(heroList);
		fighter.setBlood(maxSoilder);
		fighter.setPower(power);
		return CompletableFuture.completedFuture(manoeuvreData);
	}

	public void update(ManoeuvreData manoeuvreData) {
		manoeuvreDao.updateManoeuvre(manoeuvreData.createManoeuvre());
	}

	public void synManoeuvre() {
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_14);
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_14);
		long period = staticWorldActPlan.getContinues().get(0) * ManoeuvreConst.SECOND;// 每轮的间隔时间
		long endTime = manoeuvreData.getStage() == ManoeuvreConst.STATUS_END ? worldActPlan.getEndTime() : worldActPlan.getOpenTime() + period * manoeuvreData.getStage();

		playerManager.getOnlinePlayer().forEach(e -> {
			SynManoeuvreChangeLineRq.Builder msg = SynManoeuvreChangeLineRq.newBuilder();
			msg.setStatus(manoeuvreData.getStatus());
			msg.setStage(manoeuvreData.getStage());
			msg.setEndTime(endTime);
			ManoeuvreFighter fighter = manoeuvreData.getApplyMap().get(e.getRoleId());
			msg.setApply(fighter == null ? 0 : 1);
			msg.setLine(fighter == null ? 0 : fighter.getLine());

			int stage = manoeuvreData.getStage();
			msg.addAllApplyLine(manoeuvreData.getApplyLinePb(e.getCountry(), stage));

			if (stage >= ManoeuvreConst.STAGE_ONE && stage <= ManoeuvreConst.STAGE_THRE) {
				ManoeuvreCourse manoeuvreCourse = manoeuvreData.getCourseMap().get(stage);
				msg.setCountry(TwoInt.newBuilder().setV1(manoeuvreCourse.getCountryLeft()).setV2(manoeuvreCourse.getCountryRight()).build());
			}
			SynHelper.synMsgToPlayer(e, SynManoeuvreChangeLineRq.EXT_FIELD_NUMBER, SynManoeuvreChangeLineRq.ext, msg.build());
		});
	}

	public void synActivityOpen() {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_14);

		SynManoeuvreChangeLineRq.Builder msg = SynManoeuvreChangeLineRq.newBuilder();
		msg.setStatus(manoeuvreData.getStatus());
		msg.setEndTime(worldActPlan.getOpenTime());
		msg.setStage(manoeuvreData.getStage());
		msg.setApply(0);

		playerManager.getOnlinePlayer().forEach(e -> {
			SynHelper.synMsgToPlayer(e, SynManoeuvreChangeLineRq.EXT_FIELD_NUMBER, SynManoeuvreChangeLineRq.ext, msg.build());
		});
	}

	public void synActivityDoEnd() {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_14);

		SynManoeuvreChangeLineRq.Builder msg = SynManoeuvreChangeLineRq.newBuilder();
		msg.setStatus(manoeuvreData.getStatus());
		msg.setEndTime(worldActPlan.getEndTime());
		msg.setStage(manoeuvreData.getStage());

		playerManager.getOnlinePlayer().forEach(e -> {
			SynHelper.synMsgToPlayer(e, SynManoeuvreChangeLineRq.EXT_FIELD_NUMBER, SynManoeuvreChangeLineRq.ext, msg.build());
		});
	}

	public void synActivityDispear() {
		SynManoeuvreChangeLineRq.Builder msg = SynManoeuvreChangeLineRq.newBuilder();
		msg.setStatus(0);
		msg.setStage(0);
		playerManager.getOnlinePlayer().forEach(e -> {
			SynHelper.synMsgToPlayer(e, SynManoeuvreChangeLineRq.EXT_FIELD_NUMBER, SynManoeuvreChangeLineRq.ext, msg.build());
		});
	}


	/**
	 * 报名测试
	 */
	public void testApply() {
		Map<Integer, List<Player>> map = playerManager.getPlayers().values().stream().filter(e -> e.getLevel() >= 150).collect(Collectors.groupingBy(Player::getCountry));
		for (int country = 1; country <= 3; country++) {
			List<Player> list = map.get(country);
			for (int line = 1; line <= 3; line++) {
				Player player = list.remove(0);
				List<Integer> embattleList = player.getEmbattleList();
				if (embattleList.isEmpty()) {
					continue;
				}
				List<Integer> heroList = new ArrayList<>();
				HashMap<Integer, Hero> heros = player.getHeros();
				for (Integer heroId : embattleList) {
					if (heroList.contains(heroId)) {
						continue;
					}
					Hero hero = heros.get(heroId);
					if (hero == null) {
						continue;
					}
					hero.setCurrentSoliderNum(hero.getSoldierNum());
					if (hero.getCurrentSoliderNum() > 0) {
						heroList.add(heroId);
					}
				}
				if (heroList.isEmpty()) {
					continue;
				}
				testApply(player.getRoleId(), line, heroList);
			}
		}
	}

	public void testApplyOne(int country) {
		Map<Integer, List<Player>> map = playerManager.getPlayers().values().stream().filter(e -> e.getLevel() >= 50).collect(Collectors.groupingBy(Player::getCountry));
		List<Player> list = map.get(country);
		Random random = new Random();
		for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
			int count = 0;
			do {
				if (list.isEmpty()) {
					break;
				}

				List<Integer> heroList = new ArrayList<>();

				Player player = list.remove(0);
				HashMap<Integer, Hero> heros = player.getHeros();
				List<Integer> embattleList = player.getEmbattleList();
				if (embattleList.isEmpty()) {
					List<Hero> tempList = heros.values().stream().sorted(Comparator.comparing(Hero::getHeroLv).reversed()).collect(Collectors.toList());
					if (tempList != null && !tempList.isEmpty()) {
						embattleList.add(tempList.get(0).getHeroId());
					}
					if (embattleList.isEmpty()) {
						continue;
					}
				}

				for (Integer heroId : embattleList) {
					if (heroList.contains(heroId)) {
						continue;
					}
					Hero hero = heros.get(heroId);
					if (hero == null) {
						continue;
					}
					hero.setCurrentSoliderNum(hero.getSoldierNum());
					if (hero.getCurrentSoliderNum() > 0) {
						heroList.add(heroId);
					}
				}
				if (heroList.isEmpty()) {
					continue;
				}
				testApply(player.getRoleId(), line, heroList);
				count++;
			} while (count < random.nextInt(8) + 20);
		}
	}

	public GameError testApply(long playerId, int line, List<Integer> list) {
		Player player = playerManager.getPlayer(playerId);

		if (manoeuvreData == null) {//活动未开启
			return GameError.NO_HERO_FIGHT;
		}
		int status = manoeuvreData.getStatus();
		if (status != ManoeuvreConst.STATUS_APPLY) {
			return GameError.NO_HERO_FIGHT;
		}

		if (line < ManoeuvreConst.LINE_ONE || line > ManoeuvreConst.LINE_THREE) {
			return GameError.ERROR_MINING_INDEX;
		}

		ManoeuvreFighter fighter = manoeuvreData.getApplyMap().get(playerId);
		if (fighter == null) {
			fighter = new ManoeuvreFighter();
			fighter.setCountry(player.getCountry());
			fighter.setPlayerId(player.getRoleId());
			manoeuvreData.getApplyMap().put(playerId, fighter);
		}

		fighter.setLine(line);
		int maxSoilder = 0;
		List<Hero> heroList = new ArrayList<>();
		for (int heroId : list) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {//
				return GameError.NO_HERO_FIGHT;
			}
			maxSoilder += hero.getCurrentSoliderNum();
			Hero clone = hero.clone();
			heroList.add(clone);
		}
		fighter.setBlood(maxSoilder);
		fighter.setHeroes(heroList);
		fighter.setPower(player.getBattleScore());
		return GameError.OK;
	}

	public void testClean() {
		WorldData worldData = worldManager.getWolrdInfo();
		worldData.getWorldActPlans().remove(WorldActivityConsts.ACTIVITY_14);
	}


}
