package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticSoldierMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticBuySolodierTime;
import com.game.domain.s.StaticCapacityTimes;
import com.game.domain.s.StaticLimit;
import com.game.domain.s.StaticProp;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.OilOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.pb.SoldierPb.*;
import com.game.season.SeasonManager;
import com.game.season.SeasonService;
import com.game.season.grand.entity.GrandType;
import com.game.season.talent.entity.EffectType;
import com.game.util.GameHelper;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SoldierService {
	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticSoldierMgr staticSoldierMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private SoldierManager soldierManager;

	@Autowired
	private StaticPropMgr staticPropDataMgr;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private TechManager techManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	SeasonManager seasonManager;

	/**
	 * Function: 获取士兵信息
	 */
	public void getSoldierRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null in getSoldierRq.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		GetSoldierRs.Builder builder = GetSoldierRs.newBuilder();
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		for (Map.Entry<Integer, Soldier> item : soldierMap.entrySet()) {
			if (item == null) {
				continue;
			}
			Soldier soldier = item.getValue();
			if (soldier == null) {
				continue;
			}
			builder.addSoldiers(soldier.wrapPb());
		}
		handler.sendMsgToPlayer(GameError.OK, GetSoldierRs.ext, builder.build());
	}

	/**
	 * Function: 兵营扩容请求
	 */
	public void largerBarracksRq(LargerBarracksRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null in getSoldierRq.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 获取当前兵营
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		int soldierType = req.getSoldierType();
		Soldier soldier = soldierMap.get(soldierType);
		if (soldier == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		int times = soldier.getLargerTimes();

		// 检查是否达到最大次数
		Map<Integer, StaticCapacityTimes> staticCapacityTimesMap = staticSoldierMgr.getCapacityTimesMap();
		int maxTimes = staticCapacityTimesMap.size();
		if (times >= maxTimes) {
			handler.sendErrorMsgToPlayer(GameError.REACH_MAX_SOLDIER_LARGER_TIMES);
			return;
		}

		// 检查次数 0~10
		// 0~times-1;
		StaticCapacityTimes staticCapacityTimes = staticCapacityTimesMap.get(times + 1);
		if (staticCapacityTimes == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_CAPACITY_CONFIG);
			return;
		}

		int price = staticCapacityTimes.getPrice();
		if (price <= 0) {
			handler.sendErrorMsgToPlayer(GameError.CAPACITY_SOLDIER_PRICE_ERROR);
			return;
		}

		// 检查元宝够不够
		Lord lord = player.getLord();
		if (lord.getGold() < price) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// 升级
		soldier.setLargerTimes(soldier.getLargerTimes() + 1);
		// 兵营容量 = 兵营等级容量＋ 配置容量
		// 计算兵营等级
		// calCapacity
		int totalCapacity = soldierManager.calculateCapacity(player, soldier);
		soldier.setCapacity(totalCapacity); // 更新当前兵营的容量

		// 扣除金币
		playerManager.subAward(player, AwardType.GOLD, 1, price, Reason.LARGER_BARRACKS);

		// 找到对应的兵符
		List<List<Integer>> awardList = staticCapacityTimes.getAward();
		Item item = null;
		Award award = null;
		for (List<Integer> awardItem : awardList) {
			if (awardItem.size() != 3) {
				continue;
			}

			Integer itemId = awardItem.get(1);
			int getType = GameHelper.getSoldierType(itemId);
			if (getType == -1) {
				continue;
			}

			if (getType == soldier.getSoldierType()) {
				Integer awardType = awardItem.get(0);
				Integer count = awardItem.get(2);
				playerManager.addAward(player, awardType, itemId, count, Reason.LARGER_BARRACKS);
				item = player.getItem(itemId);
				award = new Award(awardType, itemId, count);
				break;
			}
		}

		LargerBarracksRs.Builder builder = LargerBarracksRs.newBuilder();
		builder.setSoldierType(soldierType);
		builder.setGold(lord.getGold());
		builder.setLargerTimes(soldier.getLargerTimes());
		if (item != null) {
			builder.setProp(item.wrapPb());
		}
		if (null != award) {
			builder.setAward(award.wrapPb());
		}

		handler.sendMsgToPlayer(GameError.OK, LargerBarracksRs.ext, builder.build());

		eventManager.dilatation(player, Lists.newArrayList(SoldierName.getName(req.getSoldierType()), soldier.getLargerTimes()));
	}

	/**
	 * Function: 单次募兵时间购买请求
	 */
	public void levelupRecruitTimeRq(LevelupRecruitTimeRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查是否达到最大购买次数
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		Soldier soldier = soldierMap.get(req.getSoldierType());
		if (soldier == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		int times = soldier.getEmployeeTimes();
		// 检查是否达到最大次数
		Map<Integer, StaticBuySolodierTime> configMap = staticSoldierMgr.getBuySoldierTime();
		int maxTimes = configMap.size();
		if (times >= maxTimes) {
			handler.sendErrorMsgToPlayer(GameError.REACH_MAX_SOLDIER_EMPLOYEE_TIME_BUYTIMES);
			return;
		}

		// 检查次数
		StaticBuySolodierTime config = configMap.get(times + 1);
		if (config == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_EMPLOYEE_TIME_CONFIG);
			return;
		}

		// 检查价格
		long iron = player.getIron();
		if (iron < config.getPrice()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_IRON);
			return;
		}

		playerManager.subAward(player, AwardType.RESOURCE, 1, config.getPrice(), Reason.LEVEL_UP_RECRUIT_TIME);

		/**
		 * 提升产能资源消耗日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(1), RoleResourceLog.OPERATE_OUT, 1, ResOperateType.UP_PRO_SKILL_OUT.getInfoType(), config.getPrice(), player.account.getChannel()));

		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, config.getPrice(), IronOperateType.UP_PRO_SKILL_OUT.getInfoType()), 1);
		soldier.setEmployeeTimes(soldier.getEmployeeTimes() + 1);
		LevelupRecruitTimeRs.Builder builder = LevelupRecruitTimeRs.newBuilder();
		builder.setResource(player.wrapResourcePb());
		builder.setSoldierType(soldier.getSoldierIndex());
		builder.setRecruitBuyTimes(soldier.getEmployeeTimes());

		handler.sendMsgToPlayer(GameError.OK, LevelupRecruitTimeRs.ext, builder.build());

	}

	/**
	 * Function: 招募士兵
	 */
	public void recruitSoldier(SoldierPb.RecruitSoldierRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null in getSoldierRq.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int soldierIndex = req.getSoldierType();

		// 士兵类型错误
		if (!soldierManager.isSoldierTypeOk(soldierIndex)) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		// 是否有空余的队列
		Map<Integer, Soldier> soldiers = player.getSoldiers();
		Soldier soldier = soldiers.get(soldierIndex);
		if (soldier == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		int soldierType = soldier.getSoldierType();

		// 判断当前士兵数量是否已经满了
		int total = soldierManager.getTotalCapacity(player, soldierType);
		int curCount = soldierManager.getSoldierNum(player, soldierType);
		if (curCount >= total) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_FULL);
			return;
		}

		if (soldierManager.isCampBuilding(soldierIndex, player)) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_IS_BUILDING);
			return;
		}

		// 兵数量
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		int minRecruit = staticLimit.getRecruitMinTime();

		// 读取募兵次数
		int maxRecruit = staticLimit.getRecruitMaxTime(); // 需要配合募兵时长
		Map<Integer, StaticBuySolodierTime> staticBuySolodierTimeMap = staticSoldierMgr.getBuySoldierTime();
		int buyTimes = soldier.getEmployeeTimes();
		// 检查次数
		StaticBuySolodierTime config = staticBuySolodierTimeMap.get(buyTimes);
		if (config != null) {
			maxRecruit = config.getTime();
		}

		// 募兵的速度
		// 活动募兵加速
		// soldierSpeed = 50 25
		// 募兵加速：基础+科技+活动
		long period = req.getPeriod();
		int minutes = (int) (period / TimeHelper.MINUTE_MS); // 招募时长
		double soldierSpeed = soldierManager.getSoldierSpeed(player, soldierType);
		int baseSoldierSpeed = staticLimit.getSoldierSpeed();
		// 招募下限
		double minSoldierCount = Math.floor(baseSoldierSpeed * minRecruit * soldierSpeed);
		// 招募上限
		double maxSoldierCount = Math.ceil(baseSoldierSpeed * maxRecruit * soldierSpeed);

		// soldierSpeed 表示当前的募兵加成
		int count = (int) Math.floor(Math.ceil(baseSoldierSpeed * minutes * soldierSpeed * 10000) / 10000);
		int buf = seasonManager.getBuf(player, EffectType.EFFECT_TYPE32, soldierType);
		count += buf;
		if (count < minSoldierCount || count > maxSoldierCount) {
			handler.sendErrorMsgToPlayer(GameError.RECRUIT_SODIER_COUNT_ERROR);
			return;
		}

		// 建造队列
		LinkedList<WorkQue> workQues = soldier.getWorkQues();
		int workqueSize = workQues.size();

		// 队列
		int totalQueSize = 1 + soldier.getLargerTimes();
		int freeQueSize = totalQueSize - workqueSize;
		// 没有多余的建造队列
		if (freeQueSize <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_FREE_QUEUE_SIZE);
			return;
		}

		// 扣除石油
		int oilCost = staticLimit.getRecruitOilCost() * count;
		int techOil = techManager.getOil(player, soldierType);
		if (techOil != 0) {
			oilCost = techOil * count;
		}
		double seasonBuf = seasonManager.getSeasonBuf(player, EffectType.EFFECT_TYPE12);
		oilCost = (int) (1 - seasonBuf) * oilCost;

		if (player.getOil() < oilCost) {
			handler.sendErrorMsgToPlayer(GameError.OIL_NOT_ENOUGH);
			return;
		}

		/**
		 * 训练士兵消耗资源的日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);

		// 如果是民兵营还需要扣除生铁
		int ironCost = 0;
		if (soldierIndex == SoldierIndex.MILITIA) {
			float factor = (float) staticLimitMgr.getNum(151) / 100.0f;
			ironCost = (int) ((float) oilCost * factor);
			if (player.getIron() < ironCost) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_IRON);
				return;
			}
			playerManager.subAward(player, AwardType.RESOURCE, ResourceType.IRON, ironCost, Reason.RECRUIT_SOLDIER);

			logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.IRON), RoleResourceLog.OPERATE_OUT, ResourceType.IRON, ResOperateType.DRILL_OUT.getInfoType(), ironCost, player.account.getChannel()));
			logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, ironCost, IronOperateType.DRILL_OUT.getInfoType()), ResourceType.IRON);
		}
		// 扣除石油
		playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, oilCost, Reason.RECRUIT_SOLDIER);

		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.OIL), RoleResourceLog.OPERATE_OUT, ResourceType.OIL, ResOperateType.DRILL_OUT.getInfoType(), oilCost, player.account.getChannel()));
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, oilCost, IronOperateType.DRILL_OUT.getInfoType()), ResourceType.OIL);
		// 生成一个建造队列
		WorkQue workQue = new WorkQue();
		workQue.setKeyId(player.maxKey());
		Building buildings = player.buildings;
		Camp camp = buildings.getCamp();
		int buildingId = getBuildingId(soldierIndex, camp);
		workQue.setBuildingId(buildingId);
		workQue.setPeriod(period);
		WorkQue lastque = null;
		if (!workQues.isEmpty()) {
			lastque = workQues.getLast();
		}

		if (lastque == null) {
			workQue.setEndTime(period + System.currentTimeMillis());
		} else {
			workQue.setEndTime(period + lastque.getEndTime());
		}

		workQue.setEmployWork(0);

		Award award = workQue.getAward();
		award.setType(AwardType.SOLDIER);
		award.setId(soldierType);
		award.setCount(count);
		award.setKeyId(0);
		workQue.setOil(oilCost);
		workQue.setIron(ironCost);
		workQues.add(workQue);
		startHireSoldierTask(player, soldierType);
		SoldierPb.RecruitSoldierRs.Builder builder = SoldierPb.RecruitSoldierRs.newBuilder();
		builder.setResource(player.wrapResourcePb());
		builder.setWorkQue(workQue.wrapPb());
		handler.sendMsgToPlayer(SoldierPb.RecruitSoldierRs.ext, builder.build());
		// 触发预备兵营招募任务
		taskManager.doTask(TaskType.ADD_SOILIER_YUBEI, player);
		dailyTaskManager.record(DailyTaskId.TRAIN_SOLDIERS, player, count);
	}

	public int getBuildingId(int soldierIndex, Camp camp) {
		Map<Integer, BuildingBase> campMap = camp.getCamp();
		int currentBuildingType = getBuildingType(soldierIndex);
		for (Map.Entry<Integer, BuildingBase> elem : campMap.entrySet()) {
			BuildingBase buildingBase = elem.getValue();
			if (buildingBase == null) {
				continue;
			}

			int buildingType = staticBuildingMgr.getBuildingType(buildingBase.getBuildingId());
			if (buildingType == Integer.MIN_VALUE) {
				LogHelper.CONFIG_LOGGER.info("ERROR BUILDING TYPE CONFIG");
				continue;
			}

			if (buildingType == currentBuildingType) {
				return buildingBase.getBuildingId();
			}
		}

		return -1;
	}

	public int getBuildingType(int soldierIndex) {
		if (soldierIndex == SoldierType.ROCKET_TYPE) {
			return BuildingType.ROCKET_CAMP;
		} else if (soldierIndex == SoldierType.TANK_TYPE) {
			return BuildingType.TANK_CAMP;
		} else if (soldierIndex == SoldierType.WAR_CAR) {
			return BuildingType.WAR_CAR_CAMP;
		} else if (soldierIndex == SoldierType.MILITIA) {
			return BuildingType.MILITIA_CAMP;
		}
		return -1;
	}

	/**
	 * Function: 招募完成
	 */
	public void recruitDone(SoldierPb.RecruitDoneRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null in getSoldierRq.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int soldierIndex = req.getSoldierType();
		// 士兵类型错误
		if (!soldierManager.isSoldierTypeOk(soldierIndex)) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		// 计算能完成的募兵队列的募兵总数
		// 计算总时长+科技加速
		Map<Integer, Soldier> soldiers = player.getSoldiers();
		Soldier soldier = soldiers.get(soldierIndex);
		if (soldier == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		int soldierType = soldier.getSoldierType();

		// 建造队列
		LinkedList<WorkQue> workQues = soldier.getWorkQues();
		if (workQues.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.WORKQUE_NOT_EXISTS);
			return;
		}

		long now = System.currentTimeMillis();
		SoldierPb.RecruitDoneRs.Builder builder = SoldierPb.RecruitDoneRs.newBuilder();
		List<Award> awards = new ArrayList<Award>();
		if (!workQues.isEmpty()) {
			// 建造队列做成同步的
			Iterator<WorkQue> iter = workQues.iterator();
			while (iter.hasNext()) {
				WorkQue workque = iter.next();
				if (workque == null) {
					continue;
				}

				long endTime = workque.getEndTime();
				if (endTime <= now) {
					if (workque.getAward() != null) {
						awards.add(workque.getAward());
					}
					builder.addWorkQue(workque.wrapPb());
					iter.remove();
				}
			}
		}
		// 奖励
		for (Award award : awards) {
			if (award == null) {
				continue;
			}
			playerManager.addAward(player, award, Reason.RECRUIT_SOLDIER);

			// 更新通行证//日常训练活动进度
			ActivityEventManager.getInst().activityTip(EventEnum.TRAINR, player, award.getCount());
//            activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.HIRE_FREE_SOLDIER_TIMES, award.getCount());
			seasonService.addTreasuryScore(player, GrandType.TYPE_2, 1, award.getCount());// 募兵数量

		}

		// 完成招募士兵的任务
		hireSoldierNum(player, soldierType, soldierManager.getSoldierNum(player, soldierType));
		hireSoldierTimes(player, soldierType);
		builder.setSoldierType(soldierIndex);
		handler.sendMsgToPlayer(SoldierPb.RecruitDoneRs.ext, builder.build());

	}

	@Autowired
	SeasonService seasonService;


	/**
	 * Function: 招募取消
	 */
	public void cancelRecruit(SoldierPb.CancelRecruitRq req, ClientHandler handler) {
		// 增加建造的队列不能取消
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null in getSoldierRq.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int soldierIndex = req.getSoldierType();
		if (!soldierManager.isSoldierTypeOk(soldierIndex)) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		Map<Integer, Soldier> soldiers = player.getSoldiers();
		Soldier soldier = soldiers.get(soldierIndex);
		if (soldier == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		// 建造队列
		LinkedList<WorkQue> workQues = soldier.getWorkQues();
		if (workQues.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_WORKQUE_IS_EMPTY);
			return;
		}

		// 检查keyId的合法性
		if (workQues.size() <= 1) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_WORKQUE_ONLY_ONE);
			return;
		}

		int index = -1;
		int keyId = req.getKeyId();
		for (int i = 0; i < workQues.size(); i++) {
			WorkQue workQue = workQues.get(i);
			if (workQue != null && workQue.getKeyId() == keyId) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_WORKQUE_NOT_EXISTS);
			return;
		}

		// 正在生产的兵营不能取消
		if (index == 0) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_IS_TRAINING);
			return;
		}

		/**
		 * 训练士兵消耗资源的日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		// 扣除石油或者生铁
		WorkQue target = workQues.get(index);
		if (target != null) {
			playerManager.addAward(player, AwardType.RESOURCE, ResourceType.OIL, target.getOil(), Reason.CANCEL_SOLDIER);
			logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, target.getOil(), OilOperateType.DRILL_IN.getInfoType()), ResourceType.OIL);
			if (target.getIron() > 0) {
				playerManager.addAward(player, AwardType.RESOURCE, ResourceType.IRON, target.getIron(), Reason.CANCEL_SOLDIER);
				logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, target.getOil(), IronOperateType.DRILL_IN.getInfoType()), ResourceType.IRON);
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
			builder.addWorkQue(currentWorkQue.wrapPb());
		}
		soldier.checkWorkQues(); // 检查相关逻辑
		builder.setSoldierType(soldierIndex);
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(SoldierPb.CancelRecruitRs.ext, builder.build());
	}

	/**
	 * Function: 招募秒Cd
	 */
	public void buyWorkQueCd(SoldierPb.RecruitWorkQueCdRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检测cost类型是否正确
		int cost = req.getCost();
		if (cost < 1 || cost > 4) {
			handler.sendErrorMsgToPlayer(GameError.BUY_SOLDIER_WORK_CD_COST_ERROR);
			return;
		}

		// 检测工作队列是否存在
		int soldierIndex = req.getSoldierType();

		int propCount = req.getPropCount();// 道具的使用数量
		// 士兵类型错误
		if (!soldierManager.isSoldierTypeOk(soldierIndex)) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		Map<Integer, Soldier> soldiers = player.getSoldiers();
		Soldier soldier = soldiers.get(soldierIndex);
		if (soldier == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		// 建造队列
		LinkedList<WorkQue> workQues = soldier.getWorkQues();
		if (workQues.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_WORKQUE_IS_EMPTY);
			return;
		}

		// 第一个是建造队列，其他都是等待队列
		WorkQue workQue = workQues.getFirst();
		if (workQue == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_WORKQUE_IS_EMPTY);
			return;
		}

		// 根据不同的消费类型来判断秒升级建筑
		if (cost == 1) {
			goldBuyCd(player, workQue, workQues, handler);
		} else if (cost == 2) {
			itemBuyCd(player, req.getPropId(), workQue, workQues, propCount, handler);
		} else if (cost == 3) {
			buyItemAndUse(player, req.getPropId(), workQue, workQues, handler);
		} else if (cost == 4) {// 参与虫族加速活动
			zergAccelerateBuyCd(player, workQue, workQues, handler);
		} else {
			handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
			return;
		}

	}

	// 金币招募秒cd
	public void goldBuyCd(Player player, WorkQue workQue, LinkedList<WorkQue> workQues, ClientHandler handler) {
		// 检查消耗
		long endTime = workQue.getEndTime();
		long minutes = TimeHelper.getTotalMinute(endTime);
		if (minutes <= 0) {
			handler.sendErrorMsgToPlayer(GameError.WORKQUE_NOT_EXISTS);
			return;
		}

		// 秒Cd消耗
		int buildCdLimit = staticLimitMgr.getBuildTimePrice();
		int needGold = (int) minutes * buildCdLimit;
		int owned = player.getGold();
		if (owned < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// 删除当前workque,后面的时间向前面挪
		handleRemoveWorkQue(workQues);

		soldierManager.checkWorkQues(workQues, 2);
		// 扣除金币
		playerManager.subAward(player, AwardType.GOLD, 1, needGold, Reason.KILL_RECRUIT_CD);

		SoldierPb.RecruitWorkQueCdRs.Builder builder = SoldierPb.RecruitWorkQueCdRs.newBuilder();
		builder.setGold(player.getGold());
		for (WorkQue workQueElem : workQues) {
			builder.addWorkQue(workQueElem.wrapPb());
		}

		handler.sendMsgToPlayer(SoldierPb.RecruitWorkQueCdRs.ext, builder.build());
		eventManager.useSpeed(player, Lists.newArrayList("募兵", minutes, owned, ""));
	}

	// 道具招募秒cd
	public void itemBuyCd(Player player, int itemId, WorkQue workQue, LinkedList<WorkQue> workQues, int propCount, ClientHandler handler) {
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

		// 可以减少的分钟数
		long reduceTimeMs = param.get(2).intValue() * TimeHelper.SECOND_MS * propCount;
		// 如果能一次性秒完，则需要删除队列，如果没有秒完，则发当前队列
		// 检查消耗
		long endTime = workQue.getEndTime();
		long diff = endTime - reduceTimeMs;
		long now = System.currentTimeMillis();
		if (diff <= now) {
			// 说明全部秒完了，后面的要往前面挪
			handleRemoveWorkQue(workQues);

		} else {
			// 说明没有全部秒完
			workQue.setEndTime(diff);
			handleMoveTime(workQues);
		}

		soldierManager.checkWorkQues(workQues, 3);

		playerManager.subAward(player, AwardType.PROP, itemId, propCount, Reason.KILL_RECRUIT_CD);

		SoldierPb.RecruitWorkQueCdRs.Builder builder = SoldierPb.RecruitWorkQueCdRs.newBuilder();
		for (WorkQue workQueElem : workQues) {
			builder.addWorkQue(workQueElem.wrapPb());
		}

		builder.setProp(item.wrapPb());

		handler.sendMsgToPlayer(SoldierPb.RecruitWorkQueCdRs.ext, builder.build());
		eventManager.useSpeed(player, Lists.newArrayList("募兵", diff, 0, staticProp.getPropName()));
	}

	// 虫族加速活动减少建筑时长
	public void zergAccelerateBuyCd(Player player, WorkQue workQue, LinkedList<WorkQue> workQues, ClientHandler handler) {

		if (workQue.getActivityDerateCD() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
			return;
		}
		// 可以减少的分钟数
		long reduceTimeMs = 300000;// 固定减少5分钟
		// 如果能一次性秒完，则需要删除队列，如果没有秒完，则发当前队列
		// 检查消耗
		long endTime = workQue.getEndTime();
		long diff = endTime - reduceTimeMs;
		long now = System.currentTimeMillis();
		if (diff <= now) {
			// 说明全部秒完了，后面的要往前面挪
			workQue.setActivityDerateCD(0);
			handleRemoveWorkQue(workQues);

		} else {
			// 说明没有全部秒完
			workQue.setEndTime(diff);
			workQue.setActivityDerateCD(0);
			handleMoveTime(workQues);
		}

		soldierManager.checkWorkQues(workQues, 3);

		SoldierPb.RecruitWorkQueCdRs.Builder builder = SoldierPb.RecruitWorkQueCdRs.newBuilder();
		for (WorkQue workQueElem : workQues) {
			builder.addWorkQue(workQueElem.wrapPb());
		}

		handler.sendMsgToPlayer(SoldierPb.RecruitWorkQueCdRs.ext, builder.build());
	}

	public void handleRemoveWorkQue(LinkedList<WorkQue> workQues) {
		// 后面的que往前面挪
		if (!workQues.isEmpty()) {
			long now = System.currentTimeMillis();
			WorkQue firstElem = workQues.getFirst();
			if (firstElem != null) {
				firstElem.setEndTime(now);
			}
			firstElem.setActivityDerateCD(0);
			// 时间向前面挪
			handleMoveTime(workQues);
		}

	}

	// 后面的que时间往前面挪
	public void handleMoveTime(LinkedList<WorkQue> workQues) {
		for (int index = 1; index < workQues.size(); index++) {
			WorkQue preElem = workQues.get(index - 1);
			WorkQue curElem = workQues.get(index);
			if (curElem == null)
				continue;
			curElem.setEndTime(preElem.getEndTime() + curElem.getPeriod());
		}
	}

	public void buyItemAndUse(Player player, int itemId, WorkQue workQue, LinkedList<WorkQue> workQues, ClientHandler handler) {

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

		// 可以减少的分钟数(毫秒)
		long reduceTimeMs = param.get(2).intValue() * TimeHelper.SECOND_MS;
		// 检查这个物品需要多少钱
		int needGold = staticProp.getPrice();
		int owned = player.getGold();
		if (owned < needGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		long endTime = workQue.getEndTime();
		long diff = endTime - reduceTimeMs;
		long now = System.currentTimeMillis();
		if (diff <= now) {
			// 说明全部秒完了，后面的要往前面挪
			handleRemoveWorkQue(workQues);
		} else {
			// 说明没有全部秒完
			workQue.setEndTime(diff);
			handleMoveTime(workQues);
		}

		soldierManager.checkWorkQues(workQues, 1);
		// 扣除金币
		playerManager.subAward(player, AwardType.GOLD, 1, needGold, Reason.KILL_RECRUIT_CD);

		SoldierPb.RecruitWorkQueCdRs.Builder builder = SoldierPb.RecruitWorkQueCdRs.newBuilder();
		builder.setGold(player.getGold());
		for (WorkQue workQueElem : workQues) {
			builder.addWorkQue(workQueElem.wrapPb());
		}
		handler.sendMsgToPlayer(SoldierPb.RecruitWorkQueCdRs.ext, builder.build());
		eventManager.useSpeed(player, Lists.newArrayList("募兵", diff, needGold, staticProp.getPropName()));
	}

	// 点击按钮一次
	public void startHireSoldierTask(Player player, int soldierType) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(soldierType);
		taskManager.doTask(TaskType.START_HIRE_SOLDIER, player, triggers);
	}

	// 招募兵x个(param: 兵类型, 兵数)
	public void hireSoldierNum(Player player, int soldierType, int solderNum) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(soldierType);
		triggers.add(solderNum);
		taskManager.doTask(TaskType.HIRE_SOLDIER_NUM, player, triggers);
	}

	// 招募兵x次(param: 兵类型)(最大进度表示次数)
	public void hireSoldierTimes(Player player, int soldierType) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(soldierType);
		taskManager.doTask(TaskType.HIRE_SOLDIER_TIMES, player, triggers);
		taskManager.doTask(TaskType.HIRE_SOLDIER_TIMES_ANY, player, null);
	}

	public void primarySoldierSpeed(SoldierPb.PrimarySoldierSpeedRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检测工作队列是否存在
		int soldierType = req.getSoldierType();
		// 士兵类型错误
		if (soldierType < SoldierType.ROCKET_TYPE || soldierType > SoldierType.WAR_CAR) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		Map<Integer, Soldier> soldiers = player.getSoldiers();
		Soldier soldier = soldiers.get(soldierType);
		if (soldier == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
			return;
		}

		// 生产队列
		LinkedList<WorkQue> workQues = soldier.getWorkQues();
		if (workQues.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_WORKQUE_IS_EMPTY);
			return;
		}

		// 第一个是建造队列，其他都是等待队列
		WorkQue workQue = workQues.getFirst();
		if (workQue == null) {
			handler.sendErrorMsgToPlayer(GameError.SOLDIER_WORKQUE_IS_EMPTY);
			return;
		}

		// 检查消耗
		long endTime = workQue.getEndTime();
		long time = workQue.getPrimarySpeed();
		if (time <= 0) {
			handler.sendErrorMsgToPlayer(GameError.WORKQUE_CAN_NOT_SPEED);
			return;
		}

		workQue.setPrimarySpeed(0L);
		long diff = endTime - time;
		long now = System.currentTimeMillis();
		if (diff <= now) {
			// 说明全部秒完了，后面的要往前面挪
			handleRemoveWorkQue(workQues);
		} else {
			// 说明没有全部秒完
			workQue.setEndTime(diff);
			handleMoveTime(workQues);
		}

		soldierManager.checkWorkQues(workQues, 4);

		SoldierPb.PrimarySoldierSpeedRs.Builder builder = SoldierPb.PrimarySoldierSpeedRs.newBuilder();
		for (WorkQue workQueElem : workQues) {
			builder.addWorkQue(workQueElem.wrapPb());
		}

		handler.sendMsgToPlayer(SoldierPb.PrimarySoldierSpeedRs.ext, builder.build());

	}

}
