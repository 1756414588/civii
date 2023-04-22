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
 * 惊喜礼包显示
 */
public class ActSuripriseGiftRet implements IActivityEventRet<Activity> {

	protected boolean show;
	protected SynEnum type;
	protected ActRecord actRecord;
	protected ActivityBase activityBase;

	public ActSuripriseGiftRet(ActivityBase activityBase, ActRecord actRecord, SynEnum type, boolean show) {
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
		return PbHelper.createActivityPb(activityBase, new Date(actRecord.getExpireTime()), true, show);
	}
}
