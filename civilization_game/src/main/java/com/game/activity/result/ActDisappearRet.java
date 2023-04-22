package com.game.activity.result;

import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityEventRet;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;

/**
 * 活动消失
 */
public class ActDisappearRet implements IActivityEventRet<Integer> {

	protected boolean show;
	protected SynEnum type;
	protected ActRecord actRecord;
	protected ActivityBase activityBase;

	public ActDisappearRet(ActivityBase activityBase, ActRecord actRecord, SynEnum type, boolean show) {
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
	public Integer onResult(Player player) {
		if (!show) {
			return activityBase.getActivityId();
		}
		return 0;
	}
}
