package com.game.activity.result;

import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityEventRet;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.pb.CommonPb.Activity;
import com.game.util.PbHelper;
import java.util.Date;

/**
 * 月卡显示
 */
public class ActMonthGiftRet implements IActivityEventRet<Activity> {

	protected boolean show;
	protected SynEnum type;
	protected ActRecord actRecord;
	protected ActivityBase activityBase;

	public ActMonthGiftRet(ActivityBase activityBase, ActRecord actRecord, SynEnum type, boolean show) {
		this.show = show;
		this.type = type;
		this.activityBase = activityBase;
		this.actRecord = actRecord;
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
		long timeKey = 0;
		long endTime = 0;
		if (actRecord.getStatus().containsKey(timeKey)) {
			endTime = actRecord.getStatus(timeKey);
			if (endTime <= System.currentTimeMillis()) {
				return PbHelper.createActivityPb(activityBase, activityBase.getEndTime(), true, show);
			}
		}
		return PbHelper.createActivityPb(activityBase, new Date(endTime), true, show);
	}
}
