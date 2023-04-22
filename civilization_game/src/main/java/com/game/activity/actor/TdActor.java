package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;

public class TdActor extends BaseActivityActor {


	public TdActor(Player player, ActRecord actRecord, ActivityData activityData, ActivityBase activityBase) {
		this.player = player;
		this.actRecord = actRecord;
		this.activityData = activityData;
		this.activityBase = activityBase;
	}

}
