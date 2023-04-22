package com.game.domain.p;

import com.game.constant.DevideFactor;
import com.game.domain.s.StaticHeroProp;

// 等级
public class PropertyFactor {
    private double attackFactor;
    private double defenceFactor;
    private double soldierFactor;

    public PropertyFactor() {
    }

    public PropertyFactor(float attackFactor, float defenceFactor, float soldierFactor) {
        this.setAttackFactor(attackFactor);
        this.setDefenceFactor(defenceFactor);
        this.setSoldierFactor(soldierFactor);
    }

    public void initFactor(StaticHeroProp prop) {
        attackFactor = prop.getAttackFactor() / DevideFactor.FACTOR_NUM;
        defenceFactor = prop.getDefenceFactor() / DevideFactor.FACTOR_NUM;
        soldierFactor = prop.getSoldierFactor() / DevideFactor.FACTOR_NUM;
    }

    public double getAttackFactor() {
        return attackFactor;
    }

    public void setAttackFactor(double attackFactor) {
        this.attackFactor = attackFactor;
    }

    public double getDefenceFactor() {
        return defenceFactor;
    }

    public void setDefenceFactor(double defenceFactor) {
        this.defenceFactor = defenceFactor;
    }

    public double getSoldierFactor() {
        return soldierFactor;
    }

    public void setSoldierFactor(double soldierFactor) {
        this.soldierFactor = soldierFactor;
    }
}
