package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.ActivityData;
import com.game.domain.s.ActivityBase;

public class ActServerCensusActor extends BaseActivityActor {

    public ActServerCensusActor(ActivityBase activityBase, ActivityData activityData) {
        this.activityBase = activityBase;
        this.activityData = activityData;
    }
}
