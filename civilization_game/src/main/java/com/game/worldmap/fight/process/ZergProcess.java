package com.game.worldmap.fight.process;

import com.game.constant.ActPassPortTaskType;
import com.game.constant.BattleEntityType;
import com.game.constant.MailId;
import com.game.constant.MapId;
import com.game.constant.MarchReason;
import com.game.constant.MarchState;
import com.game.constant.Reason;
import com.game.constant.WarState;
import com.game.constant.WarType;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticZerglMgr;
import com.game.define.Fight;
import com.game.define.WorldActCmd;
import com.game.domain.MapDistance;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.City;
import com.game.domain.p.HeroAddExp;
import com.game.domain.p.Report;
import com.game.domain.p.ReportMsg;
import com.game.domain.p.Team;
import com.game.domain.p.WorldActPlan;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticMonster;
import com.game.domain.s.StaticWorldMap;
import com.game.domain.s.StaticZergRound;
import com.game.manager.ZergManager;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerZergWar;
import com.game.pb.WorldPb;
import com.game.service.WorldActPlanService;
import com.game.spring.SpringUtil;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.worldmap.EntityType;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Monster;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.Fighter;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.game.worldmap.fight.zerg.ZergConst;
import com.game.worldmap.fight.zerg.ZergData;
import com.game.worldmap.fight.zerg.ZergFightReport;
import com.game.worldmap.fight.zerg.ZergFighter;
import com.game.worldmap.fight.war.ZergWarInfo;
import com.google.common.collect.HashBasedTable;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@WorldActCmd(actId = WorldActivityConsts.ACTIVITY_13, actName = "虫族主宰")
@Fight(warName = "虫族主宰活动", warType = {WarType.ATTACK_ZERG, WarType.DEFEND_ZERG}, marthes = {MarchType.ZERG_WAR, MarchType.ZERG_DEFEND_WAR})
@Component
public class ZergProcess extends FightProcess {

	@Autowired
	private ZergManager zergManager;
	@Autowired
	private StaticZerglMgr staticZerglMgr;

	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	@Autowired
	private ZergFightReport zergFightReport;


	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;
		// 注册战斗
		registerProcess(WarState.Waiting, this::waiting);
		registerProcess(WarState.Fighting, this::battle);
		registerProcess(WarState.Finish, this::warFinish);

		// 注册行军
		registerMarch(MarchType.ZERG_WAR, MarchState.Begin, this::marchAttackArrive);
		registerMarch(MarchType.ZERG_WAR, MarchState.Back, this::doFinishedMarch);

