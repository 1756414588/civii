package com.game.worldmap.fight.process;

import com.game.constant.LostTargetReason;
import com.game.constant.MarchState;
import com.game.constant.Reason;
import com.game.constant.WarState;
import com.game.constant.WarType;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticWorldMonster;
import com.game.log.consumer.EventManager;
import com.game.manager.BigMonsterManager;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerQuickWar;
import com.game.util.LogHelper;
import com.game.worldmap.BigMonster;
import com.game.worldmap.Entity;
import com.game.worldmap.EntityType;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.BigMonsterWarInfo;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "巨型虫族", warType = {WarType.BIGMONSTER_WAR}, marthes = {MarchType.BigWar})
@Component
public class BigMonsterProcess extends FightProcess {

	@Autowired
	private EventManager eventManager;
	@Autowired
	private BigMonsterManager bigMonsterManager;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		// 注册战斗
		registerProcess(WarState.Waiting, this::wating);
		registerProcess(WarState.Fighting, this::doWar);
		registerProcess(WarState.Finish, this::warFinish);

		// 注册行军
		registerMarch(MarchType.BigWar, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.BigWar, MarchState.Back, this::doFinishedMarch);
	}

	private void marchArrive(MapInfo mapInfo, March march) {
		IWar war = march.getWarId() != 0 ? mapInfo.getWar(march.getWarId()) : mapInfo.getWarMap().values().stream().filter(e -> e.getWarType() == WarType.BIGMONSTER_WAR).filter(e -> {
			BigMonsterWarInfo warInfo = (BigMonsterWarInfo) e;
			return warInfo.getPos().equals(march.getEndPos()) && warInfo.getCountry() == march.getCountry();
		}).findFirst().orElse(null);

		Entity entity = mapInfo.getEntity(march.getEndPos());
		if (entity == null || !(entity instanceof BigMonster)) {//目标已经丢失
//			LogHelper.MESSAGE_LOGGER.info("巨型虫族--行军再次抵达  目标丢失：{} ", march.getEndPos());
			if (war != null) {//则移除战斗队列
				war.getAttacker().getMarchList().remove(march);
			}
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.MONSTER_ENTITY_NULL);
			return;
		}

		Player player = playerManager.getPlayer(march.getLordId());
		if (war != null) {
			BigMonsterWarInfo warInfo = (BigMonsterWarInfo) war;
			LogHelper.MESSAGE_LOGGER.info("巨型虫族--行军再次抵达 战斗ID：{} 战斗状态:{}", warInfo.getWarId(), warInfo.getState());
			if (war.getState() == WarState.Fighting || war.getAttacker().getMarchList().contains(march)) {
				return;
			}
			march.setState(MarchState.Waiting);
			worldManager.synMarch(mapInfo.getMapId(), march);
			int size = warInfo.getAttackMarches().stream().collect(Collectors.groupingBy(e -> e.getLordId())).size();
			if (size < 2) {
				march.setEndTime(march.getFightTime() + 1000L);
				warInfo.getAttackMarches().add(march);

				if (size >= 1) {// 新增之后人数大于等于二，可开战
					warInfo.setState(WarState.Fighting);
				}
			}
		} else {
			StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) entity.getId());
			if (staticWorldMonster == null) {
				warManager.handleLostTarget(march, mapInfo, LostTargetReason.MONSTER_ENTITY_NULL);
				return;
			}

			if (staticWorldMonster.getType() == EntityType.BIG_MONSTER) {
				BigMonsterWarInfo bigMonsterWarInfo = warManager.createBigMonsterWar(player.getLord().getLordId(), player.getCountry(), staticWorldMonster.getId(), player.getPos(), new Pos(march.getEndPos().getX(), march.getEndPos().getY()), WarType.BIGMONSTER_WAR, mapInfo);
				march.setState(MarchState.Waiting);
				march.setEndTime(march.getFightTime() + 1000L);
				bigMonsterWarInfo.getAttackMarches().add(march);
				worldManager.synMarch(mapInfo.getMapId(), march);
				LogHelper.MESSAGE_LOGGER.info("巨型虫族--行军首次抵达 战斗ID：{} 战斗状态:{} endTime:{}", bigMonsterWarInfo.getWarId(), bigMonsterWarInfo.getState(), bigMonsterWarInfo.getEndTime());
			}
		}
	}

	/**
	 * 等待时间到了,没有人则直接结束
	 *
	 * @param mapInfo
	 * @param war
	 */
	private void wating(MapInfo mapInfo, IWar war) {
		BigMonster bigMonster = mapInfo.getBigMonsterMap().get(war.getDefencer().getPos());
		//bigMonster 为null说明该虫族已被消灭

		// 未到开战时间
		if (bigMonster != null && war.getEndTime() > System.currentTimeMillis()) {
			return;
		}
		//遣返部队
		if (!war.getAttacker().getMarchList().isEmpty()) {
			war.getAttacker().getMarchList().forEach(e -> {
				marchManager.handleMarchReturn(e, Reason.KILL_WORLD_MONSTER);
				worldManager.synMarch(mapInfo.getMapId(), e);
			});
		}

		// 战斗结束
		BigMonsterWarInfo warInfo = (BigMonsterWarInfo) war;
		worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
		warInfo.setEnd(true);
	}

	private void warFinish(MapInfo mapInfo, IWar war) {
		BigMonsterWarInfo warInfo = (BigMonsterWarInfo) war;
		warInfo.setEnd(true);
	}

	private void doWar(MapInfo mapInfo, IWar war) {
		BigMonsterWarInfo warInfo = (BigMonsterWarInfo) war;
		bigMonsterManager.doBigMonsterWar(mapInfo, warInfo);
		worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
	}

