package com.game.manager;

import com.alibaba.fastjson.JSONObject;
import com.game.Loading;
import com.game.constant.*;
import com.game.dao.p.WorldDao;
import com.game.dao.p.WorldMapDao;
import com.game.dataMgr.*;
import com.game.define.LoadData;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WarAssemble;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.flame.BuffType;
import com.game.flame.FlameMap;
import com.game.flame.FlameWarManager;
import com.game.flame.FlameWarService;
import com.game.log.LogUser;
import com.game.log.constant.*;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.pb.*;
import com.game.pb.SerializePb.SerMapInfo;
import com.game.pb.SerializePb.SerPvpBattleData;
import com.game.pb.SerializePb.SerWorldBossData;
import com.game.pb.SerializePb.SerWorldTargetData;
import com.game.pb.WorldPb.SynPlayerCityCallRq;
import com.game.pb.WorldPb.SynWorldTargetRq;
import com.game.server.GameServer;
import com.game.server.LogicServer;
import com.game.service.CastleService;
import com.game.service.WorldActPlanService;
import com.game.service.WorldTargetTaskService;
import com.game.util.*;
import com.game.worldmap.Resource;
import com.game.worldmap.*;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.game.worldmap.fight.war.ZergWarInfo;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


@Component
@LoadData(name = "世界管理", type = Loading.LOAD_USER_DB, initSeq = 1600)
public class WorldManager extends BaseManager {

	@Autowired
	private WorldDao worldDao;

	@Autowired
	private WorldMapDao worldMapDao;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	@Autowired
	private CountryManager countryManager;

	@Autowired
	private CityManager cityManager;

	@Autowired
	private TechManager techManager;

	@Autowired
	private StaticHeroMgr staticHeroMgr;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private StaticTaskMgr staticTaskMgr;

	@Autowired
	private StaticCountryMgr staticCountryMgr;

	@Autowired
	private WorldPvpMgr worldPvpMgr;

	@Autowired
	private RiotManager riotManager;

	@Autowired
	private ServerManager serverManager;

	@Autowired
	private SoldierManager soldierManager;

	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private CastleService castleService;
	@Autowired
	private StaticWorldNewTargetMgr staticWorldNewTargetMgr;
	@Autowired
	private WorldTargetTaskService worldTargetTaskService;
	@Autowired
	private StealCityManager stealCityManager;

	@Autowired
	private WarBookManager warBookManager;

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private BigMonsterManager bigMonsterManager;
	@Autowired
	private MarchManager marchManager;
	@Autowired
	private BroodWarManager broodWarManager;
	@Autowired
	private ZergManager zergManager;
	@Autowired
	private BattleManager battleManager;
	@Autowired
	private FlameWarManager flameWarManager;
	@Autowired
	private FlameWarService flameWarService;
	@Autowired
	private WorldActPlanService worldActPlanService;
	@Autowired
	private StaticMailDataMgr staticMailDataMgr;
	@Autowired
	NickManager nickManager;


	// 世界地图信息
	private ConcurrentMap<Integer, MapInfo> worldMapInfo = Maps.newConcurrentMap();

	// 世界目标信息
	private ConcurrentHashMap<Integer, WorldData> worldData = new ConcurrentHashMap<Integer, WorldData>();
	// @Getter
//    @Setter
//    private Map<Integer, Map<Integer, WarAssemble>> war = new HashMap<>();//key1 mapId  key2 阵营id
	private ConcurrentLinkedQueue<WarInfo> allWar = new ConcurrentLinkedQueue<>();// 全图所有的战争

	public boolean initOk = false;
	private HashSet<Integer> mapIds = new HashSet<Integer>();
	private boolean isActMonsterClear = false;
	// private Lock lock = new ReentrantLock();

	// p_world_map对象
	private Map<Integer, WorldMap> worldMaps = new HashMap<>();

	@Getter
	private List<StaticWorldMap> bornMaps = new ArrayList<>();

	public boolean isNotOk() {
		return !initOk;
	}

	// 已被占用的pos坐标
	private HashBasedTable<Integer, Integer, Pos> illegalPos = HashBasedTable.create();

	private World world = null;
	private boolean isNewCreate = false;

	private Logger logger = LoggerFactory.getLogger(getClass());

	// 世界的ID
	private static final int WORLD_ID = 1;


	@Override
	public void load() throws Exception {
		// 加载过滤坐标
		loadUsedPos();//world.xml文件

		// bron出生地图信息
		initBornMap();

		// WorldData封装
		worldData.put(WORLD_ID, new WorldData());

		// 世界如果是空的,则插入新的
		world = worldDao.selectWorld(WORLD_ID);
		if (world == null) {
			world = new World();
			world.setKeyId(WORLD_ID);
			worldDao.insertWorld(world);
			isNewCreate = true;
		}
	}

	@Override
	public void init() throws Exception {
		if (!isNewCreate) {
			dserWorld(world);
		}

		// 通过StaticWorldMap初始化mapInfo对象
		initMapInfo();

		// 通过StaticWorld加载MapInfo的城池信息
		initCityInfoOfMapInfo();

		// 加载玩家坐标
		loadWorldPlayer();

		checkInitMonster();

		openNewSeason();

		// 检查城池
		checkCountryCity();
		initOk = true;
		logger.info("load wolrd data finished!!!");

		loadStealCity();

		iniMergeServerPlayer();
		serverManager.updateBootStrap("world");
	}

//	public void loadWorldInfo() throws Exception {
//		LogHelper.GAME_LOGGER.error("loadWorldInfo");
//
////        iniUsedPos();
//		//初始化
//		initWorld();
//		// 加载初级区域地图Id
//		loadMapIds();
//		// 加载世界目标、世界BOSS、季节信息
//		initWorldMap();
//		// 加载世界地图坐标
//		initMapInfo();
//		// 加载城池坐标
//		initCityInfoOfMapInfo();
//		// 加载玩家坐标
//		loadWorldPlayer();
//		// 初始化世界数据
//		initWorldMap();
//		// 加载城池
//		cityManager.init();
//		// 加载世界数据: 怪、资源、战斗
//		loadWorldMap();
//		//
//		checkInitMonster();
//		// flushInitActMonster();
//		// 加载世界地图行军
//		// loadMarches();
//		openNewSeason();
//		// 加载皇城血战
//		worldPvpMgr.initBattle();
//		// 检查城池
//		checkCountryCity();
//		initOk = true;
//		logger.info("load wolrd data finished!!!");
//
//		loadStealCity();
//
//		serverManager.updateBootStrap("world");
//	}

	/**
	 * 处理集结按钮数据
	 *
	 * @param info
	 * @param flag
	 * @param country
	 */
	public void flushWar(WarInfo info, boolean flag, int country) {
		try {
			if (info.getMapId() == 0) {
				int mapId = worldManager.getMapId(info.getDefencerPos());
				info.setMapId(mapId);
				int mapAreaType = worldManager.getMapAreaType(mapId);
				info.setMapType(mapAreaType);
			}
			if (flag) {
				if (allWar.contains(info)) {// 已添加过一次则不在添加
					return;
				}
				allWar.add(info);
			} else {
				Iterator<WarInfo> it = allWar.iterator();
				while (it.hasNext()) {
					WarInfo next = it.next();
					if (next.getWarId() == info.getWarId()) {
						it.remove();
					}
				}
			}
			flushWarInfos();
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("flushWar", e);
		} finally {
		}
	}

	public void flushWarInfos() {
		LogicServer mainLogicServer = GameServer.getInstance().mainLogicServer;
		if (mainLogicServer != null) {
			mainLogicServer.addCommand(() -> {
				if (allWar.size() >= 0) {
					for (Player player : playerManager.getOnlinePlayer()) {
						sendWar(player);
					}
				}
			});
		}
	}

	/**
	 * 通知在线玩家行军线变更
	 *
	 * @param mapId
	 * @param march
	 */
	public void synMarch(int mapId, March march) {
		WorldPb.SynMarchRq synMarchRq = createSynMarchRq(march);

		// 异步通知所有在线玩家 行军线变更
		playerManager.getOnlinePlayer().forEach(target -> {
			playerManager.synMarchToPlayer(target, synMarchRq);
		});

		// 行军线9的需要接着推送2 此方法需要注意，容易引起死循环
		if (march.getState() == MarchState.FightOver) {
			worldManager.synRewards(march);
			checkCompanion(march);
		}
	}

	public void synMarch(March march, int country) {
		WorldPb.SynMarchRq synMarchRq = createSynMarchRq(march);
		// 异步通知所有在线玩家 行军线变更
		playerManager.getOnlinePlayer().stream().filter(e -> e.getCountry() == country).forEach(target -> {
			playerManager.synMarchToPlayer(target, synMarchRq);
		});
	}


	/**
	 * 世界地图上绘制已占用的坐标点
	 */
	public void loadUsedPos() {
		NodeList nodeList = XmlUtil.getXmlNodeList("world.xml", "Pos");
		if (nodeList == null) {// 不需要进行剔除
			return;
		}
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Element elem = (Element) node;
			int x = Integer.valueOf(elem.getAttribute("x"));
			int y = Integer.valueOf(elem.getAttribute("y"));

			illegalPos.put(x, y, new Pos(x, y));
//			LogHelper.GAME_LOGGER.info("user Pos x:{} y:{}", x, y);
		}
	}

	/**
	 * 出生地图信息
	 */
	public void initBornMap() {
		Map<Integer, StaticWorldMap> worldMapMap = staticWorldMgr.getWorldMap();
		for (StaticWorldMap worldMap : worldMapMap.values()) {
			if (worldMap.getAreaType() == 1) {
				mapIds.add(worldMap.getMapId());
			}
			if (worldMap.getBorn() == 1) {
				bornMaps.add(worldMap);
			}
		}
		bornMaps.sort(Comparator.comparing(e -> e.getMapId()));
		bornMaps.forEach(e -> {
			LogHelper.GAME_LOGGER.info("出生地 {}", e.getMapId());
		});

	}

	/**
	 * 初始化地图信息
	 */
	public void initMapInfo() throws Exception {
		getWorldMapInfo().clear();
		Map<Integer, StaticWorldMap> staticWorldMapMap = staticWorldMgr.getWorldMap();
		for (Map.Entry<Integer, StaticWorldMap> entry : staticWorldMapMap.entrySet()) {
			if (entry == null) {
				continue;
			}
			StaticWorldMap staticWorldMap = entry.getValue();
			if (staticWorldMap == null) {
				continue;
			}

			int mapId = entry.getKey();

			// 初始化相关表
			WorldMap worldMap = worldMapDao.selectWolrdMap(mapId);
			if (worldMap == null) {
				worldMap = new WorldMap();
				worldMap.setMapId(mapId);
				worldMapDao.insertWorldMap(worldMap);
			}

			MapInfo mapInfo;
			if (mapId != MapId.FIRE_MAP) {
				mapInfo = new MapInfo(mapId);
				mapInfo.initPos(staticWorldMap, illegalPos);
			} else {
				mapInfo = new FlameMap(mapId);
			}

			// 同步mapInfo的更新信息
			mapInfo.setMaxKey(worldMap.getMaxKey());
			mapInfo.setLastSaveTime(worldMap.getLastSaveTime());

			// worldMap对象
			worldMaps.put(worldMap.getMapId(), worldMap);

			// 序列化数据读取

			readWorldData(worldMap, mapInfo);

			getWorldMapInfo().put(mapId, mapInfo);
		}

	}

	// 加载世界地图玩家
	public void loadWorldPlayer() {
		Iterator<Player> it = playerManager.getPlayers().values().iterator();
		while (it.hasNext()) {
			Player player = it.next();
			if (player == null) {
				continue;
			}
			addPlayerInfo(player);
		}
	}

	public int getMapId(int posX, int posY) {
		return staticWorldMgr.getMapId(posX, posY);
	}

	public int getMapId(CommonPb.Pos pos) {
		return staticWorldMgr.getMapId(pos.getX(), pos.getY());
	}

	public void addPlayerInfo(Player player) {
		int posX = player.getPosX();
		int posY = player.getPosY();
		int mapId = getMapId(posX, posY);
		if (mapId == 0) {
			return;
		}

		Pos pos = new Pos(posX, posY);
		if (pos.isError()) {
			return;
		}
		MapInfo mapInfo = getWorldMapInfo().computeIfAbsent(mapId, e -> new MapInfo(mapId));
		if (!mapInfo.isFreePos(pos)) {
			pos = mapInfo.randPickPos();
		}
		//mapInfo.removeCityPos(pos);
		addPlayerCity(pos, mapInfo, player);
		mapInfo.updatePlayerNum(player.getCountry(), 1);
	}

	// 创建玩家城池
	public PlayerCity createPlayerCity(Player player) {
		PlayerCity playerCity = new PlayerCity();
		Pos pos = new Pos(player.getPosX(), player.getPosY());
		playerCity.setEntityType(EntityType.PleyerCity);
		playerCity.setId(player.roleId);
		playerCity.setLordId(player.roleId);
		playerCity.setLevel(player.getCommandLv());
		playerCity.setPos(pos);
		playerCity.setCommandLv(player.getCommandLv());
		playerCity.setCountry(player.getCountry());
		playerCity.setName(player.getNick());
		playerCity.setCallCount(player.getLord().getCallCount());
		playerCity.setCallEndTime(player.getLord().getCallEndTime());
		playerCity.setCallReply(player.getLord().getCallReply());

		Wall wall = player.getWall();
		playerCity.setAssitNum(0);
		if (wall != null) {
			playerCity.setAssitNum(wall.getWallFriendSize());
		}
		playerCity.setMaxMonsterLv(player.getMaxMonsterLv());
		playerCity.setPlayer(player);
		return playerCity;
	}

	// 增加玩家城池
	public PlayerCity addPlayerCity(Pos pos, MapInfo mapInfo, Player player) {
		Map<Pos, PlayerCity> playerCityMap = mapInfo.getPlayerCityMap();
		if (playerCityMap.containsKey(pos)) {
			return null;
		}
		PlayerCity playerCity = createPlayerCity(player);
		boolean flag = mapInfo.addPos(pos, playerCity);
		if (!flag) {// 添加失败,该坐标已被占用
			return null;
		}

		playerCity.setPos(pos);
		playerCityMap.put(pos, playerCity);
		playerManager.clearPos(pos);
		player.getPushPos().put(pos.toPosStr(), true);
		return playerCity;

	}

	// 删除玩家城池
	public void removePlayerCity(Player player) {
		MapInfo mapInfo = getMapInfo(player.getLord().getMapId());
		if (mapInfo == null) {
			return;
		}
		Pos pos = player.getPos();
		Map<Pos, PlayerCity> playerCityMap = mapInfo.getPlayerCityMap();
		if (!playerCityMap.containsKey(pos)) {
			return;
		}
		playerCityMap.remove(pos);
		mapInfo.removePos(pos);
		mapInfo.updatePlayerNum(player.getCountry(), -1);
	}

	public MapInfo getMapInfo(int mapId) {
		if (worldMapInfo.containsKey(mapId)) {
			return worldMapInfo.get(mapId);
		}
		return null;
	}

	// 玩家开启世界的时候随机坐标
	public Pos pickPos(int country) {
		int mapId = getInitMapId(country);
		if (mapId == 0) {
			LogHelper.CONFIG_LOGGER.error("get primer mapId wrong!");
			return new Pos();
		}

		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("get mapInfo wrong!");
			return new Pos();
		}

		Pos pos = givePlayerPos(mapInfo);
		return pos;
	}

	// 初始资源随机坐标
	// x x-2 ~ x+ 2 y y-2 ~ y+2
	// 需要判断坐标的合法性
	// 玩家周围3*3格子范围内刷新时不生成采集点
	//public Pos getPrimaryPos(Player player) {
	//    int configNum = staticLimitMgr.getNum(SimpleId.COLLECT_CELL_NUM);
	//    Pos pos = randPos(player, configNum);
	//
	//    return pos;
	//}

	// 在玩家周围随机一个坐标
	//public Pos randPos(Player player, int configNum) {
	//    Pos pos = new Pos();
	//    List<Pos> aroundPos = new ArrayList<Pos>();
	//    int x = player.getPosX();
	//    int y = player.getPosY();
	//    MapInfo mapInfo = getWorldMapInfo().get(player.getLord().getMapId());
	//    if (mapInfo == null) {
	//        LogHelper.CONFIG_LOGGER.error("getPrimaryPos pos may error!");
	//        return pos;
	//    }
	//    // 找出合法的坐标
	//    for (int i = x - configNum; i <= x + configNum; i++) {
	//        if (i < 0) {
	//            continue;
	//        }
	//        for (int j = y - configNum; j <= y + configNum; j++) {
	//            if (j < 0) {
	//                continue;
	//            }
	//            if (i == x && j == y) {
	//                continue;
	//            }
	//            Pos elem = new Pos(i, j);
	//            if (mapInfo.isFreePos(elem)) {
	//                aroundPos.add(elem);
	//            }
	//        }
	//    }
	//    // 随机一个坐标
	//    int posSize = aroundPos.size();
	//    if (posSize <= 0) {
	//        return pos;
	//    }
	//	int index = new Random().nextInt(posSize);
	//	return aroundPos.get(index);
	//}

	public int getMapId(Player player) {
		//int x = player.getPosX();
		//int y = player.getPosY();
		//return getMapId(x, y);
		return player.getLord().getMapId();
	}

	public int getMapId(Pos pos) {
		int x = pos.getX();
		int y = pos.getY();
		return getMapId(x, y);
	}

	public StaticWorldMap getMap(Pos pos) {
		int x = pos.getX();
		int y = pos.getY();
		return staticWorldMgr.getMap(x, y);
	}

	public int getMapId(DataPb.PosData pos) {
		int x = pos.getX();
		int y = pos.getY();
		int mapId = getMapId(x, y);

		return mapId;
	}

	public int getMapAreaType(int mapId) {
		StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
		if (staticWorldMap == null) {
			return 0;
		}

		return staticWorldMap.getAreaType();
	}

	// 检查玩家当前的怪物的个数, 5*5 格子
	// 统一分配格子
	public int getMonsterNum(Player player) {
		MapInfo mapInfo = getWorldMapInfo().get(player.getLord().getMapId());
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
			return 0;
		}

		Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
		int configNum = staticLimitMgr.getNum(SimpleId.WORLD_MONSTER_CELL_NUM);
		int x = player.getPosX();
		int y = player.getPosY();

		Pos monsterPos = new Pos();
		int monsterNum = 0;
		for (int i = x - configNum; i <= x + configNum; i++) {
			for (int j = y - configNum; j <= y + configNum; j++) {
				if (i == x && j == y) {
					continue;
				}

				monsterPos.setPos(i, j);
				if (monsterMap.containsKey(monsterPos)) {
					++monsterNum;
				}
			}
		}

		return monsterNum;
	}

	public int getMonsterId(int monsterLevel) {
		Map<Integer, Integer> monsterIdMap = staticWorldMgr.getMonsterIdMap();
		Integer monsterId = monsterIdMap.get(monsterLevel);
		if (monsterId == null) {
			LogHelper.CONFIG_LOGGER.error("monster Level " + monsterLevel + " not found!");
			return 0;
		}

		return monsterId;
	}

	// 创建一个野怪
	public Monster createMonster(int monsterLevel, Pos pos, int reason) {
		Monster monster = new Monster();
		monster.setEntityType(EntityType.Monster);
		int id = getMonsterId(monsterLevel);
		monster.setId(id);
		monster.setLevel(monsterLevel);
		monster.setPos(new Pos(pos.getX(), pos.getY()));

		return monster;
	}

	public Monster createCountryHero(int worldMonsterId, int monsterLv, Pos pos) {
		Monster monster = new Monster();
		monster.setEntityType(EntityType.Monster);
		monster.setId(worldMonsterId);
		monster.setLevel(monsterLv);
		monster.setPos(new Pos(pos.getX(), pos.getY()));
		return monster;
	}

	public CityInfo createCityInfo(int mapId, StaticWorldCity staticWorldCity) {
		CityInfo cityInfo = new CityInfo();
		cityInfo.setMapId(mapId);
		cityInfo.setEntityType(EntityType.NpcCity);
		cityInfo.setId(staticWorldCity.getCityId());
		cityInfo.setCityType(staticWorldCity.getType());
		cityInfo.setLevel(staticWorldCity.getLevel());
		cityInfo.setEndTime(0);
		Pos pos = new Pos(staticWorldCity.getX(), staticWorldCity.getY());
		cityInfo.setPos(pos);
		return cityInfo;
	}

	public BroodWar createHatcheryInfo(int mapId, StaticWorldCity staticWorldCity) {
		BroodWar cityInfo = new BroodWar();
		cityInfo.setMapId(mapId);
		cityInfo.setEntityType(EntityType.NpcCity);
		cityInfo.setId(staticWorldCity.getCityId());
		cityInfo.setCityType(staticWorldCity.getType());
		cityInfo.setLevel(staticWorldCity.getLevel());
		cityInfo.setState(BroodWarState.WAIT);
		cityInfo.setEndTime(0);
		Pos pos = new Pos(staticWorldCity.getX(), staticWorldCity.getY());
		cityInfo.setPos(pos);
		return cityInfo;
	}

	public BroodWar createTurretInfo(int mapId, StaticWorldCity staticWorldCity) {
		Turret cityInfo = new Turret();
		cityInfo.setMapId(mapId);
		cityInfo.setEntityType(EntityType.NpcCity);
		cityInfo.setId(staticWorldCity.getCityId());
		cityInfo.setCityType(staticWorldCity.getType());
		cityInfo.setLevel(staticWorldCity.getLevel());
		cityInfo.setEndTime(0);
		Pos pos = new Pos(staticWorldCity.getX(), staticWorldCity.getY());
		cityInfo.setPos(pos);
		return cityInfo;
	}

	/**
	 * 初始化地图信息MapInfo中的城池
	 */
	public void initCityInfoOfMapInfo() {
		Map<Integer, StaticWorldCity> citys = staticWorldMgr.getCityMap();
		for (StaticWorldCity city : citys.values()) {
			MapInfo mapInfo = getWorldMapInfo().get(city.getMapId());
			if (mapInfo == null) {
				continue;
			}
			Map<Pos, CityInfo> cityInfos = mapInfo.getCityInfos();
			// 显示坐标
			Pos pos = new Pos(city.getX(), city.getY());
			CityInfo cityInfo = null;
			switch (city.getType()) {
				default: {
					cityInfo = createCityInfo(city.getMapId(), city);
				}
				break;
				case CityType.WORLD_FORTRESS: {
					cityInfo = createHatcheryInfo(city.getMapId(), city);
				}
				break;
				case CityType.BROOD_WAR_TURRET: {
					cityInfo = createTurretInfo(city.getMapId(), city);
				}
				break;
			}
			int x1 = city.getX1();
			int x2 = city.getX2();
			int y1 = city.getY1();
			int y2 = city.getY2();
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					mapInfo.removeCityPos(new Pos(x, y));
				}
			}
			mapInfo.setCityPos(pos, cityInfo);
			cityInfos.put(pos, cityInfo);
		}
	}

	public ConcurrentMap<Integer, MapInfo> getWorldMapInfo() {
		return worldMapInfo;
	}

	// 创建一个资源点
	public Resource createResource(int entityType, int resType, int resLevel) {
		// resType, resLevel 获取当前资源的配置
		Integer resId = staticWorldMgr.getWorldRes(resType, resLevel);
		if (resId == null) {
			LogHelper.CONFIG_LOGGER.error("resId is null, check resType = " + resType + ", resLevel = " + resLevel);
			return null;
		}

		StaticWorldResource worldResource = staticWorldMgr.getStaticWorldResource(resId);
		if (worldResource == null) {
			LogHelper.CONFIG_LOGGER.error("worldResource is null, resId  = " + resId);
			return null;
		}

		Resource resource = new Resource();
		resource.setEntityType(entityType);
		resource.setId(worldResource.getId());
		resource.setLevel(resLevel);
		resource.setCount(worldResource.getResource()); // 读取配置
		resource.setType(worldResource.getType());
		return resource;

	}

	public int distance(Pos startPos, Pos endPos) {
		return Math.abs(endPos.getX() - startPos.getX()) + Math.abs(endPos.getY() - startPos.getY());
	}

	public int distance(int x1, int y1, int x2, int y2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1);
	}

	/**
	 * 创建行军
	 *
	 * @param player
	 * @param heroIds
	 * @param targetPos
	 * @return
	 */
	public March createMarch(Player player, List<Integer> heroIds, Pos targetPos) {
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		List<Integer> marchHero = march.getHeroIds();
		for (Integer heroId : heroIds) {
			marchHero.add(heroId);
		}
		march.setState(MarchState.Begin);
		march.setEndPos(targetPos);
		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());
		march.setStartPos(playerPos);
		// 兵书对行军的影响
		float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);
		long period = getPeriod(player, playerPos, targetPos, bookEffectMarch);
		int mapId = getMapId(targetPos);
		if (mapId == MapId.FIRE_MAP) {
			period = flameWarManager.getPeriod(player, player.getPos(), targetPos, bookEffectMarch);
		}
		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
		march.setCountry(player.getCountry());
		return march;
	}

	public March createMarch(Player player, List<Integer> heroIds, Pos targetPos, int marchType) {
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		List<Integer> marchHero = march.getHeroIds();
		for (Integer heroId : heroIds) {
			marchHero.add(heroId);
		}
		march.setState(MarchState.Begin);
		march.setEndPos(targetPos);
		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());
		march.setStartPos(playerPos);
		// 兵书对行军的影响
		float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);
		long period = getPeriod(player, playerPos, targetPos, bookEffectMarch, marchType);
		int mapId = getMapId(targetPos);
		if (mapId == MapId.FIRE_MAP) {
			period = flameWarManager.getPeriod(player, player.getPos(), targetPos, bookEffectMarch);
		}
		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
		march.setCountry(player.getCountry());
		march.setMarchType(marchType);
		return march;
	}

	/***
	 * 虫族主宰行军
	 *
	 * @param player
	 * @param targetPos
	 * @return
	 */
	public March createZergMarch(Player player, WarInfo warInfo, Pos targetPos) {
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.getRoleId());
		march.setAttackerId(warInfo.getAttackerId());
		march.setState(MarchState.Waiting);
		march.setEndPos(targetPos);
		march.setStartPos(targetPos);
		march.setPeriod(1000);
		march.setEndTime(System.currentTimeMillis() + 1000);
		march.setCountry(player.getCountry());
		march.setMarchType(MarchType.ZERG_DEFEND_WAR);
		march.setSide(2);
		march.setWarId(warInfo.getWarId());
		march.setFightTime(warInfo.getEndTime(), MarchReason.DEFEND_ZERG_WAR);
		return march;
	}

	public March createRebelMarch(Player player, List<Integer> heroIds, Pos targetPos) {
		if (heroIds.isEmpty()) {
			LogHelper.CONFIG_LOGGER.error("march hero is empty, logic may error!");
		}
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		List<Integer> marchHero = march.getHeroIds();
		for (Integer heroId : heroIds) {
			marchHero.add(heroId);
		}
		march.setState(MarchState.Begin);
		march.setEndPos(targetPos);
		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());
		march.setStartPos(playerPos);
		int period = (int) Math.ceil(distance(march.getStartPos(), march.getEndPos()) * 1.0f * 0.5) * 1000;
		march.setEndTime(System.currentTimeMillis() + period);
		march.setCountry(player.getCountry());
		march.setPeriod(period);
		march.setMarchType(MarchType.ReBelWar);
		return march;
	}


	/**
	 * 虫族入侵行军
	 *
	 * @param player
	 * @param heroIds
	 * @param warInfo
	 * @return
	 */
	public March createRiotMarch(Player player, List<Integer> heroIds, WarInfo warInfo) {
		March march = new March();

		List<Integer> marchHero = march.getHeroIds();
		for (Integer heroId : heroIds) {
			marchHero.add(heroId);
		}

		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());

		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		march.setCountry(player.getCountry());
		march.setWarId(warInfo.getWarId());
		march.setEndPos(playerPos);
		march.setStartPos(playerPos);
		march.setPeriod(1);
		march.setEndTime(warInfo.getEndTime());
		march.setMarchType(MarchType.RiotWar);
		march.setState(MarchState.Waiting);
		march.setDefencerId(player.roleId);
		march.setAttackerId(warInfo.getAttackerId());
		march.setSide(2);
		march.setFightTime(warInfo.getEndTime(), MarchReason.AttendPvpWar);
		return march;
	}

	/**
	 * 计算行军速度 [d/(0.125*(1+科技加成百分比))*(1-督军效果数值百分比)*(1-行军加速道具效果数值百分比)]向上取整
	 *
	 * @param player
	 * @param pos1
	 * @param pos2
	 * @param bookEffectMarch
	 * @return
	 */
	public long getPeriod(Player player, Pos pos1, Pos pos2, float bookEffectMarch) {
		int distance = distance(pos1, pos2);
		float configNum = staticLimitMgr.getNum(10) * 1.0f / 1000;
		double spAdd = techManager.getArmySp(player);
		float vipFactor = playerManager.getMarchBuff(player);
		float propAdd = playerManager.getBuffAdd(player, BuffId.MARCH_SPEED);

		// 巨型虫族buff
		int mapId = getMapId(player.getPos());
		float bigMonsterSpeed = bigMonsterManager.getMonsterBuff(mapId, player) / 100F;
		// 母巢行军BUFF
		BroodWarInfo broodWarInfo = player.getBroodWarInfo();
		float broodSpeed = 0;
		if (broodWarInfo != null) {
			int buffId = broodWarInfo.getBroodWarBuff().getOrDefault(PropertyType.SPEED, 0);
			StaticBroodWarBuff buff = broodWarManager.getBuff(buffId);
			if (buff != null) {
				if (buff.getBuff().size() > 0) {
					for (List<Integer> l : buff.getBuff()) {
						if (l.size() >= 2) {
							if (l.get(0) == PropertyType.SPEED) {
								broodSpeed = l.get(1) / 100F;
							}
						}
					}
				}
			}
		}
		double commandBuff = broodWarManager.getCommandBuff(player, TechEffectId.ADD_MARCH_SPEED);
		double cityBuf = getCityBuf(player, CityBuffType.MARCH);
		long period = (long) Math.ceil(distance / (configNum * 1.0f * (1 + spAdd)) * (1 - vipFactor) * (1 - propAdd) * (1 - bookEffectMarch) / (1 + bigMonsterSpeed) * (1 - broodSpeed) * (1 - commandBuff) * (1 - cityBuf));
		return period * TimeHelper.SECOND_MS;
	}

	/**
	 * 计算行军速度 [d/(0.125*(1+科技加成百分比))*(1-督军效果数值百分比)*(1-行军加速道具效果数值百分比)]向上取整
	 *
	 * @param player
	 * @param pos1
	 * @param pos2
	 * @param bookEffectMarch
	 * @return
	 */
	public long getPeriod(Player player, Pos pos1, Pos pos2, float bookEffectMarch, int marchType) {
		int distance = distance(pos1, pos2);
		float configNum = staticLimitMgr.getNum(10) * 1.0f / 1000;
		double spAdd = techManager.getArmySp(player);
		float vipFactor = playerManager.getMarchBuff(player);
		float propAdd = playerManager.getBuffAdd(player, BuffId.MARCH_SPEED);

		// 巨型虫族buff
		int mapId = getMapId(player.getPos());
		float bigMonsterSpeed = bigMonsterManager.getMonsterBuff(mapId, player) / 100F;
		// 母巢行军BUFF
		BroodWarInfo broodWarInfo = player.getBroodWarInfo();
		float broodSpeed = 0;
		if (broodWarInfo != null) {
			int buffId = broodWarInfo.getBroodWarBuff().getOrDefault(PropertyType.SPEED, 0);
			StaticBroodWarBuff buff = broodWarManager.getBuff(buffId);
			if (buff != null) {
				if (buff.getBuff().size() > 0) {
					for (List<Integer> l : buff.getBuff()) {
						if (l.size() >= 2) {
							if (l.get(0) == PropertyType.SPEED) {
								broodSpeed = l.get(1) / 100F;
							}
						}
					}
				}
			}
		}
		double commandBuff = broodWarManager.getCommandBuff(player, TechEffectId.ADD_MARCH_SPEED);
		double cityBuf = getCityBuf(player, CityBuffType.MARCH);
		double buff = 0d;
		if (getMapId(pos2) == MapId.FIRE_MAP) {
			buff = flameWarService.getBuff(BuffType.buff_5, player.getCountry(), player);
		}
		//double seasonBuf = seasonManager.getSeasonBuf(player, EffectType.EFFECT_TYPE_1);
		long period = (long) Math.ceil(distance / (configNum * 1.0f * (1 + spAdd)) * (1 - vipFactor) * (1 - propAdd) * (1 - bookEffectMarch) / (1 + bigMonsterSpeed) * (1 - broodSpeed) * (1 - commandBuff) * (1 - cityBuf) * (1 - buff));//* (1 - seasonBuf)
		// return period * TimeHelper.SECOND_MS;
		// long period = (long) Math.ceil(distance / (configNum * 1.0f * (1 + spAdd)) * (1 - vipFactor) * (1 - propAdd) * (1 - bookEffectMarch) / (1 + bigMonsterSpeed) * (1 - broodSpeed) * (1 - commandBuff) * (1 - cityBuf));
		long l = period * TimeHelper.SECOND_MS;
		//LogHelper.GAME_LOGGER.info("距离大于2得部分={}，大于部分得速度= {}", i, l1);
		Item item = player.getItem(ItemId.QUICK_MONSTER);
		if (item != null && marchType == MarchType.AttackMonster) {
			long l2;
			if (distance > 2) {
				int i = distance - 2;
				long ceil = (long) Math.ceil(i / (configNum * 1.0f * (1 + spAdd)) * (1 - vipFactor) * (1 - propAdd) * (1 - bookEffectMarch) / (1 + bigMonsterSpeed) * (1 - broodSpeed) * (1 - commandBuff) * (1 - cityBuf));
				long l1 = ceil * TimeHelper.SECOND_MS;
				l2 = l1 + 2000;
			} else {
				l2 = distance * 1000;
			}
			if (l2 > 0) {
				return l < l2 ? l : l2;
			}
		}
		return l;
	}

	public void addMarch(int mapId, March march) {
		MapInfo mapInfo = worldMapInfo.get(mapId);
		if (mapInfo == null) {
			return;
		}

		mapInfo.addMarch(march);
	}

	public void removeMarch(int mapId, March march) {
		MapInfo mapInfo = worldMapInfo.get(mapId);
		if (mapInfo == null) {
			return;
		}
		mapInfo.removeMarch(march);
	}

	// 获取当前采集进度
	public long getCollectPeriod(int quality) {
		if (quality == Quality.WHITE.get()) {
			return staticLimitMgr.getNum(12);
		} else if (quality == Quality.BLUE.get()) {
			return staticLimitMgr.getNum(13);

		} else if (quality == Quality.GREEN.get()) {
			return staticLimitMgr.getNum(14);

		} else if (quality == Quality.GOLD.get()) {
			return staticLimitMgr.getNum(15);

		} else if (quality == Quality.RED.get()) {
			return staticLimitMgr.getNum(16);

		} else if (quality == Quality.PURPLE.get()) {
			return staticLimitMgr.getNum(17);
		}

		return staticLimitMgr.getNum(12);
	}

	// 战斗体力值
	public int getEnergy(int type) {
		if (type == 1) {
			return staticLimitMgr.getNum(18);
		} else if (type == 2) {
			return staticLimitMgr.getNum(19);
		} else if (type == 3) {
			return staticLimitMgr.getNum(20);
		}

		return 10;
	}

	//public boolean addPrimaryPos(Player player, Pos pos, PrimaryCollect primaryCollect) {
	//    int mapId = getMapId(player);
	//    MapInfo mapInfo = getMapInfo(mapId);
	//    if (mapInfo == null) {
	//        return false;
	//    }
	//    if (!mapInfo.isFreePos(pos)) {
	//        return false;
	//    }
	//    boolean flag = mapInfo.addPos(pos, primaryCollect);
	//    if (!flag) {
	//        return false;
	//    }
	//    playerManager.clearPos(pos);
	//    return true;
	//}

	//public boolean removePrimaryPos(Player player, PrimaryCollect primaryCollect) {
	//    int mapId = getMapId(player);
	//    MapInfo mapInfo = getMapInfo(mapId);
	//    if (mapInfo == null) {
	//        return false;
	//    }
	//    mapInfo.removePos(primaryCollect.getPos());
	//    return true;
	//}

	// 删除坐标以及怪物
