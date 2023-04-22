package com.game.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.game.Loading;
import com.game.constant.CityState;
import com.game.constant.Reason;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.define.LoadData;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.StaticActStealCity;
import com.game.domain.s.StaticWorldActPlan;
import com.game.domain.s.StaticWorldCity;
import com.game.pb.CommonPb;
import com.game.pb.WorldPb;
import com.game.service.CityService;
import com.game.service.WorldActPlanService;
import com.game.util.*;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Component
public class StealCityManager {

	// 被选中的名城只能由官员进行宣战（之前的被宣战状态和战斗等待队伍会在被选中时清空, 禁卫军也需要停止）
	Map<Integer, TreeSet<Integer>> selectdMap = new HashMap<Integer, TreeSet<Integer>>(); // 需要存盘

	//每个选择城池领取奖励的记录值
	Map<Integer, LinkedList<Long>> receiveAward = new ConcurrentHashMap<Integer, LinkedList<Long>>();

	int actState = 0; // 活动开启状态
	int minutes = 0;
	int hour = 18;

	// 用状态机解决城池的问题
	// city: 1 正常生产 2.红色图纸生产 3.红色图纸生产完成
	// 如果红色图纸被领取完毕，恢复到正常生产状态
	@Autowired
	private StaticActivityMgr staticActivityMgr;

	@Autowired
	private CityManager cityManager;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;

	@Autowired
	private WorldActPlanService worldActPlanService;

	@Autowired
	private LootManager lootManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private CityService cityService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	// [[18,30],[19,30]]
	public List<Date> getState(String dateStr) {
		JSONArray arrays = JSONArray.parseArray(dateStr);
		List<List<Integer>> listList = new ArrayList<List<Integer>>();
		for (int i = 0; i < arrays.size(); i++) {
			List<Integer> list = new ArrayList<Integer>();
			JSONArray array = arrays.getJSONArray(i);
			for (int j = 0; j < array.size(); j++) {
				list.add(array.getInteger(j));
			}
			listList.add(list);
		}

		List<Date> listDate = new ArrayList<Date>();
		List<Integer> start = listList.get(0);
		List<Integer> end = listList.get(1);
		listDate.add(setDate(start));
		listDate.add(setDate(end));

		return listDate;
	}

	public Date getTestData(int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	public void initSelectMap() {
		LogHelper.GAME_DEBUG.error("活动进入初始化状态, 准备清理相关的数据..");
		if (!selectdMap.isEmpty()) {
			selectdMap.clear();
			LogHelper.GAME_DEBUG.error("清空selectMap...");
			for (int i = 1; i <= 3; i++) {
				selectdMap.put(i, new TreeSet<Integer>());
				LogHelper.GAME_DEBUG.error("初始化selectMap为三个TreeSet...");
			}
		}
	}

	public List<Date> getConfigDate(List<List<Integer>> time) {
		if (!checkTime(time)) {
			return new ArrayList<Date>();
		}

		List<Date> listDate = new ArrayList<Date>();
		List<Integer> start = time.get(0);
		if (!checkDate(start)) {
			return new ArrayList<Date>();
		}

		List<Integer> end = time.get(1);
		if (!checkDate(end)) {
			return new ArrayList<Date>();
		}

		listDate.add(setDate(start));
		listDate.add(setDate(end));

		return listDate;
	}

	public boolean checkTime(List<List<Integer>> time) {
		if (time == null) {
			LogHelper.CONFIG_LOGGER.info("time is null.");
			return false;
		}

		if (time.size() != 2) {
			LogHelper.CONFIG_LOGGER.info("time.size() != 2.");
			return false;
		}

		return true;
	}

	public boolean checkDate(List<Integer> date) {
		if (date == null || date.size() != 2) {
			LogHelper.CONFIG_LOGGER.info("date == null || date.size() != 2.");
			return false;
		}
		return true;
	}


	public Date setDate(List<Integer> format) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, format.get(0));
		calendar.set(Calendar.MINUTE, format.get(1));
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	public StaticActStealCity getConfig() {
		Map<Integer, StaticActStealCity> stealCityMap = staticActivityMgr.getStealCityMap();
		if (stealCityMap.isEmpty()) {
			return null;
		}

		StaticActStealCity config = null;
		for (StaticActStealCity actStealCity : stealCityMap.values()) {
			if (actStealCity == null) {
				continue;
			}
			config = actStealCity;
		}

		if (config == null) {
			LogHelper.CONFIG_LOGGER.info("staticActStealCity is null.");
			return null;
		}

		return config;
	}

