package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 材料生产
 */
@Component
public class ActOrderTipEvent extends BaseActivityEvent {


	//private static ActOrderTipEvent inst = new ActOrderTipEvent();
	//
	//public static ActOrderTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_ORDER, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}
		List<StaticActAward> list = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (list.isEmpty()) {
			return;
		}
		int status = actRecord.getRecord(1);
		StaticActAward staticActAward = list.stream().filter(x -> x.getCond() < status && !actRecord.getReceived().containsKey(x.getKeyId()))
			.findAny().orElse(null);
		if (staticActAward != null) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
		// 没有奖励可领取了,左侧活动上红点关闭
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
