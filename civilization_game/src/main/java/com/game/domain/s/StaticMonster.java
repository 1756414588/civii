package com.game.domain.s;

public class StaticMonster {
    private int monsterId;      //怪物Id
    private int level;          //等级
    private int soldierLines;   //兵排数
    private int soldierType;    //怪物类型
    private int attack;         //攻击
    private int defence;        //防御
    private int soldierCount;     //兵力
    private int strongAttack;   //强攻
    private int strongDefence;  //强防
    private int hit;            //命中,千分比
    private int miss;           //闪避,千分比
    private int criti;          //暴击,千分比
    private int tenacity;       //抗暴击,千分比
    private int quality;        //怪物品质
    private String name;        //怪物名字


    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    public int getSoldierLines() {
        return soldierLines;
    }

    public void setSoldierLines(int soldierLines) {
        this.soldierLines = soldierLines;
    }

    public int getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
