package com.game.worldmap.fight.process;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.HeroAddExp;
import com.game.domain.p.Team;
import com.game.domain.p.WorldActPlan;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticWorldMonster;
import com.game.log.consumer.EventManager;
import com.game.manager.RiotManager;
import com.game.service.RoitService;
import com.game.util.LogHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.BigMonsterWarInfo;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "虫族入侵", warType = {WarType.RIOT_WAR}, marthes = {MarchType.RiotWar})
@Component
public class RiotProcess extends FightProcess {

	@Autowired
	private RoitService roitService;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private RiotManager riotManager;
	@Autowired
	ActivityEventManager activityEventManager;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

//		// 注册战斗
		registerProcess(WarState.Waiting, this::warWaiting);
		registerProcess(WarState.Fighting, this::doRiotWar);
		registerProcess(WarState.Finish, this::warFinish);
//
//		// 注册行军
		registerMarch(MarchType.RiotWar, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.RiotWar, MarchState.Waiting, this::marchWaiting);
		registerMarch(MarchType.RiotWar, MarchState.Fighting, this::marchFighting);
		registerMarch(MarchType.RiotWar, MarchState.Back, this::doFinishedMarch);
	}

	@Override
	public void checkMarch(MapInfo mapInfo, March march) {

	}

	private void marchArrive(MapInfo mapInfo, March march) {
		march.setState(MarchState.Waiting);
		march.setEndTime(march.getFightTime());
		Player target = playerManager.getPlayer(march.getDefencerId());
		WarInfo warInfo = target.getSimpleData().getRiotWarInfo();
		warInfo.addDefenceMarch(march);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void marchWaiting(MapInfo mapInfo, March march) {
		march.setState(MarchState.Fighting);
		march.setEndTime(march.getFightTime() + 1000L);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void marchFighting(MapInfo mapInfo, March march) {
		march.setState(MarchState.FightOver);
		marchManager.handleMarchReturn(march, MarchReason.RIOT_WAR_ATTEND);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}


	private void warWaiting(MapInfo mapInfo, IWar war) {
		LogHelper.GAME_LOGGER.info("【虫族入侵.战斗.等待】");
		if (war.getState() != WarState.Waiting) {
			return;
		}
		war.updateState(WarState.Fighting);
	}

	private void warFinish(MapInfo mapInfo, IWar war) {
		LogHelper.GAME_LOGGER.info("【虫族入侵.战斗.结束】");
		BigMonsterWarInfo warInfo = (BigMonsterWarInfo) war;
		warInfo.setEnd(true);
	}

	public void doRiotWar(MapInfo mapInfo, IWar war) {
		LogHelper.GAME_LOGGER.info("【虫族入侵.战斗.doRiotWar】");
		int monsterId = (int) war.getAttacker().getId();
		StaticWorldMonster worldMonster = staticWorldMgr.getMonster(monsterId);
		if (worldMonster == null) {
			LogHelper.CONFIG_LOGGER.info("worldMonster is null.");
			return;
		}
		Player player = playerManager.getPlayer(war.getDefencer().getId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("target is null!");
			return;
		}

		WarInfo warInfo = (WarInfo) war;
		List<Integer> monsterIds = worldMonster.getMonsterIds();
		Team monsterTeam = battleMgr.initRiotMonsterTeam(monsterIds, BattleEntityType.ROIT_MONSTER, player);
		// 防守方: 援助 + 当前玩家的驻防武将 + 其他玩家驻防武将 + 城防军
		Team playerTeam = battleMgr.initDefencer(warInfo, BattleEntityType.HERO, false);
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(monsterTeam, playerTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		activityManager.calcuKillAll(warInfo, playerTeam, monsterTeam);

		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("attacker is null!");
			return;
		}

		if (!playerTeam.isWin()) {
			//不能继续了
			player.getSimpleData().setWaveContinue(true);
			//清除信息
			if (player.getSimpleData() != null) {
				player.getSimpleData().getRiotWar().clear();
			}
			WorldActPlan worldActPlan = roitService.getWorldRoitActPlan();
			eventManager.worldActRiot(player, 2,
				Lists.newArrayList(
					worldActPlan.getId(),
					player.getSimpleData().getAttackWave()
				));
		}
		//清除行军信息
		March march = player.getSimpleData().getRiotMarchs();
		if (march != null) {
			march.setState(MarchState.Done);
			worldManager.synMarchToPlayer(player, march);
			marchManager.backKey(march.getKeyId());
			player.getSimpleData().setRiotMarchs(null);
		}

		player.getSimpleData().setAttackWave(player.getSimpleData().getAttackWave() + 1);
		achievementService.addAndUpdate(player, AchiType.AT_24,player.getSimpleData().getAttackWave());
		// 防守者经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, playerTeam.getLordId());
		worldManager.caculateTeamDefenceKill(playerTeam);
		// 处理玩家扣血
		HashMap<Integer, Integer> defenceRec = new HashMap<Integer, Integer>();
		HashMap<Long, HashMap<Integer, Integer>> allDefenceRec = new HashMap<>();
		worldManager.handleDefenceSoldier(playerTeam, player, MarchReason.RIOT_WAR_ATTEND, allDefenceRec);
		defenceRec = allDefenceRec.get(player.getLord().getLordId());
		// 同步所有参战人员的属性变化, 处理士兵血量和威望
		HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
		handlePvpWarHp(warInfo, playerTeam, allSoldierRec, WarType.RIOT_WAR);
		// 根据结果不同发送不同的邮件给玩家
		worldManager.soldierAutoAdd(player);
		warManager.handePvpWarRemove(warInfo);  // 通知参战双方删除战斗
		battleMailManager.riotMail(playerTeam, monsterTeam, player,
			worldMonster.getId(), new ArrayList<Award>(),
			heroAddExp, allSoldierRec);
		//战斗结束 移除兵力减少的buff
		player.getSimpleData().getRiotBuff().remove(RiotBuff.LESSTROOPS);
		riotManager.synRiotBuff(player);
		//防守兵力返回
		warInfo.getDefenceMarches().forEach(march2 -> {
			Player target = playerManager.getPlayer(march2.getLordId());
			// 通知所有部队遣返
			marchManager.doWarMarchReturn(target, warInfo, Reason.RIOT_WAVE);
		});
		if (playerTeam.isWin()) {
			//更新通行证任务
			activityEventManager.activityTip(EventEnum.RIOT_WAR, player, 1, 0);
		}
	}

	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {

	}


}
