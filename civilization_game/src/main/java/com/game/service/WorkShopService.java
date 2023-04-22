package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticWorkShopMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticWorkShop;
import com.game.domain.s.StaticWorkShopBuy;
import com.game.log.constant.CopperOperateType;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.WorkShopPb;
import com.game.pb.WorkShopPb.*;
import com.game.season.SeasonManager;
import com.game.season.talent.entity.EffectType;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class WorkShopService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private WorkShopMgr workShopMgr;

	@Autowired
	private CondMgr condDataMgr;

	@Autowired
	private ItemManager itemManager;

	@Autowired
	private StaticWorkShopMgr staticWorkShopMgr;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private StaticPropMgr staticPropMgr;

	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	SeasonManager seasonManager;


	public boolean isWorkShopLvOk(int workShopLv, int quality) {
		if (workShopLv == 1 && quality >= Quality.GOLD.get()) {
			return false;
		} else if (workShopLv == 2 && quality >= Quality.RED.get()) {
			return false;
		} else if (workShopLv == 3 && quality >= Quality.PURPLE.get()) {
			return false;
		} else if (workShopLv <= 0 || workShopLv > 3) {
			return false;
		} else if (quality <= Quality.BLUE.get() || quality > Quality.PURPLE.get()) {
			return false;
		}
		return true;
	}

	// 生产道具请求
	public void makePropBegin(MakePropRq req, ClientHandler handler) {
		// 检查是否有可以升级的建筑
		//LogHelper.GAME_DEBUG.error("开始生产一个道具....");
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int quality = req.getQuality();

		//LogHelper.GAME_DEBUG.error("生产的材料品质为: " + quality);

		// 检查材料品质是否ok
		if (!workShopMgr.isQualityOk(quality)) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_QUALITY_ERROR);
			return;
		}

		int propId = req.getPropId();
		StaticProp paperConfig = staticPropMgr.getStaticProp(propId);
		if (paperConfig == null) {
			LogHelper.CONFIG_LOGGER.info("propId = " + propId + " config is not exist!");
			return;
		}

		// 必须是图纸[防止外挂]
		if (paperConfig.getPropType() != ItemType.EQUIP_PAPER) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_TYPE_ERROR);
			return;
		}

		//LogHelper.GAME_DEBUG.error("使用的图纸Id: " + propId);

		// 检查使用的图纸的品质是否ok
		// 绿色图纸分解成金色材料
		// 图纸品质+1 = 材料品质
		if (!workShopMgr.isQualityOk(propId, quality)) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_PROP_QUALITY_ERROR);
			return;
		}

		// 检查物品数量是否ok
		if (!workShopMgr.isItemNumOk(player, propId)) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		int workShopLv = workShopMgr.getWorkShopLv(player);

		StaticWorkShop staticWorkShop = workShopMgr.getStaticWorkShop(workShopLv);
		if (staticWorkShop == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		if (!isWorkShopLvOk(workShopLv, quality)) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_QUALITY_ERROR);
			return;
		}

		// 检查司令部等级是否ok
		if (!workShopMgr.isCommandLvOk(player, staticWorkShop)) {
			handler.sendErrorMsgToPlayer(GameError.COMMAND_LV_NOT_ENOUGH);
			return;
		}

		// 检查指挥官等级ok
		if (!workShopMgr.isLordLvOk(player, staticWorkShop)) {
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}

		// 升级建筑的时候不能生产
		if (!workShopMgr.workShopcanMake(player)) {
			handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_CAN_NOT_MAKE);
			return;
		}

		// 检查资源是否ok
		int level = workShopMgr.getLevel(quality);//判斷是什麽材質的材料
		StaticWorkShop staticWorkShop1 = workShopMgr.getStaticWorkShop(level);//通過level找到材料品質
		List<List<Long>> resourceCond = staticWorkShop1.getResource();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		// 检查生产队列序号
		int totalNum = workShopMgr.getTotalQue(player);

		// 检查队列是否已满
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}

		WorkShop workShop = building.getWorkShop();
		if (workShop == null) {
			handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_IS_NULL);
			return;
		}

		Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
		int workQueNum = workShopMgr.getWorkQueNum(workQues);
		//超过最大
		if (totalNum < workQueNum + 1) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}
		int index = workShopMgr.getMinWorkShop(workShop, totalNum);
		if (index == 0) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_WORKQUE_EXISTS);
			return;
		}

		// 掉落, 应该根据品质来获取
		int qualityLevel = workShopMgr.getLevel(quality);
		StaticWorkShop qualityConfig = staticWorkShopMgr.getWorkShop(qualityLevel);
		if (qualityConfig == null) {
			LogHelper.CONFIG_LOGGER.info("quality config is null!");
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		List<Award> awards = workShopMgr.lootAwardList(player, qualityConfig);
		if (awards == null) {
			LogHelper.CONFIG_LOGGER.info("awards is null!");
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_AWARD_ERROR);
			return;
		}

		if (awards.size() != 1) {
			LogHelper.CONFIG_LOGGER.info("awards.size() > 1");
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_AWARD_ERROR);
			return;
		}

		// 获取当前应该减少的时间
		long deltaTime = workShopMgr.getTimeDelta(player, workQueNum + 1);
		//LogHelper.GAME_DEBUG.error("获取当前人口减少的时间: " + deltaTime);

		// 检查之前的建筑队列
		workShopMgr.checkWorkQue(workShop, deltaTime, player);
		// 生成生产队列
		WsWorkQue workQue = new WsWorkQue();
		//LogHelper.GAME_DEBUG.error("开始创建一个生产队列...");
		long now = System.currentTimeMillis();

		long configTime = workShopMgr.getTime(staticWorkShop, quality);
		//LogHelper.GAME_DEBUG.error("生产当前道具的时间 (小时): " + configTime/ TimeHelper.HOUR_MS);
		long time = configTime - deltaTime;
		long resTime = workShopMgr.getWorkTime(time, player);
		resTime = Math.max(0, resTime);
		long workQueTime = now + resTime;
		//LogHelper.GAME_DEBUG.error("当前时间为: " + now);
		//LogHelper.GAME_DEBUG.error("生产队列的时间为: " + workQueTime);
		workQue.setKeyId(player.maxKey());
		workQue.setIndex(index);
		workQue.setEndTime(workQueTime);
		workQue.setConfigTime(configTime);
		workQue.setReduceTime(deltaTime);

		// 当前生产队列的实际period
		long period = workQueTime - now;
		period = Math.max(0, period);
		workQue.setPeriod(period);
		//LogHelper.GAME_DEBUG.error("生产队列的实际period为: " + period);

		Award award = awards.get(0);
		int buf = seasonManager.getBuf(player, EffectType.EFFECT_TYPE31);
		award.setCount(award.getCount() + buf);
		workQue.setAward(award);
		workQues.put(workQue.getKeyId(), workQue);

		// 扣除图纸和资源
		playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.MAKE_PROP);
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}
			// 资源类型
			int awardType = item.get(0).intValue();
			int resType = item.get(1).intValue();
			Long res = item.get(2);
			playerManager.subAward(player, awardType, resType, res, Reason.LEVEL_UP_BUILDING);

			/**
			 * 作坊(材料工厂)消耗资源日志埋点
			 */
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			if (awardType == AwardType.RESOURCE) {
				logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
					player.account.getCreateDate(),
					player.getLevel(),
					player.getNick(),
					player.getVip(),
					player.getCountry(),
					player.getTitle(),
					player.getHonor(),
					player.getResource(resType),
					RoleResourceLog.OPERATE_OUT, resType, ResOperateType.WORKSHOP_MAKE_OUT.getInfoType(), res, player.account.getChannel()));
				int t = 0;
				switch (resType) {
					case ResourceType.IRON:
						t = IronOperateType.WORKSHOP_MAKE_OUT.getInfoType();
						break;
					case ResourceType.COPPER:
						t = CopperOperateType.WORKSHOP_MAKE_OUT.getInfoType();
						break;
					default:
						break;
				}
				if (t != 0) {
					logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
						player.getNick(),
						player.getLevel(),
						player.getTitle(),
						player.getHonor(),
						player.getCountry(),
						player.getVip(),
						player.account.getChannel(),
						1, res, t), resType);
				}
			}

		}

		// 生产队列
		MakePropRs.Builder builder = MakePropRs.newBuilder();
		for (Map.Entry<Integer, WsWorkQue> elem : workQues.entrySet()) {
			if (elem == null) {
				continue;
			}

			WsWorkQue workQueElem = elem.getValue();
			if (workQueElem == null) {
				continue;
			}

			builder.addWorkQue(workQueElem.wrapPb());
		}

		// 预设队列
		Map<Integer, WsWaitQue> WsWaitQueMap = workShop.getWaitQues();
		for (WsWaitQue wsWaitQueElem : WsWaitQueMap.values()) {
			if (wsWaitQueElem == null) {
				continue;
			}

			builder.addWaitQue(wsWaitQueElem.wrapPb());
		}

		Item item = player.getItem(propId);
		if (item != null) {
			builder.setProp(item.wrapPb());
		}

		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(MakePropRs.ext, builder.build());

		// 完成一次打造, 检查获得的材料的品质
		int itemId = award.getId();
		StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
		if (staticProp != null) {
			doMakePropTask(player, staticProp.getColor());
		}

		//更新通行证活动进度
		ActivityEventManager.getInst().activityTip(EventEnum.WORKS_PRODUCE, player, 1, 0);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MAKE_PROP_IN_WORKSHOP, 1);
