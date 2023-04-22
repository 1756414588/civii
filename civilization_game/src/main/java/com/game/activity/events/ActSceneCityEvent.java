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
import java.util.Map;

/**
 * 攻城掠地
 *
 * @author 陈奎
 */
@Component
public class ActSceneCityEvent extends BaseActivityEvent {

	//private static ActSceneCityEvent inst = new ActSceneCityEvent();
	//
	//public static ActSceneCityEvent getInst() {
	//	return inst;
	//}


	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SCENE_CITY, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();

		ActRecord actRecord = actor.getActRecord();
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());

		Map<Integer, Integer> received = actRecord.getReceived();

		boolean flag = false;

		for (StaticActAward e : awardList) {
			int record = (int) actRecord.getStatus(e.getSortId());
			if (e.getCond() <= record && !received.containsKey(e.getKeyId())) {
				flag = true;
				break;
			}
		}

		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}
}
