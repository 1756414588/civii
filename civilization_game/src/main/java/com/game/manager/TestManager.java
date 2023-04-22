package com.game.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.game.server.GameServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.AwardType;
import com.game.constant.CityType;
import com.game.constant.GameError;
import com.game.constant.ItemType;
import com.game.constant.Reason;
import com.game.constant.ResourceType;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticLordDataMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticTaskMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.Account;
import com.game.domain.Award;
import com.game.domain.p.Building;
import com.game.domain.p.City;
import com.game.domain.p.Command;
import com.game.domain.p.Equip;
import com.game.domain.p.Hero;
import com.game.domain.p.Item;
import com.game.domain.p.Lord;
import com.game.domain.p.LostRes;
import com.game.domain.p.Mail;
import com.game.domain.p.Resource;
import com.game.domain.p.Soldier;
import com.game.domain.p.Task;
import com.game.domain.p.Ware;
import com.game.domain.p.WorkQue;
import com.game.domain.p.WorkShop;
import com.game.domain.p.WorldTarget;
import com.game.domain.p.WorldTargetAward;
import com.game.domain.p.WsWorkQue;
import com.game.domain.s.StaticExchangeHero;
import com.game.domain.s.StaticLimit;
import com.game.domain.s.StaticLost;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticTask;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticWorldMonster;
import com.game.pb.ActivityPb;
import com.game.pb.BuildingPb;
import com.game.pb.RolePb;
import com.game.pb.SoldierPb;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.WorldBoss;
import com.game.worldmap.WorldLogic;

@Component
public class TestManager {
	@Autowired
	private StaticTaskMgr staticTaskMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private SoldierManager soldierManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private CityManager cityManager;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private StaticLordDataMgr lordDataMgr;

	@Autowired
	private StaticActivityMgr staticActivityMgr;

	@Autowired
	private EquipManager equipManager;

	@Autowired
    private WorldLogic worldLogic;

	@Autowired
    private StaticPropMgr staticPropMgr;

	public void testPlayer() {
        //testTaskState();
        Player player = playerManager.getPlayer(100663L);
        if (player != null) {
//            Map<Integer, StaticEquip> equipMap = staticEquipMgr.getEquipMap();
//            for (StaticEquip staticEquip : equipMap.values()) {
//                equipManager.addEquip(player, staticEquip.getEquipId(), Reason.GM_TOOL);
//            }
//
//            playerManager.addAward(player, AwardType.RESOURCE, ResourceType.IRON, 10000000, Reason.GM_TOOL);
//            playerManager.addAward(player, AwardType.RESOURCE, ResourceType.COPPER, 20000000, Reason.GM_TOOL);
//            playerManager.addAward(player, AwardType.RESOURCE, ResourceType.OIL, 20000000, Reason.GM_TOOL);
//            playerManager.addAward(player, AwardType.RESOURCE, ResourceType.STONE, 20000000, Reason.GM_TOOL);
//
//            Map<Integer, StaticBuilding> allBuilding = staticBuildingMgr.getBuildingMap();
//            List<Integer> openBuildingId = new ArrayList<Integer>();
//            for (StaticBuilding building : allBuilding.values()) {
//                openBuildingId.add(building.getBuildingId());
//            }
//
//            taskManager.synBuildings(player, openBuildingId);
//            player.getLord().setNewState(150);
//            Map<Integer, Task> taskMap = player.getTaskMap();
//            taskMap.clear();
//            taskManager.addTask(200, taskMap);
//            Building buildings = player.buildings;
//            for (Integer buildingId : openBuildingId) {
//                int buildingType = staticBuildingMgr.getBuildingType(buildingId);
//                StaticBuildingType staticBuildingType = staticBuildingMgr.getStaticBuildingType(buildingType);
//                buildings.setLevel(buildingType, buildingId, staticBuildingType.getMaxLv());
//            }
//            player.getLord().setLevel(50);
//            player.getLord().setVip(3);
//            player.getLord().setGold(2000);
//            Pos pos = player.getPos();
//            if (pos.isError()) {
//                worldManager.randerPlayerPos(player);
//            }
//            playerManager.checkStatus(player);
//            // 给玩家添加所有的英雄
//            Map<Integer, StaticHero> heroConfig = staticHeroMgr.getHeroMap();
//            for (StaticHero staticHero : heroConfig.values()) {
//                if (staticHero == null) {
//                    continue;
//                }
//
//                if (staticHero.getHeroId() >= 301 && staticHero.getHeroId() <= 315) {
//                    continue;
//                }
//
//                if (heroManager.hasHeroType(player, staticHero.getHeroId())) {
//                    continue;
//                }
//                heroManager.addHero(player, staticHero.getHeroId(), Reason.GM_TOOL);
//            }
//
//            player.setMaxMonsterLv(22);

            Map<Integer, StaticProp> staticPropMap = staticPropMgr.getPropMap();
            for (StaticProp staticProp : staticPropMap.values()) {
                if (staticProp.getPropType() == ItemType.EQUIP_PAPER) {
                    playerManager.addAward(player, AwardType.PROP, staticProp.getPropId(), 10, Reason.GM_TOOL);
                } else if (staticProp.getPropType() == ItemType.RAND_BOX) {
                    playerManager.addAward(player, AwardType.PROP, staticProp.getPropId(), 10, Reason.GM_TOOL);
                }
            }
            Map<Integer, Equip> equipMap = player.getEquips();
            equipMap.clear();
            player.getSimpleData().getSevenRecord().clear();
            player.getLord().setLevel(150);

        }
    }

