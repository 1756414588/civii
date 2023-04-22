package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.activity.result.ActBuildQueRet;
import com.game.activity.result.ActDisappearRet;
import com.game.activity.result.ActMonthGiftRet;
import com.game.activity.result.ActSuripriseGiftRet;
import com.game.constant.ActivityConst;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActivityRecord;
import com.game.domain.s.ActivityBase;
import com.game.manager.PlayerManager;
import com.game.spring.SpringUtil;
import java.util.Date;
import java.util.List;

/**
 * 同时推送synActivity和SynActivityDisappearRq
 */
public class ActSynAndTipEvent extends BaseActivityEvent {

	private static ActSynAndTipEvent inst = new ActSynAndTipEvent();

	public static ActSynAndTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, ActivityConst.ACT_BEAUTY_GIFT, this::process);
		listenEvent(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, ActivityConst.ACT_MONTH_GIFT, this::actMonthGift);
		listenEvent(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, ActivityConst.ACT_BUILD_QUE, this::actBuildQueue);
		listenEvent(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, ActivityConst.ACT_SURIPRISE_GIFT, this::actSuripriseGift);
		listenEvent(EventEnum.ACT_BUY_GIFT, ActivityConst.ACT_MONTH_GIFT, this::actDisppear);
		listenEvent(EventEnum.ACT_BUY_GIFT, ActivityConst.ACT_BUILD_QUE, this::actDisppear);
		listenEvent(EventEnum.ACT_BUY_GIFT, ActivityConst.ACT_SURIPRISE_GIFT, this::actSuripriseDisppear);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SURIPRISE_GIFT, this::actSuripriseGiftTipDisappear);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_MONTH_GIFT, this::actMonthGiftTipDisappear);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_BUILD_QUE, this::actBuildQueTipDisappear);
//		listenEvent(EventEnum.TIME_DISAPPEAR, ActivityConst.ACT_SURIPRISE_GIFT, this::actSuripriseDisppear);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		playerManager.synActivity(player, activityBase.getActivityId());

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
	}


	/**
	 * 双卡礼包
	 *
	 * @param eventEnum
	 * @param actor
	 */
	public void actMonthGift(EventEnum eventEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		playerManager.synActivity(player, activityBase.getActivityId());

		actor.setResult(new ActMonthGiftRet(activityBase, actRecord, SynEnum.ACT_TIP_DISAPEAR, true));
	}

	/**
	 * 购买双卡礼包,购买后消失
	 *
	 * @param eventEnum
	 * @param actor
	 */
	public void actDisppear(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		actor.setResult(new ActDisappearRet(activityBase, actRecord, SynEnum.ACT_DISAPEAR, false));
	}

	public void actSuripriseDisppear(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		List<ActivityRecord> activityRecordList = actRecord.getActivityRecords();
		if (activityRecordList.isEmpty()) {
			actor.setResult(new ActDisappearRet(activityBase, actRecord, SynEnum.ACT_DISAPEAR, false));
			return;
		}

		long curTime = System.currentTimeMillis();
		for (ActivityRecord e : activityRecordList) {
			if (e.getExpireTime() > curTime) {// 还有标签存在
				return;
			}
		}
		actor.setResult(new ActDisappearRet(activityBase, actRecord, SynEnum.ACT_DISAPEAR, false));
	}

	public void actBuildQueue(EventEnum eventEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		playerManager.synActivity(player, activityBase.getActivityId());

		actor.setResult(new ActBuildQueRet(activityBase, actRecord, SynEnum.ACT_TIP_DISAPEAR, true));
	}


	public void actSuripriseGift(EventEnum eventEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		if (actRecord == null) {
			return;
		}

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		playerManager.synActivity(player, activityBase.getActivityId());

		actor.setResult(new ActSuripriseGiftRet(activityBase, actRecord, SynEnum.ACT_TIP_DISAPEAR, true));
	}


	public void actSuripriseGiftTipDisappear(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		if (activityBase.disappear(new Date())) {
			actor.setResult(new ActSuripriseGiftRet(activityBase, actRecord, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
		actor.setResult(new ActSuripriseGiftRet(activityBase, actRecord, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	public void actMonthGiftTipDisappear(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		actor.setResult(new ActMonthGiftRet(activityBase, actRecord, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	public void actBuildQueTipDisappear(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		actor.setResult(new ActBuildQueRet(activityBase, actRecord, SynEnum.ACT_TIP_DISAPEAR, false));
	}


}
