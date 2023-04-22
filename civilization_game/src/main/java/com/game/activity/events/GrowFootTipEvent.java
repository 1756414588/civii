package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 基金活动领取奖励后红点消失
 */
@Component
public class GrowFootTipEvent extends BaseActivityEvent {

	//private static GrowFootTipEvent inst = new GrowFootTipEvent();
	//
	//public static GrowFootTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_GROW_FOOT, this::process);
	}

	@Override
	public void process(EventEnum eventEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		// 未参与活动
		if (actRecord.getStatus().isEmpty()) {
			return;
		}
		int awardId = actRecord.getAwardId();
		//StaticActivityMgr mgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(awardId);

		for (StaticActAward actAward : awardList) {
			// 未购买
			if (!actRecord.getStatus().containsKey(actAward.getSortId())) {
				continue;
			}

			// 已经领取
			if (actRecord.getReceived().containsKey(actAward.getKeyId())) {
				continue;
			}

			long buyTime = actRecord.getStatus(1L);
			int status = TimeHelper.equation(buyTime,TimeHelper.curentTime()) + 1;
			if (status >= actAward.getCond()) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
