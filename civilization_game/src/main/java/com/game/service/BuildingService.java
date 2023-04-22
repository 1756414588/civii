package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.ActivityConst;
import com.game.constant.AwardType;
import com.game.constant.BeautySkillTypeNew;
import com.game.constant.BuildingId;
import com.game.constant.BuildingType;
import com.game.constant.ConditionType;
import com.game.constant.DailyTaskId;
import com.game.constant.DevideFactor;
import com.game.constant.GameError;
import com.game.constant.ItemType;
import com.game.constant.NineCellConst;
import com.game.constant.OpenConsts;
import com.game.constant.Quality;
import com.game.constant.Reason;
import com.game.constant.ResourceType;
import com.game.constant.SoldierIndex;
import com.game.constant.SoldierType;
import com.game.constant.TaskType;
import com.game.constant.TechEffectId;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticOpenManger;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticSoldierMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.dataMgr.StaticWallMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BuildQue;
import com.game.domain.p.Building;
import com.game.domain.p.BuildingBase;
import com.game.domain.p.Camp;
import com.game.domain.p.Command;
import com.game.domain.p.EmployInfo;
import com.game.domain.p.Employee;
import com.game.domain.p.Item;
import com.game.domain.p.Lord;
import com.game.domain.p.LostRes;
import com.game.domain.p.ResBuildings;
import com.game.domain.p.Resource;
import com.game.domain.p.SimpleData;
import com.game.domain.p.Soldier;
import com.game.domain.p.Staff;
import com.game.domain.p.Tech;
import com.game.domain.p.TechInfo;
import com.game.domain.p.TechQue;
import com.game.domain.p.Wall;
import com.game.domain.p.WallDefender;
import com.game.domain.p.Ware;
import com.game.domain.p.WorkQue;
import com.game.domain.p.WorkShop;
import com.game.domain.s.StaticBuilding;
import com.game.domain.s.StaticBuildingLv;
import com.game.domain.s.StaticEmployee;
import com.game.domain.s.StaticLimit;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticPropBuilding;
import com.game.domain.s.StaticSoldierLv;
import com.game.domain.s.StaticVip;
import com.game.domain.s.StaticWallMonsterLv;
import com.game.log.LogUser;
import com.game.log.constant.CopperOperateType;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.OilOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.constant.StoneOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.ActivityManager;
import com.game.manager.BeautyManager;
import com.game.manager.BroodWarManager;
import com.game.manager.BuildingManager;
import com.game.manager.CondMgr;
import com.game.manager.DailyTaskManager;
import com.game.manager.PlayerManager;
import com.game.manager.SoldierManager;
import com.game.manager.TaskManager;
import com.game.manager.TechManager;
import com.game.manager.WallManager;
import com.game.manager.WorkShopMgr;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.BuyRebMilitiaQueueHandler;
import com.game.message.handler.cs.BuyRebuildQueueHandler;
import com.game.message.handler.cs.BuyUpBuildQueHandler;
import com.game.message.handler.cs.RecoverBuildHandler;
import com.game.pb.BasePb;
import com.game.pb.BuildingPb;
import com.game.pb.BuildingPb.BuyBuildQueCdRq;
import com.game.pb.BuildingPb.BuyBuildQueCdRs;
import com.game.pb.BuildingPb.BuyBuildTeamRq;
import com.game.pb.BuildingPb.BuyBuildTeamRs;
import com.game.pb.BuildingPb.BuyRebMilitiaQueueRq;
import com.game.pb.BuildingPb.BuyRebMilitiaQueueRs;
import com.game.pb.BuildingPb.BuyRebuildQueueRs;
import com.game.pb.BuildingPb.DoAllResourceRs;
import com.game.pb.BuildingPb.DoResourceRs;
import com.game.pb.BuildingPb.GetBuildingRq;
import com.game.pb.BuildingPb.GetBuildingRs;
import com.game.pb.BuildingPb.GetWareAwardRs;
import com.game.pb.BuildingPb.GetWareRs;
import com.game.pb.BuildingPb.HireOfficerRq;
import com.game.pb.BuildingPb.HireOfficerRs;
import com.game.pb.BuildingPb.OpenBuildingRq;
import com.game.pb.BuildingPb.OpenBuildingRs;
import com.game.pb.BuildingPb.OpenMilitiaRs;
import com.game.pb.BuildingPb.RecoverBuildRq;
import com.game.pb.BuildingPb.RecoverBuildRs;
import com.game.pb.BuildingPb.SynBuildingUpRq;
import com.game.pb.BuildingPb.UpBuildingRq;
import com.game.pb.BuildingPb.UpBuildingRs;
import com.game.pb.CommonPb;
import com.game.season.SeasonManager;
import com.game.season.talent.entity.EffectType;
import com.game.server.GameServer;
import com.game.util.ArrayHelper;
import com.game.util.GameHelper;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.RandomHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildingService {

	private static Logger logger = LoggerFactory.getLogger(BuildingService.class);
	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private BuildingManager buildingManager;

	@Autowired
	private CondMgr condDataMgr;

	@Autowired
	private StaticPropMgr staticPropDataMgr;

	@Autowired
	private TechManager techManager;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private StaticWallMgr staticWallMgr;

	@Autowired
	private WallManager wallMgr;

	@Autowired
	private SoldierManager soldierMgr;

	@Autowired
	private WorkShopMgr workShopMgr;

	@Autowired
	private StaticSoldierMgr staticSoldierMgr;

	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private StaticOpenManger staticOpenManger;
	@Autowired
	private BeautyManager beautyManager;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private BroodWarManager broodWarManager;

	// 获取建筑信息
	public void getBuildingRq(GetBuildingRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Building buildings = player.buildings;
		GetBuildingRs.Builder builder = GetBuildingRs.newBuilder();
		Command command = buildings.getCommand();
		CommonPb.CommandInfo.Builder commandInfo = command.wrapPb();
		commandInfo.setOfficerId(player.getOfficerId());
		commandInfo.setOfficerTime(player.getOfficerTime());

		// 建筑等级
		buildingManager.wrapBuildings(builder, buildings);
		// 升级队列
		buildingManager.wrapBuildQue(builder, buildings);

		// 征收
		Lord lord = player.getLord();
		int collectInterval = staticLimitMgr.getCollectInterval();
		builder.setCollectTimeLeft(lord.getCollectEndTime() + collectInterval);
		builder.setCollectTimes(lord.getCollectTimes());

		// 内政官
		EmployInfo employInfo = player.getEmployInfo();
		builder.setOfficerId(employInfo.getOfficerId());
		builder.setOfficerTime(employInfo.getOfficerTime());

		// 建造队时间
		builder.setBuildTeamTime(lord.getBuildTeamTime());

		// 兵营信息
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		for (Map.Entry<Integer, Soldier> soldierElem : soldierMap.entrySet()) {
			Soldier soldier = soldierElem.getValue();
			if (soldier == null) {
				continue;
			}
			builder.addSoldierInfo(soldier.wrapPb());

			// 兵营的建造队列
			soldier.checkWorkQues();
			LinkedList<WorkQue> workQues = soldier.getWorkQues();
			for (WorkQue workQue : workQues) {
				builder.addWorkQue(workQue.wrapPb());
			}
		}

		// 打造
		Building building = player.buildings;
		LinkedList<WorkQue> workQueList = building.getEquipWorkQue();
		if (!workQueList.isEmpty()) {//
			WorkQue workQue = workQueList.peekFirst();
			if (workQue != null) {
				builder.addWorkQue(workQue.wrapPb());
			}
		}

		// 铁匠官
		builder.setBlackSmithId(employInfo.getBlackSmithId());
		builder.setBlackSmithTime(employInfo.getBlackSmithTime());

		// 序列化科技等级
		Tech tech = buildings.getTech();
		if (tech != null) {
			Map<Integer, TechInfo> techLevelInfoMap = tech.getTechInfoMap();
			int techBuilingLv = tech.getLv();
			for (Map.Entry<Integer, TechInfo> elem : techLevelInfoMap.entrySet()) {
				if (elem == null) {
					continue;
				}

				TechInfo techInfo = elem.getValue();
				if (techInfo == null) {
					continue;
				}

				if (techInfo.getLevel() <= techBuilingLv) {
					builder.addTechInfo(techManager.wrapTechPb(techInfo));
				}
			}

			// 序列化科技进度
			LinkedList<TechQue> techQues = tech.getTechQues();
			for (TechQue techQue : techQues) {
				builder.setTechQue(techManager.wrapTechQuePb(player, techQue));
				break;
			}
		}

		// 研究员
		builder.setResearcherId(employInfo.getResearcherId());
		builder.setResearcherTime(employInfo.getResearcherTime());

		// 雇佣工免费次数状态
		Map<Integer, Employee> employeeMap = employInfo.getEmployeeMap();
		for (Map.Entry<Integer, Employee> elem : employeeMap.entrySet()) {
			if (elem == null) {
				continue;
			}

			Employee employee = elem.getValue();
			if (employee == null) {
				continue;
			}

			CommonPb.EmployeeUseTimes.Builder employFreeTimes = CommonPb.EmployeeUseTimes.newBuilder();
			employFreeTimes.setEmployeeId(employee.getEmployeeId());
			employFreeTimes.setUseTimes(employee.getUseTimes());
			builder.addEmployeeUseTimes(employFreeTimes);
		}

		builder.addAllRecoverData(buildings.getRecoverBuilds());

		handler.sendMsgToPlayer(GetBuildingRs.ext, builder.build());
	}

	// 征收
	public void doRescoureRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查次数够不够
		Lord lord = player.getLord();
		if (lord.getCollectTimes() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.COLLECT_RESOURCE_NO_TIMES);
			return;
		}

		// 检查建筑是否存在
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_EXISTS);
			return;
		}

		// 每种资源都判断一下
		Resource resource = player.getResource();
		if (resource == null) {
			LogHelper.CONFIG_LOGGER.info("resource is null!");
			handler.sendErrorMsgToPlayer(GameError.RESOURCE_NULL);
			return;
		}

		// 减少收集次数
		lord.setCollectTimes(lord.getCollectTimes() - 1);
		doGetResourceTask(player);
		DoResourceRs.Builder builder = DoResourceRs.newBuilder();
		builder.setCollects(lord.getCollectTimes());
		// 结束时间
		int collectInterval = staticLimitMgr.getCollectInterval();
		builder.setCollectTime(lord.getCollectEndTime() + collectInterval);
		// 征收信息
		CommonPb.Resource.Builder resAdd = buildingManager.getAllResAdd(player);
		playerManager.addResource(player, ResourceType.IRON, resAdd.getIron(), Reason.COLLECT_RESOURCE);
		playerManager.addResource(player, ResourceType.COPPER, resAdd.getCopper(), Reason.COLLECT_RESOURCE);
		playerManager.addResource(player, ResourceType.OIL, resAdd.getOil(), Reason.COLLECT_RESOURCE);
		playerManager.addResource(player, ResourceType.STONE, resAdd.getStone(), Reason.COLLECT_RESOURCE);
		activityManager.updActPerson(player, ActivityConst.ACT_SQUA, 1, NineCellConst.CELL_8);
		/**
		 * 建筑资源征收日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.IRON), RoleResourceLog.OPERATE_IN, ResourceType.IRON, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAdd.getIron(), player.account.getChannel()));
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.COPPER), RoleResourceLog.OPERATE_IN, ResourceType.COPPER, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAdd.getCopper(), player.account.getChannel()));
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.OIL), RoleResourceLog.OPERATE_IN, ResourceType.OIL, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAdd.getOil(), player.account.getChannel()));
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.IRON), RoleResourceLog.OPERATE_IN, ResourceType.IRON, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAdd.getIron(), player.account.getChannel()));
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.STONE), RoleResourceLog.OPERATE_IN, ResourceType.STONE, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAdd.getStone(), player.account.getChannel()));

		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAdd.getIron(), IronOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.IRON);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAdd.getCopper(), CopperOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.COPPER);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAdd.getOil(), OilOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.OIL);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAdd.getStone(), StoneOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.STONE);

		builder.setResource(resAdd);
		handler.sendMsgToPlayer(DoResourceRs.ext, builder.build());

		dailyTaskManager.record(DailyTaskId.IMPOSE, player, 1);
		ActivityEventManager.getInst().activityTip(EventEnum.LEVY_RESOURCE, player, 1);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.LEVY_RESOURCE, 1);
	}

	// 一键征收所有资源
	public void doAllRescoureRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查次数够不够
		Lord lord = player.getLord();
		if (lord.getCollectTimes() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.COLLECT_RESOURCE_NO_TIMES);
			return;
		}

		// 检查建筑是否存在
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_EXISTS);
			return;
		}

		// 每种资源都判断一下
		Resource resource = player.getResource();
		if (resource == null) {
			LogHelper.CONFIG_LOGGER.info("resource is null!");
			handler.sendErrorMsgToPlayer(GameError.RESOURCE_NULL);
			return;
		}

		DoAllResourceRs.Builder builder = DoAllResourceRs.newBuilder();
		/* List<CommonPb.Resource.Builder> resAddList= new ArrayList<>(); */

		// 减少收集次数
		int collectTimes = lord.getCollectTimes();
		long coper, iron, oil, stone = 0;
		doGetResourceTask(player);
		CommonPb.Resource.Builder resAdd = buildingManager.getAllResAdd(player);
		coper = resAdd.getCopper() * collectTimes;
		iron = resAdd.getIron() * collectTimes;
		oil = resAdd.getOil() * collectTimes;
		stone = resAdd.getStone() * collectTimes;
		playerManager.addResource(player, ResourceType.IRON, iron, Reason.COLLECT_RESOURCE);
		playerManager.addResource(player, ResourceType.COPPER, coper, Reason.COLLECT_RESOURCE);
		playerManager.addResource(player, ResourceType.OIL, oil, Reason.COLLECT_RESOURCE);
		playerManager.addResource(player, ResourceType.STONE, stone, Reason.COLLECT_RESOURCE);
		activityManager.updActPerson(player, ActivityConst.ACT_SQUA, collectTimes, NineCellConst.CELL_8);
		CommonPb.Resource.Builder resAddAll = CommonPb.Resource.newBuilder();
		resAddAll.setCopper(coper);
		resAddAll.setIron(iron);
		resAddAll.setOil(oil);
		resAddAll.setStone(stone);

		// 结束时间
		int collectInterval = staticLimitMgr.getCollectInterval();
		lord.setCollectTimes(0);
		builder.setCollects(0);
		builder.setCollectTime(lord.getCollectEndTime() + collectInterval);

		/**
		 * 建筑资源征收日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.IRON), RoleResourceLog.OPERATE_IN, ResourceType.IRON, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAddAll.getIron(), player.account.getChannel()));
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.COPPER), RoleResourceLog.OPERATE_IN, ResourceType.COPPER, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAddAll.getCopper(), player.account.getChannel()));
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.OIL), RoleResourceLog.OPERATE_IN, ResourceType.OIL, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAddAll.getOil(), player.account.getChannel()));
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.STONE), RoleResourceLog.OPERATE_IN, ResourceType.STONE, ResOperateType.RES_BUILDING_COLLECT_IN.getInfoType(), (int) resAddAll.getStone(), player.account.getChannel()));

		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAddAll.getIron(), IronOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.IRON);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAddAll.getCopper(), CopperOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.COPPER);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAddAll.getOil(), OilOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.OIL);
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resAddAll.getStone(), StoneOperateType.RES_BUILDING_COLLECT_IN.getInfoType()), ResourceType.STONE);

		builder.setResource(resAddAll);
		handler.sendMsgToPlayer(DoAllResourceRs.ext, builder.build());

		dailyTaskManager.record(DailyTaskId.IMPOSE, player, collectTimes);
		// 更新通行证活动进度
		ActivityEventManager.getInst().activityTip(EventEnum.LEVY_RESOURCE, player, collectTimes);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.LEVY_RESOURCE, collectTimes);
	}

	public void doHireEmployee(HireOfficerRq req, ClientHandler handler) {
		// 检测是否有免费次数, 先检查玩家身上有没有这个内政官
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int employeeId = req.getEmployeeId();
		StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(employeeId);
		if (staticEmployee == null) {
			handler.sendErrorMsgToPlayer(GameError.EMPLOYEE_CONFIG_ERROR);
			return;
		}

		// 检查employeeId的合法性
		int commandLv = staticEmployee.getCommandLv();
		int playerCommandLv = player.getCommandLv();
		// 内政官要求的司令部等级不足
		if (playerCommandLv < commandLv) {
			handler.sendErrorMsgToPlayer(GameError.COMMAND_LEVEL_LOW_EMPLOYEE);
			return;
		}

		EmployInfo employInfo = player.getEmployInfo();
		Map<Integer, Employee> employeeMap = employInfo.getEmployeeMap();
		Employee employee = employeeMap.get(req.getEmployeeId());

		// 说明没有招募过这个内政官
		if (employee == null) {
			employee = buildingManager.createEmployee(employeeId);
			employeeMap.put(employeeId, employee);
		}

		// 区分免费和非免费情形，简化逻辑
		int freeTimes = staticEmployee.getFreeBuyTimes();
		if (employee.getUseTimes() < freeTimes) {
			// 扣除免费次数
			employee.setUseTimes(employee.getUseTimes() + 1);
		} else {
			boolean isOk = hireOfficer(handler, employeeId, employInfo, player);
			if (!isOk) {
				return;
			}
		}

		// 更新当前英雄Id
		employee.setEndTime(staticEmployee.getDurationTime() * TimeHelper.SECOND_MS + System.currentTimeMillis());
		employInfo.setOfficerId(employeeId); // 设置内政官Id

		if (staticEmployee.getQuality() >= Quality.PURPLE.get()) {
			activityManager.updActSeven(player, ActivityConst.TYPE_ADD, 4, 0, 1);
		}

		doHireOfficerTask(player, staticEmployee.getLevel());
		// 同步消息到客户端
		HireOfficerRs.Builder builder = HireOfficerRs.newBuilder();
		builder.setEmployeeId(employee.getEmployeeId());
		builder.setPeriod(staticEmployee.getDurationTime() * TimeHelper.SECOND_MS);
		builder.setEndTime(employee.getEndTime());
		builder.setUseTimes(employee.getUseTimes());
		builder.setIron(player.getIron());
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(HireOfficerRs.ext, builder.build());

		eventManager.hireOfficer(player, Lists.newArrayList(employee.getUseTimes() < freeTimes, staticEmployee.getLevel(), staticEmployee.getName(), staticEmployee.getEmployId()));
	}

	// 1.当前是金币，招募的是金币，时间还没到，则不能招募
	// 2.当前是金币，招募的是铁币，时间还没到，则不能招募
	// 3.当前是铁币，招募的是铁币，时间还没到，则不能招募
	// 可以招募
	// 4.当前是金币，招募的是金币，时间到了，则能招募, 扣除目标货币类型
	// 5.当前是金币，招募的是铁币，时间到了，则能招募, 扣除目标货币类型
	// 6.当前是铁币，招募的是金币，时间还没到，则能招募, 扣除目标货币类型
	// 7.当前是铁币，招募的是金币，时间到，则能招募, 扣除目标货币类型
	// 8.当前是铁币，招募的是铁币，时间到，则能招募, 扣除目标货币类型
	// 9.当前没有招募英雄，则能招募，扣除目标货币类型
	// 总结：
	// 能招募，扣除目标货币类型，更新当前英雄Id
	public boolean hireOfficer(ClientHandler handler, int employeeId, EmployInfo employInfo, Player player) {
		// 当前有生铁内政, 说明当前内政是生铁类型
		int currentEmId = employInfo.getOfficerId();
		StaticEmployee currentEmployee = staticBuildingMgr.getEmployee(currentEmId);
		StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(employeeId);
		boolean isEmployeeIron = staticEmployee.getCostIron() > 0;
		boolean isEmployeeGold = staticEmployee.getCostGold() > 0;
		boolean isCurIron = false;
		boolean isCurGold = false;
		if (currentEmployee != null) {
			isCurIron = currentEmployee.getCostIron() > 0;
			isCurGold = currentEmployee.getCostGold() > 0;
		}

		Map<Integer, Employee> employeeMap = employInfo.getEmployeeMap();
		Employee employee = employeeMap.get(currentEmId);
		int curLevel;
		int targetLevel = staticEmployee.getLevel();
		if (currentEmployee == null) {
			curLevel = 0;
		} else {
			curLevel = currentEmployee.getLevel();
		}

		if (curLevel >= targetLevel && employee != null) {
			long timeLeft = employee.getEndTime() - System.currentTimeMillis();
			// 如果招募的是金币，且又继续招募金币内政官
			if (isCurIron && isEmployeeIron && timeLeft > 0) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_SAME_TYPE_OFFICER);
				return false;
			}

			// 如果招募的是钻石，又招募金币，不允许
			if (isCurGold && isEmployeeIron && timeLeft > 0) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_SAME_TYPE_OFFICER);
				return false;
			}

			// 如果招募的是钻石，又招募钻石, 不允许叠加
			if (isCurGold && isEmployeeGold && timeLeft > 0) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_SAME_TYPE_OFFICER);
				return false;
			}
		}

		// 如果有更高等级的内政官,一样可以招募
		// 检测当前招募的英雄类型
		if (isEmployeeIron) {
			int need = staticEmployee.getCostIron();
			if (player.getIron() < need) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_OFFICER_NOT_ENOUGH_IRON);
				return false;
			}
			playerManager.subAward(player, AwardType.RESOURCE, ResourceType.IRON, need, Reason.HIRE_OFFICER);

		} else if (isEmployeeGold) {
			int gold = staticEmployee.getCostGold();
			if (player.getGold() < gold) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_OFFICER_NOT_ENOUGH_GOLD);
				return false;
			}
			playerManager.subAward(player, AwardType.GOLD, 1, gold, Reason.HIRE_OFFICER);

		}

		return true;

	}

	// 升级建筑, 根据不同类型进行升级，注意科技馆、兵营、作坊
	public void upBuilding(UpBuildingRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int buildingId = req.getBuildingId();
		// 根据建筑Id, find 建筑Type
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_BUILDING_TYPE_CONFIG);
			return;
		}

		Building buildings = player.buildings;
		if (buildings == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_EXISTS);
			return;
		}

		// 当前建筑等级
		int currentLv = buildings.getBuildingLv(buildingType, buildingId);

		// 当前建筑的最高等级
		int maxBuildingLv = staticBuildingMgr.maxBuildLv(buildingType);
		if (maxBuildingLv == Integer.MIN_VALUE) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_MAX_BUILDING_LEVEL);
			return;
		}

		// 是否达到最高等级
		if (currentLv >= maxBuildingLv) {
			handler.sendErrorMsgToPlayer(GameError.REACH_MAX_BUILDING_LEVEL);
			return;
		}

		// 检查建筑是否正在升级
		if (buildings.isBuildUping(buildingId)) {
			// 如果升级的是科技馆, 查看是否有研究正在进行
			if (buildingType == BuildingType.TECH) {
				// 如果有科技升级，则不能升级当前建筑
				boolean hasTecher = techManager.hasTecher(player);
				if (!hasTecher) {
					// 科技正在升级
					boolean isTechResearching = techManager.isTechResearching(player);
					if (isTechResearching) {
						handler.sendErrorMsgToPlayer(GameError.TECH_IS_RESEARCHING);
						return;
					}
				}
			} else {
				handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_UPING);
				return;
			}
		}

		// 检查是否有空闲的建造队
		if (player.getFreeTeam() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_FREE_BUILD_TEAM);
			return;
		}

		// 如果是兵营,如果有募兵, 则不能升级建筑
		if (buildings.isCamp(buildingType)) {
			int soldierType = soldierMgr.getSoldierType(buildingId);
			if (player.isSoldierTraining(soldierType)) {
				handler.sendErrorMsgToPlayer(GameError.SOLDIER_IS_TRAINING);
				return;
			}
		}

		// 如果是作坊，查看是否有正在生产的道具
		if (buildingType == BuildingType.WORK_SHOP) {
			// 生产的时候是否能升级建筑
			if (!workShopMgr.workShopCanUp(player)) {
				handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_CAN_NOT_UP);
				return;
			}
		}
//============================================
		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, currentLv + 1);
		if (staticBuildingLv == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_CONFIG_LV_ERROR);
			return;
		}

		List<List<Long>> upCond = staticBuildingLv.getUpCond();
		// 检查升级条件
		for (List<Long> item : upCond) {
			if (item.size() <= 1) {
				continue;
			}
			int condType = item.get(0).intValue();
			List<Long> param = ArrayHelper.copyArray(item);
			GameError gameError = condDataMgr.onCondition(player, condType, param);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		// 检查资源条件
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				handler.sendErrorMsgToPlayer(GameError.AWARD_TYPE_ERROR);
				return;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.LEVEL_UP_BUILDING);

			/**
			 * 建筑升级资源消耗日志埋点
			 */
			LogUser logUser = SpringUtil.getBean(LogUser.class);
			if (awardType == AwardType.RESOURCE) {
				logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(resType), RoleResourceLog.OPERATE_OUT, resType, ResOperateType.BUILDING_UP_OUT.getInfoType(), res, player.account.getChannel()));
				int t = 0;
				switch (resType) {
					case ResourceType.IRON:
						t = IronOperateType.BUILDING_UP_OUT.getInfoType();
						break;
					case ResourceType.COPPER:
						t = CopperOperateType.BUILDING_UP_OUT.getInfoType();
						break;
					default:
						break;
				}
				if (t != 0) {
					logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, res, t), resType);
				}
			}

		}

		// 检查当前建造队列类型
		ConcurrentLinkedDeque<BuildQue> buildQues = buildings.getBuildQues();

		// 开始升级建筑
		doStartLevelUp(player, buildingId, currentLv + 1);
		StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
		SpringUtil.getBean(EventManager.class).build_level_up(player, buildingId, staticBuilding.getName(), resourceCond, currentLv + 1);

		UpBuildingRs.Builder builder = UpBuildingRs.newBuilder();
		// 开始倒计时升级
		BuildQue buildQue = new BuildQue();
		buildQue.setBuildingId(buildingId);
		// 毫秒
		long upTime = staticBuildingLv.getUptime() * 1000L;
		// double reduce = techManager.getBuildReduce(player);//科技加成
		long leftTime = getReduce(player, buildingType, upTime);
		// long leftTime = (long) (upTime * (1.0 - reduce) );
		buildQue.setPeriod(leftTime);
		buildQue.setEndTime(TimeHelper.getEndTime(leftTime));
		buildQue.setBuildQueType(buildingManager.getBuildType(player));
		buildQues.add(buildQue);
		for (BuildQue item : buildQues) {
			builder.addBuildQue(item.wrapPb());
		}

		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(UpBuildingRs.ext, builder.build());
