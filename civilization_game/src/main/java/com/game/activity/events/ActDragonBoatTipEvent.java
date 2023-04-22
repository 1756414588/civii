package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActivityChrismasAward;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 端午/国庆活动
 */
@Component
public class ActDragonBoatTipEvent extends BaseActivityEvent {

	//private static ActDragonBoatTipEvent inst = new ActDragonBoatTipEvent();
	//
	//public static ActDragonBoatTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_DRAGON_BOAT_GIFT, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		Integer totalCost = actRecord.getRecord(-1);
		if (totalCost == null || totalCost == 0) {
			return;
		}
		List<StaticActivityChrismasAward> list = new ArrayList<>(staticActivityMgr.getChrismasAwardMap().values());
		for (StaticActivityChrismasAward award : list) {
			if (totalCost >= award.getCost()) {
				if (!actRecord.getReceived().containsKey(award.getKeyId())) {
					actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
					return;
				}
			}
		}
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

}
