package com.game.domain.s;


public class StaticWallMonsterLv {
    private int Id;
    private int defenceLv;
    private int quality;
    private int attack;
    private int defence;
    private int soldier;
    private int soldierLines;   //兵排数
    private int soldierCount;     //兵力
    private int strongAttack;   //强攻
    private int strongDefence;  //强防
    private int hit;            //命中,千分比
    private int miss;           //闪避,千分比
    private int criti;          //暴击,千分比
    private int tenacity;       //抗暴击,千分比


    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getDefenceLv() {
        return defenceLv;
    }

    public void setDefenceLv(int defenceLv) {
        this.defenceLv = defenceLv;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public int getSoldier() {
        return soldier;
    }

    public void setSoldier(int soldier) {
        this.soldier = soldier;
    }

    public int getSoldierLines() {
        return soldierLines;
    }

    public void setSoldierLines(int soldierLines) {
        this.soldierLines = soldierLines;
    }

    public int getSoldierCount() {
        return soldierCount;
    }

    public void setSoldierCount(int soldierCount) {
        this.soldierCount = soldierCount;
    }

    public int getStrongAttack() {
        return strongAttack;
    }

    public void setStrongAttack(int strongAttack) {
        this.strongAttack = strongAttack;
    }

    public int getStrongDefence() {
        return strongDefence;
    }

    public void setStrongDefence(int strongDefence) {
        this.strongDefence = strongDefence;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getMiss() {
        return miss;
    }

    public void setMiss(int miss) {
        this.miss = miss;
    }

    public int getCriti() {
        return criti;
    }

    public void setCriti(int criti) {
        this.criti = criti;
    }

    public int getTenacity() {
        return tenacity;
    }

    public void setTenacity(int tenacity) {
        this.tenacity = tenacity;
    }
}
