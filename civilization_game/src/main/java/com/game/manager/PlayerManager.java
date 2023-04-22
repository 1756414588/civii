package com.game.manager;

import com.game.Loading;
import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dao.p.AccountDao;
import com.game.dao.p.DetailDao;
import com.game.dao.p.LordDao;
import com.game.dao.p.PDataDao;
import com.game.dao.uc.UServerInfoDao;
import com.game.dataMgr.*;
import com.game.define.LoadData;
import com.game.domain.*;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.LogUser;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.*;
import com.game.pay.channel.PlayerExist;
import com.game.pb.ActivityPb.SynActivityRq;
import com.game.pb.BasePb.Base;
import com.game.pb.*;
import com.game.pb.BeautyPb.SynBeautySGameTimeRs;
import com.game.pb.BeautyPb.SynUnlockingBeautyRs;
import com.game.pb.CommonPb.ManoeuverApply;
import com.game.pb.CommonPb.ManoeuverReport;
import com.game.pb.CommonPb.PayStatus;
import com.game.pb.CommonPb.ResourceGiftShow;
import com.game.pb.CountryPb.SynOfficeRq;
import com.game.pb.MailPb.SynMailRq;
import com.game.pb.RolePb.SynChangeRq;
import com.game.pb.RolePb.SynGoldRq;
import com.game.pb.WallPb.SynAutoWallRq;
import com.game.pb.WorldPb.SynCountryWarRq;
import com.game.pb.WorldPb.SynMarchRq;
import com.game.rank.RankInfo;
import com.game.rank.RebelScoreRankMgr;
import com.game.server.GameServer;
import com.game.server.datafacede.SavePlayerServer;
import com.game.server.exec.LoginExecutor;
import com.game.service.AccountService;
import com.game.service.CityGameService;
import com.game.service.WorldActPlanService;
import com.game.spring.SpringUtil;
import com.game.util.*;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.Pos;
import com.game.worldmap.WorldBoss;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@LoadData(name = "用户管理", type = Loading.LOAD_USER_DB, initSeq = 1500)
public class PlayerManager extends BaseManager {

	// 清理账号开始时间
	public static final int CLEAN_ACCOUNT_BEGIN = 4;
	// 清理账号结束时间
	public static final int CLEAN_ACCOUNT_END = 6;

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private LordDao lordDao;

	@Autowired
	private DetailDao detailDao;

	@Autowired
	private PDataDao itemDao;

	@Autowired
	private UServerInfoDao uServerInfoDao;

	@Autowired
	private StaticIniDataMgr staticIniDataMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticPropMgr staticPropMgr;

	@Autowired
	private StaticSoldierMgr staticSoldierMgr;

	@Autowired
	private StaticDepotMgr staticDepotMgr;

	@Autowired
	private ItemManager itemDataManager;

	@Autowired
	private MailManager mailManager;

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private BuildingManager buildingManager;

	@Autowired
	private EquipManager equipManager;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private LordManager lordManager;

	@Autowired
	private StaticTaskMgr staticTaskMgr;

	@Autowired
	private RankManager rankManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private CountryManager countryManager;

	@Autowired
	private MissionManager missionManager;

	@Autowired
	private StaticWorkShopMgr staticWorkShopMgr;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private TechManager techManager;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private SoldierManager soldierManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private CityManager cityManager;

	@Autowired
	private StaticMailDataMgr staticMailDataMgr;

	@Autowired
	private WorldPvpMgr worldPvpMgr;

	@Autowired
	private RebelScoreRankMgr rebelScoreRankMgr;
	@Autowired
	private NewRankManager newRankManager;

	@Autowired
	private BeautyManager beautyManager;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private StaticCountryMgr staticCountryMgr;

	@Autowired
	private StaticOmamentMgr staticOmamentMgr;

	@Autowired
	private OmamentManager omamentManager;

	@Autowired
	private JourneyManager journeyManager;

	@Autowired
	private WorldActPlanService worldService;

	@Autowired
	private RiotManager riotManager;

	@Autowired
	private WarBookManager warBookManager;

	@Autowired
	private PersonalityManager personalityManager;

	@Autowired
	private CommandSkinManager commandSkinManager;

	@Autowired
	private LogUser logUser;
	@Autowired
	private StaticCityGameMgr cityGameManager;
	@Autowired
	private CityGameService cityGameService;
	@Autowired
	private XinkuaiManager xinkuaiManager;
	@Autowired
	private BroodWarManager broodWarManager;
	@Autowired
	private NickManager nickManager;

	@Autowired
	private ServerManager serverManager;
	@Autowired
	private ZergManager zergManager;
	@Autowired
	private StaticSensitiveWordMgr staticSensitiveWordMgr;
	@Autowired
	private WorldTargetManager worldTargetManager;

	@Autowired
	ActivityEventManager activityEventManager;

	private Map<Integer, Map<Integer, AtomicInteger>> maxLordIdPlatNo = new ConcurrentHashMap<>();

	@Getter
	@Value("${serverId}")
	private int serverId;

//    @Value("${mergeServerId}")
//    String mergeServerId;

	public boolean inited = false;

	private Logger logger = LoggerFactory.getLogger(getClass());


	private Map<Integer, Map<Integer, Account>> accountCache = new ConcurrentHashMap<Integer, Map<Integer, Account>>();
	private Map<Integer, Account> accountKeyCache = new ConcurrentHashMap<Integer, Account>();

	// 已经注册的玩家
	private Map<Long, Player> playerCache = new ConcurrentHashMap<Long, Player>();

	private Set<String> usedNames = Collections.synchronizedSet(new HashSet<String>());

	// 登陆过的玩家 需要持久化的
	@Getter
	private ConcurrentHashMap<Long, Boolean> loginCache = new ConcurrentHashMap<Long, Boolean>();

	@Getter
	private ConcurrentHashMap<Long, Player> onlinePlayer = new ConcurrentHashMap<Long, Player>();

	@Getter
	private ConcurrentHashMap<Long, OffLiner> offLinePlayer = new ConcurrentHashMap<Long, OffLiner>();

	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, PlayerExist>> playerExistMap = new ConcurrentHashMap<>();

	@Getter
	private Map<String, Player> namePlayer = new ConcurrentHashMap<>();

	// 用户channelId:accountKey
	@Getter
	private ConcurrentHashMap<Long, Integer> accountSessionMap = new ConcurrentHashMap<Long, Integer>();


	@Override
	public void load() throws Exception {
		loadAccount();

		loadDetail();
		loadServerInfoMap();
	}

	@Override
	public void init() throws Exception {
		inited = true;
		serverManager.updateBootStrap("user");
	}


	/**
	 * 记录登陆过的用户
	 *
	 * @param player
	 */
	public void recordLogin(Player player) {
		loginCache.put(player.roleId, true);
		onlinePlayer.put(player.roleId, player);
		if (offLinePlayer.containsKey(player.roleId)) {
			offLinePlayer.remove(player.roleId);
		}
	}

//	// 加载玩家数据到内存
//	public void loadAllPlayer() {
//		load();
//		com.game.util.LogHelper.GAME_LOGGER.error("load all players data!!");
//
//	}

	public Map<Long, Player> getPlayers() {
		return playerCache;
	}

	public Player createPlayer(Account account, int country) {
		Player player = initPlayerData(account, country);
		return player;
	}

	public Account getAccount(int serverId, int accountKey) {
		return accountCache.computeIfAbsent(accountKey, e -> new ConcurrentHashMap<>()).get(serverId);
	}

