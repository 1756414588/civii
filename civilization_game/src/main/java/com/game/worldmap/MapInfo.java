package com.game.worldmap;

import com.game.constant.CityType;
import com.game.constant.MapId;
import com.game.constant.MarchState;
import com.game.constant.WarType;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticWorldMap;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerCountryWar;
import com.game.pb.SerializePb.SerFarWar;
import com.game.pb.SerializePb.SerMapInfo;
import com.game.pb.SerializePb.SerQuickWar;
import com.game.pb.SerializePb.SerZergWar;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

// 地图信息, 都需要存盘
@Getter
@Setter
public class MapInfo {

	private int mapId;

	// 玩家城池信息
	private Map<Pos, PlayerCity> playerCityMap = new ConcurrentHashMap<Pos, PlayerCity>();
	//伏击叛军信息
	private ConcurrentHashMap<Long, WarInfo> rebelWarMap = new ConcurrentHashMap<>();
	// 战斗集合
	private ConcurrentHashMap<Long, IWar> warMap = new ConcurrentHashMap<Long, IWar>();
	// 行军定时器, 行军，击杀流寇、采集、城战、国战
	private ConcurrentHashMap<Integer, March> marches = new ConcurrentHashMap<>();

	// 流寇信息
	private Map<Pos, Monster> monsterMap = new ConcurrentHashMap<>();
	// 叛军信息
	private Map<Pos, RebelMonster> rebelMap = new ConcurrentHashMap<>();
	// 资源信息
	private Map<Pos, Resource> resourceMap = new HashMap<Pos, Resource>();
	// 地图的城池信息
	private Map<Pos, CityInfo> cityInfos = new HashMap<Pos, CityInfo>();
	// 已占用坐标
	private ConcurrentHashMap<Pos, Entity> posTake = new ConcurrentHashMap<Pos, Entity>();
	// 未占用坐标
	private ConcurrentHashMap<Pos, Boolean> posFree = new ConcurrentHashMap<Pos, Boolean>();
	// 叛军数量容器
	private ConcurrentHashMap<Integer, Integer> monsterNumMap = new ConcurrentHashMap<Integer, Integer>();
	// 最后一次存盘时间
	private long lastSaveTime;
	// 世界地图的maxkey
	private AtomicLong maxKey = new AtomicLong();
	// 导人数量
	private ConcurrentHashMap<Integer, Integer> mapPlayerNum = new ConcurrentHashMap<Integer, Integer>();
	// key: country, value : cityIds
	private Map<Integer, HashSet<Integer>> cityIdRecord = new TreeMap<Integer, HashSet<Integer>>();
	/**
	 * 城市首杀  city type
	 */
	private Map<Integer, CityFirstBloodInfo> cityFirstBlood = new HashMap<>();
	//巨型虫族活动
	@Getter
	@Setter
	private Map<Pos, BigMonster> bigMonsterMap = new ConcurrentHashMap<>();
	/**
	 * 待复活的虫子
	 */
	@Getter
	@Setter
	private List<BigMonster> deathMonsterMap = new ArrayList<>();

	@Getter
	@Setter
	private Map<Integer, List<SuperResource>> superResMap = new ConcurrentHashMap<>();
	@Getter
	@Setter
	private Map<Pos, SuperResource> superPosResMap = new ConcurrentHashMap<>();

	public MapInfo() {
	}

	public IWar getWarInfoByWarId(long warId) {
		return warMap.get(warId);
	}

	public void addWar(IWar war) {
//        LogHelper.GAME_LOGGER.info("【战斗.添加】 战斗Key:{} 类型:{} 状态:{} 结束世间:{}", war.getWarId(), war.getWarType(), war.getState(), DateHelper.getDate(war.getEndTime()));
		warMap.put(war.getWarId(), war);
	}

	public IWar getWar(long warId) {
		return warMap.get(warId);
	}

	public void removeWar(IWar war) {
		warMap.remove(war.getWarId());
	}

	public boolean isContain(long warId) {
		return warMap.containsKey(warId);
	}

