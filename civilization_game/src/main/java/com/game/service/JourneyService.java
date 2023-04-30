package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.util.PbHelper;

import java.util.*;

import com.game.constant.*;
import com.game.log.consumer.EventManager;
import com.game.log.domain.JourneyLog;
import com.game.manager.*;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.dataMgr.StaticJourneyMgr;
import com.game.dataMgr.StaticOpenManger;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Player;
import com.game.domain.p.AttackInfo;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.Hero;
import com.game.domain.p.Lord;
import com.game.domain.p.Team;
import com.game.domain.s.StaticJourney;
import com.game.domain.s.StaticJourneyPrice;
import com.game.domain.s.StaticVip;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Award;
import com.game.pb.CommonPb.FightBefore;
import com.game.pb.JourneyPb;
import com.game.pb.JourneyPb.BuyJourneyTimesRs;
import com.game.pb.JourneyPb.GetAllJourneyRq;
import com.game.pb.JourneyPb.GetAllJourneyRs;
import com.game.pb.JourneyPb.JourneyDoneRq;
import com.game.pb.JourneyPb.JourneyDoneRs;
import com.game.util.LogHelper;

/**
 * 2020年8月17日
 *
 *    halo_game JourneyService.java
 **/
@Service
public class JourneyService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private BattleMgr battleDataMgr;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private JourneyManager journeyManager;

	@Autowired
	private StaticJourneyMgr staticJourneyMgr;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private StaticOpenManger staticOpenManger;

	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	ActivityEventManager activityEventManager;

	/**
	 * 获取胜利的最后关卡
	 *
	 * @param req
	 * @param handler
	 */
	public void getAllJourneyRq(GetAllJourneyRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		JourneyPb.GetAllJourneyRs.Builder builder = JourneyPb.GetAllJourneyRs.newBuilder();
		CommonPb.Journey.Builder nowLastJourney = journeyManager.getNowLastJourney(player);
		builder.setJourney(nowLastJourney);

		handler.sendMsgToPlayer(GetAllJourneyRs.ext, builder.build());
	}

	/**
	 * 是否上阵英雄
	 *
	 * @param player
	 * @param heroId
	 * @return
	 */
	public boolean isEmbattleHero(Player player, int heroId) {
		List<Integer> embattleHero = player.getEmbattleList();
		for (Integer elem : embattleHero) {
			if (elem == heroId) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 征途战斗
	 *
	 * @param req
	 * @param handler
	 */
	public void journeyDoneRq(JourneyDoneRq req, ClientHandler handler) {
		/*************************** 条件判断 ***********************************/

		// 判断体力值
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		if (!staticOpenManger.isOpen(OpenConsts.OPEN_11, player)) {// 开放条件40级
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}

		int journeyId = req.getJourneyId();
		int lastJourney = player.getLord().getLastJourney();
		if (journeyId <= lastJourney) {
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_STATE_WRONG);
			return;
		}

		StaticJourney staticJourney = staticJourneyMgr.getStaticJourney(journeyId);
		// 关卡不存在
		if (staticJourney == null) {
			LogHelper.CONFIG_LOGGER.info("StaticJourney is null, journeyId = " + journeyId);
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_CONFIG_NOT_EXISTS);
			return;
		}

		int winCost = staticJourney.getWinCost();
		if (winCost <= 0) {
			LogHelper.CONFIG_LOGGER.info("error winCost, winCost = " + winCost + ", JOURNEYId = " + staticJourney.getWinCost());
			handler.sendErrorMsgToPlayer(GameError.ENERGY_COST_ERROR);
			return;
		}

		Lord lord = player.getLord();
		if (lord.getJourneyTimes() < winCost) {
			handler.sendErrorMsgToPlayer(GameError.ENERGY_NOT_ENOUGH);
			return;
		}

		// 没有出站的英雄
		List<Integer> heroList = req.getHeroIdList();
		if (heroList.size() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_HERO_FIGHT);
			return;

		}
		// 应该用玩家出战的英雄
		// 出战英雄的Id有没有重复
		Set<Integer> checkHeroSet = new HashSet<Integer>();
		Map<Integer, Hero> heros = player.getHeros();
		// 检查出战的英雄Id的合法性
		for (Integer heroId : heroList) {
			Hero hero = heros.get(heroId);
			if (hero == null) {
				handler.sendErrorMsgToPlayer(GameError.HERO_FIGHT_NOT_EXISTS);
				return;
			}
			checkHeroSet.add(heroId);
		}

		// 检查出战的英雄有重复的
		if (checkHeroSet.size() != heroList.size()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
			return;
		}

		// 检查是否是非上阵武将
		for (Integer heroId : heroList) {
			if (!isEmbattleHero(player, heroId)) {
				LogHelper.CONFIG_LOGGER.info("not embattle hero, heroId =" + heroId);
				handler.sendErrorMsgToPlayer(GameError.NOT_EMBATTLE_HERO);
				return;
			}
		}

		/******************************************* 战斗演算 ***********************************/
		// 进入副本,查看战斗
		List<Integer> monsterIds = staticJourney.getMonsterIds();
		if (monsterIds == null) {
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_NO_MONSTER);
			return;
		}

		Team monsterTeam = battleDataMgr.initMonsterTeam(monsterIds, BattleEntityType.MONSTER);
		Team playerTeam = battleDataMgr.initPvePlayerTeam(player, heroList, BattleEntityType.HERO);

		// 战斗前
		JourneyPb.JourneyDoneRs.Builder builder = JourneyDoneRs.newBuilder();
		FightBefore.Builder fightBefore = FightBefore.newBuilder();
		// 玩家
		ArrayList<BattleEntity> playerEntities = playerTeam.getAllEnities();
		for (BattleEntity battleEntity : playerEntities) {
			fightBefore.addLeftEntities(battleEntity.wrapPb());
		}

		// 野怪
		ArrayList<BattleEntity> monsterEntities = monsterTeam.getAllEnities();
		for (BattleEntity battleEntity : monsterEntities) {
			fightBefore.addRightEntities(battleEntity.wrapPb());
		}

		builder.setFightBefore(fightBefore);

		// 随机seed不用存盘，没有回放, 种子需要发送到客户端
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleDataMgr.doTeamBattle(playerTeam, monsterTeam, rand, !ActPassPortTaskType.IS_WORLD_WAR);
		// 战中信息
		CommonPb.FightIn.Builder fightIn = CommonPb.FightIn.newBuilder();
		// 玩家
		ArrayList<AttackInfo> playerAttackInfos = playerTeam.getAttackInfos();
		for (AttackInfo attackInfo : playerAttackInfos) {
			fightIn.addLeftInfo(attackInfo.wrapPb());
		}
		// 野怪
		ArrayList<AttackInfo> monsterAttackInfos = monsterTeam.getAttackInfos();
		for (AttackInfo attackInfo : monsterAttackInfos) {
			fightIn.addRightInfo(attackInfo.wrapPb());
		}
		builder.setFightIn(fightIn);

		// 失败要特殊处理
		// 根据实际扣除玩家体力
		if (playerTeam.isWin()) {
			// chatManager.updateChatShow(ChatShowType.PASS_JOURNEY, staticJOURNEY.getJOURNEYId(), player);
			handleJourneyWin(player, staticJourney, journeyId, playerTeam, handler, builder);

			activityEventManager.activityTip(EventEnum.JOURNEY_DONE, player, 1, 0);
			// 更新通行证活动进度
//            activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.DONE_JOURNEY_OR_SWEEP, 1);
			activityManager.calculDailyExpedition(player, 1);
			dailyTaskManager.record(DailyTaskId.EXPEDITION, player, 1);
			activityManager.updActSeven(player, ActivityConst.TYPE_SET, Integer.valueOf(ActSevenConst.JOUNERY_DONE + String.valueOf(staticJourney.getJourneyId())), 0, 1);
			achievementService.addAndUpdate(player,AchiType.AT_35,1);
		} else {
			handleJourneyFail(player, staticJourney, journeyId, playerTeam, handler, builder);
		}
		eventManager.journeryDone(player, Lists.newArrayList(journeyId, lord.getLastJourney(), 1, "", playerTeam.isWin()));
		com.game.log.LogUser.log(LogTable.journey_log, JourneyLog.builder().lordId(player.roleId).nick(player.getNick()).level(player.getLevel()).journeyId(journeyId).result(playerTeam.isWin() ? 0 : 1).maxJourneyLog(player.getLord().getLastJourney()).build());
	}

	public void handleJourneyWin(Player player, StaticJourney staticJourney, int journeyId, Team playerTeam, ClientHandler handler, JourneyDoneRs.Builder builder) {

		Lord lord = player.getLord();
		int winCost = staticJourney.getWinCost();
		playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.JOURNEY_TIME, winCost, Reason.SUB_JOURNEY_TIMES);// 增加远征次数
		// lord.setJourneyTimes(lord.getJourneyTimes() - staticJourney.getWinCost());
		if (lord.getLastJourney() < journeyId) {
			lord.setLastJourney(journeyId);
		}

		/**************************** 消息回应 ***********************************/
		builder.setIsWin(playerTeam.isWin());
		builder.setJourneyTimes(lord.getJourneyTimes());

		List<Award> list = new ArrayList<>();
		Award stableAwards = journeyManager.getStableAwards(journeyId);
		if (null != stableAwards) {
			builder.addAward(stableAwards);
			playerManager.addAward(player, stableAwards.getType(), stableAwards.getId(), stableAwards.getCount(), Reason.ADD_OMAMENTE);
			list.add(stableAwards);
		}

		Award randomAwards = journeyManager.getRandomAwards(journeyId);
		if (null != randomAwards) {
			builder.addAward(randomAwards);
			playerManager.addAward(player, randomAwards.getType(), randomAwards.getId(), randomAwards.getCount(), Reason.ADD_OMAMENTE);
			list.add(randomAwards);
		}

		handler.sendMsgToPlayer(JourneyDoneRs.ext, builder.build());
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(journeyId);
		taskManager.doTask(TaskType.DONE_JOURNEY, player, triggers);
	}

	public void handleJourneyFail(Player player, StaticJourney staticJourney, int journeyId, Team playerTeam, ClientHandler handler, JourneyDoneRs.Builder builder) {

		Lord lord = player.getLord();
		lord.setJourneyTimes(lord.getJourneyTimes() - staticJourney.getFailedCost());
		/**************************** 消息回应 ***********************************/

		builder.setIsWin(playerTeam.isWin());
		builder.setJourneyTimes(lord.getJourneyTimes());
		handler.sendMsgToPlayer(JourneyDoneRs.ext, builder.build());
	}

	/**
	 * Function: 征途扫荡
	 */
	public void SweepJourneyRq(JourneyPb.SweepJourneyRq req, ClientHandler handler) {
		// check player
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int journeyId = req.getJourneyId();// 扫荡关卡ID
		int sweepTimes = req.getSweepTimes();// 扫荡次数

		StaticJourney staticJourney = staticJourneyMgr.getStaticJourney(journeyId);
		// check JOURNEY exists or not.
		if (staticJourney == null) {
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_CONFIG_NOT_EXISTS);
			return;
		}

		int lastJourney = player.getLord().getLastJourney();
		if (journeyId > lastJourney) {
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_STATE_WRONG);
			return;
		}

		// 只有boss关卡才能扫荡
		if (staticJourney.getJourneyType() != 2) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_JOURNEY_TYPE);
			return;
		}

		Lord lord = player.getLord();
		int journeyTimes = lord.getJourneyTimes();
		// int time = lord.getJourneyTimes() / staticJourney.getWinCost();
		/*
		 * time = (time >= 5 ? CommonDefine.SWEEP_TIMES : time); // 检查体力值是否够 if (time == 0) { handler.sendErrorMsgToPlayer(GameError.ENERGY_COST_ERROR); return; }
		 */
		if (sweepTimes == 0 || journeyTimes < sweepTimes) {
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_COST_ERROR);
			return;
		}

		int cost = staticJourney.getWinCost() * sweepTimes;
		playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.JOURNEY_TIME, cost, Reason.SUB_JOURNEY_TIMES);// 增加远征次数
		// lord.setJourneyTimes(lord.getJourneyTimes() - cost);
		JourneyPb.SweepJourneyRs.Builder builder = JourneyPb.SweepJourneyRs.newBuilder();
		builder.setJourneyTimes(lord.getJourneyTimes());

		List<Award> awards = new ArrayList<>();
		for (int i = 0; i < sweepTimes; i++) {// 发放扫荡奖品
			Award stableAwards = journeyManager.getStableAwards(journeyId);
			if (null != stableAwards) {
				awards.add(PbHelper.createAward(stableAwards.getType(), stableAwards.getId(), stableAwards.getCount()).build());
				playerManager.addAward(player, stableAwards.getType(), stableAwards.getId(), stableAwards.getCount(), Reason.ADD_OMAMENTE);
			}

			Award randomAwards = journeyManager.getRandomAwards(journeyId);
			if (null != randomAwards && randomAwards.getCount() != 0) {
				awards.add(PbHelper.createAward(randomAwards.getType(), randomAwards.getId(), randomAwards.getCount()).build());
				playerManager.addAward(player, randomAwards.getType(), randomAwards.getId(), randomAwards.getCount(), Reason.ADD_OMAMENTE);
			}
		}

		List<Award> createAwardList = PbHelper.createAwardList(PbHelper.finilAward(awards));
		builder.addAllAward(createAwardList);
		handler.sendMsgToPlayer(JourneyPb.SweepJourneyRs.ext, builder.build());

		// 更新通行证活动进度
		activityEventManager.activityTip(EventEnum.JOURNEY_DONE, player, sweepTimes, 0);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.DONE_JOURNEY_OR_SWEEP, sweepTimes);
		activityManager.calculDailyExpedition(player, sweepTimes);
		dailyTaskManager.record(DailyTaskId.EXPEDITION, player, sweepTimes);

		// 
		List<String> awardStringList = new ArrayList<>();
		awards.forEach(e -> {
			awardStringList.add(new com.game.domain.Award(e.getKeyId(), e.getType(), e.getId(), e.getCount()).toString());
		});
		eventManager.journeryDone(player, Lists.newArrayList(journeyId, lord.getLastJourney(), sweepTimes, awardStringList, true));

		JourneyLog log = JourneyLog.builder().lordId(player.roleId).nick(player.getNick()).level(player.getLevel()).journeyId(journeyId).result(0).maxJourneyLog(player.getLord().getLastJourney()).build();
		for (int i = 0; i < sweepTimes; i++) {
			com.game.log.LogUser.log(LogTable.journey_log, log);
		}
	}

	/**
	 * 购买征途次数
	 *
	 * @param req
	 * @param handler
	 */
	public void buyJourneyTimesRq(JourneyPb.BuyJourneyTimesRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Lord lord = player.getLord();
		StaticVip staticVip = staticVipMgr.getStaticVip(player.getVip());
		if (null == staticVip) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int count = req.getCount();
		int price = req.getPrice();
		int buyTotal = lord.getBuyJourneyTimes() + count;// 计算总购买远征次数

		int journeyBuyTime = staticVip.getJourneyBuyTime();
		StaticJourneyPrice staticJourneyPrice = staticJourneyMgr.getStaticJourneyPrice(buyTotal);
		int priceTager = staticJourneyPrice.getPrice();

		if (buyTotal > journeyBuyTime) {
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_BUY_COUNT_ERROR);// 超出最大购买次数
			return;
		}

		if (priceTager != price) {
			handler.sendErrorMsgToPlayer(GameError.JOURNEY_PRICE_ERROR);// 价格和配置不一致
			return;
		}

		if (lord.getGold() < price) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);// 钻石不足
			return;
		}

		JourneyPb.BuyJourneyTimesRs.Builder builder = BuyJourneyTimesRs.newBuilder();

		playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.JOURNEY_TIME, count * 10, Reason.BUY_JOURNEY_TIMES);// 增加远征次数
		playerManager.subAward(player, AwardType.GOLD, 0, price, Reason.BUY_JOURNEY_TIMES);// 扣除钻石
		lord.setBuyJourneyTimes(buyTotal);

		builder.setGold(lord.getGold());
		builder.setBuyTotal(buyTotal);
		builder.setJourneyTimes(lord.getJourneyTimes());// 增加征途次数

		handler.sendMsgToPlayer(JourneyPb.BuyJourneyTimesRs.ext, builder.build());
		activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.DONE_JOURNEY, 1);
		eventManager.journeryBuy(player, Lists.newArrayList(count, price));
		achievementService.addAndUpdate(player,AchiType.AT_16,count);

	}
	@Autowired
	AchievementService achievementService;
}