//        activityManager.updActPerson(player, ActivityConst.ACT_SQUA, 1, NineCellConst.CELL_9);
		eventManager.makeProp(player, Lists.newArrayList(
			paperConfig.getPropName(),
			staticProp.getPropName()
		));
	}

	// 生产完成
	public void makePropDone(MakeDoneRq req, ClientHandler handler) {
		// 生产完成，需要发送keyId,然后删除队列
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int keyId = req.getKeyId();
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}

		// 获取作坊
		WorkShop workShop = building.getWorkShop();
		if (workShop == null) {
			handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_IS_NULL);
			return;
		}

		Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();

		WsWorkQue workQue = workQues.get(keyId);
		if (workQue == null || workQue.getIndex() != 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_WORKSHOP_WORKQUE);
			return;
		}

		// 检查时间是否到
		long now = System.currentTimeMillis();
		if (workQue.getEndTime() > now) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_TIME_IS_NOT_UP);
			return;
		}

		Award award = workQue.getAward();
		if (award == null) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_AWARD_NULL);
			return;
		}

		playerManager.addAward(player, award, Reason.MAKE_PROP);
		workQues.remove(workQue.getKeyId());
		MakeDoneRs.Builder builder = MakeDoneRs.newBuilder();
		builder.setWorkQue(workQue.wrapPb());
		handler.sendMsgToPlayer(MakeDoneRs.ext, builder.build());

		// 完成一次打造, 检查获得的材料的品质
		int itemId = award.getId();
		StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
		if (staticProp != null) {
			doMakePropTask(player, staticProp.getColor());
		}
		//更新材料生产活动
		activityManager.updateOrder(player, award.getCount(), staticProp);
	}


	// 生产完成
	public void makePropAllDone(WorkShopPb.MakeAllDoneRq req, ClientHandler handler) {
		// 生产完成，需要发送keyId,然后删除队列
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}

		// 获取作坊
		WorkShop workShop = building.getWorkShop();
		if (workShop == null) {
			handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_IS_NULL);
			return;
		}
		Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
		WorkShopPb.MakeAllDoneRs.Builder builder = WorkShopPb.MakeAllDoneRs.newBuilder();
		for (Map.Entry<Integer, WsWorkQue> entry : workQues.entrySet()) {
			WsWorkQue workQue = entry.getValue();
			if (workQue == null) {
				continue;
			}
			// 检查时间是否到
			long now = System.currentTimeMillis();
			if (workQue.getEndTime() > now || workQue.getIndex() != 0) {
				continue;
			}
			Award award = workQue.getAward();
			if (award == null) {
				continue;
			}
			playerManager.addAward(player, award, Reason.MAKE_PROP);
			workQues.remove(workQue.getKeyId());
			builder.addWorkQue(workQue.wrapPb());
			// 完成一次打造, 检查获得的材料的品质
			int itemId = award.getId();
			StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
			if (staticProp != null) {
				doMakePropTask(player, staticProp.getColor());
			}
			//更新材料生产活动
			activityManager.updateOrder(player, award.getCount(), staticProp);
		}
		handler.sendMsgToPlayer(WorkShopPb.MakeAllDoneRs.ext, builder.build());
	}

	public void doMakePropTask(Player player, int quality) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(quality);
		taskManager.doTask(TaskType.MAKE_PROP, player, triggers);

		//生产的是蓝色及以上品质材料，做订单活动统计

	}

	public void buyWorkPropQue(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 当前购买次数
		Lord lord = player.getLord();
		int buyTimes = playerManager.buyWorkTimes(player);
		Map<Integer, StaticWorkShopBuy> staticWorkShopBuyMap = staticWorkShopMgr.getWorkShopBuyMap();
		if (staticWorkShopBuyMap == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int maxBuyTimes = staticWorkShopBuyMap.size();
		if (buyTimes >= maxBuyTimes) {
			handler.sendErrorMsgToPlayer(GameError.REACH_MAX_BUY_WORKSHOP_QUE_TIMES);
			return;
		}

		int nexTimes = buyTimes + 1;
		StaticWorkShopBuy staticWorkShopBuy = staticWorkShopBuyMap.get(nexTimes);
		if (staticWorkShopBuy == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int price = staticWorkShopBuy.getPrice();
		if (price <= 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		if (lord.getGold() < price) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		playerManager.subAward(player, AwardType.GOLD, 1, price, Reason.BUY_PROP_WORKQUE);
		lord.setBuyWorkShopQue(nexTimes);
		WorkShopPb.BuyQueRs.Builder builder = WorkShopPb.BuyQueRs.newBuilder();
		builder.setGold(lord.getGold());
		builder.setBuyTimes(nexTimes);
		handler.sendMsgToPlayer(WorkShopPb.BuyQueRs.ext, builder.build());
		eventManager.buyQue(player, Lists.newArrayList(
			price
		));
	}

	// 每次生产队列变化的时候，预设队列也要发生变化,用定时器做
	public void prePropMake(PreMakeRq req, ClientHandler handler) {
		// 检查是否有可以升级的建筑
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 购买7级礼包才开放预设功能
		List<Integer> vipGifts = player.getVipGifts();
		if (!vipGifts.contains(7)) {
			handler.sendErrorMsgToPlayer(GameError.NO_BUY_VIP7_GIFTS);
			return;
		}

		int index = req.getIndex();
		int quality = req.getQuality();

		// 检查材料品质是否ok
		if (!workShopMgr.isQualityOk(quality)) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_QUALITY_ERROR);
			return;
		}

		int propId = req.getPropId();
		// 检查使用的图纸的品质是否ok
		if (!workShopMgr.isQualityOk(propId, quality)) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_PROP_QUALITY_ERROR);
			return;
		}

		// 检查物品数量是否ok
		if (!workShopMgr.isItemNumOk(player, propId)) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		// 检查生产队列是否存在
		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}

		// 获取作坊
		WorkShop workShop = building.getWorkShop();
		if (workShop == null) {
			handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_IS_NULL);
			return;
		}
		WsWorkQue workQue = workShopMgr.getWorkShop(workShop, index);
		if (workQue == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_WORKSHOP_WORKQUE);
			return;
		}

		// 检查等待队列已经存在
		Map<Integer, WsWaitQue> waitQues = workShop.getWaitQues();
		if (waitQues.containsKey(index)) {
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_WAITQUE_EXISTS);
			return;
		}

		// 获取作坊等级
		int workShopLv = workShopMgr.getLevel(quality);
		StaticWorkShop staticWorkShop = workShopMgr.getStaticWorkShop(workShopLv);
		if (staticWorkShop == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 掉落
		List<Award> awards = workShopMgr.lootAwardList(player, staticWorkShop);
		if (awards == null) {
			LogHelper.CONFIG_LOGGER.info("awards is null!");
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_AWARD_ERROR);
			return;
		}

		if (awards.size() != 1) {
			LogHelper.CONFIG_LOGGER.info("awards.size() != 1");
			handler.sendErrorMsgToPlayer(GameError.WORKSHOP_AWARD_ERROR);
			return;
		}

		// 检查资源是否ok
		List<List<Long>> resourceCond = staticWorkShop.getResource();
		for (List<Long> item : resourceCond) {
			GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
			if (gameError != GameError.OK) {
				handler.sendErrorMsgToPlayer(gameError);
				return;
			}
		}

		WsWaitQue waitQue = new WsWaitQue();
		waitQue.setIndex(index);
		waitQue.setStartTime(workQue.getEndTime());
		Award award = awards.get(0);
		waitQue.setAward(award);
		waitQues.put(index, waitQue);

		// 扣除图纸和资源
		playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.MAKE_PROP);
		// 扣除资源
		for (List<Long> item : resourceCond) {
			if (item.size() != 3) {
				continue;
			}

			// 资源类型
			int awardType = item.get(0).intValue();
			int resType = item.get(1).intValue();
			Long res = item.get(2);
			playerManager.subAward(player, awardType, resType, res, Reason.LEVEL_UP_BUILDING);
		}

		PreMakeRs.Builder builder = PreMakeRs.newBuilder();
		builder.setWorkQue(waitQue.wrapPb());
		Item item = player.getItem(propId);
		if (item != null) {
			builder.setProp(item.wrapPb());
		}
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(PreMakeRs.ext, builder.build());
	}

	// 获取所有的队列
	public void getWsQue(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Building building = player.buildings;
		if (building == null) {
			handler.sendErrorMsgToPlayer(GameError.BUILDING_NULL);
			return;
		}

		WorkShop workShop = building.getWorkShop();
		if (workShop == null) {
			handler.sendErrorMsgToPlayer(GameError.WORK_SHOP_IS_NULL);
			return;
		}

		Map<Integer, WsWorkQue> wsWorkQueMap = workShop.getWorkQues();
		Map<Integer, WsWaitQue> waitQueMap = workShop.getWaitQues();
		WorkShopPb.GetWsQueRs.Builder builder = WorkShopPb.GetWsQueRs.newBuilder();
		long now = System.currentTimeMillis();
		for (WsWorkQue wsWorkQue : wsWorkQueMap.values()) {
			if (wsWorkQue == null) {
				continue;
			}

			if (wsWorkQue.getIndex() == 0 &&
				wsWorkQue.getEndTime() > now) {
				wsWorkQue.setEndTime(now);
			}

			builder.addWorkQue(wsWorkQue.wrapPb());
		}

		for (WsWaitQue wsWaitQue : waitQueMap.values()) {
			if (wsWaitQue == null || wsWaitQue.getStartTime() < now) {
				continue;
			}

			builder.addWaitQue(wsWaitQue.wrapPb());
		}

		handler.sendMsgToPlayer(WorkShopPb.GetWsQueRs.ext, builder.build());
	}

	// 检查替换队列
	public void checkWsQue() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		long now = System.currentTimeMillis();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}
			checkPlayerWsQue(player, now);

		}
	}

	public void checkPlayerWsQue(Player player, long now) {
		Building building = player.buildings;
		if (building == null) {
			return;
		}
		WorkShop workShop = building.getWorkShop();
		if (workShop == null) {
			return;
		}
		// 1.检查生产队列时候完成
		// 2.如果完成, 检查预设队列
		// 3.如果有预设队列，生成新的工作队列
		Map<Integer, WsWorkQue> wsWorkQueMap = workShop.getWorkQues();
		if (wsWorkQueMap.isEmpty()) {
			return;
		}
		Iterator<WsWorkQue> wsWorkQueIt = wsWorkQueMap.values().iterator();
		boolean needSyn = false;
		while (wsWorkQueIt.hasNext()) {
			WsWorkQue wsWorkQue = wsWorkQueIt.next();
			if (wsWorkQue == null || wsWorkQue.getEndTime() > now || wsWorkQue.getIndex() == 0) {
				continue;
			}
			needSyn = true;
			wsWorkQue.setIndex(0);
		}
		Map<Integer, WsWaitQue> wsWaitQueMap = workShop.getWaitQues();
		Iterator<WsWaitQue> iterator = wsWaitQueMap.values().iterator();
		while (iterator.hasNext()) {
			WsWaitQue next = iterator.next();
			WsWorkQue wsWorkQue = wsWorkQueMap.values().stream().filter(x -> x.getIndex() == next.getIndex()).findAny().orElse(null);
			if (wsWorkQue == null) {
				WsWorkQue addWsWorkQue = createWorkQue(player, next, now);
				if (addWsWorkQue != null) {
					iterator.remove();
				}
			}
		}
		// 同步所有的队列
		if (needSyn) {
			SynGetWsQueRq.Builder builder = SynGetWsQueRq.newBuilder();
			// 生产队列
			for (WsWorkQue wsWorkQueELem : wsWorkQueMap.values()) {
				if (wsWorkQueELem == null) {
					continue;
				}

				builder.addWorkQue(wsWorkQueELem.wrapPb());
			}
			// 预设队列
			// Map<Integer, WsWaitQue> wsWaitQueMap = workShop.getWaitQues();
			for (WsWaitQue wsWaitQueElem : wsWaitQueMap.values()) {
				if (wsWaitQueElem == null) {
					continue;
				}

				builder.addWaitQue(wsWaitQueElem.wrapPb());
			}
			SynHelper.synMsgToPlayer(player, SynGetWsQueRq.EXT_FIELD_NUMBER, SynGetWsQueRq.ext, builder.build());
		}
	}

	public WsWorkQue createWorkQue(Player player, WsWaitQue wsWaitQue, long now) {
		Building building = player.buildings;
		if (building == null) {
			LogHelper.CONFIG_LOGGER.info("building == null");
			return null;
		}

		WorkShop workShop = building.getWorkShop();
		if (workShop == null) {
			LogHelper.CONFIG_LOGGER.info("workShop == null");
			return null;
		}

		int workShopLv = workShop.getLv();
		StaticWorkShop staticWorkShop = workShopMgr.getStaticWorkShop(workShopLv);
		if (staticWorkShop == null) {
			LogHelper.CONFIG_LOGGER.info("createWorkQue staticWorkShop == null");
			return null;
		}

		if (wsWaitQue == null) {
			LogHelper.CONFIG_LOGGER.info("createWorkQue wsWaitQue == null");
			return null;
		}

		// 检查wsWaitQue的品质
		Award award = wsWaitQue.getAward();
		if (award == null) {
			LogHelper.CONFIG_LOGGER.info("createWorkQue award == null");
			return null;
		}

		WsWorkQue workQue = new WsWorkQue();
		workQue.setKeyId(player.maxKey());
		int itemId = award.getId();
		int quality = itemManager.getQuality(itemId);

		Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
		int workQueNum = workShopMgr.getWorkQueNum(workQues);
		// 获取当前减少的时间[人口减少的时间]
		long deltaTime = workShopMgr.getTimeDelta(player, workQueNum + 1);
		// 检查之前的建筑队列
		workShopMgr.checkWorkQue(workShop, deltaTime, player);

		long configTime = workShopMgr.getTime(staticWorkShop, quality);
		long time = configTime - deltaTime;
		long resTime = workShopMgr.getWorkTime(time, player);
		resTime = Math.max(0, resTime);
		long workQueTime = now + resTime;
		workQue.setIndex(wsWaitQue.getIndex());
		workQue.setEndTime(workQueTime);
		workQue.setConfigTime(configTime);
		// 当前生产队列的实际period
		long period = workQueTime - now;
		period = Math.max(0, period);
		workQue.setPeriod(period);
		workQue.setAward(award);
		workQue.setReduceTime(deltaTime);

		workQues.put(workQue.getKeyId(), workQue);

		//更新通行证活动进度
		activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MAKE_PROP_IN_WORKSHOP, 1);
		return workQue;
	}

}