	public Player loginLoadPlayer(Account account, long roleId) {
		if (playerCache.containsKey(roleId)) {
			Player player = playerCache.get(roleId);

			if (player.getFullLoad().compareAndSet(false, true)) {//未加载全部,则加载玩家
				loadPlayerDetail(player);
			}

			if (player.getPos().isError() && player.getCountry() != 0) {
				worldManager.randerPlayerPos(player, true);
			}
			return player;
		}

		Lord lord = lordDao.selectLordById(roleId);
		if (lord == null) {// 用户角色为null
			return null;
		}

		Detail detail = detailDao.selectDetail(roleId);
		if (detail == null) {
			return null;
		}
		int now = TimeHelper.getCurrentSecond();
		// lord 角色未初始化
		if (lord.getLevel() == 0 || lord.getSoliderLines() == 0) {
			initLord(lord);
		}
		Player player = initPlayer(lord, now);

		try {
			if (player != null) {
				player.dserDetail(detail);
				dserItem(player, detail.getRoleData());
				player.account = account;
				caculateAllScore(player); // 计算玩家的战斗力
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("loadDetail", e);
		}

		player = playerCache.get(roleId);
		// 判断玩家对象中国家的值是否为0 为0时不赋值坐标
		if (player != null && player.getCountry() != 0) {
			worldManager.randerPlayerPos(player, true);
		}
		player.getFullLoad().set(true);
		return player;
	}

	public Player getPlayer(Long roleId) {
		if (roleId == 0l) {
			return null;
		}
		if (!playerCache.containsKey(roleId)) {
			return null;
		}
		return playerCache.get(roleId);
	}

	private Player initPlayer(Lord lord, int now) {
		Player player;
		if (StringUtil.isNullOrEmpty(lord.getNick())) {
			nickManager.setPlayerNick(lord, nickManager.getNewNick());
		}
		player = new Player(lord, now);
		addPlayer(player);
		countryManager.updatePlayerCt(player.getCountry());
		mailManager.mailSet(player);
		if (!StringUtil.isNullOrEmpty(lord.getNick())) {
			usedNames.add(lord.getNick());
		}
		return player;
	}

	public Player getPlayer(String nick) {
		Optional<Player> optional = getPlayers().values().parallelStream().filter(e -> nick.equals(e.getNick())).findFirst();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	public void addPlayer(Player player) {
		playerCache.put(player.roleId, player);
		namePlayer.put(player.getNick(), player);
	}

	public void addOnline(Player player) {
		player.setLogin(true);
	}

	public void removeOnline(Player player) {
		player.setLogin(false);
		if (onlinePlayer.containsKey(player.roleId)) {
			onlinePlayer.remove(player.roleId);
		}
		// player.offTime();
	}

	public void offLine(OffLiner offLiner) {
//        offLiner.getPlayer().offTime();
		offLinePlayer.put(offLiner.getPlayerId(), offLiner);
		savePlayerServer.saveData(new Role(offLiner.getPlayer()));
	}

	public List<Player> getOnlinePlayer() {
		return onlinePlayer.values().stream().filter(e -> e.isLogin).collect(Collectors.toList());
	}


	private Account createAccount(Account account, Player player) {
		// accountDao.insertAccount(account);
		account.setLordId(player.roleId);
		player.account = account;
		// accountCache.get(account.getServerId()).put(account.getAccountKey(), account);
		return account;
	}

	// 初始化装备基础信息
	private void initLord(Lord lord) {
		StaticIniLord staticIniLord = staticIniDataMgr.getLordIniData();
		long now = System.currentTimeMillis();
		if (staticIniLord != null) {
			// 默认给玩家的账号
			lord.setLevel(staticIniLord.getLevel());
			lord.setVip(staticIniLord.getVip());
			lord.setGold(staticIniLord.getGoldGive());
			lord.setSystemGold(staticIniLord.getGoldGive());
			lord.setGoldGive(staticIniLord.getGoldGive());
			lord.setHonor(staticIniLord.getHonour());
			lord.setEnergy(staticIniLord.getPower());
			lord.setEnergyTime(now);
			lord.setNewState(staticIniLord.getNewState());
			lord.setPosX(-1);
			lord.setPosY(-1);
			lord.setBuyEquipSlotTimes(0);
			lord.setWashSkillTimes(staticIniLord.getWashSkillTimes());
			lord.setWashHeroTimes(staticIniLord.getWashHeroTimes());
			lord.setSoliderLines(staticLimitMgr.getSoldierLines());
			lord.setCollectEndTime(now);
			lord.setWashSkillEndTime(now);
			lord.setWashHeroEndTime(now);
			StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
			if (staticLimit != null) {
				lord.setLootCommonHeroTime(now + staticLimit.getCommonHeroPeriod() * TimeHelper.SECOND_MS);
			}

			lord.setLootCommonFreeTimes(0);
			lord.setBuyEnergy(0);
			lord.setProtectedTime(now);
			lord.setBuildTeamTime(now);
			lord.setPeople(0);
			lord.setRecoverPeopleTime(now);
			lord.setMaxMonsterLv(0);
			lord.setKillMonsterNum(0);
			lord.setWorldKillMonsterStatus(0);
			lord.setKillWorldBossDay(0);
			lord.setWareBuildDay(0);
			lord.setWareTimes(0);
			lord.setWareHighTimes(0);
			lord.setFlyTimes(0);
			lord.setFlyDay(0);
			lord.setOnBuild(0);
			lord.setFreeBackTimes(0);
			lord.setDepotTime(lord.getDepotTime());
			lord.setDepotBuyTime(0);
			lord.setDepotRefresh(0);
			lord.setSoldierAuto(0);
			lord.setTitle(1);
			lord.setBuildingScore(0);
			lord.setCreateState(1);
		} else {
			lord.setLevel(1);
			lord.setVip(0);
			lord.setGold(0);
			lord.setGoldGive(0);
			lord.setHonor(0);
			lord.setEnergy(100);
			lord.setEnergyTime(now);
			lord.setNewState(0);
			lord.setPosX(-1);
			lord.setPosY(-1);
			lord.setBuyEquipSlotTimes(0);
			lord.setWashSkillTimes(0);
			lord.setWashHeroTimes(0);
			lord.setSoliderLines(2);
			lord.setCollectEndTime(now);
			lord.setCollectEndTime(now);
			lord.setWashSkillEndTime(now);
			lord.setWashHeroEndTime(now);
			StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
			if (staticLimit != null) {
				lord.setLootCommonHeroTime(now + staticLimit.getCommonHeroPeriod() * TimeHelper.SECOND_MS);
			}
			lord.setLootCommonFreeTimes(0);
			lord.setBuyEnergy(0);
			lord.setProtectedTime(now);
			lord.setBuildTeamTime(now);
			lord.setPeople(0);
			lord.setRecoverPeopleTime(now);
			lord.setMaxMonsterLv(0);
			lord.setKillMonsterNum(0);
			lord.setWorldKillMonsterStatus(0);
			lord.setKillWorldBossDay(0);
			lord.setWareBuildDay(0);
			lord.setWareTimes(0);
			lord.setWareHighTimes(0);
			lord.setFlyTimes(0);
			lord.setFlyDay(0);
			lord.setOnBuild(0);
			lord.setFreeBackTimes(0);
			lord.setDepotTime(lord.getDepotTime());
			lord.setDepotBuyTime(0);
			lord.setDepotRefresh(0);
			lord.setSoldierAuto(0);
			lord.setTitle(1);
			lord.setBuildingScore(0);
			lord.setCreateState(1);
		}

	}

	// -1：未开启，0：未上阵, 非0表示已经上阵
	// 默认上阵一个武将
	private void initEmbatle(Player player) {
		List<Integer> embattleList = player.getEmbattleList();
		for (int i = 0; i < CommonDefine.MAX_EMBATTLE_POS; ++i) {
			embattleList.add(-1);
		}

		// 开启两个坐标
		for (int i = 0; i < 2; i++) {
			embattleList.set(i, 0);
		}
	}

	// -1：未开启，0：未上阵, 非0表示已经上阵
	// 默认上阵一个武将
	public void createMiningList(Player player) {
		List<Integer> miningList = player.getMiningList();
		List<Integer> configList = staticLimitMgr.getAddtion(SimpleId.MINING_LEVEL_NUM);
		int defaultHeroCount = 0;
		int limitopenCount = defaultHeroCount + configList.size() - miningList.size();
		// 初始化上阵武将Id，-1：未开启，0：未上阵, 大于0表示已经上阵的武将Id
		for (int i = 0; i < limitopenCount; i++) {
			miningList.add(-1);
		}
		int openHeroCount = defaultHeroCount;
		// 根据等级计算能上阵的武将人数
		for (int i = 0; i < configList.size(); i++) {
			int lv = configList.get(i);
			if (player.getLevel() >= lv) {
				openHeroCount++;
			} else {
				break;
			}
		}
		// 循环设置开启的坑位
		for (int i = 0; i < openHeroCount && openHeroCount <= miningList.size(); i++) {
			Integer integer = miningList.get(i);
			if (integer != null && integer == -1) {
				miningList.set(i, 0);
			}
		}
	}

	// -1：未开启，0：未上阵, 非0表示已经上阵
	// 默认上阵一个武将
	public void createDefanceArmyList(Player player) {
		List<WarDefenseHero> defenseArmyList = player.getDefenseArmyList();
		List<Integer> configList = staticLimitMgr.getAddtion(CastleConsts.CONDITION_188);

		// TODO 默认能上阵的武将人数
		int defaultHeroCount = 0;
		// 上限个数为默认个数+配置个数
		int limitopenCount = defaultHeroCount + configList.size() - defenseArmyList.size();
		for (int i = 0; i < limitopenCount; i++) {
			defenseArmyList.add(new WarDefenseHero(-1, 0, 0));
		}
		//List<WarDefenseHero> warDefenseHeroes = new CopyOnWriteArrayList<>();
		// 初始化上阵武将Id，-1：未开启，0：未上阵, 大于0表示已经上阵的武将Id

		int openHeroCount = defaultHeroCount;
		// 根据等级计算能上阵的武将人数
		for (int i = 0; i < configList.size(); i++) {
			int lv = configList.get(i);
			if (player.getLevel() >= lv) {
				openHeroCount++;
			} else {
				break;
			}
		}
		// 循环设置开启的坑位
		for (int i = 0; i < openHeroCount && openHeroCount <= defenseArmyList.size(); i++) {
			WarDefenseHero warDefenseHero = defenseArmyList.get(i);
			if (warDefenseHero != null && warDefenseHero.getHeroId() == -1) {
				warDefenseHero.reset(0);
			}
		}
	}

	// 初始化兵数据
	private void initSoldier(Player player) {
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		soldierMap.clear();
		StaticIniLord staticIniLord = staticIniDataMgr.getLordIniData();
		Map<Integer, Integer> config = staticIniLord.getTanks();
		// 需要读取配置
		Map<Integer, StaticSoldierLv> staticSoldierLvMap = staticSoldierMgr.getSoldierLvMap();
		StaticSoldierLv staticSoldierLv = staticSoldierLvMap.get(1);
		for (int i = 1; i < CommonDefine.MAX_SOLDIER_TYPE + 1; i++) {
			Soldier soldier = new Soldier();
			soldier.setSoldierType(i);
			soldier.setCapacity(staticSoldierLv.getCapacity());
			soldier.setLargerTimes(0);
			soldier.setEmployeeTimes(0);
			soldier.setSoldierIndex(i);
			Integer count = config.get(i);
			if (count != null) {
				soldierManager.addSoldier(player, soldier, count, Reason.InitLord);
			}

			soldierMap.put(i, soldier);
		}
	}

	// 初始化玩家建筑信息
	private void initBuildings(Player player) {
		Building buildings = player.buildings;
		buildings.setBuildingTeams(staticLimitMgr.minBuildTeams());
		StaticIniLord staticIniLord = staticIniDataMgr.getLordIniData();
		if (staticIniLord == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("staticInitLord is null!");
			return;
		}
		Map<Integer, Integer> openBuildings = staticIniLord.getOpenBuildings();
		for (Map.Entry<Integer, Integer> elem : openBuildings.entrySet()) {
			int buildingId = elem.getKey();
			int buildingLv = elem.getValue();
			StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
			if (staticBuilding == null) {
				continue;
			}
			if (staticBuilding.getBuildingType() == BuildingType.COMMAND) {
				Command command = buildings.getCommand();
				command.initBase(buildingId, buildingLv);
			} else if (GameHelper.isCamp(staticBuilding.getBuildingType())) {
				Camp camp = buildings.getCamp();
				camp.addCamp(buildingId, buildingLv);
			} else if (staticBuilding.getBuildingType() == BuildingType.TECH) {
				Tech tech = buildings.getTech();
				tech.initBase(buildingId, buildingLv);
			} else if (staticBuilding.getBuildingType() == BuildingType.WALL) {
				Wall wall = buildings.getWall();
				wall.initBase(buildingId, buildingLv);
			} else if (staticBuilding.getBuildingType() == BuildingType.WORK_SHOP) {
				WorkShop workShop = buildings.getWorkShop();
				workShop.initBase(buildingId, buildingLv);
			} else if (staticBuilding.getBuildingType() == BuildingType.WARE) {
				Ware ware = buildings.getWare();
				ware.initBase(buildingId, buildingLv);
			} else if (GameHelper.isResourceBuilding(staticBuilding.getBuildingType())) {
				ResBuildings resBuildings = buildings.getResBuildings();
				resBuildings.addResourceBuilding(buildingId, buildingLv);
			} else if (staticBuilding.getBuildingType() == BuildingType.STAFF) {
				Staff staff = buildings.getStaff();
				staff.initBase(buildingId, buildingLv);
			} else if (staticBuilding.getBuildingType() == BuildingType.OMAMENT) {
				Omament omament = buildings.getOmament();
				omament.initBase(buildingId, buildingLv);
			}
		}
	}

	public void recordLogin(Account account) {
		SpringUtil.getBean(LoginExecutor.class).add(() -> {
			accountDao.recordLoginTime(account);
		});
	}

	// 初始化玩家数据[注册]
	private Player initPlayerData(Account account, int country) {
		Lord lord = createLord(account, country);
		if (lord == null) {
			return null;
		}
		Player player = new Player(lord, TimeHelper.getCurrentSecond());
		createAccount(account, player);
		playerCache.put(player.roleId, player);
		namePlayer.put(player.getNick(), player);
		return player;
	}

	public boolean createFullPlayer(Player player, RolePb.CreateRoleRq req) {
		StaticIniLord staticIniLord = staticIniDataMgr.getLordIniData();
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		DataSourceTransactionManager txManager = (DataSourceTransactionManager) SpringUtil.getApplicationContext().getBean("gameTransactionManager");
		TransactionStatus status = txManager.getTransaction(def);
		try {
			// maxkey一定要保证在最前面
			player.setMaxKey(0);
			// 玩家数据初始化
			GameError gameError = initNewPlayerInfo(player, req, staticIniLord);
			if (gameError != GameError.OK) {
				return false;
			}
			// 最后插入数据
			if (selectData(player) == null) {
				insertData(player);
			}

			accountDao.updateCreateRole(player.account);
		} catch (Exception ex) {
			txManager.rollback(status);
			logger.error("createFullPlayer", ex);
			return false;
		}

		txManager.commit(status);

		return true;
	}

	// 创建主角, 在创建账号的时候创建的
	private Lord createLord(Account account, int country) {
		Lord lord = new Lord();
		lord.setIsSeven(2);
		nickManager.setPlayerNick(lord, nickManager.getNewNick());
		Map<Integer, AtomicInteger> integerAtomicIntegerMap = maxLordIdPlatNo.computeIfAbsent(account.getServerId(), x -> new ConcurrentHashMap<>());
		AtomicInteger atomicInteger = integerAtomicIntegerMap.computeIfAbsent(country, x -> new AtomicInteger(0));
		int i = atomicInteger.incrementAndGet();
		long lordId = createLordId(account.getServerId(), country, i);
		try {
			lord.setLordId(lordId);
			initLord(lord);
			lord.setCountry(country);
			lordDao.updateLord(lord);
			lord.setDataOk(true);
		} catch (Exception ex) {
			lord = null;
			logger.error("createLord error", ex);
		}

		return lord;
	}

	public static long createLordId(int serverId, int country, int maxLord) {
		return (serverId * 10l + country) * 10000 + maxLord;
	}

	public static int getServerIdByLord(long lordId) {
		return (int) (lordId / 100000);
	}

	public static int getCountryIdByLord(long lordId) {
		return (int) (((lordId / 10000)) % 10l);
	}

	public static int getMaxLordIdByLord(long lordId) {
		return (int) (lordId % 10000);
	}

	private void insertData(Player player) {
		detailDao.insertDetail(player.serDetail());
	}

	public Detail selectData(Player player) {
		return detailDao.selectDetail(player.roleId);
	}

	// 创建初始道具
	private void initProps(Player player, StaticIniLord staticIniLord) {
		Map<Integer, Integer> staticProps = staticIniLord.getProps();
		if (staticProps == null) {
			return;
		}

		// 背包
		for (Map.Entry<Integer, Integer> entry : staticProps.entrySet()) {
			addItem(player, entry.getKey(), entry.getValue(), Reason.INIT_PLAYER);
		}
	}

	// 占用一个名字:true：占用, false：未占用
	public boolean takeNick(String nick) {
		synchronized (usedNames) {
			if (usedNames.contains(nick)) {
				return true;
			}

			usedNames.add(nick);
			return false;
		}
	}

	public void removeNickName(String nick) {
		synchronized (usedNames) {
			if (!usedNames.contains(nick)) {
				return;
			}

			usedNames.remove(nick);
		}
	}

	public boolean fullEnergy(Lord lord) {
		if (lord == null) {
			return false;
		}
		return lord.getEnergy() >= staticLimitMgr.getMaxEnegy();
	}

	// 恢复能量, now改成毫秒
	// 体力值处理：在线定时器，登录，重新登录
	public void backEnergy(Player player, long now) {
		if (null == player) {
			return;
		}
		Lord lord = player.getLord();
		int maxEnergy = staticLimitMgr.getMaxEnegy();
		if (fullEnergy(lord)) {
			lord.setEnergyTime(now);
			return;
		}

		long period = now - lord.getEnergyTime();
		int energyInterval = staticLimitMgr.getEnergyInterval();
		int back = Math.round(period / energyInterval);
		if (back <= 0) {
			return;
		}

		// 当前玩家达到的体力值
		int power = (int) (lord.getEnergy() + back);

		// 体力值恢复上限
		power = (power >= maxEnergy) ? maxEnergy - lord.getEnergy() : back;

		// 当前恢复体力的时间
		if (power < maxEnergy) {
			lord.setEnergyTime(lord.getEnergyTime() + back * energyInterval);
		} else {
			lord.setEnergyTime(now);
		}

//        lord.setEnergy(power);
		lordManager.addEnergy(lord, power, Reason.ENERGY_RESET);
	}

	public void backCollectTimes(Player player, long now) {
		if (null == player) {
			return;
		}
		Lord lord = player.getLord();
		int maxCollectTimes = staticLimitMgr.getMaxCollectTimes();
		if (lord.getCollectTimes() >= maxCollectTimes) {
			lord.setCollectEndTime(now);
			return;
		}

		// 如果满了这一次恢复的时间为now
		long period = now - lord.getCollectEndTime();
		// to do lyz
		int collectInterval = staticLimitMgr.getCollectInterval();
		if (collectInterval == 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("collectInterval is 0.");
			return;
		}

		long back = period / collectInterval;
		if (back <= 0) {
			return;
		}

		// 如果小于0, 则上一次恢复的时间为now
		if (lord.getCollectEndTime() <= 0) {
			lord.setCollectEndTime(now);
		}

		int collectTimes = (int) (lord.getCollectTimes() + back);
		collectTimes = (collectTimes >= maxCollectTimes) ? maxCollectTimes : collectTimes;
		lord.setCollectTimes(collectTimes);

		// 如果没有满，则上次恢复收集的时间
		if (collectTimes < maxCollectTimes) {
			lord.setCollectEndTime(lord.getCollectEndTime() + back * collectInterval);
		} else {
			lord.setCollectEndTime(now); // 比如服务器停机维护
		}

	}

	public void backLootCommonHeroTimes(Player player) {
		if (null == player) {
			return;
		}
		long now = System.currentTimeMillis();
		Lord lord = player.getLord();
		if (lord.getLootCommonHeroTime() <= now && lord.getLootCommonFreeTimes() <= 0) {
			lord.setLootCommonFreeTimes(1);
			StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
			if (staticLimit != null) {
				lord.setLootCommonHeroTime(now + staticLimit.getCommonHeroPeriod() * TimeHelper.SECOND_MS);
			}
		}
	}

	// 下次恢复能量的剩余时间秒数
	public long leftBackPowerTime(Lord lord) {
		if (!fullEnergy(lord)) {
			return lord.getEnergyTime() + staticLimitMgr.getEnergyInterval() - System.currentTimeMillis();
		}
		return 0;
	}

	public void updateRole(Role role) {
		if (null == role) {
			return;
		}
		lordDao.updateLord(role.getLord());
		detailDao.updateDetail(role.getDetail());
	}

	// 增加道具, 物品类型从配置表读取，不要写死在程序中
	public Item addItem(Player player, int propId, int count, int reason) {
		if (propId <= 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("type <= 0, reason = " + reason);
		}

		StaticProp staticProp = staticPropMgr.getStaticProp(propId);
		if (staticProp == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("prop Id error = " + propId + ", reason =" + reason);
			return null;
		}

		/**
		 * 道具获得资源使用日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleItemLog(new RoleItemLog(player.roleId, propId, count, RoleItemLog.ITEM_ADD, reason));

		Item item = itemDataManager.addItem(player, propId, count, reason);

		SpringUtil.getBean(EventManager.class).get_item(player, Lists.newArrayList(propId, count, reason, staticProp.getPropName(), staticProp.getPrice(), item.getItemNum()));
		if (item.getItemId() == SimpleId.COST_CARD) {
			SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_card);
		}

		return item;
	}

	// 扣除道具
	public GameError subItem(Player player, int propId, int count, int reason) {
		/**
		 * 道具使用资源使用日志埋点
		 */
		if (null == player) {
			return GameError.PLAYER_NOT_EXIST;
		}
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleItemLog(new RoleItemLog(player.roleId, propId, count, RoleItemLog.ITEM_USE, reason));
		return itemDataManager.removeItem(player, propId, count, reason);
	}

	// 增加配饰, 物品类型从配置表读取，不要写死在程序中
	public Omament addOmament(Player player, int omamentId, int count, int reason) {
		if (omamentId <= 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("type <= 0, reason = " + reason);
		}

		StaticOmament staticOmament = staticOmamentMgr.getStaticOmament(omamentId);
		if (staticOmament == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("prop Id error = " + omamentId + ", reason =" + reason);
			return null;
		}
		logUser.omamentLog(OmamentLog.builder().lordId(player.roleId).nick(player.getNick()).level(player.getLevel()).vip(player.getVip()).omamentId(omamentId).omamentName(staticOmament.getName()).reason(reason).build());
		return omamentManager.addOmament(player, omamentId, count, reason);
	}

	// 减少配饰, 物品类型从配置表读取，不要写死在程序中
	public Omament subOmament(Player player, int omamentId, int count, int reason) {
		if (omamentId <= 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("type <= 0, reason = " + reason);
		}

		StaticOmament staticOmament = staticOmamentMgr.getStaticOmament(omamentId);
		if (staticOmament == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("prop Id error = " + omamentId + ", reason =" + reason);
			return null;
		}

		return omamentManager.subOmament(player, omamentId, count, reason);
	}

	/**
	 * 添加附带战报的邮件
	 *
	 * @param player
	 * @param report
	 * @param mailId
	 * @return
	 */
	public Mail sendReportMail(Player player, Report report, ReportMsg reportMsg, int mailId, List<Award> awards, HashMap<Integer, Integer> soldierRecMap, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		if (mail == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("sendReport mail failed, mail = null, mailId = " + mailId);
			return null;
		}

		if (report != null) {
			mail.setReport(report.wrapPb().build());
		}

		if (reportMsg != null) {
			mail.setReportMsg(reportMsg.wrapPb().build());
		}

		if (awards != null && !awards.isEmpty()) {
			List<CommonPb.Award> awardList = PbHelper.createAwardList(awards);
			mail.setAward(awardList);

			if (mailId == MailId.KILL_REBEL_WIN) {
				for (CommonPb.Award e : awardList) {
					if (e.getType() == AwardType.PROP) {
						mail.getShowAward().add(e);
					}
				}
			}
		}

		// 伤兵恢复
		if (soldierRecMap != null && !soldierRecMap.isEmpty()) {
			List<CommonPb.SoldierRec> soldierRecs = new ArrayList<CommonPb.SoldierRec>();
			float soldierAdd = getBuffAdd(player, BuffId.SOLDIER_REC);
			for (Map.Entry<Integer, Integer> entry : soldierRecMap.entrySet()) {
				int soliderNum = entry.getValue();
				int rec = (int) (soldierAdd * (float) soliderNum);
				if (rec == 0) {
					continue;
				}

				CommonPb.SoldierRec.Builder soldierRec = CommonPb.SoldierRec.newBuilder();
				soldierRec.setSoldierType(entry.getKey());
				soldierRec.setSoldierNum(rec);
				soldierRecs.add(soldierRec.build());
				addAward(player, AwardType.SOLDIER, entry.getKey(), rec, Reason.SOLDIER_REC);
			}

			if (!soldierRecs.isEmpty()) {
				mail.setSoldierRecs(soldierRecs);
			}
		}

		synMailToPlayer(player, mail);
		SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		return mail;
	}

	/**
	 * 添加附带战报的邮件
	 *
	 * @param player
	 * @param report
	 * @param mailId
	 * @return
	 */
	public Mail sendReportMailOnActivity(Player player, Report report, ReportMsg reportMsg, int mailId, List<Award> awards, HashMap<Integer, Integer> soldierRecMap, float percent, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		if (mail == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("sendReport mail failed, mail = null, mailId = " + mailId);
			return null;
		}

		if (report != null) {
			mail.setReport(report.wrapPb().build());
		}

		if (reportMsg != null) {
			mail.setReportMsg(reportMsg.wrapPb().build());
		}

		if (awards != null && !awards.isEmpty()) {
			List<CommonPb.Award> awardList = PbHelper.createAwardList(awards);
			mail.setAward(awardList);

			if (mailId == MailId.KILL_REBEL_WIN || mailId == MailId.ATTACK_REBEL_MONSTER_SUCCESS) {
				for (CommonPb.Award e : awardList) {
					if (e.getType() == AwardType.PROP) {
						mail.getShowAward().add(e);
					}
				}
			}
		}
		// 伤兵恢复
		if (soldierRecMap != null && !soldierRecMap.isEmpty()) {
			List<CommonPb.SoldierRec> soldierRecs = new ArrayList<>();
			float soldierAdd = getBuffAdd(player, BuffId.SOLDIER_REC);
			// 最多返还100%
			soldierAdd = soldierAdd + percent;
			soldierAdd = Math.min(1, soldierAdd);
			for (Map.Entry<Integer, Integer> entry : soldierRecMap.entrySet()) {
				int soliderNum = entry.getValue();
				int rec = (int) (soldierAdd * (float) soliderNum);
				if (rec == 0) {
					continue;
				}
				CommonPb.SoldierRec.Builder soldierRec = CommonPb.SoldierRec.newBuilder();
				soldierRec.setSoldierType(entry.getKey());
				soldierRecs.add(soldierRec.build());
				addAward(player, AwardType.SOLDIER, entry.getKey(), rec, Reason.SOLDIER_REC);
			}

			if (!soldierRecs.isEmpty()) {
				mail.setSoldierRecs(soldierRecs);
			}
		}
		synMailToPlayer(player, mail);
		SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		return mail;
	}

	/**
	 * 发送普通邮件
	 *
	 * @param player
	 * @param mailId
	 * @param param
	 */
	public Mail sendNormalMail(Player player, int mailId, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		if (mail != null) {
			List<CommonPb.Award> awards = mail.getAward();
			if (awards == null || awards.isEmpty()) {
				mail.setAwardGot(2);
			}

			synMailToPlayer(player, mail);
			SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		}
		return mail;
	}

	public Mail sendManoeuvreMail(Player player, ManoeuverReport report, List<ManoeuverApply> applyList, int mailId, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		if (report != null) {
			mail.setManoeuverReport(report);
		}

		if (applyList != null && !applyList.isEmpty()) {
			mail.getManoeuverApply().addAll(applyList);
		}

		if (mail != null) {
			List<CommonPb.Award> awards = mail.getAward();
			if (awards == null || awards.isEmpty()) {
				mail.setAwardGot(2);
			}

			synMailToPlayer(player, mail);
			SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		}
		return mail;
	}

	/**
	 * 充值邮件
	 *
	 * @param player
	 * @param mailId
	 * @param gold
	 * @param awards
	 * @param param
	 * @return
	 */
	public Mail sendNormalMail(Player player, int mailId, int gold, List<Award> awards, String... param) {
		if (gold == 0 && (awards == null || awards != null && awards.isEmpty())) {
			return null;
		}
		Mail mail = mailManager.addMail(player, mailId, param);
		if (mail != null) {
			if (gold > 0) {
				mail.getAward().add(PbHelper.createAward(AwardType.GOLD, 0, gold).build());
			}
			if (awards != null && !awards.isEmpty()) {
				awards.forEach(e -> {
					mail.getAward().add(PbHelper.createAward(e.getType(), e.getId(), e.getCount()).build());
				});
			}
			mail.setAwardGot(2);
			synMailToPlayer(player, mail);
			SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		}
		return mail;
	}

	/**
	 * @param player
	 * @param mailId
	 * @param param
	 */
	public Mail addNormalMail(Player player, int mailId, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		return mail;
	}

	/**
	 * 发送含附件邮件
	 *
	 * @param player
	 * @param awards
	 * @param mailId
	 * @param param
	 */
	public void sendAttachMail(Player player, List<Award> awards, int mailId, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		if (mail != null) {
			if (awards != null && awards.size() > 0) {
				mail.setAward(PbHelper.createAwardList(awards));
			}

			List<CommonPb.Award> award = mail.getAward();
			if (award.isEmpty()) {
				mail.setAwardGot(2); // 如果为空则邮件got为2
			} else {
				mail.setAwardGot(0);
			}

			synMailToPlayer(player, mail);
			SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		}
	}

	public void sendAttachPbMail(Player player, List<CommonPb.Award> awardList, int mailId, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		if (mail != null) {
			if (awardList != null && !awardList.isEmpty()) {
				mail.setAward(awardList);
				mail.setAwardGot(0);
			} else {
				mail.setAwardGot(2); // 如果为空则邮件got为2
			}
			SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
			synMailToPlayer(player, mail);
		}
	}

	public void sendCountryVoteMail(int country) {
		Iterator<Player> it = getPlayers().values().iterator();
		while (it.hasNext()) {
			Player next = it.next();
			if (!next.isActive()) {
				continue;
			}
			if (next.getLevel() < 30) {
				continue;
			}
			if (next.getCountry() != country) {
				continue;
			}
			mailManager.addMail(next, MailId.COUNTRY_ELECTION);
		}
	}

	public void synActivity(Player target, int activityId, int... param) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			SynActivityRq.Builder builder = SynActivityRq.newBuilder();
			builder.addActivityId(activityId);
			if (param != null) {
				for (int i = 0; i < param.length; i++) {
					builder.addParam(param[i]);
				}
			}
			Base.Builder msg = PbHelper.createSynBase(SynActivityRq.EXT_FIELD_NUMBER, SynActivityRq.ext, builder.build());
			GameServer.getInstance().sendMsgToPlayer(target, msg);
		}
	}