	public List<IWar> getWarList(Predicate<IWar> predicate) {
		return warMap.values().stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * 拿到某个id  某个等级的怪 离自己城市最近的
	 *
	 * @param level
	 * @return
	 */
	public Entity getEntitys(int level, Pos pos) {
		Entity entity = null;
		Iterator<Map.Entry<Pos, Entity>> iterator = posTake.entrySet().iterator();
		int distance = 0;
		while (iterator.hasNext()) {
			Map.Entry<Pos, Entity> entityEntry = iterator.next();
			if (entityEntry.getValue().getEntityType() == EntityType.Monster && entityEntry.getValue().getLevel() == level) {
				Pos next = entityEntry.getKey();
				int tempDistance = Math.abs(pos.getX() - next.getX()) + Math.abs(pos.getY() - next.getY());
				if (distance == 0 || distance > tempDistance) {
					distance = tempDistance;
					entity = entityEntry.getValue();
				}
			}
		}
		return entity;
	}

	public Map<Integer, CityFirstBloodInfo> getCityFirstBlood() {
		return cityFirstBlood;
	}

	public void setFirstBlood(Integer cityType, Player attacker, List<Player> attackerList, int mapId) {
		if (attacker == null) {
			return;
		}
		CityFirstBloodInfo cityFirstBloodInfo = new CityFirstBloodInfo(cityType, attacker, attackerList, mapId);
		if (cityType == CityType.WORLD_FORTRESS) {
			if (cityFirstBlood.containsKey(cityType)) {
				cityFirstBlood.remove(cityType);
			}
			cityFirstBlood.put(cityType, cityFirstBloodInfo);
		} else if (!cityFirstBlood.containsKey(cityType)) {
			cityFirstBlood.put(cityType, cityFirstBloodInfo);
		}
	}

	public MapInfo(int mapId) {
		this.mapId = mapId;
	}

	public Map<Pos, PlayerCity> getPlayerCityMap() {
		return playerCityMap;
	}

	public Map<Pos, Monster> getMonsterMap() {
		return monsterMap;
	}

	public Map<Pos, Resource> getResourceMap() {
		return resourceMap;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public Map<Pos, CityInfo> getCityInfos() {
		return cityInfos;
	}

	public synchronized void addPos(Pos pos, Entity entity) {
		posFree.remove(pos);
		posTake.put(pos.clone(), entity);
	}

	//巨型虫族
	public synchronized void addMonsterPos(Pos pos, Entity entity) {
		if (posFree.containsKey(pos)) {
			posFree.remove(pos);
			posTake.put(pos.clone(), entity);
			return;
		}
	}

	public void setCityPos(Pos pos, Entity entity) {
		posTake.put(pos, entity);
	}

	public void removeCityPos(Pos pos) {
		posFree.remove(pos);
	}

	// 被击飞、野怪清理、资源采集完
	public synchronized void removePos(Pos pos) {
		posFree.put(pos.clone(), true);
		posTake.remove(pos);
	}

	// 随机获得坐标
	// 随机获得坐标
	public synchronized Pos randPickPos() {
		// 防御性代码
		if (posFree.isEmpty()) {
			return new Pos();
		}

		// 随机算法
		Random random = new Random(System.nanoTime());
		List<Pos> keys = new ArrayList<Pos>(posFree.keySet());
		if (keys.isEmpty()) {
			return new Pos();
		}
		Pos randomPos = keys.get(random.nextInt(keys.size()));
		return randomPos.clone();
	}


	/**
	 * @Description 随机指定范围的格子
	 * @Param [rise, end]
	 * @Return com.game.worldmap.Pos
	 * @Date 2021/6/1 21:28
	 **/
	public synchronized Pos randomAppointPos(int rise, int end) {
		StaticWorldMap staticWorldMap = SpringUtil.getBean(StaticWorldMgr.class).getStaticWorldMap(mapId);
		if (staticWorldMap == null) {
			return new Pos();
		}

		int x1 = staticWorldMap.getX1() + rise;
		int x2 = staticWorldMap.getX1() + end;
		int x3 = staticWorldMap.getX2() - rise;
		int x4 = staticWorldMap.getX2() - end;

		int y1 = staticWorldMap.getY1() + rise;
		int y2 = staticWorldMap.getY1() + end;
		int y3 = staticWorldMap.getY2() - rise;
		int y4 = staticWorldMap.getY2() - end;

		// 防御性代码
		if (posFree.isEmpty()) {
			return new Pos();
		}
		HashSet<Pos> set = new HashSet<>();

		for (Pos pos : posFree.keySet()) {
			int x = pos.getX();
			int y = pos.getY();
			if (x >= x1 && x <= x3 && y >= y1 && y <= y3) {
				if (x <= x2 || x >= x4) {
					set.add(pos);
					continue;
				}
				if (y <= y2 || y > y4) {
					set.add(pos);
					continue;
				}
			}

		}

		// 随机算法
		Random random = new Random(System.nanoTime());
		List<Pos> keys = new ArrayList<Pos>(set);
		if (keys.isEmpty()) {
			return new Pos();
		}
		Pos randomPos = keys.get(random.nextInt(keys.size()));
		return randomPos.clone();
	}


	// 随机获得坐标
	public synchronized Pos randResPickPos() {
		// 防御性代码
		if (posFree.isEmpty()) {
			return new Pos();
		}

		// 随机算法
		Random random = new Random(System.nanoTime());
		List<Pos> keys = new ArrayList<Pos>();
		for (Pos pos : posFree.keySet()) {
			if (isResFreePos(pos)) {
				keys.add(pos);
			}
		}
		if (keys.isEmpty()) {
			return new Pos();
		}
		Pos randomPos = keys.get(random.nextInt(keys.size()));
		return randomPos.clone();
	}

	public int getPosFreeSize() {
		return posFree.size();
	}

	public int getPosTakeSize() {
		return posTake.size();
	}

	// 初始化坐标
	public void initPos(int x1, int x2, int y1, int y2) {
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				posFree.put(new Pos(x, y), true);
			}
		}
	}