//		activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.UP_BUILDING, 1);
		ActivityEventManager.getInst().activityTip(EventEnum.BUILD_LEVEL_UP, player, 1, 0);
	}

	// 美女对建筑升级加成
	public long getReduce(Player player, int buildingType, long upTime) {
		double reduce = techManager.getBuildReduce(player);// 科技加成
		double commandBuff = broodWarManager.getCommandBuff(player, TechEffectId.ADD_MARCH_SPEED);
		// 基础升级时间*（1-建筑学科技加成）/（1+美女技能加成）
		// 美女百分比加成
		double effectValue = beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.BUILD_BONUS, 0) / DevideFactor.PERCENT_NUM;
		// 基础升级时间*（1-建筑学科技加成-母巢职位加成）/（1+美女技能加成）
		return (long) (upTime * (1.0 - reduce - commandBuff) / (1.0 + effectValue));
	}

	public void doStartLevelUp(Player player, int buildingId, int buildingLv) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(buildingId);
		triggers.add(buildingLv + 1);
		taskManager.doTask(TaskType.START_LEVELUP_BUILDING, player, triggers);
		dailyTaskManager.record(DailyTaskId.UP_BUILD, player, 1);
	}

	// 检查升级队列
	public void checkUpBuildings() {
		// 迭代玩家建造队列定时器
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		long now = System.currentTimeMillis();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}
			checkBuildQue(player, now);
			checkEmployee(player, now);
		}
	}

	public void checkBuildQue(Player player, long now) {
		// 检查建筑
		ConcurrentLinkedDeque<BuildQue> buildQues = player.getBuildQues();
		if (!buildQues.isEmpty()) {
			// 建造队列做成同步的
			Iterator<BuildQue> iter = buildQues.iterator();
			while (iter.hasNext()) {
				BuildQue buildQue = iter.next();
				if (buildQue == null) {
					continue;
				}

				long endTime = buildQue.getEndTime();
				if (endTime <= now) {
					// 建筑升级, 并从队列删除UpBuilding,推送消息给player
					// 先删除队列，再升级
					iter.remove();
					if (buildQue.isRebuild()) {
						rebuildSuccess(buildQue, player);
					} else {
						levelUpBuilding(buildQue.getBuildingId(), player);
					}
				}
			}
		}
	}

	// 检查内政官、科技官、铁匠官
	public void checkEmployee(Player player, long now) {
		EmployInfo employInfo = player.getEmployInfo();
		if (employInfo == null) {
			return;
		}

		if (employInfo.getOfficerTime() > 0 && employInfo.getOfficerTime() < now) {
			employInfo.setOfficerId(0);
		}

		if (employInfo.getResearcherTime() > 0 && employInfo.getResearcherTime() < now) {
			employInfo.setResearcherId(0);
		}

		if (employInfo.getBlackSmithTime() > 0 && employInfo.getBlackSmithTime() < now) {
			employInfo.setBlackSmithId(0);
		}
	}

	// 升级建筑, 定时器出发
	public void levelUpBuilding(int buildingId, Player player) {
		// 根据建筑Id, find 建筑Type
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			LogHelper.CONFIG_LOGGER.info("ERROR BUILDING TYPE CONFIG");
			return;
		}

		Building buildings = player.buildings;
		if (buildings == null) {

			return;
		}

		// 当前建筑等级
		int currentLv = buildings.getBuildingLv(buildingType, buildingId);

		// 当前建筑的最高等级
		int maxBuildingLv = staticBuildingMgr.maxBuildLv(buildingType);
		if (maxBuildingLv == Integer.MIN_VALUE) {
			return;
		}

		if (currentLv >= maxBuildingLv) {
			return;
		}

		// 升级
		buildings.levelupBuilding(buildingType, buildingId);
		// 美女获取
		beautyManager.levelUpBuildingGetBeauty(player, buildingId, 1);

		// 计算战斗力
		buildingManager.caculateBattleScore(player);

		int buildingLv = currentLv + 1;
		// LogHelper.logBuilding(player, buildingId, buildingLv);

		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, buildingLv);
		Award award = new Award();
		if (staticBuildingLv != null) {
			award = addBuildingLvAward(player, staticBuildingLv);
			// 发放美女模型
			List<Integer> beautyAward = staticBuildingLv.getBeautyAward();
			if (null != beautyAward && beautyAward.size() > 0) {
				playerManager.addAward(player, beautyAward.get(0), beautyAward.get(1), beautyAward.get(2), Reason.LEVEL_UP_BUILDING);
			}
		}

		SynBuildingUpRq.Builder builder = SynBuildingUpRq.newBuilder();
		CommonPb.Building.Builder baseBuilding = buildings.wrapBase(buildingType, buildingId);
		if (baseBuilding != null) {
			builder.setBuilding(baseBuilding);
		} else {
			LogHelper.CONFIG_LOGGER.info("no buildingId, buildingId = " + buildingId);
			return;
		}
		if (award.isOk()) {
			builder.setAward(award.wrapPb());
//            LogHelper.GAME_DEBUG.error("获得经验值:"+award.getCount());
		}
		triggerBuildingLvTask(buildingId, buildingLv, player);
		if (buildingType == BuildingType.TECH) {
			playerManager.changeTech(player);
			builder.setResearcherId(player.getEmployInfo().getResearcherId());
		}
		SynHelper.synMsgToPlayer(player, SynBuildingUpRq.EXT_FIELD_NUMBER, SynBuildingUpRq.ext, builder.build());
		// TODO jyb 升级指挥中心 开放建筑
		if (buildingType == BuildingType.COMMAND) {
			buildingManager.synBuildingsByCommandlv(player);
		}
		if (buildingType == BuildingType.WARE) {
			playerManager.synWareTimes(player);
		}
		if (!player.isLogin) {
			player.getOnlineMessage().addBuild(CommonPb.ThreeInt.newBuilder().setV1(buildingId).setV2(currentLv).setV3(buildingLv).build());
		}

		ActivityEventManager.getInst().activityTip(EventEnum.BUILD_UP_FINISH, player, buildingType, buildingLv);
	}

	public void triggerBuildingLvTask(int buildingId, int buildingLv, Player player) {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		arrayList.add(buildingId);
		arrayList.add(buildingLv);
		taskManager.doTask(TaskType.BUILDING_LEVELUP, player, arrayList);
		// 检测多个资源升级的任务
		if (buildingManager.isResouceBuilding(buildingId)) {
			taskManager.doTask(TaskType.RES_BUILDING_LEVEL_UP, player, arrayList);
		}
	}

	// 建筑加速
	public void buyBuildQueCd(BuyBuildQueCdRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int cost = req.getCost();
		if (cost < 1 || cost > 5) {
			handler.sendErrorMsgToPlayer(GameError.BUY_BUILD_CD_COST_ERROR);
			return;
		}

		int buildingId = req.getBuildingId();
		int itemId = req.getPropId();
		int propCount = req.getPropCount();
		// 根据不同的消费类型来判断秒升级建筑
		if (cost == 1) {// 砖石加速
			goldBuyBuildCd(player, buildingId, handler);
		} else if (cost == 2) {// 道具加速
			itemBuyBuildCd(player, itemId, buildingId, propCount, handler);
		} else if (cost == 3) {// 购买道具直接加速
			buyItemAndUse(player, itemId, buildingId, handler);
		} else if (cost == 4) { // 免费秒
			freeKillCd(player, handler, buildingId);
		} else if (cost == 5) {// 参与虫族加速活动
			zergAccelerateBuilding(player, handler, buildingId);
		} else {
			handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
			return;
		}
	}

	// 金币加速升级完成
	public void goldBuyBuildCd(Player player, int buildingId, ClientHandler handler) {
		BuildQue buildQue = player.getBuildQue(buildingId);
		if (buildQue == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}
		long endTime = buildQue.getEndTime();
		long minutes = TimeHelper.getTotalMinute(endTime);
		if (minutes <= 0) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}
		int buildCdLimit = staticLimitMgr.getBuildTimePrice();
		int needGold = (int) minutes * buildCdLimit;
		int owned = player.getGold();
		if (owned < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		buildQue.setEndTime(System.currentTimeMillis());
		// 扣除金币
		playerManager.subAward(player, AwardType.GOLD, 1, needGold, Reason.KILL_BUILD_CD);

		// 返回消息
		BuyBuildQueCdRs.Builder builder = BuyBuildQueCdRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setBuildQue(buildQue.wrapPb());
		handler.sendMsgToPlayer(BuyBuildQueCdRs.ext, builder.build());
		eventManager.useSpeed(player, Lists.newArrayList("建筑升级", minutes, owned, ""));
	}

	// 建筑加速 一次使用一个道具(改为一次可以使用多个道具)
	public void itemBuyBuildCd(Player player, int itemId, int buildingId, int propCount, ClientHandler handler) {
		// 检测道具的合法性
		int itemType = staticPropDataMgr.getPropType(itemId);
		if (itemType == ItemType.KILL_CD_PROP) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_TYPE_ERROR);
			return;
		}

		// 检测背包是否有一个道具
		Item item = player.getItem(itemId);
		if (item == null) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
			return;
		}

		// 物品数量不足
		if (item.getItemNum() < propCount) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		// 检测物品是否存在
		StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
		if (staticProp == null) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
			return;
		}

		// 根据effectValue来执行具体得到什么
		List<List<Long>> effectValue = staticProp.getEffectValue();
		if (effectValue == null) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_EFFECT_VALUE_IS_NULL);
			return;
		}

		if (effectValue.size() != 1) {
			handler.sendErrorMsgToPlayer(GameError.EFFECTVALUE_SIZE_ERROR);
			return;
		}

		// 计算道具减少的建筑cd
		List<Long> param = effectValue.get(0);
		if (param.size() != 3) {
			handler.sendErrorMsgToPlayer(GameError.USE_KILL_BUILD_RECRUIT_CD_ERROR);
			return;
		}

		int awardType = param.get(0).intValue();
		if (awardType != AwardType.KILL_BUILD_RECRUIT_CD) {
			handler.sendErrorMsgToPlayer(GameError.KILL_BUILD_RECRUIT_CD_AWARD_TYPE_ERROR);
			return;
		}

		// 可以减少的分钟数(换成毫秒)
		long reduceMinutes = param.get(2).intValue() * TimeHelper.SECOND_MS * propCount;

		// 建造队列剩余时间
		BuildQue buildQue = player.getBuildQue(buildingId);
		if (buildQue == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}
		long endTime = buildQue.getEndTime();
		long now = System.currentTimeMillis();
		if (endTime <= now) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}

		endTime = endTime - reduceMinutes;
		buildQue.setEndTime(endTime);

		// 扣除道具
		playerManager.subItem(player, itemId, propCount, Reason.KILL_BUILD_CD);

		// 返回消息
		BuyBuildQueCdRs.Builder builder = BuyBuildQueCdRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setBuildQue(buildQue.wrapPb());
		builder.setProp(item.wrapPb());

		handler.sendMsgToPlayer(BuyBuildQueCdRs.ext, builder.build());

		eventManager.useSpeed(player, Lists.newArrayList("建筑升级", reduceMinutes, 0, staticProp.getPropName()));
	}

	// 建筑加速 购买并使用
	public void buyItemAndUse(Player player, int itemId, int buildingId, ClientHandler handler) {
		// 检测物品是否存在
		StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
		if (staticProp == null) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
			return;
		}

		// 根据effectValue来执行具体得到什么
		List<List<Long>> effectValue = staticProp.getEffectValue();
		if (effectValue == null) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_EFFECT_VALUE_IS_NULL);
			return;
		}

		if (effectValue.size() != 1) {
			handler.sendErrorMsgToPlayer(GameError.EFFECTVALUE_SIZE_ERROR);
			return;
		}

		// 计算道具减少的建筑cd
		List<Long> param = effectValue.get(0);
		if (param.size() != 3) {
			handler.sendErrorMsgToPlayer(GameError.USE_KILL_BUILD_RECRUIT_CD_ERROR);
			return;
		}

		int awardType = param.get(0).intValue();
		if (awardType != AwardType.KILL_BUILD_RECRUIT_CD) {
			handler.sendErrorMsgToPlayer(GameError.KILL_BUILD_RECRUIT_CD_AWARD_TYPE_ERROR);
			return;
		}

		// 可以减少的分钟数(换成毫秒)
		long reduceMinutes = param.get(2).intValue() * TimeHelper.SECOND_MS;
		// 检查这个物品需要多少钱
		int needGold = staticProp.getPrice();
		int owned = player.getGold();
		if (owned < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// 建造队列剩余时间
		BuildQue buildQue = player.getBuildQue(buildingId);
		if (buildQue == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}
		long endTime = buildQue.getEndTime();
		long now = System.currentTimeMillis();
		if (endTime <= now) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}

		endTime = endTime - reduceMinutes;
		buildQue.setEndTime(endTime);
		// 扣除金币
		playerManager.subAward(player, AwardType.GOLD, 1, needGold, Reason.KILL_BUILD_CD);

		// 返回消息
		BuyBuildQueCdRs.Builder builder = BuyBuildQueCdRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setBuildQue(buildQue.wrapPb());
		handler.sendMsgToPlayer(BuyBuildQueCdRs.ext, builder.build());
		eventManager.useSpeed(player, Lists.newArrayList("建筑升级", reduceMinutes, needGold, staticProp.getPropName()));
	}
	@Autowired
	SeasonManager seasonManager;
	// 免费秒cd
	public void freeKillCd(Player player, ClientHandler handler, int buildingId) {
		// 检测物品是否存在
		StaticVip staticVip = staticVipMgr.getStaticVip(player.getVip());
		if (staticVip == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		// 可以减少的分钟数(换成毫秒)
		long reduceMinutes = staticVip.getFreeTime() * TimeHelper.SECOND_MS;
		double seasonBuf = seasonManager.getSeasonBuf(player, EffectType.EFFECT_TYPE25);
		reduceMinutes = (long) (reduceMinutes * (1 + seasonBuf));
		// 建造队列剩余时间
		BuildQue buildQue = player.getBuildQue(buildingId);
		if (buildQue == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}
		if (buildQue.getFreeTimes() == 1) {
			reduceMinutes = 300000;
		}
		// 已经用完之后发送空协议
		if (buildQue.getFreeTimes() == 1) {
			// handler.sendErrorMsgToPlayer(GameError.BUILD_QUE_FREE_UESD);
			BuyBuildQueCdRs.Builder builder = BuyBuildQueCdRs.newBuilder();
			handler.sendMsgToPlayer(BuyBuildQueCdRs.ext, builder.build());
			return;
		}

		long endTime = buildQue.getEndTime();
		long now = System.currentTimeMillis();
		if (endTime <= now) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}

		endTime = endTime - reduceMinutes;
		buildQue.setEndTime(endTime);
		buildQue.setFreeTimes(1);

		// 返回消息
		BuyBuildQueCdRs.Builder builder = BuyBuildQueCdRs.newBuilder();
		builder.setBuildQue(buildQue.wrapPb());
		handler.sendMsgToPlayer(BuyBuildQueCdRs.ext, builder.build());

	}

	// 虫族加速活动减少建筑时长
	public void zergAccelerateBuilding(Player player, ClientHandler handler, int buildingId) {
		// 检测物品是否存在
		StaticVip staticVip = staticVipMgr.getStaticVip(player.getVip());
		if (staticVip == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		logger.debug("加速->[{}]", player.getLord().getLordId());
		// 可以减少的分钟数(换成毫秒)
		long reduceMinutes = 300000;// 活动默认减少5分钟暂时写死

		// 建造队列剩余时间
		BuildQue buildQue = player.getBuildQue(buildingId);
		if (buildQue == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}
		if (buildQue.getActivityDerateCD() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.BUY_BUILD_CD_COST_ERROR);
			return;
		}
		if (buildQue.getFreeTimes() == 1) {
			reduceMinutes = 300000;
		}
		// 已经用完之后发送空协议
//        if (buildQue.getActivityDerateCD() == 1) {
//            // handler.sendErrorMsgToPlayer(GameError.BUILD_QUE_FREE_UESD);
//            BuyBuildQueCdRs.Builder builder = BuyBuildQueCdRs.newBuilder();
//            handler.sendMsgToPlayer(BuyBuildQueCdRs.ext, builder.build());
//            return;
//        }

		long endTime = buildQue.getEndTime();
		long now = System.currentTimeMillis();
		if (endTime <= now) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}

		endTime = endTime - reduceMinutes;
		buildQue.setEndTime(endTime);
		buildQue.setActivityDerateCD(0);

		// 返回消息
		BuyBuildQueCdRs.Builder builder = BuyBuildQueCdRs.newBuilder();
		builder.setBuildQue(buildQue.wrapPb());
		handler.sendMsgToPlayer(BuyBuildQueCdRs.ext, builder.build());

	}

	// 购买建造队
	public void buyBuildTeam(BuyBuildTeamRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 主线任务达到41才开启
		if (!staticOpenManger.isOpen(OpenConsts.OPEN_59, player)) {
			handler.sendErrorMsgToPlayer(GameError.BUILD_TEAM_NOT_OPEN);
			return;
		}

		SimpleData simpleData = player.getSimpleData();
		if (simpleData.getBuyBuildTeam() == 0) {
			simpleData.setBuyBuildTeam(1);
			List<Award> awards = new ArrayList<Award>();
			int gold = staticLimitMgr.getNum(121);
			Award award = new Award(0, AwardType.GOLD, 0, gold);
			awards.add(award);
			playerManager.sendAttachMail(player, awards, 52);
		}

		// 检查元宝是否充足
		int buyType = req.getBuyType();
		int needGold = 0;
		long lastTime = 0L;
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		if (buyType == 1) {
			needGold = staticLimit.getOneDayBuildTeamCost();
			lastTime = TimeHelper.DAY_MS;
		} else if (buyType == 2) {
			needGold = staticLimit.getSevenDayBuildTeamCost();
			lastTime = TimeHelper.DAY_MS * 7;
		}

		Lord lord = player.getLord();
		if (lord.getGold() < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		playerManager.subAward(player, AwardType.GOLD, 1, needGold, Reason.BUY_BUILD_TEAM);

		long now = System.currentTimeMillis();
		if (lord.getBuildTeamTime() <= now) {
			lord.setBuildTeamTime(now);
		}

		lord.setBuildTeamTime(lord.getBuildTeamTime() + lastTime);

		BuyBuildTeamRs.Builder builder = BuyBuildTeamRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setBuildTeamTime(lord.getBuildTeamTime());

		handler.sendMsgToPlayer(BuyBuildTeamRs.ext, builder.build());

		eventManager.buildTeam(player, Lists.newArrayList(needGold));
	}

	// 资源图纸合成建筑
	public void openBuilding(OpenBuildingRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int buildingId = req.getBuildingId();
		// 检查建筑是否存在
		BuildingBase buildingBase = player.getResBuilding(buildingId);
		if (buildingBase != null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_EXISTS);
			return;
		}

		// 检查需要的图纸
		StaticPropBuilding staticPropBuilding = staticBuildingMgr.getStaticPropBuilding(buildingId);
		if (staticPropBuilding == null) {
			handler.sendErrorMsgToPlayer(GameError.PROP_BUILDING_CONFIG_NOT_EXISTS);
			return;
		}

		int itemNum = staticPropBuilding.getPropNum();
		int itemId = staticPropBuilding.getPropId();
		Item item = player.getItem(itemId);
		if (item == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		if (item.getItemNum() < itemNum) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		BuildingBase openBuilding = player.openResBuilding(buildingId);
		if (openBuilding == null) {
			handler.sendErrorMsgToPlayer(GameError.OPEN_BUILDING_IS_NULL);
			return;
		}
		playerManager.subAward(player, AwardType.PROP, itemId, itemNum, Reason.PROP_OPEN_BUILDING);

		OpenBuildingRs.Builder builder = OpenBuildingRs.newBuilder();
		builder.setBuilding(openBuilding.wrapPb());
		builder.setProp(item.wrapPb());
		handler.sendMsgToPlayer(OpenBuildingRs.ext, builder.build());

		buildingManager.caculateBattleScore(player);

		// LogHelper.logBuilding(player, buildingId, 1);

	}

	// 征收任务
	public void doGetResourceTask(Player player) {
		taskManager.doTask(TaskType.GET_RESOURCE, player, null);
	}

	// 招募内政官
	public void doHireOfficerTask(Player player, int employeeLv) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(employeeLv);
		taskManager.doTask(TaskType.HIRE_OFFIER, player, triggers);
	}

	// 建造加速
	public void primaryBuildSpeed(BuildingPb.PrimaryBuildSpeedRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int buildingId = req.getBuildingId();
		// 建造队列剩余时间
		BuildQue buildQue = player.getBuildQue(buildingId);
		if (buildQue == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}

		long endTime = buildQue.getEndTime();
		long now = System.currentTimeMillis();
		if (endTime <= now) {
			handler.sendErrorMsgToPlayer(GameError.BUILDQUE_NOT_EXISTS);
			return;
		}

		long time = buildQue.getPrimarySpeed();
		if (time <= 0) {
			handler.sendErrorMsgToPlayer(GameError.ALREADY_SPEED_BUILD);
			return;
		}

		endTime = endTime - time;
		buildQue.setPrimarySpeed(0L);
		buildQue.setEndTime(endTime);

		// 返回消息
		BuildingPb.PrimaryBuildSpeedRs.Builder builder = BuildingPb.PrimaryBuildSpeedRs.newBuilder();
		builder.setBuildQue(buildQue.wrapPb());
		handler.sendMsgToPlayer(BuildingPb.PrimaryBuildSpeedRs.ext, builder.build());
	}

	public void getWareRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 计算高级的
		int highGetTimes = playerManager.getHighWareLeftTimes(player);
		// 计算低级的
		int lowGetTimes = playerManager.getRebuildWareLeftTimes(player);

		BuildingPb.GetWareRs.Builder builder = BuildingPb.GetWareRs.newBuilder();
		builder.setRebuildTimes(lowGetTimes);
		builder.setHighLvTimes(highGetTimes);

		LostRes lostRes = player.getLostRes();
		List<CommonPb.Award> awards = lostRes.createAward();
		builder.addAllAward(awards);

		handler.sendMsgToPlayer(GetWareRs.ext, builder.build());

	}

	public void getWareAward(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		LostRes lostRes = player.getLostRes();
		List<Award> awards = lostRes.getAward();
		playerManager.addAward(player, awards, Reason.REBUILD_WARE);
		player.clearLostRes();
		GetWareAwardRs.Builder builder = GetWareAwardRs.newBuilder();
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(GetWareAwardRs.ext, builder.build());

	}

	public void checkAuto() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		long now = System.currentTimeMillis();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}

			checkAutoBuild(player);
			checkAutoWall(player, now);
		}
	}

	public void checkAutoWall(Player player, long now) {
		Wall wall = player.getWall();
		if (wall == null) {
			return;
		}

		Lord lord = player.getLord();
		// 未开启自动建造或者自动城防
		if (lord.getOnWall() == 0) {
			return;
		}

		// 检查开关和次数
		if (lord.getAutoWallTimes() <= 0) {
			lord.setOnWall(0);
			return;
		}

		// 自动城防
		recruitDefend(player, wall, now);

	}

	public boolean recruitDefend(Player player, Wall wall, long now) {
		// 检查剩余格子够不够
		int wallLv = wall.getLv();
		Map<Integer, WallDefender> wallDefenders = wall.getWallDefenders();
		int defenderCount = wallDefenders.size();
		int leftSize = wallLv - defenderCount;
		if (leftSize <= 0) {
			return false;
		}

		// 检查cd时间
		if (wall.getEndTime() > now) {
			return false;
		}

		// 随机一个城防军等级, 城墙的4~5倍等级
		int minLv = wallLv * 4;
		int maxLv = wallLv * 5;
		int randLv = RandomHelper.threadSafeRand(minLv, maxLv);

		int quality = wallMgr.randQuality(wallLv, wallDefenders);
		int soldier = RandomHelper.threadSafeRand(1, 3);
		int id = RandomHelper.threadSafeRand(1, 10);
		WallDefender wallDefender = new WallDefender();
		wallDefender.setKeyId(player.maxKey());
		wallDefender.setId(id); // 头像
		wallDefender.setLevel(randLv);
		wallDefender.setQuality(quality);
		wallDefender.setSoldier(soldier);
		wall.setEndTime(staticLimitMgr.getNum(24) * TimeHelper.SECOND_MS + now);
		StaticWallMonsterLv staticWallMonsterLv = staticWallMgr.getWallMonster(randLv, quality);
		wallDefender.setSoldierNum(staticWallMonsterLv.getSoldier());
		wall.addWallDefender(wallDefender);

		player.getLord().setAutoWallTimes(player.getLord().getAutoWallTimes() - 1);

		playerManager.synAutoWall(player, wallDefender, wall.getEndTime());

		return true;
	}

	public void checkAutoBuild(Player player) {
		if (!player.isOpenBuild()) {
			return;
		}

		// 检查开关和次数
		if (player.getAutoBuildTimes() <= 0) {
			player.setOnBuild(0);
			return;
		}

		autoBuild(player);
	}

	public void autoBuild(Player player) {
		int freeTeams = player.getFreeTeam();
		if (freeTeams <= 0) {
			return;
		}

		List<Integer> taskBuild = taskManager.getTaskBuilding(player);
		// 找出剩余的建筑
		TreeSet<Integer> buildings = player.getBuildingIds();
		if (!taskBuild.isEmpty()) {
			buildings.removeAll(taskBuild);
		}

		// 开始自动建造任务建筑，然后排序建筑
		for (Integer buildingId : taskBuild) {
			if (!canBuild(player, buildingId)) {
				continue;
			}

			if (!doAutoBuild(player, buildingId)) {
				continue;
			} else {
				break;
			}
		}

		for (Integer buildingId : buildings) {
			if (!canBuild(player, buildingId)) {
				continue;
			}

			if (!doAutoBuild(player, buildingId)) {
				continue;
			} else {
				break;
			}
		}

		// 如果没有可以升级的任务则关闭自动建造
		if (taskBuild.isEmpty() && buildings.isEmpty()) {
			player.setOnBuild(0);
			playerManager.synAutoBuild(player, Reason.AUTO_BUILD);
		}
	}

	public boolean canBuild(Player player, int buildingId) {
		Building buildings = player.buildings;
		if (buildings == null) {
			LogHelper.CONFIG_LOGGER.info("buildings is null!");
			return false;
		}
		// 建筑未收复
//        StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
		if (!buildings.getRecoverBuilds().contains(buildingId) && buildingId < 17) {
//            LogHelper.CONFIG_LOGGER.info("buildings is not recover !");
			return false;
		}

		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			LogHelper.CONFIG_LOGGER.info("not found building type, buildingId = " + buildingId);
			return false;
		}

		// 当前建筑等级
		int currentLv = buildings.getBuildingLv(buildingType, buildingId);
		if (currentLv == Integer.MIN_VALUE) {
			currentLv = 0;
		}

		// 当前建筑的最高等级
		int maxBuildingLv = staticBuildingMgr.maxBuildLv(buildingType);
		if (maxBuildingLv == Integer.MIN_VALUE) {
			LogHelper.CONFIG_LOGGER.info("config error, buildingType  = " + buildingType);
			return false;
		}

		// 是否达到最高等级
		if (currentLv >= maxBuildingLv) {
			return false;
		}

		// 检查建筑是否正在升级, 除了科技快研
		if (buildings.isBuildUping(buildingId)) {
			return false;
		}

		// 如果是兵营,如果有募兵, 则不能升级建筑
		if (buildings.isCamp(buildingType)) {
			int soldierType = soldierMgr.getSoldierType(buildingId);
			if (player.isSoldierTraining(soldierType)) {
				return false;
			}
		}

		// 如果升级的是科技馆，查看是否有研究正在进行
		if (buildingType == BuildingType.TECH) {
			// 如果有科技升级，则不能升级当前建筑
			boolean hasTecher = techManager.hasTecher(player);
			if (!hasTecher) {
				// 科技正在升级
				boolean isTechResearching = techManager.isTechResearching(player);
				if (isTechResearching) {
					return false;
				}
				boolean isHasSuccessTech = techManager.isHasSuccessTech(player);
				if (isHasSuccessTech) {
					return false;
				}
			}
		}

		// 如果是作坊，查看是否有正在生产的道具
		if (buildingType == BuildingType.WORK_SHOP) {
			// 生产的时候是否能升级建筑
			if (!workShopMgr.workShopCanUp(player)) {
				return false;
			}
		}

		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, currentLv + 1);
		if (staticBuildingLv == null) {
			LogHelper.CONFIG_LOGGER.info("static building lv is null! buildingType:{} lv:{}", buildingType, currentLv);
			return false;
		}

		List<List<Long>> upCond = staticBuildingLv.getUpCond();
		if (upCond == null || upCond.size() <= 0 || upCond.get(0) == null || upCond.get(0).size() <= 0) {
			LogHelper.CONFIG_LOGGER.info("upCond size is error!");
			return false;
		}

		// 检查升级条件
		for (List<Long> item : upCond) {
			if (item.size() <= 1) {
				continue;
			}
			int condType = item.get(0).intValue();
			List<Long> param = ArrayHelper.copyArray(item);
			GameError gameError = condDataMgr.onCondition(player, condType, param);
			if (gameError != GameError.OK) {
				return false;
			}
		}

		// 检查资源条件
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				return false;
			}
		}

		return true;
	}

	private void createBuildings(Player player, int buildingId, int buildingLv) {
		Building buildings = player.buildings;
		StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
		if (staticBuilding == null) {
			LogHelper.CONFIG_LOGGER.info("staticBuilding is null!");
			return;
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
		}

	}

	// 开始自动建造
	public boolean doAutoBuild(Player player, int buildingId) {
		// 根据建筑Id, find 建筑Type
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			LogHelper.CONFIG_LOGGER.info("buildingType == Integer.MIN_VALUE");
			return false;
		}

		Building building = player.buildings;

		// 当前建筑等级
		int currentLv = building.getBuildingLv(buildingType, buildingId);
		if (currentLv == Integer.MIN_VALUE) {
			LogHelper.CONFIG_LOGGER.info("buildingId error ->[{}]", buildingId);
			return false;
//            currentLv = 0;
//            createBuildings(player, buildingId, currentLv + 1);
		}

		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, currentLv + 1);
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				LogHelper.CONFIG_LOGGER.info("award type is not resource!");
				return false;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.LEVEL_UP_BUILDING);
		}

		// 开始升级建筑
		doStartLevelUp(player, buildingId, currentLv + 1);
		StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
		SpringUtil.getBean(EventManager.class).build_level_up(player, buildingId, staticBuilding.getName(), resourceCond, currentLv + 1);
		player.setAutoBuildTimes(player.getAutoBuildTimes() - 1);

		SynBuildingUpRq.Builder builder = SynBuildingUpRq.newBuilder();
		// 开始倒计时升级
		ConcurrentLinkedDeque<BuildQue> buildQues = building.getBuildQues();
		BuildQue buildQue = new BuildQue();
		buildQue.setBuildingId(buildingId);

		// 毫秒
		long upTime = staticBuildingLv.getUptime() * 1000L;
		// double reduce = techManager.getBuildReduce(player);
		long leftTime = getReduce(player, buildingType, upTime);// 美女对建筑的加成
		// long leftTime = (long) ((1.0 - reduce) * upTime);
		buildQue.setPeriod(leftTime);
		buildQue.setEndTime(TimeHelper.getEndTime(leftTime));
		buildQue.setBuildQueType(buildingManager.getBuildType(player));
		buildQues.add(buildQue);
		for (BuildQue item : buildQues) {
			builder.setBuildQue(item.wrapPb());
		}

		builder.setResource(player.wrapResourcePb());

		CommonPb.Building.Builder baseBuilding = building.wrapBase(buildingType, buildingId);
		if (baseBuilding != null) {
			builder.setBuilding(baseBuilding);
		}

		builder.setBuildQue(buildQue.wrapPb());
		builder.setAutoBuildTimes(player.getAutoBuildTimes());
		synAutoBuildToPlayer(player, builder);
