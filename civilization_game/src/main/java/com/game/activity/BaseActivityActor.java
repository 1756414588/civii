package com.game.activity;

import com.game.activity.facede.IActivityActor;
import com.game.activity.facede.IActivityEventRet;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;

/**
 * 活动
 */
public abstract class BaseActivityActor implements IActivityActor {

	protected Player player;
	protected ActRecord actRecord;
	protected ActivityBase activityBase;
	protected ActivityData activityData;
	protected IActivityEventRet result;

	public BaseActivityActor() {
	}

	public BaseActivityActor(Player player, ActRecord actRecord, ActivityBase activityBase) {
		this.player = player;
		this.actRecord = actRecord;
		this.activityBase = activityBase;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public ActRecord getActRecord() {
		return actRecord;
	}

	@Override
	public ActivityBase getActivityBase() {
		return activityBase;
	}

	@Override
	public ActivityData getActivityData() {
		return activityData;
	}

	@Override
	public int getActivityId() {
		return activityBase.getActivityId();
	}

	@Override
	public IActivityEventRet onResult() {
		return result;
	}

	@Override
	public void setResult(IActivityEventRet result) {
		this.result = result;
	}

	@Override
	public int getChange() {
		return 0;
	}

	@Override
	public int getParam2() {
		return 0;
	}
}
