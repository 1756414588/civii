package com.game.manager;

import com.game.Loading;
import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.ActivityConst;
import com.game.constant.AwardType;
import com.game.constant.ChatId;
import com.game.constant.CityId;
import com.game.constant.CityState;
import com.game.constant.CityType;
import com.game.constant.MailId;
import com.game.constant.MapId;
import com.game.constant.WarType;
import com.game.dao.p.CityDao;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticSuperResMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.define.LoadData;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.City;
import com.game.domain.p.CityElection;
import com.game.domain.p.CityMonster;
import com.game.domain.p.CityMonsterInfo;
import com.game.domain.p.ElectionCompare;
import com.game.domain.p.SquareMonster;
import com.game.domain.s.StaticMonster;
import com.game.domain.s.StaticSquareMonster;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticWorldMap;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerCityMonster;
import com.game.pb.SerializePb.SerElectionData;
import com.game.pb.WorldPb;
import com.game.service.CityService;
import com.game.service.SuperResService;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "据点管理", type = Loading.LOAD_USER_DB, initSeq = 1700)
public class CityManager extends BaseManager {

	@Autowired
	private CityDao cityDao;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	@Autowired
	private LootManager lootManager;

	@Autowired
	private BattleMgr battleMgr;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private WarManager warManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private CityService cityService;
	@Autowired
	ActivityEventManager activityEventManager;

	private ConcurrentHashMap<Integer, City> cityMap = new ConcurrentHashMap<Integer, City>();

	private ConcurrentHashMap<Integer, Map<Long, CityElection>> cityElectionMap = new ConcurrentHashMap<Integer, Map<Long, CityElection>>();
	private ConcurrentHashMap<Integer, CityMonster> cityMonsterMap = new ConcurrentHashMap<Integer, CityMonster>();
	private List<Integer> squareFortress = new ArrayList<Integer>();
	private List<Integer> famousCity = new ArrayList<Integer>();
	private List<Integer> allCenterCity = new ArrayList<Integer>();

	private int worldFortress = 0;

	private boolean isSquareLevelFull = true;
	private ConcurrentHashMap<Integer, HashSet<Long>> warAttenders = new ConcurrentHashMap<Integer, HashSet<Long>>(); // 里面的hashset最好用同步的
	private List<City> cityList = null;

	@Override
	public void load() throws Exception {
		cityList = cityDao.selectCityList();
	}

	@Override
	public void init() throws Exception {
		initCity();
	}

	public City insertCity(int cityId) {
		City city = new City();
		city.setCityId(cityId);
		city.setCountry(0);
		city.setLordId(0);
		city.setEndTime(System.currentTimeMillis());
		cityDao.insertCity(city);
		return city;
	}

	public void initCity() {
		Map<Integer, StaticWorldCity> citys = staticWorldMgr.getCityMap();
		for (StaticWorldCity city : citys.values()) {
			if (city == null) {
				LogHelper.CONFIG_LOGGER.info("city is null!!!!");
				continue;
			}
			if (city.getType() == CityType.SQUARE_FORTRESS) {
				this.squareFortress.add(city.getCityId());
			} else if (city.getType() == CityType.FAMOUS_CITY) {
				this.famousCity.add(city.getCityId());
			} else if (city.getType() == CityType.WORLD_FORTRESS) {
				this.worldFortress = city.getCityId();
			}
			if (cityList == null || cityList.isEmpty()) {
				City city1 = insertCity(city.getCityId());
				if (city1 != null) {
					city1.setCityType(city.getType());
					handleCityMonster(city1);
					StaticWorldCity staticWorldCity = staticWorldMgr.getCity(city.getCityId());
					if (staticWorldCity != null && city1.getCityLv() == 0 && !squareFortress.contains(city.getCityId())) {
						city1.setCityLv(staticWorldCity.getLevel());
					} else {
						city1.setCityLv(city1.getCityLv());
					}

					city1.setPos(new Pos(staticWorldCity.getX(), staticWorldCity.getY()));
					city1.setMapId(staticWorldCity.getMapId());
					cityMap.put(city.getCityId(), city1);
				}
			}
		}
		allCenterCity.addAll(famousCity);
		allCenterCity.addAll(squareFortress);
		long now = System.currentTimeMillis();
		if (cityList != null && !cityList.isEmpty()) {
			for (City city : cityList) {
				if (city == null) {
					continue;
				}
				cityMap.put(city.getCityId(), city);
				StaticWorldCity staticWorldCity = staticWorldMgr.getCity(city.getCityId());
				if (staticWorldCity == null) {
					LogHelper.CONFIG_LOGGER.info("static world city is null!");
					continue;
				}
				if (city.getLastSaveTime() == 0) {
					city.setLastSaveTime(now);
				}
				if (city.getCityLv() == 0 && !squareFortress.contains(city.getCityId())) {
					city.setCityLv(staticWorldCity.getLevel());
				} else {
					city.setCityLv(city.getCityLv());
				}
				city.setExp(city.getExp());
				if (city.getCountry() == 0) {
					city.setEndTime(0);
				}
				byte[] electionData = city.getElectionData();
				if (electionData != null) {
					dserElection(city.getCityId(), electionData);
				}
				byte[] warAttendData = city.getWarAttender();
				if (warAttendData != null) {
					dserWarAttender(city.getCityId(), warAttendData);
				}
				city.setCityType(staticWorldCity.getType());
				handleCityMonster(city);

				if (squareFortress.contains(city.getCityId()) && city.getFlush() == 1) {
					flush(city);
				}
				city.setPos(new Pos(staticWorldCity.getX(), staticWorldCity.getY()));
				city.setMapId(staticWorldCity.getMapId());
			}
		}
	}

	@Autowired
	SuperResService superResService;
	@Autowired
	StaticSuperResMgr staticSuperResMgr;

	public void flush(City city) {
		city.setFlush(2);
		city.setCityLv(0);
		city.setExp(0);
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		mapInfo.getSuperResMap().clear();
		mapInfo.getSuperPosResMap().clear();
	}