	public int getMonsterNum(int monsterLv) {
		Integer num = monsterNumMap.get(monsterLv);
		if (num == null) {
			return 0;
		}

		return num;
	}

	public synchronized void removeMonsterNum(int level) {
		synchronized (monsterNumMap) {
			Integer num = monsterNumMap.get(level);
			if (num != null) {
				monsterNumMap.put(level, num - 1 >= 0 ? num - 1 : 0);
			}
		}
	}

	public synchronized void updateMonsterNum(int monsterLv) {
		synchronized (monsterNumMap) {
			int monsterNum = getMonsterNum(monsterLv);
			monsterNumMap.put(monsterLv, monsterNum + 1);
		}
	}

	public ConcurrentLinkedDeque<March> getMarches() {
		return new ConcurrentLinkedDeque<>(marches.values());
	}

	public void addMarch(March march) {
//		LogHelper.MESSAGE_LOGGER.info("【地图.行军.添加】 MarchType:{} state:{} 结束时间：{}", march.getMarchType(), march.getState(), DateHelper.getDate(march.getEndTime()));
		marches.put(march.getKeyId(), march);
	}

	public void removeMarch(March march) {
		if (march == null) {
			return;
		}
		marches.remove(march.getKeyId());
	}

	public ConcurrentHashMap<Integer, March> getMarchMap() {
		return marches;
	}


	public Entity getEntity(Pos pos) {
		if (posTake.containsKey(pos)) {
			return posTake.get(pos);
		}
		return null;
	}

	public PlayerCity getPlayerCity(Pos pos) {
		if (playerCityMap.containsKey(pos)) {
			return playerCityMap.get(pos);
		}
		return null;
	}

	// 检测一个点上是否有行军
	public March getMarch(Pos pos) {
		for (March march : marches.values()) {
			if (march.getEndPos().isEqual(pos) && march.getState() == MarchState.Collect) {
				return march;
			}
		}

		return null;
	}

	public March getMarch(int keyId) {
		if (marches.containsKey(keyId)) {
			return marches.get(keyId);
		}
		return null;
	}

	public long getLastSaveTime() {
		return lastSaveTime;
	}

