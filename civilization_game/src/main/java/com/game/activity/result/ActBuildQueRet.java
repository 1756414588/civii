package com.game.activity.result;

import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityEventRet;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.pb.CommonPb.Activity;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import java.util.Date;

/**
 * 建造队列返回结果
 */
public class ActBuildQueRet implements IActivityEventRet<Activity> {

	private ActivityBase activityBase;
	private ActRecord actRecord;
	private SynEnum synEnum;
	private boolean show;

	public ActBuildQueRet(ActivityBase activityBase, ActRecord actRecord, SynEnum type, boolean show) {
		this.activityBase = activityBase;
		this.actRecord = actRecord;
		this.synEnum = type;
		this.show = show;
	}

	@Override
	public long getId() {
		return activityBase.getActivityId();
	}

	@Override
	public ActivityBase getActivityBase() {
		return activityBase;
	}

	@Override
	public Activity onResult(Player player) {
		StaticLimitMgr staticLimitMgr = SpringUtil.getBean(StaticLimitMgr.class);
		int openDays = staticLimitMgr.getAddtion(254).get(2);
		Date openTime = new Date(actRecord.getBeginTime() * TimeHelper.SECOND_MS);
		Date cleanTime = new Date(openTime.getTime() + openDays * TimeHelper.DAY_MS);
		return PbHelper.createActivityPb(activityBase, cleanTime, true, show);
	}
}