//    /**
//     * 伏击叛军
//     *
//     * @param mapInfo
//     * @param warInfo
//     */
//    public void doRebelWar(MapInfo mapInfo, WarInfo warInfo) {
//        RebelMonster rebelMonster = mapInfo.getRebelMap().get(warInfo.getDefencerPos());
//        //说明战争已经结束了
//        if (rebelMonster == null) {
//            warManager.handleRebelMarchReturn(mapInfo, warInfo);
//            return;
//        }
//        Team playerTeam = battleMgr.initAttackerWarTeam(warInfo, true);
//        StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) warInfo.getDefencerId());
//        List<Integer> monsterIds = staticWorldMonster.getMonsterIds();
//        Team monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.REBEL_MONSTER);
//        Random rand = new Random(System.currentTimeMillis());
//        // seed 开始战斗
//        battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
//        activityManager.calcuKillAll(warInfo, playerTeam, monsterTeam);
//        // 计算经验值
//        HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, warInfo.getAttackerId());
//        // 处理玩家扣血
//        HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
//        ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
//        Player player = playerManager.getPlayer(warInfo.getAttackerId());
//        warManager.handlRebeleWarChange(attackerMarches, playerTeam, allSoldierRec, WarType.REBEL_WAR);
//        //掉落
//        List<Award> awardList = staticWorldMonster.getAwards();
//        Map<Long, Boolean> attackMap = new HashMap<>();
//        for (March march : warInfo.getAttackMarches()) {
//            attackMap.put(march.getLordId(), true);
//        }
//        if (playerTeam.isWin()) {
//            // 清除野怪
//            worldManager.clearRebelMonsterPos(mapInfo, rebelMonster.getPos());
//
//            // 同步野怪
//            worldManager.synEntityRemove(rebelMonster, mapInfo.getMapId(), rebelMonster.getPos());
//
//            Award iron1 = new Award(AwardType.RESOURCE, ResourceType.IRON, staticWorldMonster.getIron());
//            Award copper1 = new Award(AwardType.RESOURCE, ResourceType.COPPER, staticWorldMonster.getCopper());
//            awardList.add(iron1);
//            awardList.add(copper1);
//            // 全区域广播
//            List<Long> players = new ArrayList<>();
//            for (March march : warInfo.getAttackMarches()) {
//                //一场战斗 伏击叛军不管出兵几次 只发一次奖励
//                if (!players.contains(march.getLordId())) {
//                    march.addAllAwards(awardList);
//                    players.add(march.getLordId());
//                }
//                //回城
//                warManager.handleRebelMarchReturn(march, MarchReason.KILL_REBEL_MONSTER_SUCCESS);
//                //同步
//                worldManager.synMarch(mapInfo.getMapId(), march);
//                Player p = playerManager.getPlayer(march.getLordId());
//
//                eventManager.worldActRebel(p, 1, Lists.newArrayList(
//                        WorldActivityConsts.ACTIVITY_2,
//                        staticWorldMonster.getId(),
//                        staticWorldMonster.getLevel()));
//            }
//            //记录所有玩家击杀叛军数量
//            players.forEach(playerId -> {
//                Player p = playerManager.getPlayer(playerId);
//                if (p != null) {
//                    p.addKillRebel();
//                    //杀敌贡献值变化
//                    worldBoxManager.calcuPoints(WorldBoxTask.KILL_MONSTER, p, 1);
//                }
//            });
//            warManager.cancelRebelWar(rebelMonster, mapInfo, warInfo.getWarId());
//        } else {
//            // 全区域广播
//            for (March march : warInfo.getAttackMarches()) {
//                //回城
//                warManager.handleRebelMarchReturn(march, MarchReason.KILL_REBEL_MONSTER_FAIL);
//                //同步
//                worldManager.synMarch(mapInfo.getMapId(), march);
//            }
//        }
//
//        playerManager.synChange(player, Reason.KILL_REBEL_MONSTER);
//        battleMailManager.handleSendKillRebelMonster(warInfo, playerTeam, monsterTeam, player, rebelMonster, awardList, heroAddExp, staticWorldMonster.getIron(), staticWorldMonster.getCopper(), allSoldierRec);
//    }


	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
		if (worldMap.getBigMonsterWarData() == null) {
			return;
		}
		SerQuickWar serWar = null;
		try {
			serWar = SerQuickWar.parseFrom(worldMap.getBigMonsterWarData());
			if (serWar == null) {
				return;
			}
			List<DataPb.WarData> warDatas = serWar.getWarDataList();
			if (warDatas == null) {
				return;
			}
			for (DataPb.WarData warData : warDatas) {
				if (warData == null) {
					continue;
				}
				DataPb.PosData dataPos = warData.getDefencerPos();
				if (dataPos.getX() == 0 && dataPos.getY() == 0) {
					continue;
				}

				WarInfo warInfo = warManager.createBigMonsterWar(warData, mapInfo);

				Pos pos = new Pos(dataPos.getX(), dataPos.getY());
				Entity entity = mapInfo.getEntity(pos);
				if (entity == null || !(entity instanceof BigMonster)) {
					continue;
				}

				mapInfo.addWar(warInfo);


				if (warInfo.getAttackerCountry() != 0) {
					worldManager.flushWar(warInfo, true, warInfo.getAttackerCountry());
				}
			}
		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}
}
