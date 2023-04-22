package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActEquipUpdate;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 装备精研 1.活动期间装备精研达到指定次数，可领取装备精研次数; 2.每天完成任意充值还能领取额外精研次数; 3.累积充值天数越多，额外获得的改造次数越多 4.购买每日特惠礼包不计入累积天数
 */
public class ActWashEquipTipEvent extends BaseActivityEvent {

	private static ActWashEquipTipEvent inst = new ActWashEquipTipEvent();

	public static ActWashEquipTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_WASH_EQUIP, this::process);
		listenEvent(EventEnum.EQUIP_WASH, ActivityConst.ACT_WASH_EQUIP, this::washEquip);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActEquipUpdate> staticActEquipUpdateList = staticActivityMgr.getStaticActEquipUpdateList();
		if (null == staticActEquipUpdateList || staticActEquipUpdateList.size() == 0) {
			return;
		}

		int status = (int) actRecord.getStatus(StaticActEquipUpdate.WASH_CONUT);
		int got = (int) actRecord.getStatus(StaticActEquipUpdate.PAY_CONUT);
		Map<Integer, Integer> received = actRecord.getReceived();

		for (StaticActEquipUpdate e : staticActEquipUpdateList) {
			boolean canAward = e.getType() == 1 ? got >= e.getCond() : status >= e.getCond();
			if (canAward && !received.containsKey(e.getKeyId())) {// 有奖励未领取
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	/**
	 * 充值事件
	 *
	 * @param event
	 * @param actor
	 */
	public void pay(EventEnum event, IActivityActor actor) {

	}

	/**
	 * 装备洗练
	 *
	 * @param event
	 * @param actor
	 */
	public void washEquip(EventEnum event, IActivityActor actor) {
		ActRecord record = actor.getActRecord();
		long status = record.getStatus(StaticActEquipUpdate.WASH_CONUT);
		long payCount = record.getStatus(StaticActEquipUpdate.PAY_CONUT);
		boolean preReward = isHadReward(record, (int) status, (int) payCount);
		status += actor.getChange();
		record.putState(StaticActEquipUpdate.WASH_CONUT, status);
		boolean newReward = isHadReward(record, (int) status, (int) payCount);
		if (preReward != newReward) {// 状态发生变化
			this.process(event, actor);
		}
	}

	public boolean isHadReward(ActRecord actRecord, int status, int payCount) {

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActEquipUpdate> staticActEquipUpdateList = staticActivityMgr.getStaticActEquipUpdateList();

		Map<Integer, Integer> received = actRecord.getReceived();

		Optional<StaticActEquipUpdate> optional = staticActEquipUpdateList.stream().filter(e -> !received.containsKey(e.getKeyId())).filter(e -> e.getType() == 1 ? payCount >= e.getCond() : status >= e.getCond()).findFirst();

		// 有奖励可领取
		if (optional.isPresent()) {
			return true;
		}

		return false;
	}
}
