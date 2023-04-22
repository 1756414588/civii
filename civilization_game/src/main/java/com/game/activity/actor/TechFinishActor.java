package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.Player;

/**
 * 科技神级完成
 */
public class TechFinishActor extends BaseActivityActor {

    private int techType;
    private int techLv;

    public TechFinishActor(Player player, int techType, int techLv) {
        this.player = player;
        this.techType = techType;
        this.techLv = techLv;
    }

    public int getTechType() {
        return techType;
    }

    public void setTechType(int techType) {
        this.techType = techType;
    }

    public int getTechLv() {
        return techLv;
    }

    public void setTechLv(int techLv) {
        this.techLv = techLv;
    }
}
