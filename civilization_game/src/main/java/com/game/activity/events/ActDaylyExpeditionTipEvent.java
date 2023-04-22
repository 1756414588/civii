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
import java.util.Optional;

/**
 * 每日远征
 */
@Component
public class ActDaylyExpeditionTipEvent extends BaseActivityEvent {

	//public static ActDaylyExpeditionTipEvent inst = new ActDaylyExpeditionTipEvent();
	//
	//public static ActDaylyExpeditionTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_DAYLY_EXPEDITION, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			return;
		}

		long status = actRecord.getStatus(-1);
		Optional<StaticActAward> option = condList.stream().filter(e -> e.getCond() <= status && !actRecord.getReceived().containsKey(e.getKeyId())).findAny();
		if (option.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}
}