	public void synMailToPlayer(Player target, Mail mail) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			SynMailRq.Builder builder = SynMailRq.newBuilder();
			builder.setMail(mail.serDefault());
			builder.addAllNotRead(getNotRead(target));
			Base.Builder msg = PbHelper.createSynBase(SynMailRq.EXT_FIELD_NUMBER, SynMailRq.ext, builder.build());
			// System.out.println("========================================="+builder.build());
			GameServer.getInstance().sendMsgToPlayer(target, msg);
		}
	}

	public void synPersonChatToPlayer(Player target, PersonChat chat) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			ChatPb.SynPersonChatRq.Builder builder = ChatPb.SynPersonChatRq.newBuilder();
			CommonPb.PersonChat.Builder chatPb = chat.serShow();
			chatPb.setOtherHeadSculpture(target.getLord().getHeadIndex());
			builder.setChat(chatPb);
			builder.setNotRead(getPersonChatNotRead(target));
			Base.Builder msg = PbHelper.createSynBase(ChatPb.SynPersonChatRq.EXT_FIELD_NUMBER, ChatPb.SynPersonChatRq.ext, builder.build());
			// System.out.println("========================================="+builder.build());
			GameServer.getInstance().sendMsgToPlayer(target, msg);
		}
	}

	public Integer getPersonChatNotRead(Player player) {
		ConcurrentHashMap<Long, PersonChatRoom> personChatRoom = player.getPersonChatRoom();
		Iterator<PersonChatRoom> iterator = personChatRoom.values().iterator();
		int count = 0;
		while (iterator.hasNext()) {
			PersonChatRoom next = iterator.next();
			if (next == null) {
				continue;
			}
			List<PersonChat> chats = next.getChats();
			for (PersonChat chat : chats) {
				if (chat == null) {
					continue;
				}

				if (chat.getState() != MailConst.UN_READ) {
					continue;
				}
				count++;
			}
		}
		return count;
	}

	public void synVote(int country, int state, long time, LinkedList<CtyGovern> rankList) {
		SynOfficeRq.Builder builder = SynOfficeRq.newBuilder();
		builder.setState(state);
		builder.setVoteTime(time);
		for (int i = 0; i < rankList.size(); i++) {
			CtyGovern govern = rankList.get(i);
			Player target = getPlayer(govern.getLordId());
			int areaId = getMapId(target);

			CommonPb.Govern.Builder governPb = PbHelper.createGovern(govern, target);
			governPb.setOffice(govern.getGovernId());
			governPb.setFight(target.getBattleScore());
			governPb.setPortrait(target.getPortrait());
			builder.addGovern(governPb.build());
		}

		Base.Builder msg2 = PbHelper.createSynBase(SynOfficeRq.EXT_FIELD_NUMBER, SynOfficeRq.ext, builder.build());

		Iterator<Player> it = getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next.isLogin && next.getChannelId() != -1 && next.getCountry() == country) {
//				ctx = next.ctx;
				if (state == CountryConst.VOTE_ING) {// 开启选举
					Nation nation = countryManager.getNation(next);
					if (nation != null) {
						int totalVote = next.getLord().getVip() + next.getLord().getTitle() + nation.getVoteExtra();
						totalVote = totalVote - nation.getVote() < 0 ? 0 : totalVote - nation.getVote();
						builder.setVote(totalVote);
						Base.Builder msg = PbHelper.createSynBase(SynOfficeRq.EXT_FIELD_NUMBER, SynOfficeRq.ext, builder.build());
						GameServer.getInstance().sendMsgToPlayer(next, msg);
					}
				} else {
					GameServer.getInstance().sendMsgToPlayer(next, msg2);
				}
			}
		}
	}

	public void synMarchToPlayer(Player target, SynMarchRq msg) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, SynMarchRq.EXT_FIELD_NUMBER, SynMarchRq.ext, msg);
		}
	}

	public void synRemoveMarch(Player target, CommonPb.March march) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			WorldPb.SynMarchRq.Builder builder = WorldPb.SynMarchRq.newBuilder();
			builder.setRemove(march);
			SynHelper.synMsgToPlayer(target, WorldPb.SynMarchRq.EXT_FIELD_NUMBER, WorldPb.SynMarchRq.ext, builder.build());
		}
	}

	public void synWarInfoToPlayer(Player target, WorldPb.SynCountryWarRq synCountryWarRq) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, SynCountryWarRq.EXT_FIELD_NUMBER, SynCountryWarRq.ext, synCountryWarRq);
		}
	}

	public void synReWarInfoToPlayer(Player target, WorldPb.SynRebelWarRq builder) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, WorldPb.SynRebelWarRq.EXT_FIELD_NUMBER, WorldPb.SynRebelWarRq.ext, builder);
		}
	}

	public void synEntityToPlayer(Player target, CommonPb.WorldEntity worldEntity, CommonPb.Pos posPb) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			WorldPb.SynEntityRq.Builder builder = WorldPb.SynEntityRq.newBuilder();
			if (worldEntity != null) {
				builder.setEntity(worldEntity);
			}
			builder.setOldPos(posPb);
			builder.setMaxMonsterLv(target.getMaxMonsterLv());
			SynHelper.synMsgToPlayer(target, WorldPb.SynEntityRq.EXT_FIELD_NUMBER, WorldPb.SynEntityRq.ext, builder.build());
		}
	}

	public WorldPb.SynEntityRq createEntityRq(Player target, CommonPb.WorldEntity worldEntity, CommonPb.Pos posPb) {
		WorldPb.SynEntityRq.Builder builder = WorldPb.SynEntityRq.newBuilder();
		if (worldEntity != null) {
			builder.setEntity(worldEntity);
		}
		builder.setOldPos(posPb);
		builder.setMaxMonsterLv(target.getMaxMonsterLv());
		return builder.build();
	}

	public void synGoldToPlayer(Player target, int addGold, int addTopup, int payType, int productId, List<Award> awards, String serialId, boolean flag) {
		if (target != null) {
			if (productId != 8401) {
				sendNormalMail(target, MailId.PAY_MAIL, addGold, awards);
			}
			SynGoldRq.Builder builder = SynGoldRq.newBuilder();
			builder.setGold(target.getLord().getGold());
			builder.setAddGold(addGold);
			builder.setAddTopup(addTopup);
			builder.setVip(target.getLord().getVip());
			builder.setSerialId(serialId);
			builder.setPayType(payType);
			builder.setProductId(productId);
			builder.setFlag(flag);
			builder.setPayFirst(target.getLord().getFirstPay());
			if (null != awards && awards.size() > 0) {
				for (Award award : awards) {
					if (award.getKeyId() != 0) {
						builder.addAward(PbHelper.createAward(target, award.getType(), award.getId(), award.getCount(), award.getKeyId()));
					} else {
						builder.addAward(PbHelper.createAward(award.getType(), award.getId(), award.getCount()));
					}
				}
			}
			if (!offLinePlayer.containsKey(target.roleId)) {
				builder.setPayOnline(1);
				SynHelper.synMsgToPlayer(target, SynGoldRq.EXT_FIELD_NUMBER, SynGoldRq.ext, builder.build());
				LogHelper.MESSAGE_LOGGER.info("synGoldToPlayer isLogin roleId={},productType={},productId={}", target.roleId, payType, productId);
			} else {
				//缓存起来
				builder.setPayOnline(2);
				target.getPayMsg().add(builder.build());
				LogHelper.MESSAGE_LOGGER.info("synGoldToPlayer not isLogin roleId={},productType={},productId={}", target.roleId, payType, productId);
			}
		}
	}

	// 同步玩家高级重建次数和低级重建次数
	public void synWareTimes(Player target) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			// 计算高级的
			int highGetTimes = getHighWareLeftTimes(target);
			// 计算低级的
			int lowGetTimes = getRebuildWareLeftTimes(target);

			RolePb.SynWareRq.Builder builder = RolePb.SynWareRq.newBuilder();

			builder.setRebuildTimes(lowGetTimes);
			builder.setHighLvTimes(highGetTimes);
			LostRes lostRes = target.getLostRes();
			List<CommonPb.Award> awards = lostRes.createAward();
			builder.addAllAward(awards);
			builder.setProtectedTime(target.getProectedTime());

			SynHelper.synMsgToPlayer(target, RolePb.SynWareRq.EXT_FIELD_NUMBER, RolePb.SynWareRq.ext, builder.build());
		}
	}

	// 高级重建剩余次数
	public int getHighWareLeftTimes(Player player) {
		Lord lord = player.getLord();
		int useTimes = lord.getWareHighTimes();
		Ware ware = player.getWare();
		if (ware == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("ware is null!");
			return 0;
		}

		// 剩余次数
		int highGetTimes = ware.getLv() - useTimes;
		highGetTimes = Math.max(0, highGetTimes);

		return highGetTimes;
	}

	public int getRebuildWareLeftTimes(Player player) {
		Lord lord = player.getLord();
		int limitTimes = staticLimitMgr.getNum(63) - lord.getWareTimes();
		limitTimes = Math.max(0, limitTimes);

		return limitTimes;
	}

	public Map<Long, Player> getAllPlayer() {
		return getPlayers();
	}

	public Set<String> getUsedNames() {
		return usedNames;
	}

	public void setUsedNames(Set<String> usedNames) {
		this.usedNames = usedNames;
	}

//	public Player loadPlayer(long lordId) {
//		Lord lord = lordDao.selectLordById(lordId);
//		if (lord == null) {
//			return null;
//		}
//		Player player = null;
//		int now = TimeHelper.getCurrentSecond();
//		if (lord.getLevel() == 0 || lord.getSoliderLines() == 0) {
//			initLord(lord);
//		}
//		player = initPlayer(lord, now);
//		// 初始化lord基础信息
//		initLord(lord);
//		// loadAccount
//		Optional<Account> accounts = accountCache.get(serverId).values().parallelStream().filter(e -> e.getLordId() == lordId).findFirst();
//		if (accounts.isPresent()) {
//			player.account = accounts.get();
//		}
//
//		// load detail
//		Detail detail = detailDao.selectDetail(lordId);
//		if (detail != null) {
//			try {
//				player.dserDetail(detail);
//				dserItem(player, detail.getRoleData());
//			} catch (InvalidProtocolBufferException e) {
//				com.game.util.LogHelper.CONFIG_LOGGER.info("loadPlayer->[{}] error", lordId);
//			}
//		}
//		rankManager.checkRankList(player.getLord());
//		return player;
//	}


	private void loadServerInfoMap() {
		Map<Integer, List<Integer>> map = new HashMap<>();
		for (Player value : playerCache.values()) {
			Account account = value.getAccount();
			if (account != null) {
				map.computeIfAbsent(account.getServerId(), x -> new ArrayList<>()).add(account.getAccountKey());
			}
		}
		if (map.isEmpty()) {
			return;
		}
		uServerInfoDao.load(map).forEach(e -> {
			playerExistMap.computeIfAbsent(e.getServerId(), x -> new ConcurrentHashMap<>()).put(e.getAccountKey(), e);
		});
	}

	private List<Lord> getLoadList() {
		List<Lord> list = new ArrayList<>();
		long curIndex = 0L;
		int count = 1000;
		int pageSize = 0;

		while (true) {
			List<Lord> page = lordDao.load(curIndex, count);
			pageSize = page.size();
			if (pageSize > 0) {
				list.addAll(page);
				curIndex = page.get(pageSize - 1).getLordId();
			} else {
				break;
			}

			if (pageSize < count) {
				break;
			}
		}
		return list;
	}

	/**
	 * Method: loadAccount
	 *
	 * @Description: 加载账号数据 @return void @throws
	 */
	private void loadAccount() {
		List<Account> list = loadAccountAsPage();
		list.parallelStream().forEach(account -> {
			Map<Integer, Account> accountMap = accountCache.computeIfAbsent(account.getAccountKey(), key -> new ConcurrentHashMap<Integer, Account>());
			accountMap.put(account.getServerId(), account);
			//accountKeyCache.put(account.getAccountKey(), account);
			if (account.getIsDelete() != 1 && account.getLordId() > 0) {
				Lord lord = lordDao.selectLordById(account.getLordId());
				if (lord != null) {
					if (lord.getLevel() == 0 || lord.getSoliderLines() == 0) {
						initLord(lord);
					}
					Player player = initPlayer(lord, TimeHelper.getCurrentSecond());
					int mapId = worldManager.getMapId(player.getPos());
					lord.setMapId(mapId);
					if (mapId == MapId.CENTER_MAP_ID) {
						int cityIdByPos = staticWorldMgr.getCityIdByPos(player.getPos());
						player.getLord().setCity(cityIdByPos);
					}
					int serverId = getServerIdByLord(lord.getLordId());
					int country = getCountryIdByLord(lord.getLordId());
					int maxLord = getMaxLordIdByLord(lord.getLordId());
					Map<Integer, AtomicInteger> integerAtomicIntegerMap = maxLordIdPlatNo.computeIfAbsent(serverId, x -> new ConcurrentHashMap<>());
					AtomicInteger maxId = integerAtomicIntegerMap.get(country);
					if (maxId == null) {
						integerAtomicIntegerMap.put(country, new AtomicInteger(maxLord));
					} else {
						int id2 = maxId.get();
						maxId.set(Math.max(maxLord, id2));
					}
					player.account = account;
					rankManager.checkRankList(player.getLord());
				}
			}
		});
	}

	private List<Account> loadAccountAsPage() {
		List<Account> list = new ArrayList<>();
		long curIndex = 0L;
		int count = 1000;
		int pageSize = 0;
		while (true) {
			List<Account> page = accountDao.load(curIndex, count);
			pageSize = page.size();
			if (pageSize > 0) {
				list.addAll(page);
				curIndex = page.get(pageSize - 1).getKeyId();
			} else {
				break;
			}

			if (pageSize < count) {
				break;
			}
		}
		return list;
	}

	/**
	 * Method: loadDetail
	 *
	 * @Description: 加载 @return void @throws
	 */
	private void loadDetail() {
		playerCache.values().parallelStream().forEach(player -> {
			Detail detail = detailDao.selectDetail(player.getRoleId());
			if (detail != null) {
				try {
					player.dserDetail(detail);
					dserItem(player, detail.getRoleData());
					caculateAllScore(player); // 计算玩家的战斗力
					player.getFullLoad().set(true);
				} catch (InvalidProtocolBufferException e) {
					logger.error("loadDetail", e);
				}
			}
		});
		//List<Detail> list = getLoadDetailList();
		//list.parallelStream().forEach(data -> {
		//    Player player = playerCache.get(data.getLordId());
		//    if (player != null) {
		//
		//    }
		//});
	}

	private boolean loadPlayerDetail(Player player) {
		Detail detail = detailDao.selectDetail(player.getRoleId());
		try {
			player.dserDetail(detail);
			dserItem(player, detail.getRoleData());
			caculateAllScore(player); // 计算玩家的战斗力
		} catch (InvalidProtocolBufferException e) {
			logger.error("loadPlayerDetail", e);
			return false;
		}
		return true;

	}

	private List<Detail> getLoadDetailList() {
		List<Detail> list = new ArrayList<>();
		long curIndex = 0;
		int count = 10;
		int pageSize = 0;
		while (true) {
			List<Detail> page = detailDao.loadDetail(curIndex, count);
			pageSize = page.size();
			if (pageSize > 0) {
				list.addAll(page);
				curIndex = page.get(pageSize - 1).getLordId();
			} else {
				break;
			}

			if (page.size() < count) {
				break;
			}
		}
		return list;
	}

	// 玩家创建角色信息初始化
	public GameError initNewPlayerInfo(Player player, RolePb.CreateRoleRq req, StaticIniLord staticIniLord) {
		Lord lord = player.getLord();
		if (lord == null) {
			return GameError.NO_LORD;
		}
		int portrait = req.getPortrait();
		int sex = req.getSex();
		int country = req.getCountry();

		Account account = player.account;
		account.setCreated(1);
		account.setCreateDate(new Date());
		lord.setPortrait(portrait);
		// nickManager.setPlayerNick(lord, nick);
		lord.setSex(sex);
		lord.setCountry(country);

		// 初始化lord基础信息
		initLord(lord);
		initEmbatle(player);
//        List<Integer> miningList = createMiningList(player.getLevel());
//        player.setMiningList(miningList);
		initSoldier(player);
		initBuildings(player);
		initResource(player);
		// initDepots(player);
		initCollectEndTime(player);
		commandSkinManager.getCommandSkinList(player);
		initHeros(player, staticIniLord);
		// TODO 上阵

		initEquips(player, staticIniLord);
		// 初始化玩家的任务信息
		initTask(player);
		initProps(player, staticIniLord);
		// 初始化关卡
		missionManager.initMission(player, 101);

		// 初始化玩家科技
		// initTech(player);

		initMapStatus(player);
		countryManager.updatePlayerCt(country);
		// 如果玩家选取的国家和当初进来时的国家相同，则发送邮件
		player.setMinCountry(account.getMinCoutry());
		if (player.getCountry() == player.getMinCountry()) {
			List<Integer> addtionGold = staticLimitMgr.getAddtion(SimpleId.CREATE_FULL_PLAYER_GOLD);
			List<Integer> addtionVip = staticLimitMgr.getAddtion(SimpleId.CREATE_FULL_PLAYER_VIP);
			if (addtionGold.size() != 3 && addtionVip.size() != 3) {
				return GameError.NO_CONFIG;
			}

			// 钻石通过邮件发送
			int typeGold = addtionGold.get(0);
			int idGold = addtionGold.get(1);
			int countGold = addtionGold.get(2);
			Award awardGold = new Award(typeGold, idGold, countGold);
			List<Award> awardGolds = new ArrayList<>();
			awardGolds.add(awardGold);
			sendAttachMail(player, awardGolds, MailId.JOIN_MIN_COUNTRY, String.valueOf(player.getCountry()));

			// 赠送vip等级
			int typeVipExp = addtionVip.get(0);
			int idVipExp = addtionVip.get(1);
			int countVipExp = addtionVip.get(2);
			addAward(player, typeVipExp, idVipExp, countVipExp, Reason.CREATE_FULL_PLAYER);

			/**
			 * vip升级日志埋点
			 */
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			logUser.vipExpLog(new VipExpLog(lord.getLordId(), lord.getNick(), player.account.getChannel(), serverManager.getServerId(), lord.getLevel(), lord.getVip(), lord.getVipExp(), countVipExp, 0, 0, player.getLord().getGold(), player.account.getLoginDate(), false));
		} else {
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			logUser.vipExpLog(new VipExpLog(lord.getLordId(), lord.getNick(), player.account.getChannel(), serverManager.getServerId(), lord.getLevel(), lord.getVip(), lord.getVipExp(), 0, 0, 0, player.getLord().getGold(), player.account.getLoginDate(), false));
		}
		player.setMinCountry(-1); // 防止重复进入
		caculateAllScore(player); // 计算玩家的战斗力
		// 状态为1的人不进入排行榜
		// try {
		// rankManager.checkRankList(player.getLord()); // 检查排行榜
		// } catch (Exception ex) {
		// LogHelper.ERROR_LOGGER.error(ex.getMessage());
		// }

		// 发送初始化邮件(弃用)
		// List<Integer> addtion = staticLimitMgr.getAddtion(223);
		List<List<Integer>> additions = staticLimitMgr.getRegisterAdditions(1);
		if (additions == null) {
			return GameError.NO_CONFIG;
		}
		List<Award> awards = new ArrayList<>();
		for (List<Integer> addition : additions) {
			awards.add(new Award(addition.get(0), addition.get(1), addition.get(2)));
		}

		if (lord.getVip() == 1) {
			sendAttachMail(player, awards, MailId.INIT_MAIL_VIP);
		} else {
			sendAttachMail(player, awards, MailId.INIT_MAIL);
		}

//        worldManager.randerPlayerPos(player);
//        this.checkStatus(player);
//        // 同步玩家所有地图信息
//        worldManager.synPlayerMapStatus(player);
		return GameError.OK;

	}

	// 初始化玩家地图开启状态
	public void initMapStatus(Player player) {
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		if (worldMap == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("worldMap is null");
			return;
		}
		Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			if (staticWorldMap == null) {
				continue;
			}

			MapStatus mapStatus = new MapStatus();
			mapStatus.setStatus(0);
			mapStatus.setMapId(staticWorldMap.getMapId());
			mapStatuses.put(mapStatus.getMapId(), mapStatus);
		}

	}

	public void initTask(Player player) {
		Map<Integer, Task> taskMap = player.getTaskMap();
		Map<Integer, StaticTask> nextTaskMap = staticTaskMgr.getNextTaskMap();
		StaticTask taskByNext = nextTaskMap.values().stream().filter(x -> x.getFirst() == 1).findFirst().orElse(null);
		if (taskByNext != null) {
			Task task = taskManager.createTask(taskByNext.getTaskId());
			// 条件个数
			List<List<Integer>> param = taskByNext.getParam();
			if (param != null) {
				task.initCond(param.size());
			}
			taskMap.put(task.getTaskId(), task);
			List<StaticTask> taskList = staticTaskMgr.getTaskList(taskByNext.getTaskId());
			if (taskList != null) {
				taskList.forEach(x -> {
					taskManager.addTask(x.getTaskId(), taskMap);
				});
			}
		}
	}

	public void initHeros(Player player, StaticIniLord staticIniLord) {
		List<Integer> heroConfig = staticIniLord.getHeros();
		int index = 0;
		for (Integer hero : heroConfig) {
			Hero h = heroManager.addHero(player, hero, Reason.INIT_LORD);
			h.setCurrentSoliderNum(h.getSoldierNum());
			player.getEmbattleList().set(index, h.getHeroId());
			index++;
		}

	}

	public void initEquips(Player player, StaticIniLord staticIniLord) {
		List<Integer> equips = staticIniLord.getEquips();
		for (Integer equipId : equips) {
			equipManager.addEquip(player, equipId, Reason.INIT_LORD);
		}
	}

	// 初始化资源
	public void initResource(Player player) {
		try {
			StaticIniLord staticIniLord = staticIniDataMgr.getLordIniData();
			addResource(player, ResourceType.IRON, staticIniLord.getIron(), Reason.INIT_PLAYER);
			addResource(player, ResourceType.COPPER, staticIniLord.getCopper(), Reason.INIT_PLAYER);
			addResource(player, ResourceType.OIL, staticIniLord.getOil(), Reason.INIT_PLAYER);
			addResource(player, ResourceType.STONE, staticIniLord.getStone(), Reason.INIT_PLAYER);

		} catch (Exception ex) {
			com.game.util.LogHelper.ERROR_LOGGER.error(ex.getMessage(), ex);
			addResource(player, ResourceType.IRON, 50000L, Reason.INIT_PLAYER);
			addResource(player, ResourceType.COPPER, 50000L, Reason.INIT_PLAYER);
			addResource(player, ResourceType.OIL, 50000L, Reason.INIT_PLAYER);
			addResource(player, ResourceType.STONE, 50000L, Reason.INIT_PLAYER);
		}

	}

	// 检查玩家坐标
	public RolePb.RoleLoginRs.Builder wrapRoleLoginRs(Player player) {
		player.setLoginNum(player.getLoginNum() + 1);
		journeyManager.getJourneyTimes(player);// 刷新远征数据
		techManager.checkHeroTech(player);
		taskManager.checkTask(player);
		// checkStatus(player);
		checkMapStatusByWorldTarget(player);
		handlePlayerMarch(player);
		checkBanquet(player);

		Lord lord = player.getLord();
		RolePb.RoleLoginRs.Builder builder = RolePb.RoleLoginRs.newBuilder();
		if (lord == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("lord is null!");
			return builder;
		}
		// 添加每日登录邮件
//        int today = TimeHelper.getCurrentDay();
//        if (today != lord.getLoginMail()) {
//            lord.setLoginMail(today);
//            addLoginMail(player);
//        }
		builder.setLordId(lord.getLordId());
		builder.setNick(lord.getNick() == null ? "" : lord.getNick());
		builder.setPortrait(lord.getPortrait());
		builder.setLevel(lord.getLevel());
		builder.setExp(lord.getExp());
		builder.setVip(lord.getVip());
		builder.setVipDot(lord.getVipExp());
		builder.setPosX(lord.getPosX());
		builder.setPosY(lord.getPosY());
		builder.setGold(lord.getGold());
		builder.setEnergy(lord.getEnergy());
		builder.setEnergyCD(getEnergyCD(player));
		int day = GameServer.getInstance().currentDay;
		if (day != lord.getBuyEnergyTime()) {
			lord.setBuyEnergy(0);
			lord.setBuyEnergyTime(day);
		}

		builder.setEnergyBuy(lord.getBuyEnergy());
		builder.setCountry(lord.getCountry());
		builder.setBattleScore(player.getBattleScore()); // 启动加载的时候计算玩家战力
		builder.setHonor(lord.getHonor());
		builder.setWashHeroTimes(lord.getWashHeroTimes());
		builder.setTitle(lord.getTitle());
		builder.setResource(player.wrapResourcePb());

		long dateEndTime = TimeHelper.getZeroOfDay();
		if (dateEndTime < lord.getMonthCard()) {
			builder.setMonthCard(lord.getMonthCard());
		}

		if (dateEndTime < lord.getSeasonCard()) {
			builder.setMonthCard(lord.getSeasonCard());
		}

		resetDepot(player, GameServer.getInstance().currentDay);

		if (lord.getDepotBuyTime() == GameServer.getInstance().currentDay) {
			builder.setDepotBuy(DepotType.BUY_YES);
			builder.setDepotOpen(lord.getDepotTime());
		} else {
			builder.setDepotBuy(DepotType.BUY_NO);
			builder.setDepotOpen(lord.getDepotTime());
		}

		for (Depot depot : player.getDepots()) {
			builder.addDepot(PbHelper.depot(depot));
		}

		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		builder.setWashHeroEndTime(lord.getWashHeroEndTime() + staticLimit.getWashHeroInterval() * TimeHelper.SECOND_MS);
		builder.setWashSkillTimes(lord.getWashSkillTimes());
		builder.setExpertWashSkillTimes(lord.getExpertWashSkillTimes());
		builder.setWashSkillEndTime(lord.getWashSkillEndTime() + staticLimit.getWashSkillInterval() * TimeHelper.SECOND_MS);
		builder.setLootCommonHero(lord.getLootCommonHero() % CommonDefine.LOOT_HERO_TIMES);
		backLootCommonHeroTimes(player);
		builder.setLootCommonHeroEndTime(lord.getLootCommonHeroTime());
		SimpleData simpleData = player.getSimpleData();
		if (simpleData != null) {
			builder.setLootGoodHero(simpleData.getLootGoodTotalTimes() % 10);
		}
		builder.setLootGoodHeroEndTime(lord.getLootGoodHeroEndTime());
		builder.setLootGoodFreeTimes(lord.getLootGoodFreeTimes());
		builder.setLootGoodHeroProcess(lord.getGoodHeroProcess());
		builder.setLootGoodHeroFiveTimes(lord.getLootGoodHeroFiveTimes());
		builder.setSoldierLine(lord.getSoliderLines());
		builder.setProtectedTime(lord.getProtectedTime());
		builder.setBuildTeamTime(lord.getBuildTeamTime());
		builder.setEquipBuyTimes(lord.getBuyEquipSlotTimes());
		builder.setPeople(lord.getPeople());
		builder.setBuyWorkShopQue(lord.getBuyWorkShopQue());
		builder.setRecoverPeopleEndTime(lord.getRecoverPeopleTime() + staticLimit.getRecoverPeopleInterval() * TimeHelper.SECOND_MS);
		builder.setMaxMonsterLv(lord.getMaxMonsterLv());
		builder.setClothes(lord.getClothes());

		if (simpleData == null) {
			builder.setKillMonsterNum(0);
		} else {
			int currentDay = GameServer.getInstance().currentDay;
			if (simpleData.getKillActMonsterDay() != currentDay) {
				simpleData.setKillActMonsterDay(currentDay);
				simpleData.setKillActMonsterTimes(0);
				simpleData.setKillRebelDay(currentDay);
				simpleData.setKillRebelTimes(0);
				simpleData.setKillRiot(0);
				simpleData.setBigMonsterReward(0);
				simpleData.setFirstBigMonsterReward(false);
				player.getLord().setDayRecharge(0);
				lord.setLoginDays(lord.getLoginDays() + 1);
				player.getPWorldBox().setTodayPoints(0);
				player.getPlayerDailyTask().reset();
				player.setLoginNum(1);
			}
			builder.setKillMonsterNum(simpleData.getKillRebelTimes());
		}
		builder.setWorldKillMonsterStatus(lord.getWorldKillMonsterStatus());
		builder.setOpenWorldMap(worldManager.getWorldMapOpen());
		if (day != lord.getKillWorldBossDay() && lord.getKillWorldBossDay() != 0) {
			builder.setKillWorldBossTimes(0);
		} else if (day == lord.getKillWorldBossDay()) {
			builder.setKillWorldBossTimes(1);
		}

		if (day != lord.getWareBuildDay() && lord.getWareBuildDay() != 0) {
			lord.setWareBuildDay(day);
			lord.setWareTimes(0);
		}
		builder.setWareTimes(getRebuildWareLeftTimes(player));
		builder.setWareHighTimes(getHighWareLeftTimes(player));
		if (day != lord.getFlyDay() && lord.getFlyDay() != 0) {
			lord.setFlyDay(day);
			lord.setFlyTimes(0);
		}
		builder.setAutoBuildTimes(lord.getAutoBuildTimes());
		builder.setAutoWallTimes(lord.getAutoWallTimes());
		Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
		for (MapStatus mapStatus : mapStatuses.values()) {
			if (mapStatus == null) {
				com.game.util.LogHelper.CONFIG_LOGGER.info("mapStatus is null!");
				continue;
			}

			if (mapStatus.getMapId() == 0) {
				continue;
			}

			builder.addMapStatus(mapStatus.wrapPb());
		}

		// 玩家当前的世界目标
		WorldTarget worldTarget = worldManager.getWorldTarget(player);
		if (worldTarget != null) {
			builder.setWorldTarget(worldTarget.wrapPb());
		}

		// 季节
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData != null) {
			CommonPb.SeasonInfo.Builder data = CommonPb.SeasonInfo.newBuilder();
			data.setSeasonId(worldData.getSeason());
			data.setEndTime(worldData.getSeasonEndTime());
			data.setEffectId(worldData.getEffect());
			builder.setInfo(data);
		}

		builder.setOnBuild(lord.getOnBuild());
		builder.setNewState(lord.getNewState());
		builder.setVipTech(lord.getVipTech());
		builder.setVipWorkShop(lord.getVipWorkShop());
