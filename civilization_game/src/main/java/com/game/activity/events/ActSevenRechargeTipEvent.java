package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.p.ActRecord;
import com.game.domain.s.StaticActAward;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 七日充值
 */
@Component
public class ActSevenRechargeTipEvent extends BaseActivityEvent {
	//
	//private static ActSevenRechargeTipEvent inst = new ActSevenRechargeTipEvent();
	//
	//public static ActSevenRechargeTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.PAY, ActivityConst.ACT_SEVEN_RECHARGE, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SEVEN_RECHARGE, this::reward);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			return;
		}

		// 累计充值记录
		int state = (int) actRecord.getStatus(0);

		// 显示红点
		Optional<StaticActAward> optional = condList.stream().filter(e -> e.getCond() <= state && !actRecord.getReceived().containsKey(e.getKeyId())).findFirst();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void reward(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			return;
		}

		// 累计充值记录
		int state = (int) actRecord.getStatus(0);

		// 显示红点
		Optional<StaticActAward> optional = condList.stream().filter(e -> e.getCond() <= state && !actRecord.getReceived().containsKey(e.getKeyId())).findFirst();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}
}