	public void setMonsterLv(Player player) {
		Lord lord = player.getLord();
		lord.setMaxMonsterLv(22);
	}

	public void addHero(Player player) {
		heroManager.addHero(player, 1, Reason.GM_TOOL);
		heroManager.addHero(player, 5, Reason.GM_TOOL);
		List<Integer> embattleList = player.getEmbattleList();
		if (embattleList.size() >= 4) {
			embattleList.set(0, 1);
			embattleList.set(1, 5);
		}
	}

	public void removeProectd(Player player, long now) {
		// player.getLord().setProtectedTime(now);

	}

	public void makeFullResource(Player player) {
		Resource resource = player.getResource();
		Map<Integer, Long> res = resource.getResource();
		for (int i = 1; i <= 4; i++) {
			res.put(i, 1000000000L);
		}
	}

	public void testTask() {
		// Player player = playerManager.getPlayer("布朗宝儿");
		// if (player != null) {
		// for (Task task : player.getTaskMap().values()) {
		// int typeChild = taskManager.getTypeChild(task.getTaskId());
		// if (typeChild == TaskType.MAKE_KILL_EQUIP ||
		// typeChild == TaskType.LEVELUP_KILL_EQUIP ) {
		// taskManager.updateTask(task);
		// }
		//
		// if (task.getTaskId() == 126 || ok
		// task.getTaskId() == 127|| ok
		// task.getTaskId() == 131||
		// task.getTaskId() == 132 ||
		// task.getTaskId() == 135 ||
		// task.getTaskId() == 136 ||
		// task.getTaskId() == 140 ||
		// task.getTaskId() == 143 ||
		// task.getTaskId() == 144 ||
		// task.getTaskId() == 148 ||
		// task.getTaskId() == 152 ||
		// task.getTaskId() == 153 ||
		// task.getTaskId() == 160 ||
		// task.getTaskId() == 161 ||
		// task.getTaskId() == 162 ||
		// task.getTaskId() == 173 ||
		// task.getTaskId() == 174 ||
		// task.getTaskId() == 175) {
		// taskManager.updateTask(task, 5);
		// }
		// }
		// }
	}

	public void giveSth(Player player) {
		player.getLord().setLevel(120);
		for (Hero hero : player.getHeros().values()) {
			hero.setHeroLv(120);
		}
		Resource resource = player.getResource();
		Map<Integer, Long> res = resource.getResource();
		res.put(1, 100000000L);
		res.put(2, 100000000L);
		res.put(3, 100000000L);
	}

	// 测试掠夺
	public void testRebuildWare(Player player) {
		List<Award> awards = new ArrayList<Award>();
		Award award1 = new Award(AwardType.RESOURCE, ResourceType.IRON, 1000);
		Award award2 = new Award(AwardType.RESOURCE, ResourceType.COPPER, 1000);
		Award award3 = new Award(AwardType.RESOURCE, ResourceType.OIL, 1000);
		awards.add(award1);
		awards.add(award2);
		awards.add(award3);

		worldManager.handPlayerLost(player, awards);

		LostRes lostRes = player.getLostRes();
		List<Award> awardsTest = lostRes.getAward();
		playerManager.addAward(player, awardsTest, Reason.REBUILD_WARE);

		BuildingPb.GetWareAwardRs.Builder builder = BuildingPb.GetWareAwardRs.newBuilder();
		builder.setResource(player.wrapResourcePb());

	}

	public void testInfo(Player player) {
		player.getLord().setLevel(120);
	}

	public void testWare(Player player) {
		Lord lord = player.getLord();
		lord.setWareHighTimes(2);
		handleFlyTimes(player);

		RolePb.SynWareRq.Builder builder = RolePb.SynWareRq.newBuilder();
		// 同步可以重建的次数
		// 先发可以领取的高级重建
		int high = lord.getWareHighTimes();
		int flyTimes = lord.getFlyTimes();
		Ware ware = player.getWare();
		if (ware == null) {
			LogHelper.CONFIG_LOGGER.error("ware is null!");
			return;
		}

		// 计算高级的
		int highGetTimes = ware.getLv() - high;
		highGetTimes = Math.max(0, highGetTimes);
		highGetTimes = Math.min(flyTimes, highGetTimes);

		// 计算低级的
		int lowGetTimes = flyTimes - highGetTimes;
		lowGetTimes = Math.max(lowGetTimes, 0);
		int limitTimes = staticLimitMgr.getNum(63) - lord.getWareTimes();
		limitTimes = Math.max(0, limitTimes);
		lowGetTimes = Math.min(limitTimes, lowGetTimes);

		builder.setRebuildTimes(lowGetTimes);
		builder.setHighLvTimes(highGetTimes);

		LogHelper.CONFIG_LOGGER.error("info = " + builder.build());
	}