//	public void clearMonsterPos(MapInfo mapInfo, Pos pos) {
//		if (mapInfo == null) {
//			return;
//		}
//
//		mapInfo.clearMonsterPos(pos);
//	}

	// 删除坐标以及怪物
//	public void clearRebelMonsterPos(MapInfo mapInfo, Pos pos) {
//		if (mapInfo == null) {
//			return;
//		}
//
//		mapInfo.clearRebelMonsterPos(pos);
//	}

	// clearResourcePos
//	public void clearResourcePos(MapInfo mapInfo, Pos pos) {
//		if (mapInfo == null) {
//			return;
//		}
//
//		mapInfo.clearResourcePos(pos);
//	}
//
//	public void removeResPosOnly(MapInfo mapInfo, Pos pos) {
//		if (mapInfo == null) {
//			return;
//		}
//
//		mapInfo.removeResPosOnly(pos);
//	}

	public long getAttackPeriod(int type) {
		if (type == 1) {
			return staticLimitMgr.getNum(21) * TimeHelper.SECOND_MS;
		} else if (type == 2) {
			return staticLimitMgr.getNum(22) * TimeHelper.SECOND_MS;
		} else if (type == 3) {
			return staticLimitMgr.getNum(23) * TimeHelper.SECOND_MS;
		}
		return 0L;
	}

	// 获取行军消耗
	public int getMarchOil(List<Integer> heroIds, Player player, Pos targetPos) {
		int totalSoldier = 0;
		for (Integer heroId : heroIds) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}

			totalSoldier += hero.getCurrentSoliderNum();
		}

		Pos playerPos = player.getPos();
		int distance = distance(playerPos, targetPos);
		int oilCost = (int) Math.ceil(totalSoldier / 10) + distance * 30;

		return oilCost;
	}

	// 玩家被击飞,在当前地图随机一个坐标
	public void playerFly(Player player) {
		Pos oldPos = player.getPos();
		//int oldMapId = getMapId(oldPos);
		//MapInfo beforeMap = getMapInfo(oldMapId);
		int mapId = getMapId(player);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null, in player fly!");
			return;
		}

		Pos randPos = handlePlayerFlyPos(player);
		if (randPos.isError()) {
			LogHelper.CONFIG_LOGGER.error("randPos.isError, randPos = " + randPos + ", mapId = " + mapId);
			return;
		}

		// 飞到地图C了
		int randMapId = getMapId(randPos);
		MapInfo randMapInfo = getMapInfo(randMapId);
		if (randMapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("playerFly randMapInfo is null, randMapId = " + randMapId);
			return;
		}

		//if (!randMapInfo.isFreePos(randPos)) {
		//    LogHelper.CONFIG_LOGGER.error("pos is not free, randPos = " + randPos + ", randMapId = " + randMapId);
		//    return;
		//}

		PlayerCity playerCity = changePlayerPos(player, randPos);

//		handleWallFriendReturn(player);
		// 删除实体
		//int newMapId = getMapId(player);
		//MapInfo newMapInfo = getMapInfo(newMapId);
		//PlayerCity playerCity = newMapInfo.getPlayerCity(player.getPos());

		if (playerCity != null) {
			synEntityRq(playerCity, randMapInfo.getMapId(), oldPos); // 同步城池
		}

		if (player.getSimpleData() != null) {
			March march = player.getSimpleData().getRiotMarchs();
			if (march != null) {
				march.setEndPos(randPos);
				march.setStartPos(randPos);
				synMarch(mapId, march);
			}
		}

		MapInfo centerMap = getMapInfo(MapId.CENTER_MAP_ID);
		Iterator<March> it = centerMap.getMarches().iterator();
		while (it.hasNext()) {
			March next = it.next();
			if (next.getMarchType() == MarchType.ZERG_DEFEND_WAR && next.getLordId() == player.getRoleId()) {
				next.setStartPos(randPos);
				next.setEndPos(randPos);
				synMarch(next, player.getCountry());
			}
		}

		// 玩家由高等级地图被打到低等级地图 推送世界活动
		StaticWorldMap staticWorldMapNew = staticWorldMgr.getStaticWorldMap(randMapId);
		StaticWorldMap staticWorldMapOld = staticWorldMgr.getStaticWorldMap(mapId);
		if (staticWorldMapNew != null && staticWorldMapOld != null && staticWorldMapNew.getAreaType() == WorldAreaType.LOW_AREA) {
			if (staticWorldMapOld.getAreaType() == WorldAreaType.MIDDLE_AREA || staticWorldMapOld.getAreaType() == WorldAreaType.HIGH_AREA) {
				WorldData worldData = worldManager.getWolrdInfo();
				WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_9);
				if (worldActPlan != null) {
					worldActPlanService.syncWorldActivityPlan();
				}
			}
		}
	}


	public double getResRate() {
		return (double) staticLimitMgr.getNum(33) / 100.0;
	}

	public double getPeopleRate() {
		return (double) staticLimitMgr.getNum(34) / 100.0;
	}

	public double getGotResRate() {
		return (double) staticLimitMgr.getNum(233) / 100.0;
	}

	public double getGotPeopleRate() {
		return (double) staticLimitMgr.getNum(234) / 100.0;
	}

	// 抢夺人口+资源, 通过邮件来同步
	public List<Award> getLost(Player target) {
		Ware ware = target.getWare();
		int wareLv = ware.getLv();
		StaticWare staticWare = staticBuildingMgr.getStaticWare(wareLv);

		// 当前可以容纳的资源的上限
		long ironLimit = 0;
		long copperLimit = 0;
		long oilLimit = 0;
		if (staticWare != null) {
			// 当前可以容纳的资源的上限
			ironLimit = staticWare.getIron();
			copperLimit = staticWare.getCopper();
			oilLimit = staticWare.getOil();
		}

		long iron = target.getIron();
		long copper = target.getCopper();
		long oil = target.getOil();

		// 科技增加的百分比
		double techAdd = techManager.getWareCapacity(target);
		long addIronLimit = (long) (techAdd * (double) ironLimit);
		long addcopperLimit = (long) (techAdd * (double) copperLimit);
		long addoilLimit = (long) (techAdd * (double) oilLimit);

//        List<Integer> effects = beautyManager.getBeautySkillEffect(target, BeautySkillType.ADD_MAX_RESOURCE_WAREHOUSE);
//        double beautyAdd = 0;
//        if (effects != null && effects.size() > 0) {
//            beautyAdd = effects.get(1) * 1.0f / 100;
//        }
//        long addBeautyIronLimit = (long) (beautyAdd * (double) ironLimit);
//        long addBeautyCopperLimit = (long) (beautyAdd * (double) copperLimit);
//        long addBeautyOilLimit = (long) (beautyAdd * (double) oilLimit);

//        long ironDiff = iron - (ironLimit + addIronLimit + addBeautyIronLimit);
//        ironDiff = Math.max(0, ironDiff);
//        long copperDiff = copper - (copperLimit + addcopperLimit + addBeautyCopperLimit);
//        copperDiff = Math.max(0, copperDiff);
//        long oilDiff = oil - (oilLimit + addoilLimit + addBeautyOilLimit);
//        oilDiff = Math.max(0, oilDiff);

		long ironDiff = iron - (ironLimit + addIronLimit);
		ironDiff = Math.max(0, ironDiff);
		long copperDiff = copper - (copperLimit + addcopperLimit);
		copperDiff = Math.max(0, copperDiff);
		long oilDiff = oil - (oilLimit + addoilLimit);
		oilDiff = Math.max(0, oilDiff);
		long robeIron = (long) ((double) ironDiff * getResRate());
		long robeCopper = (long) ((double) copperDiff * getResRate());
		long robeOil = (long) ((double) oilDiff * getResRate());
		int people = target.getPeople();
		int robePeople = (int) ((double) people * getPeopleRate());

		List<Award> awards = new ArrayList<Award>();
		awards.add(new Award(0, AwardType.RESOURCE, 1, (int) robeIron));
		awards.add(new Award(0, AwardType.RESOURCE, 2, (int) robeCopper));
		awards.add(new Award(0, AwardType.RESOURCE, 3, (int) robeOil));
		awards.add(new Award(0, AwardType.PERSON, 0, robePeople));

		return awards;
	}

	public List<Award> getGot(Player target) {
		List<Award> awards = new ArrayList<>();
		awards.addAll(getLost(target));
		for (Award award : awards) {
			if (AwardType.PERSON == award.getType()) {
				award.setCount((int) (((double) award.getCount()) * getGotPeopleRate() / getPeopleRate()));
			} else {
				award.setCount((int) (((double) award.getCount()) * getGotResRate() / getResRate()));
			}
		}
		return awards;
	}

	// 检查城战是否已经结束了
	public boolean isPvpWarOver(WarInfo warInfo) {
		if (warInfo == null) {
			return true;
		}

		if (warInfo.getEndTime() <= System.currentTimeMillis()) {
			return true;
		}

		return false;
	}

	// 给玩家随机一个坐标点, 初级区域
	public void randerPlayerPos(Player player, boolean push) {
		Pos pos = pickPos(player.getCountry());
		int mapId = getMapId(pos);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
			return;
		}
		Pos oldPos = player.getOldPos();
		int oldMapId = getMapId(oldPos);
		MapInfo oldMapInfo = getMapInfo(oldMapId);
		PlayerCity playerCity = worldManager.changePlayerPos(player, pos);
		// 开启地图
		playerManager.openMap(player, mapId, MapStatusType.OPEN_CAN_MOVE);

		// 开启48小时保护时间
		Lord lord = player.getLord();
		long now = System.currentTimeMillis();
		long period = staticLimitMgr.getNum(71) * TimeHelper.HOUR_MS;
		if (lord != null) {
			lord.setProtectedTime(now + period);
			if (push) {
				synAllProtected(mapInfo.getMapId(), player);
			}
		}
		if (playerCity != null) {
			synEntityRq(playerCity, mapId, oldPos); // 同步城池
		}
	}

	// 给玩家随机一个坐标点, 初级区域
	public void playerBronPos(Player player) {
		Pos pos = bornPos(player.getCountry());
		int mapId = getMapId(pos);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
			return;
		}
		Pos oldPos = player.getOldPos();
		int oldMapId = getMapId(oldPos);
		MapInfo oldMapInfo = getMapInfo(oldMapId);
		PlayerCity playerCity = worldManager.changePlayerPos(player, pos);

		// 开启地图
		playerManager.openMap(player, mapId, MapStatusType.OPEN_CAN_MOVE);
		// 开启48小时保护时间
		Lord lord = player.getLord();
		long now = System.currentTimeMillis();
		long period = staticLimitMgr.getNum(71) * TimeHelper.HOUR_MS;
		if (lord != null) {
			lord.setProtectedTime(now + period);
		}
		if (playerCity != null) {
			synEntityRq(playerCity, mapId, oldPos); // 同步城池
		}
	}

	/**
	 * 获取出生坐标点
	 *
	 * @param country
	 * @return
	 */
	private Pos bornPos(int country) {
		int mapId = getBronMapId(country);
		if (mapId == 0) {
			LogHelper.CONFIG_LOGGER.error("get primer mapId wrong!");
			return new Pos();
		}

		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("get mapInfo wrong!");
			return new Pos();
		}

		Pos pos = givePlayerPos(mapInfo);
		return pos;
	}

	// 全区域通知保护罩
	public void synAllProtected(int mapId, Player target) {
		Map<Long, Player> playerMap = playerManager.getPlayers();
		Lord lord = target.getLord();
		if (lord == null) {
			LogHelper.CONFIG_LOGGER.error("lord is null!");
			return;
		}

		RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
		builder.setProtectedTime(lord.getProtectedTime());
		builder.setLordId(target.roleId);
		for (Player player : playerMap.values()) {
			if (getMapId(player) != mapId) {
				continue;
			}

			if (player.isLogin && player.getChannelId() != -1) {
				SynHelper.synMsgToPlayer(player, RolePb.SynChangeRq.EXT_FIELD_NUMBER, RolePb.SynChangeRq.ext, builder.build());
			}
		}
	}

	// 野怪先于资源刷[这个要重新优化]
	public void checkInitMonster() {
		// 找到配置
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		ConcurrentMap<Integer, MapInfo> worldMapInfo = getWorldMapInfo();
		WorldData data = getWolrdInfo();
		// mapType, monsterLevel, monsterRate * 10
		HashBasedTable<Integer, Integer, Map<Integer, Integer>> monsterFlushRate = staticWorldMgr.getMonsterFlushRate();
		List<Entity> list = new ArrayList<>();
		// 检查当前区域地图是否刷怪Ok
		// 每个区域刷1个
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			int areaType = staticWorldMap.getAreaType();
			// 不刷世界地图
			if (areaType == 3) {
				continue;
			}

			HashBasedTable<Integer, Integer, Integer> recordFlushNum = HashBasedTable.create();
			int mapId = staticWorldMap.getMapId();
			MapInfo mapInfo = worldMapInfo.get(staticWorldMap.getMapId());
			if (mapInfo == null) {
				continue;
			}

			Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
			for (Monster monster : monsterMap.values()) {
				if (monster == null) {
					continue;
				}
				Integer monsterCount = recordFlushNum.get(mapId, monster.getLevel());
				if (monsterCount == null) {
					monsterCount = 0;
				}
				recordFlushNum.put(mapId, monster.getLevel(), monsterCount + 1);
			}
			// logger.info("地图type={},区域名称={},刷新前虫族数量={}", areaType, staticWorldMap.getName(), monsterMap.size());
			int monsterMinLv = 1;
			int monsterMaxLv = 1;
			if (areaType == 1) {
				monsterMinLv = 1;
				monsterMaxLv = 10;
			} else if (areaType == 2) {
				monsterMinLv = 2;
				monsterMaxLv = 15;
			}
			// TODO 测试代码
			int targetType = data.getTarget() > 10 ? 10 : data.getTarget();
			Map<Integer, Integer> integerIntegerMap = monsterFlushRate.get(targetType, areaType);
			for (int monsterLv = monsterMinLv; monsterLv <= monsterMaxLv; monsterLv++) {
				// int maxNum = monsterFlushRate.get(areaType, monsterLv);
				Integer maxNum = integerIntegerMap.get(monsterLv);
				Integer currentNum = recordFlushNum.get(mapId, monsterLv);
				for (int monsterNum = 1; monsterNum <= maxNum; monsterNum++) {

					if (currentNum == null) {
						currentNum = 0;
					}

					if (currentNum >= maxNum) {
						break;
					}

					Pos monsterPos = mapInfo.randPickPos();

					Monster monster = addMonster(monsterPos, 1000 + monsterLv, monsterLv, mapInfo, AddMonsterReason.FLUSH_INIT_MONSTER);
					if (monster == null) {
						continue;
					}

					recordFlushNum.put(mapId, monsterLv, currentNum + 1);
				}
			}
		}
	}

	public void clearDate(Calendar c) {
		if (c != null) {
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
		}
	}

	public int dayDiff(Date origin, Date now) {
		Calendar orignC = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		orignC.setTime(origin);
		clearDate(orignC);
		calendar.setTime(now);
		clearDate(calendar);

		return (int) ((calendar.getTimeInMillis() - orignC.getTimeInMillis()) / (24 * 3600 * 1000)) + 1;
	}

	@Deprecated
	public void flushInitActMonster() {
		// 找到配置
		StaticActMonster actMonster = staticWorldMgr.getActMonster(1);
		if (actMonster == null) {
			LogHelper.CONFIG_LOGGER.error("config actMonster error!");
			return;
		}

		int day = dayDiff(serverManager.getServer().getOpenTime(), new Date());
		int configDay = actMonster.getLastTime() / 24;
		// 超过几天之后不刷怪
		int diff = Math.abs(day) - configDay;
		if (diff > 0) {
			clearActMonster();
			return;
		}

		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		ConcurrentMap<Integer, MapInfo> worldMapInfo = getWorldMapInfo();

		// 检查当前区域地图是否刷怪Ok
		// 每个区域刷1个
		int maxNum = actMonster.getMaxNum();
		int monsterLv = actMonster.getMonsterLv();
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			int areaType = staticWorldMap.getAreaType();
			// 不刷世界地图
			if (areaType == 2 || areaType == 3) {
				continue;
			}

			int mapId = staticWorldMap.getMapId();
			MapInfo mapInfo = worldMapInfo.get(mapId);
			if (mapInfo == null) {
				continue;
			}

			Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
			int totalNum = 0;
			for (Monster monster : monsterMap.values()) {
				if (monster.getId() == 1101) {
					totalNum++;
				}
			}

			if (totalNum >= maxNum) {
				continue;
			}

			for (int num = 1; num <= maxNum; num++) {
				Pos monsterPos = mapInfo.randPickPos();
				// 创建一个野怪
				Monster monster = addMonster(monsterPos, 1000 + monsterLv, monsterLv, mapInfo, AddMonsterReason.FLUSH_ACT_MONSTER);
				if (monster == null) {
					continue;
				}
				totalNum++;
				if (totalNum >= maxNum) {
					break;
				}
			}
		}
	}

	// 时间结束则清除活动怪 // 清除野怪
	// worldManager.clearMonsterPos(mapInfo, monster.getPos());
	public void clearActMonster() {
		if (isActMonsterClear) {
			return;
		}

		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		ConcurrentMap<Integer, MapInfo> worldMapInfo = getWorldMapInfo();
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			int areaType = staticWorldMap.getAreaType();
			// 不刷世界地图
			if (areaType == 2 || areaType == 3) {
				continue;
			}

			int mapId = staticWorldMap.getMapId();
			MapInfo mapInfo = worldMapInfo.get(mapId);
			if (mapInfo == null) {
				continue;
			}

			Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
			Iterator<Monster> iterator = monsterMap.values().iterator();
			while (iterator.hasNext()) {
				Monster monster = iterator.next();
				if (monster != null && monster.getId() == 1101) {
//					removeMonsterPos(mapInfo, monster.getPos());
					mapInfo.clearPos(monster.getPos());
					iterator.remove();
				}
			}
		}

		isActMonsterClear = true;
	}

	public ConcurrentHashMap<Integer, WorldData> getWorldData() {
		return worldData;
	}

	public void setWorldData(ConcurrentHashMap<Integer, WorldData> worldData) {
		this.worldData = worldData;
	}

	public void updateWorld(World world) {
		try {
			worldDao.updateWorld(world);
		} catch (Exception ex) {
			LogHelper.ERROR_LOGGER.error(ex.getMessage()); // 断网会触发
		}

	}

	public void initWorld() {

	}

	// 单独开个定时器保存即可
	public void saveWorldTimerLogic() {
		WorldData data = getWolrdInfo();
		if (data == null) {
			LogHelper.CONFIG_LOGGER.error("data is null");
			return;
		}

		long now = System.currentTimeMillis();
		saveWorldData(data, now);
		// checkSeason(data, now);
		checkNewSeason(data);
	}

	public void saveWorldData(WorldData data, long now) {
		int saveCount = 0;
		if (now - data.getLastSaveTime() >= 180000L) {
			saveCount++;
			try {
				data.setLastSaveTime(now);
				worldManager.updateWorld(new World(data));
			} catch (Exception e) {
				logger.error("saveWorldData{}", e);
			}
		}

		if (saveCount != 0) {
			// LogHelper.SAVE_LOGGER.error("save city count:" + saveCount);
		}
	}

	public void checkNewSeason(WorldData data) {
		if (data.getSeason() == 0) {
			return;
		}
		int value = LocalDate.now().getDayOfWeek().getValue();
		if (data.getSeasonUp() != 2) {
			StaticSeason seasonByWeek = staticWorldMgr.getSeasonByWeek(value);
			data.setSeason(seasonByWeek.getKeyId());
			data.setEffect(seasonByWeek.getKeyId());
			data.setSeasonUp(2);
			data.setSeasonEndTime(TimeHelper.getZeroOfDay() + 86400);
		} else {
			StaticSeason season = staticWorldMgr.getSeason(data.getSeason());
			if (season != null && season.getWeekDay() != value) {
				StaticSeason season1 = staticWorldMgr.getSeason(season.getNext());
				if (season1 != null) {
					data.setSeason(season1.getKeyId());
					data.setEffect(season1.getKeyId());
					synSeasonToPlayer(data);
				}
			}
		}
	}

	public void openNewSeason() {
		WorldData worldData = getWolrdInfo();
		if (worldData == null || worldData.getTasks().get(7) == null) {
			LogHelper.CONFIG_LOGGER.error("worldData is null!");
			return;
		}
		// 开启季节系统
		if (worldData.getSeason() == 0) {
			flushWorldSeason(worldData);
			// 同步季节
			synSeasonToPlayer(worldData);
		}
	}

	public void flushWorldSeason(WorldData worldData) {
		StaticSeason seasonByWeek = staticWorldMgr.getSeasonByWeek(LocalDate.now().getDayOfWeek().getValue());
		if (seasonByWeek != null) {
			worldData.setSeason(seasonByWeek.getKeyId());
			worldData.setEffect(seasonByWeek.getKeyId());
			worldData.setSeasonUp(2);
			worldData.setSeasonEndTime(TimeHelper.getZeroOfDay() + 86400);
		}
	}

	public void synSeasonToPlayer(WorldData data) {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		WorldPb.SynSeasonRq.Builder builder = WorldPb.SynSeasonRq.newBuilder();
		CommonPb.SeasonInfo.Builder dataSeason = CommonPb.SeasonInfo.newBuilder();
		dataSeason.setSeasonId(data.getSeason());
		dataSeason.setEndTime(TimeHelper.getZeroOfDay() + 86400);
		dataSeason.setEffectId(data.getEffect());
		builder.setInfo(dataSeason);
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null || !player.isLogin || player.getChannelId() == -1) {
				continue;
			}
			SynHelper.synMsgToPlayer(player, WorldPb.SynSeasonRq.EXT_FIELD_NUMBER, WorldPb.SynSeasonRq.ext, builder.build());
		}
	}


	public void dserWorld(World world) {
		WorldData data = worldData.get(WORLD_ID);
		if (data == null) {
			LogHelper.CONFIG_LOGGER.error("data is null!");
			return;
		}
		data.setKeyId(world.getKeyId());
		// read boss data.
		byte[] bossData = world.getBossData();
		if (bossData == null) {
			handlerWolrdBoss(data);
		} else {
			dserBossData(bossData, data);
		}

		// read target data.
		byte[] targetData = world.getTargetData();
		if (targetData == null) {
			handlerWorldTarget(data);
		} else {
			dserTargetData(targetData, data);
		}

		// read chat show data.
		byte[] chatShow = world.getChatShowData();
		if (chatShow != null) {
			dserChatShow(chatShow, data);
		}

		// read season
		data.setSeason(world.getSeason());
		// read season end time
		data.setSeasonEndTime(world.getSeasonEndTime());
		// read effect
		data.setEffect(world.getEffect());
		data.setSeasonUp(world.getSeasonUp());
		// read pvp battle
		byte[] pvpBattleData = world.getPvpBattleData();
		if (pvpBattleData != null) {
			dserPvpBattleData(pvpBattleData, data);
		}

		// check season, season and effect is both open!
		if (data.getSeason() == 0 && data.getEffect() != 0) {
			data.setEffect(0);
		}

		// TODO jyb反序列化世界目标任务
		desrWorldTargetTask(world.getWorldTargetData(), data);

		byte[] worldActPlanData = world.getWorldActPlanData();
		if (worldActPlanData != null) {
			desrWorldActPlan(worldActPlanData, data);
		}
		data.setTodayMaxOnLineNum(world.getTodayMaxOnLineNum());
		data.setTotalMaxOnLineNum(world.getTotalMaxOnLineNum());
		data.setRefreshTime(world.getRefreshTime());
		data.setStealCity(world.getStealCityData());
		data.setRiotLevel(world.getRiotLevel());
		data.setBigMonster(world.getBigMonster());

		// 虫族主宰数据
		data.dserZergData(world.getZerg());

		data.dserCityRemarkData(world);
	}

	public void dserPvpBattleData(byte[] pvpBattleData, WorldData worldData) {
		try {
			SerPvpBattleData builder = SerPvpBattleData.parseFrom(pvpBattleData);
			worldData.setWorldPvpState(builder.getWorldPvpState());
			LinkedList<PvpBattle> pvpBattles = worldData.getPvpBattles();
			for (DataPb.PvpBattle data : builder.getPvpBattlesList()) {
				if (data == null) {
					continue;
				}

				PvpBattle pvpBattle = new PvpBattle();
				pvpBattle.readData(data);
				pvpBattles.add(pvpBattle);
				if (pvpBattles.size() > 4) {
					LogHelper.CONFIG_LOGGER.error("存入pvpBattles ->[{}]", new Exception("error"));
				}
			}

			LinkedList<RankPvpHero> rankList = broodWarManager.getRanks();
			for (DataPb.RankPvpHero rankHero : builder.getRankListList()) {
				if (rankHero == null) {
					continue;
				}
				RankPvpHero rankPvpHero = new RankPvpHero();
				rankPvpHero.readData(rankHero);
				rankList.add(rankPvpHero);
			}

			HashSet<Long> attenders = worldData.getAttenders();
			for (Long lordId : builder.getAttendersList()) {
				attenders.add(lordId);
			}

			LinkedList<PvpHero> deadHeros = worldData.getDeadHeroes();
			for (DataPb.PvpHeroData pvpHeroData : builder.getDeadHerosList()) {
				if (pvpBattleData == null) {
					continue;
				}

				PvpHero pvpHero = new PvpHero();
				pvpHero.readData(pvpHeroData);
				deadHeros.add(pvpHero);
			}

			HashMap<Integer, HashMap<Integer, DigInfo>> digPapers = worldData.getDigPapers();
			for (int i = 1; i <= 3; i++) {
				HashMap<Integer, DigInfo> check = digPapers.get(i);
				if (check == null) {
					check = new HashMap<Integer, DigInfo>();
					digPapers.put(i, check);
				}
			}

			for (DataPb.CountryDigInfo countryDigInfo : builder.getCountryDigInfoList()) {
				if (countryDigInfo == null) {
					continue;
				}

				int country = countryDigInfo.getCountry();
				HashMap<Integer, DigInfo> check = digPapers.get(country);
				for (DataPb.DigInfoData digInfoData : countryDigInfo.getInfoList()) {
					if (digInfoData == null) {
						continue;
					}
					DigInfo digInfo = new DigInfo();
					digInfo.readData(digInfoData);
					check.put(digInfo.getItemId(), digInfo);
				}
			}

			worldData.setBanquetEndTime(builder.getBanquetEndTime());
			worldData.setPvpCountry(builder.getPvpCountry());
			worldData.setPvpEndTime(builder.getPvpEndTime());
			worldData.setPvpPeriod(builder.getPvpPeriod());
			worldData.setActivityEndTime(builder.getActivityEndTime());

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			logger.error("dserPvpBattleData error", e);
		}
	}

	public void dserChatShow(byte[] chatShow, WorldData worldData) {
		Map<Integer, ChatShow> chatShowMap = worldData.getChatShowMap();
		try {
			SerializePb.SerChatShowData builder = SerializePb.SerChatShowData.parseFrom(chatShow);
			for (DataPb.ChatShowData data : builder.getChatShowDataList()) {
				ChatShow chatShowInfo = new ChatShow();
				chatShowInfo.readData(data);
				chatShowMap.put(chatShowInfo.getKeyId(), chatShowInfo);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("dserChatShow error", e);
		}
	}

	public void handlerWolrdBoss(WorldData worldData) {
		Map<Integer, StaticWorldTarget> worldTarget = staticWorldMgr.getWorldTargetMap();
		Map<Integer, WorldBoss> bossMap = worldData.getBossMap();
		for (StaticWorldTarget target : worldTarget.values()) {
			if (target.getType() != 3) {
				continue;
			}
			int monsterId = target.getBossId();
			if (monsterId == 0) {
				continue;
			}

			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
			if (staticMonster == null) {
				LogHelper.CONFIG_LOGGER.error("boss id =" + monsterId + " is not exists!");
				continue;
			}

			if (monsterId == WorldBossId.WORLD_BOSS_1) {
				for (int country = 1; country <= 3; country++) {
					WorldBoss worldBoss = new WorldBoss();
					worldBoss.setMonsterId(staticMonster.getMonsterId());
					worldBoss.setSoldier(staticMonster.getSoldierCount());
					worldBoss.setCountry(country);
					worldBoss.setMaxSoldier(staticMonster.getSoldierCount());
					bossMap.put(country, worldBoss);
				}
			} else if (monsterId == WorldBossId.WORLD_BOSS_2) {
				WorldBoss share = new WorldBoss();
				share.setMonsterId(staticMonster.getMonsterId());
				share.setSoldier(staticMonster.getSoldierCount());
				share.setMaxSoldier(staticMonster.getSoldierCount());
				share.setCountry(0);
				worldData.setShareBoss(share);
			}
		}
	}

	public void dserBossData(byte[] bossData, WorldData worldData) {
		Map<Integer, WorldBoss> bossMap = worldData.getBossMap();
		try {
			SerWorldBossData builder = SerWorldBossData.parseFrom(bossData);
			for (DataPb.WorldBossData data : builder.getWorldBossList()) {
				if (data == null) {
					continue;
				}
				WorldBoss worldBoss = new WorldBoss();
				worldBoss.readData(data);

				StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(WorldBossId.WORLD_BOSS_1);
				if (staticMonster != null) {
					worldBoss.setMaxSoldier(staticMonster.getSoldierCount());
					if (worldBoss.getSoldier() >= worldBoss.getMaxSoldier()) {
						worldBoss.setSoldier(worldBoss.getMaxSoldier());
					}
					if (worldBoss.isKilled()) {
						worldBoss.setSoldier(0);
					}
				}
				bossMap.put(worldBoss.getCountry(), worldBoss);
			}

			// 兼容性代码
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(WorldBossId.WORLD_BOSS_1);
			if (staticMonster != null) {
				for (int country = 1; country <= 3; country++) {
					WorldBoss worldBoss = bossMap.get(country);
					if (worldBoss == null) {
						worldBoss = new WorldBoss();
						worldBoss.setMonsterId(staticMonster.getMonsterId());
						worldBoss.setSoldier(staticMonster.getSoldierCount());
						worldBoss.setCountry(country);
						worldBoss.setMaxSoldier(staticMonster.getSoldierCount());
						if (worldBoss.getSoldier() >= worldBoss.getMaxSoldier()) {
							worldBoss.setSoldier(worldBoss.getMaxSoldier());
						}
						bossMap.put(country, worldBoss);
					}
				}
			}

			if (builder.getShareBoss() != null) {
				WorldBoss shareBoss = new WorldBoss();
				shareBoss.readData(builder.getShareBoss());
				StaticMonster boss2Config = staticMonsterMgr.getStaticMonster(WorldBossId.WORLD_BOSS_2);
				if (boss2Config != null) {
					shareBoss.setMaxSoldier(boss2Config.getSoldierCount());
				}
				if (shareBoss.getSoldier() >= shareBoss.getMaxSoldier()) {
					shareBoss.setSoldier(shareBoss.getMaxSoldier());
				}

				worldData.setShareBoss(shareBoss);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("dserBossData error", e);
		}

	}

	// 处理世界目标
	public void handlerWorldTarget(WorldData worldData) {
		Map<Integer, StaticWorldTarget> staticWorldTarget = staticWorldMgr.getWorldTargetMap();
		Map<Integer, WorldTarget> worldTargets = worldData.getWorldTargets();
		for (StaticWorldTarget target : staticWorldTarget.values()) {
			if (target == null) {
				continue;
			}

			if (target.getType() != 2) {
				continue;
			}

			WorldTarget worldTarget = new WorldTarget();
			worldTarget.setTargetId(target.getTargetId());

			worldTarget.setStatus(0);
			worldTarget.setCountry(0);
			// 攻克城
			if (target.getType() == 2) {
				boolean isDone = cityManager.captureCity(target.getCityType());
				if (isDone) {
					worldTarget.setStatus(1);
				} else {
					worldTarget.setStatus(0);
				}
			}

			worldTargets.put(target.getTargetId(), worldTarget);
		}
	}

	public void dserTargetData(byte[] targetData, WorldData worldData) {
		Map<Integer, WorldTarget> worldTargets = worldData.getWorldTargets();
		try {
			SerWorldTargetData builder = SerWorldTargetData.parseFrom(targetData);
			for (DataPb.WorldTargetData data : builder.getWorldTargetList()) {
				if (data == null) {
					continue;
				}
				WorldTarget worldTarget = new WorldTarget();
				worldTarget.readData(data);
				// 攻克城
				StaticWorldTarget config = staticWorldMgr.getStaticWorldTarget(worldTarget.getTargetId());
				if (config.getType() == 2) {
					boolean isDone = cityManager.captureCity(config.getCityType());
					if (isDone) {
						worldTarget.setStatus(1);
					} else {
						worldTarget.setStatus(0);
					}
				}
				worldTargets.put(worldTarget.getTargetId(), worldTarget);
			}

			// check
			Map<Integer, StaticWorldTarget> staticWorldTarget = staticWorldMgr.getWorldTargetMap();
			for (StaticWorldTarget config : staticWorldTarget.values()) {
				if (config == null) {
					continue;
				}

				if (config.getType() != 2) {
					continue;
				}

				WorldTarget worldTarget = worldTargets.get(config.getTargetId());
				if (worldTarget == null) {
					worldTarget = new WorldTarget();
					worldTarget.setTargetId(config.getTargetId());
					worldTarget.setStatus(0);
					worldTarget.setCountry(0);
				}

				// 攻克城
				if (config.getType() == 2) {
					boolean isDone = cityManager.captureCity(config.getCityType());
					if (isDone) {
						worldTarget.setStatus(1);
					} else {
						worldTarget.setStatus(0);
					}
				}

				worldTargets.put(config.getTargetId(), worldTarget);
			}

		} catch (InvalidProtocolBufferException e) {
			logger.error("dserTargetData error", e);
		}
	}

	public int getMonsterNum(int targetId) {
		StaticWorldTarget staticWorldTarget = staticWorldMgr.getStaticWorldTarget(targetId);
		if (staticWorldTarget == null) {
			LogHelper.CONFIG_LOGGER.error("targetId = " + targetId + " config not exists!");
			return Integer.MAX_VALUE;
		}

		return staticWorldTarget.getMonsterNum();
	}

	public void doKillWorldMonster(int targetId, Player player) {
		if (targetId == 0) {
			LogHelper.CONFIG_LOGGER.error("3.server logic error here!");
			return;
		}
		if (targetId == WorldTargetType.KILL_ROIT) {
			SimpleData data = player.getSimpleData();
			if (data != null) {
				data.addKillRiot();
			}
		}
	}

	public void attackCityTarget(int cityId, int country) {
		int targetId = 0;
		// cityType相关的逻辑
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity is null");
			return;
		}

		int cityType = staticWorldCity.getType();
		// 名城和四方要塞不进行target完成
		if (cityType == CityType.FAMOUS_CITY || cityType == CityType.SQUARE_FORTRESS) {
			return;
		}
		if (cityId != 0) {
			targetId = getWorldTargetId(cityType);
		}

		// 能通过cityType 找到targetId说明完成了这个target
		// 名城或者都城会出现这个
		if (targetId == 0) {
			// LogHelper.ERROR_LOGGER.error("1. server logic error here!");
			return;
		}

		if (isWorldTargetFinished(targetId)) {
			return;
		}

		if (targetId == WorldTargetType.WIN_CITY_TYPE2) {
			cityManager.handleAttackTown(staticWorldCity);
		}

		cityOpenMap(cityId);
		doneWorldTarget(targetId, country);
		synCityWorldTarget2All(targetId, country);

	}

//    public void doBossWorldTarget(int country, int bossId) {
//        // cityType相关的逻辑
//        int targetId = 0;
//        if (bossId != 0) {
//            targetId = getBossType(bossId);
//        }
//
//        if (targetId == 0) {
//            LogHelper.CONFIG_LOGGER.error("2.server logic error here!");
//            return;
//        }
//
//        if (!isBossCondionOk(targetId, bossId)) {
//            return;
//        }
//
//        bossOpenMap(bossId, country);
//        if (targetId != 9) {
//            // 发送下一个世界目标
//            synBossWorldTarget2All(targetId);
//        }
//    }

	// 仅用来检测城池
	public boolean isWorldTargetFinished(int targetId) {
		WorldTarget worldTarget = getWorldTarget(targetId);
		if (worldTarget == null) {
			LogHelper.CONFIG_LOGGER.error("worldTarget is null!");
			return false;
		} else {
			return worldTarget.getStatus() >= 1; // 表示已经同步过了
		}
	}

	public int getWorldMapOpen() {
		boolean isCaupture = cityManager.captureCity(CityType.WALL);
		if (isCaupture) {
			return 1;
		}

		return 0;
	}

	public int getWorldTargetId(int cityType) {
		if (cityType == 1) {
			return WorldTargetType.WIN_CITY_TYPE1;
		} else if (cityType == 2) {
			return WorldTargetType.WIN_CITY_TYPE2;
		} else if (cityType == 3) {
			return WorldTargetType.WIN_CITY_TYPE3;
		} else if (cityType == 4) {
			return WorldTargetType.WIN_CITY_TYPE4;
		} else if (cityType == 5) {
			return WorldTargetType.WIN_CITY_TYPE5;
		} else if (cityType == 6) {
			return WorldTargetType.WIN_CITY_TYPE6;
		}

		return 0;
	}

	public int getBossType(int bossId) {
		if (bossId == 10000) {
			return WorldTargetType.KILL_BOSS1;
		} else if (bossId == 10001) {
			return WorldTargetType.KILL_BOSS2;
		}

		return 0;
	}

	public boolean isKillWorldMonsteOk(int targetId, Player player) {
		StaticWorldTarget staticWorldTarget = staticWorldMgr.getStaticWorldTarget(targetId);
		if (staticWorldTarget == null) {
			LogHelper.CONFIG_LOGGER.error("worldtarget config is null, targetId = " + targetId);
			return false;
		}

		if (staticWorldTarget.getType() == 1) {
			if (player.getKillMonsterNum() >= getMonsterNum(targetId)) {
				return true;
			}
		}

		return false;
	}

	public boolean isBossCondionOk(int targetId, int bossId) {
		StaticWorldTarget staticWorldTarget = staticWorldMgr.getStaticWorldTarget(targetId);
		if (staticWorldTarget == null) {
			LogHelper.CONFIG_LOGGER.error("worldtarget config is null, targetId = " + targetId);
			return false;
		}

		if (bossId == staticWorldTarget.getBossId()) {
			// 全服广播
			return true;
		}

		return false;
	}

//    // 同步击杀世界野怪的状态
//    public void synWorldTargetRq(int targetId, Player player, int country) {
//        // 如果是世界boss, 需要同步世界boss
//        SynWorldTargetRq.Builder builder = SynWorldTargetRq.newBuilder();
//        builder.setTargetId(targetId);
//        builder.setStatus(player.getWorldKillMonsterStatus());
//        builder.setCountry(country);
//        builder.setKillMonsterNum(player.getKillMonsterNum());
//
//        sendWorldTarget(player, builder);
//
//    }

	// 同步击杀城池状态
	public void synCityWorldTarget2All(int targetId, int country) {
		// 如果是世界boss, 需要同步世界boss
		SynWorldTargetRq.Builder builder = SynWorldTargetRq.newBuilder();
		WorldTarget worldTarget = getWorldTarget(targetId);
		builder.setTargetId(targetId);
		builder.setStatus(worldTarget.getStatus());
		builder.setCountry(country);

		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}

			WorldTarget playerTarget = getWorldTarget(player);
			if (playerTarget == null) {
				continue;
			}

			if (playerTarget.getTargetId() != worldTarget.getTargetId()) {
				continue;
			}

			sendWorldTarget(player, builder);
		}

		worldTarget.setStatus(2);
	}