	/**
	 * 处理活动相关
	 *
	 * @param cityId
	 * @param list
	 */
	public void handlerActHeroTask(int cityId, List<Player> list) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.info("city config is null, cityId = " + cityId);
			return;
		}
		if (staticWorldCity.getType() != CityType.POINT) {
			return;
		}
		list.forEach(player -> {
			activityEventManager.activityTip(EventEnum.CAPTURE_CITY, player, staticWorldCity.getType(), 1);
		});
	}


	public ConcurrentHashMap<Integer, City> getCityMap() {
		return cityMap;
	}

	public void setCityMap(ConcurrentHashMap<Integer, City> cityMap) {
		this.cityMap = cityMap;
	}

	public void update(City city) {
		int cityId = city.getCityId();
		byte[] electionData = serElection(cityId);
		if (electionData != null) {
			city.setElectionData(electionData);
		}

		byte[] monsterInfo = serCityMonster(cityId);
		if (monsterInfo != null) {
			city.setMonsterData(monsterInfo);
		}
		byte[] warAttender = serWarAttender(cityId);
		if (warAttender != null) {
			city.setWarAttender(warAttender);
		}

		cityDao.updateCity(city);
	}


	public void handlerCityBreak(int cityId, WarInfo warInfo) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.info("city config is null, cityId = " + cityId);
			return;
		}

		City city = cityMap.get(cityId);
		if (city == null) {
			LogHelper.CONFIG_LOGGER.info("city is not exists!, cityId = " + cityId);
			return;
		}

		clearCityLordId(city);
		if (city.getIsDestroyed() == 0) {
			city.setIsDestroyed(1);
			handleFamousCity(city, warInfo);
		}
		long protectedTime = staticLimitMgr.getNum(42) * TimeHelper.SECOND_MS;
		long now = System.currentTimeMillis();
		//陈树华要注释的
		//city.setProtectedTime(now + protectedTime);
		if (city.getState() == CityState.COMMON_MAKE_ITEM) {
			city.setAwardNum((byte) 0); // 图纸清零
		}
		int attackCountry = warInfo.getAttackerCountry();
		if (staticWorldCity.getType() == CityType.FAMOUS_CITY) {
			if (attackCountry != 0 && !hasFullFamous(attackCountry) && city.getState() == CityState.COMMON_MAKE_ITEM) {
				city.setCountry(attackCountry);
			} else if (attackCountry != 0 && city.getState() != CityState.COMMON_MAKE_ITEM) {
				city.setCountry(attackCountry);
			} else if (attackCountry == 0) { // 群雄攻城不受限制
				city.setCountry(attackCountry);
			} else {
				city.setCountry(0);        //更改主城给虫族
				city.setLordId(0);
				CityMonster cityMonster = getCityMonster(cityId);
				cityService.recoverCityMonster(cityMonster, now);
			}
		} else if (staticWorldCity.getType() == CityType.SQUARE_FORTRESS) {
			boolean hasCity = hasSquareFortress(attackCountry);
			if (!hasCity) {
				city.setCountry(attackCountry);
				city.setFlush(2);
			}
		} else {
			city.setCountry(attackCountry);
		}

		//设置攻破占领时间
		city.setBreakTime(System.currentTimeMillis());
		city.setCityTime(System.currentTimeMillis() + TimeHelper.SECOND_MS * 5 * 60);
		// 找到city的配置
		long electionPeriod = staticLimitMgr.getNum(43) * TimeHelper.SECOND_MS;
		city.setElectionEndTime(now + electionPeriod);

		if (city.getState() == CityState.COMMON_MAKE_ITEM) {
			long period = getMakePeriod(cityId);
			city.setMakeItemTime(System.currentTimeMillis() + period);
		}
		//city.setMakeItemTime(System.currentTimeMillis() + staticWorldCity.getPeriod() * TimeHelper.SECOND_MS);

		worldManager.updateCityNum(attackCountry, staticWorldCity.getMapId(), staticWorldCity.getCityId());


	}

	public void handleFamousCity(City city, WarInfo warInfo) {
		StaticWorldCity worldCity = staticWorldMgr.getCity(city.getCityId());
		if (worldCity == null) {
			LogHelper.CONFIG_LOGGER.info("worldCity is null!");
			return;
		}

		if (worldCity.getType() != CityType.FAMOUS_CITY) {
			return;
		}

		// 只有在第一次攻击的时候领取金色图纸
		Award award = getCityAward(city.getCityId());
		if (award == null || !award.isOk()) {
			LogHelper.CONFIG_LOGGER.info("award is null, award is ok!");
			return;
		}
		ArrayList<Award> awards = new ArrayList<Award>();
		awards.add(award);

		// 给每一个人发一封附件
		// 先把人筛选出来
		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
		if (attacker.isEmpty()) {
			LogHelper.CONFIG_LOGGER.info("attacker is empty!");
			return;
		}

		HashSet<Long> allPlayers = new HashSet<Long>();
		for (March march : attacker) {
			allPlayers.add(march.getLordId());
		}

		if (allPlayers.isEmpty()) {
			LogHelper.CONFIG_LOGGER.info("allPlayers is empty!");
			return;
		}

		for (Long lordId : allPlayers) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			playerManager.sendAttachMail(player, awards, MailId.ATTACK_FAMOUS_CITY, String.valueOf(city.getCityId()));
		}

	}

	public City getCity(int cityId) {
		if (cityMap.containsKey(cityId)) {
			return cityMap.get(cityId);
		}
		return null;
	}

	public int getCityCoutry(int cityId) {
		City city = getCity(cityId);
		if (city == null) {
			return 0;
		}

		return city.getCountry();
	}

	public long getCityLordId(int cityId) {
		City city = getCity(cityId);
		if (city == null) {
			return 0;
		}

		return city.getLordId();
	}

	public CityMonster getCityMonster(int cityId) {
		return cityMonsterMap.get(cityId);
	}

	public byte[] serElection(int cityId) {
		SerElectionData.Builder builder = SerElectionData.newBuilder();

		Map<Long, CityElection> cityElections = cityElectionMap.get(cityId);
		if (cityElections == null) {
			return builder.build().toByteArray();
		}
		for (CityElection info : cityElections.values()) {
			if (info == null) {
				continue;
			}
			DataPb.CityElectionData.Builder data = info.writeData();
			builder.addData(data);
		}
		return builder.build().toByteArray();
	}

	public void dserElection(int cityId, byte[] electionData) {
		try {
			SerElectionData cityElectionData = SerElectionData.parseFrom(electionData);
			for (DataPb.CityElectionData data : cityElectionData.getDataList()) {
				if (data == null) {
					continue;
				}

				CityElection cityElection = new CityElection();
				cityElection.readData(data);
				addCityElection(cityElection, cityId);
			}

		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}

	}

	public void dserWarAttender(int cityId, byte[] attenderData) {
		if (attenderData == null || attenderData.length <= 0) {
			return;
		}
		DataPb.CityWarAttender builder = null;
		try {
			builder = DataPb.CityWarAttender.parseFrom(attenderData);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		if (builder == null) {
			return;
		}

		ConcurrentHashMap<Integer, HashSet<Long>> warAttenders = getWarAttenders();
		HashSet<Long> attenders = warAttenders.get(cityId);

		// 兼容多个信息
		if (attenders == null) {
			attenders = new HashSet<Long>();
			warAttenders.put(cityId, attenders);
		}

		for (Long elem : builder.getLordIdsList()) {
			if (elem == null) {
				LogHelper.CONFIG_LOGGER.error("elem is null!");
				continue;
			}
			attenders.add(elem);
		}
	}

	public Map<Long, CityElection> getCityElection(int cityId) {
		return cityElectionMap.get(cityId);
	}

	public CityElection createCityElection(long lordId, List<Award> awards, int cityId) {
		CityElection cityElection = new CityElection();
		cityElection.setLordId(lordId);
		cityElection.setAwards(awards);
		cityElection.setElectionTime(System.currentTimeMillis());
		cityElection.setCityId(cityId);
		return cityElection;
	}

	public void addCityElection(CityElection cityElection, int cityId) {
		if (cityElection == null) {
			LogHelper.CONFIG_LOGGER.error("cityElection is null!");
			return;
		}

		Map<Long, CityElection> info = cityElectionMap.get(cityId);
		if (info == null) {
			info = new HashMap<Long, CityElection>();
			cityElectionMap.put(cityId, info);
		}

		info.put(cityElection.getLordId(), cityElection);
	}

	public void removeElection(long lordId, int cityId) {
		Map<Long, CityElection> info = cityElectionMap.get(cityId);
		if (info == null) {
			return;
		}

		if (!info.containsKey(lordId)) {
			return;
		}

		info.remove(lordId);
	}

	public ConcurrentHashMap<Integer, Map<Long, CityElection>> getCityElectionMap() {
		return cityElectionMap;
	}

	public void setCityElectionMap(ConcurrentHashMap<Integer, Map<Long, CityElection>> cityElectionMap) {
		this.cityElectionMap = cityElectionMap;
	}

	public boolean isCityElectionExists(int cityId, long lordId) {
		Map<Long, CityElection> info = cityElectionMap.get(cityId);
		if (info != null) {
			CityElection cityElection = info.get(lordId);
			if (cityElection != null) {
				return true;
			}
		}

		return false;
	}

	public ElectionCompare createElectionCompare(CityElection cityElection) {
		if (cityElection == null) {
			LogHelper.CONFIG_LOGGER.error("cityElection is null.");
			return null;
		}
		long lordId = cityElection.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return null;
		}
		ElectionCompare electionCompare = new ElectionCompare();
		electionCompare.setLordId(lordId);
		electionCompare.setTime(cityElection.getElectionTime());
		electionCompare.setTitle(player.getTitle());
		electionCompare.setCityId(cityElection.getCityId());
		electionCompare.setAwards(cityElection.getAwards());

		return electionCompare;

	}

	public ConcurrentHashMap<Integer, CityMonster> getCityMonsterMap() {
		return cityMonsterMap;
	}

	public void setCityMonsterMap(ConcurrentHashMap<Integer, CityMonster> cityMonsterMap) {
		this.cityMonsterMap = cityMonsterMap;
	}

	public boolean isSpecialCity(City city) {
		StaticWorldCity worldCity = staticWorldMgr.getCity(city.getCityId());
		if (worldCity == null) {
			return false;
		}

		return worldCity.getType() == CityType.FAMOUS_CITY || worldCity.getType() == CityType.SQUARE_FORTRESS;
	}

	public void handleCityMonster(City city) {
		byte[] monsterData = city.getMonsterData();
		// 说明没有,则需要读取配置
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(city.getCityId());
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity is null, cityId = " + city.getCityId());
			return;
		}

		java.util.List<Integer> monsters = staticWorldCity.getMonsters();
		if (monsters == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity monsters is null, cityId = " + city.getCityId());
			return;
		}

		if (monsters.isEmpty() && city.getCityId() != 25) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity monsters is monsters.isEmpty(), cityId = " + city.getCityId());
			return;
		}

		if (staticWorldCity.getType() == CityType.POINT) {
			if (city.getCityLv() <= 1) {
				monsters = staticWorldCity.getPreMonsters();
			} else {
				monsters = staticWorldCity.getMonsters();
			}
		}

		if (monsterData != null) {
			SerCityMonster builder = null;
			try {
				builder = SerCityMonster.parseFrom(monsterData);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

			CityMonster cityMonster = new CityMonster();
			cityMonster.readData(builder.getData());
			cityMonsterMap.put(cityMonster.getCityId(), cityMonster);
			//这里处理 如果城池被打下来了 并且没有城主 则不恢复城池兵力
			if (city.getLordId() == 0 && city.getCountry() != 0) {
				Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
				if (monsterInfoMap != null) {
					for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
						cityMonsterInfo.setSoldier(0);
					}
				}
				return;
			}

			// 检查血量, 以及有没有新的Id加进来, 有没有旧的Id删除
			// 1.先检查有没有被打过，被打过且血量大于配置的，直接变成配置血量
			// 2.被打过且血量小于于配置的，让其慢慢恢复
			// 3.没有被打过的，当前血量大于配置，变成配置血量；当前血量小于配置，变成配置血量
			// 4.如果有新的Id加入，则新增新的怪
			// 5.如果有旧的Id删除，则删除老的怪
			for (Integer monsterId : monsters) {
				StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
				if (staticMonster == null) {
					continue;
				}

				int configId = staticMonster.getMonsterId();
				if (staticMonster.getSoldierCount() <= 0) {
					LogHelper.CONFIG_LOGGER.error("config monster soldier is 0, monsterId = " + staticMonster.getMonsterId());
					continue;
				}

				CityMonsterInfo cityMonsterInfo = cityMonster.getCityMonster(staticMonster.getMonsterId());
				if (cityMonsterInfo == null) { // 4.如果有新的Id加入，则新增新的怪
					cityMonsterInfo = new CityMonsterInfo();
					cityMonsterInfo.setMonsterId(configId);
					cityMonsterInfo.setMaxSoldier(staticMonster.getSoldierCount());
					cityMonsterInfo.setSoldier(staticMonster.getSoldierCount());
					cityMonster.addMonster(cityMonsterInfo);
					continue;
				}

				// 2.被打过且血量小于于配置的，让其慢慢恢复
				if (cityMonsterInfo.getSoldier() < cityMonsterInfo.getMaxSoldier()) {
					cityMonsterInfo.setMaxSoldier(staticMonster.getSoldierCount());
					if (cityMonsterInfo.getSoldier() > staticMonster.getSoldierCount()) { // 1.先检查有没有被打过，被打过且血量大于配置的，直接变成配置血量
						cityMonsterInfo.setSoldier(staticMonster.getSoldierCount());
					}

				} else { // 3.没有被打过的，当前血量大于配置，变成配置血量；当前血量小于配置，变成配置血量
					cityMonsterInfo.setSoldier(staticMonster.getSoldierCount());
					cityMonsterInfo.setMaxSoldier(staticMonster.getSoldierCount());
				}

				// 如果是名城，且当前hp<maxhp
				if (isSpecialCity(city) && cityMonsterInfo.getSoldier() < cityMonsterInfo.getMaxSoldier()) {
					cityMonsterInfo.setSoldier(cityMonsterInfo.getMaxSoldier());
				}
			}

			// 5.如果有旧的Id删除，则删除老的怪
			Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
			Iterator<CityMonsterInfo> iterator = monsterInfoMap.values().iterator();
			while (iterator.hasNext()) {
				CityMonsterInfo info = iterator.next();
				if (info == null) {
					continue;
				}

				boolean found = false;
				for (Integer monsterId : monsters) {
					if (monsterId == info.getMonsterId()) {
						found = true;
						break;
					}
				}

				if (!found) {
					iterator.remove();
				}
			}

		} else {
			CityMonster monsterInfo = new CityMonster();
			monsterInfo.setCityId(city.getCityId());
			for (Integer monsterId : monsters) {
				StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
				if (staticMonster == null) {
					continue;
				}
				monsterInfo.addMonster(monsterId, staticMonster.getSoldierCount());
			}

			if (null == cityMonsterMap.get(city.getCityId())) {
				cityMonsterMap.put(city.getCityId(), monsterInfo);
			}
		}
	}

	// 获取城池怪物血量
	public StaticMonster getCityMonster(int cityId, int monsterId) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity is null, cityId = " + cityId);
			return null;
		}

		if (staticWorldCity.getMonsters() == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity.getMonsters() == null = " + cityId);
			return null;
		}

		for (Integer elem : staticWorldCity.getMonsters()) {
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(elem);
			if (elem == monsterId) {
				return staticMonster;
			}
		}

		return null;

	}

	public byte[] serCityMonster(int cityId) {
		SerCityMonster.Builder builder = SerCityMonster.newBuilder();
		CityMonster cityMonster = cityMonsterMap.get(cityId);
		if (cityMonster == null) {
			return builder.build().toByteArray();
		}
		builder.setData(cityMonster.writeData());
		return builder.build().toByteArray();
	}

	public byte[] serWarAttender(int cityId) {
		DataPb.CityWarAttender.Builder builder = DataPb.CityWarAttender.newBuilder();
		HashSet<Long> warAttenders = getWarAttenders().get(cityId);
		if (warAttenders != null) {
			builder.addAllLordIds(warAttenders);
		}
		return builder.build().toByteArray();
	}

	public List<Award> getAwards(int cityId) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			return new ArrayList<Award>();
		}

		List<Award> awards = new ArrayList<Award>();
		List<List<Integer>> outPut = staticWorldCity.getOutput();

		City city = getCity(cityId);
		if (city == null) {
			return new ArrayList<Award>();
		}

		// 把当前等级奖励筛选出来
		List<List<Integer>> lastRes = new ArrayList<List<Integer>>();
		for (List<Integer> elem : outPut) {
			if (elem == null || elem.size() != 5) {
				continue;
			}

			if (elem.get(0) != city.getCityLv()) {
				continue;
			}

			List<Integer> realAward = new ArrayList<Integer>();
			realAward.add(elem.get(1));
			realAward.add(elem.get(2));
			realAward.add(elem.get(3));
			realAward.add(elem.get(4));
			lastRes.add(realAward);
		}

		int lootNum = staticLimitMgr.getNum(48);
		for (int i = 1; i <= lootNum; i++) {
			Award award = lootManager.lootAwardByRate(lastRes);
			if (award.isOk()) {
				awards.add(award);
			}
		}
		return awards;
	}

	// 经过策划确认一次掉落一个物品
	public Award getCityAward(int cityId) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			return null;
		}

		List<List<Integer>> outPut = staticWorldCity.getOutput();

		City city = getCity(cityId);
		if (city == null) {
			return null;
		}

		// 把当前等级奖励筛选出来
		List<List<Integer>> lastRes = new ArrayList<List<Integer>>();
		for (List<Integer> elem : outPut) {
			if (elem == null || elem.size() != 5) {
				continue;
			}

			if (city.getCityLv() < elem.get(0)) {
				continue;
			}

			List<Integer> realAward = new ArrayList<Integer>();
			realAward.add(elem.get(1));
			realAward.add(elem.get(2));
			realAward.add(elem.get(3));
			realAward.add(elem.get(4));
			lastRes.add(realAward);
		}

		Award award = lootManager.lootAwardByRate(lastRes);
		return award;
	}

	public int getCitySoldier(int cityId) {
		CityMonster cityMonster = cityMonsterMap.get(cityId);
		if (cityMonster != null) {
			return cityMonster.getCitySoldier();
		}

		return 0;
	}

	public int getCityMaxSoldier(int cityId) {
		CityMonster cityMonster = cityMonsterMap.get(cityId);
		if (cityMonster != null) {
			return cityMonster.getCityMaxSoldier();
		}

		return 0;
	}

	public ConcurrentHashMap<Integer, HashSet<Long>> getWarAttenders() {
		return warAttenders;
	}

	public void setWarAttenders(ConcurrentHashMap<Integer, HashSet<Long>> warAttenders) {
		this.warAttenders = warAttenders;
	}

	public HashSet<Long> getWarAttenders(int cityId) {
		return warAttenders.get(cityId);
	}

	public void addWarAttender(int cityId, long lordId) {
		HashSet<Long> attenders = warAttenders.get(cityId);
		if (attenders == null) {
			attenders = new HashSet<Long>();
			warAttenders.put(cityId, attenders);
		}
		attenders.add(lordId);
	}

	public long getMakePeriod(int cityId) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity is null!");
			return 3600000L;
		}
		double activityFactor = activityManager.actDouble(ActivityConst.ACT_DESIGN_REDUCE);
		activityFactor = Math.max(0.0, activityFactor);
		activityFactor = Math.min(0.5, activityFactor);
		double totalAdd = 1.0 - activityFactor;
		return (long) (staticWorldCity.getPeriod() * TimeHelper.SECOND_MS * totalAdd);
	}

	public String getCityName(int cityId) {
		String cityName = "city";
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		if (worldCity != null) {
			int mapId = worldCity.getMapId();
			StaticWorldMap worldMap = staticWorldMgr.getStaticWorldMap(mapId);
			if (worldMap != null) {
				cityName = worldMap.getName() + worldCity.getName();
			}
		}

		return cityName;

	}

	// 攻占镇之后，所有的据点升级到2级
	public void handleAttackTown(StaticWorldCity staticWorldCity) {
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.error("staticWorldCity is null in handleAttackTown!");
			return;
		}

		if (isCityLevelup()) {
			LogHelper.CONFIG_LOGGER.error("isCityLevelup...");
			return;
		}

		handleCityLv();
		City city = getCity(staticWorldCity.getCityId());
		if (city.getCountry() == 0) {
			handleCityMonster();
		}
		// 所有的玩家发送一封邮件
		int gold = staticLimitMgr.getNum(73);
		int energy = staticLimitMgr.getNum(74);
		int iron = staticLimitMgr.getNum(75);
		Award goldAward = new Award(AwardType.GOLD, 0, gold);
		Award energyAward = new Award(AwardType.PROP, 96, energy);
		Award ironAward = new Award(AwardType.PROP, 1, iron);
		List<Award> awards = new ArrayList<Award>();
		awards.add(goldAward);
		awards.add(energyAward);
		awards.add(ironAward);
		// %s的%s被攻下来啦，军民联欢，全地图的据点从1级升到2级，可以有几率征收绿图纸了。
		int mapId = staticWorldCity.getMapId();

		String mapName = "unkown";
		String cityName = staticWorldCity.getName();
		StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
		if (staticWorldMap != null) {
			mapName = staticWorldMap.getName();
		}

		if (cityName == null || mapName == null) {
			LogHelper.CONFIG_LOGGER.error("cityName is null or mapName is null");
			return;
		}

		Map<Long, Player> playerMap = playerManager.getPlayers();
		for (Player player : playerMap.values()) {
			if (player == null) {
				continue;
			}
			playerManager.sendAttachMail(player, awards, MailId.ATTACK_CAMP, mapName, cityName);
		}

		synCityLevelUp();
	}

	public void synCityLevelUp() {
		WorldPb.SynCityLevelUpRq.Builder builder = WorldPb.SynCityLevelUpRq.newBuilder();
		for (City city : cityMap.values()) {
			int cityId = city.getCityId();
			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (worldCity.getType() != CityType.POINT) {
				continue;
			}

			CommonPb.CityLv.Builder cityLv = CommonPb.CityLv.newBuilder();
			cityLv.setCityId(cityId);
			cityLv.setCityLv(city.getCityLv());
			builder.addCityLv(cityLv);
		}

		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			playerManager.synCityLvRq(player, builder);
		}

	}

	public boolean isCityLevelup() {
		for (City city : cityMap.values()) {
			int cityId = city.getCityId();
			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (worldCity.getType() != CityType.POINT) {
				continue;
			}

			if (city.getCityLv() >= 2) {
				return true;
			}

		}

		return false;
	}

	public void handleCityLv() {
		for (City city : cityMap.values()) {
			int cityId = city.getCityId();
			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (worldCity.getType() != CityType.POINT) {
				continue;
			}

			int currentLv = city.getCityLv() + 1;
			currentLv = Math.min(2, currentLv);
			city.setCityLv(currentLv);
			// 清除图纸、怪物清除与替换
			city.setAwardNum((byte) 0);
		}
	}

	public void handleCityMonster() {
		for (CityMonster cityMonster : cityMonsterMap.values()) {
			int cityId = cityMonster.getCityId();
			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (worldCity.getType() != CityType.POINT) {
				continue;
			}

			// 先清除城池的野怪
			cityMonster.clear();
			List<Integer> monsters = battleMgr.getCityMonster(cityId, worldCity);
			for (Integer monsterId : monsters) {
				CityMonsterInfo monster = createCityMonster(monsterId);
				if (monster == null) {
					continue;
				}
				cityMonster.addMonster(monster);
			}
		}
	}

	public CityMonsterInfo createCityMonster(int monsterId) {
		StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
		if (staticMonster == null) {
			LogHelper.CONFIG_LOGGER.error("monsterId = " + monsterId + " not found!");
			return null;
		}
		CityMonsterInfo cityMonsterInfo = new CityMonsterInfo();
		cityMonsterInfo.setMonsterId(staticMonster.getMonsterId());
		cityMonsterInfo.setMaxSoldier(staticMonster.getSoldierCount());
		cityMonsterInfo.setSoldier(staticMonster.getSoldierCount());
		return cityMonsterInfo;
	}

	public boolean captureCity(int cityType) {
		for (City city : cityMap.values()) {
			if (city == null) {
				continue;
			}
			int cityId = city.getCityId();
			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (city.getCountry() != 0 && worldCity.getType() == cityType) {
				return true;
			}
		}

		return false;
	}

	// 获得一个国家拥有的名城的个数
	public int getFamousCityNum(int country) {
		int num = 0;
		for (Integer cityId : getFamousCity()) {
			City city = cityMap.get(cityId);
			if (city == null) {
				continue;
			}

			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (city.getCountry() == country) {
				num += 1;
			}
		}

		return num;
	}

	public int getTotalFamousCity() {
		int num = 0;
		for (Integer cityId : getFamousCity()) {
			City city = cityMap.get(cityId);
			if (city == null) {
				continue;
			}

			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity == null) {
				continue;
			}

			if (city.getCountry() != 0) {
				num += 1;
			}
		}

		return num;
	}

	public boolean hasFullFamous(int county) {
		return getFamousCityNum(county) >= staticLimitMgr.getNum(110);
	}

	// 检查一个都城是否可以宣战
	public boolean hasSquareFortress(int country) {
		for (Integer cityId : getSquareFortress()) {
			City city = cityMap.get(cityId);
			if (city == null) {
				continue;
			}

			if (city.getCountry() == country) {
				return true;
			}
		}

		return false;
	}

	// 检查一个都城是否有国家
	public boolean squareHasCountry(int cityId) {
		City city = getCity(cityId);
		if (city == null) {
			LogHelper.CONFIG_LOGGER.error("city is null!");
			return false;
		}

		return city.getCountry() != 0;
	}

	public List<Integer> getSquareFortress() {
		return squareFortress;
	}

	public void setSquareFortress(List<Integer> squareFortress) {
		this.squareFortress = squareFortress;
	}

	public boolean isSquareLevelFull() {
		return isSquareLevelFull;
	}

	public void setSquareLevelFull(boolean squareLevelFull) {
		isSquareLevelFull = squareLevelFull;
	}

	public void checkSquareCity() {
		for (Integer cityId : getSquareFortress()) {
			City city = getCity(cityId);
			if (city == null) {
				continue;
			}

			if (city.getCityLv() < 3) {
				isSquareLevelFull = false;
				break;
			}
		}

	}

	// 禁卫军发起名城战
	public void attackFamous() {
		long now = System.currentTimeMillis();
		for (int i = 0; i < getSquareFortress().size(); i++) {
			int cityId = getSquareFortress().get(i);
			City city = getCity(cityId);
			handleFamousAttack(city, now);
		}
	}

	// 攻打名城
	public void handleFamousAttack(City city, long now) {
		if (city == null) {
			return;
		}

		if (staticLimitMgr.isSimpleWarOpen()) {
			if (city.getNextAttackTime() > now + 900000L) {
				city.setNextAttackTime(900000L + now);
			}
		}

		if (city.getNextAttackTime() > now) {
			return;
		}

		if (city.getCountry() == 0) {
			handleGuardsAttack(city, now); // 群雄禁卫军
		} else {
			handleCountryAttack(city, now);
		}

	}

	// 群雄攻打玩家
	public void handleGuardsAttack(City city, long now) {
		int countryA = getFamousCityNum(1);
		int countryB = getFamousCityNum(2);
		int countryC = getFamousCityNum(3);
		if (countryA + countryB + countryC <= 0) {
			return;
		}

		// 检查禁卫军是否已经出动,已经出动的禁卫军就不会再出动
		int max = Math.max(countryA, countryB);
		max = Math.max(max, countryC);
		int min = Math.min(countryA, countryB);
		min = Math.min(min, countryC);
		int other = countryA + countryB + countryC - min - max;
		int total = countryA + countryB + countryC;
		int country;
		if (countryA == max) {
			country = 1;
		} else if (countryB == max) {
			country = 2;
		} else {
			country = 3;
		}

		if (max < (min + other)) { // 4个小时打一次, 随机选取国家
			// 按照countryA,countryB,countryC权重进行随机
			int randNum = RandomHelper.threadSafeRand(1, total);
			long interval = staticLimitMgr.getNum(107) * TimeHelper.SECOND_MS;
			if (staticLimitMgr.isSimpleWarOpen()) {
				interval = 900000L;
			}

			if (randNum <= countryA) {
				selectPlayerCity(city, 1, 1);
			} else if (randNum <= countryA + countryB) {
				selectPlayerCity(city, 2, 1);
			} else if (randNum <= total) {
				selectPlayerCity(city, 3, 1);
			}
			city.setNextAttackTime(now + interval);
		} else if (max >= (min + other)) { // 2个小时打一次, 选择max国家
			long interval = staticLimitMgr.getNum(108) * TimeHelper.SECOND_MS;
			if (staticLimitMgr.isSimpleWarOpen()) {
				interval = 900000L;
			}

			selectPlayerCity(city, country, 2);
			city.setNextAttackTime(now + interval);
		} else if (min + other == 0) { // 1个小时打一次, 选择max国家
			long interval = staticLimitMgr.getNum(109) * TimeHelper.SECOND_MS;
			if (staticLimitMgr.isSimpleWarOpen()) {
				interval = 900000L;
			}

			selectPlayerCity(city, country, 3);
			city.setNextAttackTime(now + interval);
		}
	}

	// 选择一个距离最近的玩家城池
	public void selectPlayerCity(City guardCity, int country, int selectState) {
		long now = System.currentTimeMillis();
		if (country == 0) {
			LogHelper.CONFIG_LOGGER.error("country == 0, logic error!");
			return;
		}

		if (guardCity == null) {
			LogHelper.CONFIG_LOGGER.error("guardCity is null!");
			return;
		}

		if (guardCity.getCountry() != 0) {
			LogHelper.CONFIG_LOGGER.error("guardCity country is 0.");
			return;
		}

		StaticWorldCity worldCity = staticWorldMgr.getCity(guardCity.getCityId());
		if (worldCity == null) {
			LogHelper.CONFIG_LOGGER.error("world city is null, cityId = " + guardCity.getCityId());
			return;
		}

		int cityPosx = worldCity.getX();
		int cityPosy = worldCity.getY();

		// 选择这个国家距离最近的都城
		int minDis = Integer.MAX_VALUE;
		int targetCityId = 0;
		for (Integer cityId : getFamousCity()) {
			City city = getCity(cityId);
			if (city == null) {
				continue;
			}

			if (city.getCountry() == 0) {
				continue;
			}

			if (city.getCountry() != country) {
				continue;
			}

			// 有保护的城池不能攻打
			if (city.getProtectedTime() > now) {
				continue;
			}

			//被选中的抢夺名城不能攻打
			if (city.getState() != CityState.COMMON_MAKE_ITEM) {
				continue;
			}

			StaticWorldCity config = staticWorldMgr.getCity(city.getCityId());
			if (config == null) {
				LogHelper.CONFIG_LOGGER.error("world city is null, cityId = " + city.getCityId());
				continue;
			}

			int distance = worldManager.distance(cityPosx, cityPosy, config.getX(), config.getY());
			if (minDis > distance) {
				minDis = distance;
				targetCityId = city.getCityId();
			}
		}

		if (targetCityId == 0) {
			return;
		}

		// 开始宣战
		// 检查当前城池是否有战争,如果有说明已经宣战了
		// 搞清楚playerPos 和 targetPos的作用
		StaticWorldCity targetConfig = staticWorldMgr.getCity(targetCityId);
		if (targetConfig == null) {
			LogHelper.CONFIG_LOGGER.error("targetConfig is null, targetCityId = " + targetCityId);
			return;
		}

		int cityLv = guardCity.getCityLv();
		if (cityLv < 1 || cityLv > 3) {
			LogHelper.CONFIG_LOGGER.error("cityLv is error, cityLv = " + cityLv);
			return;
		}

		// 获取禁卫军的Id
		List<Integer> pickLvs = new ArrayList<Integer>();
		if (selectState == 1) {
			// 随机选择1个
			pickLvs.add(RandomHelper.threadSafeRand(1, cityLv));
		} else if (selectState == 2) {
			// 随机选择2个
			List<Integer> monsteLvs = new ArrayList<Integer>();
			for (int i = 1; i <= cityLv; i++) {
				monsteLvs.add(i);
			}

			Collections.shuffle(monsteLvs);
			for (int i = 1; i <= monsteLvs.size() && i <= 2; i++) {
				pickLvs.add(monsteLvs.get(i - 1));
			}

		} else if (selectState == 3) {
			// 选择3个
			for (int i = 1; i <= cityLv; i++) {
				pickLvs.add(i);
			}
		}

		if (pickLvs.isEmpty()) {
			LogHelper.CONFIG_LOGGER.error("logic error, pickLvs is empty!");
			return;
		}

		Collections.sort(pickLvs);
		Collections.reverse(pickLvs);

		int mapId = targetConfig.getMapId();
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.error("mapId = " + mapId + ", mapInfo is null!");
			return;
		}

		City targetCity = getCity(targetCityId);
		if (targetCity == null) {
			LogHelper.CONFIG_LOGGER.error("targetCity is null");
			return;
		}

		Pos targetPos = new Pos(targetConfig.getX(), targetConfig.getY());
		long period = targetConfig.getWarPeriod() * TimeHelper.MINUTE_MS;

		// 检查当前国家是否有战斗，如果有战斗则加入战斗，如果没有战斗，则创建战斗
		// 如果有战斗，则加入战斗
		CountryCityWarInfo countryWar = mapInfo.getCountryCityWar(targetCityId, 0);
		if (countryWar == null) {
			int monsterLv = pickLvs.get(0);
			StaticSquareMonster monster = staticWorldMgr.getSquareMonster(monsterLv);
			if (monster == null) {
				LogHelper.CONFIG_LOGGER.error("square monster is null, cityLv = " + cityLv);
				return;
			}

			countryWar = warManager.createMonsterActCountryWar(period, monster.getMonsters(), guardCity.getCountry(), targetCity, new Pos(cityPosx, cityPosy),
				targetPos, WarType.ATTACK_COUNTRY);

			mapInfo.addWar(countryWar);
		}

		// 获取禁卫军的Id
		for (Integer lv : pickLvs) {
			StaticSquareMonster config = staticWorldMgr.getSquareMonster(lv);
			if (config == null) {
				LogHelper.CONFIG_LOGGER.error("square monster is null, lv = " + lv);
				continue;
			}

			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(config.getMonsters());
			if (staticMonster == null) {
				LogHelper.CONFIG_LOGGER.error("staticMonster config error, monsteId = " + config.getMonsters());
				continue;
			}

			SquareMonster squareMonster = new SquareMonster();
			squareMonster.setMonsterLv(lv);
			squareMonster.setMonsterId(staticMonster.getMonsterId());
			countryWar.updateMonster(squareMonster.getMonsterId(), squareMonster);
		}

		// 通知当前国家的且在世界要塞地图上的玩家有攻城战产生, 同步给地图上的玩家
		warManager.synWarInfo(countryWar, 0, targetCity.getCountry());