	public void handleFlyTimes(Player player) {
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("player is null");
			return;
		}

		Lord lord = player.getLord();
		int day = GameServer.getInstance().currentDay;
		if (day != lord.getFlyDay()) {
			lord.setFlyDay(day);
			lord.setFlyTimes(0);
		}

		// 被击飞次数加1
		lord.setFlyTimes(lord.getFlyTimes() + 1);

	}

	// 给玩家创建一个1级的科技，然后开启一个任务，然后再触发一个任务
	public void initTask(Player player) {
		Map<Integer, Task> taskMap = player.getTaskMap();
		taskMap.clear();
		List<Task> tasks = openTask(261);
		for (Task task : tasks) {
			taskMap.put(task.getTaskId(), task);
		}
	}

	public void initTech(Player player) {

	}

	public List<Task> openTask(int... taskId) {
		List<Task> tasks = new ArrayList<Task>();
		for (int i = 0; i < taskId.length; i++) {
			Task task = new Task();
			task.setTaskId(taskId[i]);
			task.setProcess(0);
			task.setStatus(0);
			StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
			List<List<Integer>> param = staticTask.getParam();
			if (param != null) {
				task.initCond(param.size());
			}
			task.doneCond();
			tasks.add(task);
		}

		return tasks;
	}

	public void testTask(Player player) {
		// 开启一个任务
		initTask(player);
		// 升级司令部到五级
		Command command = player.getCommand();
		command.getBase().setLevel(30);
		player.getTech().getBase().setLevel(5);
		// List<Integer> triggers = new ArrayList<Integer>();
		// triggers.add(command.getBuildingId());
		// triggers.add(command.getLv() + 1);
		// taskManager.doTask(TaskType.START_LEVELUP_BUILDING, player,
		// triggers);
	}

	public void getCity(Player player) {
//		int mapId = worldManager.getMapId(player);
//
//		WorldPb.GetCityRs.Builder builder = WorldPb.GetCityRs.newBuilder();
//		ConcurrentHashMap<Integer, City> cityMap = cityManager.getCityMap();
//		for (City cityInfo : cityMap.values()) {
//			if (cityInfo == null) {
//				continue;
//			}
//
//			int cityId = cityInfo.getCityId();
//			if (staticWorldMgr.getCityMapId(cityId) != mapId) {
//				continue;
//			}
//
//			CommonPb.MapCity.Builder mapCity = CommonPb.MapCity.newBuilder();
//			mapCity.setCityId(cityId);
//			mapCity.setCityLv(cityInfo.getCityLv());
//			mapCity.setCountry(cityInfo.getCountry());
//			mapCity.setSoldier(cityManager.getCitySoldier(cityId));
//			mapCity.setOwnId(cityInfo.getLordId());
//			long lordId = cityInfo.getLordId();
//			Player owner = playerManager.getPlayer(lordId);
//			if (owner != null) {
//				mapCity.setOwn(owner.getNick());
//			}
//			mapCity.setOwnEndTime(cityInfo.getEndTime());
//			StaticWorldCity config = staticWorldMgr.getCity(cityId);
//			if (config != null) {
//				if (cityInfo.getCountry() == 0) {
//					mapCity.setPeriod(0);
//				} else {
//					mapCity.setPeriod(config.getPeriod() * TimeHelper.MINUTE_MS);
//				}
//
//			}
//			mapCity.setEndTime(cityInfo.getMakeItemTime());
//			// 找到当前的国战信息
//			List<WarInfo> warInfos = worldManager.getCtWar(mapId, cityId);
//			for (WarInfo warInfo : warInfos) {
//				mapCity.addWarInfo(warInfo.wrapPb(warInfo.isJoin(player)));
//			}
//
//			Map<Long, CityElection> cityElections = cityManager.getCityElection(cityId);
//			List<CityElection> electionList = new ArrayList<CityElection>();
//			for (CityElection election : cityElections.values()) {
//				electionList.add(election);
//			}
//
//			Collections.sort(electionList);
//			for (CityElection cityElection : electionList) {
//				if (cityElection == null) {
//					continue;
//				}
//				long targetLordId = cityElection.getLordId();
//				Player election = playerManager.getPlayer(targetLordId);
//				if (election == null) {
//					continue;
//				}
//				CommonPb.CityElection.Builder data = CommonPb.CityElection.newBuilder();
//				data.setName(player.getNick());
//				data.setTitle(player.getTitle());
//				data.setEndTime(cityElection.getElectionTime());
//				mapCity.addCityElection(data);
//			}
//			mapCity.setElectionEndTime(cityInfo.getElectionEndTime());
//			builder.addCity(mapCity);
//		}
//
//		LogHelper.ERROR_LOGGER.error(builder.build().toString());

	}

	public static void main(String[] args) {
		// ApplicationContext ac = new
		// ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		// PlayerService service = (PlayerService) SpringUtil.getBean("playerService");
		// RolePb.NewStateRq.Builder req = RolePb.NewStateRq.newBuilder();
		// req.setNewStateId(6);
		// service.newStateRq(req.build(), null);
	}

	public void testRecoverWashHeroTimes(Player player) {
		long now = System.currentTimeMillis();
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
			LogHelper.CONFIG_LOGGER.error("washHeroInterval is 0.");
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

	// 清除世界目标和玩家相关信息
	public void clearWorldTarget() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}

			if (player.getCityId() != 0) {
				player.setCityId(0);
			}
			// 清除所有击杀的世界野怪的状态
			player.setWorldKillMonsterStatus(0);
			player.getLord().setKillMonsterNum(0);
			player.getLord().setKillWorldBossDay(0);
			// 清除所有玩家的世界目标
			Map<Integer, WorldTargetAward> worldTargetAwardMap = player.getWorldTargetAwardMap();
			worldTargetAwardMap.clear();
		}

		// 清除所有城池的信息以及世界boss的信息
		ConcurrentHashMap<Integer, City> cityMap = cityManager.getCityMap();
		for (City cityInfo : cityMap.values()) {
			if (cityInfo == null) {
				continue;
			}

			StaticWorldCity worldCity = staticWorldMgr.getCity(cityInfo.getCityId());
			if (worldCity.getType() == CityType.POINT) {
				cityInfo.setCityLv(1);
			}

			cityInfo.setCountry(0);
			cityInfo.setProtectedTime(0);
			if (cityInfo.getLordId() != 0) {
				Player player = playerManager.getPlayer(cityInfo.getLordId());
				if (player != null) {
					player.setCityId(0);
				}
			}
			cityInfo.setLordId(0);
			cityInfo.setEndTime(0L);
			cityInfo.setIsDestroyed(0);
		}

		WorldData worldData = worldManager.getWolrdInfo();
		worldData.setSeason(0);
		worldData.setEffect(0);
		Map<Integer, WorldBoss> bossMap = worldData.getBossMap();
		for (WorldBoss worldBoss : bossMap.values()) {
			worldBoss.setSoldier(worldBoss.getMaxSoldier());
			worldBoss.setKilled(false);
		}

		Map<Integer, WorldTarget> worldTargets = worldData.getWorldTargets();
		worldTargets.clear();

		WorldBoss shareBoss = worldData.getShareBoss();
		shareBoss.setSoldier(shareBoss.getMaxSoldier());
		shareBoss.setKilled(false);

	}

	public boolean isOpenTestMode() {
		int config = staticLimitMgr.getNum(161);
		return config == 1;
	}

	// 自动执行所有的任务
	//public void doAutoTestNewTask(Player player) {
    //    Map<Integer, Task> taskMap = player.getTaskMap();
    //    TreeSet<Integer> finishedTask = player.getFinishedTask();
    //    // 完成任务，开启任务,
    //    Integer lastTask = 0;
    //    while (finishedTask.size() < 483) {
    //        Set<Integer> allTask = new HashSet<Integer>();
    //        HashSet<Integer> record = new HashSet<Integer>();
    //        for (Task task : taskMap.values()) {
    //            if (task == null) {
    //                continue;
    //            }
    //            int taskId = task.getTaskId();
    //            finishedTask.add(taskId);
    //            StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
    //            if (staticTask != null && staticTask.getType() == 1) {
    //                if (staticTask.getTaskId() > lastTask) {
    //                    lastTask = staticTask.getTaskId();
    //                }
    //            }
    //            if (taskId == 483) {
    //                LogHelper.GAME_DEBUG.error("taskId = 483");
    //            }
    //            Set<Integer> openTask = taskManager.getMutiTriggerTask(taskId, player);
    //            if (!openTask.isEmpty()) {
    //                allTask.addAll(openTask);
    //            }
	//
    //            record.add(taskId);
    //        }
	//
    //        if (allTask.isEmpty()) {
    //            LogHelper.GAME_DEBUG.error("任务断掉了, task = " + record);
    //            LogHelper.GAME_DEBUG.error("任务断掉了, 主线进行到 = " + lastTask);
    //            break;
    //        }
	//
    //        Iterator<Task> iterator = taskMap.values().iterator();
    //        while (iterator.hasNext()) {
    //            Task task = iterator.next();
    //            if (task != null) {
    //                iterator.remove();
    //            }
    //        }
	//
    //        if (allTask != null) {
    //            for (Integer openId : allTask) {
    //                List<Task> tasks = openTask(openId);
    //                for (Task task : tasks) {
    //                    taskMap.put(task.getTaskId(), task);
    //                }
    //            }
    //        }
	//
    //    }
	//
    //    HashSet<Integer> nofinshed = new HashSet<Integer>();
    //    for (StaticTask task : staticTaskMgr.getTaskMap().values()) {
    //        if (task == null) {
    //            continue;
    //        }
    //        if (!finishedTask.contains(task.getTaskId())) {
    //            nofinshed.add(task.getTaskId());
    //        }
    //    }
    //    LogHelper.GAME_DEBUG.error("最终完成的任务数:" + finishedTask.size());
    //    LogHelper.GAME_DEBUG.error("最终完成的任务为:" + finishedTask);
    //    LogHelper.GAME_DEBUG.error("最终完成的未任务为:" + nofinshed);
    //    if (finishedTask.size() < 450) {
    //        LogHelper.GAME_DEBUG.error("最终可能开启任务有bugs!" );
	//
    //    }
	//
    //}

	public void canCelSoldier() {
		// 增加建造的队列不能取消
		Player player = playerManager.getPlayer(100814L);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("player is null in getSoldierRq.");
			return;
		}

		int soldierIndex = 1;
		if (!soldierManager.isSoldierTypeOk(soldierIndex)) {
			LogHelper.CONFIG_LOGGER.error(GameError.SOLDIER_TYPE_ERROR.toString());
			return;
		}

		Map<Integer, Soldier> soldiers = player.getSoldiers();
		Soldier soldier = soldiers.get(soldierIndex);
		if (soldier == null) {
			LogHelper.CONFIG_LOGGER.error(GameError.SOLDIER_TYPE_ERROR.toString());
			return;
		}

		// 建造队列
		LinkedList<WorkQue> workQues = soldier.getWorkQues();
		if (workQues.isEmpty()) {
			LogHelper.CONFIG_LOGGER.error(GameError.SOLDIER_WORKQUE_IS_EMPTY.toString());
			return;
		}

		// 检查keyId的合法性
		if (workQues.size() <= 1) {
			LogHelper.CONFIG_LOGGER.error(GameError.SOLDIER_WORKQUE_ONLY_ONE.toString());
			return;
		}

		int index = -1;
		int keyId = 7809;
		for (int i = 0; i < workQues.size(); i++) {
			WorkQue workQue = workQues.get(i);
			if (workQue != null && workQue.getKeyId() == keyId) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			LogHelper.CONFIG_LOGGER.error(GameError.SOLDIER_WORKQUE_NOT_EXISTS.toString());
			return;
		}

		// 正在生产的兵营不能取消
		if (index == 0) {
			LogHelper.CONFIG_LOGGER.error(GameError.SOLDIER_IS_TRAINING.toString());
			return;
		}

		// 扣除石油或者生铁
		WorkQue target = workQues.get(index);
		if (target != null) {
			playerManager.addAward(player, AwardType.RESOURCE, ResourceType.OIL, target.getOil(), Reason.CANCEL_SOLDIER);
			if (target.getIron() > 0) {
				playerManager.addAward(player, AwardType.RESOURCE, ResourceType.IRON, target.getIron(), Reason.CANCEL_SOLDIER);
			}
		}

		// 找到需要删除的等待队列，并将改变的workque发送到客户端
		workQues.remove(index);
		// 将index之后的向前面移动时间
		SoldierPb.CancelRecruitRs.Builder builder = SoldierPb.CancelRecruitRs.newBuilder();
		for (int i = index; i < workQues.size(); i++) {
			int preIndex = i - 1;
			if (preIndex < 0)
				continue;

			WorkQue preWorkQue = workQues.get(i - 1);
			if (preWorkQue == null)
				continue;

			WorkQue currentWorkQue = workQues.get(i);
			if (currentWorkQue == null)
				continue;

			currentWorkQue.setEndTime(preWorkQue.getEndTime() + currentWorkQue.getPeriod());

		}

		for (WorkQue workQue : workQues) {
			builder.addWorkQue(workQue.wrapPb());
		}


	}

	// 读取所有流失人的LordId, 然后打印相关人的任务状态
	public void printAllTask() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		Map<Integer, StaticLost> lostMap = lordDataMgr.getLostList();
		TreeMap<Integer, Integer> taskNum = new TreeMap<Integer, Integer>();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}

			long lordId = player.getLord().getLordId();
			StaticLost config = lostMap.get((int) lordId);
			if (config == null) {
				continue;
			}

			StringBuffer sb = new StringBuffer();
			sb.append("lordId =" + player.roleId + ", task =");
			Map<Integer, Task> taskMap = player.getTaskMap();
			for (Task task : taskMap.values()) {
				sb.append(task.getTaskId() + ",");
				Integer number = taskNum.get(task.getTaskId());
				if (number == null) {
					taskNum.put(task.getTaskId(), 1);
				} else {
					taskNum.put(task.getTaskId(), number + 1);
				}

			}
			sb.deleteCharAt(sb.length() - 1);
			// LogHelper.GAME_DEBUG.error(sb.toString());
		}

