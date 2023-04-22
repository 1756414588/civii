package com.game.domain.s;

import java.util.List;

public class StaticEquip {
    private int equipId;
    private String equipName;
    private int quality;
    private int attack;
    private int defence;
    private int soldierCount;
    private List<Integer> skillId;
    private int equipType;
    private List<List<Long>> compose;
    private int lordLv;
    private int canCompose;
    private int period;  // 秒
    private List<List<Long>> decompose;
    private int secretSkill;


    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
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

    public int getSoldierCount() {
        return soldierCount;
    }

    public void setSoldierCount(int soldierCount) {
        this.soldierCount = soldierCount;
    }

    public List<Integer> getSkillId() {
        return skillId;
    }

    public void setSkillId(List<Integer> skillId) {
        this.skillId = skillId;
    }

    public int getEquipType() {
        return equipType;
    }

    public void setEquipType(int equipType) {
        this.equipType = equipType;
    }

    public int getLordLv () {
        return lordLv;
    }

    public void setLordLv (int lordLv) {
        this.lordLv = lordLv;
    }

    public int getCanCompose () {
        return canCompose;
    }

    public void setCanCompose (int canCompose) {
        this.canCompose = canCompose;
    }

    public int getPeriod () {
        return period;
    }

    public void setPeriod (int period) {
        this.period = period;
    }

    public List<List<Long>> getCompose () {
        return compose;
    }

    public void setCompose (List<List<Long>> compose) {
        this.compose = compose;
    }

    public List<List<Long>> getDecompose () {
        return decompose;
    }

    public void setDecompose (List<List<Long>> decompose) {
        this.decompose = decompose;
    }

    public String getEquipName() {
        return equipName;
    }

    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    public int getSecretSkill() {
        return secretSkill;
    }

    public void setSecretSkill(int secretSkill) {
        this.secretSkill = secretSkill;
    }
}
