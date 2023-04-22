package com.game.domain.p;

import com.game.constant.PropertyType;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Property implements Cloneable {
    private int attack;         //攻击
    private int defence;        //防御
    private int soldierNum;     //总兵力
    private int maxSoldier;     //当前最大兵力: 只是用来计算战斗实体的实际血量
    private int strongAttack;   //强攻
    private int strongDefence;  //强防
    private int attackCity;   //攻城
    private int defenceCity;   //守城
    private int percentageOfForceAdition;   //美女兵力加成百分比

    public Property() {

    }

    public Property(int attack, int defence, int soldierNum) {
        this.attack = attack;
        this.defence = defence;
        this.soldierNum = soldierNum;
    }

    public Property(int attack, int defence, int soldierNum, int maxSoldier, int strongAttack, int strongDefence, int attackCity, int defenceCity) {
        this.attack = attack;
        this.defence = defence;
        this.soldierNum = soldierNum;
        this.maxSoldier = maxSoldier;
        this.strongAttack = strongAttack;
        this.strongDefence = strongDefence;
        this.attackCity = attackCity;
        this.defenceCity = defenceCity;
    }

    public Property(Property qualify) {
        this.attack = qualify.getAttack();
        this.defence = qualify.getDefence();
        this.soldierNum = qualify.getSoldierNum();
    }

    public Property cloneInfo() {
        Property builder = new Property();
        builder.setAttack(this.attack);
        builder.setDefence(this.defence);
        builder.setSoldierNum(this.soldierNum);
        return builder;
    }

    public CommonPb.Property.Builder wrapPb() {
        CommonPb.Property.Builder builder = CommonPb.Property.newBuilder();
        builder.setAttack(attack);
        builder.setDefence(defence);
        builder.setSoldierNum(soldierNum);
        builder.setAttackCity(attackCity);
        builder.setDefenceCity(defenceCity);
        builder.setStrongAttack(strongAttack);
        builder.setStrongDefence(strongDefence);
        return builder;
    }

    public void unwrapPb(CommonPb.Property build) {
        attack = build.getAttack();
        defence = build.getDefence();
        soldierNum = build.getSoldierNum();
    }

    public void add(Property param) {
        attack += param.getAttack();
        defence += param.getDefence();
        soldierNum += param.getSoldierNum();
        strongAttack += param.getStrongAttack();
        strongDefence += param.getStrongDefence();
        attackCity += param.getAttackCity();
        defenceCity += param.getDefenceCity();

    }

    public void sub(Property param) {
        attack -= param.getAttack();
        defence -= param.getDefence();
        soldierNum -= param.getSoldierNum();

    }

    public void clear() {
        attack = 0;
        defence = 0;
        soldierNum = 0;
    }

    public void addAttack(Property property) {
        attack += property.getAttack();
    }

    public void addDefence(Property property) {
        defence += property.getDefence();
    }


    public void addSoldier(Property property) {
        soldierNum += property.getSoldierNum();
    }


    public DataPb.PropertyData.Builder writeData() {
        DataPb.PropertyData.Builder builder = DataPb.PropertyData.newBuilder();
        builder.setAttack(attack);
        builder.setDefence(defence);
        builder.setSoldierNum(soldierNum);
        return builder;
    }

    public void readData(DataPb.PropertyData build) {
        attack = build.getAttack();
        defence = build.getDefence();
        soldierNum = build.getSoldierNum();
    }


    public boolean isInit() {
        return attack == 0 && defence == 0 && soldierNum == 0;
    }

    @Override
    public String toString() {
        return "攻击:" + attack +
                ",防御:" + defence +
                ",总兵力:" + soldierNum +
                ",最大兵力:" + maxSoldier +
                ",强攻:" + strongAttack +
                ",强防:" + strongDefence +
                ",攻城:" + attackCity +
                ",守城:" + defenceCity
                ;
    }

    public void addAttackValue(int value) {
        if (value < 0) {
            return;
        }
        attack += value;
    }

    public void addDefenceValue(int value) {
        if (value < 0) {
            return;
        }
        defence += value;
    }

    public void addSoldierNumValue(int value) {
        if (value < 0) {
            return;
        }
        soldierNum += value;
    }


    public boolean isZero() {
        return attack == 0 || defence == 0 || soldierNum == 0;
    }

    public void unwrapDataPb(CommonPb.Property data) {
        attack = data.getAttack();
        defence = data.getDefence();
        soldierNum = data.getSoldierNum();
        maxSoldier = data.getMaxSoldier();
        strongAttack = data.getStrongAttack();
        strongDefence = data.getStrongDefence();
        attackCity = data.getAttackCity();
        defenceCity = data.getDefenceCity();
    }

    public void addValue(int attrId, int value) {
        switch (attrId) {
            case PropertyType.ATTCK:
                this.attack += value;
                break;
            case PropertyType.DEFENCE:
                this.defence += value;
                break;
            case PropertyType.SOLDIER_NUM:
                this.soldierNum += value;
                break;
            case PropertyType.STRONG_ATTACK:
                this.strongAttack += value;
                break;
            case PropertyType.STRONG_DEFENCE:
                this.strongDefence += value;
                break;
            case PropertyType.ATTACK_CITY:
                this.attackCity += value;
                break;
            case PropertyType.DEFENCE_CITY:
                this.defenceCity += value;
                break;
            default:
                break;
        }
    }

    @Override
    protected Property clone() {
        Property property = null;
        try {
            property = (Property) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return property;
    }

}

