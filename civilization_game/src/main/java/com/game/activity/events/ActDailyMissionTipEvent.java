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

/**
 * @author zcp
 * @date 2021/9/6 10:48
 */
@Component
public class ActDailyMissionTipEvent extends BaseActivityEvent {

	//private static ActDailyMissionTipEvent inst = new ActDailyMissionTipEvent();
	//
	//public static ActDailyMissionTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		this.listenEvent(EventEnum.MISSSION_DONE, ActivityConst.ACT_DAILY_MISSION, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		long status = actRecord.getStatus(0);
		status += actor.getChange();
		actRecord.putState(0, status);
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		boolean flag = false;
		if (awardList != null) {
			for (StaticActAward e : awardList) {
				int keyId = e.getKeyId();
				boolean canAward = status >= e.getCond();
				//可领取 未领奖
				if (canAward && !actRecord.getReceived().containsKey(keyId)) {
					flag = true;
					break;
				}
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}
}
