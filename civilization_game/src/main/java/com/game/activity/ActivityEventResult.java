package com.game.activity;

import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityEventRet;
import com.game.domain.Player;
import com.game.domain.s.ActivityBase;
import com.game.pb.CommonPb.Activity;
import com.game.util.DateHelper;
import com.game.util.PbHelper;
import java.util.Date;

public class ActivityEventResult implements IActivityEventRet<Activity> {

	protected SynEnum type;
	protected ActivityBase activityBase;
	protected boolean show;
	protected Activity activity;

	public ActivityEventResult() {
	}

	public ActivityEventResult(ActivityBase activityBase, SynEnum type, boolean show) {
		this.activityBase = activityBase;
		this.type = type;
		this.show = show;
	}

	@Override
	public Activity onResult(Player player) {
		int less = activityBase.getStaticActivity().getLess();
		if (less != 0) {
			Date endTime = DateHelper.addDate(player.account.getCreateDate(), less);
			activity = PbHelper.createActivityPb(activityBase, endTime, true, show);
		} else {
			activity = PbHelper.createActivityPb(activityBase, null, true, show);
		}
		return activity;
	}

	public SynEnum getType() {
		return type;
	}

	public void setType(SynEnum type) {
		this.type = type;
	}

	@Override
	public long getId() {
		return activityBase.getActivityId();
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	@Override
	public ActivityBase getActivityBase() {
		return activityBase;
	}


}
