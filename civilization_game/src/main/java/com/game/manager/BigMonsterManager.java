package com.game.manager;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.pb.WorldPb;
import com.game.season.SeasonService;
import com.game.season.seven.entity.SevenType;
import com.game.util.*;
import com.game.worldmap.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author cpz 巨型虫族活动管理器
 */
@Component
public class BigMonsterManager {

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private BattleMgr battleMgr;
	@Autowired
	private StaticWorldMgr staticWorldMgr;
	@Autowired
	private WarManager warManager;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private BattleMailManager battleMailManager;
	@Autowired
	private WorldBoxManager worldBoxManager;
	@Autowired
	private StaticLimitMgr staticLimitMgr;
	@Autowired
	private MarchManager marchManager;

	public void checkBigMonster() {
		WorldActPlan worldActPlan = worldManager.getWorldActPlan(WorldActivityConsts.ACTIVITY_10);
		if (worldActPlan == null) {
			return;
		}
		flushMonster();
		long curent = TimeHelper.curentTime();
		for (Integer mapId : worldManager.getAllMap()) {
			MapInfo mapInfo = worldManager.getMapInfo(mapId);
			Iterator<BigMonster> it = mapInfo.getBigMonsterMap().values().iterator();
			while (it.hasNext()) {
				BigMonster bigMonster = it.next();
				if (bigMonster.getLeaveTime() <= curent) {
					it.remove();
					//时间到了 离开 6分钟后复活
					//巨型虫族到时撤退时，不做部队撤回处理，让战斗线程去处理部队撤回  遣返战斗
					// 清除野怪
					worldManager.clearRebelMonsterPos(mapInfo, bigMonster.getPos());
					// 同步野怪
					worldManager.synEntityRemove(bigMonster, mapInfo.getMapId(), bigMonster.getPos());
					//放入死亡队列
					bigMonster.setMapId(mapInfo.getMapId());
					bigMonster.setState(EntityState.DEATH.get());
					bigMonster.setRebornTime(System.currentTimeMillis() + 6 * TimeHelper.MINUTE_MS);
					mapInfo.getDeathMonsterMap().add(bigMonster);
				}
			}
		}
		checkDeathMonster(curent);
	}

	/**
	 * 检测虫子复活
	 *
	 * @param curent
	 */
	private void checkDeathMonster(long curent) {
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
		List<Entity> monsterList = new ArrayList<>();
		for (MapInfo mapInfo : worldMapInfo.values()) {
			List<BigMonster> deathMonsterMap = mapInfo.getDeathMonsterMap();
			if (deathMonsterMap != null) {
				Iterator<BigMonster> it = deathMonsterMap.iterator();
				while (it.hasNext()) {
					BigMonster bigMonster = it.next();
//                    StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(bigMonster.getMapId());
					if (bigMonster.getRebornTime() != 0 && bigMonster.getRebornTime() <= curent) {
						StaticGiantZerg staticGiantZerg = staticWorldMgr.getGiantZergMap().get(bigMonster.getId());
//                        List<StaticWorldMap> maps = worldMap.values().stream().filter(e -> e.getAreaType() == staticWorldMap.getAreaType()).collect(Collectors.toList());
//                        StaticWorldMap staticWorld = RandomUtil.getOneRandomElement(maps);
						MapInfo rebornMapInfo = worldMapInfo.get(mapInfo.getMapId());
						rebornMonster(rebornMapInfo, staticGiantZerg, monsterList);
						it.remove();
					}
				}
			}
		}
		worldManager.synEntityAddRq(monsterList);
	}

