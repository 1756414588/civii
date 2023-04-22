package com.game.activity.facede;

import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;

/**
 * 活动行为接口
 */
public interface IActivityActor {

	public Player getPlayer();

	public ActRecord getActRecord();

	public ActivityBase getActivityBase();

	public ActivityData getActivityData();

	public int getActivityId();

	public IActivityEventRet onResult();

	public void setResult(IActivityEventRet result);

	public int getChange();

	public int getParam2();
}