	public List<List<Date>> getAllDate() {
		StaticActStealCity config = getConfig();
		if (config == null) {
			return new ArrayList<List<Date>>();
		}
		List<Date> listDate1 = getConfigDate(config.getTime1());
		List<Date> listDate2 = getConfigDate(config.getTime2());
		List<Date> listDate3 = getConfigDate(config.getTime3());
		List<List<Date>> allDate = new ArrayList<List<Date>>();
		allDate.add(listDate1);
		allDate.add(listDate2);
		allDate.add(listDate3);

		return allDate;
	}


	// 1.判断当前处于活动的哪个状态
	// 返回1,2,3 说明处于活动的1~3阶段
	// 返回0，表示活动还未开启
	public int queryIndex() {
		List<List<Date>> allDate = getAllDate();
		if (allDate.isEmpty()) {
			return 0;
		}

		int nowIndex = 0;
		Date now = new Date();
		int len = allDate.size();
		for (int index = 0; index < len; index++) {
			List<Date> time = allDate.get(index);
			//if (now.after(time.get(0)) && now.before(time.get(1))) {
			//return index + 1;
			if (now.after(time.get(0))) {
				nowIndex = nowIndex + 1;
			}
		}

		return nowIndex;
	}


	public int queryIndexByNow(List<Integer> format) {
		List<List<Date>> allDate = getAllDate();
		if (allDate.isEmpty()) {
			return 0;
		}

		Date now = setDate(format);
		LogHelper.GAME_DEBUG.error("当前的时间为:" + TimeHelper.getFormatData(now));
		int len = allDate.size();
		for (int index = 0; index < len; index++) {
			List<Date> time = allDate.get(index);
			if (now.after(time.get(0)) && now.before(time.get(1))) {
				return index + 1;
			}
		}

		return 0;
	}


	// 2.处于活动状态，是否已经清除当前选中城池的状态
	// 活动状态机方案和皇城血战一样
	// 中途停止服务器, actState检查以及启动服务器阶段检查
	// 这个活动需要加上是哪一天，否则每天都会开
	public void doAction() {
		checkRedMakeItem();
		checkMakeItem();
		if (actState == 0) {  // 进入活动初始化
			actState = 1;     // 进入初始化状态
			initSelectMap();
			LogHelper.GAME_DEBUG.error("抢夺名城进入城池选择阶段...");
		} else if (actState == 1) {  // 进入活动状态1, 2, 3
			// 如果当前时间处于index, index没有做相关逻辑，则进行相关逻辑
			// 如果做了，就继续执行, 相关逻辑标记在
			int state = queryIndex();
			LogHelper.GAME_DEBUG.error("抢夺名城当前处于城池选择阶段...." + state);

			// 读取城池
			StaticActStealCity config = getConfig();
			if (config == null) {
				LogHelper.CONFIG_LOGGER.info("配置不存在..");
				/* minutes++;*/
				return;
			}

			// 检查当前阶段城池是否已经进行了选择
			TreeSet<Integer> selectSet = selectdMap.get(state);
			if (selectSet != null && selectSet.size() > 0) {
				/*minutes++;*/
               /* LogHelper.CONFIG_LOGGER.info("抢夺名城活动处于阶段"+state);
                LogHelper.GAME_DEBUG.error("读取到的城池2选择的配置为=" + selectSet);*/
				return;
			}

			// 获取当前的配置
			List<Integer> city1 = config.getCity1();
			List<Integer> city2 = config.getCity2();
			List<Integer> city3 = config.getCity3();

			List<Integer> checkCity1 = new ArrayList<Integer>();
			checkCity1.addAll(city1);

			List<Integer> checkCity2 = new ArrayList<Integer>();
			checkCity2.addAll(city2);

			List<Integer> checkCity3 = new ArrayList<Integer>();
			checkCity3.addAll(city3);

			// 进行踢出
			kickOut(checkCity1, checkCity2, checkCity3);
			LogHelper.GAME_DEBUG.error("读取到的城池1选择的配置为=" + checkCity1);
			LogHelper.GAME_DEBUG.error("读取到的城池2选择的配置为=" + checkCity2);
			LogHelper.GAME_DEBUG.error("读取到的城池3选择的配置为=" + checkCity3);
			List<Integer> currentSelected = new ArrayList<Integer>();
			handleSelect(state, checkCity1, checkCity2, checkCity3, selectdMap, currentSelected);
			makeItem(currentSelected);
			kickOut(checkCity1, checkCity2, checkCity3);
			LogHelper.GAME_DEBUG.error("踢出后,读取到的城池1选择的配置为=" + checkCity1);
			LogHelper.GAME_DEBUG.error("踢出后,读取到的城池2选择的配置为=" + checkCity2);
			LogHelper.GAME_DEBUG.error("踢出后,读取到的城池3选择的配置为=" + checkCity3);
			LogHelper.GAME_DEBUG.error("当前选中的城池为=" + currentSelected);

			LogHelper.GAME_DEBUG.error("当前活动阶段城池选择为:");
			for (int i = 1; i <= 3; i++) {
				printCityId(i, selectdMap.get(i));
			}

			if (isAllSelected()) {
				actState = 2;
			}
		} else if (actState == 2) {  // 不在活动范围内, 则计算下一次活动, 并进行活动数据清除
			LogHelper.GAME_DEBUG.error("活动选城结束了，所有城池选择完毕...");
		}
	}