	public void setLastSaveTime(long lastSaveTime) {
		this.lastSaveTime = lastSaveTime;
	}

	public WorldMap createWorldMap() {
		if (maxKey == null) {
			return null;
		}
		WorldMap worldMap = new WorldMap();
		worldMap.setMaxKey(maxKey.get());
		worldMap.setMapId(mapId);
		worldMap.setLastSaveTime(lastSaveTime);
		SerMapInfo.Builder builder = SerMapInfo.newBuilder();
		Iterator<Monster> iterator = monsterMap.values().iterator();
		while (iterator.hasNext()) {
			Monster monster = iterator.next();
			builder.addMonster(monster.writeData());
		}

		this.marches.values().forEach(march -> {
			builder.addMarchData(march.writeMarch());
		});
		for (Resource resource : resourceMap.values()) {
			if (resource != null) {
				builder.addResource(resource.writeData());
			}
		}

		for (CityFirstBloodInfo info : cityFirstBlood.values()) {
			if (info != null) {
				builder.addFirstBlood(info.writeData());
			}
		}
		Iterator<BigMonster> bigMonsterIterator = bigMonsterMap.values().iterator();
		while (bigMonsterIterator.hasNext()) {
			BigMonster monster = bigMonsterIterator.next();
			if (monster.getTeam() == null) {
				continue;
			}
			builder.addMonster(monster.writeData());
		}
		Iterator<BigMonster> deathIterator = deathMonsterMap.iterator();
		while (deathIterator.hasNext()) {
			BigMonster e = deathIterator.next();
			builder.addMonster(e.writeData());
		}

		if (getCityIdRecord().isEmpty()) {
			for (int i = 1; i <= 3; i++) {
				getCityIdRecord().put(i, new HashSet<Integer>());
			}
		}

		for (Map.Entry<Integer, HashSet<Integer>> record : getCityIdRecord().entrySet()) {
			Integer country = record.getKey();
			if (country == null) {
				continue;
			}

			DataPb.CtCityIdRecord.Builder crBuilder = DataPb.CtCityIdRecord.newBuilder();
			crBuilder.setCountry(country);
			HashSet<Integer> cityIds = record.getValue();
			if (cityIds != null && !cityIds.isEmpty()) {
				crBuilder.addAllCityId(cityIds);
			}
			builder.addCtCityIdRecord(crBuilder);
		}

		//地图大型矿点入库
		this.getSuperResMap().values().forEach(x -> {
			x.forEach(mine -> {
				builder.addSuperMine(mine.encode());
			});
		});

		SerQuickWar.Builder quickWar = SerQuickWar.newBuilder();
		SerFarWar.Builder farWar = SerFarWar.newBuilder();
		SerCountryWar.Builder countryWar = SerCountryWar.newBuilder();
		SerZergWar.Builder zergWar = SerZergWar.newBuilder();
		SerQuickWar.Builder bigmonsterWar = SerQuickWar.newBuilder();
		for (IWar war : warMap.values()) {
			DataPb.WarData.Builder warPb = war.writeData();
			if (warPb.getWarType() == WarType.ATTACK_QUICK) {
				quickWar.addWarData(warPb.build());
			} else if (warPb.getWarType() == WarType.ATTACK_FAR || warPb.getWarType() == WarType.Attack_WARFARE) {
				farWar.addWarData(warPb.build());
			} else if (warPb.getWarType() == WarType.ATTACK_COUNTRY) {
				countryWar.addWarData(warPb.build());
			} else if (warPb.getWarType() == WarType.ATTACK_ZERG || warPb.getWarType() == WarType.DEFEND_ZERG) {
				zergWar.addWarData(warPb.build());
			} else if (warPb.getWarType() == WarType.BIGMONSTER_WAR) {
				bigmonsterWar.addWarData(warPb.build());
			}
		}

		if (quickWar.getWarDataCount() > 0) {
			worldMap.setQuickWarData(quickWar.build().toByteArray());
		}
		if (farWar.getWarDataCount() > 0) {
			worldMap.setFarWarData(farWar.build().toByteArray());
		}
		if (countryWar.getWarDataCount() > 0) {
			worldMap.setCountryWarData(countryWar.build().toByteArray());
		}
		if (zergWar.getWarDataCount() > 0) {
			worldMap.setZergWarData(zergWar.build().toByteArray());
		}
		if (bigmonsterWar.getWarDataCount() > 0) {
			worldMap.setBigMonsterWarData(bigmonsterWar.build().toByteArray());
		}

		worldMap.setMapData(builder.build().toByteArray());
		return worldMap;
	}