//		String tarPos = String.format("%s,%s", targetPos.getX(), targetPos.getY());
//		chatManager.sendWorldChat(ChatId.NPC_COUNTRY_WAR, String.valueOf(targetCity.getCountry()),String.valueOf(mapId), String.valueOf(targetCityId), tarPos);
	}

	// 处理玩家禁卫军出动
	public void handleCountryAttack(City currentCity, long now) {
		// LogHelper.GAME_DEBUG.error("玩家禁卫军开始出动...");
		if (currentCity == null) {
			LogHelper.CONFIG_LOGGER.error("currentCity is null!");
			return;
		}

		if (currentCity.getCountry() == 0) {
			LogHelper.CONFIG_LOGGER.error("currentCity country is 0.");
			return;
		}

		StaticWorldCity worldCity = staticWorldMgr.getCity(currentCity.getCityId());
		if (worldCity == null) {
			LogHelper.CONFIG_LOGGER.error("world currentCity is null, currentCityId = " + currentCity.getCityId());
			return;
		}

		int cityPosx = worldCity.getX();
		int cityPosy = worldCity.getY();

		// 选择这个国家距离最近的都城
		int minDis = Integer.MAX_VALUE;
		int targetCityId = 0;
		int currentCountry = currentCity.getCountry();
		for (Integer cityId : getFamousCity()) {
			City city = getCity(cityId);
			if (city == null) {
				continue;
			}

			if (city.getCountry() == 0) {
				continue;
			}

			// 过滤玩家的城池
			if (city.getCountry() == currentCountry) {
				continue;
			}

			// 有保护的城池不能攻打
			if (city.getProtectedTime() > now) {
				continue;
			}

			//被选中的抢夺名城不能攻打
			if (city.getState() != CityState.COMMON_MAKE_ITEM) {
				continue;
			}

			StaticWorldCity config = staticWorldMgr.getCity(city.getCityId());
			if (config == null) {
				LogHelper.GAME_DEBUG.debug("world city is null, cityId = " + city.getCityId());
				continue;
			}

			int distance = worldManager.distance(cityPosx, cityPosy, config.getX(), config.getY());
			if (minDis > distance) {
				minDis = distance;
				targetCityId = city.getCityId();
			}
		}

		// 说明找不到城池可以被攻击了
		if (targetCityId == 0) {
			// LogHelper.GAME_DEBUG.debug("targetCityId is 0...");
			return;
		}

		// 开始宣战
		// 检查当前城池是否有战争,如果有说明已经宣战了
		// 搞清楚playerPos 和 targetPos的作用, playerPos无作用，tartgetPos用来行军和邮件
		StaticWorldCity targetConfig = staticWorldMgr.getCity(targetCityId);
		if (targetConfig == null) {
			LogHelper.GAME_DEBUG.debug("targetConfig is null, targetCityId = " + targetCityId);
			return;
		}

		int cityLv = currentCity.getCityLv();
		if (cityLv < 1 || cityLv > 3) {
			LogHelper.GAME_DEBUG.debug("cityLv is error, cityLv = " + cityLv);
			return;
		}

		// 获取禁卫军的Id
		int squareLv = Math.min(3, cityLv);
		squareLv = Math.max(1, squareLv);
		int randLv = RandomHelper.threadSafeRand(1, squareLv);
		StaticSquareMonster monster = staticWorldMgr.getSquareMonster(randLv);
		if (monster == null) {
			LogHelper.GAME_DEBUG.debug("square monster is null, cityLv = " + cityLv);
			return;
		}

		int mapId = targetConfig.getMapId();
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.GAME_DEBUG.debug("mapId = " + mapId + ", mapInfo is null!");
			return;
		}

		City targetCity = getCity(targetCityId);
		if (targetCity == null) {
			LogHelper.GAME_DEBUG.debug("targetCity is null");
			return;
		}

		// 获取禁卫军的Id
		StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monster.getMonsters());
		if (staticMonster == null) {
			LogHelper.GAME_DEBUG.debug("staticMonster is error, id = " + monster.getMonsters());
			return;
		}

		Pos targetPos = new Pos(targetConfig.getX(), targetConfig.getY());
		long period = targetConfig.getWarPeriod() * TimeHelper.MINUTE_MS;
		// 玩家的近卫军
		CountryCityWarInfo countryWar = mapInfo.getCountryCityWar(targetCityId, currentCity.getCountry());
		if (countryWar == null) {
			countryWar = warManager.createMonsterActCountryWar(period, monster.getMonsters(), currentCity.getCountry(), targetCity, new Pos(cityPosx, cityPosy),
				targetPos, WarType.ATTACK_COUNTRY);

			mapInfo.addWar(countryWar);
		}

		// 获取禁卫军的Id
		SquareMonster squareMonster = new SquareMonster();
		squareMonster.setMonsterLv(randLv);
		squareMonster.setMonsterId(staticMonster.getMonsterId());
		countryWar.updateMonster(staticMonster.getMonsterId(), squareMonster);

		// 通知当前国家的且在世界要塞地图上的玩家有攻城战产生, 同步给地图上的玩家
		currentCity.setNextAttackTime(staticLimitMgr.getNum(111) * TimeHelper.SECOND_MS + now);
		if (staticLimitMgr.isSimpleWarOpen()) {
			long interval = 900000L;
			currentCity.setNextAttackTime(interval + now);
		}

		warManager.synWarInfo(countryWar, currentCity.getCountry(), targetCity.getCountry());
		String tarPos = String.format("%s,%s", targetPos.getX(), targetPos.getY());
		chatManager.sendCountryChat(currentCountry, ChatId.NPC_COUNTRY_WAR, String.valueOf(targetCity.getCountry()), String.valueOf(mapId), String.valueOf(targetCityId), tarPos);
	}

	public List<Integer> getFamousCity() {
		return famousCity;
	}

	public void setFamousCity(List<Integer> famousCity) {
		this.famousCity = famousCity;
	}

	public int getWorldFortress() {
		return worldFortress;
	}

	public void setWorldFortress(int worldFortress) {
		this.worldFortress = worldFortress;
	}

	public List<Integer> getAllCenterCity() {
		return allCenterCity;
	}

	public void setAllCenterCity(List<Integer> allCenterCity) {
		this.allCenterCity = allCenterCity;
	}

	// 获取当前国家人口
	public int getCountryPeople(int country) {
		int total = 0;
		for (Integer cityId : allCenterCity) {
			City city = getCity(cityId);
			if (city == null) {
				continue;
			}

			if (city.getCountry() != country) {
				continue;
			}

			StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
			if (worldCity.getType() == CityType.FAMOUS_CITY) {
				total += staticLimitMgr.getNum(97);
			} else if (worldCity.getType() == CityType.SQUARE_FORTRESS) {
				int cityLv = city.getCityLv();
				if (cityLv == 1) {
					total += staticLimitMgr.getNum(98);
				} else if (cityLv == 2) {
					total += staticLimitMgr.getNum(99);
				} else if (cityLv == 3) {
					total += staticLimitMgr.getNum(100);
				}
			}

		}
		return total;
	}


	public void clearCityLordId(City city) {
		// 先清除玩家身上的cityId
		long lordId = city.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player != null) {
			player.setCityId(0);
		}

		// 再清除city身上的玩家Id
		if (city.getLordId() != 0) {
			city.setLordId(0L);
		}
	}

	public boolean isInFortress(Pos pos) {
		StaticWorldCity worldCity = staticWorldMgr.getCity(CityId.WORLD_CITY_ID);
		if (worldCity == null) {
			return false;
		}

		int rangeX1 = worldCity.getRangex1();
		int rangeX2 = worldCity.getRangex2();
		int rangeY1 = worldCity.getRangey1();
		int rangeY2 = worldCity.getRangey2();

		return pos.getX() >= rangeX1 && pos.getX() <= rangeX2 && pos.getY() >= rangeY1 && pos.getY() <= rangeY2;
	}

	public void handleRecSoldier(int cityId) {
		CityMonster cityMonster = getCityMonster(cityId);
		if (cityMonster != null) {
			Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
			for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
				if (cityMonsterInfo != null) {
					cityMonsterInfo.setSoldier(cityMonsterInfo.getMaxSoldier());
				}
			}
		}
	}

	public void handleCityAwardTime(City city) {
		int sendTime = staticLimitMgr.getNum(46);
		long nighHour = TimeHelper.getCityMailTime(sendTime);
		long now = System.currentTimeMillis();
		if (city.getSendAwardTime() <= now) {
			if (now >= nighHour) { // 九点以后
				city.setSendAwardTime(nighHour + TimeHelper.DAY_MS);
			} else {
				// pass, 九点前
				city.setSendAwardTime(nighHour);
			}
		} else {
			// pass, 时间还没到
		}
	}

	public void handleSetAwardTime(City city) {
		int sendTime = staticLimitMgr.getNum(46);
		long nighHour = TimeHelper.getCityMailTime(sendTime);
		city.setSendAwardTime(nighHour + TimeHelper.DAY_MS);
	}

	// 获取己方四方要塞
	public City checkAndGetHome(int country) {
		for (Integer cityId : this.squareFortress) {
			City city = getCity(cityId);
			if (city != null && city.getCountry() == country) {
				return city;
			}
		}
		return null;
	}
}