	/**
	 * 重生
	 *
	 * @param mapInfo
	 * @param staticGiantZerg
	 */
	private void rebornMonster(MapInfo mapInfo, StaticGiantZerg staticGiantZerg, List<Entity> list) {
		if (staticGiantZerg == null || staticGiantZerg.getRefreshArea() == null || staticGiantZerg.getRefreshArea().size() != 2) {
			return;
		}
		Pos monsterPos = mapInfo.randomAppointPos(staticGiantZerg.getRefreshArea().get(0), staticGiantZerg.getRefreshArea().get(1));
		if (monsterPos.isError()) {
			return;
		}
		// 创建一个野怪
		BigMonster bigMonster = worldManager.addBigMonster(monsterPos,
			staticGiantZerg.getId(),
			staticGiantZerg.getLevel(),
			mapInfo,
			AddMonsterReason.ADD_BIG_MONSTER);
		bigMonster.setLeaveTime(System.currentTimeMillis() + 180 * TimeHelper.MINUTE_MS);
		bigMonster.setState(EntityState.SURVIVAL.get());
		bigMonster.setSoldierType(staticGiantZerg.getSoldierType());

		StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster(Long.valueOf(staticGiantZerg.getId()).intValue());
		if (staticWorldMonster == null) {
			LogHelper.CONFIG_LOGGER.info("staticGiantZerg error->[{}]", staticGiantZerg.getId());
			return;
		}
		Team monsterTeam = battleMgr.initMonsterTeam(staticWorldMonster.getMonsterIds(), BattleEntityType.BIG_MONSTER);
		bigMonster.setTeam(monsterTeam);
		bigMonster.setTotalHp(monsterTeam.getLessSoldier());
		list.add(bigMonster);

	}

	public Integer getKey(String key) {
		WorldData worldData = worldManager.getWolrdInfo();
		try {
			if (!StringUtil.isNullOrEmpty(worldData.getBigMonster())) {
				JSONObject jsonObject = JSONObject.parseObject(worldData.getBigMonster());
				if (jsonObject.get(key) != null) {
					return jsonObject.getInteger(key);
				}
				return 0;
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	public synchronized void setKey(String key, int val) {
		WorldData worldData = worldManager.getWolrdInfo();
		try {
			JSONObject jsonObject = new JSONObject();
			if (!StringUtil.isNullOrEmpty(worldData.getBigMonster())) {
				jsonObject = JSONObject.parseObject(worldData.getBigMonster());
			}
			jsonObject.put(key, val);
			worldData.setBigMonster(jsonObject.toJSONString());
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(key, val);
			worldData.setBigMonster(jsonObject.toJSONString());
		}
	}

	/**
	 * 是否首次进来
	 *
	 * @return
	 */
	private boolean isFirstBigMonsters() {
		Integer val = getKey(BigMonsteKey.IS_FIRST);
		return val == 1;
	}

	/**
	 * 击杀虫子数量 分阵营 分平原
	 *
	 * @param country
	 * @param areaType
	 * @return
	 */

	public int getBigMonsterKill(int country, int areaType) {
		Integer val = getKey(getMonsterKillKey(country, areaType));
		return val;
	}

	/**
	 * 击杀虫族数量 key=key+阵营+地图
	 *
	 * @param country
	 * @param areaType
	 * @return
	 */
	public String getMonsterKillKey(int country, int areaType) {
		return BigMonsteKey.MONSTER_KILL + "_" + country + "_" + areaType;
	}


	/**
	 * 活动结束处理 清除下活动数据 等待下次开启
	 */
	public void endActivity() {
		WorldData worldData = worldManager.getWolrdInfo();
		worldData.setBigMonster("");
	}

	/**
	 * 活动开始刷新怪物
	 */
	public void flushMonster() {
		if (isFirstBigMonsters()) {
			return;
		}
		setKey(BigMonsteKey.IS_FIRST, 1);
		List<StaticGiantZerg> list = Lists.newArrayList(staticWorldMgr.getGiantZergMap().values());
		ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
		//刷新地图数据
		List<Entity> monsterList = new ArrayList<>();
		for (MapInfo mapInfo : worldMapInfo.values()) {
			StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapInfo.getMapId());
			if (staticWorldMap == null) {
				continue;
			}
			List<StaticGiantZerg> list1 = list.parallelStream().filter(e -> e.getType() == staticWorldMap.getAreaType()).collect(Collectors.toList());
			for (StaticGiantZerg staticGiantZerg : list1) {
				int num = staticGiantZerg.getNum();
				for (; num > 0; num--) {
//                    Pos monsterPos = mapInfo.randPickPos();
//                    if (monsterPos.isError() || !mapInfo.isFreePos(monsterPos)) {
//                        continue;
//                    }
					rebornMonster(mapInfo, staticGiantZerg, monsterList);
				}
			}
		}
		worldManager.synEntityAddRq(monsterList);
	}

	@Autowired
	SeasonService seasonService;
	/**
	 * 巨型虫族
	 *
	 * @param mapInfo
	 * @param warInfo
	 */
	public void doBigMonsterWar(MapInfo mapInfo, WarInfo warInfo) {
		BigMonster bigMonster = mapInfo.getBigMonsterMap().get(warInfo.getDefencerPos());
		//说明战争已经结束了
		if (bigMonster == null) {
			warInfo.getAttackMarches().forEach(march -> {
				warManager.handleLostTarget(march, null, MarchReason.CANCEL_BIGMONSTER_BACK);
				worldManager.synMarch(mapInfo.getMapId(), march);
			});
			return;
		}
		//拿出已经到位了的人 避免出现第三个
		List<March> readyList = warInfo.getAttackMarches().stream().collect(Collectors.toList());
		Team playerTeam = battleMgr.initBigMonsterWarTeam(readyList, true);
		Team monsterTeam = bigMonster.getTeam();
		StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) warInfo.getDefencerId());
		if (monsterTeam == null) {
			List<Integer> monsterIds = staticWorldMonster.getMonsterIds();
			monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.BIG_MONSTER);
			bigMonster.setTeam(monsterTeam);
			bigMonster.setTotalHp(monsterTeam.getLessSoldier());
		}
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		activityManager.calcuKillAll(warInfo, playerTeam, monsterTeam);
		// 计算经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, warInfo.getAttackerId());
		// 处理玩家扣血
		HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		warManager.handlRebeleWarChange(attackerMarches, playerTeam, allSoldierRec, WarType.REBEL_WAR);

