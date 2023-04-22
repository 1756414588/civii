package com.game.dataMgr;

import com.game.constant.MapId;
import com.game.define.LoadData;
import com.game.worldmap.Pos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.domain.s.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.BattleEntityType;
import com.game.constant.WorldAreaType;
import com.game.constant.WorldExpType;
import com.game.dao.s.StaticDataDao;
import com.game.domain.p.ConfigException;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.Range;
import com.google.common.collect.HashBasedTable;

@Component
@LoadData(name = "世界", initSeq = 2000)
public class StaticWorldMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	private Map<Integer, StaticWorldMap> worldMap = new HashMap<Integer, StaticWorldMap>();
	/*** 地图上所有的城市 ***/
	private Map<Integer, StaticWorldCity> cityMap = new HashMap<Integer, StaticWorldCity>();
	private Map<Integer, List<StaticWorldCity>> cityList = new HashMap<>();


	/*** 场景资源 ***/
	private Map<Integer, StaticWorldResource> worldRes = new HashMap<Integer, StaticWorldResource>();
	private HashBasedTable<Integer, Integer, Integer> worldResTable = HashBasedTable.create();

	/*** 场景野怪 ***/
	@Getter
	private Map<Integer, StaticWorldMonster> worldMonsterMap = new HashMap<Integer, StaticWorldMonster>();
	private Map<Integer, Integer> monsterIdMap = new HashMap<Integer, Integer>();
	/*** 模拟玩家 ***/
	private List<StaticWorldPlayer> wolrdPlayer = new ArrayList<StaticWorldPlayer>();

	/*** 初级资源配置 ***/
	private Map<Integer, StaticPrimaryResource> primaryResourceMap = new HashMap<Integer, StaticPrimaryResource>();
	/*** 神秘矿洞配置 ***/
	private Map<Integer, StaticMysteriousCave> mysteriousCaveMap = new HashMap<Integer, StaticMysteriousCave>();
	private List<StaticMysteriousCave> mysteriousCaves = new ArrayList<StaticMysteriousCave>();
	private Map<Integer, StaticMonsterFlush> monsterFlushMap = new HashMap<Integer, StaticMonsterFlush>();
	private Map<Integer, StaticMonsterNum> monsterNumMap = new HashMap<Integer, StaticMonsterNum>();
	/*** 野怪刷新规则辅助 ***/
	//private HashBasedTable<Integer, Integer, Integer> monsterFlushNum = HashBasedTable.create();
	//private HashBasedTable<Integer, Integer, Integer> monsterFlushRate= HashBasedTable.create();

	private HashBasedTable<Integer, Integer, Map<Integer, Integer>> monsterFlushNum = HashBasedTable.create();
	private HashBasedTable<Integer, Integer, Map<Integer, Integer>> monsterFlushRate = HashBasedTable.create();
	private HashBasedTable<Integer, Integer, Range> monsterFlushRange = HashBasedTable.create();
	private Map<Integer, StaticWorldResNum> resNumMap = new HashMap<Integer, StaticWorldResNum>();
	private Map<Integer, StaticWorldTarget> worldTargetMap = new HashMap<Integer, StaticWorldTarget>();
	private Map<Integer, StaticMapMove> mapMoveMap = new HashMap<Integer, StaticMapMove>();
	/*** 世界经验 ***/
	private Map<Integer, StaticWorldExp> worldExpMap = new HashMap<Integer, StaticWorldExp>();
	private Map<Integer, Map<Integer, StaticWorldExp>> worldExp = new HashMap<Integer, Map<Integer, StaticWorldExp>>();
	private Map<Integer, StaticPlayerKick> playerKickMap = new HashMap<Integer, StaticPlayerKick>();
	/*** 禁卫军 ***/
	private Map<Integer, StaticSquareMonster> squareMonsterMap = new HashMap<Integer, StaticSquareMonster>();
	private Map<Integer, StaticFortressMonster> fortressMonsterMap = new HashMap<Integer, StaticFortressMonster>();
	private Map<Integer, StaticFortressResource> fortressResourceMap = new HashMap<Integer, StaticFortressResource>();
	private Map<Integer, Integer> fortressMonsterConfig = new HashMap<Integer, Integer>();
	private HashBasedTable<Integer, Integer, Integer> fortressResourceConfig = HashBasedTable.create();
	private Map<Integer, StaticSeason> seasonMap = new HashMap<Integer, StaticSeason>();
	// 世界刷怪活动
	private Map<Integer, StaticActMonster> actMonsterMap = new HashMap<Integer, StaticActMonster>();
	// resource flush count, type, level, count
	private HashBasedTable<Integer, Integer, Integer> resFlushConfig = HashBasedTable.create();
	/*** 血战要塞操作消耗 ***/
	private Map<Integer, StaticPvpCost> pvpCostMap = new HashMap<Integer, StaticPvpCost>();
	/*** 血战要塞排名配置 ***/
	private Map<Integer, StaticPvpRank> pvpRankMap = new HashMap<Integer, StaticPvpRank>();
	private Map<Integer, StaticPvpTotalKill> pvpTotalKillMap = new HashMap<Integer, StaticPvpTotalKill>();
	private Map<Integer, StaticPvpDig> pvpDigMap = new HashMap<Integer, StaticPvpDig>();
	private Map<Integer, StaticPvpExchange> pvpExchangeMap = new HashMap<Integer, StaticPvpExchange>();
	private Map<Integer, StaticPvpExchange> newBroodWarShop = new HashMap<>();
	private Map<Integer, StaticPvpBattleLv> pvpBattleLvMap = new HashMap<>();
	private List<Integer> primaryMapId = new ArrayList<Integer>();
	private List<Integer> middleMapId = new ArrayList<Integer>();
	private List<Integer> centerMapId = new ArrayList<Integer>();
	private HashMap<Integer, Integer> rebelMapper = new HashMap<Integer, Integer>();
	@Getter
	@Setter
	private Map<Long, StaticGiantZerg> giantZergMap = new HashMap<>();
	@Getter
	@Setter
	private Map<Integer, StaticGiantZergBuff> giantZergBuffMap = new HashMap<>();


	// 神秘矿洞总概率
	private int totalNum = 0;

	@Override
	public void load() throws Exception {
		cityMap = staticDataDao.selectWorldCity();
		worldMap = staticDataDao.selectWorldMap();
		worldRes = staticDataDao.selectWorldResourceMap();
		worldMonsterMap = staticDataDao.selectWorldMonsterMap();
		wolrdPlayer = staticDataDao.selectWorldPlayer();
		primaryResourceMap = staticDataDao.selectPrimaryResource();
		mysteriousCaveMap = staticDataDao.selectMysteriousCave();
		mysteriousCaves = staticDataDao.selectMysteriousCaveList();
		totalNum = 0;
		for (StaticMysteriousCave staticMysteriousCave : mysteriousCaves) {
			setTotalNum(getTotalNum() + staticMysteriousCave.getRate());
		}

		setMonsterFlushMap(staticDataDao.selectMonsterFlush());
		setMonsterNumMap(staticDataDao.selectMonsterNum());

		setResNumMap(staticDataDao.selectWorldResNumMap());

		monsterIdMap.clear();
		makeMonsterIdMap();

		worldTargetMap = staticDataDao.selectStaticWorldTarget();
		//checkConfig();
		mapMoveMap = staticDataDao.selectMapMoveMap();
		worldResTable.clear();
		makeResMapper();
		worldExpMap = staticDataDao.selectWorldExpMap();
		worldExp.clear();
		makeWorldExp();
		setPlayerKickMap(staticDataDao.selectPlayerKickMap());
		squareMonsterMap = staticDataDao.selectSquareMonsterMap();
		fortressMonsterMap = staticDataDao.selectFortressMonsterMap();
		fortressResourceMap = staticDataDao.selectFortressResource();
		fortressMonsterConfig.clear();
		makeFortressConfig();

		seasonMap = staticDataDao.selectSeasonMap();
		actMonsterMap = staticDataDao.selectActMonster();
		resFlushConfig.clear();
		makeResourceFlush();
		pvpCostMap = staticDataDao.selectWorldPvpCost();
		pvpRankMap = staticDataDao.selectPvpRankMap();
		pvpTotalKillMap = staticDataDao.selectPvpTotalKillMap();
		pvpDigMap = staticDataDao.selectPvpDigMap();
		pvpExchangeMap = staticDataDao.selectPvpExchange();
		newBroodWarShop = staticDataDao.selectBroodWarShop();
//        pvpBattleLvMap = staticDataDao.selectBattleLvMap();
		primaryMapId.clear();
		middleMapId.clear();
		centerMapId.clear();
		makeMapId();
		rebelMapper.clear();
		makeRebelMapper();
		pvpBattleLvMap = staticDataDao.selectBattleLvMap();
		giantZergMap = staticDataDao.loadStaticGiantZerg();
		giantZergBuffMap = staticDataDao.loadStaticGiantZergBuff();
	}

	/**
	 * Overriding: init
	 *
	 * @see com.game.dataMgr.BaseDataMgr#init()
	 */
	@Override
	public void init() throws Exception {
		makeMonsterRule();
		makeFlushRange();
		checkMonster();
		checkCityMonster();
		cityList.clear();
		flushCityList();
	}

	public void makeMonsterIdMap() {
		for (StaticWorldMonster monster : worldMonsterMap.values()) {
			getMonsterIdMap().put(monster.getLevel(), monster.getId());
		}
	}

	public Map<Integer, StaticPvpBattleLv> getPvpBattleLvMap() {
		return pvpBattleLvMap;
	}

	public Map<Integer, Integer> getMonsterIdMap() {
		return monsterIdMap;
	}

	public void setMonsterIdMap(Map<Integer, Integer> monsterIdMap) {
		this.monsterIdMap = monsterIdMap;
	}

	// resource flush , type, level, count
	public void makeResourceFlush() {
		for (StaticWorldResNum resNum : resNumMap.values()) {
			if (resNum == null) {
				continue;
			}
			getResFlushConfig().put(resNum.getType(), resNum.getLevel(), (int) resNum.getCount());
		}
	}


	public Map<Integer, StaticWorldCity> getCityMap() {
		return cityMap;
	}

	public StaticWorldResource getMonstersByWildId(int wildId) {
		return worldRes.get(wildId);
	}

	/*** 模拟玩家 ***/
	public List<StaticWorldPlayer> getWolrdPlayer() {
		return wolrdPlayer;
	}

	public void setWolrdPlayer(List<StaticWorldPlayer> wolrdPlayer) {
		this.wolrdPlayer = wolrdPlayer;
	}

	/*** 大地图场景，每张大地图包含100张小地图 ***/
	public Map<Integer, StaticWorldMap> getWorldMap() {
		return worldMap;
	}

	// 找到当前map的配置
	public StaticWorldMap getStaticWorldMap(int mapId) {
		return worldMap.get(mapId);
	}

	public void setWorldMap(Map<Integer, StaticWorldMap> worldMap) {
		this.worldMap = worldMap;
	}

	public int getMapId(int posX, int posY) {
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			if (staticWorldMap == null) {
				continue;
			}

			if (posX >= staticWorldMap.getX1() &&
				posX <= staticWorldMap.getX2() &&
				posY >= staticWorldMap.getY1() &&
				posY <= staticWorldMap.getY2()) {
				return staticWorldMap.getMapId();
			}
		}

		return 0;
	}

	public StaticWorldMap getMap(int posX, int posY) {
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			if (posX >= staticWorldMap.getX1() &&
				posX <= staticWorldMap.getX2() &&
				posY >= staticWorldMap.getY1() &&
				posY <= staticWorldMap.getY2()) {
				return staticWorldMap;
			}
		}
		return null;
	}

	/*** 初级采集点配置 ***/
	public Map<Integer, StaticPrimaryResource> getPrimaryResourceMap() {
		return primaryResourceMap;
	}

	/*** 神秘矿洞配置 ***/
	public Map<Integer, StaticMysteriousCave> getMysteriousCaveMap() {
		return mysteriousCaveMap;
	}

	public List<StaticMysteriousCave> getMysteriousCaves() {
		return mysteriousCaves;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public StaticPrimaryResource getPrimaryResouce(int collectType) {
		return primaryResourceMap.get(collectType);
	}

	public StaticMysteriousCave getMySteriousCave(int randId) {
		return mysteriousCaveMap.get(randId);
	}


	// 生成怪物规则和概率
	public void makeMonsterRule() throws ConfigException {
		monsterFlushNum.clear();
		monsterFlushRate.clear();
		for (StaticMonsterNum monsterNum : getMonsterNumMap().values()) {
			if (monsterNum == null) {
				continue;
			}

			int mapType = monsterNum.getMapType();

			List<List<Integer>> monsters = monsterNum.getMonsters();
			if (monsters == null || monsters.size() <= 0) {
				LogHelper.CONFIG_LOGGER.error("makeMonsterRule monsters size is error");
				continue;
			}

			List<Integer> rate = monsters.get(0);
			if (rate == null || rate.size() <= 0) {
				LogHelper.CONFIG_LOGGER.error("makeMonsterRule rate size is error");
				continue;
			}

			int monsterLevel = rate.get(0);
			int monsterRate = rate.get(1);
			int maxNum = monsterNum.getMaxNum();
			int num = staticLimitMgr.getNum(251) == 0 ? 10 : staticLimitMgr.getNum(251);
			if (staticLimitMgr.getNum(251) == 0) {
				throw new ConfigException("config err: value 'num' can not be null at simple_config(251) !");
			}

			if (monsterRate * num > maxNum) {
				throw new ConfigException("StaticMonsterNum config err: The initial value cannot over the max value !");
			}

//            monsterFlushNum.put(mapType, monsterLevel, maxNum);
//            monsterFlushRate.put(mapType, monsterLevel, monsterRate * num);
			Map<Integer, Integer> integerIntegerMap = monsterFlushNum.get(monsterNum.getWorldTarget(), mapType);
			if (integerIntegerMap == null) {
				integerIntegerMap = new HashMap<>();
				monsterFlushNum.put(monsterNum.getWorldTarget(), mapType, integerIntegerMap);
			}
			integerIntegerMap.put(monsterLevel, maxNum);
			Map<Integer, Integer> integerIntegerMap1 = monsterFlushRate.get(monsterNum.getWorldTarget(), mapType);
			if (integerIntegerMap1 == null) {
				integerIntegerMap1 = new HashMap<>();
				monsterFlushRate.put(monsterNum.getWorldTarget(), mapType, integerIntegerMap1);
			}
			integerIntegerMap1.put(monsterLevel, monsterRate * num);
		}
	}

//    public HashBasedTable<Integer, Integer, Integer> getMonsterFlushNum() {
//        return monsterFlushNum;
//    }
//
//    public HashBasedTable<Integer, Integer, Integer> getMonsterFlushRate() {
//        return monsterFlushRate;
//    }


	public HashBasedTable<Integer, Integer, Map<Integer, Integer>> getMonsterFlushRate() {
		return monsterFlushRate;
	}

	public HashBasedTable<Integer, Integer, Map<Integer, Integer>> getMonsterFlushNum() {
		return monsterFlushNum;
	}


	public void makeFlushRange() {
		monsterFlushRange.clear();
		for (StaticMonsterFlush monsterFlush : getMonsterFlushMap().values()) {
			if (monsterFlush == null) {
				continue;
			}

			// 司令部等级
			int commandLv = monsterFlush.getCommandLv();
			List<Integer> lowRange = monsterFlush.getLowRange();
			List<Integer> middleRange = monsterFlush.getMiddleRange();
			List<Integer> highRange = monsterFlush.getLowRange();
			Range lrange = Range.create(lowRange);
			Range mrange = Range.create(middleRange);
			Range hrange = Range.create(highRange);
			monsterFlushRange.put(commandLv, WorldAreaType.LOW_AREA, lrange);
			monsterFlushRange.put(commandLv, WorldAreaType.MIDDLE_AREA, mrange);
			monsterFlushRange.put(commandLv, WorldAreaType.HIGH_AREA, hrange);
		}
	}

	public Range getRange(int commandLv, int areaType) {
		return monsterFlushRange.get(commandLv, areaType);
	}

	public StaticWorldMonster getMonster(int monsterId) {
		return worldMonsterMap.get(monsterId);
	}


	/*** 资源分布表 ***/
	public Map<Integer, StaticWorldResNum> getResNumMap() {
		return resNumMap;
	}

	public void setResNumMap(Map<Integer, StaticWorldResNum> resNumMap) {
		this.resNumMap = resNumMap;
	}

	public void checkMonster() throws ConfigException {
		LogHelper.CONFIG_LOGGER.info("checkMonster");
		for (StaticWorldMonster monster : worldMonsterMap.values()) {
			List<Integer> monsters = monster.getMonsterIds();
			if (monsters != null) {
				for (Integer monsterId : monsters) {
					StaticMonster pveMonster = staticMonsterMgr.getStaticMonster(monsterId);
					if (pveMonster == null) {
						LogHelper.CONFIG_LOGGER.info("check table s_world_monster id:{} monsters:{} ; table s_pve_monster monsterId:{} not exists", monster.getId(), monsters, monsterId);
						throw new ConfigException("check world monster, monsterId = " + monsterId + " not exists.");
					}
				}
			}
		}
	}

	public void checkCityMonster() throws ConfigException {
		for (StaticWorldCity worldCity : cityMap.values()) {
			List<Integer> monsters = worldCity.getMonsters();
			if (monsters != null) {
				for (Integer monsterId : monsters) {
					StaticMonster pveMonster = staticMonsterMgr.getStaticMonster(monsterId);
					if (pveMonster == null) {
						throw new ConfigException(
							"cityId = " + worldCity.getCityId() + ", check world monster, monsterId = " + monsterId + " not exists.");
					}
				}
			}

			List<Integer> premonsters = worldCity.getMonsters();
			if (premonsters != null) {
				for (Integer monsterId : premonsters) {
					StaticMonster pveMonster = staticMonsterMgr.getStaticMonster(monsterId);
					if (pveMonster == null) {
						throw new ConfigException(
							"cityId = " + worldCity.getCityId() + ", check world monster, monsterId = " + monsterId + " not exists.");
					}
				}
			}

		}
	}

	// 资源Id映射
	public void makeResMapper() {
		for (StaticWorldResource worldResource : worldRes.values()) {
			worldResTable.put(worldResource.getType(), worldResource.getLevel(), worldResource.getId());
		}
	}

	public HashBasedTable<Integer, Integer, Integer> getWorldResTable() {
		return worldResTable;
	}

	public void setWorldResTable(HashBasedTable<Integer, Integer, Integer> worldResTable) {
		this.worldResTable = worldResTable;
	}

	// 获取资源Id
	public Integer getWorldRes(int resType, int resLevel) {
		return worldResTable.get(resType, resLevel);
	}

	public StaticWorldResource getStaticWorldResource(int resouceId) {
		return worldRes.get(resouceId);
	}

	public StaticWorldCity getCity(int cityId) {
		return cityMap.get(cityId);
	}


	public int getCityMapId(int cityId) {
		StaticWorldCity staticWorldCity = cityMap.get(cityId);
		if (staticWorldCity != null) {
			return staticWorldCity.getMapId();
		}

		return 0;
	}


	public Map<Integer, StaticMonsterNum> getMonsterNumMap() {
		return monsterNumMap;
	}

	public void setMonsterNumMap(Map<Integer, StaticMonsterNum> monsterNumMap) {
		this.monsterNumMap = monsterNumMap;
	}

	/*** 野怪刷新规则 ***/
	public Map<Integer, StaticMonsterFlush> getMonsterFlushMap() {
		return monsterFlushMap;
	}

	public void setMonsterFlushMap(Map<Integer, StaticMonsterFlush> monsterFlushMap) {
		this.monsterFlushMap = monsterFlushMap;
	}

	/*** 世界目标 ***/
	public Map<Integer, StaticWorldTarget> getWorldTargetMap() {
		return worldTargetMap;
	}

	public void setWorldTargetMap(Map<Integer, StaticWorldTarget> worldTargetMap) {
		this.worldTargetMap = worldTargetMap;
	}

	public void checkConfig() throws ConfigException {
		for (StaticWorldTarget staticWorldTarget : worldTargetMap.values()) {
			List<List<Integer>> award = staticWorldTarget.getAward();
			if (award == null || award.size() <= 0) {
				throw new ConfigException("award == null || award.size() <= 0 ");

			}

			for (List<Integer> elem : award) {
				if (elem == null || elem.size() != 3) {
					throw new ConfigException("elem == null || award.size() != 3");
				}

			}

			if (staticWorldTarget.getType() != 3) {
				continue;
			}

			int monsterId = staticWorldTarget.getBossId();
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
			if (staticMonster == null) {
				throw new ConfigException("no world monster, monster id = " + monsterId);
			}
		}
	}

	public StaticWorldTarget getStaticWorldTarget(int targetId) {
		return worldTargetMap.get(targetId);
	}

	public StaticMapMove getStaticMapMove(int mapType) {
		return mapMoveMap.get(mapType);
	}

	public void makeWorldExp() {
		for (StaticWorldExp elem : worldExpMap.values()) {
			if (elem == null) {
				continue;
			}

			Map<Integer, StaticWorldExp> worldType = worldExp.get(elem.getEntityLv());
			if (worldType == null) {
				worldType = new HashMap<Integer, StaticWorldExp>();
				worldType.put(elem.getEntityType(), elem);
				worldExp.put(elem.getEntityLv(), worldType);
			} else {
				worldType.put(elem.getEntityLv(), elem);
			}

		}

		worldExpMap.clear();
		worldExpMap = null;
	}

	public int getLostExp(int level, int type) {
		Map<Integer, StaticWorldExp> worldType = worldExp.get(level);
		if (worldType == null) {
			LogHelper.CONFIG_LOGGER.error("worldType is null! level:{}", level);
			return 0;
		}

		StaticWorldExp curElem = worldType.get(type);
		if (curElem == null) {
			LogHelper.CONFIG_LOGGER.error("StaticWorldExp config error, level:{} type:{}", level, type);
			return 0;
		}

		return curElem.getLostExp();
	}

	private int getKillExp(int level, int type) {
		Map<Integer, StaticWorldExp> worldType = worldExp.get(level);
		if (worldType == null) {
			LogHelper.CONFIG_LOGGER.error("worldType is null! level:{}", level);
			return 0;
		}

		StaticWorldExp curElem = worldType.get(type);
		if (curElem == null) {
			LogHelper.CONFIG_LOGGER.error("StaticWorldExp config error, level:{} type:{}", level, type);
			return 0;
		}

		return curElem.getKillExp();
	}


	public int getKillExpByType(int level, int type) {
		int expType = getExpType(type);
		return getKillExp(level, expType);
	}

	// 通过id,type找到对应的quality
	public int getExpType(int type) {
		if (isHeroType(type)) {
			return WorldExpType.HeroExp;
		} else if (isRebel(type)) {
			return WorldExpType.RebelExp;
		} else if (isCityNpc(type)) {
			return WorldExpType.CityNpc;
		} else if (isWallMonster(type)) {
			return WorldExpType.WallExp;
		}

		return 0;
	}

	public boolean isHeroType(int type) {
		return type == BattleEntityType.HERO ||
			type == BattleEntityType.FRIEND_HERO ||
			type == BattleEntityType.WALL_FRIEND_HERO;
	}

	public boolean isRebel(int type) {
		return type == BattleEntityType.REBEL;
	}

	public boolean isWallMonster(int type) {
		return type == BattleEntityType.WALL_DEFENCER;
	}

	public boolean isCityNpc(int type) {
		return type == BattleEntityType.CITY_MONSTER;
	}


	public Map<Integer, Map<Integer, StaticWorldExp>> getWorldExp() {
		return worldExp;
	}

	public void setWorldExp(Map<Integer, Map<Integer, StaticWorldExp>> worldExp) {
		this.worldExp = worldExp;
	}

	public Map<Integer, StaticPlayerKick> getPlayerKickMap() {
		return playerKickMap;
	}

	public void setPlayerKickMap(Map<Integer, StaticPlayerKick> playerKickMap) {
		this.playerKickMap = playerKickMap;
	}

	public StaticPlayerKick getPlayerKick(int mapType) {
		return playerKickMap.get(mapType);
	}

	public StaticSquareMonster getSquareMonster(int cityLv) {
		return squareMonsterMap.get(cityLv);
	}

	/***  世界要塞怪和资源 ***/
	public Map<Integer, StaticFortressMonster> getFortressMonsterMap() {
		return fortressMonsterMap;
	}

	public void setFortressMonsterMap(Map<Integer, StaticFortressMonster> fortressMonsterMap) {
		this.fortressMonsterMap = fortressMonsterMap;
	}

	public Map<Integer, StaticFortressResource> getFortressResourceMap() {
		return fortressResourceMap;
	}

	public void setFortressResourceMap(Map<Integer, StaticFortressResource> fortressResourceMap) {
		this.fortressResourceMap = fortressResourceMap;
	}

	public void makeFortressConfig() {
		for (StaticFortressMonster monster : fortressMonsterMap.values()) {
			if (monster == null) {
				LogHelper.CONFIG_LOGGER.error("monster fortress is null");
				continue;
			}
			getFortressMonsterConfig().put(monster.getMonsterLv(), monster.getMaxNum());
		}

		for (StaticFortressResource fortressResource : fortressResourceMap.values()) {
			if (fortressResource == null) {
				continue;
			}

			getFortressResourceConfig().put(fortressResource.getType(), fortressResource.getLevel(), fortressResource.getCount());
		}

	}

	public Map<Integer, Integer> getFortressMonsterConfig() {
		return fortressMonsterConfig;
	}

	public void setFortressMonsterConfig(Map<Integer, Integer> fortressMonsterConfig) {
		this.fortressMonsterConfig = fortressMonsterConfig;
	}

	public HashBasedTable<Integer, Integer, Integer> getFortressResourceConfig() {
		return fortressResourceConfig;
	}

	public void setFortressResourceConfig(HashBasedTable<Integer, Integer, Integer> fortressResourceConfig) {
		this.fortressResourceConfig = fortressResourceConfig;
	}

	public boolean isFortressResExists(int resType, int lv) {
		Integer resData = fortressResourceConfig.get(resType, lv);
		if (resData == null) {
			return false;
		}
		return true;
	}

	public long getSeasonPeriod(int effectId) {
		StaticSeason staticSeason = seasonMap.get(effectId);
		if (staticSeason == null) {
			LogHelper.CONFIG_LOGGER.error("effectId = " + effectId + " config error!");
			return TimeHelper.DAY_MS;
		}

		return staticSeason.getPeriod() * TimeHelper.SECOND_MS;
	}

	public StaticSeason getSeasonByWeek(int week) {
		StaticSeason staticSeason = null;
		int key = 0;
		boolean flag = false;
		for (StaticSeason value : seasonMap.values()) {
			if (value.getWeekDay() == week) {
				if (!flag) {
					key = value.getKeyId();
					flag = true;
				}
				if (value.getKeyId() <= key) {
					staticSeason = value;
					key = staticSeason.getKeyId();
				}
			}
		}
		return staticSeason;
	}


	public StaticSeason getSeason(int effectId) {
		StaticSeason staticSeason = seasonMap.get(effectId);
		return staticSeason;
	}

	public StaticActMonster getActMonster(int keyId) {
		return actMonsterMap.get(keyId);
	}

	public HashBasedTable<Integer, Integer, Integer> getResFlushConfig() {
		return resFlushConfig;
	}

	public void setResFlushConfig(HashBasedTable<Integer, Integer, Integer> resFlushConfig) {
		this.resFlushConfig = resFlushConfig;
	}

	public StaticPvpCost getStaticPvpCost(int actionId) {
		return pvpCostMap.get(actionId);
	}

	public int getPvpScore(int rank) {
		int totalRank = pvpRankMap.size();
		if (rank >= totalRank) {
			StaticPvpRank config = pvpRankMap.get(totalRank);
			if (config == null) {
				LogHelper.CONFIG_LOGGER.error("config error, totalRank = " + totalRank);
				return 0;
			}
			return config.getScore();
		}

		StaticPvpRank config = pvpRankMap.get(rank);
		if (config == null) {
			LogHelper.CONFIG_LOGGER.error("config error, totalRank = " + rank);
			return 0;
		}

		return config.getScore();
	}

	public int getBuyCost(int times) {
		StaticPvpDig staticPvpDig = pvpDigMap.get(times);
		if (staticPvpDig == null) {
			return 0;
		}

		return staticPvpDig.getBuyCost();
	}

	public int getMaxDigTimes() {
		return pvpDigMap.size() - 1;
	}


	public StaticPvpDig getPvpDig(int digIndex) {
		StaticPvpDig staticPvpDig = pvpDigMap.get(digIndex + 1);
		return staticPvpDig;
	}

	public StaticPvpExchange getPvpExchange(int propId) {
		return pvpExchangeMap.get(propId);
	}

	public StaticPvpExchange getNewBroodWarShop(int propId) {
		return newBroodWarShop.get(propId);
	}

	public int getTotalScore(int killNum) {
		int maxSize = pvpTotalKillMap.size();
		StaticPvpTotalKill minConfig = pvpTotalKillMap.get(1);
		StaticPvpTotalKill maxConfig = pvpTotalKillMap.get(maxSize);
		if (killNum < minConfig.getKillNum()) {
			return 0;
		}

		if (killNum >= maxConfig.getKillNum()) {
			return maxConfig.getScore();
		}

		for (int i = 1; i < maxSize; i++) {
			StaticPvpTotalKill current = pvpTotalKillMap.get(i);
			StaticPvpTotalKill next = pvpTotalKillMap.get(i + 1);
			if (current == null || next == null) {
				continue;
			}

			if (killNum >= current.getKillNum() && killNum < next.getKillNum()) {
				return current.getScore();
			}
		}

		return 0;
	}


	public List<Integer> getPrimaryMapId() {
		return primaryMapId;
	}

	public void setPrimaryMapId(List<Integer> primaryMapId) {
		this.primaryMapId = primaryMapId;
	}

	public List<Integer> getMiddleMapId() {
		return middleMapId;
	}

	public void setMiddleMapId(List<Integer> middleMapId) {
		this.middleMapId = middleMapId;
	}

	public List<Integer> getCenterMapId() {
		return centerMapId;
	}

	public void setCenterMapId(List<Integer> centerMapId) {
		this.centerMapId = centerMapId;
	}


	public void makeMapId() {
		for (StaticWorldMap elem : worldMap.values()) {
			if (elem == null) {
				continue;
			}

			if (elem.getAreaType() == 1) {
				primaryMapId.add(elem.getMapId());
			} else if (elem.getAreaType() == 2) {
				middleMapId.add(elem.getMapId());
			} else if (elem.getAreaType() == 3) {
				centerMapId.add(elem.getMapId());
			}
		}
	}

	public void makeRebelMapper() {
		for (StaticWorldMonster worldMonster : worldMonsterMap.values()) {
			if (worldMonster == null) {
				continue;
			}
			List<Integer> monsterIds = worldMonster.getMonsterIds();
			if (monsterIds == null) {
				continue;
			}

			for (Integer monsterId : monsterIds) {
				rebelMapper.put(monsterId, worldMonster.getId());
			}
		}
	}

	public Integer getWorldMonsterId(int entityId) {
		return rebelMapper.get(entityId);
	}

	public void flushCityList() {
		cityMap.values().forEach(x -> {
			List<StaticWorldCity> staticWorldCities = cityList.computeIfAbsent(x.getMapId(), a -> new ArrayList<>());
			staticWorldCities.add(x);
		});
	}

	public List<StaticWorldCity> getStaticWorldCityByMapId(int mapId) {
		return cityList.get(mapId);
	}


	public int getCityIdByPos(Pos pos) {
		List<StaticWorldCity> staticWorldCityByMapId = getStaticWorldCityByMapId(MapId.CENTER_MAP_ID);
		for (StaticWorldCity staticWorldCity : staticWorldCityByMapId) {
			if (pos.getX() >= staticWorldCity.getRangex1() && pos.getX() <= staticWorldCity.getRangex2() && pos.getY() >= staticWorldCity.getRangey1() && pos.getY() <= staticWorldCity.getRangey2()) {
				return staticWorldCity.getCityId();
			}
		}
		return 0;
	}

}
