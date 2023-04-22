package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.ActivityConst;
import com.game.constant.AwardType;
import com.game.constant.BeautySkillTypeNew;
import com.game.constant.ChatShowType;
import com.game.constant.ConditionType;
import com.game.constant.DailyTaskId;
import com.game.constant.DevideFactor;
import com.game.constant.GameError;
import com.game.constant.Quality;
import com.game.constant.Reason;
import com.game.constant.ResourceType;
import com.game.constant.TaskType;
import com.game.constant.TechEffectId;
import com.game.constant.TechType;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticTechMgr;
import com.game.domain.Player;
import com.game.domain.p.EmployInfo;
import com.game.domain.p.Employee;
import com.game.domain.p.Tech;
import com.game.domain.p.TechInfo;
import com.game.domain.p.TechQue;
import com.game.domain.s.StaticEmployee;
import com.game.domain.s.StaticLimit;
import com.game.domain.s.StaticTechInfo;
import com.game.domain.s.StaticTechType;
import com.game.log.constant.CopperOperateType;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.TechPb;
import com.game.season.SeasonManager;
import com.game.season.talent.entity.EffectType;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TechService {

	private static Logger logger = LoggerFactory.getLogger(TechService.class);

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private CondMgr condDataMgr;

	@Autowired
	private BuildingManager buildingManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticTechMgr staticTechMgr;

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private TechManager techManager;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private BeautyManager beautyManager;
	@Autowired
	private BroodWarManager broodWarManager;
	@Autowired
	HeroManager heroManager;

	// 获取科技
	public void getTechInfo(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Tech tech = player.getTech();

		// 科技等级信息
		Map<Integer, TechInfo> techInfoMap = tech.getTechInfoMap();
		// 科技研发进度
		LinkedList<TechQue> techQues = tech.getTechQues();

		TechPb.GetTechRs.Builder builder = TechPb.GetTechRs.newBuilder();
		// 科技信息
		for (TechInfo techInfo : techInfoMap.values()) {
			builder.addTechInfo(techManager.wrapTechPb(techInfo));
		}

		if (!techQues.isEmpty()) {
			TechQue techQue = techQues.getFirst();
			if (techQue != null) {
				if (techQue.getLevel() <= 0) {
					techQue.setLevel(1);
				}
				builder.setTechQue(techManager.wrapTechQuePb(player, techQue));
			}
		}

		handler.sendMsgToPlayer(TechPb.GetTechRs.ext, builder.build());
	}

	// 升级科技
	public void upTech(TechPb.UpTechRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int techType = req.getTechType();
		int techLevel = req.getTechLevel();

		Tech tech = player.getTech();
		if (tech == null) {
			handler.sendErrorMsgToPlayer(GameError.TECH_NOT_EXISTS);
			return;
		}

		// 科技等级信息检查
		TechInfo techInfo = tech.getTechInfo(techType);
		if (techInfo == null) {
			techInfo = techManager.createTechInfo(player, techType);
		}

		// 检查科技是否存在
		StaticTechInfo staticTechInfo = staticTechMgr.getStaticTechLevel(techType, techLevel);
		if (staticTechInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		StaticTechType staticTechType = staticTechMgr.getStaticTechType(techType);
		if (staticTechType == null) {
			handler.sendErrorMsgToPlayer(GameError.TECH_TYPE_NOT_EXISTS);
			return;
		}

		// 检查科技是否最高等级了
		// 科技是否到满级
		int maxLevel = staticTechType.getMaxLevel();
		if (techInfo.getLevel() >= maxLevel) {
			handler.sendErrorMsgToPlayer(GameError.TECH_REACH_MAX_LEVEL);
			return;
		}

		// 如果3/3了，还发升级信息,那就有问题了, 配置启动的时候进行计算
		int maxProcess = staticTechMgr.getMaxProcess(techType, techLevel);
		if (maxProcess == Integer.MIN_VALUE) {
			handler.sendErrorMsgToPlayer(GameError.TECH_CONFIG_ERROR);
			return;
		}

		// 是否有科技正在升级
		LinkedList<TechQue> techQues = tech.getTechQues();
		long now = System.currentTimeMillis();
		for (TechQue techQue : techQues) {
			if (techQue == null) {
				continue;
			}

			long endTime = techQue.getEndTime();
			if (endTime > now) {
				handler.sendErrorMsgToPlayer(GameError.TECH_IS_LEVEL_UPING);
				return;
			}
		}

		// 开启VIP科研之后,雇佣高级科研，还可以升级科技
		boolean hasTecher = techManager.hasTecher(player);
		if (!hasTecher) {
			// 科技正在升级
			boolean isTechBuilding = techManager.isTechBuilding(player);
			if (isTechBuilding) {
				handler.sendErrorMsgToPlayer(GameError.BUILDING_IS_UPING);
				return;
			}

		}

		// 检查科技等级
		if (tech.getLv() < staticTechInfo.getTechLv()) {
			handler.sendErrorMsgToPlayer(GameError.TECH_LEVEL_NOT_ENOUGH);
			return;
		}

		// 检查资源条件
		List<List<Long>> resourceCond = staticTechInfo.getResourceCond();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		// 检查建筑的前置科技
		if (!techManager.isPreOk(techType, player)) {
			handler.sendErrorMsgToPlayer(GameError.PRE_TECH_NOT_OK);
			return;
		}

		int currentProcess = techInfo.getProcess();
		if (currentProcess >= maxProcess) {
			handler.sendErrorMsgToPlayer(GameError.TECH_REACH_MAX_PROCESS);
			return;
		}

		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			int resType = item.get(1).intValue();
			Long res = item.get(2);

			playerManager.subAward(player, awardType, resType, res, Reason.LEVEL_UP_TECH);

			/**
			 * 科技研究资源消耗日志埋点
			 */
			if (awardType == AwardType.RESOURCE) {
				com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
				logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(resType), RoleResourceLog.OPERATE_OUT, resType, ResOperateType.TEC_RESEARCH_OUT.getInfoType(), res, player.account.getChannel()));
				int t = 0;
				switch (resType) {
					case ResourceType.IRON:
						t = IronOperateType.TEC_RESEARCH_OUT.getInfoType();
						break;
					case ResourceType.COPPER:
						t = CopperOperateType.TEC_RESEARCH_OUT.getInfoType();
						break;
					default:
						break;
				}
				if (t != 0) {
					logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, res, t), resType);
				}
			}

		}

		// 更新科技
		TechQue techQue = new TechQue();
		techQue.setLevel(techInfo.getLevel() + 1);
		techQue.setTechType(techType);
		long upTime = TimeHelper.SECOND_MS * staticTechInfo.getUpTime();
		// 美女科技加成
		double beautySkillEffect = beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.RESEARCH_SPEED_BONUS, 0) / DevideFactor.PERCENT_NUM;
		// 母巢职位加成
		double commandBuff = broodWarManager.getCommandBuff(player, TechEffectId.ADD_TECH_UP_SPEED);
		upTime = (long) Math.floor(upTime * (1 - beautySkillEffect - commandBuff));
		techQue.setEndTime(now + upTime);
		techQue.setSpeed(0);

		// 现在只允许有一个升级队列
		techQues.clear();
		techQues.add(techQue);

		// 开始研究科技
		doUpTech(player, techType, techInfo.getLevel(), techInfo.getProcess(), TaskType.START_TECH_UP);
		TechPb.UpTechRs.Builder builder = TechPb.UpTechRs.newBuilder();
		builder.setTechQue(techManager.wrapTechQuePb(player, techQue));
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(TechPb.UpTechRs.ext, builder.build());

		// 该活动中科技研究类型为1000+techType,完成科技研究类型为2000+techType
		activityManager.updActSeven(player, ActivityConst.TYPE_ADD, 1000 + techType, 0, 1);
		ActivityEventManager.getInst().activityTip(EventEnum.TECH_UP, player, 1, 0);