	public long maxKey() {
		return maxKey.addAndGet(1);
	}

	public long getMaxKey() {
		return maxKey.get();
	}

	public void setMaxKey(long maxKey) {
		this.maxKey.set(maxKey);
	}

	public boolean isFreePos(Pos pos) {
		Entity entity = getEntity(pos);
		if (entity == null && posFree.containsKey(pos)) {
			return true;
		}
		return false;
	}


	public boolean isResFreePos(Pos pos) {
		StaticLimitMgr limit = SpringUtil.getBean(StaticLimitMgr.class);
		int x1 = 0;
		int x2 = 0;
		int y1 = 0;
		int y2 = 0;

		List<Integer> addtion12 = limit.getAddtion(293);
		List<Integer> addtion11 = limit.getAddtion(294);
		List<Integer> addtion10 = limit.getAddtion(295);
		List<Integer> addtion9 = limit.getAddtion(296);
		if (addtion9.size() != 4 || addtion10.size() != 4 || addtion11.size() != 4 || addtion12.size() != 4) {
			return false;
		}

		if (mapId == 9) {
			x1 = addtion9.get(0);
			x2 = addtion9.get(1);
			y1 = addtion9.get(2);
			y2 = addtion9.get(3);
		} else if (mapId == 10) {
			x1 = addtion10.get(0);
			x2 = addtion10.get(1);
			y1 = addtion10.get(2);
			y2 = addtion10.get(3);
		} else if (mapId == 11) {
			x1 = addtion11.get(0);
			x2 = addtion11.get(1);
			y1 = addtion11.get(2);
			y2 = addtion11.get(3);
		} else if (mapId == 12) {
			x1 = addtion12.get(0);
			x2 = addtion12.get(1);
			y1 = addtion12.get(2);
			y2 = addtion12.get(3);
		}

		if (pos.getX() >= x1 && pos.getX() <= x2 && pos.getY() >= y1 && pos.getY() <= y2) {
			return true;
		} else {
			return false;
		}
	}


	public Entity getEntity(int x, int y) {
		return posTake.get(new Pos(x, y));
	}

	public Resource getResource(Pos pos) {
		return resourceMap.get(pos);
	}

	public void clearMonsterPos(Pos pos) {
		Entity common = posTake.get(pos);
		if (common != null && common instanceof Monster) {
			removePos(pos);    // 一般
			if (monsterMap != null) {
				monsterMap.remove(pos);
			}
			removeMonsterNum(common.getLevel());
		} else {
			LogHelper.CONFIG_LOGGER.info("clear pos:{} error", pos);
		}
	}


	public void clearRebelMonsterPos(Pos pos) {
		Entity common = posTake.get(pos);
		if (common != null && common instanceof RebelMonster) {
			removePos(pos);    // 一般
			if (rebelMap != null) {
				rebelMap.remove(pos);
			}
			removeMonsterNum(common.getLevel());
		} else if (common != null && common instanceof BigMonster) {
			removePos(pos);    // 一般
			bigMonsterMap.remove(pos);
		}
	}

	public void clearResourcePos(Pos pos) {
		Entity common = posTake.get(pos);
		if (common != null) {
			removePos(pos);    // 一般
			if (common instanceof Resource) {
				resourceMap.remove(pos);
			}
			if (common instanceof SuperResource) {
				superPosResMap.remove(pos);
			}
		}
	}

	public ConcurrentHashMap<Integer, Integer> getMapPlayerNum() {
		return mapPlayerNum;
	}

	public void setMapPlayerNum(ConcurrentHashMap<Integer, Integer> mapPlayerNum) {
		this.mapPlayerNum = mapPlayerNum;
	}