//    // 同步击杀世界Boss状态
//    public void synBossWorldTarget2All(int targetId) {
//        // 如果是世界boss, 需要同步世界boss
//        SynWorldTargetRq.Builder builder = SynWorldTargetRq.newBuilder();
//        WorldTarget worldTarget = getWorldTarget(targetId + 1);
//        if (worldTarget != null) {
//            builder.setTargetId(worldTarget.getTargetId());
//            builder.setStatus(worldTarget.getStatus());
//            builder.setCountry(worldTarget.getCountry());
//        }
//
//        Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
//        while (iterator.hasNext()) {
//            Player player = iterator.next();
//            if (player != null && player.isLogin && player.getChannelId() != -1) {
//                WorldTarget playerTarget = getWorldTarget(player);
//                if (playerTarget == null) {
//                    continue;
//                }
//
//                if (worldTarget == null) {
//                    continue;
//                }
//
//                if (playerTarget.getTargetId() != worldTarget.getTargetId()) {
//                    continue;
//                }
//
//                sendWorldTarget(player, builder);
//            }
//        }
//
//    }

	public void sendWorldTarget(Player player, SynWorldTargetRq.Builder builder) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(player, WorldPb.SynWorldTargetRq.EXT_FIELD_NUMBER, WorldPb.SynWorldTargetRq.ext, builder.build());
		}
	}

	public WorldTarget getWorldTarget(int targetId) {
		WorldData data = worldData.get(1);
		if (data == null) {
			return null;
		}
		// 城池的世界目标
		ConcurrentHashMap<Integer, WorldTarget> worldTargets = data.getWorldTargets();
		WorldTarget find = null;
		for (WorldTarget worldTarget : worldTargets.values()) {
			if (worldTarget.getTargetId() == targetId) {
				find = worldTarget;
				break;
			}
		}

		// 兼容代码
		if (find == null) {
			WorldTarget worldTarget = new WorldTarget();
			worldTarget.setTargetId(targetId);
			worldTarget.setStatus(0);
			worldTarget.setCountry(0);
			worldTargets.put(targetId, worldTarget);
			return worldTarget;
		}

		return find;

	}

	public void doneWorldTarget(int targetId, int country) {
		WorldTarget find = getWorldTarget(targetId);
		if (find == null) {
			LogHelper.CONFIG_LOGGER.error("world target id = " + targetId + " is null!");
			return;
		}

		find.setStatus(1);
		find.setCountry(country);
	}

	// 开启地图
	public void cityOpenMap(int cityId) {
		// 1.攻克cityType=3后开启世界地图（未开放的区域显示锁定状态）
		// 3.攻克cityType=4后解锁临近州城区域显示，但不可迁移至其他州城
		// 4.攻克cityType=5后解锁季节系统, 开启选举
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity is null, cityId = " + cityId);
			return;
		}

		int cityType = staticWorldCity.getType();
		if (cityType == CityType.WALL) {
			// 解锁世界地图
			Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
			while (iterator.hasNext()) {
				Player player = iterator.next();
				if (player != null) {
					synWorldMapStatus(player, 1);
				}
			}

		} else if (cityType == CityType.CAMP) {
			openMiddleMap();
		}

		if (cityType == staticLimitMgr.getNum(163)) {
			handlerVote();
		}

//        if (cityType == staticLimitMgr.getNum(164)) {
//            openSeason();
//        }
	}

	public void handlerVote() {
		try {
			countryManager.notifyVote(); // 开启选举
		} catch (Exception ex) {
			logger.error("handlerVote {}", ex);
		}
	}

	public void bossOpenMap(int bossId, int country) {
		// 2.打败BossId=10000后解锁上一级州城区域和兄弟区域（玩家可迁移至州城和临近的兄弟区域）
		// 5.击败董卓后BossId=10001解锁皇城并开放全图区域
		if (bossId == WorldBossId.WORLD_BOSS_1) {
			openPrimaryMap();
		} else if (bossId == WorldBossId.WORLD_BOSS_2) {
			openAll();
		}

	}

	public void synWorldMapStatus(Player player, int worldMapOpen) {
		if (player == null) {
			return;
		}

		if (!player.isLogin) {
			return;
		}

		if (player.getChannelId() == -1) {
			return;
		}

		WorldPb.SynWorldMapStatusRq.Builder builder = WorldPb.SynWorldMapStatusRq.newBuilder();
		builder.setMapStatus(worldMapOpen);
		SynHelper.synMsgToPlayer(player, WorldPb.SynWorldMapStatusRq.EXT_FIELD_NUMBER, WorldPb.SynWorldMapStatusRq.ext, builder.build());
	}

	public WorldData getWolrdInfo() {
		WorldData data = worldData.get(1);
		if (data == null) {
			return null;
		}
		return data;
	}

	// 解锁上一级州城区域和兄弟区域（玩家可迁移至州城和临近的兄弟区域）
	public List<Integer> getPrimaryMap(int mapId) {
		List<Integer> mapIds = new ArrayList<Integer>();
		// 先找兄弟
		Map<Integer, StaticWorldMap> config = staticWorldMgr.getWorldMap();
		StaticWorldMap currentMap = staticWorldMgr.getStaticWorldMap(mapId);
		if (currentMap.getAreaType() != WorldAreaType.LOW_AREA) {
			return mapIds;
		}

		int parentId = currentMap.getBelong();
		if (parentId == 0) {
			return mapIds;
		}

		mapIds.add(parentId);
		for (StaticWorldMap staticWorldMap : config.values()) {
			if (staticWorldMap.getMapId() == mapId) {
				continue;
			}

			if (staticWorldMap.getBelong() == parentId) {
				mapIds.add(staticWorldMap.getMapId());
			}
		}

		return mapIds;

	}

	public void openPrimaryMap() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player != null) {
				List<MapStatus> mapStatuses = checkPrimaryMap(player);
				if (!mapStatuses.isEmpty()) {
					player.updateMapStatuses(mapStatuses);
				}
				synWorldMapStatus(player, mapStatuses);
			}
		}
	}

	// 解锁上一级州城区域和兄弟区域（玩家可迁移至州城和临近的兄弟区域）
	public List<MapStatus> checkPrimaryMap(Player player) {
		// 获取当前玩家的初级区域
		int mapId = playerManager.getMapId(player);
		if (mapId == 0) {
			return new ArrayList<MapStatus>();
		}

		List<Integer> mapIds = getPrimaryMap(mapId);
		List<MapStatus> updates = new ArrayList<MapStatus>();
		for (Integer elem : mapIds) {
			MapStatus mapStatus = player.findMapStatus(elem);
			if (mapStatus == null) {
				LogHelper.CONFIG_LOGGER.error("mapStatus is null, mapId = " + elem);
				continue;
			}
			mapStatus.setStatus(2);
			updates.add(mapStatus);
		}

		return updates;
	}

	public void synWorldMapStatus(Player player, List<MapStatus> mapStatuses) {
		if (!player.isLogin) {
			return;
		}

		if (player.getChannelId() == -1) {
			return;
		}

		if (mapStatuses.isEmpty()) {
			return;
		}
		WorldPb.SynAllMapStatusRq.Builder builder = WorldPb.SynAllMapStatusRq.newBuilder();
		for (MapStatus mapStatus : mapStatuses) {
			if (mapStatus == null) {
				LogHelper.CONFIG_LOGGER.error("mapStatus is null!");
				continue;
			}

			if (mapStatus.getMapId() == 0) {
				continue;
			}

			builder.addMapStatus(mapStatus.wrapPb());
		}

		SynHelper.synMsgToPlayer(player, WorldPb.SynAllMapStatusRq.EXT_FIELD_NUMBER, WorldPb.SynAllMapStatusRq.ext, builder.build());
	}

	public void synWorldMapStatus(Player player) {
		Collection<MapStatus> mapStatuses = player.getMapStatusMap().values();
		if (!player.isLogin) {
			return;
		}

		if (player.getChannelId() == -1) {
			return;
		}

		if (mapStatuses.isEmpty()) {
			return;
		}
		WorldPb.SynAllMapStatusRq.Builder builder = WorldPb.SynAllMapStatusRq.newBuilder();
		for (MapStatus mapStatus : mapStatuses) {
			if (mapStatus == null) {
				LogHelper.CONFIG_LOGGER.error("mapStatus is null!");
				continue;
			}

			if (mapStatus.getMapId() == 0) {
				continue;
			}

			builder.addMapStatus(mapStatus.wrapPb());
		}
		SynHelper.synMsgToPlayer(player, WorldPb.SynAllMapStatusRq.EXT_FIELD_NUMBER, WorldPb.SynAllMapStatusRq.ext, builder.build());
	}

	// 解锁临近州城区域 （玩家不可迁移至州城）
	public List<Integer> getMiddleMap() {
		List<Integer> mapIds = new ArrayList<Integer>();
		// 先找兄弟
		Map<Integer, StaticWorldMap> config = staticWorldMgr.getWorldMap();
		for (StaticWorldMap staticWorldMap : config.values()) {
			if (staticWorldMap.getAreaType() != WorldAreaType.MIDDLE_AREA) {
				continue;
			}

			mapIds.add(staticWorldMap.getMapId());
		}

		return mapIds;

	}

	// /解锁临近州城区域 （玩家不可迁移至州城）
	public List<MapStatus> checkMiddleMap(Player player) {
		int mapId = playerManager.getMapId(player);
		if (mapId == 0) {
			return new ArrayList<MapStatus>();
		}

		List<Integer> mapIds = getMiddleMap();
		List<MapStatus> updates = new ArrayList<MapStatus>();
		for (Integer elem : mapIds) {
			MapStatus mapStatus = player.findMapStatus(elem);
			if (mapStatus == null) {
				LogHelper.CONFIG_LOGGER.error("mapStatus is null, mapId = " + elem);
				continue;
			}

			if (mapStatus.getStatus() == 2) {
				continue;
			}

			mapStatus.setStatus(1);
			updates.add(mapStatus);
		}

		return updates;
	}

	public void openMiddleMap() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player != null) {
				List<MapStatus> mapStatuses = checkMiddleMap(player);
				if (!mapStatuses.isEmpty()) {
					player.updateMapStatuses(mapStatuses);
				}
				synWorldMapStatus(player, mapStatuses);
			}
		}
	}

	// 解锁临近州城区域 （玩家不可迁移至州城）
	public List<Integer> getAllMap() {
		List<Integer> mapIds = new ArrayList<Integer>();
		// 先找兄弟
		Map<Integer, StaticWorldMap> config = staticWorldMgr.getWorldMap();
		for (StaticWorldMap staticWorldMap : config.values()) {
			mapIds.add(staticWorldMap.getMapId());
		}

		return mapIds;

	}

	// /解锁临近州城区域 （玩家不可迁移至州城）
	public List<MapStatus> checkAllMap(Player player) {
		int mapId = playerManager.getMapId(player);
		if (mapId == 0) {
			return new ArrayList<MapStatus>();
		}
		List<Integer> mapIds = getAllMap();
		List<MapStatus> updates = new ArrayList<MapStatus>();
		for (Integer elem : mapIds) {
			MapStatus mapStatus = player.findMapStatus(elem);
			if (mapStatus == null) {
				LogHelper.CONFIG_LOGGER.error("mapStatus is null, mapId = " + elem);
				continue;
			}

			if (mapStatus.getStatus() == 2) {
				continue;
			}

			mapStatus.setStatus(2);
			updates.add(mapStatus);
		}

		return updates;
	}

	// 开启所有
	public void openAll() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player != null) {
				List<MapStatus> mapStatuses = checkAllMap(player);
				if (!mapStatuses.isEmpty()) {
					player.updateMapStatuses(mapStatuses);
				}
				synWorldMapStatus(player, mapStatuses);
			}
		}
	}

	public int getSeasonEffect() {
		WorldData worldData = getWolrdInfo();
		if (worldData == null) {
			return 0;
		}

		return worldData.getEffect();
	}

	public int getSeasonResType() {
		int effect = getSeasonEffect();
		StaticSeason staticSeason = staticWorldMgr.getSeason(effect);
		if (staticSeason == null) {
			return 0;
		}
		return staticSeason.getResourceType();
	}

	public float getSeasonResFactor() {
		int effect = getSeasonEffect();
		StaticSeason staticSeason = staticWorldMgr.getSeason(effect);
		return (float) staticSeason.getResource() / 100.0f;
	}

	// 募兵速度
	public double getSoldierEffect() {
		int effect = getSeasonEffect();
		StaticSeason staticSeason = staticWorldMgr.getSeason(effect);
		if (staticSeason == null) {
			// LogHelper.CONFIG_LOGGER.error("staticSeason is null!");
			return 0.0;
		}

		if (staticSeason.getType() != 5) {
			return 0.0;
		}

		return (double) staticSeason.getHireSoldier() / 100.0;
	}

	// 采集加速
	public double getResEffect(int type) {
		if (type == 5 || type == 0) {
			return 0.0;
		}

		int effect = getSeasonEffect();
		StaticSeason staticSeason = staticWorldMgr.getSeason(effect);
		if (staticSeason == null) {
			// LogHelper.CONFIG_LOGGER.error("staticSeason is null!");
			return 0.0;
		}

		if (staticSeason.getType() != type) {
			return 0.0;
		}
		// LogHelper.GAME_DEBUG.error("resEffect = " + staticSeason.getType() +
		// ", target type = " + type);
		return (double) staticSeason.getCollect() / 100.0;
	}

	// 计算物品保存个数
	public int getCityMaxAward(int country) {
		int itemNum = staticLimitMgr.getNum(50);
		WorldData data = worldData.get(1);
		if (data == null) {
			return itemNum;
		}

		WorldData worldData = getWolrdInfo();
		if (worldData == null) {
			LogHelper.CONFIG_LOGGER.error("worldData is null");
			return itemNum;
		}

		WorldBoss worldBoss = worldData.getWorldBoss(1);

		if (worldBoss == null) {
			return itemNum;
		}

		if (worldBoss.isKilled()) {
			itemNum += staticLimitMgr.getNum(51);
		}

		WorldBoss shareBoss = worldData.getShareBoss();
		if (shareBoss == null) {
			return itemNum;
		}

		if (shareBoss.isKilled()) {
			itemNum += staticLimitMgr.getNum(52);
		}

		return itemNum;
	}

	/**
	 * 同步entity, 通知所有看过老坐标和新坐标的人（玩家城池的坐标改变）
	 *
	 * @param entity
	 * @param mapId
	 * @param oldPos
	 */
	public void synEntityRq(Entity entity, int mapId, Pos oldPos) {
		if (entity == null) {
			return;
		}
		MapInfo mapInfo = worldMapInfo.get(mapId);
		if (mapInfo == null) {
			return;
		}
		CommonPb.WorldEntity worldEntityPb = entity.wrapPb().build();
		CommonPb.Pos posPB = oldPos.wrapPb();
		playerManager.getOnlinePlayer().forEach(player -> {
			playerManager.synEntityToPlayer(player, worldEntityPb, posPB);
		});
	}

	/**
	 * 地图资源的移除，同步entity,通知所有看过此坐标的人
	 *
	 * @param entity
	 * @param mapId
	 * @param oldPos
	 */
	public void synEntityRemove(Entity entity, int mapId, Pos oldPos) {
		if (entity == null) {
			return;
		}
		MapInfo mapInfo = worldMapInfo.get(mapId);
		if (mapInfo == null) {
			return;
		}
		CommonPb.Pos posPB = oldPos.wrapPb();
		playerManager.getOnlinePlayer().forEach(player -> {
			playerManager.synEntityToPlayer(player, null, posPB);
		});
	}

