package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;


public class RankActor extends BaseActivityActor {

	public RankActor(Player player, ActRecord actRecord, ActivityBase activityBase, ActivityData activityData) {
		this.player = player;
		this.actRecord = actRecord;
		this.activityBase = activityBase;
		this.activityData = activityData;
	}
}
