package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.util.LogHelper;

import com.game.spring.SpringUtil;
import java.util.List;

/**
 * 配表StaticActAward 活动状态记录为ActRecord中map对象status的sortId为key的 活动领奖之后左侧活动上红点提示或者消失
 */
public class ActRewardTipEvent extends BaseActivityEvent {

	private static ActRewardTipEvent inst = new ActRewardTipEvent();

	public static ActRewardTipEvent getInst() {
		return inst;
	}

	/**
	 * 监听事件
	 */
	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, 0, this::process);
	}


	@Override
	public void process(EventEnum eventEnum, IActivityActor activityActor) {
		ActivityBase activityBase = activityActor.getActivityBase();
		ActRecord actRecord = activityActor.getActRecord();

		// 不可领奖阶段,则没有tips
		if (!activityBase.canAward()) {
			return;
		}

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());

		if (null == condList || condList.size() == 0) {
			return;
		}

		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			long cond = actRecord.getStatus(e.getSortId());
			if (cond >= e.getCond()) {
				if (!actRecord.getReceived().containsKey(keyId)) {// 未领取奖励
					activityActor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
					return;
				}
			}
		}

		// 没有奖励可领取了,左侧活动上红点关闭
		activityActor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

}