		registerMarch(MarchType.ZERG_DEFEND_WAR, MarchState.Begin, this::marchDefendArrive);
		registerMarch(MarchType.ZERG_DEFEND_WAR, MarchState.Waiting, this::monsterMatchWaiting);
		registerMarch(MarchType.ZERG_DEFEND_WAR, MarchState.Back, this::doFinishedMarch);
	}

	@Override
	public void doWorldActPlan(WorldActPlan worldActPlan) {
		try {
			// 更改worldActPlan状态，是否开启活动
			boolean b = zergManager.checkZergWorldActPlan(worldActPlan);
			// 如果活动开启则进行轮询
			if(b){
				zergManager.checkRound(worldActPlan);
			}

		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * 进攻主宰行军抵达
	 *
	 * @param mapInfo
	 * @param march
	 */
	private void marchAttackArrive(MapInfo mapInfo, March march) {
		IWar war = mapInfo.getWar(march.getWarId());
		if (war == null) {
			handleMarchReturn(march, Reason.BROOD_WAR);
			return;
		}
		march.setState(MarchState.Waiting);
		march.setEndTime(war.getEndTime() - 1000);
		// 广播行军
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void monsterMatchWaiting(MapInfo mapInfo, March march) {
		IWar war = mapInfo.getWar(march.getWarId());
		if (war == null) {
			handleMarchReturn(march, Reason.BROOD_WAR);
		}
	}

	/**
	 * 防守主宰行军抵达
	 *
	 * @param mapInfo
	 * @param march
	 */
	private void marchDefendArrive(MapInfo mapInfo, March march) {
		IWar war = mapInfo.getWar(march.getWarId());
		if (war == null) {
			handleMarchReturn(march, Reason.BROOD_WAR);
			return;
		}
		march.setState(MarchState.Waiting);
		march.setEndTime(war.getEndTime() - 1000);
		war.getDefencer().addMarch(march);
		// 广播行军
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void waiting(MapInfo mapInfo, IWar war) {
		long now = System.currentTimeMillis();
		if (war.getEndTime() > now) {
			return;
		}
		war.updateState(WarState.Fighting);
	}


	public void battle(MapInfo mapInfo, IWar war) {
		if (war.getState() != WarState.Fighting) {
			return;
		}
		ZergWarInfo warInfo = (ZergWarInfo) war;
		if (warInfo.getWarType() == WarType.ATTACK_ZERG) {// 玩家进攻怪物
			doMonsterWar(warInfo);
			ZergData zergData = zergManager.getZergData();
			if (zergData.getStep() != 0) {
				StaticZergRound staticZergRound = staticZerglMgr.getRound(zergData.getStep());
				zergData.setStepFinish(staticZergRound.getRoundFinish());
			}
		} else if (warInfo.getWarType() == WarType.DEFEND_ZERG) {// 怪物进攻玩家
			doDefendWar(warInfo);
		}

		warInfo.updateState(WarState.Finish);
		warManager.synWarInfoToWorld(warInfo);
	}

	private void warFinish(MapInfo mapInfo, IWar war) {
		ZergWarInfo warInfo = (ZergWarInfo) war;
		warInfo.setEnd(true);
	}

	public void doMonsterWar(ZergWarInfo warInfo) {
		ZergData zergData = zergManager.getZergData();
//        LogHelper.GAME_LOGGER.info("【虫族主宰.主宰战】 {}回合开始", zergData.getStep());

		Team monsterTeam = zergData.getTeam();
		List<March> readyList = warInfo.getAttackMarches().stream().collect(Collectors.toList());
		if (readyList.isEmpty()) {
			LogHelper.GAME_LOGGER.info("【虫族主宰.主宰战】 {}回合结束,主宰剩余兵力：{}", zergData.getStep(), monsterTeam.getCurSoldier());
			return;
		}

		// 取第一个行军
		March march = warInfo.getAttackMarches().getFirst();
		long playerId = march.getLordId();

		Team playerTeam = battleMgr.initBigMonsterWarTeam(readyList, true);

		// 防守怪物阵容
//		MapDistance mapDistance = new MapDistance();
		Monster bigMonster = new Monster();
		bigMonster.setId(warInfo.getDefencerId());
//		bigMonster.setDistance(warInfo.getDefencerPos());
		bigMonster.setEntityType(EntityType.BIG_MONSTER);
		bigMonster.setLevel(120);

		StaticMonster staticMonster = staticMonsterMgr.getStaticMonster((int) warInfo.getDefencerId());

		int totalSoldierNum = monsterTeam.getAllEnities().stream().filter(e -> e.getCurSoldierNum() > 0).mapToInt(BattleEntity::getCurSoldierNum).sum();

		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		int reless = monsterTeam.getAllEnities().stream().filter(e -> e.getCurSoldierNum() > 0).mapToInt(BattleEntity::getCurSoldierNum).sum();
		int lossSoider = totalSoldierNum - reless;

		// 计算经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, playerId);

		// 处理玩家扣血
		HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();

		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();

		warManager.handlRebeleWarChange(attackerMarches, playerTeam, allSoldierRec, WarType.ATTACK_ZERG);

		Map<Long, List<March>> marchMap = readyList.stream().collect(Collectors.groupingBy(e -> e.getLordId()));

		// 第一个发兵
		Player player = playerManager.getPlayer(playerId);

		if (playerTeam.isWin()) {
			WorldActPlan worldActPlan = worldManager.getWorldActPlan(WorldActivityConsts.ACTIVITY_13);
			worldActPlan.setState(WorldActPlanConsts.DO_END);
		}

		for (March m : warInfo.getAttackMarches()) {
			handleMarchReturn(m, MarchReason.CANCEL_BIGMONSTER_BACK);
			worldManager.synMarch(MapId.CENTER_MAP_ID, m);
		}

		// 记录玩家杀敌数量 发放首杀奖励
		zergFightReport.createKillMonsterReport(playerTeam, monsterTeam, player, bigMonster, totalSoldierNum, lossSoider).thenAcceptAsync(report -> {
			Map<Long, Integer> lost = playerTeam.getLostSoldiersByPlayer();
			if (playerTeam.isWin()) {//
				for (Map.Entry<Long, List<March>> entry : marchMap.entrySet()) {
					Player p = playerManager.getPlayer(entry.getKey());
					if (p == null) {
						continue;
					}
					// 本轮的参与人员,获胜全部都获得2500积分
					p.getSimpleData().addZergScore(ZergConst.SCORE_KILL);
					playerManager.sendNormalMail(p, MailId.ZERG_ATTACK_WIN_REWARD, staticMonster.getName(), warInfo.getDefencerPos().toPosStr(), String.valueOf(ZergConst.SCORE_KILL));
				}
			}

			// 损失兵力获得积分邮件,参与的人都有战报邮件
			Iterator<Entry<Long, Integer>> it = lost.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Long, Integer> entry = it.next();
				Player p = playerManager.getPlayer(entry.getKey());
				if (p == null) {
					continue;
				}
				int score = entry.getValue() / ZergConst.DEAD_NUM_CONVERT_SCORE;
				p.getSimpleData().addZergScore(score);
				ReportMsg reportMsg = battleMailManager.createReportMsg(playerTeam, monsterTeam, new ArrayList<>(), heroAddExp);
				playerManager.sendReportMail(p, report, reportMsg, MailId.ZERG_ATTACK_REWARD, null, null, p.getNick(), player.getPosStr(), staticMonster.getName(), warInfo.getDefencerPos().toPosStr(), String.valueOf(score));
				playerManager.synChange(p, Reason.KILL_REBEL_MONSTER);
			}
		});
		LogHelper.GAME_LOGGER.info("【虫族主宰.主宰战】 {}回合结束,主宰剩余兵力：{}", zergData.getStep(), monsterTeam.getCurSoldier());
	}


	/**
	 * 相当于城战
	 *
	 * @param warInfo
	 */
	public void doDefendWar(ZergWarInfo warInfo) {
		try {
			LogHelper.GAME_LOGGER.info("【虫族主宰】 防守阶段战斗开始");
			MapInfo mapInfo = worldManager.getWorldMapInfo().get(warInfo.getMapId());

			// 进攻部队
			ZergFighter zergFighter = (ZergFighter) warInfo.getAttacker();
			Team attacker = zergFighter.getTeam();

			// 防守方: 援助 + 当前玩家的驻防武将 + 其他玩家驻防武将 + 城防军
			Team defencer = battleMgr.initDefencer(warInfo, BattleEntityType.HERO, false);

			Random rand = new Random(System.currentTimeMillis());
			// seed 开始战斗
			battleMgr.doTeamBattle(attacker, defencer, rand, ActPassPortTaskType.IS_WORLD_WAR);

			Player target = playerManager.getPlayer(warInfo.getDefencerId());
			if (target == null) {
				LogHelper.CONFIG_LOGGER.info("target is null!");
				return;
			}

			// 每个玩家的杀敌数量
			Map<Long, Integer> killSolider = defencer.getKillSoliderByPlayer();
			if (!killSolider.containsKey(target.getRoleId())) {
				killSolider.put(target.getRoleId(), 0);
			}
			worldManager.caculateTeamDefenceKill(defencer);
			// 处理玩家扣血
//		HashMap<Integer, Integer> defenceRec = new HashMap<Integer, Integer>();
			HashMap<Long, HashMap<Integer, Integer>> allDefenceRec = new HashMap<>();

			worldManager.handleDefenceSoldier(defencer, target, MarchReason.AttendZergWar, allDefenceRec);
//		defenceRec = allDefenceRec.get(target.getLord().getLordId());

			// 同步所有参战人员的属性变化, 处理士兵血量和威望
			HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
			handlePvpWarHp(warInfo, defencer, allSoldierRec, WarType.DEFEND_ZERG);

			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster((int) warInfo.getAttackerId());

			Report report = zergFightReport.createReport(warInfo, attacker, defencer);
			ReportMsg reportMsg = zergFightReport.createReportMsg(attacker, defencer);

			// 杀敌数积分
			killSolider.forEach((e, f) -> {
				int score = f / ZergConst.KILL_NUM_CONVERT_SCORE;
				Player p = playerManager.getPlayer(e.longValue());
				if (p == null) {
					return;
				}
				p.getSimpleData().addZergScore(score);// 添加积分
				if (e.longValue() != target.getRoleId().longValue()) {//协防玩家
					playerManager.sendReportMail(p, report, reportMsg, MailId.ZERG_DEFENCE_HELP_REWARD, null, null, target.getNick(), staticMonster.getName(), warInfo.getDefencerPos().toPosStr(), String.valueOf(score));
				} else {
					playerManager.sendReportMail(target, report, reportMsg, MailId.ZERG_DEFENCE_REWARD, null, null, target.getNick(), staticMonster.getName(), warInfo.getDefencerPos().toPosStr(), String.valueOf(score));
				}
			});

			if (!attacker.isWin()) { // 主宰进攻失败,玩家胜利
				killSolider.forEach((e, f) -> {
					Player p = playerManager.getPlayer(e.longValue());
					if (p == null) {
						return;
					}
					p.getSimpleData().addZergScore(ZergConst.SCORE_DEFEND_WIN);// 添加积分
					playerManager.sendNormalMail(p, MailId.ZERG_DEFENCE_WIN_REWARD, staticMonster.getName(), warInfo.getDefencerPos().toPosStr(), String.valueOf(ZergConst.SCORE_DEFEND_WIN));
				});
			}

			// 删除虫族行军数据
			handleZergWarMarch(warInfo, mapInfo, MarchReason.FarWarFailed);

			worldManager.soldierAutoAdd(target);
			synAllPlayerChange(warInfo);
			// 通知参战双方删除战斗
			warManager.handePvpWarRemove(warInfo);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

//    private int getWarAttackNum(WarInfo warInfo) {
//        if (warInfo.getWarType() == WarType.DEFEND_ZERG) {// 防守阶段,进攻队伍为怪物
//            Team team = zergManager.getTeam((int) warInfo.getAttackerId());
//            return team.getCurSoldier();
//        } else {
//            ConcurrentLinkedDeque<March> marches = warInfo.getAttackMarches();
//            return worldManager.getMarchesSolderNum(marches);
//        }
//    }

//    private int getDefenceNum(WarInfo warInfo) {
//        if (warInfo.getWarType() == WarType.ATTACK_ZERG) {// 进攻阶段,防守队伍为怪物
//            Team team = zergManager.getTeam((int) warInfo.getDefencerId());
//            return team.getCurSoldier();
//        } else {
//            ConcurrentLinkedDeque<March> marches = warInfo.getDefenceMarches();
//            return worldManager.getMarchesSolderNum(marches);
//        }
//    }


	public void handleMarchReturn(March march, int reason) {
		march.setState(MarchState.FightOver);
		march.swapPos(reason);
		long period = 1000;// 1秒回城
		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
	}


	public void handleZergWarMarch(WarInfo warInfo, MapInfo mapInfo, int reason) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		for (March march : attackerMarches) {

			march.setState(MarchState.FightOver);
			march.swapPos(reason);
			march.setPeriod(1);
			march.setEndTime(System.currentTimeMillis());

			Player player = playerManager.getPlayer(warInfo.getDefencerId());
			mapInfo.removeMarch(march);

			// 推送给国家
			WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
			worldManager.synRemoveWar(player, synCityWarRq);

			// 异步通知所有在线玩家 删除行军
			WorldPb.SynMarchRq synMarchRq = worldManager.createSynRemoveMarchRq(march);
			playerManager.getOnlinePlayer().forEach(target -> {
				playerManager.synMarchToPlayer(target, synMarchRq);
			});
		}

		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		for (March march : defencerMarches) {
			handleMarchReturn(march, reason);
			worldManager.synMarch(mapInfo.getMapId(), march);
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				playerManager.synChange(player, Reason.ATTACK_CITY);
			}
		}
	}

	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
		if (worldMap.getZergWarData() == null) {
			return;
		}
		SerZergWar serZergWar = null;
		try {
			serZergWar = SerZergWar.parseFrom(worldMap.getZergWarData());
			if (serZergWar == null) {
				return;
			}
			List<DataPb.WarData> warDatas = serZergWar.getWarDataList();
			if (warDatas == null) {
				return;
			}
			for (DataPb.WarData warData : warDatas) {
				if (warData == null) {
					continue;
				}
				WarInfo warInfo = warManager.createZergWar(warData, mapInfo);
				StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapInfo.getMapId());
				if (staticWorldMap != null) {
					warInfo.setMapType(staticWorldMap.getAreaType());
				}

				mapInfo.addWar(warInfo);
			}
		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

}
