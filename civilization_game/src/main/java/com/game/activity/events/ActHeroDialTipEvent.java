package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActDialPurp;
import com.game.server.GameServer;
import com.game.spring.SpringUtil;

/**
 * 夺命魅影
 */
public class ActHeroDialTipEvent extends BaseActivityEvent {

	private static ActHeroDialTipEvent inst = new ActHeroDialTipEvent();

	public static ActHeroDialTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_HERO_DIAL, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);

		// 是否能抽奖
		StaticActDialPurp dial = staticActivityMgr.getDialPurp(actRecord.getAwardId());
		if (dial != null) {//
			int freeTimes = dial.getFreeTimes();
			int curentDay = GameServer.getInstance().currentDay;
			int getCount = actRecord.getRecord(curentDay);
			if (getCount < freeTimes) {// 有免费次数,红点
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
