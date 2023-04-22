package com.game.worldmap.fight.process;

import com.game.constant.*;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.HeroAddExp;
import com.game.domain.p.Team;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticWorldMonster;
import com.game.log.consumer.EventManager;
import com.game.service.RebelService;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.RebelMonster;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "叛军活动", warType = {WarType.REBEL_WAR}, marthes = {MarchType.ReBelWar})
@Component
public class RebelProcess extends FightProcess {

	@Autowired
	private RebelService rebelService;
	@Autowired
	private EventManager eventManager;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		// 注册战斗
		registerProcess(WarState.Waiting, this::warWating);
		registerProcess(WarState.Fighting, this::warProcess);
		registerProcess(WarState.Finish, this::warFinish);

		// 注册行军
		registerMarch(MarchType.ReBelWar, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.ReBelWar, MarchState.Waiting, this::marchWating);
		registerMarch(MarchType.ReBelWar, MarchState.Back, this::doFinishedMarch);
	}

	/**
	 * 行军抵达
	 *
	 * @param mapInfo
	 * @param march
	 */
	private void marchArrive(MapInfo mapInfo, March march) {
		if (march.getState() != MarchState.Begin) {
			return;
		}
		Map<Long, WarInfo> rebelWarMap = mapInfo.getRebelWarMap();
		WarInfo rebelWar = rebelWarMap.get(march.getWarId());
		if (rebelWar == null) {//战斗已取消
			marchManager.handleMarchReturn(march, Reason.KILL_REBEL_MONSTER);
			return;
		}

		long now = System.currentTimeMillis();

		if (rebelWar.getEndTime() <= now) {
			marchManager.handleMarchReturn(march, Reason.KILL_REBEL_MONSTER);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}

		march.setState(MarchState.Waiting);
		march.setPeriod(rebelWar.getEndTime() - now);
		march.setEndTime(System.currentTimeMillis() + march.getPeriod());
		march.setWarId(rebelWar.getWarId());
		// 同步叛军战斗信息
		worldManager.handleRebelWarSoldier(rebelWar);
		warManager.synRebelWarInfo(rebelWar);
		// 同步行军信息
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void marchWating(MapInfo mapInfo, March march) {
		if (march.getState() != MarchState.Waiting) {
			return;
		}
		march.setState(MarchState.Fighting);
		march.setPeriod(1000L);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void warWating(MapInfo mapInfo, IWar war) {
		if (war.getState() != WarState.Waiting) {
			return;
		}
		// 战斗还未开始
		if (war.getEndTime() > System.currentTimeMillis()) {
			return;
		}
		war.updateState(WarState.Fighting);
	}

	private void warFinish(MapInfo mapInfo, IWar war) {
		WarInfo warInfo = (WarInfo) war;
		warInfo.setEnd(true);
	}

	private void warProcess(MapInfo mapInfo, IWar war) {
		WarInfo warInfo = (WarInfo) war;
		RebelMonster rebelMonster = mapInfo.getRebelMap().get(warInfo.getDefencerPos());

		if (rebelMonster == null) {//目标已不存在
			warManager.handleRebelMarchReturn(mapInfo, warInfo);
			return;
		}

		Team playerTeam = battleMgr.initAttackerWarTeam(warInfo, true);
		StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) warInfo.getDefencerId());
		List<Integer> monsterIds = staticWorldMonster.getMonsterIds();
		Team monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.REBEL_MONSTER);
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		activityManager.calcuKillAll(warInfo, playerTeam, monsterTeam);
		// 计算经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, warInfo.getAttackerId());
		// 处理玩家扣血
		HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		Player player = playerManager.getPlayer(warInfo.getAttackerId());
		warManager.handlRebeleWarChange(attackerMarches, playerTeam, allSoldierRec, WarType.REBEL_WAR);
		//掉落
		List<Award> awardList = staticWorldMonster.getAwards();
		Map<Long, Boolean> attackMap = new HashMap<>();
		for (March march : warInfo.getAttackMarches()) {
			attackMap.put(march.getLordId(), true);
		}
		if (playerTeam.isWin()) {
			// 清除野怪
//			worldManager.clearRebelMonsterPos(mapInfo, rebelMonster.getPos());
//
//			// 同步野怪
//			worldManager.synEntityRemove(rebelMonster, mapInfo.getMapId(), rebelMonster.getPos());
			mapInfo.clearPos(rebelMonster.getPos());
			Award iron1 = new Award(AwardType.RESOURCE, ResourceType.IRON, staticWorldMonster.getIron());
			Award copper1 = new Award(AwardType.RESOURCE, ResourceType.COPPER, staticWorldMonster.getCopper());
			awardList.add(iron1);
			awardList.add(copper1);
			// 全区域广播
			List<Long> players = new ArrayList<>();
			for (March march : warInfo.getAttackMarches()) {
				//一场战斗 伏击叛军不管出兵几次 只发一次奖励
				if (!players.contains(march.getLordId())) {
					march.addAllAwards(awardList);
					players.add(march.getLordId());
				}
				//回城
				warManager.handleRebelMarchReturn(march, MarchReason.KILL_REBEL_MONSTER_SUCCESS);
				//同步
				worldManager.synMarch(mapInfo.getMapId(), march);
				Player p = playerManager.getPlayer(march.getLordId());

				eventManager.worldActRebel(p, 1, Lists.newArrayList(
					WorldActivityConsts.ACTIVITY_2,
					staticWorldMonster.getId(),
					staticWorldMonster.getLevel()));
			}
			//记录所有玩家击杀叛军数量
			players.forEach(playerId -> {
				Player p = playerManager.getPlayer(playerId);
				if (p != null) {
					p.addKillRebel();
					//杀敌贡献值变化
					worldBoxManager.calcuPoints(WorldBoxTask.KILL_MONSTER, p, 1);
				}
			});
			warManager.cancelRebelWar(rebelMonster, mapInfo, warInfo.getWarId());

		} else {
			// 全区域广播
			for (March march : warInfo.getAttackMarches()) {
				//回城
				warManager.handleRebelMarchReturn(march, MarchReason.KILL_REBEL_MONSTER_FAIL);
				//同步
				worldManager.synMarch(mapInfo.getMapId(), march);
			}
		}

		playerManager.synChange(player, Reason.KILL_REBEL_MONSTER);
		battleMailManager.handleSendKillRebelMonster(warInfo, playerTeam, monsterTeam, player, rebelMonster, awardList, heroAddExp, staticWorldMonster.getIron(), staticWorldMonster.getCopper(), allSoldierRec);

		// 战斗结束
		warInfo.setState(WarState.Finish);
		// 推送战行信息
		warManager.synRebelWarInfo(warInfo);
		warManager.synRebelWarInfoToWorld(warInfo);
		worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
	}

	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
	}

}