	public CountryCityWarInfo getCountryCityWar(int cityId, int country) {
		Optional<IWar> optional = warMap.values().stream().filter(e -> {
			if (e instanceof CountryCityWarInfo) {
				if (cityId == ((CountryCityWarInfo) e).getCityId() && e.getAttacker().getCountry() == country) {
					return true;
				}
			}
			return false;
		}).findAny();
		if (optional.isPresent()) {
			return (CountryCityWarInfo) optional.get();
		}
		return null;
	}

	public List<IWar> getCountryCityWar(int cityId) {
		return warMap.values().stream().filter(e -> {
			if (e instanceof CountryCityWarInfo) {
				if (cityId == ((CountryCityWarInfo) e).getCityId()) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
	}

	public void updatePlayerNum(int country) {
		Integer num = mapPlayerNum.get(country);
		if (num == null) {
			num = 0;
		}

		mapPlayerNum.put(country, num + 1);
	}

	public void removeMonsterPos(Pos pos) {
		Entity common = posTake.get(pos);
		if (common != null && (common instanceof Monster || common instanceof BigMonster)) {
			removePos(pos);    // 一般
		}
	}

	public void removeResPosOnly(Pos pos) {
		Entity common = posTake.get(pos);
		if (common != null && common instanceof Resource) {
			removePos(pos);    // 一般
		}
	}

	public Map<Integer, HashSet<Integer>> getCityIdRecord() {
		return cityIdRecord;
	}

	public void setCityIdRecord(Map<Integer, HashSet<Integer>> cityIdRecord) {
		this.cityIdRecord = cityIdRecord;
	}

	public Map<Pos, RebelMonster> getRebelMap() {
		return rebelMap;
	}


	public ConcurrentHashMap<Long, WarInfo> getRebelWarMap() {
		return rebelWarMap;
	}

	public List<WarInfo> getRebelWarMapByCountry(int country) {
		return Lists.newArrayList(rebelWarMap.values());
	}

	public void addBroodEntity(Pos pos, Entity entity) {
		if (posTake.containsKey(pos)) {
			posTake.put(pos, entity);
		}
		if (cityInfos.containsKey(pos) && entity instanceof CityInfo) {
			cityInfos.put(pos, (CityInfo) entity);
		}
	}

	/**
	 * @Description 随机城市范围内的坐标
	 * @Param [cityID]
	 * @Return com.game.worldmap.Pos
	 * @Date 2021/7/27 11:04
	 **/
	public synchronized Pos randomCityRangePos(StaticWorldCity city) {
		if (this.mapId != MapId.CENTER_MAP_ID) {
			return null;
		}
		if (city == null || city.getMapId() != this.mapId) {
			return null;
		}
		int x1 = city.getRangex1();
		int x2 = city.getRangex2();
		int y1 = city.getRangey1();
		int y2 = city.getRangey2();

		// 防御性代码
		if (posFree.isEmpty()) {
			return null;
		}
		List<Pos> freeSet = new ArrayList<>();
		for (Pos pos : posFree.keySet()) {
			int x = pos.getX();
			int y = pos.getY();
			if (x >= x1 && x <= x2 && y >= y1 && y <= y2) {
				freeSet.add(pos);
			}
		}
		// 随机算法
		if (freeSet.isEmpty()) {
			return null;
		}
		Collections.shuffle(freeSet);
		return freeSet.remove(0);
	}

	public Entity searchPos(Pos pos, int type, int level) {
		Entity entity = null;
		Iterator<Monster> iterator = monsterMap.values().iterator();
		while (iterator.hasNext()) {
			Monster next = iterator.next();
//			if()
			next.setDistance(pos);
			if (entity == null) {
				entity = next;
			} else {
				if (next.getDistance() < entity.getDistance()) {
					entity = next;
				}
			}
		}
		return entity;
	}

	public March getMarchByPos(Pos pos) {
		for (March march : marches.values()) {
			if (march.getEndPos().isEqual(pos)) {
				return march;
			}
		}

		return null;
	}
}