//        builder.setMarchEffect(getEffectEndTime(player, LordPropertyType.MARCH_SPEED));
//        builder.setSoilderEffect(getEffectEndTime(player, LordPropertyType.RECRUIT_SOLDIERS));

		if (day != lord.getFreeBackDay() && lord.getFreeBackDay() != 0) {
			lord.setFreeBackDay(day);
			lord.setFreeBackTimes(0);
		}

		builder.setFreeBackTimes(lord.getFreeBackTimes());
		builder.addAllLevelupAwards(PbHelper.createLevelAwards(player));
		builder.setExchangeResTime(lord.getExchangeRes());
		builder.setSoldierAuto(lord.getSoldierAuto());
		builder.setOnWall(lord.getOnWall());
		builder.setCountryPeople(cityManager.getCountryPeople(player.getCountry()));
		builder.setVipEquip(lord.getVipEquip());
		List<Integer> gifts = player.getVipGifts();
		if (gifts != null && gifts.size() > 0) {
			builder.addAllVipGifts(gifts);
		}

		if (day != lord.getMailShareDay()) {
			lord.setMailShareDay(day);
			lord.setMailTimes(0);
			builder.setMailShareTimes(0);
		} else {
			builder.setMailShareTimes(lord.getMailTimes());
		}

		int office = countryManager.getOfficeId(player);
		builder.setOffice(office);
		if (lord.getCallDay() != day) {
			builder.setCallTimes(0);
		} else {
			builder.setCallTimes(lord.getCallTimes());
		}
		builder.setSilence(lord.getSilence());

		Date createRoleDate = player.account.getCreateDate();
		if (createRoleDate != null) {
			builder.setCreateRoleTime(createRoleDate.getTime());
		}

		if (day != lord.getUseEnergyDay()) {
			lord.setUseEnergyNum(0);
			lord.setUseEnergyDay(day);
		}
		builder.setUseEnergyNum(lord.getUseEnergyNum());
		builder.addAllBuff(player.wrapBuffs());
		builder.setRoleLvUpTime(lord.getLvUpTime());

		// 活动召唤
		int activityCount = activityManager.getActGovernCall();
		if (activityCount != 0) {
			builder.setActCallTimes(activityCount);
		}

		if (lord.getSuggestTime() != day) {
			builder.setSuggestCount(0);
		} else {
			builder.setSuggestCount(lord.getSuggestCount());
		}
		builder.setPayFirst(lord.getFirstPay());
		builder.setPvpCountry(worldPvpMgr.getPvpCountry());
		builder.addAllNotRead(getNotRead(player));
		if (simpleData != null) {
			builder.setIsExchangeEquip(simpleData.isExchangeEquip());
		}
		builder.addAllIcons(player.wrapIcons());
		builder.setGuideKey(player.getLord().getGuideKey());

		builder.setSGameTimes(player.getLord().getsGameTimes());
		builder.setSafety(player.getLord().getSafety());
		builder.setSeekingTimes(player.getLord().getSeekingTimes());
		builder.addAllEffect(player.wrapEffects());

		builder.setBuySGameTimes(player.getLord().getBuySGameTimes());
		builder.setFirstBReName(player.getLord().getFirstBReName());
		builder.setLoginNum(player.getLoginNum());

		// 判断每个计费点是否是首充
		List<StaticPay> payList = staticVipMgr.getPayList();
		for (int i = 0; i < payList.size(); i++) {
			CommonPb.PayStatus.Builder pbuild = PayStatus.newBuilder();
			pbuild.setPayId(payList.get(i).getPayId());
			pbuild.setStatus(0);

			List<Integer> payStatus = player.getLord().getPayStatusList();
			if (null != payStatus && payStatus.size() > 0) {
				for (int j = 0; j < payStatus.size(); j++) {
					if (payList.get(i).getPayId() == payStatus.get(j)) {
						pbuild.setStatus(1);
						break;
					}
				}
			}
			builder.addPayStatus(pbuild);
		}
		builder.setOpenSpeak(player.getLord().getOpenSpeak());
		builder.setServerOpenTime(serverManager.getServer().getOpenTime().getTime());

		builder.setJourneyTimes(player.getLord().getJourneyTimes());
		builder.setBuyJourneyTimes(player.getLord().getBuyJourneyTimes());

		if (day != lord.getBuyBookShopRefreshTime()) {
			lord.setWarBookShopRefresh(500);
			lord.setBuyBookShopRefreshTime(day);
		}
		builder.setWarBookShopRefresh(lord.getWarBookShopRefresh());

		Map<Integer, Hero> heros = player.getHeros();
		long now = System.currentTimeMillis();
		if (heros != null && heros.size() > 0) {
			Iterator<Hero> iterator = heros.values().iterator();
			while (iterator.hasNext()) {
				Hero hero = iterator.next();
				if (hero == null) {
					continue;
				}
				Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.GET_HERO_EXP);
				if (heroWarBookSkillEffect == null) {
					continue;
				}
				ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
				if (heroBooks == null || heroBooks.size() == 0) {
					continue;
				}
				HeroBook heroBook = heroBooks.get(0);
				Buff buff = heroBook.getBuffMap().get(BookEffectType.GET_HERO_EXP);
				if (buff == null) {
					continue;
				}
				long endTime = buff.getEndTime();
				if (now >= endTime) {
					int sweepHeroBookEffect = warBookManager.getSweepHeroBookEffect(player);
					heroManager.addExp(hero, player, sweepHeroBookEffect, Reason.ADD_HERO_EXP_BOOK);
					warBookManager.updateWarBookBuff(player, hero, BookEffectType.GET_HERO_EXP);
				}
			}
		}

		commandSkinManager.getCommandSkinList(player);
		builder.setNowSkin(lord.getSkin());
		if (player.isFlag()) {
			buildingManager.calRes(player);
			CommonPb.OnlineMessage.Builder onlineMessage = player.getOnlineMessage();
			builder.setOnMessage(onlineMessage);
		}
		builder.setRealServer(serverManager.getServer().getServerId());
		builder.setMoldId(serverManager.getServer().getActMold());
		builder.setCreateTiem(player.account.getCreateDate() == null ? 0 : player.account.getCreateDate().getTime() / 1000);
		builder.setResourceGiftShow(getResourceGift(player));
		refreshBeauty(player, false);

		// 是否开启过虫族主宰活动
		builder.setIsOpenZerg(zergManager.isBeaOpenActivity());
		CityRemark cityRemark = worldData.getCityRemark(player.getCountry());
		if (cityRemark != null) {
			builder.setCityRemark(cityRemark.encode());
		}
		AtomicInteger atomicInteger = worldData.getRemarkMap().get(player.getCountry());
		builder.setRemarkCount(atomicInteger.get());
		builder.setTitleAward(player.getTitleAward().encode());
		return builder;
	}

	public ResourceGiftShow getResourceGift(Player player) {
		Map<Integer, StaticResourceGift> resourceGift = activityManager.getResourceGift();
		Map<Integer, Long> resourceGiftRecord = player.getSimpleData().getResourceGiftRecord();
		ResourceGiftShow.Builder builder = ResourceGiftShow.newBuilder();
		resourceGift.values().forEach(e -> {
			int payId = e.getPayid();
			if (e.getCount() == 1 && resourceGiftRecord.containsKey(payId) && TimeHelper.isSameDay(resourceGiftRecord.get(payId))) {
				return;
			}
			builder.addKeyId(e.getKeyId());
		});
		return builder.build();
	}

	public void refreshBeauty(Player player, boolean flag) {
		if (player == null) {
			return;
		}
		Lord lord = player.getLord();
		if (player.getBeautys().isEmpty()) {
			lord.setsGameTimes(0);
		} else if (!TimeHelper.isSameDay(lord.getFreeSGameEndTime())) {
			StaticLimitMgr staticLimitMgr = SpringUtil.getBean(StaticLimitMgr.class);
			lord.setsGameTimes(staticLimitMgr.getNum(SimpleId.EVERYDAY_BEAUTY_GAME_TIMES));
			lord.setFreeSGameEndTime(System.currentTimeMillis());
			Map<Integer, BeautyData> beautys = beautyManager.getBeautys(player);
			BeautyPb.SynBeautyInfoRs.Builder builder = BeautyPb.SynBeautyInfoRs.newBuilder();
			builder.setGameTimes(player.getLord().getsGameTimes());
			if (beautys != null) {
				for (BeautyData beautyData : beautys.values()) {
					beautyData.setSeekingTimes(1);
					beautyData.setClickCount(0);
					beautyData.setFreeSeekingEndTime(System.currentTimeMillis());
					if (player.isOk()) {
						CommonPb.NewBeautyInfo.Builder newBeautyInfoBuilder = CommonPb.NewBeautyInfo.newBuilder();
						newBeautyInfoBuilder.setBeautyId(beautyData.getKeyId());
						newBeautyInfoBuilder.setSeekingTimes(beautyData.getSeekingTimes());
						newBeautyInfoBuilder.setClickCount(beautyData.getClickCount());
						builder.addNewBeautyInfo(newBeautyInfoBuilder);
					}
				}
			}
			if (player.isOk() && flag) {
				SynHelper.synMsgToPlayer(player, BeautyPb.SynBeautyInfoRs.EXT_FIELD_NUMBER, BeautyPb.SynBeautyInfoRs.ext, builder.build());
			}
		}
	}

	public void checkBanquet(Player player) {
		if (worldPvpMgr.isBanquetOver()) {
			SimpleData simpleData = player.getSimpleData();
			simpleData.clearBanquetInfo();
			// logger.info("当前国宴已经结束, 倒计时=" + (worldPvpMgr.getBanquetEndTime() <= System.currentTimeMillis()));
		}
	}

	public void handlerMarch(Player player) {
		ConcurrentLinkedDeque<March> marches = player.getMarchList();
		Iterator<March> iterator = marches.iterator();
		// 删除玩家身上的行军
		while (iterator.hasNext()) {
			March march = iterator.next();
			if (march.getState() == MarchState.Done) {
				iterator.remove();
			}
		}
	}

	public long getEnergyCD(Player player) {
		Lord lord = player.getLord();
		int energyInterval = staticLimitMgr.getEnergyInterval();
		long endTime = lord.getEnergyTime() + energyInterval;
		return endTime;
	}

	public RolePb.RoleReloginRs.Builder wrapRoleReLogin(Player player) {
		RolePb.RoleReloginRs.Builder builder = RolePb.RoleReloginRs.newBuilder();
		Lord lord = player.getLord();
		if (lord == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("lord is null!");
			return builder;
		}

		builder.setEnergy(lord.getEnergy());
		int day = GameServer.getInstance().currentDay;
		if (day != lord.getBuyEnergyTime()) {
			lord.setBuyEnergy(0);
			lord.setBuyEnergyTime(day);
		}
		builder.setEnergyBuy(lord.getBuyEnergy());
		builder.setEnergyCD(getEnergyCD(player));
		builder.setPosX(lord.getPosX());
		builder.setPosY(lord.getPosY());
		if (lord.getDepotBuyTime() == GameServer.getInstance().currentDay) {
			builder.setDepotBuy(DepotType.BUY_YES);
			builder.setDepotOpen(lord.getDepotTime());
		} else {
			builder.setDepotBuy(DepotType.BUY_NO);
			builder.setDepotOpen(lord.getDepotTime());
		}

		for (Depot depot : player.getDepots()) {
			builder.addDepot(depot.ser());
		}
		builder.setProtectedTime(lord.getProtectedTime());

		return builder;
	}

	public RolePb.RefreshDataRs.Builder wrapRefreshData(Player player) {
		RolePb.RefreshDataRs.Builder builder = RolePb.RefreshDataRs.newBuilder();
		Lord lord = player.getLord();
		if (lord == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("lord is null!");
			return builder;
		}
		// builder.setEnergyBuy(lord.getBuyEnergy());
		int day = GameServer.getInstance().currentDay;
		if (day != lord.getMailShareDay()) {
			lord.setMailShareDay(day);
			lord.setMailTimes(0);
		} else {
			lord.setMailTimes(lord.getMailTimes());
		}
		return builder;
	}

	// 增加威望
	public void addHonor(Player player, long honor, int reason) {
		Lord lord = player.getLord();
		if (lord == null) {
			return;
		}

		// 设置玩家最高威望
		long resHonor = lord.getHonor() + honor;
		long maxHonor = staticLimitMgr.getMaxHonor();
		resHonor = resHonor >= maxHonor ? maxHonor : honor;
		lord.setHonor(resHonor);
		SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_honner);
	}

	public boolean addGold(Player player, int add, int reason) {
		if (add <= 0) {
			return false;
		}
		player.addGold(add);

		// 非充值都记录系统钻石
		if (reason != Reason.PAY) {
			player.addSystemGold(add);
		} else {
			player.addRechargeGold(add);
		}
		List param = Lists.newArrayList(add, reason, player.getGold(), add);
		SpringUtil.getBean(EventManager.class).get_diamond(player, param);
		/**
		 * 充值产出钻石
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, add, reason));
		return true;
	}

	public boolean subGold(Player player, int num, int reason) {
		player.subGold(num);
		boolean result = false;
		if (num > 0 && reason != Reason.ACT_HOPE) {
			activityManager.updActRecharScore(player, num, ActivityConst.ACT_COST_GOLD);
			activityEventManager.activityTip(EventEnum.SUB_GOLD, player, num);
			// 母巢之战单独处理
			if (reason == Reason.BROOD_WAR) {
				activityEventManager.activityTip(EventEnum.BUY_BROOD_BUFF, player, num);
			}
		}
		List param = Lists.newArrayList(num, reason, player.getGold());
		SpringUtil.getBean(EventManager.class).cost_diamond(player, param);
//        activityManager.checkHeroKowtow(player, TaskType.COST_GOLD, num);
		xinkuaiManager.pushGold(player, num);

		/**
		 * 充值产出钻石
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, -num, reason));
		return result;
	}

	public GameError addResource(Player player, int resType, long count, int reason) {
		Resource resource = player.getResource();
		if (resource == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("resource is null");
			return GameError.RESOURCE_NULL;
		}

		Map<Integer, Long> resourceMap = resource.getResource();
		if (!resourceMap.containsKey(resType)) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("no resType = " + resType);
			return GameError.RESOURCE_TYPE_ERROR;
		}

		long limitNum = staticLimitMgr.getLimitNum(resType);
		Long resNum = resourceMap.get(resType);
		if (resNum == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("resNum is null in addResource!");
			return GameError.RESOURCE_NUM_ERROR;
		}

		long resCount = resourceMap.get(resType) + count;
		resCount = Math.min(limitNum, resCount);
		resCount = Math.max(0, resCount);
		resourceMap.put(resType, resCount);

		if (resType == ResourceType.OIL && Reason.CANCEL_SOLDIER != reason) {
			if (count > 0) {
				// 更新粮食排行榜活动
				activityManager.updActPerson(player, ActivityConst.ACT_OIL_RANK, count, 0);
				// 更新通行证任务
//				activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.GET_OI, (int) count);
				activityEventManager.activityTip(EventEnum.GET_OIL, player, (int) count, 0);
			}
		} else if (resType == ResourceType.STONE) {
			if (count > 0) {
				activityManager.updActPerson(player, ActivityConst.ACT_STONE_RANK, count, 0);
			}
		}
		List param = Lists.newArrayList(count, reason, player.getResource(resType));
		if (reason == Reason.BUY_SHOP) {
			param.add(player.getPrice());
		} else {
			param.add(0);
		}
		param.add(Reason.ReasonName.getName(reason));
		player.setPrice(0);
		switch (resType) {
			case ResourceType.IRON:
				SpringUtil.getBean(EventManager.class).get_coins(player, param);
				break;
			case ResourceType.OIL:
				SpringUtil.getBean(EventManager.class).get_food(player, param);
				break;
			case ResourceType.COPPER:
				SpringUtil.getBean(EventManager.class).get_iron(player, param);
				break;
			case ResourceType.STONE:
				SpringUtil.getBean(EventManager.class).get_crystal(player, param);
				break;
		}

		return GameError.OK;
	}

	public void subResource(Player player, int resType, long count, int reason) {
		Resource resource = player.getResource();
		if (resource == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("resource is null");
			return;
		}

		Map<Integer, Long> resourceMap = resource.getResource();
		if (!resourceMap.containsKey(resType)) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("no resType = " + resType);
			return;
		}

		Long number = resourceMap.get(resType);
		if (number < count) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("remove resource failed,  owned = " + number + ", need count = " + count + ", reason = " + reason);
			return;
		}

		resourceMap.put(resType, resourceMap.get(resType) - count);
		if (resType == ResourceType.STONE) {
			if (count > 0) {
				activityManager.updActPerson(player, ActivityConst.ACT_STONE_DIAL, count, 0);
			}
		}
		List param = Lists.newArrayList(count, reason, player.getResource(resType), Reason.ReasonName.getName(reason));
		player.setPrice(0);
		switch (resType) {
			case ResourceType.IRON:
				SpringUtil.getBean(EventManager.class).cost_coins(player, param);
				break;
			case ResourceType.OIL:
				SpringUtil.getBean(EventManager.class).cost_food(player, param);
				break;
			case ResourceType.COPPER:
				SpringUtil.getBean(EventManager.class).cost_iron(player, param);
				break;
			case ResourceType.STONE:
				SpringUtil.getBean(EventManager.class).cost_crystal(player, param);
				break;
		}
		// do log
	}

	// 使用资源
	// times : 使用次数
	// awardType, resType,count
	public GameError doAddResource(Player player, List<Long> param, int times, int reason) {
		if (param.size() != 3) {
			return GameError.ADD_RESOURCE_PARAM_ERROR;
		}

		int awardType = param.get(0).intValue();
		int resType = param.get(1).intValue();
		if (awardType != AwardType.RESOURCE) {
			return GameError.AWARD_RESOURCE_TYPE_ERROR;
		}

		if (!GameHelper.isValidResType(resType)) {
			return GameError.RESOURCE_TYPE_ERROR;
		}

		Long count = param.get(2) * times;

		return addResource(player, resType, count, reason);

	}

	// 获取内政官加成
	public CommonPb.Resource.Builder getOfficerAdd(Player player) {
		CommonPb.Resource.Builder officerAdd = CommonPb.Resource.newBuilder();
		StaticBuildingMgr staticBuildingMgr = SpringUtil.getBean(StaticBuildingMgr.class);
		// 过了时间就不能加成了
		EmployInfo employInfo = player.getEmployInfo();
		if (employInfo == null) {
			return officerAdd;
		}
		Employee employee = employInfo.getOfficer();
		if (employee == null) {
			return officerAdd;
		}
		long now = System.currentTimeMillis();
		boolean isTimeLeft = employee.getEndTime() > now;
		if (!isTimeLeft) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("no time left..");
			return officerAdd;
		}

		// 基础加成
		Building buildings = player.buildings;
		CommonPb.Resource.Builder resAdd = buildingManager.getResource(buildings.getResBuildings());
		int employeeId = employee.getEmployeeId();
		StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(employeeId);
		if (staticEmployee == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("employee config is error!");
			return officerAdd;
		}

		List<Integer> resAddFlag = staticEmployee.getResAddFlag();
		if (resAddFlag.size() != 4) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("resAddFlag.size() != 4");
			return officerAdd;
		}

		double addFactor = staticEmployee.getBaseResoureFactor();

		long ironAdd = 0;
		if (resAddFlag.get(ResourceType.IRON - 1) == 1) {
			ironAdd = (int) (addFactor * resAdd.getIron());
		}

		long copperAdd = 0;
		if (resAddFlag.get(ResourceType.COPPER - 1) == 1) {
			copperAdd = (int) (addFactor * resAdd.getCopper());
		}

		long oilAdd = 0;
		if (resAddFlag.get(ResourceType.OIL - 1) == 1) {
			oilAdd = (int) (addFactor * resAdd.getOil());
		}

		long stoneAdd = 0;
		if (resAddFlag.get(ResourceType.STONE - 1) == 1) {
			stoneAdd = (int) (addFactor * resAdd.getStone());
		}

		officerAdd.setIron(ironAdd);
		officerAdd.setCopper(copperAdd);
		officerAdd.setOil(oilAdd);
		officerAdd.setStone(stoneAdd);

		return officerAdd;
	}

	public void initCollectEndTime(Player player) {
		int collectInterval = staticLimitMgr.getCollectInterval();
		if (collectInterval == 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("collectInterval is 0.");
			return;
		}

		Lord lord = player.getLord();
		if (lord.getCollectEndTime() <= 0) {
			lord.setCollectEndTime(System.currentTimeMillis());
		}
	}

	private void addSoldier(Player player, int soldierType, int count, int reason) {
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		Soldier soldier = soldierMap.get(soldierType);
		if (soldier == null) {
			return;
		}
		soldierManager.addSoldier(player, soldier, count, reason);
	}

	private void subSoldier(Player player, int soldierType, int count, int reason) {
		soldierManager.subSoldierNum(player, soldierType, count, reason);
	}

	// 增加主角属性
	public void addLordProperty(Player player, int id, long count, int reason, int amount, int topUp, int serverId) {
		switch (id) {
			case LordPropertyType.EXP:
				lordManager.addExp(player, (int) count, reason);
				break;
			case LordPropertyType.VIP_LEVEL:
				lordManager.addVipLevel(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.VIP_EXP:
				lordManager.addVipExp(player, (int) count, reason, amount, topUp, serverId);
				break;
			case LordPropertyType.HONOR:
				lordManager.addHonor(player.getLord(), count, reason);
				break;
			case LordPropertyType.HERO_WASH_TIMES:
				lordManager.addHeroWashTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.EQUIP_WASH_TIMES:
				lordManager.addSkillWashTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.MARCH_SPEED:
				addEffect(player, LordPropertyType.MARCH_SPEED, (int) count, count);
				break;
			case LordPropertyType.RECRUIT_SOLDIERS:
				addEffect(player, LordPropertyType.RECRUIT_SOLDIERS, (int) count, count);
				break;
			case LordPropertyType.ENERGY:
				lordManager.addEnergy(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.BEAUTY_SEEKINGTIMES:
				beautyManager.addSeekingTimes(player, (int) count, reason);
				break;
			case LordPropertyType.BEAUTY_SGAMETIMES:
				lordManager.addSGameTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.BEAUTY_SAFETY:
				lordManager.addSafety(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.JOURNEY_TIME:
				lordManager.addJourneyTime(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.FRIEND_SCORE:
				addFriendScore(player, count);
				break;
			case LordPropertyType.EQUIP_EXPERT_WASH_TIMES:
				lordManager.addExpertSkillWashTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.WORLDBOX_POINT:
				player.getPWorldBox().addPoints((int) count);
				break;
			case LordPropertyType.TD_MONEY:
				addTDMoney(player, (int) count, reason);
				break;
			default:
				break;
		}
	}

	// 减少主角属性
	public void subLordProperty(Player player, int id, long count, int reason) {
		if (count <= 0) {
			return;
		}
		switch (id) {
			case LordPropertyType.HONOR:
				lordManager.subHonor(player.getLord(), count, reason);
				SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_honner);
				break;
			case LordPropertyType.HERO_WASH_TIMES:
				lordManager.subHeroWashTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.EQUIP_WASH_TIMES:
				lordManager.subSkillWashTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.ENERGY:
				lordManager.subEnergy(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.BEAUTY_SEEKINGTIMES:
				lordManager.subSeekingTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.BEAUTY_SGAMETIMES:
				lordManager.subSGameTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.BEAUTY_SAFETY:
				lordManager.subSafety(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.JOURNEY_TIME:
				lordManager.subJourneyTime(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.FRIEND_SCORE:
				subFriendScore(player, count);
				break;
			case LordPropertyType.EQUIP_EXPERT_WASH_TIMES:
				lordManager.subExpertSkillWashTimes(player.getLord(), (int) count, reason);
				break;
			case LordPropertyType.TD_MONEY:
				subTDMoney(player, (int) count, reason);
				break;
			default:
				break;
		}

	}

	private void addFriendScore(Player player, long count) {
		player.getMasterShop().setScore(player.getMasterShop().getScore() + count);
	}

	private void subFriendScore(Player player, long count) {
		if (player.getMasterShop() == null) {
			return;
		}
		if (player.getMasterShop().getScore() == 0) {
			return;
		}
		player.getMasterShop().setScore(player.getMasterShop().getScore() - count);
	}

	public int addAward(Player player, int type, int id, long count, int reason) {
		return addAward(player, type, id, count, reason, 0, 0, serverId);
	}

	/**
	 * 通用加属性、物品、数据
	 */
	public int addAward(Player player, int type, int id, long count, int reason, int amount, int topUp, int serverId) {
		if (count < 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("count < 0, reason = " + reason);
			return 0;
		}

		switch (type) {
			case AwardType.LORD_PROPERTY:
				addLordProperty(player, id, count, reason, amount, topUp, serverId);
				break;
			case AwardType.GOLD:
				addGold(player, (int) count, reason);
				break;
			case AwardType.RESOURCE:
				addResource(player, id, count, reason);
				break;
			case AwardType.SOLDIER:
				addSoldier(player, id, (int) count, reason);
				break;
			case AwardType.PROP:
				addItem(player, id, (int) count, reason);
				break;
			case AwardType.OMAMENT:
				addOmament(player, id, (int) count, reason);
				break;
			case AwardType.EQUIP:
				return equipManager.addEquip(player, id, reason).getKeyId();
			case AwardType.HERO: {
				heroManager.addHero(player, id, reason);
				break;
			}
			case AwardType.PERSON: {
				player.getLord().setPeople(player.getLord().getPeople() + (int) count);
				break;
			}
			case AwardType.BUILD_LV: {
				buildingManager.addBuildingLv(player, id, (int) count, reason);
				break;
			}
			case AwardType.COMMERCIAL_TEAM_TIME: {
				addBuildTeamTime(player, count, reason);
				break;
			}
			case AwardType.ICON: {
				addIcon(player, id, reason);
				break;
			}
			case AwardType.COLLECT_TIMES: {
				addCollectTimes(player, (int) count);
				break;
			}
			case AwardType.RIOT_ITEM: {
				addRiotItem(player, (int) count, reason);
				break;
			}
			case AwardType.RIOT_SCORE: {
				addRiotScore(player, (int) count, reason);
				break;
			}
			case AwardType.STAFF_SOLDIER: {
				addStaffSoldier(player, (int) count, reason);
				break;
			}
			case AwardType.REBEL_SCORE: {
				addRebelScore(player, (int) count, reason);
				break;
			}

			case AwardType.BEAUTY_WOMEN: {
				beautyManager.addBeautyInfo(player, id, reason);
				break;
			}

//            case AwardType.BEAUTY_CLOTHES: {
//                beautyManager.addBeautyClothes(player, id, reason);
//                break;
//            }
			case AwardType.DOUBLE_CARD: {
				activityManager.actPayCard(player, id);
				break;
			}
			case AwardType.POSSPORT_LV: {
				activityManager.addPassPortLv(player, id);
				synActivity(player, ActivityConst.ACT_PASS_PORT, 0);
				break;
			}
			case AwardType.PASSPORT: {
				break;
			}
			case AwardType.POSSPORT_SCORE: {
				activityManager.addPassPortScore(player, count);
				break;
			}
			case AwardType.PVP_SCORE: {
				addWorldPvpScore(player, (int) count, reason);
				break;
			}
			case AwardType.WAR_BOOK_SKILL: {
			}
			case AwardType.WAR_BOOK_SPECIAL: {
			}
			case AwardType.WAR_BOOK: {
				return warBookManager.addWarBookById(player, id, reason, type, count).getKeyId();
			}
			case AwardType.WORLD_BOX_POINT: {
				player.getPWorldBox().addPoints((int) count);
				break;
			}
			case AwardType.COUNTRY_POINT: {
				addCountryExp(player.getCountry(), count);
			}
			break;
			case AwardType.FRAME_HEAD: {
				personalityManager.addAward(player, id, (int) count);
			}
			break;
			case AwardType.COMMAND_SKIN: {
				commandSkinManager.addCommandSkin(player, id, reason);
			}
			break;
			case AwardType.ROLE_CLOTHES: {
				player.getLord().setClothes(id);
			}
			break;
			/*
			 * case AwardType.BEAUTY_VOICE: { beautyManager.addBeautyVoice(player, id, reason); break; }
			 */
			case AwardType.BROOD_WAR:
				broodWarManager.addScore(player, count);
				break;
			case AwardType.MANOEUVRE_SCORE: {
				addManoeuvreScore(player, (int) count, reason);
				break;
			}
			case AwardType.FLAME_SCORE: {
				player.addScore(count);
				break;
			}
			default:
				break;
		}
		// LogHelper.logAddItem(player, type, id, (int) count, reason);
		return 0;
	}

	private void addCountryExp(int countryId, long exp) {
		CountryData countryData = countryManager.getCountry(countryId);
		if (countryData == null) {
			return;
		}
		countryManager.addCountryExp(countryData, (int) exp);
	}

	/**
	 * 美女通用属性的增加
	 *
	 * @param player
	 * @param beautyId
	 * @param type
	 * @param id
	 * @param count
	 * @param reason
	 * @return
	 */
	public int addAward(Player player, int beautyId, int type, int id, long count, int reason) {
		////////// 美女相关的属性值添加
		switch (type) {
			case AwardType.BEAUTY_PROPERTY: {
				addBeautyProperty(player, beautyId, id, (int) count, reason);
				break;
			}
			default:
				break;
		}
		return 0;
	}

	private void addBeautyProperty(Player player, int beautyId, int id, int count, int reason) {
		switch (id) {
//            case BeautyPropertyType.EXP: // 经验
//                beautyManager.addExp(player, beautyId, (int) count, reason);
//                break;
//            case BeautyPropertyType.CHARM_VALUE:    //魅力值
//                beautyManager.addCharmValue(player, beautyId, (int) count, reason);
//                break;
			case BeautyPropertyType.INTIMACY_VALUE:// 亲密度
				beautyManager.addIntimacyValue(player, beautyId, count, reason);
				break;
			default:
				break;
		}
	}

	private boolean addRebelScore(Player player, int rebelScore, int reason) {
		if (rebelScore <= 0) {
			return false;
		}
		SimpleData simpleData = player.getSimpleData();
		simpleData.setRebelScore(simpleData.getRebelScore() + rebelScore);
		rebelScoreRankMgr.update(new RankInfo(player.getLord().getLordId(), player.getSimpleData().getRebelScore()));
		return true;
	}

	private boolean addManoeuvreScore(Player player, int score, int reason) {
		if (score <= 0) {
			return false;
		}
		SimpleData simpleData = player.getSimpleData();
		simpleData.setManoeuvreScore(simpleData.getManoeuvreScore() + score);
		return true;
	}

	private boolean addWorldPvpScore(Player player, int score, int reason) {
		if (score <= 0) {
			return false;
		}
		SimpleData simpleData = player.getSimpleData();
		simpleData.addPvpScore(score);
		return true;
	}

	/**
	 * 给国家添加预备兵
	 *
	 * @param player
	 * @param count
	 * @param reason
	 */
	private void addStaffSoldier(Player player, int count, int reason) {
		CountryData country = countryManager.getCountry(player.getCountry());
		country.setKillNum(country.getKillNum() + count);

	}

	// 新增形象
	public void addIcon(Player player, int id, int reason) {
		SimpleData simpleData = player.getSimpleData();
		HashMap<Integer, Integer> portraits = simpleData.getIcons();
		if (portraits.containsKey(id)) {
			return;
		}

		StaticPortrait portrait = staticIniDataMgr.getPortrait(id);
		if (portrait == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("icon error, icon id = " + id);
			return;
		}
		portraits.put(id, 0);
	}

	public int addAward(Player player, Award award, int reason) {
		return addAward(player, award.getType(), award.getId(), award.getCount(), reason);
	}

	// 为玩家增加buff
	// [19, 1, 15, 900] 伤害减免 15% [awardType, id, value, time]
	public void addBuff(Player player, int type, int id, long period, int value, int count, int reason) {
		SimpleData simpleData = player.getSimpleData();
		HashMap<Integer, Buff> buffMap = simpleData.getBuffMap();
		Buff buff = buffMap.get(id);
		long currentTime = System.currentTimeMillis();
		if (buff == null) {
			buff = new Buff();
			buff.setBuffId(id);
			buff.setPeriod(period);
			buff.setEndTime(period + currentTime);
			buff.setValue(value);
			buffMap.put(id, buff);
			// 战力计算
			if (id == BuffId.ADD_DEFENCE || id == BuffId.ADD_ATTACK) {
				heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
			}
		} else {
			if (buff.getEndTime() <= currentTime) {
				buff.setEndTime(currentTime);
				buff.setPeriod(0);
				// 战力计算
				if (id == BuffId.ADD_DEFENCE || id == BuffId.ADD_ATTACK) {
					heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
				}
			}
			if (buff.getValue() < value) {
				buff.setValue(value);
				buff.setPeriod(buff.getPeriod() + period);
				buff.setEndTime(buff.getEndTime() + period);
			}
		}
		// LogHelper.logAddItem(player, type, id, count, reason);
	}

	/**
	 * 通用减属性、物品、数据
	 */
	public void subAward(Player player, int type, int id, long count, int reason) {
		if (count < 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("count < 0");
			return;
		}

		switch (type) {
			case AwardType.LORD_PROPERTY:
				subLordProperty(player, id, count, reason);
				break;
			case AwardType.GOLD:
				subGold(player, (int) count, reason);
				break;
			case AwardType.RESOURCE:
				subResource(player, id, count, reason);
				break;
			case AwardType.SOLDIER:
				subSoldier(player, id, (int) count, reason);
				break;
			case AwardType.PROP:
				subItem(player, id, (int) count, reason);
				break;
			case AwardType.EQUIP:
				equipManager.subEquip(player, id, reason);
				break;
			case AwardType.PERSON: {
				int diff = player.getLord().getPeople() - (int) count;
				diff = Math.max(0, diff);
				player.getLord().setPeople(diff);
				break;
			}
			case AwardType.RIOT_ITEM: {
				subRiotItem(player, (int) count, reason);
				break;
			}
			case AwardType.RIOT_SCORE: {
				subRiotScore(player, (int) count, reason);
				break;
			}
			case AwardType.REBEL_SCORE: {
				subRebelScore(player, (int) count, reason);
				break;
			}
			case AwardType.OMAMENT: {
				subOmament(player, id, (int) count, reason);
				break;
			}
			default:
				break;
		}
	}

	private boolean subRebelScore(Player player, int rebelScore, int reason) {
		if (rebelScore <= 0) {
			return false;
		}

		SimpleData simpleData = player.getSimpleData();
		int res = simpleData.getRiotScore() - rebelScore;
		res = Math.max(0, res);
		simpleData.setRiotScore(res);
		return true;
	}

	public void subAward(Player player, Award award, int reason) {
		subAward(player, award.getType(), award.getId(), award.getCount(), reason);
	}

	public void subGoldOk(Player player, int count, int reason) {
		subAward(player, AwardType.GOLD, 0, count, reason);
	}

	public void addAward(Player player, List<Award> awards, int reason) {
		for (Award award : awards) {
			addAward(player, award.getType(), award.getId(), award.getCount(), reason);
		}
	}

	/**
	 * 重置聚宝盆
	 *
	 * @param player
	 */
	public void resetDepot(Player player, int currentDay) {
		if (player.getLord().getDepotRefresh() != currentDay) {
			player.getDepots().clear();
			player.getDepots().addAll(staticDepotMgr.getRandomDeport(player.getLevel()));
			player.getLord().setDepotRefresh(currentDay);
		}
	}

	// 恢复洗练技能次数
	public void recoverWashSkillTimes(Player player, long now) {
		Lord lord = player.getLord();
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		int maxWashSkillTimes = staticLimit.getMaxWashSkillTimes();
		if (lord.getWashSkillTimes() >= maxWashSkillTimes) {
			lord.setWashSkillEndTime(now);
			return;
		}

		// 如果满了这一次恢复的时间为now
		long period = now - lord.getWashSkillEndTime();
		// to do lyz
		long washSkillInterval = staticLimit.getWashSkillInterval() * TimeHelper.SECOND_MS;
		if (washSkillInterval == 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("collectInterval is 0.");
			return;
		}

		long back = period / washSkillInterval;
		if (back <= 0) {
			return;
		}

		// 如果小于0, 则上一次恢复的时间为now
		if (lord.getWashSkillEndTime() <= 0) {
			lord.setWashSkillEndTime(now);
		}

		int washSkillTimes = (int) (lord.getWashSkillTimes() + back);
		washSkillTimes = (washSkillTimes >= maxWashSkillTimes) ? maxWashSkillTimes : washSkillTimes;
		lord.setWashSkillTimes(washSkillTimes);

		// 如果没有满，则上次恢复收集的时间
		if (washSkillTimes < maxWashSkillTimes) {
			lord.setWashSkillEndTime(lord.getWashSkillEndTime() + back * washSkillInterval);
		} else {
			lord.setWashSkillEndTime(now); // 比如服务器停机维护
		}
	}

	// 恢复洗练英雄次数
	public void recoverWashHeroTimes(Player player, long now) {
		Lord lord = player.getLord();
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		int maxWashHeroTimes = staticLimit.getMaxWashHeroTimes();
		if (lord.getWashHeroTimes() >= maxWashHeroTimes) {
			lord.setWashHeroEndTime(now);
			return;
		}

		// 如果满了这一次恢复的时间为now
		long period = now - lord.getWashHeroEndTime();
		long washHeroInterval = staticLimit.getWashHeroInterval() * TimeHelper.SECOND_MS;
		if (washHeroInterval == 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("washHeroInterval is 0.");
			return;
		}

		long back = period / washHeroInterval;
		if (back <= 0) {
			return;
		}

		// 如果小于0, 则上一次恢复的时间为now
		if (lord.getWashHeroEndTime() <= 0) {
			lord.setWashHeroEndTime(now);
		}

		int washHeroTimes = (int) (lord.getWashHeroTimes() + back);
		washHeroTimes = (washHeroTimes >= maxWashHeroTimes) ? maxWashHeroTimes : washHeroTimes;
		lord.setWashHeroTimes(washHeroTimes);

		// 如果没有满，则上次恢复收集的时间
		if (washHeroTimes < maxWashHeroTimes) {
			lord.setWashHeroEndTime(lord.getWashHeroEndTime() + back * washHeroInterval);
		} else {
			lord.setWashHeroEndTime(now); // 比如服务器停机维护
		}
	}

	// 恢复人口
	public void backPeople(Player player, long now) {
		int commandLv = player.getCommandLv();
		if (commandLv < staticWorkShopMgr.getCommandLevel()) {
			return;
		}

		int lordLv = staticWorkShopMgr.getLordLevel();
		if (player.getLevel() < lordLv) {
			return;
		}

		Lord lord = player.getLord();
		float current = (float) lord.getPeople();
		// 如果满了这一次恢复的时间为now
		long period = now - lord.getRecoverPeopleTime();
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		long interval = staticLimit.getRecoverPeopleInterval() * TimeHelper.SECOND_MS;
		if (interval == 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("washHeroInterval is 0.");
			return;
		}

		long back = period / interval;
		if (back <= 0) {
			return;
		}

		// 如果小于0, 则上一次恢复的时间为now
		if (lord.getRecoverPeopleTime() <= 0) {
			lord.setRecoverPeopleTime(now);
		}

		int min = staticWorkShopMgr.getBasePeople(commandLv);
		int max = staticWorkShopMgr.getLimitPeople(commandLv);
		if (current <= 0) {
			lord.setPeople(min);
			current = min;
		}
		float peopleRes = current;
		if (current < min) {
			// 恢复
			for (int i = 0; peopleRes < min && i < back; i++) {
				peopleRes += (current * (float) staticLimit.getRecoverPeople() / (float) DevideFactor.FACTOR_NUM);
			}
		} else if (current > max) {
			// 逃跑
			for (int i = 0; peopleRes > max && i < back; i++) {
				peopleRes -= (current * (float) staticLimit.getEscapePeople() / (float) DevideFactor.FACTOR_NUM);
			}
		}

		lord.setPeople((int) peopleRes);
		// check recover
		lord.setRecoverPeopleTime(now);
	}

	// 增加国器碎片
	public int addCountryItem(Player player, int type, int id, int count, int maxCount, int reason) {
		itemDataManager.addItemWithLimit(player, id, count, maxCount, reason);
		// LogHelper.logAddItem(player, type, id, count, reason);

		return 0;
	}

	// 获取兵排数
	public int getSoldierLine(Player player) {
		int soldierLines = player.getSoldierLines();
		// 科技加成
		int addSoldierLine = techManager.getSoldierLine(player);

		return soldierLines + addSoldierLine;
	}

	/**
	 * 获取募兵加速
	 *
	 * @param player
	 * @return
	 */
	public float getSoldierBuff(Player player) {
		long current = System.currentTimeMillis();
		Effect effect = player.getEffects().get(LordPropertyType.RECRUIT_SOLDIERS);
		if (effect != null && effect.getBeginTime() <= current && current < effect.getEndTime()) {
			return effect.getEffect() / 100f;
		}
		return 0f;
	}

	/**
	 * 获取特效结束时间
	 *
	 * @param player
	 * @param effectId
	 * @return
	 */
	public long getEffectEndTime(Player player, int effectId) {
		long current = System.currentTimeMillis();
		Effect effect = player.getEffects().get(effectId);
		if (effect != null && effect.getBeginTime() <= current && current < effect.getEndTime()) {
			return effect.getEndTime();
		}
		return 0L;
	}

	/**
	 * 添加
	 *
	 * @param player
	 * @param effectId
	 * @param exproid
	 */
	public long addEffect(Player player, int effectId, int value, long exproid) {
		Effect effect = player.getEffects().computeIfAbsent(effectId, e -> new Effect());
		effect.setEffectId(effectId);
		effect.setEffect(value);
		long current = System.currentTimeMillis();
		if (current >= effect.getBeginTime() && current < effect.getEndTime()) {
			effect.setEndTime(effect.getEndTime() + exproid * 1000);
		} else {
			effect.setBeginTime(current);
			effect.setEndTime(current + exproid * 1000);
		}
		return effect.getEndTime();
	}

	/**
	 * 获取行军加速
	 *
	 * @param player
	 * @return
	 */
	public float getMarchBuff(Player player) {
		long current = System.currentTimeMillis();
		if (player.getEffects().containsKey(LordPropertyType.MARCH_SPEED)) {
			Effect effect = player.getEffects().get(LordPropertyType.MARCH_SPEED);
			if (current >= effect.getBeginTime() && current < effect.getEndTime()) {
				return effect.getEffect() / 100f;
			}
		}
		return 0f;
	}

	// 获取玩家坐标
	public int getMapId(Player player) {
		return worldManager.getMapId(player);
	}

	// 获取玩家的国家
	public int getPlayerCountry(long lordId) {
		Player player = getPlayer(lordId);
		if (player == null) {
			return 0;
		}
		return player.getCountry();
	}

	public void subIron(Player player, long count, int reason) {
		subAward(player, AwardType.RESOURCE, ResourceType.IRON, count, reason);
	}

	public void subCopper(Player player, long count, int reason) {
		subAward(player, AwardType.RESOURCE, ResourceType.COPPER, count, reason);
	}

	public void subOil(Player player, long count, int reason) {
		subAward(player, AwardType.RESOURCE, ResourceType.OIL, count, reason);
	}

	public void subStone(Player player, long count, int reason) {
		subAward(player, AwardType.RESOURCE, ResourceType.STONE, count, reason);
	}

	public void openMap(Player player, int mapId, int mapStatus) {
		Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
		MapStatus find = mapStatuses.get(mapId);
		if (find == null) {
			find = new MapStatus();
			find.setMapId(mapId);
			find.setStatus(mapStatus);
			mapStatuses.put(mapId, find);
			return;
		}

		find.setStatus(mapStatus);
	}

	// 同步当前玩家相关变化
	public void synAutoWall(Player player, WallDefender wallDefender, long endTime) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			SynAutoWallRq.Builder builder = SynAutoWallRq.newBuilder();
			builder.setAutoWallTimes(player.getAutoWallTimes());
			Lord lord = player.getLord();
			if (lord != null) {
				if (lord.getAutoWallTimes() <= 0) {
					lord.setOnWall(0);
				}
				builder.setOnAutoWall(lord.getOnWall());
				builder.setAutoWallTimes(lord.getAutoWallTimes());
			}
			if (wallDefender != null) {
				builder.setDefender(wallDefender.wrapPb());
			}
			builder.setEndTime(endTime);

			SynHelper.synMsgToPlayer(player, SynAutoWallRq.EXT_FIELD_NUMBER, SynAutoWallRq.ext, builder.build());
		}
	}

	// 同步当前玩家相关变化
	public void synChange(Player player, int reason) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
			// 玩家资源
			builder.setResource(player.wrapResourcePb());
			// 玩家人口
			Award people = new Award(0, AwardType.PERSON, 0, player.getPeople());
			builder.addAward(people.wrapPb());
			// 玩家荣誉
			Award honor = new Award(0, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, (int) player.getHonor());
			// 玩家英雄兵力
			builder.addAward(honor.wrapPb());
			for (Integer heroId : player.getEmbattleList()) {
				Hero hero = player.getHero(heroId);
				if (hero == null) {
					continue;
				}
				// 武将兵力
				builder.addHeroChange(hero.createHeroChange());
			}

			// 玩家兵营剩余兵力
			Map<Integer, Soldier> soldierMap = player.getSoldiers();
			for (Map.Entry<Integer, Soldier> soldierElem : soldierMap.entrySet()) {
				Soldier soldier = soldierElem.getValue();
				if (soldier == null) {
					continue;
				}
				if (soldier.getSoldierIndex() == SoldierIndex.MILITIA) {
					continue;
				}
				builder.addSoldierInfo(soldier.wrapPb());
			}

			builder.setProtectedTime(player.getProectedTime());
			builder.setLordId(player.roleId);
			SimpleData simpleData = player.getSimpleData();
			if (simpleData != null) {
				builder.setRiotItem(simpleData.getRiotItem());
				builder.setRiotScore(simpleData.getRiotScore());
			}

			SynHelper.synMsgToPlayer(player, SynChangeRq.EXT_FIELD_NUMBER, SynChangeRq.ext, builder.build());
		}
	}

	// 同步当前玩家相关变化
	public void synHeroChange(Player player, int heroId, int reason) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				return;
			}

			builder.addHeroChange(hero.createHeroChange());
			SynHelper.synMsgToPlayer(player, SynChangeRq.EXT_FIELD_NUMBER, SynChangeRq.ext, builder.build());
		}
	}

	public void synProtectedTime(Player player, int reason) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
			builder.setProtectedTime(player.getProectedTime());
			SynHelper.synMsgToPlayer(player, SynChangeRq.EXT_FIELD_NUMBER, SynChangeRq.ext, builder.build());
		}
	}

	// 同步当前玩家兵力
	public void synSoldierChange(Player player, int reason) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();

			// 兵营信息
			Map<Integer, Soldier> soldierMap = player.getSoldiers();
			for (Map.Entry<Integer, Soldier> soldierElem : soldierMap.entrySet()) {
				Soldier soldier = soldierElem.getValue();
				if (soldier == null) {
					continue;
				}
				if (soldier.getSoldierIndex() == SoldierIndex.MILITIA) {
					continue;
				}
				builder.addSoldierInfo(soldier.wrapPb());
			}

			for (Integer heroId : player.getEmbattleList()) {
				Hero hero = player.getHero(heroId);
				if (hero == null) {
					continue;
				}
				// 武将兵力
				builder.addHeroChange(hero.createHeroChange());
			}

			SynHelper.synMsgToPlayer(player, SynChangeRq.EXT_FIELD_NUMBER, SynChangeRq.ext, builder.build());
		}
	}

	public void synMapCityRq(Player target, WorldPb.SynMapCityRq builder) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, WorldPb.SynMapCityRq.EXT_FIELD_NUMBER, WorldPb.SynMapCityRq.ext, builder);
		}
	}

	public void synCityLvRq(Player target, WorldPb.SynCityLevelUpRq.Builder builder) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(target, WorldPb.SynCityLevelUpRq.EXT_FIELD_NUMBER, WorldPb.SynCityLevelUpRq.ext, builder.build());
		}
	}

	public void synChangePos(Player target, int reason) {
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
			Pos pos = target.getPos();
			builder.setNewPos(pos.wrapPb());
			SynHelper.synMsgToPlayer(target, SynChangeRq.EXT_FIELD_NUMBER, SynChangeRq.ext, builder.build());
		}
	}

	// 同步玩家的城防信息
	public void synWallInfo(Player target) {
		// 同步城防军
		if (target != null && target.isLogin && target.getChannelId() != -1) {
			WallPb.SynWallRq.Builder builder = WallPb.SynWallRq.newBuilder();
			Wall wall = target.getWall();
			if (wall == null) {
				LogHelper.CONFIG_LOGGER.info("synWallInfo wall is null!");
				return;
			}

			Map<Integer, WallDefender> wallDefenders = wall.getWallDefenders();
			for (WallDefender defender : wallDefenders.values()) {
				builder.addDefender(defender.wrapPb());
			}

			Map<Integer, WallFriend> wallFriends = wall.getWallFriends();
			for (WallFriend friend : wallFriends.values()) {
				builder.addFriend(wrapWallFriend(friend));
			}

			SynHelper.synMsgToPlayer(target, WallPb.SynWallRq.EXT_FIELD_NUMBER, WallPb.SynWallRq.ext, builder.build());
		}
	}

	public CommonPb.WallFriend.Builder wrapWallFriend(WallFriend wallFriend) {
		CommonPb.WallFriend.Builder builder = CommonPb.WallFriend.newBuilder();
		builder.setKeyId(wallFriend.getKeyId());
		builder.setLordLv(wallFriend.getLordLv());
		long lordId = wallFriend.getLordId();
		Player player = getPlayer(lordId);
		if (player != null) {
			builder.setName(player.getNick());
		} else {
			builder.setName("unkown");
		}

		builder.setHeroId(wallFriend.getHeroId());
		builder.setHeroSoldier(wallFriend.getSoldier());
		builder.setMarchId(wallFriend.getMarchId());
		builder.setLordId(wallFriend.getLordId());

		return builder;
	}

	/**
	 * 随机折扣
	 *
	 * @param player
	 */
	public ShopInfo getShopInfo(Player player) {
		ShopInfo shopInfo = player.getShopInfo();
		try {
			int today = GameServer.getInstance().currentDay;
			if (shopInfo.getTime() != today) {

				List<StaticVipBuy> discountList = staticPropMgr.getDiscountList();

				shopInfo.setTime(today);
				shopInfo.getShops().clear();

				int size = discountList.size();
				int c = 0;
				for (int i = 0; i < 3; ) {
					int index = RandomHelper.randomInSize(size);
					StaticVipBuy e = discountList.get(index);
					if (!shopInfo.getShops().contains(e.getPropId())) {
						shopInfo.getShops().add(e.getPropId());
						i++;
					}
					if (++c >= 50) {
						break;
					}
				}

			}
		} catch (Exception ex) {
			logger.error("getShopInfo error", ex);
		}
		return shopInfo;
	}

	public boolean isHeroFree(Player player, int heroId) {
		if (player.isHeroInMarch(heroId)) {
			return false;
		}

		if (player.isInMass(heroId)) {
			return false;
		}

		if (player.hasPvpHero(heroId)) {
			return false;
		}

		return true;
	}

	// 检查是否有充足的资源升级某个建筑
	public boolean hasEnoughRes(Player player, int buildingId) {
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("building Type error!");
			return false;
		}

		int currentLv = player.getBuildingLv(buildingId);
		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, currentLv + 1);
		if (staticBuildingLv == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("buildingLv is null!");
			return false;
		}

		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = player.hasResource(item);
			if (gameError != GameError.OK) {
				return false;
			}
		}

		return true;
	}

	// 获取指定的资源建筑
	public List<Integer> getBuildingRes(Player player, int resType) {
		List<Integer> buildings = new ArrayList<Integer>();
		Building building = player.buildings;
		if (building == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("builing is null!");
			return buildings;
		}

		ResBuildings resBuildings = building.getResBuildings();
		if (resBuildings == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("resBuildings is null!");
			return buildings;
		}

		Map<Integer, BuildingBase> res = resBuildings.getRes();
		if (res.isEmpty()) {
			return buildings;
		}

		for (BuildingBase buildingBase : res.values()) {
			if (buildingBase == null) {
				continue;
			}

			int buildingType = staticBuildingMgr.getBuildingType(buildingBase.getBuildingId());
			if (buildingType == Integer.MIN_VALUE) {
				com.game.util.LogHelper.CONFIG_LOGGER.info("building Type error!");
				continue;
			}

			int resourceType = staticBuildingMgr.getResourceType(buildingType);
			if (resourceType != resType) {
				continue;
			}
			buildings.add(buildingBase.getBuildingId());
		}

		return buildings;
	}

	public void synProtectedTime(Player player) {
		Lord lord = player.getLord();
		if (lord == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("lord is null!");
			return;
		}

		RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
		builder.setProtectedTime(lord.getProtectedTime());
		builder.setLordId(player.roleId);
		if (player.isLogin && player.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(player, RolePb.SynChangeRq.EXT_FIELD_NUMBER, RolePb.SynChangeRq.ext, builder.build());
		}
	}

	// 处理保护时间, 同时需要通知所有区域的人
	public void handleClearProtected(Player player) {
		if (player.getProectedTime() <= TimeHelper.curentTime()) {
			return;
		}
		player.setProtectedTime(System.currentTimeMillis());
		Lord lord = player.getLord();
		if (lord == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("lord is null!");
			return;
		}

		RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
		builder.setProtectedTime(lord.getProtectedTime());
		builder.setLordId(player.roleId);

//		int mapId = getMapId(player);
		Map<Long, Player> playerMap = getPlayers();
		for (Player curPlayer : playerMap.values()) {
			if (curPlayer == null) {
				continue;
			}

//			if (mapId != getMapId(curPlayer) && mapId != MapId.CENTER_MAP_ID) {
//				continue;
//			}

			if (curPlayer.isLogin && curPlayer.getChannelId() != -1) {
				SynHelper.synMsgToPlayer(curPlayer, RolePb.SynChangeRq.EXT_FIELD_NUMBER, RolePb.SynChangeRq.ext, builder.build());
			}
		}

	}

