package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.constant.SpringType;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.ActivityData;
import com.game.domain.p.ActRecord;
import com.game.domain.s.StaticActSpringFestival;
import com.game.manager.ActivityManager;
import com.game.server.GameServer;
import com.game.service.ActivityService;
import com.game.spring.SpringUtil;
import java.util.List;

/**
 * @Description 春节活动&新年特惠
 * @ProjectName halo_server
 * @Date 2022/1/12 16:44
 **/
public class ActSpringFestivalEvent extends BaseActivityEvent {

	private static ActSpringFestivalEvent inst = new ActSpringFestivalEvent();

	public static ActSpringFestivalEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		this.listenEvent(EventEnum.PAY, ActivityConst.ACT_SPRING_FESTIVAL, this::process);
		this.listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SPRING_FESTIVAL, this::process);
		this.listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SPRING_FESTIVAL_GIFT, this::springGiftTip);
	}

	public void springGiftTip(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		if (actRecord != null) {
			boolean disappear = SpringUtil.getBean(ActivityService.class).springGiftDisappear(actRecord);
			if (disappear) {
				actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_DISAPEAR, false));
			}
		}
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		ActRecord actRecord = actor.getActRecord();
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		boolean show = false;
		if (activityData == null) {
			show = false;
		}
		List<StaticActSpringFestival> springFestivals = staticActivityMgr.getSpringFestivals(actRecord.getAwardId());
		if (springFestivals != null && !springFestivals.isEmpty()) {
			for (StaticActSpringFestival springFestival : springFestivals) {
				int keyId = springFestival.getKeyId();
				if (springFestival.getType() == SpringType.SpringAward) {
					if (!actRecord.getReceived().containsKey(keyId) && activityData.getRecordNum(0) >= springFestival.getCond()) {
						show = true;
					}
				}
				if (springFestival.getType() == SpringType.SpringRecharge) {
					if (springFestival.getCond() == 0 && actRecord.getReceived(keyId) != GameServer.getInstance().currentDay) {
						actRecord.getReceived().remove(springFestival.getKeyId());
					}
					if (!actRecord.getReceived().containsKey(keyId) && actRecord.getStatus(0) >= springFestival.getCond()) {
						show = true;
					}
				}
			}
		}
		actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, show));
	}
}
