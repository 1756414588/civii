package com.game.activity;

import com.game.activity.actor.CommonTipActor;
import com.game.activity.actor.GameEventActor;
import com.game.activity.define.EventEnum;
import com.game.activity.events.*;
import com.game.activity.facede.IActivityActor;
import com.game.activity.facede.IActivityEventRet;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.manager.ActivityManager;
import com.game.spring.SpringUtil;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * 活动事件管理器
 */
public class ActivityEventManager {

	private static ActivityEventManager inst = new ActivityEventManager();

	// 事件类型,活动ID,事件处理器
	private static HashBasedTable<EventEnum, Integer, BiConsumer<EventEnum, IActivityActor>> events = HashBasedTable.create();

	// 事件类型,事件处理器
	private static ConcurrentHashMap<EventEnum, List<BiConsumer<EventEnum, IActivityActor>>> handlers = new ConcurrentHashMap<>();

	public static ActivityEventManager getInst() {
		return inst;
	}

	public void listen() {
		ActRewardTipEvent.getInst().register();
		HopeTipEvent.getInst().register();
		SevenDayTipEvent.getInst().register();
		PayFristDisappearEvent.getInst().register();
		AwardThenRemoveTipEvent.getInst().register();
		GrowFootTipEvent.getInst().register();
		ServerCensusTipEvent.getInst().register();
		ServerCensusPersonTipEvent.getInst().register();
		InvestTipEvent.getInst().register();
		ActCampMembersTipEvent.getInst().register();
		RankHistoryTipEvent.getInst().register();
		RankLastTipEvent.getInst().register();
		RankTipEvent.getInst().register();
		ActBuildingLevelTipEvent.getInst().register();
		ActWashEquipTipEvent.getInst().register();
		ActGrandTotalTipEvent.getInst().register();
		ActDailyTrainrsEvent.getInst().register();
		ActKillAllEvent.getInst().register();
		ActMonsterEvent.getInst().register();
		ActSceneCityEvent.getInst().register();
		ActWorldBattleEvent.getInst().register();
		ActRecharDialTipEvent.getInst().register();
		ActRaidersTipEvent.getInst().register();
		ActPassPortTipEvent.getInst().register();
		ActCostPersonTipEvent.getInst().register();
		ActTurntableTipEvent.getInst().register();
		ActWeelCrownThreeTipEvent.getInst().register();
		ActDailyMissionTipEvent.getInst().register();
		ActSevenTipEvent.getInst().register();
		ActTaskHeroTipEvent.getInst().register();
		ActSquaTipEvent.getInst().register();
		ActHeroWashTipEvent.getInst().register();
		ActOrderTipEvent.getInst().register();
		ActDaylyExpeditionTipEvent.getInst().register();
		ActCostRankTipEvent.getInst().register();
		ActSynAndTipEvent.getInst().register();
		ActZeroGiftTipEvent.getInst().register();
		ActArmsPayTipEvent.getInst().register();
		ActSevenRechargeTipEvent.getInst().register();
		ActTopupPersonTipEvent.getInst().register();
		ActHeroDialTipEvent.getInst().register();
		ActMedalExchangeTipEvent.getInst().register();
		ActSearchTipEvent.getInst().register();
		ActDragonBoatTipEvent.getInst().register();
		SevenPMTipEvent.getInst().register();
		ActLuckDialTipEvent.getInst().register();
		ActCloseByTimeEvent.getInst().register();
		ActBroodTipEvent.getInst().register();
		ActSpringFestivalEvent.getInst().register();
		ActTDSevenTaskEvent.getInst().register();
	}

	public void addEvent(EventEnum event, int activityId, BiConsumer<EventEnum, IActivityActor> fun) {
		events.put(event, activityId, fun);
	}

	public void addEvent(EventEnum event, BiConsumer<EventEnum, IActivityActor> fun) {
		List<BiConsumer<EventEnum, IActivityActor>> list = handlers.get(event);
		if (list == null) {
			list = new ArrayList<>();
			handlers.put(event, list);
		}
		if (!list.contains(fun)) {
			list.add(fun);
		}
	}

	/**
	 * 添加红点推送事件针对领奖
	 *
	 * @param eventEnum
	 * @param actor
	 */
	public void addTipsSyn(EventEnum eventEnum, IActivityActor actor) {

		if (events.contains(eventEnum, actor.getActivityId())) {
			events.get(eventEnum, actor.getActivityId()).accept(eventEnum, actor);
		} else if (events.contains(eventEnum, 0)) {// 通用
			events.get(eventEnum, 0).accept(eventEnum, actor);
		}

		if (actor.onResult() != null) {
			IActivityEventRet eventResult = actor.onResult();
			ActTipManager.getInst().addActivityEventRet(actor.getPlayer().getRoleId(), eventResult);
		}
	}

	/**
	 * 更新活动行为
	 *
	 * @param eventEnum
	 * @param actor
	 */
	public void updateActivityHandler(EventEnum eventEnum, IActivityActor actor) {
		if (!handlers.containsKey(eventEnum)) {
			return;
		}
		handlers.get(eventEnum).forEach(e -> {
			e.accept(eventEnum, actor);
			if (actor.onResult() != null) {
				IActivityEventRet eventResult = actor.onResult();
				ActTipManager.getInst().addActivityEventRet(actor.getPlayer().getRoleId(), eventResult);
			}
		});

	}

	/**
	 * @param eventEnum
	 * @param activityActor
	 */
	public void activityTip(EventEnum eventEnum, IActivityActor activityActor) {
		addTipsSyn(eventEnum, activityActor);
	}

	/**
	 * 常规活动红点
	 *
	 * @param player
	 * @param actRecord
	 * @param activityBase
	 */
	public void activityTip(Player player, ActRecord actRecord, ActivityBase activityBase) {
		CommonTipActor activityAwardActor = new CommonTipActor(player, actRecord, activityBase);
		addTipsSyn(EventEnum.GET_ACTIVITY_AWARD_TIP, activityAwardActor);
	}

	/**
	 * 事件处理
	 *
	 * @param eventEnum
	 * @param player
	 * @param param
	 * @param param2
	 */
	public void activityTip(EventEnum eventEnum, Player player, int param, int param2) {
		if (events.row(eventEnum) == null) {
			return;
		}
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		events.row(eventEnum).entrySet().stream().forEach(e -> {
			int activityId = e.getKey();
			ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
			if (activityBase == null) {
				return;
			}
			if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
				return;
			}
			ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
			ActivityData activityData = activityManager.getActivity(activityBase);
			BaseActivityActor actor = new GameEventActor(player, actRecord, activityData, activityBase, param, param2);
			e.getValue().accept(eventEnum, actor);
			if (actor.onResult() != null) {
				IActivityEventRet eventResult = actor.onResult();
				ActTipManager.getInst().addActivityEventRet(player.getRoleId(), eventResult);
			}
		});
	}

	/***
	 *
	 * @param eventEnum 事件
	 * @param player
	 */
	public void activityTip(EventEnum eventEnum, Player player, int change) {
		this.activityTip(eventEnum, player, change, 0);
	}


}