//    /**
//     * 地图上实体的增加，同步entity, 通知所有看过此坐标的人
//     *
//     * @param entity
//     */
//    public void synEntityAddRq(Entity entity, int mapId) {
//        if (entity == null) {
//            return;
//        }
//        MapInfo mapInfo = worldMapInfo.get(mapId);
//        if (mapInfo == null) {
//            return;
//        }
//        SpringUtil.getBean(LogicExecutor.class).add(() -> {
//            CommonPb.WorldEntity worldEntityPb = entity.wrapPb().build();
//            for (Player player : playerManager.getAllOnlinePlayer().values()) {
////                if (player == null || !player.getPushPos().containsKey(entity.getPos().toPosStr())) {
////                    continue;
////                }
//                playerManager.synEntityAddToPlayer(player, worldEntityPb);
//            }
//            return null;
//        });
//    }

	/**
	 * 地图上实体的增加，同步entity, 通知所有看过此坐标的人
	 *
	 * @param entity
	 */
	public void synEntityAddRq(List<Entity> entity) {
		if (entity == null || entity.isEmpty()) {
			return;
		}
		List<Entity> collect = entity.stream().collect(Collectors.toList());
		WorldPb.SynEntityAddRq.Builder builder = WorldPb.SynEntityAddRq.newBuilder();
		collect.forEach(x -> {
			if (x != null) {
				builder.addEntity(x.wrapPb());
			}
		});
		WorldPb.SynEntityAddRq msg = builder.build();

		for (Player player : playerManager.getOnlinePlayer()) {
			SynHelper.synMsgToPlayer(player, WorldPb.SynEntityAddRq.EXT_FIELD_NUMBER, WorldPb.SynEntityAddRq.ext, msg);
		}
	}

	// 通知国家的
	public void SynPlayerCityCallRq(Player target, Entity entity, int mapId) {
		if (entity == null) {
			return;
		}

		MapInfo mapInfo = worldMapInfo.get(mapId);
		if (mapInfo == null) {
			return;
		}

//        WorldEntity worldEntity = new WorldEntity();
//        worldEntity.setEntityType(entity.getEntityType());
//        worldEntity.setId(entity.getId());
//        worldEntity.setLevel(entity.getLevel());
//        worldEntity.setPos(entity.getPos());
//        worldEntity.setEntityType(entity.getEntityType());
//        if (entity instanceof PlayerCity) {
//            PlayerCity playerCity = (PlayerCity) entity;
//            worldEntity.setName(playerCity.getName());
//            worldEntity.setCountry(playerCity.getCountry());
//            worldEntity.setCallCount(playerCity.getCallCount());
//            worldEntity.setCallReply(playerCity.getCallReply());
//            worldEntity.setCallEndTime(playerCity.getCallEndTime());
//            worldEntity.setProtectedTime(target.getProectedTime());
//        }

		CommonPb.WorldEntity entityPb = entity.wrapPb().build();

		SynPlayerCityCallRq.Builder builder = SynPlayerCityCallRq.newBuilder();
		builder.setEntity(entityPb);
		SynPlayerCityCallRq cityCallRq = builder.build();

		for (Player player : playerManager.getOnlinePlayer()) {
			if (player == null) {
				continue;
			}

			if (player.getCountry() != target.getCountry()) {
				continue;
			}

			if (player.isLogin && player.getChannelId() != -1) {
				SynHelper.synMsgToPlayer(player, SynPlayerCityCallRq.EXT_FIELD_NUMBER, SynPlayerCityCallRq.ext, cityCallRq);
			}
		}

	}

	// 通知单个玩家城池信息
	public void SynPlayerCityCallSingleRq(Entity entity, int mapId, Player player) {
		if (entity == null) {
			return;
		}
		MapInfo mapInfo = worldMapInfo.get(mapId);
		if (mapInfo == null) {
			return;
		}
//        WorldEntity worldEntity = new WorldEntity();
//        worldEntity.setEntityType(entity.getEntityType());
//        worldEntity.setId(entity.getId());
//        worldEntity.setLevel(entity.getLevel());
//        worldEntity.setPos(entity.getPos());
//        worldEntity.setEntityType(entity.getEntityType());
//        if (entity instanceof PlayerCity) {
//            PlayerCity playerCity = (PlayerCity) entity;
//            worldEntity.setName(playerCity.getName());
//            worldEntity.setCountry(playerCity.getCountry());
//            worldEntity.setCallCount(playerCity.getCallCount());
//            worldEntity.setCallReply(playerCity.getCallReply());
//            worldEntity.setCallEndTime(playerCity.getCallEndTime());
//            if (player != null) {
//                worldEntity.setSkin(player.getLord().getSkin());
//            }
//        }
		CommonPb.WorldEntity entityPb = entity.wrapPb().build();
		SynPlayerCityCallRq.Builder builder = SynPlayerCityCallRq.newBuilder();
		builder.setEntity(entityPb);
		SynPlayerCityCallRq cityCallRq = builder.build();
		if (player.isLogin && player.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(player, SynPlayerCityCallRq.EXT_FIELD_NUMBER, SynPlayerCityCallRq.ext, cityCallRq);
		}
	}

	/**
	 * 加载野怪和资源点
	 *
	 * @param worldMap
	 * @param mapInfo
	 * @throws Exception
	 */
	private void readWorldData(WorldMap worldMap, MapInfo mapInfo) throws Exception {
		if (worldMap.getMapData() == null) {
			return;
		}

		SerMapInfo serMapInfo = null;

		serMapInfo = SerMapInfo.parseFrom(worldMap.getMapData());

		if (serMapInfo == null) {
			logger.error("serMapInfo is null");
			return;
		}

		// LogHelper.GAME_DEBUG.error("1.monster size = " +
		// serMapInfo.getMonsterList().size());
		for (DataPb.MonsterData monsterData : serMapInfo.getMonsterList()) {
			if (monsterData == null) {
				continue;
			}
			StaticWorldMonster worldMonster = staticWorldMgr.getMonster(monsterData.getId());
			if (worldMonster == null) {
				continue;
			}

			// 暂时不加载国家英雄
			if (staticCountryMgr.isCountryMonster(worldMonster.getId()) && staticLimitMgr.isCloseCtyHero()) {
				continue;
			}

			DataPb.PosData pbPos = monsterData.getPos();
			Pos pos = new Pos(pbPos.getX(), pbPos.getY());
			StaticWorldCity worldCity = staticWorldMgr.getCity(CityId.WORLD_CITY_ID);
			if (worldCity != null) {
				int x1 = worldCity.getX1();
				int x2 = worldCity.getX2();
				int y1 = worldCity.getY1();
				int y2 = worldCity.getY2();
				if (pos.getX() >= x1 && pos.getX() <= x2 && pos.getY() >= y1 && pos.getY() <= y2) {
					continue;
				}
			}
			if (!mapInfo.isFreePos(pos)) {
				pos = mapInfo.randPickPos();
			}

			if (worldMonster.getType() == WorldMonsterType.HUANGJINJUN || worldMonster.getType() == WorldMonsterType.XILIANGJUN) {
				continue;
			}

			switch (monsterData.getEntityType()) {
				case EntityType.Monster:
					addMonster(pos, worldMonster.getId(), monsterData.getLevel(), mapInfo, AddMonsterReason.LOAD_MONSTER);
					break;
				case EntityType.RIOT_MONSTER:
					addRiotMonster(pos, worldMonster.getId(), monsterData.getLevel(), mapInfo, AddMonsterReason.LOAD_MONSTER);
					break;
				case EntityType.BIG_MONSTER:
					BigMonster bigMonster = new BigMonster();
					bigMonster.unWriteData(monsterData);
					int total = 0;
					for (BattleEntity entity : bigMonster.getTeam().getAllEnities()) {
						StaticMonster ts = staticMonsterMgr.getStaticMonster(entity.getEntityId());
						if (ts != null) {
							total += ts.getSoldierCount();
						}
					}
					bigMonster.setTotalHp(total);
					// 已经死亡的
					if (bigMonster.getState() == EntityState.DEATH.get()) {
						mapInfo.getDeathMonsterMap().add(bigMonster);
					} else {
						boolean flag = mapInfo.addPos(pos, bigMonster);
						Map<Pos, BigMonster> bigMonsterMap = mapInfo.getBigMonsterMap();
						playerManager.clearPos(pos);
						bigMonster.setPos(pos);
						bigMonsterMap.put(pos, bigMonster);
					}
					break;
			}
		}
		// 初始化地图行军数据
		initMarch(serMapInfo, mapInfo);
		initResource(serMapInfo, mapInfo);
		for (CommonPb.FirstBloodMapInfo info : serMapInfo.getFirstBloodList()) {
			CityFirstBloodInfo firstBloodInfo = new CityFirstBloodInfo();
			firstBloodInfo.readData(info);
			if (firstBloodInfo != null) {
				mapInfo.getCityFirstBlood().put(firstBloodInfo.getCityType(), firstBloodInfo);
			}
		}

		// 城池Id记录
		Map<Integer, HashSet<Integer>> cityRecordInfo = mapInfo.getCityIdRecord();
		for (int i = 0; i < serMapInfo.getCtCityIdRecordCount(); i++) {
			DataPb.CtCityIdRecord cityRecord = serMapInfo.getCtCityIdRecord(i);
			if (cityRecord == null) {
				continue;
			}

			HashSet<Integer> countryCity = cityRecordInfo.get(cityRecord.getCountry());
			if (countryCity == null) {
				countryCity = new HashSet<Integer>();
				cityRecordInfo.put(cityRecord.getCountry(), countryCity);
			}

			if (cityRecord.getCityIdCount() > 0) {
				countryCity.addAll(cityRecord.getCityIdList());
			}
		}

		initSuperResource(serMapInfo, mapInfo);

		bigMonsterManager.flushMonster(mapInfo);
	}

	/**
	 * 初始化行军数据
	 *
	 * @param mapInfo
	 */
	public void initMarch(SerMapInfo serMapInfo, MapInfo mapInfo) {
		// 初始化地图行军数据
		List<DataPb.MarchData> marchDataList = serMapInfo.getMarchDataList();
		marchDataList.forEach(marchData -> {
			March march = new March();
			march.readMarch(marchData);
			mapInfo.addMarch(march);
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				player.addMarch(march);
			}
		});
		marchManager.removeUseMarch(mapInfo.getMarches());
	}

	/**
	 * 初始化普通资源
	 *
	 * @param mapInfo
	 */
	public void initResource(SerMapInfo serMapInfo, MapInfo mapInfo) {
		Map<Pos, Resource> resourceMap = mapInfo.getResourceMap();
		for (DataPb.ResourceData resourceData : serMapInfo.getResourceList()) {
			Resource resource = new Resource();
			resource.readData(resourceData);
			Pos oldPos = resource.getPos();
			if (!mapInfo.isFreePos(oldPos)) {
				if (resource.getPlayer() != null) {
					Pos pos = mapInfo.randPickPos();
					resource.setPos(pos);
					Player player = resource.getPlayer();
					ConcurrentLinkedDeque<March> marchList = player.getMarchList();
					Iterator<March> iterator = marchList.iterator();
					while (iterator.hasNext()) {
						March next = iterator.next();
						if (next.equals(oldPos)) {
							next.setEndPos(pos);
						}
					}
				} else {
					continue;
				}
			}
			StaticWorldResource staticWorldResource = staticWorldMgr.getStaticWorldResource((int) resource.getId());
			if (staticWorldResource != null) {
				resource.setType(staticWorldResource.getType());
			}
			//Pos pos = resource.getPos();
			//Entity entity = mapInfo.getEntity(pos);
			//if (entity != null) {
			//    Pos pos1 = mapInfo.randPickPos();
			//    resource.setPos(pos1);
			//}
			boolean flag = mapInfo.addPos(resource.getPos(), resource);
			if (flag) {// 添加成功
				resourceMap.put(resource.getPos(), resource);
				playerManager.clearPos(resource.getPos());
			}
		}
	}

	/**
	 * 初始化大型矿点
	 *
	 * @param mapInfo
	 */
	@Autowired
	StaticSuperResMgr staticSuperResMgr;

	public void initSuperResource(SerMapInfo serMapInfo, MapInfo mapInfo) {
		// 初始化大型矿点信息
		List<CommonPb.SuperMine> superMineList = serMapInfo.getSuperMineList();
		superMineList.forEach(superMine -> {
			StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(superMine.getConfigId());
			if (staticSuperRes != null) {
				SuperResource resource = new SuperResource(staticSuperRes.getResId());
				resource.setResType(staticSuperRes.getResType());

				resource.setCountry(superMine.getCountry());
				CommonPb.Pos pos = superMine.getPos();
				Pos pos1 = new Pos(pos.getX(), pos.getY());
				if (pos != null) {
					if (!mapInfo.isFreePos(pos1)) {
						pos1 = mapInfo.randPickPos();
					}
					resource.setPos(pos1);
				}
				resource.setState(superMine.getState());
				resource.setResId(superMine.getConfigId());
				resource.setConvertRes(superMine.getConvertRes());
				resource.setCapacity(superMine.getCapacity());
				resource.setCityId(superMine.getCityId());
				resource.setNextTime(superMine.getNextTime());
				resource.setEntityType(EntityType.BIG_RESOURCE);
				List<CommonPb.SuperGuard> collectArmyList = superMine.getCollectArmyList();
				if (!collectArmyList.isEmpty()) {
					for (CommonPb.SuperGuard superGuard : collectArmyList) {
						SuperGuard guard = new SuperGuard();
						March march = mapInfo.getMarch(superGuard.getMarchId());
						if (march == null) {
							continue;
						}
						guard.setMarch(march);
						guard.setStartTime(superGuard.getStartTime());
						guard.setMaxCollectTime(superGuard.getMaxCollectTime());
						guard.setCollectTime(superGuard.getCollectTime());
						guard.setCanMaxCollectTime(superGuard.getCanMaxCollectTime());
						guard.setArmyArriveTime(superGuard.getArmyArriveTime());
						guard.setSuperMine(resource);
						resource.getCollectArmy().add(guard);
					}
				}

				List<Integer> helpArmyList = superMine.getHelpArmyList();
				if (helpArmyList != null && !helpArmyList.isEmpty()) {
					helpArmyList.forEach(marchId -> {
						March march = mapInfo.getMarch(marchId);
						if (march != null) {
							resource.getHelpArmy().add(march);
						}
					});
				}
				List<SuperResource> superResources = mapInfo.getSuperResMap().computeIfAbsent(resource.getCountry(), x -> new ArrayList<>());
				superResources.add(resource);
				if (resource.getState() == SuperResource.STATE_PRODUCED || resource.getState() == SuperResource.STATE_STOP) {
					addSuperResource(mapInfo, resource.getPos(), resource);
				}
			}
		});
	}

	public void updateWorldMap(WorldMap worldMap) {
		try {
			worldMapDao.updateWorldMap(worldMap);
		} catch (Exception ex) {
			logger.error("updateWorldMap error {}", ex);
		}

	}

	public void removeQuickWar(int mapId, long warId) {
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			logger.error("mapInfo is null!");
			return;
		}
		IWar war = mapInfo.getWarMap().remove(warId);
		worldManager.flushWar((WarInfo) war, false, war.getAttacker().getCountry());
	}

	// 删除防守方式我方的战斗
	public List<CommonPb.CityWarInfo> crateAllWar(long lordId, MapInfo mapInfo, Player player) {
		List<CommonPb.CityWarInfo> infos = new ArrayList<CommonPb.CityWarInfo>();
		Player target = playerManager.getPlayer(lordId);
		if (target == null) {
			return infos;
		}

		// 远征和奔袭
		List<IWar> cityWars = mapInfo.getWarMap().values().stream().filter(e -> e.getWarType() == WarType.Attack_WARFARE || e.getWarType() == WarType.ATTACK_FAR || e.getWarType() == WarType.ATTACK_QUICK).collect(Collectors.toList());
		for (IWar war : cityWars) {
			WarInfo warInfo = (WarInfo) war;
			if (warInfo.getDefencer().getId() == lordId) {
				CommonPb.CityWarInfo builder = createWarInfo(warInfo, player);
				infos.add(builder);
			}
		}
		Map<Long, WarInfo> riotWar = target.getSimpleData().getRiotWar();
		for (WarInfo warInfo : riotWar.values()) {
			if (warInfo.getDefencerId() == lordId) {
				handleWarSoldier(warInfo);
				CommonPb.CityWarInfo builder = createWarInfo(warInfo, player);
				infos.add(builder);
			}
		}
		// 虫族主宰
		MapInfo centerMap = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		if (centerMap != null && player != null) {
			centerMap.getWarMap().values().stream().filter(e -> {
				if (e.getWarType() == WarType.ATTACK_ZERG) {
					return true;
				}
				if (e.getWarType() == WarType.DEFEND_ZERG && e.getDefencer().getCountry() == player.getCountry()) {
					return true;
				}
				return false;
			}).forEach(e -> {
				ZergWarInfo warInfo = (ZergWarInfo) e;
				handleZergWarSoldier(warInfo);
				CommonPb.CityWarInfo builder = createWarInfo(warInfo, player);
				infos.add(builder);
			});
		}
		return infos;
	}

	/**
	 * 移出战斗PB
	 *
	 * @param warInfo
	 * @return
	 */
	public WorldPb.SynCityWarRq createSynCityWar(WarInfo warInfo) {
		CommonPb.CityWarInfo warInfoPb = worldManager.createWarInfo(warInfo, null);
		WorldPb.SynCityWarRq.Builder SynCityWarBuilder = WorldPb.SynCityWarRq.newBuilder();
		SynCityWarBuilder.addRemoveWarInfo(warInfoPb);
		return SynCityWarBuilder.build();
	}

	public CommonPb.CityWarInfo createWarInfo(WarInfo warInfo, Player player) {
		CommonPb.CityWarInfo.Builder builder = CommonPb.CityWarInfo.newBuilder();
		builder.setIsIn(warInfo.isJoin(player));
		builder.setWarId(warInfo.getWarId());
		CommonPb.WarAttender.Builder attacker = CommonPb.WarAttender.newBuilder();
		long attackerId = warInfo.getAttackerId();
		Player attackPlayer = playerManager.getPlayer(attackerId);
		worldManager.handleWarSoldier(warInfo);
		if (attackPlayer != null) {
			Pos playerPos = attackPlayer.getPos();
			attacker.setLordId(warInfo.getAttackerId());
			attacker.setPos(playerPos.wrapPb());
			attacker.setName(attackPlayer.getNick());
			attacker.setSoldierNum(warInfo.getAttackSoldierNum());
			attacker.setCityLevel(attackPlayer.getBuildingLv(BuildingType.COMMAND));
			attacker.setPortrait(attackPlayer.getLord().getPortrait());
			attacker.setHeadImg(attackPlayer.getLord().getHeadIndex());
			attacker.setTitle(attackPlayer.getLord().getTitle());
			CtyGovern govern = countryManager.getGovern(attackPlayer);
			if (govern != null) {
				attacker.setPost(govern.getGovernId());
			}
		}
		if (warInfo.getWarType() == WarType.RIOT_WAR) { // 虫族入侵
			StaticWorldMonster worldMonster = staticWorldMgr.getMonster((int) warInfo.getAttackerId());
			attacker.setLordId(warInfo.getAttackerId());
			attacker.setPos(warInfo.getAttackerPos().wrapPb());
			attacker.setName(worldMonster.getName());

			attacker.setSoldierNum(getRiotNum(warInfo));
		}
		if (warInfo.getWarType() == WarType.DEFEND_ZERG) {// 虫族主宰
			worldManager.handleZergWarSoldier(warInfo);
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster((int) warInfo.getAttackerId());
			attacker.setLordId(warInfo.getAttackerId());
			attacker.setPos(warInfo.getAttackerPos().wrapPb());
			attacker.setName(staticMonster.getName());
			attacker.setSoldierNum(warInfo.getAttackSoldierNum());
		}

		long defenceId = warInfo.getDefencerId();
		CommonPb.WarAttender.Builder defencer = CommonPb.WarAttender.newBuilder();
		Player defencerPlayer = playerManager.getPlayer(defenceId);
		if (defencerPlayer != null) {
			Pos defencePos = defencerPlayer.getPos();
			defencer.setLordId(warInfo.getDefencerId());
			defencer.setPos(defencePos.wrapPb());
			defencer.setName(defencerPlayer.getNick());
			List<Integer> heroIds = defencerPlayer.getEmbattleList();
			defencer.setSoldierNum(warInfo.getDefenceSoldierNum());
			defencer.setCityLevel(defencerPlayer.getBuildingLv(BuildingType.COMMAND));
			defencer.setPortrait(defencerPlayer.getLord().getPortrait());
			defencer.setHeadImg(defencerPlayer.getLord().getHeadIndex());
			defencer.setTitle(defencerPlayer.getLord().getTitle());
			CtyGovern govern = countryManager.getGovern(defencerPlayer);
			if (govern != null) {
				defencer.setPost(govern.getGovernId());
			}
		}

		builder.setAttacker(attacker);
		builder.setDefencer(defencer);
		builder.setEndTime(warInfo.getEndTime());
		builder.setHelpTime(warInfo.getAttackerHelpTime());
		builder.setDefencerHelpTime(warInfo.getDefencerHelpTime());
		int marchType = 0;
		int warType = warInfo.getWarType();
		if (warType == WarType.Attack_WARFARE || warType == WarType.ATTACK_FAR) {
			marchType = MarchType.AttackCityFar;
		} else if (warType == WarType.ATTACK_QUICK) {
			marchType = MarchType.AttackCityQuick;
		} else if (warType == WarType.ATTACK_COUNTRY) {
			marchType = MarchType.CountryWar;
			CountryCityWarInfo countryCityWarInfo = (CountryCityWarInfo) warInfo;
			builder.setCityId(countryCityWarInfo.getCityId());
		} else if (warType == WarType.RIOT_WAR) {
			marchType = MarchType.RiotWar;
		} else if (warType == WarType.ATTACK_ZERG || warType == WarType.DEFEND_ZERG) {
			marchType = MarchType.ZERG_WAR;
		} else {
			LogHelper.CONFIG_LOGGER.error("march type error!");
		}
		builder.setWarType(warInfo.getWarType());
		builder.setMarchType(marchType);
		if (attackPlayer != null) {
			builder.setAttackCountry(attackPlayer.getCountry());
		}

		if (defencerPlayer != null) {
			builder.setDefenceCountry(defencerPlayer.getCountry());
		}

		builder.setKeyId(warInfo.getWarId());
		return builder.build();

	}

	// 其他玩家攻击target玩家时加入
	public void synAddCityWar(Player target, WarInfo warInfo) {
		if (target == null) {
			LogHelper.CONFIG_LOGGER.error("player is not null!");
			return;
		}

		if (target.isLogin && target.getChannelId() != -1 && warInfo != null) {
			WorldPb.SynCityWarRq.Builder builder = WorldPb.SynCityWarRq.newBuilder();
			builder.addAddWarInfo(createWarInfo(warInfo, null));
			SynHelper.synMsgToPlayer(target, WorldPb.SynCityWarRq.EXT_FIELD_NUMBER, WorldPb.SynCityWarRq.ext, builder.build());
		}
	}

	// 城战结束
	public void synRemoveWar(Player target, WorldPb.SynCityWarRq msg) {
		if (target == null) {
			LogHelper.CONFIG_LOGGER.error("player is not null!");
			return;
		}

		int mapId = getMapId(target);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
			return;
		}

		if (target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, WorldPb.SynCityWarRq.EXT_FIELD_NUMBER, WorldPb.SynCityWarRq.ext, msg);
		}
	}

	// 国战结束
	public void synRemoveWar(Player target, WarInfo warInfo, int cityId) {
		if (target == null) {
			LogHelper.CONFIG_LOGGER.error("player is not null!");
			return;
		}

		int mapId = getMapId(target);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
			return;
		}
		CountryCityWarInfo countryCityWarInfo = (CountryCityWarInfo) warInfo;
		countryCityWarInfo.setCityId(cityId);
		CommonPb.CityWarInfo warInfoPb = createWarInfo(warInfo, null);
		WorldPb.SynCityWarRq.Builder builder = WorldPb.SynCityWarRq.newBuilder();
		builder.addRemoveWarInfo(warInfoPb);
		if (target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, WorldPb.SynCityWarRq.EXT_FIELD_NUMBER, WorldPb.SynCityWarRq.ext, builder.build());
		}
	}

	// 删除所有战斗
	public void synRemoveAllPvpWar(Player target, MapInfo beforeMap) {
		if (target == null) {
			LogHelper.CONFIG_LOGGER.error("player is not null!");
			return;
		}

		if (beforeMap == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
			return;
		}

		List<CommonPb.CityWarInfo> allWarInfo = crateAllWar(target.roleId, beforeMap, null);
		allWarInfo = allWarInfo.parallelStream().filter(e -> e.getWarType() != WarType.RIOT_WAR).collect(Collectors.toList());
		WorldPb.SynCityWarRq.Builder builder = WorldPb.SynCityWarRq.newBuilder();
		if (!allWarInfo.isEmpty()) {
			builder.addAllRemoveWarInfo(allWarInfo);
		}

		if (target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, WorldPb.SynCityWarRq.EXT_FIELD_NUMBER, WorldPb.SynCityWarRq.ext, builder.build());
		}

	}

	public long checkPeriod(March march, long old) {
		long period = old;
		int targetMapId = getMapId(march.getEndPos());
		int startMapId = getMapId(march.getStartPos());
		// 是否在一个区域
		if (targetMapId == MapId.CENTER_MAP_ID && targetMapId != startMapId) { // 不是皇城
			MapInfo mapInfo = getMapInfo(startMapId);
			WarInfo warInfo = getPvpWarInfo(mapInfo, march.getWarId());
			if (warInfo != null && march.getLordId() == warInfo.getAttackerId()) {
				if (warInfo.getWarType() == WarType.ATTACK_QUICK) {
					period = staticLimitMgr.getNum(104) * TimeHelper.SECOND_MS;
				} else if (warInfo.getWarType() == WarType.Attack_WARFARE) {
					period = staticLimitMgr.getNum(105) * TimeHelper.SECOND_MS;
				} else if (warInfo.getWarType() == WarType.ATTACK_FAR) {
					period = staticLimitMgr.getNum(106) * TimeHelper.SECOND_MS;
				}
			}
		}

		return period;
	}

	// 中途返回
	public void handleMiddleReturn(March march, int reason) {
		if (march.getState() == MarchState.Begin) {
			march.setState(MarchState.FightOver);
			march.swapPos(reason);
			long leftTime = march.getEndTime() - System.currentTimeMillis();
			leftTime = Math.max(0, leftTime);
			long marchTime = march.getPeriod() - leftTime;
			march.setEndTime(System.currentTimeMillis() + marchTime);
		} else if (march.getState() == MarchState.Waiting || march.getState() == MarchState.Fighting || march.getState() == MarchState.Collect || march.getState() == MarchState.CityAssist) {
			march.setState(MarchState.FightOver);
			march.swapPos(reason);
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				// 兵书对行军影响
				List<Integer> heroIds = march.getHeroIds();
				float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);
				long period = getPeriod(player, march.getEndPos(), march.getStartPos(), bookEffectMarch);
				Pos startPos = march.getStartPos();
				Pos endPos = march.getEndPos();
				int mapId = getMapId(endPos);
				if (mapId == MapId.FIRE_MAP) {
					period = flameWarManager.getPeriod(player, startPos, endPos, bookEffectMarch);
				}
				march.setPeriod(period);
				march.setEndTime(System.currentTimeMillis() + period);
			} else {
				LogHelper.CONFIG_LOGGER.error("player is null!");
			}
		}
	}

	/**
	 * 中途返回
	 *
	 * @param march
	 * @param reason
	 */
	public void doMiddleReturn(March march, int reason) {
		handleMiddleReturn(march, reason);
		synMarch(0, march);
	}

	public boolean isEscape(int reason) {
		return reason == MarchReason.LowMove || reason == MarchReason.MiddleMove || reason == MarchReason.HighMove || reason == MarchReason.Reply;
	}

	/**
	 * 此方法有问题 需要改造 正常所有行军应该通知到有拉取到行军信息的用户
	 *
	 * @param march
	 * @return
	 */
	public HashSet<Long> getMarchPlayers(March march) {
		// end map
		int mapId = getMapId(march.getEndPos());
		MapInfo mapInfo = getMapInfo(mapId);
		HashSet<Long> players = new HashSet<Long>();
		if (mapInfo != null) {
			WarInfo warInfo = getPvpWarInfo(mapInfo, march.getWarId());
			if (warInfo != null) {
				players = warInfo.getPlayers();
			}
		}

		// start map
		int startMapId = getMapId(march.getStartPos());
		HashSet<Long> startPlayers = new HashSet<Long>();
		if (startMapId != mapId) {
			MapInfo startMapInfo = getMapInfo(startMapId);
			if (startMapInfo != null) {
				WarInfo warInfo = getPvpWarInfo(startMapInfo, march.getWarId());
				if (warInfo != null) {
					startPlayers = warInfo.getPlayers();
				}
			}
		}

		if (!startPlayers.isEmpty()) {
			players.addAll(startPlayers);
		}

		long lordId = march.getLordId();
		if (!players.isEmpty() && players.contains(lordId)) {
			players.remove(lordId);
		}

		// 找到不在同一个地图上的人
		HashSet<Long> dealRes = new HashSet<Long>();
		for (Long playerId : players) {
			Player player = playerManager.getPlayer(playerId);
			if (player == null) {
				continue;
			}
			int playerMapId = getMapId(player);
			if (playerMapId != mapId) {
				dealRes.add(playerId);
			}
		}

		return dealRes;
	}

	public void synMarchToPlayer(HashSet<Long> players, March march) {
		for (Long lordId : players) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			synMarchToPlayer(player, march);
		}
	}


	public boolean isPlayerFly(int reason) {
		return reason == MarchReason.FarPlayerFly || reason == MarchReason.QuickPlayerFly;
	}

	// A迁城
	// 攻击A的所有队伍取消战斗, 全部回城
	// 通知A删除所有战斗
	// 删除所有战斗
	// 通知实体
	public void removePlayerWar(Player player, Pos oldPos, int reason, Pos newPos) {
		int mapId = getMapId(player);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			return;
		}

		// 删除实体
		PlayerCity playerCity = mapInfo.getPlayerCity(player.getPos());
		if (playerCity != null) {
			if (player.getLord().getMapId() == MapId.FIRE_MAP) {
				flameWarService.synFlameEntity(playerCity, oldPos);
			} else {
				synEntityRq(playerCity, mapInfo.getMapId(), oldPos); // 同步城池
			}
		}

		// 虫族入侵
		WarInfo riotWar = player.getSimpleData().getRiotWarInfo();
		if (riotWar != null) {
			// 通知所有部队遣返
			marchManager.doWarMarchReturn(player, riotWar, reason);
			riotWar.getDefenceMarches().clear();
			March march = player.getSimpleData().getRiotMarchs();
			if (march != null) {
				march.setStartPos(newPos);
				march.setEndPos(newPos);
				synMarch(mapId, march);
			}
		}

		// 虫族主宰
		MapInfo centerMap = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		if (centerMap != null && player != null) {
			centerMap.getWarMap().values().stream().filter(e -> e.getDefencer().getId() == player.getRoleId()).forEach(e -> {
				if (e instanceof ZergWarInfo) {
					ZergWarInfo warInfo = (ZergWarInfo) e;
					March march = warInfo.getAttackMarches().getFirst();
					if (march != null) {
						march.setStartPos(newPos);
						march.setEndPos(newPos);
						synMarch(march, player.getCountry());
					}
				}
			});
		}
	}


	/**
	 * 通知该地图被击飞玩家的其他战斗人员全部返回
	 *
	 * @param player
	 * @param currentWarId
	 * @param reason
	 */
	public void callDefecneOtherMarchReturn(Player player, long currentWarId, int reason) {
		int mapId = getMapId(player);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			return;
		}

		Iterator<IWar> cityWarIterator = mapInfo.getWarList(e -> e.getWarType() == WarType.ATTACK_FAR || e.getWarType() == WarType.Attack_WARFARE || e.getWarType() == WarType.ATTACK_QUICK).iterator();
		while (cityWarIterator.hasNext()) {
			WarInfo warInfo = (WarInfo) cityWarIterator.next();
			if (warInfo == null) {
				continue;
			}

			if (warInfo.getWarId() == currentWarId) {
				continue;
			}

			if (warInfo.getDefencerId() == player.roleId) {
				// 通知所有部队遣返
				marchManager.doWarMarchReturn(player, warInfo, reason);
				worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
				// 最后删除战斗
				mapInfo.getWarMap().remove(warInfo);

				WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
				worldManager.synRemoveWar(player, synCityWarRq);
			}

		}
	}

	// 获取国战信息
	public List<IWar> getCtWar(int mapId, int cityId) {
		List<IWar> warInfos = new ArrayList<>();
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			return warInfos;
		}
		return mapInfo.getCountryCityWar(cityId);
	}

	public IWar getCountryWar(int cityId, int country, MapInfo mapInfo) {
		City city = cityManager.getCity(cityId);
		if (city == null) {
			LogHelper.CONFIG_LOGGER.error("city is null!");
			return null;
		}

		Optional<IWar> optional = mapInfo.getWarMap().values().stream().filter(e -> {
			if (e instanceof CountryCityWarInfo) {
				CountryCityWarInfo warInfo = (CountryCityWarInfo) e;
				if (warInfo.getAttacker().getCountry() == country && warInfo.getCityId() == cityId) {
					return true;
				}
			}
			return false;
		}).findFirst();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	/**
	 * 拿到伏击叛军war信息
	 *
	 * @param country
	 * @param mapInfo
	 * @param monsterId
	 * @return
	 */
	public WarInfo getRebelWar(int country, MapInfo mapInfo, long monsterId, Pos pos) {
		Map<Long, WarInfo> countryWarMap = mapInfo.getRebelWarMap();
		for (WarInfo warInfo : countryWarMap.values()) {
			if (warInfo.getDefencerId() == monsterId && warInfo.getAttackerCountry() == country && warInfo.getDefencerPos().equals(pos)) {
				return warInfo;
			}
		}
		return null;

	}

	public void handPlayerLost(Player player, List<Award> robeAward) {
		int day = GameServer.getInstance().currentDay;
		Lord lord = player.getLord();
		if (day != lord.getWareBuildDay()) {
			lord.setWareBuildDay(day);
			lord.setWareTimes(0);
		}

		if (day != lord.getFlyDay()) {
			lord.setFlyDay(day);
			lord.setFlyTimes(0);
			player.clearLostRes();
		}

		long iron = 0;
		long copper = 0;
		long oil = 0;

		for (Award award : robeAward) {
			if (award.getType() != AwardType.RESOURCE) {
				continue;
			}

			if (award.getId() == ResourceType.IRON) {
				iron = award.getCount();
			} else if (award.getId() == ResourceType.COPPER) {
				copper = award.getCount();
			} else if (award.getId() == ResourceType.OIL) {
				oil = award.getCount();
			}
		}

		Map<Integer, Long> allRes = player.getAllRes();
		Long curIron = allRes.get(ResourceType.IRON);
		Long curCopper = allRes.get(ResourceType.COPPER);
		Long curOil = allRes.get(ResourceType.OIL);

		float ironRatio = (float) iron / (float) curIron;
		float copperRatio = (float) copper / (float) curCopper;
		float oilRatio = (float) oil / (float) curOil;
		long checkIronNum = (long) (ironRatio * (float) curIron);
		long checkCopperNum = (long) (copperRatio * (float) curCopper);
		long checkOilNum = (long) (oilRatio * (float) curOil);

		// 如果有高级, 则资源100%返回,如果返回的资源总数小于100k*3，则每种资源100k返回
		// 如果重建家园, 则资源30%返回,如果返回的资源总数小于10k*3，则每种资源10k返回
		int highWareTimes = playerManager.getHighWareLeftTimes(player);
		int rebuildWareTimes = playerManager.getRebuildWareLeftTimes(player);
		long highLimit = staticLimitMgr.getNum(61);
		long lowLimit = staticLimitMgr.getNum(62);
		long highProtected = staticLimitMgr.getNum(59) * TimeHelper.HOUR_MS;
		long lowProtected = staticLimitMgr.getNum(60) * TimeHelper.HOUR_MS;
		float highReturn = (float) staticLimitMgr.getNum(129) / 100.0f;
		float lowReturn = (float) staticLimitMgr.getNum(130) / 100.0f;

		long now = System.currentTimeMillis();
		if (lord.getProtectedTime() <= now) {
			lord.setProtectedTime(now);
		}
		if (highWareTimes > 0) {
			if (checkIronNum < highLimit) {
				iron = highLimit;
			} else {
				iron = (int) ((float) iron * highReturn);
			}

			if (checkCopperNum < highLimit) {
				copper = highLimit;
			} else {
				copper = (int) ((float) copper * highReturn);
			}

			if (checkOilNum < highLimit) {
				oil = highLimit;
			} else {
				oil = (int) ((float) oil * highReturn);
			}

			lord.setWareHighTimes(lord.getWareHighTimes() + 1); // 已经花费的高级重建次数
			lord.setProtectedTime(lord.getProtectedTime() + highProtected);

		} else if (rebuildWareTimes > 0) {
			if (checkIronNum < lowLimit) {
				iron = lowLimit;
			} else {
				iron = (int) ((float) iron * lowReturn);
			}

			if (checkCopperNum < lowLimit) {
				copper = lowLimit;
			} else {
				copper = (int) ((float) copper * lowReturn);
			}

			if (checkOilNum < lowLimit) {
				oil = lowLimit;
			} else {
				oil = (int) ((float) oil * lowReturn);
			}

			lord.setWareTimes(lord.getWareTimes() + 1);
			lord.setProtectedTime(lord.getProtectedTime() + lowProtected);
		} else {
			iron = 0L;
			copper = 0L;
			oil = 0L;
			lord.setProtectedTime(lord.getProtectedTime() + lowProtected);
		}

		if (iron == 0L && copper == 0L && oil == 0L) {

		} else {
			LostRes lostRes = new LostRes(iron, copper, oil);
			player.addLostRes(lostRes);
		}
		//LogUser logUser = SpringUtil.getBean(LogUser.class);
		for (Award award : robeAward) {
			playerManager.subAward(player, award, Reason.ATTACK_CITY); // synchange
			if (award.getType() == AwardType.RESOURCE) { // 记录下日志
				logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(award.getId()), RoleResourceLog.OPERATE_OUT, award.getId(), ResOperateType.BUILDING_UP_OUT.getInfoType(), award.getCount(), player.account.getChannel()));
				int t = 0;
				switch (award.getId()) {
					case ResourceType.IRON:
						t = IronOperateType.ROB_OUT.getInfoType();
						break;
					case ResourceType.COPPER:
						t = CopperOperateType.ROB_OUT.getInfoType();
						break;
					case ResourceType.OIL:
						t = OilOperateType.ROB_OUT.getInfoType();
						break;
					default:
						break;
				}
				if (t != 0) {
					logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), RoleResourceLog.OPERATE_OUT, award.getCount(), t), award.getId());
				}
			}
		}

		// 被击飞次数加1
		lord.setFlyTimes(lord.getFlyTimes() + 1);
		playerManager.synWareTimes(player);
		addProtectTime(player);
		// 同步保护时间
		playerManager.synProtectedTime(player);
	}

	public void addProtectTime(Player player) {
		boolean sameDay = TimeHelper.isSameDay(player.getBookTime());
		if (sameDay) {
			return;
		}
		player.getEmbattleList().forEach(x -> {
			Hero hero = player.getHero(x);
			if (hero != null) {
				addProtime(hero, player);
			}

		});
		List<WarDefenseHero> defenseArmyList = player.getDefenseArmyList();
		defenseArmyList.forEach(x -> {
			Hero hero = player.getHero(x.getHeroId());
			if (hero != null) {
				addProtime(hero, player);
			}

		});
	}

	public void addProtime(Hero hero, Player player) {
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		heroBooks.forEach(book -> {
			Integer hour = warBookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.REVIVE_HERO_ROYAL_CITY);
			if (hour != null) {
				Lord lord = player.getLord();
				lord.setProtectedTime(lord.getProtectedTime() + hour * TimeHelper.HOUR_MS);
				player.setBookTime(System.currentTimeMillis());
			}
		});
	}

	public CommonPb.CityOwnerInfo.Builder createCityOwnerInfo(City city, Player player) {
		CommonPb.CityOwnerInfo.Builder builder = CommonPb.CityOwnerInfo.newBuilder();
		long now = System.currentTimeMillis();
		int status = 0;
		int country = city.getCountry();
		long lordId = city.getLordId();
		if (lordId != 0 && city.getEndTime() > now && country != 0) {
			Player owner = playerManager.getPlayer(lordId);
			if (owner != null) {
				builder.setOwnName(owner.getNick());
				builder.setOwnId(owner.roleId);
			}
			status = 2;
		}

		if (lordId == 0 && country != 0) {
			status = 1;
		}

		if (country == 0) {
			status = 0;
		}

		builder.setCountry(country);
		builder.setStatus(status);
		builder.setEndTime(city.getEndTime());

		// 计算城池血量
		CityMonster cityMonster = cityManager.getCityMonster(city.getCityId());
		int total = 0;
		if (cityMonster != null) {
			Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
			if (monsterInfoMap != null) {
				for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
					total += cityMonsterInfo.getSoldier();
				}
			}
		}

		builder.setSoldier(total);
		builder.setCityId(city.getCityId());

		// 玩家是否可以参加竞选
		ConcurrentHashMap<Integer, HashSet<Long>> warAttend = cityManager.getWarAttenders();
		int cityId = city.getCityId();
		HashSet<Long> attenders = warAttend.get(cityId);
		if (attenders != null && attenders.contains(player.roleId)) {
			builder.setCanAttendElection(true);
		} else {
			builder.setCanAttendElection(false);
		}

		builder.setMakeEndTime(city.getMakeItemTime());
		builder.setMakePeriod(cityManager.getMakePeriod(cityId));
		builder.setElectionEndTime(city.getElectionEndTime());
		builder.setProtectedTime(city.getProtectedTime());
		builder.setLevel(city.getCityLv());
		builder.setPeople(city.getPeople());

		return builder;

	}

	public CommonPb.CityOwnerInfo.Builder createCityOwner(City city) {
		CommonPb.CityOwnerInfo.Builder builder = CommonPb.CityOwnerInfo.newBuilder();
		long now = System.currentTimeMillis();
		int status = 0;
		int country = city.getCountry();
		long lordId = city.getLordId();
		if (lordId != 0 && city.getEndTime() > now && country != 0) {
			Player owner = playerManager.getPlayer(lordId);
			if (owner != null) {
				builder.setOwnName(owner.getNick());
				builder.setOwnId(owner.roleId);
			}
			status = 2;
		}

		if (lordId == 0 && country != 0) {
			status = 1;
		}

		if (country == 0) {
			status = 0;
		}

		builder.setCountry(country);
		builder.setStatus(status);
		builder.setEndTime(city.getEndTime());

		// 计算城池血量
		CityMonster cityMonster = cityManager.getCityMonster(city.getCityId());
		int total = 0;
		if (cityMonster != null) {
			Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
			if (monsterInfoMap != null) {
				for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
					total += cityMonsterInfo.getSoldier();
				}
			}
		}

		builder.setSoldier(total);
		builder.setCityId(city.getCityId());

		// 玩家是否可以参加竞选
		int cityId = city.getCityId();
		if (city.getAwardNum() <= 0) {
			builder.setMakeEndTime(city.getMakeItemTime());
		} else {
			builder.setMakeEndTime(0);
		}
		builder.setMakePeriod(cityManager.getMakePeriod(cityId));
		builder.setElectionEndTime(city.getElectionEndTime());
		builder.setProtectedTime(city.getProtectedTime());
		builder.setLevel(city.getCityLv());
		builder.setPeople(city.getPeople());
		// logger.info("==cityId {}=====MakeEndTime{}=============",city.getCityId(),city.getMakeItemTime());
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(city.getCityId());
		builder.setCityName(city.getCityName() == null ? staticWorldCity.getName() : city.getCityName());
		return builder;

	}

	public void synMapCity(int mapId, int cityId) {
		City city = cityManager.getCity(cityId);
		if (city == null) {
			LogHelper.CONFIG_LOGGER.error("city is null!");
			return;
		}

		Iterator<Player> it = playerManager.getPlayers().values().iterator();
		WorldPb.SynMapCityRq.Builder builder = WorldPb.SynMapCityRq.newBuilder();
		CommonPb.CityOwnerInfo.Builder cityOwnerInfo = createCityOwner(city);

		// 玩家是否可以参加竞选
		ConcurrentHashMap<Integer, HashSet<Long>> warAttend = cityManager.getWarAttenders();
		HashSet<Long> attenders = warAttend.get(cityId);
		while (it.hasNext()) {
			Player player = it.next();
			if (player == null) {
				continue;
			}

			int playerMapId = getMapId(player);
			if (playerMapId != mapId) {
				cityOwnerInfo.setCanAttendElection(false);
			} else {
				if (attenders != null && attenders.contains(player.roleId)) {
					cityOwnerInfo.setCanAttendElection(true);
				} else {
					cityOwnerInfo.setCanAttendElection(false);
				}
			}

			builder.setInfo(cityOwnerInfo);
			playerManager.synMapCityRq(player, builder.build());
		}
	}

	public void synMapCity(int mapId, int cityId, Player player) {
		City city = cityManager.getCity(cityId);
		if (city == null) {
			LogHelper.CONFIG_LOGGER.error("city is null!");
			return;
		}
		WorldPb.SynMapCityRq.Builder builder = WorldPb.SynMapCityRq.newBuilder();
		CommonPb.CityOwnerInfo.Builder cityOwnerInfo = createCityOwner(city);
		// 玩家是否可以参加竞选
		ConcurrentHashMap<Integer, HashSet<Long>> warAttend = cityManager.getWarAttenders();
		HashSet<Long> attenders = warAttend.get(cityId);
		int playerMapId = getMapId(player);
		if (playerMapId != mapId) {
			return;
		}
		if (attenders != null && attenders.contains(player.roleId)) {
			cityOwnerInfo.setCanAttendElection(true);
		} else {
			cityOwnerInfo.setCanAttendElection(false);
		}
		builder.setInfo(cityOwnerInfo);
		playerManager.synMapCityRq(player, builder.build());
	}

	// 世界目标:1,...9:
	public WorldTarget getWorldTarget(Player player) {
		Map<Integer, WorldTargetAward> worldTargetAwardMap = player.getWorldTargetAwardMap();
		WorldTarget worldTarget = new WorldTarget();
		WorldTargetAward checkAward = null;
		for (int targetId = 1; targetId <= 9; targetId++) {
			WorldTargetAward targetAward = worldTargetAwardMap.get(targetId);
			if (targetAward != null && targetAward.getStatus() == 2) {
				continue;
			}

			// 世界boss直接跳过
			List<Integer> res = getTargetRes(targetId, player);
			if (targetId == WorldTargetType.KILL_BOSS1 || targetId == WorldTargetType.KILL_BOSS2) {
				if (res.get(0) == 1) {
					continue;
				}
			}

			if (targetAward == null) {
				targetAward = new WorldTargetAward();
				targetAward.setStatus(res.get(0));
				targetAward.setTargetId(targetId);
				targetAward.setCountry(res.get(1));
				worldTargetAwardMap.put(targetId, targetAward);
				checkAward = targetAward;
				break;
			} else {
				targetAward.setStatus(res.get(0));
				targetAward.setTargetId(targetId);
				targetAward.setCountry(res.get(1));
				checkAward = targetAward;
				if (checkAward.getCountry() == player.getCountry()) {
					worldTarget.setTimes(2);
				}

				break;
			}
		}

		if (checkAward != null) {
			worldTarget.setCountry(checkAward.getCountry());
			worldTarget.setTargetId(checkAward.getTargetId());
			worldTarget.setStatus(checkAward.getStatus());
		} else {
			return null;
		}

		return worldTarget;
	}

	public WorldTarget getWorldTargetById(Player player, int paramTargetId) {
		Map<Integer, WorldTargetAward> worldTargetAwardMap = player.getWorldTargetAwardMap();
		WorldTarget worldTarget = new WorldTarget();
		WorldTargetAward checkAward = null;
		for (int targetId = 1; targetId <= 9; targetId++) {
			WorldTargetAward targetAward = worldTargetAwardMap.get(targetId);
			if (targetAward != null && targetAward.getStatus() == 2) {
				continue;
			}

			if (paramTargetId != targetId) {
				continue;
			}

			List<Integer> res = getTargetRes(targetId, player);
			if (targetAward == null) {
				targetAward = new WorldTargetAward();
				targetAward.setStatus(res.get(0));
				targetAward.setTargetId(targetId);
				targetAward.setCountry(res.get(1));
				worldTargetAwardMap.put(targetId, targetAward);
				checkAward = targetAward;
				break;
			} else {
				targetAward.setStatus(res.get(0));
				targetAward.setTargetId(targetId);
				targetAward.setCountry(res.get(1));
				checkAward = targetAward;
				if (checkAward.getCountry() == player.getCountry()) {
					worldTarget.setTimes(2);
				}

				break;
			}
		}

		if (checkAward == null) {
			return null;
		}

		worldTarget.setCountry(checkAward.getCountry());
		worldTarget.setTargetId(checkAward.getTargetId());
		worldTarget.setStatus(checkAward.getStatus());

		return worldTarget;
	}

	public WorldBoss getBoss(Player player, int bossId) {
		WorldData worldData = getWolrdInfo();
		if (worldData == null) {
			LogHelper.CONFIG_LOGGER.error("worldData is null");
			return null;
		}

		// 找世界boss: 张角和共享
		WorldBoss worldBoss = null;
		if (bossId == WorldBossId.WORLD_BOSS_1) {
			worldBoss = worldData.getWorldBoss(1);
		} else if (bossId == WorldBossId.WORLD_BOSS_2) {
			worldBoss = worldData.getShareBoss();
		}

		return worldBoss;

	}

	// 给任务刷野怪
	public void flushTaskMonster1(Player player, int taskId) {
		// logger.error("flushTaskMonster1 START player {} taskId {}", player.getLord().getLordId(), taskId);
		StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
		if (staticTask == null) {
			LogHelper.CONFIG_LOGGER.error("staticTask is null");
			return;
		}

		int generateMonster = staticTask.getGenerateMonster();
		if (generateMonster <= 0) {
			return;
		}
		// 给玩家刷两只野怪
		int mapId = getMapId(player);
		MapInfo mapInfo = getMapInfo(mapId);
		// 表示没有开启世界地图
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
			return;
		}

		Pos pos = mapInfo.getFlushTaskMosterPos(player);
		List<Entity> list = new ArrayList<>();
		if (pos != null && !pos.isError()) {
			Monster monster = addMonster(pos, generateMonster + 1000, generateMonster, mapInfo, AddMonsterReason.FLUSH_TASK_MONSTER);
			if (monster != null) {
				list.add(monster);
			}
		}
		synEntityAddRq(list);
	}

	//public Pos getFlushTaskMosterPos(Player player) {
	//    int posx = player.getPosX();
	//    int posy = player.getPosY();
	//    int mapId = getMapId(player);
	//    MapInfo mapInfo = getMapInfo(mapId);
	//    if (mapInfo == null) {
	//        LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
	//        return null;
	//    }
	//
	//    MapDistance mapDistance = new MapDistance(player.getPos());
	//
	//    for (int i = posx - 2; i <= posx + 2; i++) {
	//        for (int j = posy - 2; j <= posy + 2; j++) {
	//            if (i == posx && j == posy) {
	//                continue;
	//            }
	//            Pos pos = new Pos(i, j);
	//
	//            if (mapInfo.isFreePos(pos)) {
	//                mapDistance.findNearestPos(pos);
	//            }
	//        }
	//    }
	//
	//    return mapDistance.getNearestPos();
	//}

	//private List<Pos> getTaskFlushMonsterPos(MapInfo mapInfo, Player player) {
	//    // 从 3*3 或者5**5
	//    List<Pos> bigArea = getPos(player);
	//    bigArea.addAll(bigArea);
	//    if (bigArea.size() >= 1) {
	//        return bigArea.subList(0, 1);
	//    } else {
	//        // TODO 这里做容错处理？
	//        return bigArea;
	//    }
	//}