	public void kickOut(List<Integer> checkCity1, List<Integer> checkCity2,
		List<Integer> checkCity3) {
		for (int i = 1; i <= 3; i++) {
			TreeSet<Integer> selectSet = selectdMap.get(i);
			checkCity1.removeAll(selectSet);
			checkCity2.removeAll(selectSet);
			checkCity3.removeAll(selectSet);
		}
	}

	public void handleSelect(int state,
		List<Integer> city1,
		List<Integer> city2,
		List<Integer> city3,
		Map<Integer, TreeSet<Integer>> citySelected,
		List<Integer> currentSelected) {
		if (city1.isEmpty() || city2.isEmpty() || city3.isEmpty()) {
			return;
		}

		if (state == 0) {
			return;
		}
		TreeSet<Integer> selectSet = citySelected.get(state);
		if (selectSet != null ) {
			if( selectSet.size() > 0){
				return;
			}
			int index1 = RandomHelper.threadSafeRand(0, city1.size() - 1);
			int index2 = RandomHelper.threadSafeRand(0, city2.size() - 1);
			int index3 = RandomHelper.threadSafeRand(0, city3.size() - 1);
			Integer cityId1 = city1.get(index1);
			Integer cityId2 = city2.get(index2);
			Integer cityId3 = city3.get(index3);
			currentSelected.clear();
			selectSet.add(cityId1);
			selectSet.add(cityId2);
			selectSet.add(cityId3);
			LogHelper.GAME_DEBUG.error("算法选中的城池 = " + cityId1 + ", " + cityId2 + ", " + cityId3);
			currentSelected.add(cityId1);
			currentSelected.add(cityId2);
			currentSelected.add(cityId3);
			LogHelper.GAME_DEBUG.error("selectd = " + citySelected);
			LogHelper.GAME_DEBUG.error("currentSelected = " + currentSelected);
		}
	}


	public void printCityId(int index, TreeSet<Integer> cityIds) {
		System.out.printf("阶段:" + index + ", cityId = " + cityIds + "\n");
	}