//		for (Map.Entry<Integer, Integer> entry : taskNum.entrySet()) {
//			LogHelper.GAME_DEBUG.error(entry.getKey() + "," + entry.getValue());
//		}

	}

	// 测试装备现世的活动

	public void exchangeHeroHandler() {
		Player player = new Player(new Lord(), 1);
		// 给玩家6个材料
        for (int itemId = 167; itemId <= 172; itemId++) {
            playerManager.addAward(player, AwardType.PROP, itemId, 1, 0);
        }
		StaticExchangeHero config = staticActivityMgr.getExchangeHero(200);
		if (config == null) {
			//System.out.println(GameError.NO_CONFIG);
			return;
		}

		// check config
		List<List<Integer>> items = config.getItems();
		if (items == null || items.size() < 1) {
			//System.out.println(GameError.CONFIG_ERROR);
			return;
		}

		// check item number ok
		for (List<Integer> item : items) {
			if (item == null) {
				//System.out.println(GameError.CONFIG_ERROR);
				return;
			}

			if (item.size() != 3) {
				//System.out.println(GameError.CONFIG_ERROR);
				return;
			}

			int itemId = item.get(1);
			int itemNum = item.get(2);
			Item itemHas = player.getItem(itemId);
			if (itemHas == null || itemHas.getItemNum() < itemNum) {
				//System.out.println(GameError.NOT_ENOUGH_ITEM);
				return;
			}
		}

		// add award
		List<List<Integer>> awardConfig = config.getAward();
		if (awardConfig == null || awardConfig.size() != 1) {
			//System.out.println(GameError.CONFIG_ERROR);
			return;
		}

		List<Integer> equipConfig = awardConfig.get(0);
		if (equipConfig == null || equipConfig.size() != 3) {
			//System.out.println(GameError.CONFIG_ERROR);
			return;
		}

		Award award = new Award(0, equipConfig.get(0), equipConfig.get(1), equipConfig.get(2));
		int freeSlot = equipManager.getFreeSlot(player);
		if (freeSlot <= 0) {
			//System.out.println(GameError.NOT_ENOUGH_EQUIP_SLOT);
			return;
		}

		int keyId = playerManager.addAward(player, award, Reason.ACT_EXCHANGE_HERO);
		ActivityPb.ExchangeHeroRs.Builder builder = ActivityPb.ExchangeHeroRs.newBuilder();
		award.setKeyId(keyId);
		Map<Integer, Equip> equipMap = player.getEquips();
		Equip equip = equipMap.get(keyId);
		if (equip != null) {
			builder.setEquip(equip.wrapPb());
		}
		for (List<Integer> item : items) {
			if (item == null) {
				//System.out.println(GameError.CONFIG_ERROR);
				return;
			}

			if (item.size() != 3) {
				//System.out.println(GameError.CONFIG_ERROR);
				return;
			}
			int type = item.get(0);
			int itemId = item.get(1);
			int itemNum = item.get(2);
			playerManager.subAward(player, type, itemId, itemNum, Reason.ACT_EXCHANGE_HERO);
			Item prop = player.getItem(itemId);
			if (prop != null) {
				builder.addProp(prop.wrapPb());
			} else {
				Item nullItem = new Item(itemId, 0);
				builder.addProp(nullItem.wrapPb());
			}
		}
		LogHelper.GAME_DEBUG.error("兑换成功, builder =" + builder.build());
	}


	public void monsterLoot() {
        LogHelper.GAME_DEBUG.error("开始攻打特战队:");
        Player player = new Player(new Lord(), 1);
        StaticWorldMonster worldMonster = staticWorldMgr.getMonster(1101);
        List<List<Integer>> dropList = worldMonster.getDropList();

        StaticExchangeHero config = staticActivityMgr.getExchangeHero(200);
        for (int man = 1; man <= 10000; man++) {
            LogHelper.GAME_DEBUG.error("----------------------------------");
            Map<Integer, Integer> takeItem = new TreeMap<Integer, Integer>();
            for (List<Integer> elem : dropList) {
                takeItem.put(elem.get(1), 0);
            }

            for (int i = 1; i <= 100; i++) {
                StringBuffer sb = new StringBuffer();
                sb.append("第" + i + "次攻打，获得碎片总数:");

                int totalCheck = 0;
                for (Integer num : takeItem.values()) {
                    if (num > 0) {
                        totalCheck++;
                    }
                }

                if (totalCheck >= 6) {
                    break;
                }

                if (totalCheck <= 3 || totalCheck >= 6) {
                    List<Award> awards = worldLogic.getMonsterAwards(player,worldMonster);
                    playerManager.addAward(player, awards.get(0), 0);
                    Integer num = takeItem.get(awards.get(0).getId());
                    if (num == null) {
                        takeItem.put(awards.get(0).getId(), 1);
                    } else {
                        takeItem.put(awards.get(0).getId(), num+1);
                    }

                } else if (totalCheck == 4) {
                    List<Integer> loot1 = config.getLoot2();
                    List<List<Integer>> makeLoot = new ArrayList<List<Integer>>();
                    makeLoot.addAll(dropList);
                    for (int index = 0; index < makeLoot.size(); index++) {
                        int itemId = makeLoot.get(index).get(1);
                        if (takeItem.get(itemId) > 0) {
                            makeLoot.get(index).set(3, loot1.get(0));
                        } else {
                            makeLoot.get(index).set(3, loot1.get(1));
                        }
                    }
//                    LogHelper.GAME_DEBUG.error("makeLoot = " + makeLoot);
                    List<Award> awards = worldLogic.getActAwards(worldMonster,makeLoot);
                    playerManager.addAward(player, awards.get(0), 0);
                    Integer num = takeItem.get(awards.get(0).getId());
                    if (num == null) {
                        takeItem.put(awards.get(0).getId(), 1);
                    } else {
                        takeItem.put(awards.get(0).getId(), num+1);
                    }

                } else if (totalCheck == 5) {
                    List<Integer> loot2 = config.getLoot2();
                    List<List<Integer>> makeLoot = new ArrayList<List<Integer>>();
                    makeLoot.addAll(dropList);
                    for (int index = 0; index < makeLoot.size(); index++) {
                        int itemId = makeLoot.get(index).get(1);
                        if (takeItem.get(itemId) > 0) {
                            makeLoot.get(index).set(3, loot2.get(0));
                        } else {
                            makeLoot.get(index).set(3, loot2.get(1));
                        }
                    }
//                    LogHelper.GAME_DEBUG.error("makeLoot = " + makeLoot);
                    List<Award> awards = worldLogic.getActAwards(worldMonster,makeLoot);
                    playerManager.addAward(player, awards.get(0), 0);
                    Integer num = takeItem.get(awards.get(0).getId());
                    if (num == null) {
                        takeItem.put(awards.get(0).getId(), 1);
                    } else {
                        takeItem.put(awards.get(0).getId(), num+1);
                    }
                }

                int total = 0;
                for (Integer num : takeItem.values()) {
                    if (num > 0) {
                        total++;
                    }

                    if (total >= 6) {
                        break;
                    }
                }

                sb.append(total + "\n");
                LogHelper.GAME_DEBUG.error(sb.toString());
                if (total >= 6)  {
                    break;
                }
            }
        }

    }


    public void testMail() {
	    Player player = new Player(new Lord(), 0);
	    LinkedList<Mail> mails = new LinkedList<Mail>();
	    for (int i = 1; i <= 5; i++) {
	        Mail mail = new Mail();
	        mail.setKeyId(player.maxKey());
	        mails.offerFirst(mail);
        }

        for (Mail mail : mails) {
	        //System.out.println("mail keyId:" + mail.getKeyId());
        }

    }

    public void addGold(Player player) {
	    playerManager.addAward(player, AwardType.GOLD, 0, 100000, Reason.GM_TOOL);
    }

    public void handleWorkShop(Player player) {
        //	    [GetWsQueRs.ext] {
        //            workQue {
        //                keyId: 1303
        //                index: 2
        //                period: 19890788
        //                endTime: 1502910564738
        //                award {
        //                    type: 5
        //                    id: 46
        //                    count: 2
        //                    keyId: 1302
        //                }
        //            }
        //            workQue {
        //                keyId: 1307
        //                index: 3
        //                period: 19900000
        //                endTime: 1502910573950
        //                award {
        //                    type: 5
        //                    id: 46
        //                    count: 1
        //                    keyId: 1306
        //                }
        //            }
        //            workQue {
        //                keyId: 1305
        //                index: 1
        //                period: 19890788
        //                endTime: 1502910564738
        //                award {
        //                    type: 5
        //                    id: 47
        //                    count: 1
        //                    keyId: 1304
        //                }
        //            }
//        Building buildings = player.buildings;
//        WorkShop workShop =buildings.getWorkShop();
//        Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
//        long now = System.currentTimeMillis();
//        for (WsWorkQue wsWorkQue : workQues.values()) {
//            wsWorkQue.setEndTime(now);
//        }
        // 1311 金色 1309 绿色
        Building buildings = player.buildings;
        WorkShop workShop =buildings.getWorkShop();
        Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
        long now = System.currentTimeMillis();
        for (WsWorkQue wsWorkQue : workQues.values()) {
            int propId = wsWorkQue.getAward().getId();
            StaticProp staticProp = staticPropMgr.getStaticProp(propId);
            if (staticProp.getColor() == 3) {
                wsWorkQue.setEndTime(now);
            } else if (staticProp.getColor() == 4) {
                wsWorkQue.setEndTime(now + TimeHelper.HOUR_MS);
                wsWorkQue.setPeriod(TimeHelper.MINUTE_MS * 5);

            }
        }

        for (int i = 52; i<=75; i++) {
            playerManager.addAward(player, AwardType.PROP, i, 1, Reason.GM_TOOL);
        }
    }

    public void checkTask() {
	    TreeSet<Integer> mainTask = new TreeSet<Integer>();
	    int lastMainTaskId = 0;
	    for (int i = 1; i <= 68; i++) {
	        mainTask.add(i);
        }

        if (!mainTask.isEmpty()) {
            if (!mainTask.contains(483)) {
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && mainTask.contains(69)) {
                mainTask.remove(483);
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && !mainTask.contains(69)) {
                lastMainTaskId = mainTask.last();
            }
        }
        LogHelper.GAME_DEBUG.error("最后一个主线任务为:" + lastMainTaskId);

        mainTask.clear();
        for (int i = 1; i <= 68; i++) {
            mainTask.add(i);
        }
        mainTask.add(483);


        if (!mainTask.isEmpty()) {
            if (!mainTask.contains(483)) {
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && mainTask.contains(69)) {
                mainTask.remove(483);
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && !mainTask.contains(69)) {
                lastMainTaskId = mainTask.last();
            }
        }
        LogHelper.GAME_DEBUG.error("最后一个主线任务为:" + lastMainTaskId);

        mainTask.clear();
        for (int i = 1; i <= 68; i++) {
            mainTask.add(i);
        }
        mainTask.add(483);
        mainTask.add(69);

        if (!mainTask.isEmpty()) {
            if (!mainTask.contains(483)) {
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && mainTask.contains(69)) {
                mainTask.remove(483);
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && !mainTask.contains(69)) {
                lastMainTaskId = mainTask.last();
            }
        }
        LogHelper.GAME_DEBUG.error("最后一个主线任务为:" + lastMainTaskId);


        mainTask.clear();
        for (int i = 1; i <= 68; i++) {
            mainTask.add(i);
        }
        mainTask.add(483);
        for (int  i = 69; i<=200; i++) {
            mainTask.add(i);
        }
        if (!mainTask.isEmpty()) {
            if (!mainTask.contains(483)) {
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && mainTask.contains(69)) {
                mainTask.remove(483);
                lastMainTaskId = mainTask.last();
            } else if (mainTask.contains(483) && !mainTask.contains(69)) {
                lastMainTaskId = mainTask.last();
            }
        }
        LogHelper.GAME_DEBUG.error("最后一个主线任务为:" + lastMainTaskId);
    }

    //public void testTaskBreak() {
	//    // 做到任务Id = 39的时候断档了
    //    Player player = new Player(new Lord(), 0);
    //    TreeSet<Integer> finishedTask = player.getFinishedTask();
    //    for (int i = 1; i <= 38; i++) {
    //        finishedTask.add(i);
    //    }
    //    finishedTask.add(39);
    //    // 玩家当前有38个任务, 实际第39个任务完成了, 没有记录到finishedTask, 然后39触发40的时候，没有触发成功
    //    Set<Integer> openTask = taskManager.getMutiTriggerTask(39, player);
    //    LogHelper.GAME_DEBUG.error("可以触发的任务为:" + openTask);
	//
    //}

    // 计算15-22号  任务taskID和流失人数的
    // 计算7-14号  任务taskID和流失人数的
    // u_account,p_lord,p_detail,p_activity
    public void testTaskState() {
	    // 先计算流失的人数, 注册时候，再也没有登录的人
        // 登录时间和创建时间在同一天
	    Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
        TreeMap<Integer, Integer> taskNum = new TreeMap<Integer, Integer>();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player == null) {
                continue;
            }

            Account account = player.account;
            if (account == null) {
                continue;
            }

            Date date1 = account.getCreateDate();
            Date date2 = account.getLoginDate();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date1);
            cal2.setTime(date2);
            int month = cal1.get(Calendar.MONTH) + 1;
            if (month != 8) {
                continue;
            }

            int day = cal1.get(Calendar.DAY_OF_MONTH);
            if (day < 7 ||
                day > 14) {
                continue;
            }
            //System.out.println("month = " + month + ", day = " + day);
            Map<Integer, Task> taskMap = player.getTaskMap();
            for (Task task : taskMap.values()) {
                if (task == null) {
                    continue;
                }

                Integer num = taskNum.get(task.getTaskId());
                if (num == null) {
                    taskNum.put(task.getTaskId(), 1);
                } else {
                    taskNum.put(task.getTaskId(), num + 1);
                }
            }

            LogHelper.GAME_DEBUG.error(player.roleId.toString());
        }

        //LogHelper.GAME_DEBUG.error("totalNum = " + totalNum);
        //        for (Map.Entry<Integer, Integer> entry : taskNum.entrySet()) {
        //            if (entry == null) {
        //                continue;
        //            }
        //            //LogHelper.GAME_DEBUG.error(entry.getKey() + "," + entry.getValue());
        //            LogHelper.GAME_DEBUG.error(entry.getValue());
        //
        //        }



    }

}