//	// 给任务刷野怪
//	public void flushTaskMonster1(Player player, int taskId) {
//		// logger.error("flushTaskMonster1 START player {} taskId {}", player.getLord().getLordId(), taskId);
//		StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
//		if (staticTask == null) {
//			LogHelper.CONFIG_LOGGER.error("staticTask is null");
//			return;
//		}
//
//		int generateMonster = staticTask.getGenerateMonster();
//		if (generateMonster <= 0) {
//			return;
//		}
//		// 给玩家刷两只野怪
//		int mapId = getMapId(player);
//		MapInfo mapInfo = getMapInfo(mapId);
//		// 表示没有开启世界地图
//		if (mapInfo == null) {
//			LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
//			return;
//		}
//		Pos pos = getTaskFlushMonsterPos(mapInfo, player);
//		List<Entity> list = new ArrayList<>();
////		for (Pos pos : flushPos) {
////			// logger.error("flushTaskMonster1 add Pos {}", pos.toString());
////
////		}
//		if (pos != null) {
//			Monster monster = addMonster(pos, generateMonster + 1000, generateMonster, mapInfo, AddMonsterReason.FLUSH_TASK_MONSTER);
//			list.add(monster);
//			synEntityAddRq(list);
//		}
//
//		// logger.error("flushTaskMonster1 END player {} taskId {}", player.getLord().getLordId(), taskId);
//	}
//
//	private Pos getTaskFlushMonsterPos(MapInfo mapInfo, Player player) {
//		// 从 3*3 或者5**5
//		List<Pos> bigArea = getPos(player);
//		// bigArea.addAll(bigArea);
//		Iterator<Pos> iterator1 = bigArea.iterator();
//		Pos pos = null;
//		while (iterator1.hasNext()) {
//			Pos next = iterator1.next();
//			next.setDistance(player.getPos());
//			if (pos == null) {
//				pos = next;
//			} else {
//				if (next.getDistance() < pos.getDistance()) {
//					pos = next;
//				}
//			}
//		}
//
//		return pos;
////		if (bigArea.size() >= 1) {
////			return bigArea.subList(0, 1);
////		} else {
////			// TODO 这里做容错处理？
////			return bigArea;
////		}
//	}

	// 获得可以刷新怪物的坐标
