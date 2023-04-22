package com.game.service;

import com.game.constant.AddMonsterReason;
import com.game.constant.MarchState;
import com.game.constant.SimpleId;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticRiotMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.p.WorldTargetTask;
import com.game.domain.s.StaticRiotMonster;
import com.game.domain.s.StaticWorldActPlan;
import com.game.domain.s.StaticWorldMap;
import com.game.manager.PlayerManager;
import com.game.manager.RiotManager;
import com.game.dataMgr.StaticActRoitManager;
import com.game.manager.WorldManager;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.EntityType;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.Monster;
import com.game.worldmap.Pos;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 虫族入侵
 */
@Service
public class RoitService {

	private static Logger logger = LoggerFactory.getLogger(RoitService.class);
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticActRoitManager manager;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private RiotManager riotManager;
	@Autowired
	private StaticRiotMgr staticRiotMgr;

	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;
	@Autowired
	private WorldActPlanService worldActPlanService;

	private Map<Integer, RoitActPlan> roitActPlanMap = new HashMap<>();

	@PostConstruct
	void init() {
		this.roitActPlanMap.put(WorldActPlanConsts.NOE_OPEN, this::noOpen);
//        this.roitActPlanMap.put(WorldActPlanConsts.PREHEAT, this::preheat);
		this.roitActPlanMap.put(WorldActPlanConsts.OPEN, this::open);
		this.roitActPlanMap.put(WorldActPlanConsts.DO_END, this::end);
	}

	/**
	 * 虫族入侵活动
	 */
	private interface RoitActPlan {

		void action(WorldActPlan worldActPlan);
	}

