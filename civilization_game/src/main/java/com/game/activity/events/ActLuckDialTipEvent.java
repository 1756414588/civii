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
import com.game.domain.s.StaticActDialPurp;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 好运转盘活动红点
 */
@Component
public class ActLuckDialTipEvent extends BaseActivityEvent {
	//
	//private static ActLuckDialTipEvent inst = new ActLuckDialTipEvent();
	//
	//public static ActLuckDialTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.LUCK_DIAL, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		List<StaticActAward> displayList = staticActivityMgr.getActAwardById(actRecord.getAwardId());

		int awardId = actRecord.getAwardId();
		StaticActDialPurp dial = staticActivityMgr.getDialPurp(awardId);
		if (actRecord.getCount() < dial.getFreeTimes()) {// 有免费次数
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		for (StaticActAward e : displayList) {
			long cond = actRecord.getStatus(1L);
			if (e.getCond() <= cond && !actRecord.getReceived().containsKey(e.getKeyId())) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