//    public List<Pos> getPos(Player player) {
//        int posx = player.getPosX();
//        int posy = player.getPosY();
//        int mapId = getMapId(player);
//        MapInfo mapInfo = getMapInfo(mapId);
//        List<Pos> pos = new ArrayList<Pos>();
//        if (mapInfo == null) {
//            LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
//            return pos;
//        }
//        // 先把3*3的坐标取出来
//        for (int i = posx - 1; i <= posx + 1; i++) {
//            for (int j = posy - 1; j <= posy + 1; j++) {
//                if (i == posx && j == posy) {
//                    continue;
//                }
//                Pos elem = new Pos(i, j);
//                if (mapInfo.isFreePos(elem)) {
//                    pos.add(elem);
//                }
//
//            }
//        }
//        // 在取5*5的坐标
//        for (int i = posx - 2; i <= posx + 2; i++) {
//            for (int j = posy - 2; j <= posy + 2; j++) {
//                if (i == posx && j == posy) {
//                    continue;
//                }
//                Pos elem = new Pos(i, j);
//                if (!pos.contains(elem)) {
//                    if (mapInfo.isFreePos(elem)) {
//                        pos.add(elem);
//                    }
//                }
//
//            }
//        }
//
////        // 在取7*7的坐标
////        for (int i = posx - 3; i <= posx + 3; i++) {
////            for (int j = posy - 3; j <= posy + 3; j++) {
////                if (i == posx && j == posy) {
////                    continue;
////                }
////                Pos elem = new Pos(i, j);
////                if (!pos.contains(elem)) {
////                    if (mapInfo.isFreePos(elem)) {
////                        pos.add(elem);
////                    }
////                }
////
////            }
////        }
//        return pos;
//
//    }

	public static void main(String[] args) {
		int posx = 91;
		int posy = 64;
		List<Pos> pos = new ArrayList<Pos>();
		// 先把3*3的坐标取出来
		for (int i = posx - 1; i <= posx + 1; i++) {
			for (int j = posy - 1; j <= posy + 1; j++) {
				if (i == posx && j == posy) {
					continue;
				}
				pos.add(new Pos(i, j));
			}
		}

		// 在取5*5的坐标
		for (int i = posx - 2; i <= posx + 2; i++) {
			for (int j = posy - 2; j <= posy + 2; j++) {
				if (i == posx && j == posy) {
					continue;
				}
				Pos elem = new Pos(i, j);
				if (!pos.contains(elem)) {
					pos.add(elem);
				}

			}
		}
	}

	// 叛军专用接口
	public Monster addMonster(Pos pos, int monsterId, int monsterLv, MapInfo mapInfo, int reason) {
		Monster monster = createMonster(monsterId, monsterLv, pos, reason);
		if (!mapInfo.addPos(pos, monster)) {
			return null;
		}

		Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();

		playerManager.clearPos(pos);
		monsterMap.put(pos, monster);
		mapInfo.updateMonsterNum(monsterLv);
		return monster;
	}

	// 叛军专用接口
	public Monster addRiotMonster(Pos pos, int monsterId, int monsterLv, MapInfo mapInfo, int reason) {
		Monster monster = createRiotMonster(monsterId, monsterLv, pos);
		if (!mapInfo.addPos(pos, monster)) {
			return null;
		}

		Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();

		playerManager.clearPos(pos);
		monsterMap.put(pos, monster);
		mapInfo.updateMonsterNum(monsterLv);
		return monster;
	}

	// 创建巨型虫族
	public BigMonster addBigMonster(Pos pos, long monsterId, int monsterLv, MapInfo mapInfo, int reason) {
		BigMonster monster = createBigMonster(monsterId, monsterLv, pos);
		if (!mapInfo.addPos(pos, monster)) {
			return null;
		}

		Map<Pos, BigMonster> monsterMap = mapInfo.getBigMonsterMap();

		playerManager.clearPos(pos);
		monsterMap.put(pos, monster);
		return monster;
	}

	// 伏击叛军专用接口
	public RebelMonster addRebelMonster(Pos pos, int monsterId, int monsterLv, MapInfo mapInfo, int reason, int country) {
		RebelMonster monster = createRebelMonster(monsterId, monsterLv, pos, reason, country);
		if (!mapInfo.addPos(pos, monster)) {
			return null;
		}

		Map<Pos, RebelMonster> monsterMap = mapInfo.getRebelMap();

		playerManager.clearPos(pos);
		monsterMap.put(pos, monster);
		mapInfo.updateMonsterNum(monsterLv);
		return monster;
	}

	// 创建一个野怪
	public Monster createMonster(int monsterId, int monsterLevel, Pos pos, int reason) {
		Monster monster = new Monster();
		monster.setEntityType(EntityType.Monster);
		monster.setId(monsterId);
		monster.setLevel(monsterLevel);
		monster.setPos(new Pos(pos.getX(), pos.getY()));

		return monster;
	}

	// 创建一个叛军
	public RebelMonster createRebelMonster(int monsterId, int monsterLevel, Pos pos, int reason, int country) {
		RebelMonster monster = new RebelMonster();
		monster.setEntityType(EntityType.REBEL_MONSTER);
		monster.setId(monsterId);
		monster.setLevel(monsterLevel);
		monster.setPos(new Pos(pos.getX(), pos.getY()));
		monster.setCreateTime(System.currentTimeMillis());
		return monster;
	}

	// 创建虫族入侵叛军
	public RebelMonster createRiotMonster(int monsterId, int monsterLevel, Pos pos) {
		RebelMonster monster = new RebelMonster();
		monster.setEntityType(EntityType.RIOT_MONSTER);
		monster.setId(monsterId);
		monster.setLevel(monsterLevel);
		monster.setPos(new Pos(pos.getX(), pos.getY()));
		monster.setCreateTime(System.currentTimeMillis());
		return monster;
	}

	// 创建虫族入侵叛军
	public BigMonster createBigMonster(long monsterId, int monsterLevel, Pos pos) {
		BigMonster monster = new BigMonster();
		monster.setEntityType(EntityType.BIG_MONSTER);
		monster.setId(monsterId);
		monster.setLevel(monsterLevel);
		monster.setPos(new Pos(pos.getX(), pos.getY()));
		return monster;
	}

	public Resource addResource(Pos pos, int resLv, int resType, MapInfo mapInfo) {

		Resource resource = createResource(EntityType.Resource, resType, resLv);
		resource.setPos(pos);

		if (!mapInfo.addPos(pos, resource)) {
			return null;
		}

		Map<Pos, Resource> resourceMap = mapInfo.getResourceMap();
		resourceMap.put(pos, resource);

		return resource;
	}

	// 处理击杀野怪后玩家血量
	public void caculatePlayer(Team team, Player player, HashMap<Integer, Integer> solderRecMap) {
		List<BattleEntity> battleEntities = team.getAllEnities();
		int soilder = 0;
		for (BattleEntity battleEntity : battleEntities) {
			// 过滤不是玩家的实体
			if (battleEntity.getLordId() != player.roleId) {
				continue;
			}

			int id = battleEntity.getEntityId();
			Hero hero = player.getHero(id);
			if (hero != null) {
				HashMap<Long, HashMap<Integer, Integer>> tmpMap = new HashMap<>();
				tmpMap.put(battleEntity.getLordId(), solderRecMap);
				handleSoldierRec(tmpMap, hero, battleEntity);
				hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
				if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
					int moreSoldierNum = hero.getCurrentSoliderNum() - hero.getSoldierNum();
					hero.setCurrentSoliderNum(hero.getSoldierNum());
					int soldierType = heroManager.getSoldierType(hero.getHeroId());
					if (moreSoldierNum > 0) {
						playerManager.addAward(player, AwardType.SOLDIER, soldierType, moreSoldierNum, Reason.KILL_CACULATE);
					}
				}
			}
			soilder += battleEntity.getMaxSoldierNum() - battleEntity.getCurSoldierNum();
		}
		if (soilder > 0) {
			activityManager.updActPerson(player, ActivityConst.ACT_SOILDER_RANK, soilder, 0);
		}
	}

	// 计算伤兵恢复[需要将对应的兵力加到兵营里面]
	public void handleSoldierRec(HashMap<Long, HashMap<Integer, Integer>> solderRecMap, Hero hero, BattleEntity battleEntity) {
		// 兵书技能加成效果
		int currentSoldier = hero.getCurrentSoliderNum();
		int lastSoldier = battleEntity.getCurSoldierNum();
		int diff = currentSoldier - lastSoldier;
		if (diff > 0) {
			int soldierType = heroManager.getSoldierType(hero.getHeroId());
			HashMap<Integer, Integer> map = solderRecMap.computeIfAbsent(battleEntity.getLordId(), x -> new HashMap<>());
			map.merge(soldierType, diff, (a, b) -> (a + b));
			// 兵书技能加成效果
			long lordId = battleEntity.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (null != player) {
				Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.SOLDIER_REC);
				float soldierAdd = 0f;
				if (heroWarBookSkillEffect != null) {
					soldierAdd = heroWarBookSkillEffect.intValue() / 1000.00f;
				}
				double buff = flameWarService.getBuff(BuffType.buff_3, player.getCountry(), player);
				int rec = (int) ((soldierAdd + buff) * (float) diff);
				playerManager.addAward(player, AwardType.SOLDIER, battleEntity.getSoldierType(), rec, Reason.SOLDIER_REC);
			}
		}
	}

	// 防守玩家英雄血量，城防军血量，友军驻防血量, player防守者
	public void handleDefenceSoldier(Team team, Player player, int reason, HashMap<Long, HashMap<Integer, Integer>> defenceRec) {
		List<BattleEntity> battleEntities = team.getAllEnities();
		int soilder = 0;
		boolean defenseFlag = false;
		for (BattleEntity battleEntity : battleEntities) {
			// 我方武将
			int id = battleEntity.getEntityId();
			if (battleEntity.getLordId() == player.roleId && battleEntity.getEntityType() == BattleEntityType.HERO) {
				Hero hero = player.getHero(id);
				if (hero != null) {
					handleSoldierRec(defenceRec, hero, battleEntity);
					hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
					if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
						hero.setCurrentSoliderNum(hero.getSoldierNum());
						LoggerFactory.getLogger(getClass()).error("hero.getCurrentSoliderNum() = " + hero.getCurrentSoliderNum() + ", hero.getSoldierNum() = " + hero.getSoldierNum());
					}
					playerManager.synSoldierChange(player, Reason.WAR);
				}
				soilder += battleEntity.getMaxSoldierNum() - battleEntity.getCurSoldierNum();
			} else if (battleEntity.getEntityType() == BattleEntityType.WALL_FRIEND_HERO) {
				// 友军驻防, 当血量为0的时候自动回城
				handleWallAssistSoldier(player, battleEntity, reason);
			} else if (battleEntity.getEntityType() == BattleEntityType.WALL_DEFENCER) {
				handleWallDefencer(player, battleEntity);
			} else if (battleEntity.getEntityType() == BattleEntityType.DEFENSE_ARMY_HERO) {
				Hero hero = player.getHero(id);
				if (hero != null) {
					hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
					defenseFlag = true;
				}
			} else if (battleEntity.getEntityType() == BattleEntityType.FRIEND_HERO) { // 处理友军协防扣血
				Player target = playerManager.getPlayer(battleEntity.getLordId());
				Hero hero = target.getHero(id);
				if (hero != null) {
					handleSoldierRec(defenceRec, hero, battleEntity);
					hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
					if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
						LoggerFactory.getLogger(getClass()).error("hero.getCurrentSoliderNum() = " + hero.getCurrentSoliderNum() + ", hero.getSoldierNum() = " + hero.getSoldierNum());
					}
					playerManager.synSoldierChange(target, Reason.WAR);
				}
			}
		}
		if (defenseFlag) {
			// TODO jyb处理参谋部城防军的逻辑
			castleService.updateDefenseSoldierByPlayer(player);
		}
		if (soilder > 0) {
			activityManager.updActPerson(player, ActivityConst.ACT_SOILDER_RANK, soilder, 0);
		}
	}

	public void handleWallAssistSoldier(Player player, BattleEntity battleEntity, int reason) {
		// 友军驻防, 当血量为0的时候自动回城
		Wall wall = player.getWall();
		// 找出当前实体
		// 驻防的玩家Id
		long lordId = battleEntity.getLordId();
		int id = battleEntity.getEntityId();
		Player target = playerManager.getPlayer(lordId);
		if (target == null) {
			LogHelper.CONFIG_LOGGER.error("target is null.");
			return;
		}

		WallFriend wallFriend = wall.getWallFriendByHeroId(id, lordId);
		if (wallFriend == null) {
			LogHelper.CONFIG_LOGGER.error("wallFriend is null.");
			return;
		}

		Hero hero = target.getHero(id);
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.error("hero is null!");
			return;
		}

		hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
		if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
			hero.setCurrentSoliderNum(hero.getSoldierNum());
			LoggerFactory.getLogger(getClass()).error("hero.getCurrentSoliderNum() = " + hero.getCurrentSoliderNum() + ", hero.getSoldierNum() = " + hero.getSoldierNum());
		}

		wallFriend.setSoldier(hero.getCurrentSoliderNum());
		if (battleEntity.getCurSoldierNum() <= 0) {
			// player 删除当前的wallFriend
			wall.removeWallFriendByHeroId(id, lordId);
			// 行军返回
			March march = wallFriend.getMarch();
			if (march.getKeyId() != 0) {
				marchManager.doMarchReturn(march, target, reason);
				// 发送邮件给客户端
				// 您在%s的基地驻防的将领%s, 由于基地被击飞或迁移，开始返回。
				String name = String.valueOf(player.getNick());
				String heroName = String.valueOf(wallFriend.getHeroId());
				playerManager.sendNormalMail(target, MailId.WALL_MARCH_RETURN, name, heroName);
			}
		}

		playerManager.synSoldierChange(target, Reason.WAR);

		// 通知玩家城防变化
		playerManager.synWallInfo(player);

	}

	public void handleWallDefencer(Player player, BattleEntity battleEntity) {
		// 需要确认城防军用哪个Id初始化的, wallDefence的Id初始化的
		int keyId = battleEntity.getWallDefencerkeyId();
		Wall wall = player.getWall();
		if (wall == null) {
			LogHelper.CONFIG_LOGGER.error("wall is null!");
			return;
		}

		WallDefender wallDefender = wall.getWallDefender(keyId);
		if (wallDefender == null) {
			LogHelper.CONFIG_LOGGER.error("wallDefer is null.");
			return;
		}

		// 设置城防军, 删除后城防军
		wallDefender.setSoldierNum(battleEntity.getRealCurSoldierNum());
		if (wallDefender.getSoldierNum() <= 0) {
			if (!wall.isWallDefencerExist(battleEntity.getWallDefencerkeyId())) {
				LogHelper.CONFIG_LOGGER.error("wall defencer keyId not exists!");
			}
			wall.removeWallDefencer(battleEntity.getWallDefencerkeyId());
		}

		// 通知玩家城防变化
		playerManager.synWallInfo(player);

	}

	public HeroAddExp caculateTeamKill(Team team, long attackerId) {
		ArrayList<BattleEntity> entities = team.getAllEnities();
		// 损兵获得的经验值
		HeroAddExp heroAddExp = new HeroAddExp(attackerId);
		for (BattleEntity battleEntity : entities) {
			if (battleEntity == null) {
				continue;
			}

			long lordId = battleEntity.getLordId();
			if (lordId == 0) {
				continue;
			}

			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			Hero hero = player.getHero(battleEntity.getEntityId());
			if (hero == null) {
				continue;
			}

			int heroLv = hero.getHeroLv();
			int heroQuality = staticHeroMgr.getQuality(hero.getHeroId());
			if (heroQuality == 0) {
				LogHelper.CONFIG_LOGGER.error("heroId = " + hero.getHeroId() + " is 0.");
				continue;
			}

			int factor = getQualityFactor(heroQuality);
			double resFactor = (double) factor / 1000.0 + 1.0;
			int lostExp = staticWorldMgr.getLostExp(heroLv, WorldExpType.HeroExp);
			double resLostExp = (double) lostExp / 1000.0;
			// 给英雄增加经验
			int totalLostExp = (int) ((double) battleEntity.getLost() * resLostExp * resFactor);
			heroManager.addExp(hero, player, totalLostExp, Reason.WORLD_BATTLE);
			heroAddExp.updateExp(player.roleId, hero.getHeroId(), totalLostExp);
		}

		// 击杀获得的经验值
		for (BattleEntity battleEntity : entities) {
			if (battleEntity == null) {
				continue;
			}

			long lordId = battleEntity.getLordId();
			if (lordId == 0) {
				continue;
			}

			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			Hero hero = player.getHero(battleEntity.getEntityId());
			if (hero == null) {
				continue;
			}

			// level, entityType, quality, num
			Map<Integer, Map<Integer, RecordEntity>> killRecord = battleEntity.getRecordEntityMap();
			if (killRecord.isEmpty()) {
				continue;
			}

			int totalKillExp = 0;
			for (Map.Entry<Integer, Map<Integer, RecordEntity>> elem : killRecord.entrySet()) {
				if (elem == null || elem.getValue() == null) {
					continue;
				}

				Map<Integer, RecordEntity> recordEntityMap = elem.getValue();
				if (recordEntityMap == null) {
					LogHelper.CONFIG_LOGGER.error("recordEntityMap is null!");
					continue;
				}

				int type = elem.getKey();

				for (Map.Entry<Integer, RecordEntity> innerElem : recordEntityMap.entrySet()) {
					if (innerElem == null) {
						continue;
					}
					int level = innerElem.getKey();
					int killExp = staticWorldMgr.getKillExpByType(level, type);
					if (killExp <= 0) {
						continue;
					}

					RecordEntity recordEntity = innerElem.getValue();
					if (recordEntity == null) {
						continue;
					}

					HashMap<Integer, Integer> entityMap = recordEntity.getEntityMap();
					Iterator<Integer> entityIter = entityMap.keySet().iterator();
					// 品质 + 击杀数量
					while (entityIter.hasNext()) {
						Integer quality = entityIter.next();
						Integer killNum = entityMap.get(quality); // 实际击杀值
						if (killNum == null) {
							continue;
						}
						int factor = getQualityFactor(quality);
						double resFactor = (double) factor / 1000.0 + 1.0;
						int configExp = staticWorldMgr.getKillExpByType(level, WorldExpType.HeroExp);
						double resKillExp = (double) configExp / 1000.0;
						totalKillExp += (int) ((double) killNum * resKillExp * resFactor);
					}

				} // end for

				heroManager.addExp(hero, player, totalKillExp, Reason.WORLD_BATTLE); // 计算击杀的经验值
				heroAddExp.updateExp(player.roleId, hero.getHeroId(), totalKillExp);

			} // end for

		}

		return heroAddExp;
	}

	public void caculateTeamDefenceKill(Team team) {
		ArrayList<BattleEntity> entities = team.getAllEnities();
		// 损兵获得的经验值
		for (BattleEntity battleEntity : entities) {
			if (battleEntity == null) {
				continue;
			}

			long lordId = battleEntity.getLordId();
			if (lordId == 0) {
				continue;
			}

			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			Hero hero = player.getHero(battleEntity.getEntityId());
			if (hero == null) {
				continue;
			}

			int heroLv = hero.getHeroLv();
			int heroQuality = staticHeroMgr.getQuality(hero.getHeroId());
			if (heroQuality == 0) {
				LogHelper.CONFIG_LOGGER.error("heroId = " + hero.getHeroId() + " is 0.");
				continue;
			}

			int factor = getQualityFactor(heroQuality);
			double resFactor = (double) factor / 1000.0 + 1.0;
			int lostExp = staticWorldMgr.getLostExp(heroLv, WorldExpType.HeroExp);
			double resLostExp = (double) lostExp / 1000.0;
			// 给英雄增加经验
			int totalLostExp = (int) ((double) battleEntity.getLost() * resLostExp * resFactor);
			heroManager.addExp(hero, player, totalLostExp, Reason.WORLD_BATTLE);
		}

		// 击杀获得的经验值
		for (BattleEntity battleEntity : entities) {
			if (battleEntity == null) {
				continue;
			}

			long lordId = battleEntity.getLordId();
			if (lordId == 0) {
				continue;
			}

			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			Hero hero = player.getHero(battleEntity.getEntityId());
			if (hero == null) {
				continue;
			}

			// level, entityType, quality, num
			Map<Integer, Map<Integer, RecordEntity>> killRecord = battleEntity.getRecordEntityMap();
			if (killRecord.isEmpty()) {
				continue;
			}

			int totalKillExp = 0;
			for (Map.Entry<Integer, Map<Integer, RecordEntity>> elem : killRecord.entrySet()) {
				if (elem == null || elem.getValue() == null) {
					continue;
				}

				Map<Integer, RecordEntity> recordEntityMap = elem.getValue();
				if (recordEntityMap == null) {
					LogHelper.CONFIG_LOGGER.error("recordEntityMap is null!");
					continue;
				}

				int type = elem.getKey();

				for (Map.Entry<Integer, RecordEntity> innerElem : recordEntityMap.entrySet()) {
					if (innerElem == null) {
						continue;
					}
					int level = innerElem.getKey();
					int killExp = staticWorldMgr.getKillExpByType(level, type);
					if (killExp <= 0) {
						continue;
					}

					RecordEntity recordEntity = innerElem.getValue();
					if (recordEntity == null) {
						continue;
					}

					HashMap<Integer, Integer> entityMap = recordEntity.getEntityMap();
					Iterator<Integer> entityIter = entityMap.keySet().iterator();
					// 品质 + 击杀数量

					while (entityIter.hasNext()) {
						Integer quality = entityIter.next();
						Integer killNum = entityMap.get(quality); // 实际击杀值
						int factor = getQualityFactor(quality);
						double resFactor = (double) factor / 1000.0 + 1.0;
						int configExp = staticWorldMgr.getKillExpByType(level, WorldExpType.HeroExp);
						double resKillExp = (double) configExp / 1000.0;
						totalKillExp += (int) ((double) killNum * resKillExp * resFactor);
					}

				} // end for

				heroManager.addExp(hero, player, totalKillExp, Reason.WORLD_BATTLE); // 计算击杀的经验值

			} // end for

		}

	}

	public int getQualityFactor(int quality) {
		if (quality == Quality.WHITE.get()) {
			return staticLimitMgr.getNum(65);
		} else if (quality == Quality.BLUE.get()) {
			return staticLimitMgr.getNum(66);
		} else if (quality == Quality.GREEN.get()) {
			return staticLimitMgr.getNum(67);
		} else if (quality == Quality.GOLD.get()) {
			return staticLimitMgr.getNum(68);
		} else if (quality == Quality.RED.get()) {
			return staticLimitMgr.getNum(69);
		} else if (quality == Quality.PURPLE.get()) {
			return staticLimitMgr.getNum(70);
		}

		return 0;
	}

	// 给玩家随机一个坐标
	public Pos givePlayerPos(MapInfo mapInfo) {
		Pos pos = mapInfo.randPickPos();
		return pos;
	}

	// 当前地图随机
	public Pos randByMapId(Player player, int mapId) {
		if (mapId != MapId.CENTER_MAP_ID) {
			return randCommonPos(mapId); // 城池: 按cityId来选
		} else {
			return handleSpecialFly(player, mapId); // 当前地图随机
		}
	}

	public Pos randCommonPos(int mapId) { // 当前地图随机
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			return new Pos();
		}

		Pos currentPos = givePlayerPos(mapInfo);
		return currentPos;
	}

	public Pos randTargetPosMap(Player player, int targetType, List<MapStatus> openMap) {
		List<MapStatus> targetMap = new ArrayList<MapStatus>();
		for (MapStatus mapStatus : openMap) {
			int elem = getMapAreaType(mapStatus.getMapId());
			if (elem == targetType) {
				targetMap.add(mapStatus);
			}
		}

		int resMapId = handleFlyRule(player, targetMap);

		// 从openMap里面选出玩家国家的mapId, if mapId = 世界要塞，则走special, 否则走随机选取坐标
		return randByMapId(player, resMapId);
	}

	public Pos randBrotherPosMap(Player player, int targetType, List<MapStatus> openMap) {
		int mapId = getMapId(player);
		List<MapStatus> targetMap = new ArrayList<MapStatus>();
		for (MapStatus mapStatus : openMap) {
			int elem = getMapAreaType(mapStatus.getMapId());
			if (elem == targetType && mapStatus.getMapId() != mapId) {
				targetMap.add(mapStatus);
			}
		}

		// 从targetMap里面选出玩家国家的mapId, if mapId = 世界要塞，则走special, 否则走随机选取坐标
		int resMapId = handleFlyRule(player, targetMap);

		// 从openMap里面选出玩家国家的mapId, if mapId = 世界要塞，则走special, 否则走随机选取坐标
		return randByMapId(player, resMapId);
	}

	// 玩家坐标
	public Pos handlePlayerFlyPos(Player player) {
		Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
		List<MapStatus> openMap = new ArrayList<MapStatus>();
		for (MapStatus mapStatus : mapStatuses.values()) {
			if (mapStatus.getStatus() == 2) {
				openMap.add(mapStatus);
			}
		}

		int currentMapId = getMapId(player);
		int mapType = getMapAreaType(currentMapId);
		StaticPlayerKick staticPlayerKick = staticWorldMgr.getPlayerKick(mapType);
		if (staticPlayerKick == null) {
			LogHelper.CONFIG_LOGGER.error("player mapId is error");
			return player.getPos();
		}

		// 初始化掉落的内容
		List<Integer> randPosRate = new ArrayList<Integer>();
		if (mapType == 1) { // up1, up2
			randPosRate.add(staticPlayerKick.getCurrentMap());
			randPosRate.add(staticPlayerKick.getUp1());
			randPosRate.add(staticPlayerKick.getUp2());
			randPosRate.add(staticPlayerKick.getOtherSameMap());

		} else if (mapType == 2) { // down1, up1
			randPosRate.add(staticPlayerKick.getCurrentMap());
			randPosRate.add(staticPlayerKick.getDown1());
			randPosRate.add(staticPlayerKick.getUp1());
			randPosRate.add(staticPlayerKick.getOtherSameMap());
		} else if (mapType == 3) { // down1, down2
			randPosRate.add(staticPlayerKick.getCurrentMap());
			randPosRate.add(staticPlayerKick.getDown1());
			randPosRate.add(staticPlayerKick.getDown2());
			randPosRate.add(staticPlayerKick.getOtherSameMap());
		}

		// 判断当前地图实际开启的掉落
		int maxMapType = 1;
		for (MapStatus status : openMap) {
			int elem = getMapAreaType(status.getMapId());
			if (maxMapType < elem) {
				maxMapType = elem;
			}
		}

		if (maxMapType == 1) {
			randPosRate.set(1, 0);
			randPosRate.set(2, 0);
		} else if (maxMapType == 2) {
			randPosRate.set(2, 0);
		}

		// 检查有没有同类型的,如果没有设置为0
		boolean hasSame = false;
		for (MapStatus status : openMap) {
			int elem = getMapAreaType(status.getMapId());
			if (elem == mapType && status.getMapId() != currentMapId) {
				hasSame = true;
				break;
			}
		}

		if (!hasSame) {
			randPosRate.set(3, 0);
		}

		int totalRate = 0;
		for (Integer rate : randPosRate) {
			totalRate += rate;
		}

		int randNum = RandomHelper.threadSafeRand(1, totalRate);
		int moveId = 0;
		int checkNum = 0;
		for (int i = 0; i < randPosRate.size(); i++) {
			checkNum += randPosRate.get(i);
			if (randNum <= checkNum) {
				moveId = i;
				break;
			}
		}

		Pos pos = null;
		if (mapType == 1) {
			if (moveId == 0) { // 当前地图, 以mapId为单位
				pos = randByMapId(player, currentMapId);
			} else if (moveId == 1) { // 升1级, 以mapId为单位
				pos = randTargetPosMap(player, mapType + 1, openMap);
			} else if (moveId == 2) { // 升2级, 以世界要塞CityId为单位
				pos = randTargetPosMap(player, mapType + 2, openMap);
			} else { // 兄弟
				pos = randBrotherPosMap(player, mapType, openMap);
			}
		} else if (mapType == 2) {
			if (moveId == 0) { // 当前
				pos = randByMapId(player, currentMapId);
			} else if (moveId == 1) { // 降1级, 以mapId为单位
				pos = randTargetPosMap(player, mapType - 1, openMap);
			} else if (moveId == 2) { // 升1级, 以世界要塞CityId为单位
				pos = randTargetPosMap(player, mapType + 1, openMap);
			} else { // 兄弟
				pos = randBrotherPosMap(player, mapType, openMap);
			}
		} else if (mapType == 3) {
			if (moveId == 0) { // 当前, 以世界要塞CityId为单位
				pos = randByMapId(player, currentMapId);
			} else if (moveId == 1) { // 降1级, 以mapId为单位
				pos = randTargetPosMap(player, mapType - 1, openMap);
			} else if (moveId == 2) { // 降2级, 以mapId为单位
				pos = randTargetPosMap(player, mapType - 2, openMap);
			}
		}

		// 去掉当前玩家的坐标
		MapInfo mapInfo = getMapInfo(currentMapId);
		if (mapInfo == null) {
			return player.getPos();
		}

		// 随便本地图随机一个
		if (pos != null && pos.isError()) {
			pos = givePlayerPos(mapInfo);
		}

		return pos;
	}

	// 如果是据点检查据点攻占了没有，如果是boss检查boss击杀了没有
	public List<Integer> getTargetRes(int targetId, Player player) {
		StaticWorldTarget nextTargetConfig = staticWorldMgr.getStaticWorldTarget(targetId);
		List<Integer> res = new ArrayList<Integer>();
		res.add(0);
		res.add(0);
		if (nextTargetConfig.getType() == 1) {
			Lord lord = player.getLord();
			if (lord.getWorldKillMonsterStatus() == 1) {
				res.set(0, 1);
			}
			res.set(1, player.getCountry());
			return res;
		} else if (nextTargetConfig.getType() == 2) {
			ConcurrentHashMap<Integer, City> cityMap = cityManager.getCityMap();
			for (City city : cityMap.values()) {
				if (city == null) {
					continue;
				}
				int cityId = city.getCityId();
				StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
				if (worldCity == null) {
					continue;
				}

				if (city.getCountry() != 0) {
					if (worldCity.getType() == nextTargetConfig.getCityType()) {
						res.set(0, 1);
						res.set(1, city.getCountry());
						return res;
					}
				}
			}
		} else if (nextTargetConfig.getBossId() == WorldBossId.WORLD_BOSS_1) {
			WorldBoss worldBoss = getBoss(player, WorldBossId.WORLD_BOSS_1);
			if (worldBoss == null) {
				return res;
			}
			if (worldBoss.getSoldier() <= 0) {
				res.set(0, 1);
				res.set(1, worldBoss.getCountry());
			}
			return res;

		} else if (nextTargetConfig.getBossId() == WorldBossId.WORLD_BOSS_2) {
			WorldBoss worldBoss = getBoss(player, WorldBossId.WORLD_BOSS_2);
			if (worldBoss == null) {
				return res;
			}

			if (worldBoss.getSoldier() <= 0) {
				res.set(0, 1);
				res.set(1, worldBoss.getCountry());
			}

			return res;
		}

		return res;
	}

	public Resource getResource(Pos pos, Player player) {
		int mapId = getMapId(player);
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			return null;
		}
		return mapInfo.getResource(pos);
	}

	public void synPlayerMapStatus(Player player) {
//        if (!player.isLogin) {
//            return;
//        }

//        if (player.getChannelId() == -1) {
//            return;
//        }

		Map<Integer, MapStatus> mapStatusMap = player.getMapStatusMap();

		if (mapStatusMap.isEmpty()) {
			return;
		}

		WorldPb.SynAllMapStatusRq.Builder builder = WorldPb.SynAllMapStatusRq.newBuilder();
		for (MapStatus mapStatus : mapStatusMap.values()) {
			if (mapStatus == null) {
				LogHelper.CONFIG_LOGGER.error("mapStatus is null!");
				continue;
			}

			if (mapStatus.getMapId() == 0) {
				continue;
			}

			builder.addMapStatus(mapStatus.wrapPb());
		}

		SynHelper.synMsgToPlayer(player, WorldPb.SynAllMapStatusRq.EXT_FIELD_NUMBER, WorldPb.SynAllMapStatusRq.ext, builder.build());
	}

	public void synCityDev(City city) {
		WorldPb.SynCityDevRq.Builder builder = WorldPb.SynCityDevRq.newBuilder();
		builder.setCityId(city.getCityId());
		builder.setPeople(city.getPeople());
		builder.setCityLv(city.getCityLv());
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}

			if (!player.isLogin) {
				continue;
			}

			if (player.getChannelId() == -1) {
				continue;
			}
			SynHelper.synMsgToPlayer(player, WorldPb.SynCityDevRq.EXT_FIELD_NUMBER, WorldPb.SynCityDevRq.ext, builder.build());
		}

	}

	// mapId = 20, 找city=player country的国家飞，其他的找中心城市是自己的国家飞
	public int handleFlyRule(Player player, List<MapStatus> openMap) {
		int curMapId = getMapId(player);
		List<Integer> jumpMapId = new ArrayList<Integer>();
		List<Integer> allMapId = new ArrayList<Integer>();
		for (MapStatus status : openMap) {
			if (status.getStatus() < 2) {
				LogHelper.CONFIG_LOGGER.error("logic error");
				continue;
			}

			int id = status.getMapId();
			StaticWorldMap worldMap = staticWorldMgr.getStaticWorldMap(id);
			if (worldMap == null) {
				LogHelper.CONFIG_LOGGER.error("worldMap is null");
				continue;
			}

			int centerCityId = worldMap.getCenterCityId();
			City city = cityManager.getCity(centerCityId);
			if (city == null) {
				LogHelper.CONFIG_LOGGER.info("cityId = " + centerCityId + " has no config!");
				continue;
			}

			int configMapId = worldMap.getMapId();
			// 所有玩家能飞的map
			if (player.isMapCanMoves(configMapId) > 2) {
				allMapId.add(configMapId);
			}

			// 玩家所属国家的map
			if (configMapId == 25) {
				jumpMapId.add(configMapId);
			} else if (city.getCountry() == player.getCountry()) {
				if (player.isMapCanMoves(configMapId) > 2) {
					jumpMapId.add(configMapId);
				}
			}
		}

		// 玩家自己所属国家
		if (!jumpMapId.isEmpty()) {
			int num = RandomHelper.threadSafeRand(1, jumpMapId.size());
			return jumpMapId.get(num - 1);
		}

		// 所有开放国家
		if (!allMapId.isEmpty()) {
			int num = RandomHelper.threadSafeRand(1, allMapId.size());
			return allMapId.get(num - 1);
		}

		return curMapId;
	}

	// 在皇城里面，找到自己国家的city，然后随机位置，如果没有自己国家的位置，随机选择city,然后
	// 取出这个city的free pos，然后随机选取一个坐标，判断坐标是否合法，如果不合法，重新随机，
	// 随机次数为3次, 然后return
	public Pos handleSpecialFly(Player player, int mapId) {
		Pos resPos = new Pos();
		if (mapId != MapId.CENTER_MAP_ID) {
			LogHelper.CONFIG_LOGGER.error("mapId != 20, logic error!");
			return resPos;
		}
		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapInfo is null, mapId = " + mapId);
			return resPos;
		}
		// cityType = 7,8
		List<Integer> allCity = cityManager.getAllCenterCity();
		List<Integer> ownedCity = new ArrayList<Integer>();
		for (Integer cityId : allCity) {
			City city = cityManager.getCity(cityId);
			if (city == null) {
				continue;
			}
			if (city.getCountry() == player.getCountry()) {
				ownedCity.add(cityId);
			}
		}

		// 自己的城池
		if (!ownedCity.isEmpty()) {
			Pos ownedPos = randCityPos(ownedCity, mapInfo);
			return ownedPos;

		}

		// 剩余国家的城池
		if (!allCity.isEmpty()) {
			Pos ownedPos = randCityPos(allCity, mapInfo);
			return ownedPos;
		}

		return resPos;

	}

	public Pos randCityPos(List<Integer> cityIds, MapInfo mapInfo) {
		Pos resPos = new Pos();
		if (cityIds == null || cityIds.isEmpty()) {
			LogHelper.CONFIG_LOGGER.error("cityId == null or cityIds.isEmpty()");
			return resPos;
		}

		int index = RandomHelper.threadSafeRand(1, cityIds.size());
		if (index < 1) {
			LogHelper.CONFIG_LOGGER.error("index < 1 error in handleSpecialFly");
			return resPos;
		}

		int cityId = cityIds.get(index - 1);
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		if (worldCity == null) {
			LogHelper.CONFIG_LOGGER.error("cityId = " + cityId + " is null!");
			return resPos;
		}
		return mapInfo.getRandomCityPos(worldCity);
	}

	// 检查矿是否被采集完
	// 采集时间=采集点资源总量/采集点采集速度
	public Award caculateResCount(March march, Resource resource, long period, Player player, boolean isBreak) {
		if (resource == null) {
			LogHelper.CONFIG_LOGGER.error("resource is null, caculateResCount = 0");
			return null;
		}
		// 检查矿是否被采集完
		// 采集时间=采集点资源总量/采集点采集速度
		int resourceId = (int) resource.getId();
		// 剩余数量
		long count = resource.getCount();
		StaticWorldResource config = staticWorldMgr.getStaticWorldResource(resourceId);
		if (config == null) {
			LogHelper.CONFIG_LOGGER.error("config is null, no resourceId found = " + resourceId);
			return null;
		}

		// 采集速度个数/秒
		period = Math.max(0, period);
		long collectCount = 0;
		if (config.getType() == 5) {
			// 计算小时数
			int hours = (int) (period / TimeHelper.HOUR_MS);
			collectCount = (long) (Math.floor((float) hours * (float) config.getSpeed())); // 一定要向下取整防止刷资源
		} else {
			float speed = (float) config.getSpeed() / (float) TimeHelper.HOUR_MS;
			collectCount = (long) (Math.floor((float) period * speed)); // 一定要向下取整防止刷资源
		}

		count = Math.min(count, config.getResource());
		collectCount = Math.min(count, collectCount);
		if (march.isCollectDone() && !isBreak) { // 没有被中断
			collectCount = resource.getCount();
		}

		// 增加award
		int id = config.getType();
		int awardType = AwardType.RESOURCE;
		// 金币
		if (id == 5) {
			awardType = AwardType.GOLD;
			id = 0;
		}

		// 采集加成
		double techAddRes = techManager.getCollectSpeed(player);
		// LogHelper.GAME_DEBUG.error("techAddRes add = " + techAddRes);

		double addFactor = (double) march.getAddFactor() / 100.0f;

		// LogHelper.GAME_DEBUG.error("season add = " + season);
		double totalAdd = techAddRes + addFactor + 1.0;
		if (awardType == AwardType.GOLD) {
			totalAdd = 1.0; // 金币没有加成
		}

		long total = (long) (totalAdd * (double) collectCount);
		Award award = new Award(0, awardType, id, (int) total);
		// LogHelper.GAME_DEBUG.error("caculate ResCount collect total = " +
		// total);
		long left = count - collectCount;
		left = Math.max(0, left);
		resource.setCount(left);

		return award;
	}

	public int getInitMapId(int country) {
		if (isMapFull()) {
			return selectMinMapId(country);
		} else {
			return selectPrimaryMapId(country);
		}
	}

	public int getBronMapId(int country) {
		if (isBornMapFull()) {
			return selectBornMinMapId(country);
		} else {
			return selectBornMapId(country);
		}
	}

	public boolean isBornMapFull() {
		int limitNum = staticLimitMgr.getNum(112);
		boolean isFull = true;
		for (StaticWorldMap staticWorldMap : bornMaps) {
			MapInfo mapInfo = worldMapInfo.get(staticWorldMap.getMapId());
			if (mapInfo == null) {
				continue;
			}
			int totalNum = 0;
			ConcurrentHashMap<Integer, Integer> mapPlayerNum = mapInfo.getMapPlayerNum();
			for (Map.Entry<Integer, Integer> entry : mapPlayerNum.entrySet()) {
				totalNum += entry.getValue();
			}

			if (totalNum < limitNum) {
				isFull = false;
				break;
			}
		}

		return isFull;
	}

	public boolean isMapFull() {
		int limitNum = staticLimitMgr.getNum(112);
		boolean isFull = true;
		for (Integer mapId : mapIds) {
			MapInfo mapInfo = worldMapInfo.get(mapId);
			if (mapInfo == null) {
				continue;
			}
			int totalNum = 0;
			ConcurrentHashMap<Integer, Integer> mapPlayerNum = mapInfo.getMapPlayerNum();
			for (Map.Entry<Integer, Integer> entry : mapPlayerNum.entrySet()) {
				totalNum += entry.getValue();
			}

			if (totalNum < limitNum) {
				isFull = false;
				break;
			}
		}

		return isFull;
	}

	public int selectPrimaryMapId(int tragetCountry) {
		int limitNum = staticLimitMgr.getNum(112);
		for (Integer mapId : mapIds) {
			MapInfo mapInfo = worldMapInfo.get(mapId);
			if (mapInfo == null) {
				continue;
			}
			ConcurrentHashMap<Integer, Integer> mapPlayerNum = mapInfo.getMapPlayerNum();
			int totalNum = 0;
			for (Map.Entry<Integer, Integer> entry : mapPlayerNum.entrySet()) {
				totalNum += entry.getValue();
			}
			if (totalNum < limitNum) {
				return mapId;
			}
		}

		return 1;

	}

	public int selectBornMapId(int tragetCountry) {
		int limitNum = staticLimitMgr.getNum(112);

		for (StaticWorldMap staticWorldMap : bornMaps) {
			MapInfo mapInfo = worldMapInfo.get(staticWorldMap.getMapId());
			if (mapInfo == null) {
				continue;
			}

			ConcurrentHashMap<Integer, Integer> mapPlayerNum = mapInfo.getMapPlayerNum();
			int totalNum = 0;
			for (Map.Entry<Integer, Integer> entry : mapPlayerNum.entrySet()) {
				totalNum += entry.getValue();
			}

			if (totalNum >= limitNum) {
				continue;
			}
			return mapInfo.getMapId();
		}

		if (!bornMaps.isEmpty()) {
			int index = RandomHelper.randomInSize(bornMaps.size());
			return bornMaps.get(index).getMapId();
		}

		// 所有出生地图都一样了，则又发送第一个地图
		return 1;
	}

	public int selectBornMinMapId(int targetCountry) {
		int minMapId = 0;
		int maxValue = Integer.MAX_VALUE;

		for (StaticWorldMap staticWorldMap : bornMaps) {
			MapInfo mapInfo = worldMapInfo.get(staticWorldMap.getMapId());
			if (mapInfo == null) {
				continue;
			}
			ConcurrentHashMap<Integer, Integer> mapPlayerNum = mapInfo.getMapPlayerNum();
			int total = 0;
			for (Integer num : mapPlayerNum.values()) {
				total += num;
			}
			if (total < maxValue) {
				maxValue = total;
				minMapId = staticWorldMap.getMapId();
			}
		}
		return minMapId;
	}

	public int selectMinMapId(int targetCountry) {
		int minMapId = 0;
		int maxValue = Integer.MAX_VALUE;

		for (Integer mapId : mapIds) {
			MapInfo mapInfo = worldMapInfo.get(mapId);
			if (mapInfo == null) {
				continue;
			}
			ConcurrentHashMap<Integer, Integer> mapPlayerNum = mapInfo.getMapPlayerNum();
			int total = 0;
			for (Integer num : mapPlayerNum.values()) {
				total += num;
			}
			if (total < maxValue) {
				maxValue = total;
				minMapId = mapId;
			}
		}
		return minMapId;
	}

	// 玩家切换地图[隐藏实现细节]
	public PlayerCity changePlayerPos(Player player, Pos newPos) {
		int newMapId = getMapId(newPos);
		MapInfo newMapInfo = getMapInfo(newMapId);
		PlayerCity playerCity = addPlayerCity(newPos, newMapInfo, player);
		if (playerCity == null) {
			return null;
		}
		newMapInfo.updatePlayerNum(player.getCountry(), 1);
		removePlayerCity(player);
		Pos pos = player.initNewPos(newPos);
		int mapId = worldManager.getMapId(newPos);
		player.getLord().setMapId(mapId);

		int oldMapId = worldManager.getMapId(pos);
		if (mapId != oldMapId) {
			LogicServer mainLogicServer = GameServer.getInstance().mainLogicServer;
			if (mainLogicServer != null) {
				mainLogicServer.addCommand(() -> {
					worldManager.sendWar(player);
				});
			}
		}
		player.getLord().setCity(0);
		if (mapId == MapId.CENTER_MAP_ID) {
			int cityIdByPos = staticWorldMgr.getCityIdByPos(player.getPos());
			player.getLord().setCity(cityIdByPos);
		}
		eventManager.record_userInfo(player, EventName.player_move);
		return playerCity;
	}

	@Autowired
	EventManager eventManager;

	//// 玩家位置改变，坐标也会发生改变
	//public void changePlayerPos(Player player, Pos newPos) {
	//
	//}

	// 玩家被击飞，驻防武将会回程
	public void handleWallFriendReturn(Player target) {
		Wall wall = target.getWall();
		if (wall == null) {
			return;
		}
		Map<Integer, WallFriend> wallFriends = wall.getWallFriends();
		Iterator<WallFriend> iterator = wallFriends.values().iterator();
		while (iterator.hasNext()) {
			WallFriend wallFriend = iterator.next();
			if (wallFriend == null) {
				continue;
			}
			long lordId = wallFriend.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			March march = player.getMarch(wallFriend.getMarchId());
			if (march == null) {
				continue;
			}

			marchManager.doMarchReturn(march, player, MarchReason.WallAssistCancelMarch);
			// 发送邮件
			// 给targetId发出遣返邮件
			// 您在%s的基地驻防的将领%s, 由于基地被击飞或迁移，开始返回。
			String name = String.valueOf(target.getNick());
			String heroName = String.valueOf(wallFriend.getHeroId());
			playerManager.sendNormalMail(player, MailId.WALL_MARCH_RETURN, name, heroName);
			iterator.remove();
		}

		// 通知target删除城防军
		playerManager.synWallInfo(target);
	}

	public boolean isWorldBossKilled(int country) {
		WorldData worldData = getWolrdInfo();
		if (worldData == null) {
			LogHelper.CONFIG_LOGGER.error("worldData is null");
			return false;
		}

		WorldBoss worldBoss = worldData.getWorldBoss(country);
		if (worldBoss == null) {
			LogHelper.CONFIG_LOGGER.error("worldBoss is null!");
			return false;
		}

		return worldBoss.getSoldier() <= 0;

	}

	// 玩家 + 禁卫军
	public int getWarAttackNum(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> marches = warInfo.getAttackMarches();
		int marchesSoldier = getMarchesSolderNum(marches);
		int squareSoldier = getSquareSoldier(warInfo);
		int total = marchesSoldier + squareSoldier;
		return total;
	}

	// 玩家、城池守卫[国战]
	public int getDefenceNum(WarInfo warInfo) {
		int citySoldier = getCitySoldier((int) warInfo.getDefencerId());
		ConcurrentLinkedDeque<March> marches = warInfo.getDefenceMarches();
		int marchesSoldier = getMarchesSolderNum(marches);
		int defenceSoldierNum = getDefenceSoldierNum(warInfo.getDefencerId());
		int total = marchesSoldier + citySoldier + defenceSoldierNum;
		return total;
	}

	public int getRiotNum(WarInfo warInfo) {
		if (warInfo.getWarType() != WarType.RIOT_WAR) {
			return 0;
		}

		int attackerId = (int) warInfo.getAttackerId();
		StaticWorldMonster worldMonster = staticWorldMgr.getMonster(attackerId);
		if (worldMonster == null) {
			LogHelper.CONFIG_LOGGER.error("worldMonster is null.");
			return 0;
		}
		List<Integer> monsterIds = worldMonster.getMonsterIds();
		if (monsterIds == null || monsterIds.isEmpty()) {
			LogHelper.CONFIG_LOGGER.error("monsterIds is null or empty.");
			return 0;
		}

		int total = 0;
		for (Integer monsterId : monsterIds) {
			StaticMonster monster = staticMonsterMgr.getStaticMonster(monsterId);
			if (monster != null) {
				total += monster.getSoldierCount();
			}
		}
		long defenceId = warInfo.getDefencerId();
		Player defencerPlayer = playerManager.getPlayer(defenceId);
		if (defencerPlayer != null) {
			SimpleData simpleData = defencerPlayer.getSimpleData();
			if (simpleData != null) {
				if (simpleData.getRiotBuff() != null) {
					Integer lessRoops = simpleData.getRiotBuff().get(RiotBuff.LESSTROOPS);
					lessRoops = lessRoops == null ? 0 : lessRoops;
					float lessSoldierNum = total * (lessRoops / 100f);
					return Math.round(total - lessSoldierNum);
				}
			}
		}
		return total;
	}

	public int getRebelMonsterNum(WarInfo warInfo) {
		if (warInfo.getWarType() != WarType.REBEL_WAR) {
			return 0;
		}

		int defencerId = (int) warInfo.getDefencerId();
		StaticWorldMonster worldMonster = staticWorldMgr.getMonster(defencerId);
		if (worldMonster == null) {
			LogHelper.CONFIG_LOGGER.error("worldMonster is null.");
			return 0;
		}
		List<Integer> monsterIds = worldMonster.getMonsterIds();
		if (monsterIds == null || monsterIds.isEmpty()) {
			LogHelper.CONFIG_LOGGER.error("monsterIds is null or empty.");
			return 0;
		}

		int total = 0;
		for (Integer monsterId : monsterIds) {
			StaticMonster monster = staticMonsterMgr.getStaticMonster(monsterId);
			if (monster != null) {
				total += monster.getSoldierCount();
			}
		}
		return total;
	}

	public int getBigMonsterNum(WarInfo warInfo) {
		int mapId = worldManager.getMapId(warInfo.getDefencerPos());
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			return 0;
		}
		BigMonster bigMonster = mapInfo.getBigMonsterMap().get(warInfo.getDefencerPos());
		if (bigMonster == null) {
			return 0;
		}
		return bigMonster.getTeam().getLessSoldier();
	}

	public int getMarchesSolderNum(ConcurrentLinkedDeque<March> marches) {
		int total = 0;
		if (marches == null) {
			return 0;
		}

		for (March march : marches) {
			total += getMarchSoldier(march);
		}

		return total;
	}

	public int getMarchSoldier(March march) {
		int marchSoldier = 0;
		long lordId = march.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return 0;
		}
		List<Integer> heroIds = march.getHeroIds();
		for (Integer heroId : heroIds) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}

			marchSoldier += hero.getCurrentSoliderNum();
		}
		return marchSoldier;
	}

	public int getSquareSoldier(WarInfo warInfo) {
		Map<Integer, SquareMonster> monsters = warInfo.getMonsters();
		int totalSoldierNum = 0;
		for (SquareMonster monster : monsters.values()) {
			if (monster == null) {
				continue;
			}
			StaticSquareMonster config = staticWorldMgr.getSquareMonster(monster.getMonsterLv());
			if (config == null) {
				LogHelper.CONFIG_LOGGER.error("square monster is null, lv = " + monster.getMonsterLv());
				continue;
			}

			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(config.getMonsters());
			if (staticMonster == null) {
				LogHelper.CONFIG_LOGGER.error("staticMonster config error, monsteId = " + config.getMonsters());
				continue;
			}

			totalSoldierNum += staticMonster.getSoldierCount();
		}

		return totalSoldierNum;
	}

	public int getCitySoldier(int cityId) {
		CityMonster cityMonster = cityManager.getCityMonster(cityId);
		if (cityMonster == null) {
			// LogHelper.CONFIG_LOGGER.error("cityMonster is null");
			return 0;
		}
		Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
		if (monsterInfoMap == null) {
			LogHelper.CONFIG_LOGGER.error("monsterInfoMap is null");
			return 0;
		}

		int total = 0;
		for (CityMonsterInfo info : monsterInfoMap.values()) {
			total += info.getSoldier();
		}

		// LogHelper.GAME_DEBUG.error("getCitySoldier total is null");

		return total;
	}

	public int getDefenceSoldierNum(long lordId) {
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return 0;
		}
		List<Integer> heroIds = player.getEmbattleList();
		int soldierNum = 0;
		// 玩家驻防
		for (Integer heroId : heroIds) {
			Hero hero = player.getHero(heroId);
			if (hero == null || player.isInMarch(hero) || player.isInMass(heroId) || player.hasPvpHero(heroId)) {
				continue;
			}
			soldierNum += hero.getCurrentSoliderNum();
		}

		// 玩家城防军
		Wall wall = player.getWall();
		for (WallDefender defender : wall.getWallDefenders().values()) {
			if (defender == null) {
				continue;
			}
			soldierNum += defender.getSoldierNum();
		}
		// 友军驻防
		for (WallFriend wallFriend : wall.getWallFriends().values()) {
			if (wallFriend == null) {
				continue;
			}
			March march = wallFriend.getMarch();
			if (march == null) {
				continue;
			}
			soldierNum += getMarchSoldier(march);
		}
		// 城防武将
		for (WarDefenseHero warDefenseHero : player.getDefenseArmyList()) {
			int heroId = warDefenseHero.getHeroId();
			Hero hero = player.getHero(heroId);
			if (hero == null || player.isInMarch(hero) || player.isInMass(heroId) || player.hasPvpHero(heroId)) {
				continue;
			}
			soldierNum += hero.getCurrentSoliderNum();
		}
		return soldierNum;
	}

	public void handleWarSoldier(WarInfo warInfo) {
		warInfo.setAttackSoldierNum(getWarAttackNum(warInfo));
		warInfo.setDefenceSoldierNum(getDefenceNum(warInfo));
	}

	public void handleRebelWarSoldier(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> marches = warInfo.getAttackMarches();
		int marchesSoldier = getMarchesSolderNum(marches);
		warInfo.setAttackSoldierNum(marchesSoldier);

		int defencerNum = getRebelMonsterNum(warInfo);
		warInfo.setDefenceSoldierNum(defencerNum);
	}

	public void handleBigMonsterWarSoldier(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> marches = warInfo.getAttackMarches();
		int marchesSoldier = getMarchesSolderNum(marches);
		warInfo.setAttackSoldierNum(marchesSoldier);

		int defencerNum = getBigMonsterNum(warInfo);
		warInfo.setDefenceSoldierNum(defencerNum);
	}

	public void handleZergWarSoldier(WarInfo warInfo) {
		if (warInfo.getWarType() == WarType.ATTACK_ZERG) {// 进攻类型战斗
			warInfo.setAttackSoldierNum(getWarAttackNum(warInfo));// 进攻为玩家
//            Team team = zergManager.getTeam((int) warInfo.getDefencerId());
//            warInfo.setDefenceSoldierNum(team.getCurSoldier());//防守为怪物
		} else {//防守类型战斗
//            Team team = zergManager.getTeam((int) warInfo.getAttackerId());// 进攻为怪物
//            warInfo.setAttackSoldierNum(team.getCurSoldier());
			warInfo.setDefenceSoldierNum(getDefenceNum(warInfo));//防守为玩家
		}
	}
	// 删除一个玩家当前所有的战斗
