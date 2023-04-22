package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.s.ActivityBase;
import org.springframework.stereotype.Component;


/**
 * 部分活动领取奖励后红点直接消失 体力补给 签到
 */
@Component
public class AwardThenRemoveTipEvent extends BaseActivityEvent {

	//private static AwardThenRemoveTipEvent inst = new AwardThenRemoveTipEvent();
	//
	//public static AwardThenRemoveTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_POWER, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_DAILY_CHECKIN, this::process);
	}

	@Override
	public void process(EventEnum eventEnum, IActivityActor activityActor) {
		ActivityBase activityBase = activityActor.getActivityBase();
		// 没有奖励可领取了,左侧活动上红点关闭
		activityActor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
