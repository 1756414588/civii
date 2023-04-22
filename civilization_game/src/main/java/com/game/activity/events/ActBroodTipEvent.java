package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.activity.result.ActDisappearRet;
import com.game.constant.ActivityConst;
import com.game.domain.ActivityData;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 母巢之战 累计消费 监听
 */
@Component
public class ActBroodTipEvent extends BaseActivityEvent {
	//
	//private static ActBroodTipEvent inst = new ActBroodTipEvent();
	//
	//public static ActBroodTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.BUY_BROOD_BUFF, ActivityConst.BLOOD_ACTIVITY, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.BLOOD_ACTIVITY, this::reward);
		listenEvent(EventEnum.BUY_BROOD_DISPEAR, ActivityConst.BLOOD_ACTIVITY, this::disPear);

	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord activity = actor.getActRecord();
		int change = actor.getChange();
		activity.setCount(activity.getCount() + change);
		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		boolean flag = false;
		for (StaticActAward award : actAwardList) {
			if (activity.getCount() > award.getCond() && !activity.getReceived().containsKey(award.getKeyId())) {
				flag = true;
				break;
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void reward(EventEnum activityEnum, IActivityActor actor) {
		//Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		ActivityData activityData = activityManager.getActivity(activityBase.getActivityId());
		if (activityData == null) {
			return;
		}
		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		boolean flag = false;
		for (StaticActAward award : actAwardList) {
			if (actRecord.getCount() > award.getCond() && !actRecord.getReceived().containsKey(award.getKeyId())) {
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

	public void disPear(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		actor.setResult(new ActDisappearRet(activityBase, actRecord, SynEnum.ACT_DISAPEAR, false));
	}
}
