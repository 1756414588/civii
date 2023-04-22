package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.p.ActRecord;
import com.game.domain.s.StaticActAward;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Optional;

/**
 * 充值有礼-个人
 */
public class ActTopupPersonTipEvent extends BaseActivityEvent {

	private static ActTopupPersonTipEvent inst = new ActTopupPersonTipEvent();

	public static ActTopupPersonTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.PAY, ActivityConst.ACT_TOPUP_PERSON, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_TOPUP_PERSON, this::reward);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
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

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
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
