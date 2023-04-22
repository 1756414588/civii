package com.game.activity.facede;

import com.game.domain.Player;
import com.game.domain.s.ActivityBase;

public interface IActivityEventRet<T> {

	public long getId();

	public ActivityBase getActivityBase();

	public T onResult(Player player);
}