	// 检查活动是否进行
	public void checkAct() {
		//活动是否加载完成
		if (worldManager.isNotOk()) {
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		//判断是否有虫族入侵
		WorldActPlan worldActPlan = getWorldRoitActPlan();
		if (worldActPlan == null) {
			return;
		}
//		LogHelper.GAME_LOGGER.info("【虫族入侵】 活动状态:{} 开启时间:{} 结束时间:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());
		WorldTargetTask worldTargetTask = worldData.getTasks().get(staticWorldActPlan.getTargetId());
		if (worldTargetTask == null) {
			worldData.getWorldActPlans().remove(worldActPlan.getId());
			return;
		}
		//根据活动对应状态执行对应逻辑
		RoitActPlan plan = this.roitActPlanMap.get(worldActPlan.getState());
		if (plan != null) {
			plan.action(worldActPlan);
		}
	}

	public WorldActPlan getWorldRoitActPlan() {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlanTen = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_8);
		if (worldActPlanTen != null) {
			return worldActPlanTen;
		}
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_7);
		return worldActPlan;
	}

	/**
	 * 虫族入侵 未开启
	 *
	 * @param worldActPlan
	 */
	private void noOpen(WorldActPlan worldActPlan) {
		//无预热 直接开
		if (System.currentTimeMillis() > worldActPlan.getOpenTime()) {
			worldActPlan.setState(WorldActPlanConsts.OPEN);
			worldActPlanService.syncWorldActivityPlan();
			initRoitMonsterLevel(worldActPlan);
			LogHelper.GAME_LOGGER.info("【虫族入侵】 活动状态:{} 开启时间:{} 实际开始时间:{} 结束时间:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(System.currentTimeMillis()), DateHelper.getDate(worldActPlan.getEndTime()));
		}
	}

	public void initRoitMonsterLevel(WorldActPlan worldActPlan) {
		int readType = 1;
		StaticWorldActPlan actPlan = worldActPlanMgr.get(worldActPlan.getId());
		if (actPlan.getTargetId() == WorldActivityConsts.ACTIVITY_10) {
			readType = 2;
		}
		WorldData data = worldManager.getWolrdInfo();
		int riotLevel = data.getRiotLevel();
		if (riotLevel == 0 && readType == 1) {    //新服 第一次
			riotLevel = 1;
		} else if (riotLevel == 0 && readType == 2) {   //老服 新开的
			riotLevel = 3;
		} else {
			riotLevel++;
		}
		checkRiotMonster(data, riotLevel, 0);
	}

	public boolean checkRiotMonster(WorldData data, int riotLevel, int countDown) {
		if (countDown >= 5) {
			data.setRiotLevel(2);
			return true;
		}
		StaticRiotMonster riotMonster = staticRiotMgr.getRoitMonster(riotLevel);
		if (riotMonster != null) {
			data.setRiotLevel(riotLevel);
			return true;
		}
		return checkRiotMonster(data, riotLevel - 1, countDown++);
	}

	/**
	 * 虫族入侵 预热阶段
	 *
	 * @param worldActPlan
	 */
	private void preheat(WorldActPlan worldActPlan) {
		if (System.currentTimeMillis() > worldActPlan.getOpenTime()) {
			worldActPlan.setState(WorldActPlanConsts.OPEN);
			worldActPlanService.syncWorldActivityPlan();
			// logger.info("虫族入侵从初始状态到开始状态 开始时间{} 结束时间{}", DateHelper.getDate(System.currentTimeMillis()), DateHelper.getDate(worldActPlan.getEndTime()));
		}
	}

	/**
	 * 虫族入侵 战斗阶段
	 *
	 * @param worldActPlan
	 */
	private void open(WorldActPlan worldActPlan) {
		//判定下当前处于什么阶段
		long now = System.currentTimeMillis();
		if (System.currentTimeMillis() > worldActPlan.getEndTime()) {
			worldActPlan.setState(WorldActPlanConsts.DO_END);
			return;
		}
		StaticWorldActPlan actPlan = worldActPlanMgr.get(worldActPlan.getId());
		long stageOneTime = actPlan.getContinues().get(0) * 60 * 60 * 1000;
		long pastTime = now - worldActPlan.getOpenTime();
//        logger.info("虫族入侵开启->[{}] 第一阶段经过时间->[{}],[{}]",DateHelper.getDate(worldActPlan.getOpenTime()),pastTime,stageOneTime);
		stageOne(worldActPlan, actPlan);
		//第一阶段过了
		if (pastTime >= stageOneTime) {
			//执行第二阶段的任务
			stageTwo(worldActPlan, actPlan);
		}
	}

	/**
	 * 虫族入侵 结束阶段
	 *
	 * @param worldActPlan
	 */
	private void end(WorldActPlan worldActPlan) {
		//清空野外刷新的虫族入侵部队
		//清空玩家buff
		playerManager.getAllPlayer().values().forEach(e -> {
			if (e.getSimpleData() != null) {
				March march = e.getSimpleData().getRiotMarchs();
				if (march != null) {
					march.setState(MarchState.Done);
					march.setEndTime(System.currentTimeMillis());
					worldManager.synMarchToPlayer(e, march);
				}
				e.getSimpleData().clearRiotData();
			}
		});
		boolean isAllClear = clearRiotMonster();
		if (isAllClear) {
			//检测下次开启时间
			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());
			if (staticWorldActPlan.getRoundType() == 0) {
				//清空
				worldActPlan.setState(WorldActPlanConsts.OVER);
				return;
			}
			worldActPlanService.activityEnd(worldActPlan);
			worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		}
	}

	private boolean clearRiotMonster() {
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
		int count = 0;
		isAll:
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			int mapId = staticWorldMap.getMapId();
			MapInfo mapInfo = worldMapInfo.get(mapId);
			if (mapInfo == null) {
				continue;
			}

//            Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
			List<Monster> riotMonsters = mapInfo.getMonsterMap().values().stream().filter(e -> e.getEntityType() == EntityType.RIOT_MONSTER).collect(Collectors.toList());
			Iterator<Monster> iterator = riotMonsters.iterator();
			while (iterator.hasNext()) {
				Monster monster = iterator.next();
				if (monster.getEntityType() == EntityType.RIOT_MONSTER) {
//					worldManager.clearMonsterPos(mapInfo, monster.getPos());
//					// 同步野怪
//					worldManager.synEntityRemove(monster, mapInfo.getMapId(), monster.getPos());
					mapInfo.clearPos(monster.getPos());
					count++;
					if (count >= 20) {
						break isAll;
					}
				}
			}
		}
		if (count == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 刷新普通怪物
	 */
	private void refushWildMonster(StaticWorldActPlan staticWorldActPlan) {
		//一小时刷新一次
		if (System.currentTimeMillis() - manager.getRefushTime() < (TimeHelper.HOUR_MS / 2)) {
			return;
		}
		manager.setRefushTime(System.currentTimeMillis());
		int readType = 1;
		if (staticWorldActPlan.getTargetId() == WorldActivityConsts.ACTIVITY_10) {
			readType = 2;
		}

		int monsterNumRange = staticLimitMgr.getNum(SimpleId.WORLD_MONSTER_NUM) + 2;
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			flushPlayerMonster(staticWorldActPlan, player, readType, monsterNumRange);
		}
	}

	/**
	 * 虫族入侵第一阶段 持续性刷新 普通怪
	 *
	 * @param worldActPlan
	 * @param staticWorldActPlan
	 */
	private void stageOne(WorldActPlan worldActPlan, StaticWorldActPlan staticWorldActPlan) {
//        refushWildMonster(staticWorldActPlan);
		WorldData data = worldManager.getWolrdInfo();
		int readType = data.getRiotLevel();
		//半小时刷新一次怪物
		if (worldActPlan.getRefushTime() != 0) {
			if (TimeHelper.getCurrentSecond() < TimeHelper.HALF_HOUR_S + worldActPlan.getRefushTime()) {
				return;
			}
		}
		worldActPlan.setRefushTime(TimeHelper.getCurrentSecond());
		riotManager.flushRiotByKeyId(readType);
	}


	/**
	 * 虫族入侵第二阶段
	 *
	 * @param worldActPlan
	 * @param staticWorldActPlan
	 */
	private void stageTwo(WorldActPlan worldActPlan, StaticWorldActPlan staticWorldActPlan) {
		WorldData data = worldManager.getWolrdInfo();
		int riotLevel = data.getRiotLevel();
		//刷新攻城怪物
//        refushAttackMonster(staticWorldActPlan);
		stageOne(worldActPlan, staticWorldActPlan);
		riotManager.flushWaveMonster(riotLevel, staticWorldActPlan);
		riotManager.checkRiotWar();
	}

	/**
	 * 刷新普通 虫族
	 *
	 * @param staticWorldActPlan
	 * @param player
	 */
	private void flushPlayerMonster(StaticWorldActPlan staticWorldActPlan, Player player, int stage, int monsterNumRange) {
		// 当前玩家周围的野怪
		int monsterNum = worldManager.getMonsterNum(player);
		// 剩余野怪
		// 周围虫族生成个数
		int leftNum = monsterNumRange - monsterNum;
		if (leftNum <= 0) {
			return;
		}
		// 当前地图Id
		int mapId = worldManager.getMapId(player);
		StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
		// 当前地图信息
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		// 当前地图类型
		int areaType = worldManager.getMapAreaType(mapId);

		int totalWeight = 0;
		// 玩家能刷的剩余等级
		List<Integer> leftMonsterLv = new ArrayList<Integer>();
		// 计算总的权重
		for (Table.Cell<Integer, Integer, Integer> set : manager.getMonster(stage).cellSet()) {
			if (set.getRowKey() == areaType) {
				leftMonsterLv.add(set.getColumnKey());
				totalWeight += set.getValue();
			}
		}
		int cellNum = staticLimitMgr.getNum(SimpleId.WORLD_MONSTER_CELL_NUM);
		while (leftNum > 0) {
			// 给玩家随机一个等级怪
			int randNum = RandomHelper.randMonster(totalWeight);
			int checkNum = 0;
			for (int i = 0; i < leftMonsterLv.size(); i++) {
				int monsterLv = leftMonsterLv.get(i);
				// 权重检查
				checkNum += manager.getMonster(stage).get(areaType, monsterLv);
				if (randNum > checkNum) {
					continue;
				}

				// 随机到一个等级, 并检查当前等级是否达到上限
				if (mapInfo.getMonsterNum(i) >= manager.getMonster(stage).get(areaType, monsterLv)) {
					// 如果达到上限，当前玩家随机到的数量减1
					leftNum--;
					break;
				}

				// 没有达到上限，则创建一个野怪
				Pos monsterPos = mapInfo.randPos(player, cellNum);

				//if (!worldManager.isPosOk(monsterPos, staticWorldMap, mapInfo)) {
				//	continue;
				//}
				
				// 创建一个野怪
				Monster monster = worldManager.addMonster(monsterPos, monsterLv, monsterLv, mapInfo, AddMonsterReason.ADD_PLAYER_MONSTER);
				if (monster == null) {
					continue;
				}

//                worldManager.synEntityAddRq(monster, mapInfo.getMapId());
				leftNum--;
				break;
			}

		}
	}
}
