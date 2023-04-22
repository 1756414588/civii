package com.game.domain.p;


import com.game.pb.CommonPb;

// pvp 战报信息
public class PvpPkInfo {
    private SoloInfo attack = new SoloInfo();
    private SoloInfo defence = new SoloInfo();

    public SoloInfo getAttack() {
        return attack;
    }

    public void setAttack(SoloInfo attack) {
        this.attack = attack;
    }

    public SoloInfo getDefence() {
        return defence;
    }

    public void setDefence(SoloInfo defence) {
        this.defence = defence;
    }

    public CommonPb.PvpPkInfo.Builder wrapPb() {
        CommonPb.PvpPkInfo.Builder builder = CommonPb.PvpPkInfo.newBuilder();
        builder.setAttacker(attack.wrapPb());
        builder.setDefencer(defence.wrapPb());
        return builder;
    }
}
