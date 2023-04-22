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
import java.util.Optional;

/**
 * 累积充值 本活动,月卡，特价礼包，限时礼包均计入充值额度
 *
 *
 */
@Component
public class ActGrandTotalTipEvent extends BaseActivityEvent {
	//
	//private static ActGrandTotalTipEvent inst = new ActGrandTotalTipEvent();
	//
	//public static ActGrandTotalTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_GRAND_TOTAL, this::process);
		listenEvent(EventEnum.PAY, ActivityConst.ACT_GRAND_TOTAL, this::pay);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();

		int status = (int) actRecord.getStatus(0L);
		Map<Integer, Integer> received = actRecord.getReceived();

		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		Optional<StaticActAward> optional = awardList.stream().filter(e -> e.getCond() <= status && !received.containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	public void pay(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();

		int status = (int) actRecord.getStatus(0L);
		Map<Integer, Integer> received = actRecord.getReceived();

		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		Optional<StaticActAward> optional = awardList.stream().filter(e -> e.getCond() <= status && !received.containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
	}
}