	// 城池是否选择
	public void checkState() {
		// 开始检测活动
		LogHelper.GAME_DEBUG.error("--------------抢夺名城状态机分隔符----------------");
		LogHelper.GAME_DEBUG.error("开始检测活动 ...");

		int index = queryIndex();
		if (index == 0) {
			actState = 0;
		} else {
			if (isAllSelected()) {
				actState = 2;
			} else {
				actState = 1;
			}
		}

		if (actState == 0) {
			for (int i = 1; i <= 3; i++) {
				selectdMap.put(i, new TreeSet<Integer>());
				LogHelper.GAME_DEBUG.error("初始化selectMap为三个TreeSet ...");
			}

			LogHelper.GAME_DEBUG.error("活动处于的阶段, index = " + index);
			if (index == 0) {
				actState = 0;
			}
		}
		doAction();
	}

	public boolean isAllSelected() {
		boolean isSelected = true;
		for (int i = 1; i <= 3; i++) {
			TreeSet<Integer> select = selectdMap.get(i);
			if (select == null) {
				return false;
//                continue;
			}

			if (select.size() != 3) {
				isSelected = false;
				break;
			}
		}
		return isSelected;
	}

	/**
	 * 为选中的城市设置打造图纸中的状态
	 *
	 * @param currentSelected
	 */
	public void makeItem(List<Integer> currentSelected) {
		for (Integer cityId : currentSelected) {
			City city = cityManager.getCity(cityId);
			if (city == null) {
				continue;
			}

			long now = System.currentTimeMillis();
			LogHelper.GAME_DEBUG.error("开始清除城池Id=" + cityId + "生产时间、数量以及关闭保护罩");

			city.setAwardNum((byte) 0);
			city.setProtectedTime(now);
			city.setMakeItemTime(now + TimeHelper.HOUR_MS);
			city.setState(CityState.RED_MAKE_ING);

			handleWarReturn(cityId);
		}

		synStealCity();
	}