//    public WorldEntity addEntity(Entity entity) {
//        WorldEntity worldEntity = new WorldEntity();
//        Pos entityPos = entity.getPos();
//        Pos pos = new Pos();
//        if (entityPos != null) {
//            pos.setX(entityPos.getX());
//            pos.setY(entityPos.getY());
//        }
//        worldEntity.setPos(pos);
//        worldEntity.setId(entity.getId());
//        worldEntity.setLevel(entity.getLevel());
//        worldEntity.setEntityType(entity.getEntityType());
//        if (entity instanceof PlayerCity) {
//            PlayerCity playerCity = (PlayerCity) entity;
//
//            long lordId = playerCity.getLordId();
//            Player target = getPlayer(lordId);
//            if (target != null) {
//                worldEntity.setProtectedTime(target.getProectedTime());
//                if (!target.getNick().equals(playerCity.getName())) {
//                    playerCity.setName(target.getNick());
//                }
//
//                if (target.getCommandLv() != playerCity.getLevel()) {
//                    playerCity.setLevel(target.getCommandLv());
//                    playerCity.setCommandLv(target.getCommandLv());
//                    worldEntity.setLevel(playerCity.getLevel());
//                }
//
//
//                int callCount = target.getLord().getCallCount();
//                CtyGovern govern = countryManager.getGovern(target);
//                if (null != govern) {
//                    StaticCountryGovern staticGovern = staticCountryMgr.getGovern(govern.getGovernId(), 2);
//                    if (staticGovern != null) {
//                        callCount = staticGovern.getPerson();
//                    }
//                }
//                // 召唤信息
//                if (playerCity.getCallEndTime() != target.getLord().getCallEndTime()) {
//                    playerCity.setCallCount(callCount);
//                    playerCity.setCallEndTime(target.getLord().getCallEndTime());
//                    playerCity.setCallReply(target.getLord().getCallReply());
//                }
//
//                //虫族入侵玩家城池是否被攻破
//                boolean isBreak = isRoitBreak(target);
//                worldEntity.setBreak(isBreak);
//                worldEntity.setAttack(isRoitAttack(target));
//            }
//
//            worldEntity.setName(playerCity.getName());
//            worldEntity.setCountry(playerCity.getCountry());
//            worldEntity.setCallCount(playerCity.getCallCount());
//            worldEntity.setCallReply(playerCity.getCallReply());
//            worldEntity.setCallEndTime(playerCity.getCallEndTime());
//            worldEntity.setSkin(target.getLord().getSkin());
//        } else if (entity instanceof com.game.worldmap.Resource) {
//            // 当前资源点是否有行军
//            int mapId = worldManager.getMapId(pos);
//            MapInfo mapInfo = worldManager.getMapInfo(mapId);
//            if (mapInfo != null) {
//                March march = mapInfo.getMarch(pos);
//                if (march != null) {
//                    Player target = getPlayer(march.getLordId());
//                    if (target != null) {
//                        worldEntity.setCountry(target.getCountry());
//                        worldEntity.setName(target.getNick());
//                    }
//                }
//            }
//
//        } else if (entity instanceof RebelMonster) {
//            worldEntity.setCreateTime(((RebelMonster) entity).getCreateTime());
//        }
//
//        return worldEntity;
//    }

	// 处理没有战斗的行军
	public void handlePlayerMarch(Player player) {
		ConcurrentLinkedDeque<March> marches = player.getMarchList();
		Iterator<March> marchIterator = marches.iterator();
		while (marchIterator.hasNext()) {
			March march = marchIterator.next();
			if (march.getState() == MarchState.Done) {
				marchIterator.remove();
				com.game.util.LogHelper.GAME_DEBUG.debug("行军移除->[{}]", player.roleId, march.getKeyId());
				Pos endPos = march.getEndPos();
				if (endPos != null) {
					int marchMapId = worldManager.getMapId(endPos);
					worldManager.removeMarch(marchMapId, march);
				}
			}

		}

	}

	// 同步当前玩家相关变化
	public void synAutoBuild(Player player, int reason) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
			builder.setOnBuild(player.getOnBuild());
			SynHelper.synMsgToPlayer(player, SynChangeRq.EXT_FIELD_NUMBER, SynChangeRq.ext, builder.build());
		}
	}

	// 同步升级礼包
	public void synLevelupAwards(Player player) {
		if (player == null) {
			return;
		}

		if (!player.isLogin) {
			return;
		}

		if (player.getChannelId() == -1) {
			return;
		}

		RolePb.SynLevelupAwardsRq.Builder builder = RolePb.SynLevelupAwardsRq.newBuilder();
		builder.addAllAwards(PbHelper.createLevelAwards(player));
		SynHelper.synMsgToPlayer(player, RolePb.SynLevelupAwardsRq.EXT_FIELD_NUMBER, RolePb.SynLevelupAwardsRq.ext,
			builder.build());
	}

	public boolean isEquipFull(List<List<Integer>> awardList, Player player) {
		if (awardList == null) {
			return false;
		}

		int size = awardList.size();
		int equipCount = 0;
		for (int i = 0; i < size; i++) {
			List<Integer> e = awardList.get(i);
			int type = e.get(0);
			int count = e.get(2);
			if (type == AwardType.EQUIP) {
				equipCount += count;
			}
		}

		// 判断背包满
		if (equipCount > 0) {
			int freeSlot = equipManager.getFreeSlot(player);
			if (freeSlot < equipCount) {
				return true;
			}
		}

		return false;
	}

	public boolean isEquipFulls(List<CommonPb.Award> awardList, Player player) {
		if (awardList == null) {
			return false;
		}

		int size = awardList.size();
		int equipCount = 0;
		for (int i = 0; i < size; i++) {
			CommonPb.Award e = awardList.get(i);
			int type = e.getType();
			int count = e.getCount();
			if (type == AwardType.EQUIP) {
				equipCount += count;
			}
		}

		// 判断背包满
		if (equipCount > 0) {
			int freeSlot = equipManager.getFreeSlot(player);
			if (freeSlot < equipCount) {
				return true;
			}
		}

		return false;
	}

	public Shop getVipShop(Player player, int propId) {
		Shop shop = player.getShops().get(propId);
		if (shop == null) {
			shop = new Shop();
			shop.setPropId(propId);
			shop.setTime(GameServer.getInstance().currentDay);
			player.getShops().put(propId, shop);
		} else if (shop.getTime() != GameServer.getInstance().currentDay) {
			shop.setFree(0);
			shop.setBuyCount(0);
			shop.setTime(GameServer.getInstance().currentDay);
		}
		return shop;
	}

	public boolean isEquipError(List<List<Long>> awardList, Player player) {
		if (awardList == null) {
			return false;
		}

		int size = awardList.size();
		int equipCount = 0;
		for (int i = 0; i < size; i++) {
			List<Long> e = awardList.get(i);
			Long type = e.get(0);
			Long count = e.get(2);
			if (type == AwardType.EQUIP) {
				equipCount += count;
			}
		}

		// 判断背包满
		if (equipCount > 0) {
			int freeSlot = equipManager.getFreeSlot(player);
			if (freeSlot < equipCount) {
				return true;
			}
		}

		return false;
	}

	// 1.检查开放兄弟、州城[世界BOSS1]、2.州城迁城[cityType == CityType.CAMP]、3.开放所有地图[世界BOSS2]
	// 开启世界一瞬间开启状态
	@Deprecated
	public void checkStatus(Player player) {
		int mapId = worldManager.getMapId(player);
		// 玩家至少进入世界才检查世界地图
		if (mapId == 0) {
			return;
		}

		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("worldData is null");
			return;
		}

		WorldBoss worldBoss = worldData.getWorldBoss(1);
		if (worldBoss == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("worldBoss is null!");
			return;
		}

		// 1.检查开放兄弟、州城[世界BOSS1]
		if (worldBoss.isKilled()) {
			List<MapStatus> mapStatuses = worldManager.checkPrimaryMap(player);
			if (!mapStatuses.isEmpty()) {
				player.updateMapStatuses(mapStatuses);
			}

		}

		// 2.州城迁城[cityType == CityType.CAMP]
		if (cityManager.captureCity(CityType.CAMP)) {
			List<MapStatus> mapStatuses = worldManager.checkMiddleMap(player);
			if (!mapStatuses.isEmpty()) {
				player.updateMapStatuses(mapStatuses);
			}
		}

		// 3.开放所有地图[世界BOSS2]
		WorldBoss shareBoss = worldData.getShareBoss();
		if (shareBoss != null && shareBoss.isKilled()) {
			List<MapStatus> mapStatuses = worldManager.checkAllMap(player);
			if (!mapStatuses.isEmpty()) {
				player.updateMapStatuses(mapStatuses);
			}
		}

	}

	/**
	 * 根据世界进程来刷玩家的地图状态
	 *
	 * @param player
	 */
	public void checkMapStatusByWorldTarget(Player player) {
		int mapId = worldManager.getMapId(player);
		// 玩家至少进入世界才检查世界地图
		if (mapId == 0) {
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		WorldTargetTask worldTargetTask = worldData.getTasks().get(MapOpenByTargetEnum.STEP_ONE.getWorldTargetId());
		if (worldTargetTask != null) {
			openMapStepOne(player);
		}
		worldTargetTask = worldData.getTasks().get(MapOpenByTargetEnum.STEP_TWO.getWorldTargetId());
		if (worldTargetTask != null) {
			openMapStepTwo(player);
		}
		worldTargetTask = worldData.getTasks().get(MapOpenByTargetEnum.STEP_THREE.getWorldTargetId());
		if (worldTargetTask != null) {
			openMapStepThree(player);
		}

		worldTargetTask = worldData.getTasks().get(MapOpenByTargetEnum.STEP_FOUR.getWorldTargetId());
		if (worldTargetTask != null) {
			openMapStepFour(player);
		}

	}

	/**
	 * 一阶段地图开启
	 *
	 * @param player
	 */
	public void openMapStepOne(Player player) {
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		MapInfo mapInfo = worldManager.getMapInfo(worldManager.getMapId(player));
		for (Map.Entry<Integer, StaticWorldMap> entry : worldMap.entrySet()) {
			// 1阶段只开启自己所在的地图
			if (mapInfo.getMapId() == entry.getKey().intValue()) {
				player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 2));
			} else {
				player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 0));

			}
		}
	}

	/**
	 * 二阶段地图开启
	 *
	 * @param player
	 */
	public void openMapStepTwo(Player player) {
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		for (Map.Entry<Integer, StaticWorldMap> entry : worldMap.entrySet()) {
			// 开启所有高原和平原
			StaticWorldMap staticWorldMap = entry.getValue();
			if (staticWorldMap.getAreaType() == 3) {
				continue;
			}
			player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 2));
		}
