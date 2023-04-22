package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;

/**
 * 游戏事件tips
 *
 * @author zcp
 * @date 2021/9/6 9:04
 */
public class GameEventActor extends BaseActivityActor {
    private int param;
    private int param2;

    public GameEventActor(Player player, ActRecord actRecord, ActivityData activityData, ActivityBase activityBase, int param,int param2) {
        super(player, actRecord, activityBase);
        this.param = param;
        this.param2 = param2;
        this.activityData = activityData;
    }

    @Override
    public int getChange() {
        return param;
    }

    @Override
    public int getParam2() {
        return param2;
    }
}
