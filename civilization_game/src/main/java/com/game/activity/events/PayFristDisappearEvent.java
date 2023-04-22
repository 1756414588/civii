package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.Player;
import com.game.domain.s.ActivityBase;
import com.game.pb.ActivityPb;
import com.game.util.SynHelper;


/**
 * 领取后活动消失
 */
public class PayFristDisappearEvent extends BaseActivityEvent {

	private static PayFristDisappearEvent inst = new PayFristDisappearEvent();

	public static PayFristDisappearEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_PAY_FIRST, this::process);
		listenEvent(EventEnum.PAY, ActivityConst.ACT_PAY_FIRST, this::payFristTip);
	}

	@Override
	public void process(EventEnum eventEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();

		ActivityPb.SynActivityDisappearRq.Builder builder = ActivityPb.SynActivityDisappearRq.newBuilder();
		builder.addParam(activityBase.getActivityId());

		SynHelper.synMsgToPlayer(player, ActivityPb.SynActivityDisappearRq.EXT_FIELD_NUMBER, ActivityPb.SynActivityDisappearRq.ext, builder.build());

	}

	/**
	 * 充值
	 *
	 * @param eventEnum
	 * @param actor
	 */
	public void payFristTip(EventEnum eventEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();
		int firstPay = player.getLord().getFirstPay();

		if (firstPay == 1) {// 可领奖
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		} else if (firstPay == 2) {// 已领奖
			ActivityPb.SynActivityDisappearRq.Builder builder = ActivityPb.SynActivityDisappearRq.newBuilder();
			builder.addParam(activityBase.getActivityId());
			SynHelper.synMsgToPlayer(player, ActivityPb.SynActivityDisappearRq.EXT_FIELD_NUMBER, ActivityPb.SynActivityDisappearRq.ext, builder.build());
		}
	}
}