	public void checkRedMakeItem() {
		long now = System.currentTimeMillis();
		for (TreeSet<Integer> cityId : selectdMap.values()) {
			if (cityId == null) {
				continue;
			}
			for (Integer id : cityId) {
				City city = cityManager.getCity(id);
				if (city == null) {
					continue;
				}

				if (city.getState() != CityState.RED_MAKE_ING) {
					continue;
				}

             /*   if (city.getMakeItemTime() <= now && city.getCountry() != 0) {
                    StaticActStealCity config = getConfig();
                    int num = 10;
                    if (null != config) {
                        int configNum = config.getNum();
                        if (configNum > 0) {
                            num = configNum;
                        }
                    }
                    city.setAwardNum((byte) num);
                    LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + "生产出" + num + "次机会!!!");
                    city.setState(CityState.RED_MAKE_DONE);
                } else if (city.getMakeItemTime() <= now && city.getCountry() == 0) {
                    city.setAwardNum((byte) 0);  // 清零红色图纸
                    LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + "没有任何国家占领，不生产任何图纸");
                    StaticWorldCity worldCity = worldMgr.getCity(city.getCityId());
                    // 如果一个名城有红色图纸产出，则这个城池就不应该生产金色图纸
                    // 分开成两个check函数编写
                    if (worldCity != null) {
                        city.setMakeItemTime(now + TimeHelper.SECOND_MS * 10);
                        LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + ", 恢复正常生产...");
                        city.setAwardNum((byte) 0);
                    }
                    city.setState(CityState.COMMON_MAKE_ITEM);
                }*/

				/**
				 * 1.当生产时间小于等于当前时间时,表示生产完成
				 * 2.不管是中立状态，还是有所属阵营的都需要生产图纸
				 */
				if (city.getMakeItemTime() <= now) {
					StaticActStealCity config = getConfig();
					int num = 10;
					if (null != config) {
						int configNum = config.getNum();
						if (configNum > 0) {
							num = configNum;
						}
					}
					city.setAwardNum((byte) num);
					LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + "生产出" + num + "次机会!!!");
					city.setState(CityState.RED_MAKE_DONE);

					synStealCity();
				}
			}
		}
	}


	public void checkMakeItem() {
		long now = System.currentTimeMillis();
		for (TreeSet<Integer> cityIds : selectdMap.values()) {
			if (cityIds == null) {
				continue;
			}
			for (Integer id : cityIds) {
				City city = cityManager.getCity(id);
				if (city == null) {
					continue;
				}

				int state = city.getState();
				long period = cityManager.getMakePeriod(city.getCityId());

				if (state == CityState.RED_MAKE_DONE) {
					if (city.getAwardNum() <= (byte) 0) {
						city.setState(CityState.COMMON_MAKE_ITEM);
						city.setMakeItemTime(now + period); // 恢复正常生产
						LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + ", 生产图纸1 ...");
						city.setAwardNum((byte) (0));

						synStealCity();
					}
				} else if (state == CityState.RED_MAKE_ING) {
					continue;
				}

				if (city.getMakeItemTime() > now) {
					continue;
				}

               /* city.setMakeItemTime(now + TimeHelper.SECOND_MS * 10); // 正常生产
                LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + ", 生产图纸1 ...");
                city.setAwardNum((byte) (city.getAwardNum() + 1));*/
			}
		}
	}

	//1.判断当前是否处于活动状态
	//2.处于活动状态，是否已经清楚当前选中城池的状态
	//3.清除生产时间
	//4.清除生产次数
	//5.清除玩家战斗
	//6.清除禁卫军战斗
	//7.禁卫军无法出动战斗，或者踢出禁卫军战斗
	//8.图纸征收变换: 普通征收或者红色图纸征收
	//9.生产时间到，如果属于群雄，则清空次数，恢复自然生产
	//10.生产时间到，如果不属于群雄，则次数变成20， 暂停生产

	// 正在处于战斗的玩家回城
	public void handleWarReturn(int cityId) {
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		if (worldCity == null) {
			LogHelper.CONFIG_LOGGER.info("worldCity is null, cityId = " + cityId);
			return;
		}

		int mapId = worldCity.getMapId();
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.info("mapInfo == null, mapId = " + mapId);
			return;
		}

		List<IWar> warList = mapInfo.getCountryCityWar(cityId);
		if (warList == null || warList.isEmpty()) {
			return;
		}
		for (IWar war : warList) {
			CountryCityWarInfo targetWar = (CountryCityWarInfo) war;
			ConcurrentLinkedDeque<March> attacker = targetWar.getAttackMarches();
			ConcurrentLinkedDeque<March> defencer = targetWar.getDefenceMarches();
			HashSet<Long> attackPlayers = new HashSet<Long>();
			for (March march : attacker) {
				worldManager.doMiddleReturn(march, Reason.STEAL_CITY);
				// 行军同步
				WorldPb.SynMarchRq synMarchRq = worldManager.createSynMarchRq(march);

				playerManager.getOnlinePlayer().forEach(e -> {
					playerManager.synMarchToPlayer(e, synMarchRq);
				});
				attackPlayers.add(march.getLordId());
			}

			// 移出战斗
			WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(targetWar);
			for (Long attackerPlayer : attackPlayers) {
				Player target = playerManager.getPlayer(attackerPlayer);
				if (target != null) {
					worldManager.synRemoveWar(target, synCityWarRq);
				}
			}

			HashSet<Long> defencerPlayers = new HashSet<Long>();
			for (March march : defencer) {
				worldManager.doMiddleReturn(march, Reason.STEAL_CITY);
				// 行军同步
				WorldPb.SynMarchRq synMarchRq = worldManager.createSynMarchRq(march);
				playerManager.getOnlinePlayer().forEach(e -> {
					playerManager.synMarchToPlayer(e, synMarchRq);
				});
				defencerPlayers.add(march.getLordId());
			}

			for (Long defencerPlayer : defencerPlayers) {
				Player target = playerManager.getPlayer(defencerPlayer);
				if (target != null) {
					worldManager.synRemoveWar(target, synCityWarRq);
				}
			}

			playerManager.getOnlinePlayer().forEach(e -> {
				worldManager.synRemoveWar(e, targetWar, cityId);
			});

			mapInfo.removeWar(targetWar);
			worldManager.flushWar(targetWar, false, 0);
		}
	}

	/**
	 * 检查抢夺名城是否开启
	 */
	public void checkStealCityOpen() {
		/**
		 * 获取世界数据
		 */
		WorldData worldData = worldManager.getWolrdInfo();
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_6);
		WorldActPlan worldActPlan = worldData.getWorldActPlans()
			.get(WorldActivityConsts.ACTIVITY_6);
		if (worldActPlan == null) {
			return;
		}
		WorldTargetTask worldTargetTask = worldData.getTasks()
			.get(staticWorldActPlan.getTargetId());
		if (worldTargetTask == null) {
			if (worldActPlan != null) {
				worldData.getWorldActPlans().remove(worldActPlan.getId());
			}
			return;
		}
		if (worldActPlan.getOpenTime() == 0) {
			return;
		}
		/**
		 * 1.活动结束,计算下一次开启时间
		 *
		 * 2.清除活动相关数据,正常生产
		 */
		long now = System.currentTimeMillis();
		long endTime = worldActPlan.getEndTime();
		if (now >= endTime) {
			logger.info("抢夺名城从开始到结束!");
			//TODO 计算下一轮抢夺名城的时间
			worldActPlanService.activityEnd(worldActPlan);
			//1.清理活动数据 2.恢复正常生产
			clean();
		}
		/**
		 * 这里主要未开启状态改成预热状态
		 */
		if (worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN) {
			if (worldActPlan.getPreheatTime() != 0 && now > worldActPlan.getPreheatTime()) {
				clean();
				worldActPlan.setState(WorldActPlanConsts.PREHEAT);
				worldActPlanService.syncWorldActivityPlan();
				logger.info("抢夺名城从初始状态到预热状态 预热开始时间{}",
					DateHelper.getDate(worldActPlan.getPreheatTime()));

				logger.info("******世界活动{}  开启时间{} ***********", staticWorldActPlan.getName(),
					DateHelper.getDate(worldActPlan.getOpenTime()));
				logger.info("******世界活动{}  预热时间{} ***********", staticWorldActPlan.getName(),
					DateHelper.getDate(worldActPlan.getPreheatTime()));
				if (worldActPlan.getEndTime() != 0) {
					logger.info("******世界活动{}  结束时间{} ***********", staticWorldActPlan.getName(),
						DateHelper.getDate(worldActPlan.getEndTime()));
				} else {
					logger.info("******世界活动{}  结束时间待定 ***********", staticWorldActPlan.getName());
				}

				logger
					.info("******世界活动{}  worldActPlan {} ***********", staticWorldActPlan.getName(),
						worldActPlan.toString());
			}
		}

		/**
		 * 活动开启时间小于当前时间,不做任何处理
		 */
		long startDate = worldActPlan.getOpenTime();
		if (now < startDate) {
			return;
		}

		/**
		 * 这里主要从预热状态改成开启状态
		 */
		if (now >= startDate) {
			if (worldActPlan.getState() == WorldActPlanConsts.PREHEAT) {
				if (worldActPlan.getOpenTime() != 0 && now > startDate) {
					clean();
					worldActPlan.setState(WorldActPlanConsts.OPEN);
					worldActPlanService.syncWorldActivityPlan();
					logger.info("抢夺名城从预热状态到开始状态 开始时间{}",
						DateHelper.getDate(worldActPlan.getOpenTime()));
					logger.info("******世界活动{}  开启时间{} ***********", staticWorldActPlan.getName(),
						DateHelper.getDate(worldActPlan.getOpenTime()));
					logger.info("******世界活动{}  预热时间{} ***********", staticWorldActPlan.getName(),
						DateHelper.getDate(worldActPlan.getPreheatTime()));
					if (worldActPlan.getEndTime() != 0) {
						logger
							.info("******世界活动{}  结束时间{} ***********", staticWorldActPlan.getName(),
								DateHelper.getDate(worldActPlan.getEndTime()));
					} else {
						logger
							.info("******世界活动{}  结束时间待定 ***********", staticWorldActPlan.getName());
					}
					logger.info("******世界活动{}  worldActPlan {} ***********",
						staticWorldActPlan.getName(), worldActPlan.toString());
				}
			}

			if (worldActPlan.getState() == WorldActPlanConsts.OPEN) {
				if (selectdMap.size() != 3) {
					for (int i = 1; i <= 3; i++) {
						TreeSet<Integer> treeSet = selectdMap.get(i);
						if (treeSet == null) {
							selectdMap.put(i, new TreeSet<Integer>());
						}
					}
				}
				checkState();
				StringBuilder selectCityDat = new StringBuilder();
				selectCityDat.append(JSON.toJSONString(selectdMap));
				selectCityDat.append(";");
				selectCityDat.append(JSON.toJSONString(receiveAward));
				worldData.setStealCity(selectCityDat.toString());
			}
		}
	}

	/**
	 * 活动结束清理相关数据
	 * <p>
	 * 恢复正常生产
	 */
	public void clean() {
		recoverCommonMake();
		synStealCity();
		Iterator<TreeSet<Integer>> iterator = selectdMap.values().iterator();
		while (iterator.hasNext()) {
			TreeSet<Integer> next = iterator.next();
			for (Integer cityId : next) {
				handleWarReturn(cityId);
			}
		}

		selectdMap.clear();
		receiveAward.clear();
		WorldData worldData = worldManager.getWolrdInfo();
		StringBuilder selectCityDat = new StringBuilder();
		selectCityDat.append(JSON.toJSONString(selectdMap));
		selectCityDat.append(";");
		selectCityDat.append(JSON.toJSONString(receiveAward));
		if (null != worldData) {
			worldData.setStealCity(selectCityDat.toString());
		}
		LogHelper.GAME_DEBUG.error("清空selectMap...");
	}

	/**
	 * 恢复正常生产
	 */
	public void recoverCommonMake() {
		long now = System.currentTimeMillis();
		for (TreeSet<Integer> cityIds : selectdMap.values()) {
			if (cityIds == null) {
				continue;
			}
			for (Integer id : cityIds) {
				City city = cityManager.getCity(id);
				if (city == null) {
					continue;
				}
				int state = city.getState();
				if (state != CityState.COMMON_MAKE_ITEM) {
					if (city.getCountry() == 0) {
						city.setAwardNum((byte) 0);  // 清零红色图纸
						LogHelper.GAME_DEBUG
							.error("城池Id=" + city.getCityId() + "没有任何国家占领，恢复正常状态,不生产任何图纸");
						StaticWorldCity worldCity = staticWorldMgr.getCity(city.getCityId());
						// 如果一个名城有红色图纸产出，则这个城池就不应该生产金色图纸
						// 分开成两个check函数编写
						if (worldCity != null) {
							city.setMakeItemTime(now);
							LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + ", 恢复正常生产...");
							city.setAwardNum((byte) 0);
						}
						city.setState(CityState.COMMON_MAKE_ITEM);
						continue;
					}

					if (city.getCountry() != 0) {
						city.setState(CityState.COMMON_MAKE_ITEM);
						long period = cityManager.getMakePeriod(city.getCityId());
						city.setMakeItemTime(now + period); // 恢复正常生产
						LogHelper.GAME_DEBUG.error("城池Id=" + city.getCityId() + ", 生产图纸1 ...");
						city.setAwardNum((byte) (0));
					}
				}
			}
		}

		//将名城数量超过7个的阵营的城池,给虫族
		for (int i = 1; i <= 3; i++) {
			boolean flag = cityManager.getFamousCityNum(i) > staticLimitMgr.getNum(110);
			if (flag) {
				List<Integer> famousCity = cityManager.getFamousCity();
				List<City> temp = new ArrayList<>();
				for (Integer famousCityId : famousCity) {
					City city = cityManager.getCity(famousCityId);
					if (city.getCountry() == i) {
						temp.add(city);
					}
				}
				//再根据时间 //再根据ID
				temp = temp.stream().sorted(Comparator.comparing(City::getBreakTime))
					.collect(Collectors.toList());
				logger.error(
					"阵营ID=" + i + ">>>>>>>>>>>>>>>>" + "数量=" + temp.size() + ">>>>>>>>>>>>>>>>"
						+ temp);

				for (int j = 0; j < temp.size(); j++) {
					if (j > staticLimitMgr.getNum(110) - 1) {
						logger.error("阵营ID=" + i + ">>>>>>>>>>>>>>>>" + "更改城池ID=" + j
							+ ">>>>>>>>>>>>>>>>给虫族");
						City city = temp.get(j);
						city.setCountry(0);        //更改主城给虫族
						city.setLordId(0);
						CityMonster cityMonster = cityManager.getCityMonster(city.getCityId());
						cityService.recoverCityMonster(cityMonster, now);
					}
				}
			}
		}
	}

	/**
	 * 异步推送抢夺名城的信息
	 */
	public void synStealCity() {
		WorldPb.SynStealCityRq.Builder builder = WorldPb.SynStealCityRq.newBuilder();
		for (TreeSet<Integer> cityIds : selectdMap.values()) {
			if (cityIds == null) {
				continue;
			}
			for (Integer id : cityIds) {
				City city = cityManager.getCity(id);
				if (city == null) {
					continue;
				}
				int state = city.getState();
				CommonPb.StealCityInfoItem.Builder item = CommonPb.StealCityInfoItem.newBuilder();
				item.setCityId(city.getCityId());
				item.setEndTime(city.getMakeItemTime());
				item.setProtectedTime(city.getProtectedTime());
				item.setAwardNum(city.getAwardNum());
				item.setState(state);
				item.setCountry(city.getCountry());
				item.setPeriod(TimeHelper.HOUR_MS);
				builder.addStealCityInfoItem(item);
			}
		}
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player target = iterator.next();
			if (target != null && target.isLogin && target.getChannelId() == -1) {
				SynHelper.synMsgToPlayer(target, WorldPb.SynStealCityRq.EXT_FIELD_NUMBER,
					WorldPb.SynStealCityRq.ext, builder.build());
			}
		}
	}

	/**
	 * 检查玩家是否领取了奖励
	 *
	 * @param player
	 * @param cityId
	 * @return
	 */
	public boolean isReceiveAward(Player player, int cityId) {
		boolean flag = false;
		LinkedList<Long> citys = receiveAward.get(cityId);
		if (citys != null) {
			if (citys.contains(player.getLord().getLordId())) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 检查玩家是否领取了奖励
	 *
	 * @param player
	 * @param cityId
	 * @return
	 */
	public void receiveAward(Player player, int cityId) {
		LinkedList<Long> citys = receiveAward.get(cityId);
		if (citys != null && null != player) {
			long lordId = player.getLord().getLordId();
			if (!citys.contains(lordId)) {
				citys.add(player.getLord().getLordId());
			}
		} else {
			citys = new LinkedList<>();
			if (player != null) {
				long lordId = player.getLord().getLordId();
				citys.add(lordId);
			}
		}
		receiveAward.put(cityId, citys);
	}

	public TreeSet<Integer> getStealCityByIndex(int index) {
		TreeSet<Integer> treeSet = selectdMap.get(index);
		return treeSet;
	}


	// 经过策划确认一次掉落一个物品
	public Award getStealCityAward(int cityId) {
		StaticActStealCity config = getConfig();
		if (null == config) {
			return null;
		}
		List<List<Integer>> awards = config.getAwards();
		if (awards == null || awards.size() == 0) {
			return null;
		}

		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			return null;
		}

		City city = cityManager.getCity(cityId);
		if (city == null) {
			return null;
		}

		// 把当前等级奖励筛选出来
		List<List<Integer>> lastRes = new ArrayList<List<Integer>>();
		for (List<Integer> elem : awards) {
			if (elem == null || elem.size() != 6) {
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

	/**
	 * 是否需要跑马灯消息
	 *
	 * @param award
	 * @return
	 */
	public boolean isChat(Award award) {
		boolean flag = false;
		try {
			StaticActStealCity config = getConfig();
			List<List<Integer>> awards = config.getAwards();
			int id = award.getId();

			for (List<Integer> list : awards) {
				Integer awardId = list.get(2);
				if (awardId == id) {
					Integer isChat = list.get(5);
					if (isChat == 1) {
						flag = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
}
