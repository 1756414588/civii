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
import com.game.domain.s.StaticActAward;

import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Optional;

/**
 * 消费有礼
 *
 * @author cpz
 */
public class ActCostPersonTipEvent extends BaseActivityEvent {

	private static ActCostPersonTipEvent inst = new ActCostPersonTipEvent();

	public static ActCostPersonTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_COST_PERSON, this::subGold);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_COST_PERSON, this::process);
	}

	public void subGold(EventEnum activityEnum, IActivityActor actor) {

		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		long state = actRecord.getStatus(0);
		state = state + actor.getChange();
		actRecord.putState(0, state);

		List<StaticActAward> list = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		boolean flag = false;
		for (StaticActAward config : list) {
			if (state >= config.getCond() && !actRecord.getReceived().containsKey(config.getKeyId())) {
				flag = true;
				break;
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);

		long state = actRecord.getStatus(0);
		List<StaticActAward> list = staticActivityMgr.getActAwardById(activityBase.getAwardId());

		Optional<StaticActAward> optional = list.stream().filter(e -> e.getCond() <= state && !actRecord.getReceived().containsKey(e.getKeyId())).findFirst();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