//        MapInfo mapInfo = worldManager.getMapInfo(worldManager.getMapId(player));
//        StaticWorldMap staticWorldMap = worldMap.get(mapInfo.getMapId());
//        for (Map.Entry<Integer, StaticWorldMap> entry : worldMap.entrySet()) {
//            //如果当前地图在区域2  要反向查找1区域的地图
//            if (staticWorldMap.getAreaType() == 2) {
//                if (entry.getValue().getBelong() == staticWorldMap.getMapId() || entry.getValue().getMapId() == staticWorldMap.getMapId()) {
//                    player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 2));
//                } else {
//                    player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 0));
//                }
//            } else if (staticWorldMap.getAreaType() == 1) {
//                //跟当前区域的belong相等，或者mapId跟当前区域的belong相等 全部开启可迁城
//                if (staticWorldMap.getBelong() == entry.getValue().getBelong() || staticWorldMap.getBelong() == entry.getKey().intValue()) {
//                    player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 2));
//                } else {
//                    player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 0));
//                }
//            } else {
//                player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 0));
//
//            }
//        }
	}

	/**
	 * 三阶段地图开启
	 *
	 * @param player
	 */
	public void openMapStepThree(Player player) {
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		for (Map.Entry<Integer, StaticWorldMap> entry : worldMap.entrySet()) {
			if (entry.getValue().getAreaType() == 2) {
				MapStatus mapStatus = player.getMapStatusMap().get(entry.getKey());
				if (mapStatus != null && mapStatus.getStatus() == 2) {
					continue;
				}
				player.getMapStatusMap().put(entry.getKey(), new MapStatus(entry.getKey(), 1));
			}
		}
	}

	/**
	 * 四阶段地图开启
	 *
	 * @param player
	 */
	public void openMapStepFour(Player player) {
		for (MapStatus mapStatus : player.getMapStatusMap().values()) {
			mapStatus.setStatus(2);
		}
	}

	/**
	 * 世界进程开启的时候地图变化
	 *
	 * @param worldTargetId
	 */
	public void openTargetToMap(int worldTargetId) {
		MapOpenByTargetEnum mapOpenByTargetEnum = MapOpenByTargetEnum.getMapOpenByTargetEnum(worldTargetId);
		if (mapOpenByTargetEnum == null) {
			return;
		}
		for (Player player : getPlayers().values()) {
			if (!player.isOnline()) {
				continue;
			}
			switch (mapOpenByTargetEnum) {
				case STEP_ONE:
					openMapStepOne(player);
					break;
				case STEP_TWO:
					openMapStepTwo(player);
					break;
				case STEP_THREE:
					openMapStepThree(player);
					break;
				case STEP_FOUR:
					openMapStepFour(player);
					break;
			}
			worldManager.synWorldMapStatus(player);
		}
	}

	// 检测资源是否已满
	public boolean isResourceFull(Player player, Map<Integer, Long> resAdd) {
		if (player == null) {
			return false;
		}

		Resource resource = player.getResource();
		Map<Integer, Long> resMap = resource.getResource();
		Map<Integer, Long> resLimit = staticLimitMgr.getResourceLimit();
		for (Map.Entry<Integer, Long> entry : resAdd.entrySet()) {
			if (entry == null) {
				continue;
			}
			Long currentValue = resMap.get(entry.getKey());
			if (currentValue == null) {
				currentValue = 0L;
			}
			Long limit = resLimit.get(entry.getKey());
			if (entry.getValue() + currentValue >= limit) {
				return true;
			}
		}

		return false;
	}

	// 检查资源是否超过上限
	public boolean isPlayerResFull(Player player) {
		if (player == null) {
			return false;
		}

		Resource resource = player.getResource();

		// 每种资源检查上限
		Map<Integer, Long> resourceMap = resource.getResource();
		for (int resType = ResourceType.IRON; resType <= ResourceType.STONE; resType++) {
			long limitNum = staticLimitMgr.getLimitNum(resType);
			Long resNum = resourceMap.get(resType);
			if (resNum >= limitNum) {
				return true;
			}
		}

		return false;
	}

	public void caculateAllScore(Player player) {
		heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
		buildingManager.caculateBattleScore(player);

	}

	public void addBuildTeamTime(Player player, long count, int reason) {
		if (player == null) {
			return;
		}

		Lord lord = player.getLord();
		if (lord == null) {
			return;
		}

		long now = System.currentTimeMillis();
		if (lord.getBuildTeamTime() <= now) {
			lord.setBuildTeamTime(now);
		}
		long buildTeamTime = lord.getBuildTeamTime();
		lord.setBuildTeamTime(buildTeamTime + count * TimeHelper.SECOND_MS);
	}

	public boolean isNickOk(String nick) {
		if (nick == null || nick.isEmpty() || nick.length() > 20 || EmojiHelper.containsEmoji(nick) || nick.contains("|") || nick.contains("=") || nick.contains(":") || nick.contains("&")) {
			return false;
		}

		// 判断是否含有utf16
		if (EmojiHelper.isNotUtf8(nick)) {
			return false;
		}

		nick = EmojiHelper.filterEmoji(nick);
		if (nick.contains("*")) {//名字不能包含表情
			return false;
		}

		// 判断是否含有敏感词
		if (staticSensitiveWordMgr.containSensitiveWord(nick, "privateChatFilter")) {
			return false;
		}

		return true;
	}

	public int getLordCountry(long lordId) {
		Player player = getPlayer(lordId);
		if (player == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("player is null.");
			return 1;
		}

		return player.getCountry();
	}

	public float getBuffAdd(Player player, int buffId) {
		HashMap<Integer, Buff> buffMap = player.getSimpleData().getBuffMap();
		Buff buff = buffMap.get(buffId);
		if (buff == null) {
			return 0.0f;
		}

		if (buff.getEndTime() < System.currentTimeMillis()) {
			return 0.0f;
		}

		float factor = buff.getValue();
		factor = Math.max(0, factor);
		factor = Math.min(1.0f, factor);
		return factor;
	}


	/**
	 * 叛军暴乱
	 *
	 * @param player
	 * @param report
	 * @param mailId
	 * @return
	 */
	public Mail sendRoitReportMail(Player player, Report report, ReportMsg reportMsg, int mailId, List<Award> awards, HashMap<Integer, Integer> soldierRecMap, List<CommonPb.RiotAssist> riotAssists, String... param) {
		Mail mail = mailManager.addMail(player, mailId, param);
		if (mail == null) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("sendReport mail failed, mail = null, mailId = " + mailId);
			return null;
		}

		if (report != null) {
			mail.setReport(report.wrapPb().build());
		} else {
			com.game.util.LogHelper.CONFIG_LOGGER.info("sendReport mail failed, report = null, mailId = " + mailId);
		}

		if (reportMsg != null) {
			mail.setReportMsg(reportMsg.wrapPb().build());
		} else {
			com.game.util.LogHelper.CONFIG_LOGGER.info("sendReport mail failed, reportMsg = null, mailId = " + mailId);
		}

		if (awards != null && !awards.isEmpty()) {
			List<CommonPb.Award> awardList = PbHelper.createAwardList(awards);
			mail.setAward(awardList);

			if (mailId == MailId.KILL_REBEL_WIN) {
				for (CommonPb.Award e : awardList) {
					if (e.getType() == AwardType.PROP) {
						mail.getShowAward().add(e);
					}
				}
			}
		}

		// 伤兵恢复
		if (soldierRecMap != null && !soldierRecMap.isEmpty()) {
			List<CommonPb.SoldierRec> soldierRecs = new ArrayList<CommonPb.SoldierRec>();
			float soldierAdd = getBuffAdd(player, BuffId.SOLDIER_REC);
			for (Map.Entry<Integer, Integer> entry : soldierRecMap.entrySet()) {
				int soliderNum = entry.getValue();
				int rec = (int) (soldierAdd * (float) soliderNum);
				if (rec == 0) {
					continue;
				}

				CommonPb.SoldierRec.Builder soldierRec = CommonPb.SoldierRec.newBuilder();
				soldierRec.setSoldierType(entry.getKey());
				soldierRec.setSoldierNum(rec);
				soldierRecs.add(soldierRec.build());
				addAward(player, AwardType.SOLDIER, entry.getKey(), rec, Reason.SOLDIER_REC);
			}

			if (!soldierRecs.isEmpty()) {
				mail.setSoldierRecs(soldierRecs);
			}
		}

		if (riotAssists != null && !riotAssists.isEmpty()) {
			mail.setRiotAssist(riotAssists);
		}

		synMailToPlayer(player, mail);
		SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
		return mail;
	}

	/**
	 * Function:主角增加暴乱信物
	 */
	public boolean addRiotItem(Player player, int roitItem, int reason) {
		if (roitItem <= 0) {
			return false;
		}
		SimpleData simpleData = player.getSimpleData();
		simpleData.setRiotItem(simpleData.getRiotItem() + roitItem);
		riotManager.synRiotBuff(player);
		return true;
	}

	/**
	 * Function:减少增加信物
	 */
	public boolean subRiotItem(Player player, int roitItem, int reason) {
		if (roitItem <= 0) {
			return false;
		}

		SimpleData simpleData = player.getSimpleData();
		int res = simpleData.getRiotItem() - roitItem;
		res = Math.max(0, res);
		simpleData.setRiotItem(res);

		return true;
	}

	/**
	 * Function:主角增加暴乱积分
	 */
	public boolean addRiotScore(Player player, int roitScore, int reason) {
		if (roitScore <= 0) {
			return false;
		}
		SimpleData simpleData = player.getSimpleData();
		simpleData.setRiotScore(simpleData.getRiotScore() + roitScore);
		riotManager.synRiotBuff(player);
		return true;
	}

	/**
	 * Function:减少增加积分
	 */
	public boolean subRiotScore(Player player, int roitScore, int reason) {
		if (roitScore <= 0) {
			return false;
		}

		SimpleData simpleData = player.getSimpleData();
		int res = simpleData.getRiotScore() - roitScore;
		res = Math.max(0, res);
		simpleData.setRiotScore(res);

		return true;
	}

	public void addCollectTimes(Player player, int count) {
		if (count <= 0) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("addCollectTimes <= 0.");
			return;
		}
		Lord lord = player.getLord();
		int collectTimes = lord.getCollectTimes();
		int addRes = collectTimes + count;
		addRes = Math.min(99, addRes);
		addRes = Math.max(0, addRes);
		lord.setCollectTimes(addRes);
	}

	public List<Integer> getNotRead(Player player) {
		List<Integer> notRead = new ArrayList<Integer>();
		notRead.add(0);
		notRead.add(0);
		notRead.add(0);
		ConcurrentLinkedDeque<Mail> mails = player.getMails();
		for (Mail mail : mails) {
			if (mail == null) {
				continue;
			}

			if (mail.getState() != MailConst.UN_READ) {
				continue;
			}

			StaticMail staticMail = staticMailDataMgr.getStaticMail(mail.getMailId());
			if (staticMail == null) {
				continue;
			}

			if (staticMail.getType() == MailConst.REPORT_MAIL) {
				notRead.set(0, notRead.get(0) + 1);
			} else if (staticMail.getType() == MailConst.SYS_MAIL) {
				notRead.set(1, notRead.get(1) + 1);
			} else if (staticMail.getType() == MailConst.PERSON_MAIL) {
				notRead.set(2, notRead.get(2) + 1);
			}
		}

		return notRead;
	}

	/**
	 * 同步effect
	 *
	 * @param player
	 */
	public void synEffects(Player player) {
		RolePb.SynEffectsRq.Builder builder = RolePb.SynEffectsRq.newBuilder();
		builder.addAllEffects(player.wrapEffects());
		SynHelper.synMsgToPlayer(player, RolePb.SynEffectsRq.EXT_FIELD_NUMBER, RolePb.SynEffectsRq.ext, builder.build());
	}

	/**
	 * @param player
	 */
	public void synBeautySTimes(Player player) {
		BeautyPb.SynBeautySGameTimeRs.Builder builder = BeautyPb.SynBeautySGameTimeRs.newBuilder();
		builder.setSGameTimes(player.getLord().getsGameTimes());
		SynHelper.synMsgToPlayer(player, SynBeautySGameTimeRs.EXT_FIELD_NUMBER, SynBeautySGameTimeRs.ext, builder.build());
	}

	public void synUnlockingBeautyRs(Player player, int beautyId) {
		SynUnlockingBeautyRs.Builder builder = SynUnlockingBeautyRs.newBuilder();
		builder.setBeautyId(beautyId);
		SynHelper.synMsgToPlayer(player, SynUnlockingBeautyRs.EXT_FIELD_NUMBER, SynUnlockingBeautyRs.ext, builder.build());
	}

	public Map<Long, Friend> getMaster(Player player, int type) {
		if (player.getFriends() == null || player.getFriends().get(type) == null) {
			player.getFriends().put(type, new HashMap<>());
		}
		return player.getFriends().get(type);
	}

	public int getPlayerIsDelete(Lord lord) {
		return serverId;
	}

	/**
	 * 检测错误的预备兵营 改成正常的
	 *
	 * @param player
	 */
	public void checkMilitiaCamp(Player player) {
		Building building = player.buildings;
		Camp camp = building.getCamp();
		BuildingBase base = camp.getBuilding(BuildingId.MILITIA_CAMP);
		if (base != null) {
			if (player.getSoldiers().get(SoldierIndex.MILITIA) == null) {
				Map<Integer, StaticSoldierLv> staticSoldierLvMap = staticSoldierMgr.getSoldierLvMap();
				StaticSoldierLv staticSoldierLv = staticSoldierLvMap.get(1);
				Soldier soldier = new Soldier();
				soldier.setSoldierType(1);
				soldier.setNum(0);
				soldier.setCapacity(staticSoldierLv.getCapacity());
				soldier.setLargerTimes(0);
				soldier.setEmployeeTimes(0);
				soldier.setSoldierIndex(SoldierIndex.MILITIA);
				player.getSoldiers().put(SoldierIndex.MILITIA, soldier);
			}
		}
	}

	public boolean isRoitBreak(Player player) {
		if (player == null) {
			return false;
		}
		SimpleData simpleData = player.getSimpleData();
		if (simpleData == null) {
			return false;
		}
		return simpleData.isWaveContinue();
	}

	/**
	 * 虫族入侵 城池是否被攻击 被攻击显示旗子
	 *
	 * @param player
	 * @return
	 */
	public boolean isRoitAttack(Player player) {
		if (player == null) {
			return false;
		}
		// 虫族入侵
		SimpleData simpleData = player.getSimpleData();
		if (simpleData != null) {
			if (simpleData.getRiotWar() != null) {
				if (simpleData.getRiotWar().size() > 0) {
					return true;
				}
			}
		}
//        int mapId = worldManager.getMapId(player.getPos());
//        MapInfo mapInfo = worldManager.getMapInfo(mapId);
//        // 闪电
//        List<WarInfo> quickWar = new ArrayList<>(mapInfo.getQuickWarMap().values());
//        boolean hasQuickWar = quickWar.stream().anyMatch(e -> e.getDefencerId() == player.roleId && e.getState() != MarchState.Back);
//        if (hasQuickWar) {
//            return true;
//        }
//        //远征或者奔袭
//        List<WarInfo> cityWar = new ArrayList<>(mapInfo.getCityWarMap().values());
//        boolean hasCityWar = cityWar.stream().anyMatch(e -> e.getDefencerId() == player.roleId && e.getState() != MarchState.Back);
//        if (hasCityWar) {
//            return true;
//        }
		return false;
	}

	public void copyPlayer(Player target, long copyId) {
		Player res = getPlayer(copyId);
		if (res == null || res.getLord().getLordId() == target.getLord().getLordId()) {
			return;
		}
		copyPlayer(res, target);
	}

	/**
	 * 复制玩家信息
	 *
	 * @param res
	 * @param copy
	 */
	private void copyPlayer(Player res, Player copy) {
		// 复制角色基础数据
		Lord copyLord = res.getLord().clone();
		copyLord.setLordId(copy.getLord().getLordId());
		nickManager.setPlayerNick(copyLord, nickManager.getNewNick());
		copyLord.setSex(copy.getLord().getSex());
		copyLord.setPosX(copy.getLord().getPosX());
		copyLord.setPosY(copy.getLord().getPosY());
		copyLord.setCountry(copy.getLord().getCountry());
		copy.setLord(copyLord);
		// 复制建筑
		copy.buildings = res.buildings.clone();
		// 复制角色资源数据
		copy.setResource(res.getResource().clone());
		// 复制世界宝箱
		copy.setPWorldBox(res.getPWorldBox().clone());
		// 复制道具
		copy.getItemMap().clear();
		res.getItemMap().forEach((key, value) -> {
			copy.getItemMap().put(key, value.clone());
		});
		// 复制将领集合
		copy.getHeros().clear();
		res.getHeros().forEach((key, value) -> {
			copy.getHeros().put(key, value.clone());
		});
		// 复制装备背包
		copy.getEquips().clear();
		res.getEquips().forEach((key, value) -> {
			copy.getEquips().put(key, value.clone());
		});
		// 复制兵书背包
		copy.getWarBooks().clear();
		res.getWarBooks().forEach((key, value) -> {
			copy.getWarBooks().put(key, value.clone());
		});
		// 复制上阵武将List Id
		copy.getEmbattleList().clear();
		res.getEmbattleList().forEach(e -> {
			copy.getEmbattleList().add(e);
		});
		// 复制采集武将列表
		copy.getMiningList().clear();
		res.getMiningList().forEach(e -> {
			copy.getMiningList().add(e);
		});
		// 复制扫荡武将列表
		copy.getSweepHeroList().clear();
		res.getSweepHeroList().forEach(e -> {
			copy.getSweepHeroList().add(e);
		});
		// 复制城防武将列表
		copy.getDefenseArmyList().clear();
		res.getDefenseArmyList().forEach(e -> {
			copy.getDefenseArmyList().add(e.clone());
		});
		// 复制士兵信息
		copy.getSoldiers().clear();
		res.getSoldiers().forEach((key, value) -> {
			copy.getSoldiers().put(key, value.clone());
		});
		// 复制关卡信息:key1 : mapId, key2: missionId
		copy.getMissions().clear();
		res.getMissions().forEach((key, value) -> {
			copy.getMissions().computeIfAbsent(key, e -> new HashMap<>());
			value.forEach((key1, value1) -> {
				copy.getMissions().get(key).put(key1, value1.clone());
			});
		});
		// 复制雇工信息
		copy.setEmployInfo(res.getEmployInfo().clone());
		// 复制聚宝盆补给站
		copy.getDepots().clear();
		res.getDepots().forEach(e -> {
			copy.getDepots().add(e.clone());
		});
		// 复制vip礼包
		copy.getVipGifts().clear();
		res.getVipGifts().forEach(e -> {
			copy.getVipGifts().add(e);
		});
		// 复制vip特价商品
		copy.getShops().clear();
		res.getShops().forEach((key, value) -> {
			copy.getShops().put(key, value.clone());
		});
		// 复制任务
		copy.getTaskMap().clear();
		res.getTaskMap().forEach((key, value) -> {
			copy.getTaskMap().put(key, value.clone());
		});
		// 复制神器
		copy.getKillEquipMap().clear();
		res.getKillEquipMap().forEach((key, value) -> {
			copy.getKillEquipMap().put(key, value.clone());
		});
		// 复制活动
		copy.activitys.clear();
		res.activitys.forEach((key, value) -> {
			copy.activitys.put(key, value.clone());
		});
		// 复制国家信息
		copy.setNation(res.getNation().clone());
		// 复制商店的相关信息
		copy.setShopInfo(res.getShopInfo().clone());
		// 复制玩家地图开放的状态
		copy.getMapStatusMap().clear();
		res.getMapStatusMap().forEach((key, value) -> {
			copy.getMapStatusMap().put(key, value.clone());
		});
		// 复制buff
		copy.getEffects().clear();
		res.getEffects().forEach((key, value) -> {
			copy.getEffects().put(key, value.clone());
		});
		// 复制玩家世界目标的领奖状态
		copy.getWorldTargetAwardMap().clear();
		res.getWorldTargetAwardMap().forEach((key, value) -> {
			copy.getWorldTargetAwardMap().put(key, value.clone());
		});
		// 复制美女信息
		copy.getBeautys().clear();
		res.getBeautys().forEach((key, value) -> {
			copy.getBeautys().put(key, value.clone());
		});
		// 复制配饰背包信息
		copy.getOmaments().clear();
		res.getOmaments().forEach((key, value) -> {
			copy.getOmaments().put(key, value);
		});
		// 复制玩家穿戴配饰信息
		copy.getPlayerOmaments().clear();
		res.getPlayerOmaments().forEach((key, value) -> {
			copy.getPlayerOmaments().put(key, value.clone());
		});
		// 复制头像框
		copy.getFrameMap().clear();
		res.getFrameMap().forEach((key, value) -> {
			copy.getFrameMap().put(key, value.clone());
		});
		// 复制日常任务
		copy.setPlayerDailyTask(res.getPlayerDailyTask().clone());
		// 复制已经触发过的新手引导
		copy.getNewStateDone().clear();
		res.getNewStateDone().forEach(key -> {
			copy.getNewStateDone().add(key);
		});
		// 复制升级奖励
		copy.getLevelAwardsMap().clear();
		res.getLevelAwardsMap().forEach((key, value) -> {
			copy.getLevelAwardsMap().put(key, value.clone());
		});
		// 复制已经完成的任务
		copy.getFinishedTask().clear();
		res.getFinishedTask().forEach(value -> {
			copy.getFinishedTask().add(value);
		});
		// 复制星级奖励
		copy.getMissionStar().clear();
		res.getMissionStar().forEach((key, value) -> {
			copy.getMissionStar().computeIfAbsent(key, e -> new TreeMap<>());
			value.forEach((key1, value1) -> {
				copy.getMissionStar().get(key).put(key1, value1);
			});
		});
		// 复制世界目标的个人任务目标
		copy.getPersonalGoals().clear();
		res.getPersonalGoals().forEach((key, value) -> {
			copy.getPersonalGoals().put(key, value.clone());
		});
		// 复制市场资源打包信息
		copy.getResPackets().clear();
		res.getResPackets().forEach((key, value) -> {
			copy.getResPackets().put(key, value.clone());
		});
		// 复制当前作战研究室的任务
		copy.setMeetingTask(res.getMeetingTask().clone());
		// 复制玩家td通关信息
		copy.getTdMap().clear();
		res.getTdMap().forEach((key, value) -> {
			copy.getTdMap().put(key, value.clone());
		});
		// 复制玩家塔防战力开启信息
		copy.getTdBouns().clear();
		res.getTdBouns().forEach((key, value) -> {
			copy.getTdBouns().put(key, value);
		});
		// 复制ui打开记录
		copy.getRecordList().clear();
		res.getRecordList().forEach(e -> {
			copy.getRecordList().add(e);
		});
		// 复制周卡过期时间
		copy.setWeekCard(res.getWeekCard().clone());
		// 复制玩家主城皮肤
		copy.getCommandSkins().clear();
		res.getCommandSkins().forEach((key, value) -> {
			copy.getCommandSkins().put(key, value.clone());
		});
		SpringUtil.getBean(AccountService.class).synOffline(copy, 1);
	}

	@Deprecated
	public void checkResource(Player player) {
		Building buildings = player.buildings;
		if (buildings == null) {
			return;
		}
		ResBuildings resBuildings = buildings.getResBuildings();
		for (StaticBuilding staticBuilding : staticBuildingMgr.getBuildingMap().values()) {
			if (staticBuilding.getRebuildingId().size() == 0) {
				continue;
			}
			// 有这个建筑
			BuildingBase base = resBuildings.getBuilding(staticBuilding.getBuildingId());
			for (List<Integer> l : staticBuilding.getRebuildingId()) {
				int rebuildId = l.get(1);
				BuildingBase rebuildBase = resBuildings.getBuilding(rebuildId);
				if (base != null && rebuildBase != null) {
					// 表示两个都有删除前一个
					resBuildings.removeBuilding(base.getBuildingId());
					break;
				}
			}
		}
	}

	public void clearPos(Pos pos) {
//        String key = pos.getX() + "," + pos.getY();
//        List<Player> list = Lists.newArrayList(getAllOnlinePlayer().values());
//        list.forEach(player -> {
//            player.getPushPos().remove(key);
//        });
	}

	public long getAutoKillEndTime(Player player) {
		Effect effect = player.getEffects().get(LordPropertyType.AUTO_KILL_MONSTGER);
		if (effect == null) {
			return 0L;
		}
		if (effect.getEndTime() < System.currentTimeMillis()) {
			return 0L;
		}
		return effect.getEndTime();
	}

	public void updateItem(Item item) {
		itemDao.replaceItem(item);
	}

	/**
	 * 反序列化背包
	 */
	private void dserItem(Player player, byte[] roleData) {
		SerializePb.SerData ser = null;
		if (roleData != null) {
			try {
				ser = SerializePb.SerData.parseFrom(roleData);
			} catch (InvalidProtocolBufferException e) {
				com.game.util.LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
			}
		}
		// 判定二进制中是否存在道具
		List<CommonPb.Prop> list = ser.getPropList();
		if (list.size() > 0) {
			for (CommonPb.Prop e : list) {
				if (e.getPropId() == 0) {
					continue;
				}
				// TODO 容错
				if (e.getPropId() == 10101 || e.getPropId() == 10102) {
					continue;
				}
				player.getItemMap().put(e.getPropId(), new Item(e));
			}
		} else {
			// 直接从数据库中加载
			List<Item> items = itemDao.loadAllItem(player.roleId);
			items.forEach(e -> {
				player.getItemMap().put(e.getItemId(), e);
			});
		}

	}

	// 如果是科技学院升级，升级时检测玩家身上如果有钻石购买的科学家，则给他升级为目前解锁的最高等级的科学家
	public void changeTech(Player player) {
		int researcherId = player.getEmployInfo().getResearcherId();
		if (researcherId != 0) {
			StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(researcherId);
			if (staticEmployee == null) {
				return;
			}
			StaticEmployee employee = staticBuildingMgr.getEmployee(staticEmployee.getNextId());
			if (employee != null && employee.getTechLv() == player.getTechLv()) {
				Employee remove = player.getEmployInfo().getEmployeeMap().remove(researcherId);
				remove.setEmployeeId(employee.getEmployId());
				player.getEmployInfo().getEmployeeMap().put(remove.getEmployeeId(), remove);
				player.getEmployInfo().setResearcherId(remove.getEmployeeId());
			}
		}
	}

	/**
	 * 清理用户数据
	 *
	 * @param player
	 */
	public void cleanPlayer(Player player) {
		Account account = player.account;
		Lord lord = player.getLord();
		player.account.setIsDelete(1);
		accountDao.deleteIordId(account);
		com.game.util.LogHelper.GAME_LOGGER.error("逻辑删除玩家lordId = " + player.roleId);

		Pos playerPos = player.getPos();
		int mapId = worldManager.getMapId(playerPos);
		MapInfo oldMapInfo = worldManager.getMapInfo(mapId);
		if (oldMapInfo == null) {
			return;
		}
		worldManager.removePlayerCity(player);// 删除玩家城池
		com.game.util.LogHelper.GAME_LOGGER.error("逻辑删除玩家并且移除城池lordId->[{}] 原来坐标->[{}] ", player.roleId, playerPos.toPosStr());

		//Integer playerNum = oldMapInfo.getMapPlayerNum().get(lord.getCountry());
		//// 区服导量人数减一
		//oldMapInfo.getMapPlayerNum().put(playerNum, playerNum - 1);
		lord.setPosX(-1);
		lord.setPosY(-1);
		//playerCache.remove(lord.getLordId());// 已经注册的玩家
		//updateRole();
		savePlayerServer.saveData(new Role(player));
	}

	@Autowired
	SavePlayerServer savePlayerServer;

	/**
	 * 离线玩家登录刷新主城小虫子
	 *
	 * @param player
	 */
	public void refushWorms(Player player) {
		SmallCityGame smallCityGame = player.getSmallCityGame();
		if (smallCityGame == null) {
			return;
		}
		smallCityGame.refushAll();
		if (smallCityGame.getLastRefushTime() == 0) {
			smallCityGame.setLastRefushTime(TimeHelper.getZeroOfDay() / TimeHelper.SECOND_MS);
		}
		long curentTimeSecond = TimeHelper.getCurrentSecond();
		// 五分钟刷一只
		if (smallCityGame.getLastRefushTime() >= curentTimeSecond) {
			return;
		}
		int maxTimes = cityGameManager.getMaxTimes();
		// 奖励是否满了
		if (smallCityGame.getTotal() >= maxTimes) {
			return;
		}
		int maxWorms = staticLimitMgr.getNum(SimpleId.CITY_GAME_WORMS);
		// 虫子是否满了
		if (smallCityGame.getWorms().size() >= maxWorms) {
			return;
		}
		// 距离上次刷新间隔时间
		long lostTime = curentTimeSecond - smallCityGame.getLastRefushTime();
		// 虫子刷新时间
		int configTime = staticLimitMgr.getNum(258);
		// 能够刷新出几只
		int worms = (int) (lostTime / configTime);
		// 刷出的虫子+历史点击超过了上限
		if (smallCityGame.getTotal() + worms > maxWorms) {
			worms = maxTimes - smallCityGame.getTotal();
		}
		if (worms > 0) {
			for (int i = 0; i < worms; i++) {
				// 奖励是否满了
				if (smallCityGame.getTotal() >= maxTimes) {
					break;
				}
				// 随机刷一只虫
				int pos = cityGameService.randomWorm(smallCityGame.getWorms());
				if (pos < 0) {
					continue;
				}
				smallCityGame.getWorms().put(pos, 1);
				smallCityGame.setLastRefushTime(curentTimeSecond);
				smallCityGame.addTotal();
			}
		}
	}

	/**
	 * 初始化玩家头像
	 *
	 * @param player
	 */
	public void initProtrait(Player player) {
		HashMap<Integer, Integer> icons = player.getSimpleData().getIcons();
		staticIniDataMgr.getPortraitMap().values().forEach(staticPortrait -> {
			if (staticPortrait.getLock() == 0) {
				icons.put(staticPortrait.getPortraitId(), 1);
			}
		});
	}

	// 保存用户信息到账号服
	public void saveUcServerInfos(Player player) {
		try {
			if (player == null || player.account == null || player.getCountry() == 0 || player.roleId == 0) {
				return;
			}
			ConcurrentHashMap<Integer, PlayerExist> map = playerExistMap.computeIfAbsent(player.account.getServerId(), e -> new ConcurrentHashMap<>());
			PlayerExist playerExist = map.get(player.account.getAccountKey());
			if (playerExist != null) {
				playerExist.setCountry(player.getCountry());
				playerExist.setLevel(player.getLevel());
				playerExist.setNick(player.getNick());
				playerExist.setPortrait(player.getPortrait());
				playerExist.setLordId(player.getRoleId());
				uServerInfoDao.updatePlayerExist(playerExist);
			} else {
				PlayerExist p = null;
				List<PlayerExist> playerExists = uServerInfoDao.loadOne(player.getAccount().getAccountKey(), player.getAccount().getServerId());
				if (playerExists != null && playerExists.size() != 0) {
					p = playerExists.get(0);
				} else {
					p = new PlayerExist(player.account.getAccountKey(), player.account.getServerId(), player.getCountry());
					p.setLevel(player.getLevel());
					p.setNick(player.getNick());
					p.setPortrait(player.getPortrait());
					p.setLordId(player.roleId);
					p.setCreateDate(player.account.getCreateDate());
					uServerInfoDao.insertPlayerExist(p);
				}
				map.put(p.getAccountKey(), p);
				// 在redis中自增阵营数量
				// StringBuffer stringBuffer = new StringBuffer().append(serverId).append(RedisKey.COUNTRY_KEY).append(player.getCountry());
				// redisService.incr(stringBuffer.toString());re
			}
		} catch (Exception e) {
			com.game.util.LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public void offLine() {
		long current = System.currentTimeMillis();
		Iterator<OffLiner> it = offLinePlayer.values().iterator();
		while (it.hasNext()) {
			OffLiner offLiner = it.next();
			long removeOnlineTime = offLiner.getPlayer().getRemoveOnlineTime();
			if (removeOnlineTime != 0 && removeOnlineTime <= current) {
				Player player = offLiner.getPlayer();
				it.remove();// 移出offlineMap
				if (player.getChannelId() == -1 || offLiner.getChannelId() == player.getChannelId()) {// 是同一个ctx
					removeOnline(offLiner.getPlayer());// 移出onlineMap
					LogHelper.GAME_LOGGER.info("移除离线列表 playerId:{}", offLiner.getPlayerId());
				}
			}
		}
	}

	// 刷新无尽塔防挑战次数
	public void refEndlessTD(Player player) {
		EndlessTDInfo endlessTDInfo = player.getEndlessTDInfo();
		int remainingTimesRefresh = endlessTDInfo.getRefreshTime();
		int currentDay = GameServer.getInstance().currentDay;
		if (currentDay != remainingTimesRefresh) {
			endlessTDInfo.resetInfo(currentDay);
			SpringUtil.getBean(TDManager.class).refreshArmoryShop(player);
		}
	}

	public void addTDMoney(Player player, int count, int reason) {
		if (count < 0) {
			return;
		}
		player.getLord().setTdMoney(player.getTdMoney() + count);
	}

	public void subTDMoney(Player player, int count, int reason) {
		player.getLord().setTdMoney(player.getTdMoney() - count);
		if (player.getTdMoney() < 0) {
			player.getLord().setTdMoney(0);
		}
	}

	public boolean checkAndSubItem(Player player, List<List<Integer>> items, int reason) {
		for (List<Integer> item : items) {
			Integer type = item.get(0);
			Integer id = item.get(1);
			Integer count = item.get(2);
			if (count <= 0) {
				return true;
			}
			if (type == AwardType.PROP) {
				Item item1 = player.getItem(id);
				if (item1 == null || item1.getItemNum() < count) {
					return false;
				}
			} else if (type == AwardType.RESOURCE) {
				Resource resource = player.getResource();
				long resource1 = resource.getResource(id);
				if (resource1 < count) {
					return false;
				}
			} else if (type == AwardType.GOLD) {
				if (player.getGold() < count) {
					return false;
				}
			}
		}
		for (List<Integer> item : items) {
			subAward(player, item.get(0), item.get(1), item.get(2), reason);
		}
		return true;
	}

	public void updateNickPlayer(String oldNick) {
		Player remove = namePlayer.remove(oldNick);
		if (remove != null) {
			namePlayer.put(remove.getNick(), remove);
		}

	}

	// 刷新钓鱼中的记录分享次数,兑换次数,队列状态
	public void refFishing(Player player) {
		PlayerFishingData playerFishingData = player.getPlayerFishingData();
		int resetTime = playerFishingData.getResetTime();//刷新时间
		int currentDay = GameServer.getInstance().currentDay;//当前天数
		if (currentDay != resetTime) {
			playerFishingData.resetCountLimit(currentDay);
		}
	}

	public void setIdentity(Account account) {
		accountDao.setIdentity(account);
	}

	public void reAccount(long channelId, Account account) {
		this.accountSessionMap.put(channelId, account.getAccountKey());
		Map<Integer, Account> AccountConcurrentHashMap = this.accountCache.computeIfAbsent(account.getAccountKey(), x -> new ConcurrentHashMap<>());
		AccountConcurrentHashMap.put(account.getServerId(), account);
		this.accountKeyCache.put(account.getAccountKey(), account);
	}

	public Account getAccountByAccountKey(int key) {
		return accountKeyCache.get(key);
	}

	public Map<Integer, Map<Integer, Account>> getAccountCache() {
		return accountCache;
	}

	public void setAccountCache(Map<Integer, Map<Integer, Account>> accountCache) {
		this.accountCache = accountCache;
	}
}
