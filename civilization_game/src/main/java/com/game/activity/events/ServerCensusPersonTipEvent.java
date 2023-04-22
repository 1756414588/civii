package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.manager.ActivityManager;
import com.game.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全服累积活动 个人领奖
 */
@Component
public class ServerCensusPersonTipEvent extends BaseActivityEvent {

	//private static ServerCensusPersonTipEvent inst = new ServerCensusPersonTipEvent();
	//
	//public static ServerCensusPersonTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_HIGH_VIP, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SER_PAY, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_TOPUP_SERVER, this::process);
	}

	@Autowired
	ActivityManager activityManager;

	@Override
	public void process(EventEnum eventEnum, IActivityActor actor) {
//		LogHelper.MESSAGE_LOGGER.info("ServerCensusPersonTipEvent");
		int activityId = actor.getActivityId();
		//ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		ActivityBase activityBase = actor.getActivityBase();
		ActivityData activityData = activityManager.getActivity(activityBase);
		Player player = actor.getPlayer();

		//StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (awardList == null || awardList.isEmpty()) {
			return;
		}

		// 全服活动已达成的领取奖励
		List<StaticActAward> finishList = awardList.stream().filter(e -> activityData.getStatus(e.getSortId()) >= e.getCond()).collect(Collectors.toList());
		if (activityBase.getActivityId() == ActivityConst.ACT_SER_PAY) {
			finishList = awardList.stream().filter(e -> activityData.getStatus(e.getSortId()) >= e.getCond() && e.getCond() != 0).collect(Collectors.toList());
		}

		// 领奖设置等级条件
		int limitLevel = SpringUtil.getBean(StaticLimitMgr.class).getNum(activityId);
		if (limitLevel != 0 && player.getLevel() < limitLevel) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		for (StaticActAward e : finishList) {
			if (!actRecord.getReceived().containsKey(e.getKeyId())) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