//		activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.UP_TECH, 1);
		SpringUtil.getBean(EventManager.class).tech_level_up(player, resourceCond, staticTechInfo.getKeyId(), staticTechInfo.getUpgradeLevelDesc(), staticTechInfo.getTechLv());
		dailyTaskManager.record(DailyTaskId.RESEARCH_TECH, player, 1);
	}

	// 科技升级完成
	public void techLevelup(TechPb.TechLevelupRq req, ClientHandler handler) {
		// 检查是否有可以升级的建筑
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Tech tech = player.getTech();
		if (tech == null) {
			handler.sendErrorMsgToPlayer(GameError.TECH_NOT_EXISTS);
			return;
		}

		LinkedList<TechQue> techQues = tech.getTechQues();
		long now = System.currentTimeMillis();
		for (TechQue techQue : techQues) {
			if (techQue == null) {
				continue;
			}

			long endTime = techQue.getEndTime();
			if (endTime > now) {
				handler.sendErrorMsgToPlayer(GameError.TECH_IS_LEVEL_UPING);
				return;
			}
		}

		if (techQues.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.NO_TECH_IS_LEVEL_UPING);
			return;
		}

		Iterator<TechQue> iter = techQues.iterator();
		TechPb.TechLevelupRs.Builder builder = TechPb.TechLevelupRs.newBuilder();
		// 目前就一个科技队列
		while (iter.hasNext()) {
			TechQue techQue = iter.next();
			if (techQue == null) {
				continue;
			}

			long endTime = techQue.getEndTime();
			if (endTime > now) {
				continue;
			}

			// 删除研究
			iter.remove();

			// 检查是否达到最大进度
			int techType = techQue.getTechType();
			TechInfo techInfo = tech.getTechInfo(techType);

			if (techInfo == null) {
				techInfo = techManager.createTechInfo(player, techType);
			}

			int currentLevel = techInfo.getLevel();
			currentLevel += 1;
			// 获取当前等级下一级的科技进度
			int maxProcess = staticTechMgr.getMaxProcess(techType, currentLevel);
			if (maxProcess == Integer.MIN_VALUE) {
				handler.sendErrorMsgToPlayer(GameError.TECH_CONFIG_ERROR);
				return;
			}

			int currentProcess = techInfo.getProcess() + 1;

			// 达到最大科技进度
			if (currentProcess >= maxProcess) {
				// 升级科技
				techManager.updateTechLevel(player, techInfo, techType);
				// 满了就需要发送到客户端
				doUpTech(player, techType, techInfo.getLevel(), techInfo.getProcess(), TaskType.START_TECH_UP);
				doUpTech(player, techType, techInfo.getLevel(), techInfo.getProcess(), TaskType.FINISH_TECH);

				ActivityEventManager.getInst().activityTip(EventEnum.TECH_UP_FINISH, player, techType, techInfo.getLevel());

				// 英雄类型
				if (techType == TechType.PRIMARY_HERO_NUM || techType == TechType.MIDDLE_HERO_NUM) {
					techManager.handlePlayerEmbatlle(player, techType);
				}
				chatManager.updateChatShow(ChatShowType.TECH, techType, player);

				// LogHelper.techLevelUp(player, techType, techInfo.getLevel());

			} else {
				techManager.updateTechProcess(techInfo, techType);
				doUpTech(player, techType, techInfo.getLevel(), techInfo.getProcess(), TaskType.START_TECH_UP);
				doUpTech(player, techType, techInfo.getLevel(), techInfo.getProcess(), TaskType.FINISH_TECH);
			}

			builder.setTechInfo(techManager.wrapTechPb(techInfo));
			builder.setTechQue(techManager.wrapTechQuePb(player, techQue));

			if (techType == TechType.PRIMARY_SOLDIER_LINE || techType == TechType.MIDDLE_SOLDIER_LINE) {
				heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
			}
			break;
		}

		if (builder.hasTechQue()) {
			handler.sendMsgToPlayer(TechPb.TechLevelupRs.ext, builder.build());
		} else {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
		}

	}

	// 完成任务
	public void doUpTech(Player player, int techType, int techLv, int process, int techId) {
		ArrayList<Integer> param = new ArrayList<Integer>();
		param.add(techType);
		param.add(techLv);
		param.add(process);
		taskManager.doTask(techId, player, param);

		// 该活动中科技研究类型为1000+techType,完成科技研究类型为2000+techType
		activityManager.updActSeven(player, ActivityConst.TYPE_SET, 2000 + techType, 0, 1);
	}

	// 招募研究员
	public void hireResearcherRq(TechPb.HireResearcherRq req, ClientHandler handler) {
		// 检测是否有免费次数, 先检查玩家身上有没有这个研究员
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
		// 研究员要求的司令部等级不足
		if (playerCommandLv < commandLv) {
			handler.sendErrorMsgToPlayer(GameError.COMMAND_LEVEL_LOW_EMPLOYEE);
			return;
		}

		// 检查科技馆等级
		int techLv = player.getTechLv();
		if (techLv < staticEmployee.getTechLv()) {
			handler.sendErrorMsgToPlayer(GameError.TECH_LEVEL_NOT_ENOUGH);
			return;
		}

		EmployInfo employInfo = player.getEmployInfo();
		if (employInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		Map<Integer, Employee> employeeMap = employInfo.getEmployeeMap();
		if (employeeMap == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		Employee employee = employeeMap.get(req.getEmployeeId());
		// 说明没有招募过这个研究员
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
			boolean isOk = hireResearcher(handler, employeeId, employInfo, player);
			if (!isOk) {
				return;
			}
		}

		// 更新当前英雄Id
		employee.setEndTime(staticEmployee.getDurationTime() * TimeHelper.SECOND_MS + System.currentTimeMillis());
		employInfo.setResearcherId(employeeId); // 设置研究员Id
		doHireReseacherTask(player, staticEmployee.getLevel());
		if (staticEmployee.getQuality() >= Quality.PURPLE.get()) {
			activityManager.updActSeven(player, ActivityConst.TYPE_ADD, 3, 0, 1);
		}

		// 同步消息到客户端
		TechPb.HireResearcherRs.Builder builder = TechPb.HireResearcherRs.newBuilder();
		builder.setEmployeeId(employee.getEmployeeId());
		builder.setPeriod(staticEmployee.getDurationTime() * TimeHelper.SECOND_MS);
		builder.setEndTime(employee.getEndTime());
		builder.setUseTimes(employee.getUseTimes());
		builder.setIron(player.getIron());
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(TechPb.HireResearcherRs.ext, builder.build());

		eventManager.hireOfficer(player, Lists.newArrayList(employee.getUseTimes() < freeTimes, staticEmployee.getLevel(), staticEmployee.getName(), staticEmployee.getEmployId()));
	}

	public boolean hireResearcher(ClientHandler handler, int employeeId, EmployInfo employInfo, Player player) {
		// 当前有生铁内政, 说明当前内政是生铁类型
		int currentEmId = employInfo.getResearcherId();
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

		if (curLevel <= targetLevel && employee != null) {
			long timeLeft = employee.getEndTime() - System.currentTimeMillis();
			// 如果招募的是铁币，且又继续招募铁币研究员
			if (isCurIron && isEmployeeIron && timeLeft > 0) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_SAME_TYPE_RESEACHER);
				return false;
			}

			// 如果招募的是金币，又招募铁币，不允许
			if (isCurGold && isEmployeeIron && timeLeft > 0) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_SAME_TYPE_RESEACHER);
				return false;
			}

			// 如果招募的是金币，又招募金币, 不允许叠加
			if (isCurGold && isEmployeeGold && timeLeft > 0) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_SAME_TYPE_RESEACHER);
				return false;
			}
		}

		// 如果有更高等级的研究员,一样可以招募
		// 检测当前招募的英雄类型
		if (isEmployeeIron) {
			int need = staticEmployee.getCostIron();
			if (player.getIron() < need) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_OFFICER_NOT_ENOUGH_IRON);
				return false;
			}
			playerManager.subAward(player, AwardType.RESOURCE, ResourceType.IRON, need, Reason.HIRE_RESEARCHER);

		} else if (isEmployeeGold) {
			int gold = staticEmployee.getCostGold();
			if (player.getGold() < gold) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_OFFICER_NOT_ENOUGH_GOLD);
				return false;
			}
			playerManager.subAward(player, AwardType.GOLD, 1, gold, Reason.HIRE_RESEARCHER);
		}
		return true;
	}

	@Autowired
	SeasonManager seasonManager;

	// 研究秒cd请求
	public void techKillCdRq(TechPb.TechKillCdRq req, ClientHandler handler) {

		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Tech tech = player.getTech();
		if (tech == null) {
			handler.sendErrorMsgToPlayer(GameError.TECH_NOT_EXISTS);
			return;
		}

		// 检查是否存在可以秒CD的研发队列
		LinkedList<TechQue> techQues = tech.getTechQues();
		if (techQues.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.NO_TECH_IS_LEVEL_UPING);
			return;
		}

		TechQue techQue = techQues.getFirst();
		if (techQue == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_TECH_IS_LEVEL_UPING);
			return;
		}

		int cost = req.getCost();
		long now = System.currentTimeMillis();
		if (cost == 1) {
			// 检查是否有免费次数
			EmployInfo employInfo = player.getEmployInfo();
			if (employInfo == null) {
				handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
				return;
			}

//			if (techQue.getSpeed() == 1) {
//				handler.sendErrorMsgToPlayer(GameError.TECH_ALREADY_SPEED);
//				return;
//			}

			Employee researcher = employInfo.getResearcher();
			if (researcher == null || researcher.getEndTime() < now) {
				handler.sendErrorMsgToPlayer(GameError.RESEARCHER_NOT_EXISTS);
				return;
			}

			// 取出研究员可以免费加速的时间
			StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(researcher.getEmployeeId());
			if (staticEmployee == null) {
				handler.sendErrorMsgToPlayer(GameError.EMPLOYEE_CONFIG_ERROR);
				return;
			}
			int reduceTime1 = staticEmployee.getReduceTime();
			int buf = seasonManager.getBuf(player, EffectType.EFFECT_TYPE24);// EFFECT_TYPE24(24, "雇佣学者减免科技研究的时间增加（固定数值）"),
			reduceTime1 += buf;

			if (techQue.getSpeed() == 1) {
				if (reduceTime1 * TimeHelper.SECOND_MS <= techQue.getSpeedTime()) {
					handler.sendErrorMsgToPlayer(GameError.TECH_ALREADY_SPEED);
					return;
				}
				// 可以免费加速
				long reduceTime = reduceTime1 * TimeHelper.SECOND_MS - techQue.getSpeedTime();
				techQue.setEndTime(techQue.getEndTime() - reduceTime);
				techQue.setSpeedTime(reduceTime1 * TimeHelper.SECOND_MS);

				TechInfo techInfo = tech.getTechInfo(techQue.getTechType());
				techInfo.addSpeed(1);
				taskManager.doTask(TaskType.HIRE_SCIENTIST, player); // 利用雇佣的科学家进行一次研究加速
			} else {
				// 可以免费加速
				techQue.setSpeed(1);
				long reduceTime = reduceTime1 * TimeHelper.SECOND_MS;
				techQue.setEndTime(techQue.getEndTime() - reduceTime);
				techQue.setSpeedTime(reduceTime);

				TechInfo techInfo = tech.getTechInfo(techQue.getTechType());
				techInfo.addSpeed(1);
				taskManager.doTask(TaskType.HIRE_SCIENTIST, player); // 利用雇佣的科学家进行一次研究加速
			}
		} else if (cost == 2) {
			// 检查金币是否足够
			long endTime = techQue.getEndTime();
			long minutes = TimeHelper.getTotalMinute(endTime);
			if (minutes <= 0) {
				handler.sendErrorMsgToPlayer(GameError.NO_TECH_IS_LEVEL_UPING);
				return;
			}

			StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
			if (staticLimit == null) {
				handler.sendErrorMsgToPlayer(GameError.LIMIT_CONFIG_IS_NULL);
				return;
			}

			int killTechCd = staticLimit.getKillTechCdPrice();
			int needGold = (int) minutes * killTechCd;
			int owned = player.getGold();
			if (owned < needGold) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}

			techQue.setEndTime(now);
			// 扣除金币
			playerManager.subAward(player, AwardType.GOLD, 1, needGold, Reason.KILL_TECH_CD);

		} else if (cost == 3) {// 虫族加速活动固定时间5分钟
			if (techQue.getActivityDerateCD() <= 0) {
				handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
				return;
			}
			long reduceTime = 300000;
			techQue.setEndTime(techQue.getEndTime() - reduceTime);
			techQue.setActivityDerateCD(0);
		} else {
			handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
			return;
		}

		TechPb.TechKillCdRs.Builder builder = TechPb.TechKillCdRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setTechQue(techManager.wrapTechQuePb(player, techQue));

		handler.sendMsgToPlayer(TechPb.TechKillCdRs.ext, builder.build());

	}

	// public void techKillCdRq(TechPb.TechKillCdRq req , ClientHandler handler)
	// {

	// 招募研究员
	public void doHireReseacherTask(Player player, int employeeLv) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(employeeLv);
		taskManager.doTask(TaskType.HIRE_RESEARCHER, player, triggers);
	}

	/**
	 * gm 升级科技
	 *
	 * @param techType
	 * @param level
	 * @param player
	 */
	public void gmLevelUpTech(int techType, int level, Player player) {
		Tech tech = player.getTech();
		TechInfo techInfo = tech.getTechInfo(techType);

		if (techInfo == null) {
			techInfo = techManager.createTechInfo(player, techType);
		}
		// 升级科技
		techManager.updateTechLevel(player, techInfo, techType);
		techInfo.setLevel(level);
		// 满了就需要发送到客户端
		doUpTech(player, techType, techInfo.getLevel(), techInfo.getProcess(), TaskType.FINISH_TECH);
		TechPb.TechLevelupRs.Builder builder = TechPb.TechLevelupRs.newBuilder();
		// 英雄类型
		if (techType == TechType.PRIMARY_HERO_NUM || techType == TechType.MIDDLE_HERO_NUM) {
			techManager.handlePlayerEmbatlle(player, techType);
		}
		chatManager.updateChatShow(ChatShowType.TECH, techType, player);
		builder.setTechInfo(techManager.wrapTechPb(techInfo));
		TechQue techQue = new TechQue();
		techQue.setTechType(techType);
		techQue.setLevel(techInfo.getLevel());
		builder.setTechQue(techManager.wrapTechQuePb(player, new TechQue()));
		SynHelper.synMsgToPlayer(player, TechPb.TechLevelupRs.EXT_FIELD_NUMBER, TechPb.TechLevelupRs.ext, builder.build());
	}
}