//    public void removeAllWar(Player player) {
//        int mapId = getMapId(player);
//        MapInfo mapInfo = getMapInfo(mapId);
//        if (mapInfo == null) {
//            LogHelper.CONFIG_LOGGER.error("mapInfo is null!");
//            return;
//        }
//
//        long lordId = player.roleId;
//        // 闪电
//        Iterator<WarInfo> quickIter = mapInfo.getQuickWarMap().values().iterator();
//        while (quickIter.hasNext()) {
//            WarInfo warInfo = quickIter.next();
//            if (warInfo == null) {
//                continue;
//            }
//
//            if (warInfo.getDefencerId() == lordId) {
//                quickIter.remove();
//
//                worldManager.flushWar(warInfo,false,warInfo.getAttackerCountry());
//                worldManager.flushWar(warInfo,false,warInfo.getDefencerCountry());
//
//            }
//        }
//
//        // 远征和奔袭
//        Iterator<WarInfo> cityWarIter = mapInfo.getCityWarMap().values().iterator();
//        while (cityWarIter.hasNext()) {
//            WarInfo warInfo = cityWarIter.next();
//            if (warInfo == null) {
//                continue;
//            }
//
//            if (warInfo.getDefencerId() == lordId) {
//                cityWarIter.remove();
//                worldManager.flushWar(warInfo,false,warInfo.getAttackerCountry());
//                worldManager.flushWar(warInfo,false,warInfo.getDefencerCountry());
//            }
//        }
//
//    }

	/**
	 * 避免跨区域战斗找不到的情况
	 *
	 * @param warId
	 * @return
	 */
	public WarInfo getPvpWarInfo(long warId) {
		for (MapInfo mapInfo : getWorldMapInfo().values()) {
//			Map<Long, WarInfo> cityWarMap = mapInfo.getCityWarMap();
//			WarInfo cityWarInfo = cityWarMap.get(warId);
			WarInfo cityWarInfo = (WarInfo) mapInfo.getWar(warId);
			if (cityWarInfo != null) {
				return cityWarInfo;
			}
//			Map<Long, WarInfo> quickWarMap = mapInfo.getQuickWarMap();
//			WarInfo quickWarInfo = quickWarMap.get(warId);
//			if (quickWarInfo != null) {
//				return quickWarInfo;
//			}
		}
		return null;
	}

	// get pvp war info
	public WarInfo getPvpWarInfo(MapInfo mapInfo, long warId) {
		// 检查当前pos是攻击还是防守
		return (WarInfo) mapInfo.getWarMap().get(warId);
//		Map<Long, WarInfo> cityWarMap = mapInfo.getCityWarMap();
//		WarInfo cityWarInfo = cityWarMap.get(warId);
//		if (cityWarInfo != null) {
//			return cityWarInfo;
//		}
//
//		Map<Long, WarInfo> quickWarMap = mapInfo.getQuickWarMap();
//		WarInfo quickWarInfo = quickWarMap.get(warId);
//		if (quickWarInfo != null) {
//			return quickWarInfo;
//		}
//		return null;
	}

	public void synPlayerFlyMarch(Player target, March march, int mapId) {
		int targetMapId = getMapId(target);
		if (targetMapId == mapId) {
			return;
		}

		WorldPb.SynMarchRq.Builder builder = WorldPb.SynMarchRq.newBuilder();
		builder.setMarch(wrapMarchPb(march));
		playerManager.synMarchToPlayer(target, builder.build());
	}

	public void synMarchToPlayer(Player target, March march) {
		WorldPb.SynMarchRq.Builder builder = WorldPb.SynMarchRq.newBuilder();
		builder.setMarch(wrapMarchPb(march));
		playerManager.synMarchToPlayer(target, builder.build());
	}

	public WorldPb.SynMarchRq createSynMarchRq(March march) {
		WorldPb.SynMarchRq.Builder builder = WorldPb.SynMarchRq.newBuilder();
		builder.setMarch(wrapMarchPb(march));
		return builder.build();
	}

	public WorldPb.SynMarchRq createSynRemoveMarchRq(March march) {
		WorldPb.SynMarchRq.Builder builder = WorldPb.SynMarchRq.newBuilder();
		builder.setRemove(wrapMarchPb(march));
		return builder.build();
	}


	public CommonPb.March.Builder wrapMarchPb(March march) {
		CommonPb.March.Builder builder = CommonPb.March.newBuilder();
		if (march == null) {
			return builder;
		}
		builder.setLordId(march.getLordId());
		builder.setState(march.getState());
		builder.setStartPos(march.getStartPos().wrapPb());
		builder.setEndPos(march.getEndPos().wrapPb());
		builder.setEndTime(march.getEndTime());
		builder.setPeriod(march.getPeriod());
		List<Integer> heroIds = march.getHeroIds();
		for (Integer heroId : heroIds) {
			builder.addHeroId(heroId);
		}

		List<Award> awards = march.getAwards();
		for (Award award : awards) {
			builder.addAwards(award.wrapPb());
		}

		builder.setCountry(march.getCountry());
		builder.setKeyId(march.getKeyId());
		builder.setMarchType(march.getMarchType());
		builder.setFightTime(march.getFightTime());
		builder.setAttackerId(march.getAttackerId());
		builder.setWarId(march.getWarId());
		builder.setSide(march.getSide());
		builder.setAssistId(march.getAssistId());
		long lordId = march.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player != null && player.getNick() != null) {
			builder.setLordName(player.getNick());
		}

		return builder;
	}

	// 只删除坐标