//		activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.UP_BUILDING, 1);
		ActivityEventManager.getInst().activityTip(EventEnum.BUILD_LEVEL_UP, player, 1, 0);
		return true;
	}

	public Award addBuildingLvAward(Player player, StaticBuildingLv staticBuildingLv) {
		List<Integer> config = staticBuildingLv.getAward();
		if (config == null || config.size() != 3) {
			return new Award();
		}

		if (config.get(2) <= 0) {
			return new Award();
		}
		Award award = new Award(config.get(0), config.get(1), config.get(2));
		playerManager.addAward(player, award, Reason.LEVEL_UP_BUILDING);
		return award;
	}

	public void synAutoBuildToPlayer(Player player, SynBuildingUpRq.Builder builder) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			BasePb.Base.Builder msg = PbHelper.createSynBase(BuildingPb.SynBuildingUpRq.EXT_FIELD_NUMBER, BuildingPb.SynBuildingUpRq.ext, builder.build());
			GameServer.getInstance().sendMsgToPlayer(player, msg);
		}
	}

	// 开启战备工厂
	public void openMilitia(BuildingPb.OpenMilitiaRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int soldierType = req.getSoldierType();
		if (soldierType < SoldierType.ROCKET_TYPE || soldierType > SoldierType.WAR_CAR) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		int buildingId = BuildingId.MILITIA_CAMP;
		int buildingType = BuildingType.MILITIA_CAMP;
		// 检查建筑是否已经开启，如果开启了，就提示错误
		Building building = player.buildings;
		Camp camp = building.getCamp();
		Map<Integer, BuildingBase> campMap = camp.getCamp();
		if (campMap.containsKey(buildingId)) {
			handler.sendErrorMsgToPlayer(GameError.CAMP_IS_ALREADY_BUILD);
			return;
		}

		// 检查开启条件
		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, 1);
		if (staticBuildingLv == null) {
			LogHelper.CONFIG_LOGGER.info("staticBuildingLv is null, buildType = " + buildingType);
			handler.sendErrorMsgToPlayer(GameError.BUILDING_CONFIG_LV_ERROR);
			return;
		}

		List<List<Long>> upCond = staticBuildingLv.getUpCond();
		for (List<Long> item : upCond) {
			if (item.size() <= 1) {
				continue;
			}
			int condType = item.get(0).intValue();
			List<Long> param = ArrayHelper.copyArray(item);
			GameError gameError = condDataMgr.onCondition(player, condType, param);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		int soldierIndex = SoldierIndex.MILITIA;
		if (soldierMap.containsKey(soldierIndex)) {
			LogHelper.CONFIG_LOGGER.info("soldier has index = " + soldierIndex);
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		BuildingBase buildingBase = new BuildingBase(buildingId, 1);
		campMap.put(buildingId, buildingBase);
		Map<Integer, StaticSoldierLv> staticSoldierLvMap = staticSoldierMgr.getSoldierLvMap();
		StaticSoldierLv staticSoldierLv = staticSoldierLvMap.get(1);
		Soldier soldier = new Soldier();
		soldier.setSoldierType(soldierType);
		soldier.setNum(0);
		soldier.setCapacity(staticSoldierLv.getCapacity());
		soldier.setLargerTimes(0);
		soldier.setEmployeeTimes(0);
		soldier.setSoldierIndex(soldierIndex);
		soldierMap.put(soldierIndex, soldier);

		buildingManager.caculateBattleScore(player);// 刷新战斗力

		OpenMilitiaRs.Builder builder = OpenMilitiaRs.newBuilder();
		builder.setBuilding(buildingBase.wrapPb());
		builder.setSoldier(soldier.wrapPb());
		handler.sendMsgToPlayer(OpenMilitiaRs.ext, builder.build());

	}

	// 拆除战备工厂
	public void rebuildMilitia(BuildingPb.RebuildMilitiaRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查是否有开天辟地科技
		int rebuild = techManager.getRebuild(player);
		if (rebuild == 0) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_DESTROY);
			return;
		}

		// 检查建筑是否已经开启，如果开启了，就提示错误
		int buildingId = BuildingId.MILITIA_CAMP;
		Building building = player.buildings;
		Camp camp = building.getCamp();
		Map<Integer, BuildingBase> campMap = camp.getCamp();
		if (!campMap.containsKey(buildingId)) {
			handler.sendErrorMsgToPlayer(GameError.CAMP_IS_NOT_BUILD);
			return;
		}

		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		int soldierIndex = SoldierIndex.MILITIA;
		if (!soldierMap.containsKey(soldierIndex)) {
			LogHelper.CONFIG_LOGGER.info("soldier has index = " + soldierIndex);
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}
		Building buildings = player.buildings;

		// 如果是兵营,如果有募兵, 则不能升级建筑
		if (buildings.isCamp(BuildingType.MILITIA_CAMP)) {
			int soldierType = soldierMgr.getSoldierType(buildingId);
			if (player.isSoldierTraining(soldierType)) {
				handler.sendErrorMsgToPlayer(GameError.SOLDIER_IS_TRAINING);
				return;
			}
		}

		BuildingBase buildingBase = buildings.getCamp().getBuilding(buildingId);
		// 当前建筑一样等级的 改建的配置
		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(BuildingType.MILITIA_CAMP, buildingBase.getLevel());
		if (staticBuildingLv == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_CONFIG_LV_ERROR);
			return;
		}
		List<List<Long>> upCond = staticBuildingLv.getUpCond();
		// 检查升级条件
		for (List<Long> item : upCond) {
			if (item.size() <= 1) {
				continue;
			}
			int condType = item.get(0).intValue();
			List<Long> param = ArrayHelper.copyArray(item);
			GameError gameError = condDataMgr.onCondition(player, condType, param);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		// 检查资源条件
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				handler.sendErrorMsgToPlayer(GameError.AWARD_TYPE_ERROR);
				return;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.REBUILD_BUILDING_);
		}

		Soldier soldier = soldierMap.get(soldierIndex);
		soldier.setSoldierType(req.getSoilderType());
		// 检查当前建造队列类型
		ConcurrentLinkedDeque<BuildQue> buildQues = player.buildings.getBuildQues();
		// 开始倒计时升级
		BuildQue buildQue = new BuildQue();
		buildQue.setBuildingId(BuildingId.MILITIA_CAMP);
		buildQue.setRebuild(true);
		// 秒
		int upTime = 24 * 3600 * 1000;
		int vip = player.getVip();
		StaticVip staticVip = staticVipMgr.getStaticVip(vip);
		int freeTime = 0;
		if (staticVip != null) {
			freeTime = staticVip.getFreeTime();
		}
		double reduce = techManager.getBuildReduce(player);
		double leftTime = (1.0 - reduce) * upTime;
		leftTime = leftTime - freeTime >= 0 ? leftTime - freeTime : 0;
		int time = (int) Math.ceil(leftTime);
		BuildingPb.RebuildMilitiaRs.Builder builder = BuildingPb.RebuildMilitiaRs.newBuilder();
		buildQue.setPeriod(time);
		buildQue.setEndTime(TimeHelper.getEndTime(time));
		buildQue.setBuildQueType(buildingManager.getBuildType(player));
		buildQues.add(buildQue);
		for (BuildQue item : buildQues) {
			builder.addBuildQue(item.wrapPb());
		}
		// 更新战斗力
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(BuildingPb.RebuildMilitiaRs.ext, builder.build());

	}

	// 改土劈地: 科技拆除资源建筑
	public void destroyRes(BuildingPb.ResBuildingDesRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查下客户端有没有发送建筑Id
		if (!req.hasBuildingId()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_NO_BUILDING_ID);
			return;
		}
		if (!req.hasReBuildingId()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_NO_BUILDING_ID);
			return;
		}

		// 检查是否有资源建筑
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}
		// 检查玩家是否学了改土劈地科技
		int rebuild = techManager.getRebuild(player);
		if (rebuild == 0) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_DESTROY);
			return;
		}
		StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(req.getBuildingId());
		if (staticBuilding == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		if (staticBuilding.getRebuild() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_DESTROY);
			return;
		}

		List<Integer> reBuildInfo = staticBuildingMgr.getRebuildInfo(req.getBuildingId(), req.getReBuildingId());
		if (reBuildInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.REBUILD_ID_ERROR);
			return;
		}

		// 查询资源建筑
		ResBuildings resBuildings = building.getResBuildings();
		if (resBuildings == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}
		// 拿到当前的建筑
		BuildingBase bb = resBuildings.getBuilding(req.getBuildingId());
		if (bb == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}
		// 当前建筑一样等级的 改建的配置
		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(staticBuilding.getBuildingType(), bb.getLevel());
		if (staticBuildingLv == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_CONFIG_LV_ERROR);
			return;
		}
		Building buildings = player.buildings;
		List<List<Long>> upCond = staticBuildingLv.getUpCond();
		// 检查升级条件
		for (List<Long> item : upCond) {
			if (item.size() <= 1) {
				continue;
			}
			int condType = item.get(0).intValue();
			List<Long> param = ArrayHelper.copyArray(item);
			GameError gameError = condDataMgr.onCondition(player, condType, param);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		// 检查资源条件
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

//        resBuildings.addResourceBuilding(buildingId, bb.getLevel());
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				handler.sendErrorMsgToPlayer(GameError.AWARD_TYPE_ERROR);
				return;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);
			playerManager.subAward(player, awardType, resType, res, Reason.REBUILD_BUILDING_);
		}
		resBuildings.removeBuilding(req.getBuildingId());
		// 检查当前建造队列类型
		ConcurrentLinkedDeque<BuildQue> buildQues = buildings.getBuildQues();
		BuildingPb.ResBuildingDesRs.Builder builder = BuildingPb.ResBuildingDesRs.newBuilder();
		// 开始倒计时升级
		BuildQue buildQue = new BuildQue();
		buildQue.setBuildingId(req.getReBuildingId());
		buildQue.setReBuildingId(req.getBuildingId());
		buildQue.setRebuild(true);
		// 毫秒
		// 秒
		int upTime = 24 * 3600 * 1000;
		int vip = player.getVip();
		StaticVip staticVip = staticVipMgr.getStaticVip(vip);
		int freeTime = 0;
		if (staticVip != null) {
			freeTime = staticVip.getFreeTime();
		}
		double reduce = techManager.getBuildReduce(player);
		double leftTime = (1.0 - reduce) * upTime;
		leftTime = leftTime - freeTime >= 0 ? leftTime - freeTime : 0;
		int time = (int) Math.ceil(leftTime);
		buildQue.setPeriod(time);
		buildQue.setEndTime(TimeHelper.getEndTime(time));
		buildQue.setBuildQueType(buildingManager.getBuildType(player));
		buildQues.add(buildQue);
		for (BuildQue item : buildQues) {
			builder.addBuildQue(item.wrapPb());
		}
		resBuildings.addResourceBuilding(req.getReBuildingId(), bb.getLevel());
		// 更新战斗力
		buildingManager.caculateBattleScore(player);
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(BuildingPb.ResBuildingDesRs.ext, builder.build());
		logger.info("改建 之前的buidld->[{}] 改建后 [{}] 结果-> [{}]", req.getBuildingId(), req.getReBuildingId(), building.getBuildingIds());
	}

	// 建造资源建筑
	public void buildRes(BuildingPb.BuildResRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查下客户端有没有发送建筑Id
		if (!req.hasBuildingId()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_NO_BUILDING_ID);
			return;
		}

		// 检查是否有资源建筑
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}

		// 查询资源建筑
		ResBuildings resBuildings = building.getResBuildings();
		if (resBuildings == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}

		// 找到对应buildingId的建筑
		int buildingId = req.getBuildingId();
		BuildingBase bb = resBuildings.getBuilding(buildingId);
		// 已经建造了
		if (bb != null) {
			handler.sendErrorMsgToPlayer(GameError.BUILING_IS_BUILDED);
			return;
		}

		// 检查当前建筑Id是否在配置中，且为资源建筑
		StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
		if (staticBuilding == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		if (staticBuilding.getRebuild() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_BUILD);
			return;
		}

		// 检查资源类型
		if (!isResType(staticBuilding.getBuildingType())) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_BUILDING_TYPE_CONFIG);
			return;
		}

		resBuildings.addResourceBuilding(buildingId, 1);
		BuildingBase buildingBase = resBuildings.getBuilding(buildingId);
		if (buildingBase == null) {
			LogHelper.CONFIG_LOGGER.info("building add failed!");
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		BuildingPb.BuildResRs.Builder builder = BuildingPb.BuildResRs.newBuilder();
		builder.setBase(buildingBase.wrapPb());
		handler.sendMsgToPlayer(BuildingPb.BuildResRs.ext, builder.build());
	}

	public boolean isResType(int type) {
		return type == BuildingType.IRON || type == BuildingType.COPPER || type == BuildingType.OIL || type == BuildingType.STONE;
	}

	/**
	 * 购买建筑升级 不需要建筑队列
	 *
	 * @param handler
	 * @param req
	 */
	public void buyUpBuildQue(BuyUpBuildQueHandler handler, BuildingPb.BuyUpBuildQueRq req) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int buildingId = req.getBuildingId();
		// 根据建筑Id, find 建筑Type
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_BUILDING_TYPE_CONFIG);
			return;
		}
