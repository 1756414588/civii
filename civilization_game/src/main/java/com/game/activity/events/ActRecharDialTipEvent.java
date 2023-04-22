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

/**
 * 充值转盘
 */
public class ActRecharDialTipEvent extends BaseActivityEvent {

	private static ActRecharDialTipEvent inst = new ActRecharDialTipEvent();

	public static ActRecharDialTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.RE_DIAL, this::process);
		listenEvent(EventEnum.DO_DIAL, ActivityConst.RE_DIAL, this::doRecharDialInfo);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		//这个是总次数
		int readyCount = actRecord.getCount() + actRecord.getRecord().size();
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		//抽满了就不给了
		if (readyCount >= actAwardList.size()) {
			return;
		}
		long state = actRecord.getStatus(0);
		//从第几档开始计算
		for (int i = readyCount; i < actAwardList.size(); i++) {
			StaticActAward config = actAwardList.get(i);
			if (state >= config.getCond()) {
				actRecord.addCount();
				actRecord.putState(config.getSortId(), state);
			}
		}
		//推送下红点
		if (actRecord.getCount() > 0) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
	}

	public void doRecharDialInfo(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		if (actRecord.getCount() > 0) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}
	}
}