//	public void removeMonsterPos(MapInfo mapInfo, Pos pos) {
//		if (mapInfo == null) {
//			return;
//		}
//
//		mapInfo.removeMonsterPos(pos);
//	}

	public boolean isBoss2Killed() {
		WorldData worldData = getWolrdInfo();
		WorldBoss shareBoss = worldData.getShareBoss();
		if (shareBoss == null) {
			return false;
		}
		boolean isKilled = shareBoss.getSoldier() <= 0;
		return isKilled;
	}

	public boolean isBoss1Killed() {
		WorldData worldData = getWolrdInfo();
		if (worldData == null) {
			LogHelper.CONFIG_LOGGER.error("worldData is null");
			return false;
		}

		for (int country = 1; country <= 3; country++) {
			WorldBoss worldBoss = worldData.getWorldBoss(country);
			if (worldBoss == null) {
				LogHelper.CONFIG_LOGGER.error("worldBoss is null!");
				return false;
			}

			if (worldBoss.getSoldier() <= 0) {
				return true;
			}
		}

		return false;
	}

	// 处理防守方兵力恢复
	public void soldierAutoAdd(Player player) {
		List<Integer> targetHeros = player.getEmbattleList();
		ArrayList<Integer> freeHeros = new ArrayList<Integer>();
		for (Integer heroId : targetHeros) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}

			// 当前英雄状态
			if (!playerManager.isHeroFree(player, heroId)) {
				continue;
			}
			freeHeros.add(heroId);
		}
		soldierManager.autoAdd(player, freeHeros);
	}

	// 获得当前地图城池历史个数
	public int getCityNum(int cityType, Player player) {
		int country = player.getCountry();
		int mapId = getMapId(player);
		if (mapId == 0) {
			return 0;
		}

		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			return 0;
		}

		Map<Integer, HashSet<Integer>> cityRecord = mapInfo.getCityIdRecord();
		if (cityRecord == null) {
			return 0;
		}

		HashSet<Integer> ctRecord = cityRecord.get(country);
		if (ctRecord == null) {
			return 0;
		}

		int num = 0;
		for (Integer cityId : ctRecord) {
			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (worldCity.getType() == cityType) {
				num += 1;
			}
		}
		return num;
	}

	public void updateCityNum(int country, int mapId, int cityId) {
		if (mapId == 0) {
			return;
		}

		if (country == 0) {
			return;
		}

		if (country < 1 || country > 3) {
			LogHelper.CONFIG_LOGGER.error("country < 1 || country > 3, country = " + country);
			return;
		}

		MapInfo mapInfo = getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapId error, mapId = " + mapId);
			return;
		}

		// 1,1,1
		Map<Integer, HashSet<Integer>> cityRecord = mapInfo.getCityIdRecord();
		if (cityRecord == null) {
			LogHelper.CONFIG_LOGGER.error("cityRecord == null...");
			return;
		}

		HashSet<Integer> ctRecord = cityRecord.get(country);
		if (ctRecord == null) {
			ctRecord = new HashSet<Integer>();
			cityRecord.put(country, ctRecord);
		}

		ctRecord.add(cityId);
	}

	// 检查城池信息
	public void checkCountryCity() {
		ConcurrentHashMap<Integer, City> cityMap = cityManager.getCityMap();
		for (City city : cityMap.values()) {
			if (city.getCountry() == 0) {
				continue;
			}
			StaticWorldCity worldCity = staticWorldMgr.getCity(city.getCityId());
			if (worldCity == null) {
				continue;
			}
			updateCityNum(city.getCountry(), worldCity.getMapId(), city.getCityId());
		}
	}

	/**
	 * 反序列化世界目标任务
	 *
	 * @param worldTargetTask
	 * @param worldData
	 */
	public void desrWorldTargetTask(byte[] worldTargetTask, WorldData worldData) {
		if (worldTargetTask == null) {
			// TODO 这里要加入世界目标开启的条件
			StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.getFirstWorldTarget();
			worldTargetTaskService.openWorldTarget(staticWorldNewTarget.getTargetId());
		} else {
			try {
				int target = 1;
				CommonPb.WorldTaget worldTargetPb = CommonPb.WorldTaget.parseFrom(worldTargetTask);
				for (CommonPb.WorldTargetTask worldTargetTaskPb : worldTargetPb.getWorldTargetTaskList()) {
					WorldTargetTask worldTarget = new WorldTargetTask();
					worldTarget.setCurHp(worldTargetTaskPb.getCurHp());
					worldTarget.setTaskId(worldTargetTaskPb.getTaskId());
					worldTarget.setNum(worldTargetTaskPb.getNum());
					worldTarget.setCount(worldTargetTaskPb.getCount());
					worldTarget.setOpenTime(worldTargetTaskPb.getOpenTime());
					worldTarget.setComplete(worldTargetTaskPb.getComplete());
					worldTargetTaskPb.getCountryProcessList().forEach(countryTaskProcess -> {
						CountryTaskProcess process = new CountryTaskProcess();
						process.setCountryId(countryTaskProcess.getCountryId());
						process.setLossSoldier(countryTaskProcess.getLossSoldier());
						process.setLastRefreshTime(countryTaskProcess.getLastRefreshTime());
						process.setPoints(countryTaskProcess.getProcess());
						process.setArea(countryTaskProcess.getArea());
						if (countryTaskProcess.getArea() != 0) {
							worldTarget.getPross().put(process.getArea(), process.getCountryId(), process);
						} else {
							worldTarget.getProcess().put(process.getCountryId(), process);
						}
					});
					worldTargetTaskPb.getRankInfoList().forEach(x -> {
						Player player = playerManager.getPlayer(x.getRoleId());
						if (player != null) {
							WorldHitRank rank = new WorldHitRank(player, x.getHit(), x.getTotalHit(), x.getTime());
							worldTarget.getHitRank().put(player.roleId, rank);
						}
					});
					worldTarget.rank();
					worldData.getTasks().put(worldTarget.getTaskId(), worldTarget);
					if (worldTarget.getTaskId() > target) {
						target = worldTarget.getTaskId();
					}
				}
				worldData.setTarget(target);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("desrWorldTargetTask error", e);
			}
		}
	}

	public void desrWorldActPlan(byte[] WorldActPlan, WorldData worldData) {
		try {
			SerializePb.SerWorldActPlanData serWorldActPlanData = SerializePb.SerWorldActPlanData.parseFrom(WorldActPlan);
			serWorldActPlanData.getSerWorldActPlanList().forEach(serWorldActPlan -> {
				WorldActPlan worldActPlan = new WorldActPlan();
				worldActPlan.setId(serWorldActPlan.getWorldActPlan().getId());
				worldActPlan.setState(serWorldActPlan.getWorldActPlan().getState());
				worldActPlan.setOpenTime(serWorldActPlan.getWorldActPlan().getOpenTime());
				worldActPlan.setPreheatTime(serWorldActPlan.getWorldActPlan().getPreheatTime());
				worldActPlan.setTargetSuccessTime(serWorldActPlan.getTargetSuccessTime());
				worldActPlan.setEndTime(serWorldActPlan.getWorldActPlan().getEndTime());
				worldActPlan.setEnterTime(serWorldActPlan.getWorldActPlan().getEnterTime());
				worldActPlan.setExhibitionTime(serWorldActPlan.getWorldActPlan().getExhibitionTime());

				StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(serWorldActPlan.getWorldActPlan().getId());
				LogHelper.GAME_LOGGER.info("【{} 加载】 活动状态:{} 开启时间:{} 结束时间:{}", staticWorldActPlan.getName(), worldActPlan.getState(), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));

				worldData.getWorldActPlans().put(worldActPlan.getId(), worldActPlan);
			});
		} catch (Exception e) {
			logger.error("desrWorldActPlan error", e);
		}
	}

	@Autowired
	StaticWorldActPlanMgr staticWorldActPlanMgr;

	/**
	 * 拿到某个活动
	 *
	 * @param activityId
	 * @return
	 */
	public WorldActPlan getWorldActPlan(int activityId) {
		WorldData worldData = getWolrdInfo();
		return worldData.getWorldActPlans().get(activityId);
	}

	public boolean isLimit(Player player, List<Award> gotAward) {
		if (player == null) {
			return false;
		}
		Ware ware = player.getWare();
		int wareLv = ware.getLv();
		StaticWare staticWare = staticBuildingMgr.getStaticWare(wareLv);
		// 当前可以抢夺的资源的上限
		int ironLimit = 0;
		int copperLimit = 0;
		int oilLimit = 0;
		if (staticWare != null) {
			// 当前可以容纳的资源的上限
			ironLimit = Math.round(staticWare.getIron() * Math.max(2, 4 - wareLv / 10f));
			copperLimit = Math.round(staticWare.getCopper() * Math.max(2, 4 - wareLv / 10f));
			oilLimit = Math.round(staticWare.getOil() * Math.max(2, 4 - wareLv / 10f));
		}
		for (Award award : gotAward) {
			if (award.getType() == AwardType.RESOURCE) {
				switch (award.getId()) {
					case ResourceType.IRON:
						if (ironLimit < award.getCount()) {
							return true;
						}
						break;
					case ResourceType.COPPER:
						if (copperLimit < award.getCount()) {
							return true;
						}
						break;
					case ResourceType.OIL:
						if (oilLimit < award.getCount()) {
							return true;
						}
						break;
				}
			}
		}
		return false;
	}

	/**
	 * 根据仓库等级上限计算能抢夺的最大资源
	 */
	public List<Award> getRobBuyWareHouser(Player player, List<Award> gotAward) {
		List<Award> resultAward = new ArrayList<>();
		Ware ware = player.getWare();
		int wareLv = ware.getLv();
		StaticWare staticWare = staticBuildingMgr.getStaticWare(wareLv);
		// 当前可以抢夺的资源的上限
		int ironLimit = 0;
		int copperLimit = 0;
		int oilLimit = 0;
		if (staticWare != null) {
			// 当前可以容纳的资源的上限
			ironLimit = Math.round(staticWare.getIron() * Math.max(2, 4 - wareLv / 10f));
			copperLimit = Math.round(staticWare.getCopper() * Math.max(2, 4 - wareLv / 10f));
			oilLimit = Math.round(staticWare.getOil() * Math.max(2, 4 - wareLv / 10f));
		}
		for (Award award : gotAward) {
			if (award.getType() == AwardType.RESOURCE) {
				switch (award.getId()) {
					case ResourceType.IRON:
						if (ironLimit < award.getCount()) {
							award.setCount(ironLimit);
						}
						break;
					case ResourceType.COPPER:
						if (copperLimit < award.getCount()) {
							award.setCount(copperLimit);
						}
						break;
					case ResourceType.OIL:
						if (oilLimit < award.getCount()) {
							award.setCount(oilLimit);
						}
						break;
				}
			}
			resultAward.add(award);
		}
		return resultAward;
	}

	// 抢夺人口+资源, 通过邮件来同步
	public List<Award> getLostMax(List<Award> robeAward) {
		List<Award> list = new ArrayList<>();
		for (Award award : robeAward) {
			Award tmpAward = new Award();
			tmpAward.setId(award.getId());
			tmpAward.setKeyId(award.getKeyId());
			tmpAward.setType(award.getType());
			tmpAward.setCount(award.getCount());
			if (award.getType() == AwardType.RESOURCE) {
				int count = Math.round(award.getCount() * 1.5f);
				tmpAward.setCount(count);
			}
			list.add(tmpAward);
		}
		return list;
	}

	// 抢夺人口+资源, 通过邮件来同步
	public List<Award> getLostMax(Player target, List<Award> robeAward) {
		Ware ware = target.getWare();
		int wareLv = ware.getLv();
		StaticWare staticWare = staticBuildingMgr.getStaticWare(wareLv);

		// 当前可以容纳的资源的上限
		long ironLimit = 0;
		long copperLimit = 0;
		long oilLimit = 0;
		if (staticWare != null) {
			// 当前可以容纳的资源的上限
			ironLimit = staticWare.getIron();
			copperLimit = staticWare.getCopper();
			oilLimit = staticWare.getOil();
		}

		long iron = target.getIron();
		long copper = target.getCopper();
		long oil = target.getOil();
		List<Award> list = new ArrayList<>();
		for (Award award : robeAward) {
			if (award.getType() == AwardType.RESOURCE) {
				int count = award.getCount();
				switch (award.getId()) {
					case 1:
						if (count >= (iron - ironLimit)) {
							count = (int) (iron - ironLimit);
						}
						break;
					case 2:
						if (count >= (copper - copperLimit)) {
							count = (int) (copper - copperLimit);
						}
						break;
					case 3:
						if (count >= (oil - oilLimit)) {
							count = (int) (oil - oilLimit);
						}
						break;
				}
				count = Math.max(0, count);
				award.setCount(count);
			}
			list.add(award);
		}
		return list;
	}

	/**
	 * 加载抢夺名城数据
	 */
	private void loadStealCity() throws Exception {
		WorldData data = worldData.get(1);
		if (null == data) {
			return;
		}
		String stealCity = data.getStealCity();
		if (stealCity == null) {
			return;
		}

		String[] split = stealCity.split(";");
		if (split.length != 2) {
			return;
		}

		Map stealCityMap = JSONObject.parseObject(split[0], Map.class);
		if (stealCityMap.size() > 0) {
			for (Object obj : stealCityMap.keySet()) {
//                    System.out.println("stealCityMap>>>>>>>>>key为：" + obj + "值为：" + stealCityMap.get(obj));
				Integer key = (Integer) obj;
				Object value = stealCityMap.get(obj);
				TreeSet<Integer> treeSet = new TreeSet<Integer>();
				if (null != value) {
					treeSet = new TreeSet<Integer>((Collection<? extends Integer>) value);
				}
				stealCityMap.put(key, treeSet);
			}
		}
		if (stealCityMap.size() > 0) {
			stealCityManager.selectdMap = stealCityMap;
		}

		Map receiveAwardMap = JSONObject.parseObject(split[1], Map.class);
		if (receiveAwardMap.size() > 0) {
			for (Object obj : receiveAwardMap.keySet()) {
//                    System.out.println("receiveAwardMap>>>>>>>>>key为：" + obj + "值为：" + receiveAwardMap.get(obj));
				Integer key = (Integer) obj;
				Object value = receiveAwardMap.get(obj);
				LinkedList<Integer> list = new LinkedList<Integer>();
				if (null != value) {
					list = new LinkedList<Integer>((Collection<? extends Integer>) value);
				}
				receiveAwardMap.put(key, list);
			}
		}
		if (receiveAwardMap.size() > 0) {
			stealCityManager.receiveAward = receiveAwardMap;
		}
	}


	/**
	 * 合服处理
	 */
	public void iniMergeServerPlayer() {
		Map<String, List<Player>> rename = playerManager.getPlayers().values().stream().collect(Collectors.groupingBy(Player::getNick));
		//StaticMailDataMgr staticMailDataMgr = SpringUtil.getBean(StaticMailDataMgr.class);
		List<Award> mailAward = staticMailDataMgr.getMailAward(MailId.MERGER_SERVER_COMPENSATE);
		//NickManager nickManager = SpringUtil.getBean(NickManager.class);
		for (Player player : playerManager.getPlayers().values()) {
			// 玩家状态为合服状态将随机地图位置 //合服状态 0:不为合服 1:合服
			if (!StringUtil.isNullOrEmpty(player.getLord().getNick()) && player.getLord().getMergeServerStatus() == 1) {
				// 随机迁城去平原地图
				randomMoveCity(player);
				// 通行证和七日登录需要处理一下
				Lists.newArrayList(ActivityConst.ACT_PASS_PORT, ActivityConst.ACT_LOGIN_SEVEN).forEach(e -> {
					activityManager.getActivityInfo(player, e);
				});
				checkMail(player);
				// 重命名改名,并发放改名卡
				List<Player> players = rename.get(player.getNick());
				if (!StringUtil.isNullOrEmpty(player.getLord().getNick()) && players != null && players.size() > 1) {
					nickManager.setPlayerNick(player.getLord(), nickManager.getNewNick());
					playerManager.sendAttachMail(player, mailAward, MailId.MERGER_SERVER_COMPENSATE);
				}
				player.getLord().setMergeServerStatus(0);
			}
		}
	}

	// 合服处理邮件
	public void checkMail(Player player) {
		if (player.getLord().getMergeServerStatus() != 1) {
			return;
		}
		Iterator<Mail> iterator = player.getMails().iterator();
		while (iterator.hasNext()) {
			Mail mail = iterator.next();
			// 奖励已领取
			if (mail.getAwardGot() == MailConst.AWARD_GOT) {
				iterator.remove();
				continue;
			}
			List<CommonPb.Award> awardList = mail.getAward();
			if (playerManager.isEquipFulls(awardList, player)) {
				continue;
			}
			for (CommonPb.Award award : awardList) {
				playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.MAIL_AWARD);
			}
			iterator.remove();
		}
	}

	// 随机用户位置(普通迁城)
	public void randomMoveCity(Player player) {
		int currentMapId = worldManager.getMapId(player);

		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		if (worldMap == null) {
			LogHelper.CONFIG_LOGGER.error("worldMap not config");
			return;
		}
		ArrayList<Integer> list = new ArrayList<>();
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			if (staticWorldMap.getAreaType() == 1) {
				list.add(staticWorldMap.getMapId());
			}
		}
		if (list.size() == 0) {
			return;
		}
		int randNum = RandomUtils.nextInt(0, list.size());
		MapInfo mapInfoRand = getMapInfo(list.get(randNum));
		if (mapInfoRand == null) {
			LogHelper.CONFIG_LOGGER.error("get mapInfo wrong!");
			return;
		}

		Pos pos = givePlayerPos(mapInfoRand);

		if (pos.isError()) {
			return;
		}
		// 去掉当前玩家的坐标
		MapInfo mapInfo = worldManager.getMapInfo(currentMapId);
		if (mapInfo == null) {
			return;
		}
		// 删除坐标
		// 更新playerCity
		Pos playerPos = player.getPos();
		worldManager.changePlayerPos(player, pos);

		// 驻防武将回城
		worldManager.handleWallFriendReturn(player);
		// 先回城
		worldManager.removePlayerWar(player, playerPos, MarchReason.LowMove, pos);

		// 开启48小时保护时间
		Lord lord = player.getLord();
		long now = System.currentTimeMillis();
		long period = staticLimitMgr.getNum(71) * TimeHelper.HOUR_MS;
		if (lord != null) {
			lord.setProtectedTime(now + period);
			synAllProtected(mapInfo.getMapId(), player);
		}
	}

	@Autowired
	LogUser logUser;

	// 同步奖励
	public void synRewards(March march) {
		long lordId = march.getLordId();
		// 找到玩家
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("return player is null!");
			return;
		}

		int marchType = march.getMarchType();
		int reason = 0;
		if (marchType == 1) {
			reason = Reason.KILL_WORLD_MONSTER;
		}

		List<Award> awards = march.getAwards();
		if (awards != null && !awards.isEmpty()) {
			playerManager.addAward(player, awards, reason);

			/**
			 * 攻打世界野怪资源产出日志埋点
			 */
			//com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			for (Award award : awards) {
				if (award.getType() == AwardType.RESOURCE) {
					if (marchType == MarchType.AttackMonster) {
						logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(award.getId()), RoleResourceLog.OPERATE_IN, award.getId(), ResOperateType.WORLD_MOSTER_IN.getInfoType(), award.getCount(), player.account.getChannel()));
						int type = 0;
						int resType = award.getId();
						switch (resType) {
							case ResourceType.IRON:
								type = IronOperateType.WORLD_MOSTER_IN.getInfoType();
								break;
							case ResourceType.COPPER:
								type = CopperOperateType.WORLD_MOSTER_IN.getInfoType();
								break;
							default:
								break;
						}
						if (type != 0) {
							logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, award.getCount(), type), resType);
						}

					} else if (marchType == MarchType.CollectResource || marchType == MarchType.SUPER_COLLECT) {
						logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(award.getId()), RoleResourceLog.OPERATE_IN, award.getId(), ResOperateType.WORLD_COLLECT_IN.getInfoType(), award.getCount(), player.account.getChannel()));
						int type = 0;
						int resType = award.getId();
						switch (resType) {
							case ResourceType.IRON:
								type = IronOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							case ResourceType.COPPER:
								type = CopperOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							case ResourceType.OIL:
								type = OilOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							case ResourceType.STONE:
								type = StoneOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							default:
								break;
						}
						if (type != 0) {
							logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, award.getCount(), type), resType);
						}
						// 更新资源采集活动
						activityManager.updCollectionResource(player, award);
					}
				}
			}
			// 同步玩家数据
			playerManager.synChange(player, 0);
		}
		march.setState(MarchState.Back);
		worldManager.synMarch(0, march);
	}

	public March createBroodWarMarch(Player player, Integer heroId, Pos targetPos) {
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		List<Integer> marchHero = march.getHeroIds();
		marchHero.add(heroId);
		march.setState(MarchState.Begin);
		march.setEndPos(targetPos);
		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());
		march.setStartPos(playerPos);
		// 兵书对行军的影响
		float bookEffectMarch = warBookManager.getBookEffectMarch(player, Lists.newArrayList(heroId));
		long period = getPeriod(player, playerPos, targetPos, bookEffectMarch);

		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
		march.setCountry(player.getCountry());
		march.setMarchType(MarchType.BROOD_WAR);
		return march;
	}

	/**
	 * @Description 更新战争发起者的战友邀请数据
	 * @Param [warInfo]
	 * @Return void
	 * @Date 2021/8/5 20:40
	 **/
	public void checkCompanion(March march) {
		long warId = march.getWarId();
		// 攻击者不在这个地图上,所以说要以行军为主
		Pos endPos = march.getEndPos();
		int mapId = worldManager.getMapId(endPos);
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		// 更新战争发起者的战友邀请数据
		WarInfo warInfo = (WarInfo) mapInfo.getWarInfoByWarId(warId);
		if (warInfo != null) {
			warInfo.getCompanionMap().remove(march.getLordId());
		}
	}

	public void sendWar(Player player) {
		if (!player.isLogin) {
			return;
		}
		HashSet<WarInfo> list = new HashSet<>();
		int pMapAreaType = worldManager.getMapAreaType(player.getLord().getMapId());
		switch (pMapAreaType) {
			case WorldAreaType.LOW_AREA:
				allWar.forEach(war -> {
					// 同地图和你相关的战斗
					if (war.getMapId() == player.getLord().getMapId()) {
						if (war.getAttackerCountry() == player.getCountry() || war.getDefencerCountry() == player.getCountry()) {
							list.add(war);
						}
					} else {
						March march = war.getAttackMarches().stream().filter(x -> x.getLordId() == player.roleId).findFirst().orElse(null);
						if (march == null) {
							march = war.getDefenceMarches().stream().filter(x -> x.getLordId() == player.roleId).findFirst().orElse(null);
						}
						if (march != null) {
							list.add(war);
						}
					}
				});
				break;
			case WorldAreaType.MIDDLE_AREA:
				allWar.forEach(war -> {
					// 同地图和你相关的战斗
					if (war.getMapId() == player.getLord().getMapId()) {
						if (war.getAttackerCountry() == player.getCountry() || war.getDefencerCountry() == player.getCountry()) {
							list.add(war);
						}
					} else {
						March march = war.getAttackMarches().stream().filter(x -> x.getLordId() == player.roleId).findFirst().orElse(null);
						if (march == null) {
							march = war.getDefenceMarches().stream().filter(x -> x.getLordId() == player.roleId).findFirst().orElse(null);
						}
						if (march != null) {
							list.add(war);
						}
						if (war.getMapType() == WorldAreaType.MIDDLE_AREA && war.getWarType() == WarType.ATTACK_COUNTRY && (war.getAttackerCountry() == player.getCountry() || war.getDefencerCountry() == player.getCountry())) {
							list.add(war);
						}
					}
				});
				break;
			case WorldAreaType.HIGH_AREA:
				allWar.forEach(war -> {
					// 同地图和你相关的战斗
					if (war.getWarType() == WarType.BIGMONSTER_WAR && war.getMapId() != player.getLord().getMapId()) {

					} else {
						if (war.getAttackerCountry() == player.getCountry() || war.getDefencerCountry() == player.getCountry()) {
							list.add(war);
						}
					}
				});
				break;
			default:
				break;
		}
		WarAssemble infos = new WarAssemble();
		infos.flush(list);
		WorldPb.SynWarInfoRq.Builder builder = WorldPb.SynWarInfoRq.newBuilder();
		if (infos.getInfo() != null) {
			builder.setEndTime(infos.getInfo().getEndTime());
		}
		builder.setNum(infos.getInfos().size());
		Map<Integer, Integer> mapNum = infos.getMapNum();
		mapNum.forEach((a, b) -> {
			CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
			builder1.setV1(a);
			builder1.setV2(b);
			builder.addWarNum(builder1);
		});
		player.setWarInfos(infos);
		BasePb.Base.Builder msg = PbHelper.createSynBase(WorldPb.SynWarInfoRq.EXT_FIELD_NUMBER, WorldPb.SynWarInfoRq.ext, builder.build());
		GameServer.getInstance().sendMsgToPlayer(player, msg);
	}

	public SuperResource addSuperResource(MapInfo mapInfo, Pos pos, SuperResource resource) {
		if (!mapInfo.addPos(pos, resource)) {
			return null;
		}

		Map<Pos, SuperResource> resourceMap = mapInfo.getSuperPosResMap();
		resourceMap.put(pos, resource);
		return resource;
	}

	/**
	 * 生成大型矿点行军
	 *
	 * @param player
	 * @param heroId
	 * @param targetPos
	 * @return
	 */
	public March createSuperResMarch(Player player, int heroId, Pos targetPos) {
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		march.getHeroIds().add(heroId);
		march.setState(MarchState.CityAssist);
		march.setEndPos(targetPos);
		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());
		march.setStartPos(playerPos);
		march.setCountry(player.getCountry());
		march.setMarchType(MarchType.SUPER_ASSIST);
		march.setEndTime(System.currentTimeMillis() + 8 * TimeHelper.HOUR_MS);
		return march;
	}

	public void caculateSuperResDefPlayer(Team team, HashMap<Integer, Integer> solderRecMap) {
		List<BattleEntity> battleEntities = team.getAllEnities();
		for (BattleEntity battleEntity : battleEntities) {
			Player player = playerManager.getPlayer(battleEntity.getLordId());
			if (player == null) {
				continue;
			}
			int id = battleEntity.getEntityId();
			Hero hero = player.getHero(id);
			if (hero != null) {
				HashMap<Long, HashMap<Integer, Integer>> tmpMap = new HashMap<>();
				tmpMap.put(battleEntity.getLordId(), solderRecMap);
				handleSoldierRec(tmpMap, hero, battleEntity);
				hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
				if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
					int moreSoldierNum = hero.getCurrentSoliderNum() - hero.getSoldierNum();
					hero.setCurrentSoliderNum(hero.getSoldierNum());
					int soldierType = heroManager.getSoldierType(hero.getHeroId());
					if (moreSoldierNum > 0) {
						playerManager.addAward(player, AwardType.SOLDIER, soldierType, moreSoldierNum, Reason.KILL_CACULATE);
					}
				}
			}
			int soilder = battleEntity.getMaxSoldierNum() - battleEntity.getCurSoldierNum();
			activityManager.updActPerson(player, ActivityConst.ACT_SOILDER_RANK, soilder, 0);
		}

	}

	public double getCityBuf(Player player, int buffType) {
		double cityBuff = 0d;
		StaticWorldCity city = staticWorldMgr.getCity(player.getLord().getCity());
		if (city != null) {
			City city1 = cityManager.getCity(city.getCityId());
			if (city1 != null && city1.getCountry() == player.getCountry()) {
				List<List<Integer>> buff = city.getBuff();
				int add = 0;
				if (buff != null && !buff.isEmpty()) {
					for (List<Integer> list : buff) {
						Integer type = list.get(0);
						Integer num = list.get(2);
						if (type == buffType) {
							add += num;
						}
					}
				}
				cityBuff = add / DevideFactor.PERCENT_NUM;
			}
		}
		return cityBuff;
	}

	public Map<Integer, WorldMap> getWorldMaps() {
		return worldMaps;
	}

}