//        // 检查是否有空闲的建造队
//        if (player.getFreeTeam() <= 0) {
//            handler.sendErrorMsgToPlayer(GameError.NO_FREE_BUILD_TEAM);
//            return;
//        }

		Building buildings = player.buildings;
		if (buildings == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_EXISTS);
			return;
		}
		// 当前建筑等级
		int currentLv = buildings.getBuildingLv(buildingType, buildingId);

		// 当前建筑的最高等级
		int maxBuildingLv = staticBuildingMgr.maxBuildLv(buildingType);
		if (maxBuildingLv == Integer.MIN_VALUE) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_MAX_BUILDING_LEVEL);
			return;
		}
		// 是否达到最高等级
		if (currentLv >= maxBuildingLv) {
			handler.sendErrorMsgToPlayer(GameError.REACH_MAX_BUILDING_LEVEL);
			return;
		}
		// 检查建筑是否正在升级
		if (buildings.isBuildUping(buildingId)) {
			// 如果升级的是科技馆, 查看是否有研究正在进行
			if (buildingType == BuildingType.TECH) {
				// 如果有科技升级，则不能升级当前建筑
				boolean hasTecher = techManager.hasTecher(player);
				if (!hasTecher) {
					// 科技正在升级
					boolean isTechResearching = techManager.isTechResearching(player);
					if (isTechResearching) {
						handler.sendErrorMsgToPlayer(GameError.TECH_IS_RESEARCHING);
						return;
					}
				}
			} else {
				handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_UPING);
				return;
			}
		}
		// 如果是兵营,如果有募兵, 则不能升级建筑
		if (buildings.isCamp(buildingType)) {
			int soldierType = soldierMgr.getSoldierType(buildingId);
			if (player.isSoldierTraining(soldierType)) {
				handler.sendErrorMsgToPlayer(GameError.SOLDIER_IS_TRAINING);
				return;
			}
		}

		// 如果是作坊，查看是否有正在生产的道具
		if (buildingType == BuildingType.WORK_SHOP) {
			// 生产的时候是否能升级建筑
			if (!workShopMgr.workShopCanUp(player)) {
				handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_CAN_NOT_UP);
				return;
			}
		}

		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, currentLv + 1);
		if (staticBuildingLv == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_CONFIG_LV_ERROR);
			return;
		}
		List<List<Long>> upCond = staticBuildingLv.getUpCond();
		// 检查升级条件
		for (List<Long> item : upCond) {
			if (item.size() <= 1) {
				continue;
			}
			int condType = item.get(0).intValue();
			List<Long> param = ArrayHelper.copyArray(item);
			if (condType == ConditionType.FREE_BUILD_TEAM) {
				continue;
			}
			GameError gameError = condDataMgr.onCondition(player, condType, param);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}
		// 检查资源条件
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		// 秒
		int upTime = staticBuildingLv.getUptime();

		int vip = player.getVip();
		StaticVip staticVip = staticVipMgr.getStaticVip(vip);
		int freeTime = 0;
		if (staticVip != null) {
			freeTime = staticVip.getFreeTime();
		}
		double reduce = techManager.getBuildReduce(player);
		double leftTime = (1.0 - reduce) * upTime;
		leftTime = leftTime - freeTime >= 0 ? leftTime - freeTime : 0;
		int minutes = (int) Math.ceil(leftTime / 60);
		int buildCdLimit = staticLimitMgr.getBuildTimePrice();
		int needGold = minutes * buildCdLimit;
		if (player.getGold() < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		/**
		 * 建筑升级资源消耗日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				handler.sendErrorMsgToPlayer(GameError.AWARD_TYPE_ERROR);
				return;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.LEVEL_UP_BUILDING);

			if (awardType == AwardType.RESOURCE) {
				logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(resType), RoleResourceLog.OPERATE_OUT, resType, ResOperateType.BUILDING_UP_OUT.getInfoType(), res, player.account.getChannel()));
				int t = 0;
				switch (resType) {
					case ResourceType.IRON:
						t = IronOperateType.BUILDING_UP_OUT.getInfoType();
						break;
					case ResourceType.COPPER:
						t = CopperOperateType.BUILDING_UP_OUT.getInfoType();
						break;
					default:
						break;
				}
				if (t != 0) {
					logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, res, t), resType);
				}
			}
		}
		playerManager.subAward(player, AwardType.GOLD, 0, needGold, Reason.LEVEL_UP_BUILDING);

		// 升级
		buildings.levelupBuilding(buildingType, buildingId);
		// 美女获取
		beautyManager.levelUpBuildingGetBeauty(player, buildingId, 1);
		// 开始升级建筑
		doStartLevelUp(player, buildingId, currentLv + 1);
		StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
		SpringUtil.getBean(EventManager.class).build_level_up(player, buildingId, staticBuilding.getName(), resourceCond, currentLv + 1);
		// 计算战斗力
		buildingManager.caculateBattleScore(player);

		int buildingLv = currentLv + 1;
		// LogHelper.logBuilding(player, buildingId, buildingLv);

		staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, buildingLv);
		Award award = new Award();
		if (staticBuildingLv != null) {
			award = addBuildingLvAward(player, staticBuildingLv);
		}
		BuildingPb.BuyUpBuildQueRs.Builder builder = BuildingPb.BuyUpBuildQueRs.newBuilder();
		builder.setResource(player.getResource().wrapPb());
		builder.setGold(player.getGold());
		builder.setBuildingId(req.getBuildingId());
		builder.setLevel(buildingLv);
		if (award.isOk()) {
			builder.setAward(award.wrapPb());
//            LogHelper.GAME_DEBUG.error("获得经验值:"+award.getCount());
		}
		triggerBuildingLvTask(buildingId, buildingLv, player);
		if (buildingType == BuildingType.TECH) {
			playerManager.changeTech(player);
			builder.setResearcherId(player.getEmployInfo().getResearcherId());
		}
		handler.sendMsgToPlayer(BuildingPb.BuyUpBuildQueRs.ext, builder.build());
		// TODO jyb 升级指挥中心 开放建筑
		if (buildingType == BuildingType.COMMAND) {
			buildingManager.synBuildingsByCommandlv(player);
		}
		if (buildingType == BuildingType.WARE) {
			playerManager.synWareTimes(player);
		}

		List<Integer> beautyAward = staticBuildingLv.getBeautyAward();
		if (null != beautyAward && beautyAward.size() > 0) {
			playerManager.addAward(player, beautyAward.get(0), beautyAward.get(1), beautyAward.get(2), Reason.LEVEL_UP_BUILDING);
		}
		ActivityEventManager.getInst().activityTip(EventEnum.BUILD_LEVEL_UP, player, 1, 0);
//		activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.UP_BUILDING, 1);
//		activityManager.checkHeroKowtow(player, TaskType.BUILDING_LEVELUP, 1);
		ActivityEventManager.getInst().activityTip(EventEnum.BUILD_UP_FINISH, player, buildingType, buildingLv);
	}

	public void gmUpBuildLevel(Player player, int buildingId, int level) {
		// 根据建筑Id, find 建筑Type
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		Building buildings = player.buildings;
		if (buildings == null) {
			return;
		}
		// 升级
		buildings.gmlevelupBuilding(buildingType, buildingId, level);
		// 美女获取
		beautyManager.levelUpBuildingGetBeauty(player, buildingId, 1);
		// 计算战斗力
		buildingManager.caculateBattleScore(player);

		int buildingLv = level;
		// LogHelper.logBuilding(player, buildingId, buildingLv);

		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, buildingLv);
		if (staticBuildingLv == null) {
			return;
		}
		Award award = addBuildingLvAward(player, staticBuildingLv);
		BuildingPb.BuyUpBuildQueRs.Builder builder = BuildingPb.BuyUpBuildQueRs.newBuilder();
		builder.setResource(player.getResource().wrapPb());
		builder.setGold(player.getGold());
		builder.setBuildingId(buildingId);
		builder.setLevel(buildingLv);
		if (award.isOk()) {
			builder.setAward(award.wrapPb());
		}
		triggerBuildingLvTask(buildingId, buildingLv, player);
		SynHelper.synMsgToPlayer(player, BuildingPb.BuyUpBuildQueRs.EXT_FIELD_NUMBER, BuildingPb.BuyUpBuildQueRs.ext, builder.build());
		// TODO jyb 升级指挥中心 开放建筑
		if (buildingType == BuildingType.COMMAND) {
			buildingManager.synBuildingsByCommandlv(player);
		}
		if (buildingType == BuildingType.TECH) {
			playerManager.changeTech(player);
		}
	}

	public void rebuildSuccess(BuildQue buildQue, Player player) {
		// 根据建筑Id, find 建筑Type
		int buildingType = staticBuildingMgr.getBuildingType(buildQue.getBuildingId());
		if (buildingType == Integer.MIN_VALUE) {
			LogHelper.CONFIG_LOGGER.info("ERROR BUILDING TYPE CONFIG");
			return;
		}
		Building buildings = player.buildings;
		if (buildings == null) {
			return;
		}

		BuildingPb.SynRebuildSuccessRq.Builder builder = BuildingPb.SynRebuildSuccessRq.newBuilder();
		CommonPb.Building.Builder baseBuilding = buildings.wrapBase(buildingType, buildQue.getBuildingId());
		if (baseBuilding != null) {
			builder.setBuilding(baseBuilding);
			builder.setReBuildingId(buildQue.getReBuildingId());
		} else {
			LogHelper.CONFIG_LOGGER.info("no buildingId, buildingId = " + buildQue.getBuildingId());
			return;
		}
		if (buildQue.getBuildingId() == BuildingId.MILITIA_CAMP) {
			Map<Integer, Soldier> soldierMap = player.getSoldiers();
			int soldierIndex = SoldierIndex.MILITIA;
			Soldier soldier = soldierMap.get(soldierIndex);
			CommonPb.Soldier.Builder b = CommonPb.Soldier.newBuilder();
			b.setSoldierType(soldier.getSoldierType());
			b.setNum(soldier.getNum());
			b.setLargerTimes(soldier.getLargerTimes());
			b.setEmployeeTimes(soldier.getEmployeeTimes());
			b.setSoldierIndex(soldier.getSoldierIndex());
			builder.setSoldier(b);
		}
		SynHelper.synMsgToPlayer(player, BuildingPb.SynRebuildSuccessRq.EXT_FIELD_NUMBER, BuildingPb.SynRebuildSuccessRq.ext, builder.build());
	}

	/**
	 * 购买资源改建请求
	 *
	 * @param handler
	 * @param req
	 */
	public void buyRebuildQueue(BuyRebuildQueueHandler handler, BuildingPb.BuyRebuildQueueRq req) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int buildingId = req.getBuild();
		// 根据建筑Id, find 建筑Type
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_BUILDING_TYPE_CONFIG);
			return;
		}
		// 检查是否有空闲的建造队
		if (player.getFreeTeam() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_FREE_BUILD_TEAM);
			return;
		}

		Building buildings = player.buildings;
		if (buildings == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_EXISTS);
			return;
		}

		// 查询资源建筑
		ResBuildings resBuildings = buildings.getResBuildings();
		if (resBuildings == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}
		// 当前建筑等级
		int currentLv = buildings.getBuildingLv(buildingType, buildingId);
		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(buildingType, currentLv);
		if (staticBuildingLv == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_CONFIG_LV_ERROR);
			return;
		}

		List<Integer> reBuildInfo = staticBuildingMgr.getRebuildInfo(req.getBuild(), req.getRebuildId());
		if (reBuildInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.REBUILD_ID_ERROR);
			return;
		}

		// 检查资源条件
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}
		// 秒
		int upTime = 24 * 3600;
		int vip = player.getVip();
		StaticVip staticVip = staticVipMgr.getStaticVip(vip);
		int freeTime = 0;
		if (staticVip != null) {
			freeTime = staticVip.getFreeTime();
		}
		double reduce = techManager.getBuildReduce(player);
		double leftTime = (1.0 - reduce) * upTime;
		leftTime = leftTime - freeTime >= 0 ? leftTime - freeTime : 0;
		int minutes = (int) Math.ceil(leftTime / 60);
		int buildCdLimit = staticLimitMgr.getBuildTimePrice();
		int needGold = minutes * buildCdLimit;
		if (player.getGold() < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				handler.sendErrorMsgToPlayer(GameError.AWARD_TYPE_ERROR);
				return;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.BUY_REBUILD_BUILDING_);
		}
		playerManager.subAward(player, AwardType.GOLD, 0, needGold, Reason.BUY_REBUILD_BUILDING_);
		// 计算战斗力
		buildingManager.caculateBattleScore(player);

		resBuildings.removeBuilding(req.getBuild());
		resBuildings.addResourceBuilding(req.getRebuildId(), currentLv);

		BuyRebuildQueueRs.Builder builder = BuyRebuildQueueRs.newBuilder();
		CommonPb.Building.Builder build = CommonPb.Building.newBuilder();
		build.setBuildingId(req.getRebuildId());
		build.setLv(currentLv);
		builder.setBuilding(build);
		builder.setResource(player.getResource().wrapPb());
		handler.sendMsgToPlayer(BuildingPb.BuyRebuildQueueRs.ext, builder.build());
		logger.info("改建 之前的buidld->[{}] 改建后 [{}] 结果-> [{}]", req.getBuild(), req.getRebuildId(), buildings.getBuildingIds());
	}

	public void buyRebMilitiaQueue(BuyRebMilitiaQueueHandler handler, BuyRebMilitiaQueueRq req) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int buildingId = BuildingId.MILITIA_CAMP;
		Building building = player.buildings;
		Camp camp = building.getCamp();
		Map<Integer, BuildingBase> campMap = camp.getCamp();
		if (!campMap.containsKey(buildingId)) {
			handler.sendErrorMsgToPlayer(GameError.CAMP_IS_NOT_BUILD);
			return;
		}

		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		int soldierIndex = SoldierIndex.MILITIA;
		if (!soldierMap.containsKey(soldierIndex)) {
			LogHelper.CONFIG_LOGGER.info("soldier has index = " + soldierIndex);
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}
		Building buildings = player.buildings;
		// 如果是兵营,如果有募兵, 则不能升级建筑
		if (buildings.isCamp(BuildingType.MILITIA_CAMP)) {
			int soldierType = soldierMgr.getSoldierType(buildingId);
			if (player.isSoldierTraining(soldierType)) {
				handler.sendErrorMsgToPlayer(GameError.SOLDIER_IS_TRAINING);
				return;
			}
		}

		BuildingBase buildingBase = buildings.getCamp().getBuilding(buildingId);
		// 当前建筑一样等级的 改建的配置
		StaticBuildingLv staticBuildingLv = staticBuildingMgr.getBuildingLv(BuildingType.MILITIA_CAMP, buildingBase.getLevel());
		if (staticBuildingLv == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_CONFIG_LV_ERROR);
			return;
		}
		List<List<Long>> upCond = staticBuildingLv.getUpCond();
		// 检查资源条件
		List<List<Long>> resourceCond = staticBuildingLv.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				handler.sendErrorMsgToPlayer(GameError.AWARD_TYPE_ERROR);
				return;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.REBUILD_BUILDING_);
		}

		// 秒
		int upTime = 24 * 3600;
		int vip = player.getVip();
		StaticVip staticVip = staticVipMgr.getStaticVip(vip);
		int freeTime = 0;
		if (staticVip != null) {
			freeTime = staticVip.getFreeTime();
		}
		double reduce = techManager.getBuildReduce(player);
		double leftTime = (1.0 - reduce) * upTime;
		leftTime = leftTime - freeTime >= 0 ? leftTime - freeTime : 0;
		int minutes = (int) Math.ceil(leftTime / 60);
		int buildCdLimit = staticLimitMgr.getBuildTimePrice();
		int needGold = minutes * buildCdLimit;
		if (player.getGold() < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			if (awardType != AwardType.RESOURCE) {
				handler.sendErrorMsgToPlayer(GameError.AWARD_TYPE_ERROR);
				return;
			}

			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.BUY_REBUILD_BUILDING_);
		}
		playerManager.subAward(player, AwardType.GOLD, 0, needGold, Reason.BUY_REBUILD_BUILDING_);
		// 计算战斗力
		buildingManager.caculateBattleScore(player);
		Soldier soldier = soldierMap.get(soldierIndex);
		soldier.setSoldierType(req.getSoilderType());
		BuyRebMilitiaQueueRs.Builder builder = BuyRebMilitiaQueueRs.newBuilder();
		builder.setGold(player.getGold());
		CommonPb.Soldier.Builder b = CommonPb.Soldier.newBuilder();
		b.setSoldierType(soldier.getSoldierType());
		b.setNum(soldier.getNum());
		b.setLargerTimes(soldier.getLargerTimes());
		b.setEmployeeTimes(soldier.getEmployeeTimes());
		b.setSoldierIndex(soldier.getSoldierIndex());
		builder.setSoldier(b);
		builder.setResource(player.getResource().wrapPb());
		handler.sendMsgToPlayer(BuildingPb.BuyRebMilitiaQueueRs.ext, builder.build());
	}

	/**
	 * 收复建筑
	 *
	 * @param req
	 * @param handler
	 */
	public void recoverBuild(RecoverBuildRq req, RecoverBuildHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查下客户端有没有发送建筑Id
		if (!req.hasBuild()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_NO_BUILDING_ID);
			return;
		}

		// 检查是否有资源建筑
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}
		// || !staticOpenManger.isBuildOpen(req.getBuild(), player)
		if (building.getRecoverBuilds().contains(req.getBuild())) {
			handler.sendErrorMsgToPlayer(GameError.BUILD_IS_RECOVERED);
			return;
		}

		building.getRecoverBuilds().add(req.getBuild());
		taskManager.doTask(TaskType.RECOVER_BUILDING, player, null);
		handler.sendMsgToPlayer(BuildingPb.RecoverBuildRs.ext, RecoverBuildRs.newBuilder().build());
		SpringUtil.getBean(EventManager.class).build_unlock(player, req.getBuild(), "");
	}

}