		Map<Long, List<March>> marchMap = readyList.stream().collect(Collectors.groupingBy(e -> e.getLordId()));
		Player player = playerManager.getPlayer(warInfo.getAttackerId());
		if (playerTeam.isWin()) {
			// 清除野怪
			worldManager.clearRebelMonsterPos(mapInfo, bigMonster.getPos());
			//放入死亡队列
			bigMonster.setMapId(mapInfo.getMapId());
			bigMonster.setState(EntityState.DEATH.get());
			bigMonster.setRebornTime(System.currentTimeMillis() + 6 * TimeHelper.MINUTE_MS);
			mapInfo.getDeathMonsterMap().add(bigMonster);

			// 同步野怪
			worldManager.synEntityRemove(bigMonster, mapInfo.getMapId(), bigMonster.getPos());

			StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapInfo.getMapId());
			//设置杀敌数增加
			int kill = getBigMonsterKill(player.getCountry(), staticWorldMap.getMapId());
			setKey(getMonsterKillKey(player.getCountry(), staticWorldMap.getMapId()), kill + 1);

			for (Map.Entry<Long, List<March>> entry : marchMap.entrySet()) {
				Player p = playerManager.getPlayer(entry.getKey());
				activityManager.updActSeven(p, ActivityConst.TYPE_ADD, ActSevenConst.KILL_MONSTERS, 0, 1);
				seasonService.addSevenScore(SevenType.BIG_WORM, 1, player, bigMonster.getLevel());
			}
		}

		//记录玩家杀敌数量 发放首杀奖励
		int maxRward = staticLimitMgr.getNum(SimpleId.BIG_MONSTER_REWARD);
		StaticGiantZerg staticGiantZerg = staticWorldMgr.getGiantZergMap().get(warInfo.getDefencerId());
		Report report = battleMailManager.createKillMonsterReport(playerTeam, monsterTeam, player, bigMonster);

		//发放奖励
		for (Map.Entry<Long, List<March>> entry : marchMap.entrySet()) {
			Player p = playerManager.getPlayer(entry.getKey());
			int mailId = playerTeam.isWin() ? MailId.BIGMONSTER_WIN : MailId.BIGMONSTER_FAIL;
			if (p != null) {
				SimpleData simpleData = p.getSimpleData();
				boolean firstKill = false;
				if (!simpleData.isFirstBigMonsterReward() && playerTeam.isWin()) {
					simpleData.setFirstBigMonsterReward(true);
					List<Award> list = new ArrayList<>();
					staticGiantZerg.getFirstAward().forEach(e -> {
						list.add(new Award(e.get(0), e.get(1), e.get(2)));
					});
					entry.getValue().get(0).addAllAwards(list);
					firstKill = true;
				}
				if (simpleData.getBigMonsterReward() < maxRward) {
					simpleData.setBigMonsterReward(simpleData.getBigMonsterReward() + 1);
					List<Award> list = getMonsterAwards(staticGiantZerg.getAward());
					entry.getValue().get(0).addAllAwards(list);
				} else {
					if (!firstKill) {
						mailId = playerTeam.isWin() ? MailId.BIGMONSTER_MAX_WIN : MailId.BIGMONSTER_MAX_FAIL;
					}
				}
				ReportMsg reportMsg = battleMailManager.createReportMsg(playerTeam, monsterTeam, entry.getValue().get(0).getAwards(), heroAddExp);
				//邮件
				String name = p.getNick();
				String pos = p.getPosStr();
				String lv = String.valueOf(bigMonster.getLevel());
				String monsterPosStr = bigMonster.getPosStr();
				String ironStr = String.valueOf(0);
				String copperStr = String.valueOf(0);
				//添加worldMonster的ID
				String worldMonster = String.valueOf(bigMonster.getId());
				playerManager.sendReportMailOnActivity(p, report, reportMsg, mailId, entry.getValue().get(0).getAwards(), null, 1, name, pos, lv, monsterPosStr, name, ironStr, copperStr, worldMonster);
				playerManager.synChange(p, Reason.KILL_REBEL_MONSTER);
			}
		}
		/**1.只要发生了战斗 无论输赢都移除该战斗
		 * 2.只通知该战斗内的部队返回，其他战斗的部队不管
		 */
		warInfo.setState(WarState.Finish);
		for (March m : warInfo.getAttackMarches()) {
			marchManager.handleMarchReturn(m, MarchReason.CANCEL_BIGMONSTER_BACK);
			worldManager.synMarch(mapInfo.getMapId(), m);
		}
		//全部遣返
		if (!playerTeam.isWin()) {
			monsterTeam.reset();
			synEntityUpdateRq(bigMonster);
		}
		playerManager.clearPos(bigMonster.getPos());
	}

	public List<Award> getMonsterAwards(List<List<Integer>> staticDropList) {
		List<Award> awards = new ArrayList<Award>();
		List<List<Integer>> dropList = new ArrayList<>();
		staticDropList.forEach(e -> {
			dropList.add(new ArrayList(e));
		});

		if (dropList != null && dropList.size() >= 1) {
			// 先计算总权重
			int total = 0;
			for (List<Integer> itemLoot : dropList) {
				if (itemLoot == null || itemLoot.size() != 4) {
					continue;
				}
				total += itemLoot.get(3);
			}

			int randNum = RandomHelper.threadSafeRand(1, total);
			//logger.error("击杀世界野怪随机掉落物品的randNum>>>>>>>>>>>"+randNum);
			int checkNum = 0;
			for (List<Integer> itemLoot : dropList) {
				if (itemLoot == null || itemLoot.size() != 4) {
					continue;
				}
				int type = itemLoot.get(0);
				int id = itemLoot.get(1);
				int count = itemLoot.get(2);
				checkNum += itemLoot.get(3);
				if (randNum <= checkNum) {
					if (type == AwardType.EMPTY_LOOT) {
						return awards;
					}
					// 空掉落不加入
					awards.add(new Award(0, type, id, count));
					break;
				}
			}
		}
		return awards;
	}

	public int getMonsterBuff(int mapId, Player player) {
		StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
		int kill = getBigMonsterKill(player.getCountry(), staticWorldMap.getMapId());
		StaticGiantZergBuff buff = staticWorldMgr.getGiantZergBuffMap().get(staticWorldMap.getAreaType());
		if (kill >= buff.getNeedNum()) {
			return buff.getValue();
		}
		return 0;
	}

	/**
	 * 推送实体类更新 针对巨型虫族
	 *
	 * @param entity
	 */
	public void synEntityUpdateRq(Entity entity) {
		if (entity == null) {
			return;
		}
		WorldPb.SynEntityUpdateRq.Builder builder = WorldPb.SynEntityUpdateRq.newBuilder();
		builder.setEntity(entity.wrapPb());
		WorldPb.SynEntityUpdateRq req = builder.build();
		playerManager.getOnlinePlayer().forEach(target -> {
//            if (target.getPushPos().containsKey(entity.getPos().toPosStr())) {
			SynHelper.synMsgToPlayer(target, WorldPb.SynEntityUpdateRq.EXT_FIELD_NUMBER, WorldPb.SynEntityUpdateRq.ext, req);
//            }
		});

	}
}
