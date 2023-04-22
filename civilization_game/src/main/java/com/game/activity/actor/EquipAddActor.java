package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.Player;
import com.game.domain.s.StaticEquip;

public class EquipAddActor extends BaseActivityActor {

    private StaticEquip staticEquip;

    public EquipAddActor(Player player, StaticEquip staticEquip) {
        this.player = player;
        this.staticEquip = staticEquip;
    }

    public StaticEquip getStaticEquip() {
        return staticEquip;
    }

    public void setStaticEquip(StaticEquip staticEquip) {
        this.staticEquip = staticEquip;
    }
}
