package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;

/**
 * 击杀士兵
 */
public class KillSoilderActor extends BaseActivityActor {

    private Player player;
    private int killed;

    public KillSoilderActor(Player player, int killed, ActRecord actRecord, ActivityBase activityBase) {
        this.player = player;
        this.killed = killed;
        this.actRecord = actRecord;
        this.activityBase = activityBase;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getKilled() {
        return killed;
    }

    public void setKilled(int killed) {
        this.killed = killed;
    }
}
