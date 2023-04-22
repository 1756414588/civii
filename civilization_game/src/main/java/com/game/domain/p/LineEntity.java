package com.game.domain.p;


import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;

//一个GameEntity由多个LineEntity组成
@Getter
@Setter
public class LineEntity {
    private int soldierType;                //兵种类型
    private Property baseProperty;
    private BattleProperty battleProperty;
    private String name;
    private int entityId;
    private int entityType;                  //实体类型 1.武将 2 pve野怪
    private int maxSoldierNum;               //最大兵力: 向下取整之后 排数*每排兵数
    private int level;
    private int techLv;                            //科技等级

    @Override
    public String toString() {
        return "兵种:" + soldierType +
                ",名称:" + name +
                ",id:" + entityId +
                ",类型:" + entityType +
                ",兵力:" + maxSoldierNum +
                ",基础属性:" + baseProperty.toString() +
                ",战斗属性:" + battleProperty.toString();
    }

    public DataPb.LineEntity wrapDataPb() {
        DataPb.LineEntity.Builder builder = DataPb.LineEntity.newBuilder();
        builder.setSoldierType(soldierType);
        CommonPb.Property.Builder property = CommonPb.Property.newBuilder();
        property.setAttackCity(baseProperty.getAttack());
        property.setDefence(baseProperty.getDefence());
        property.setSoldierNum(baseProperty.getSoldierNum());
        property.setAttackCity(baseProperty.getAttackCity());
        property.setDefenceCity(baseProperty.getDefenceCity());
        property.setStrongAttack(baseProperty.getStrongAttack());
        property.setStrongDefence(baseProperty.getStrongDefence());
        property.setHit(battleProperty.getHit());
        property.setMiss(battleProperty.getMiss());
        property.setCriti(battleProperty.getCriti());
        property.setTenacity(battleProperty.getTenacity());
        builder.setProperty(property);
        builder.setName(StringUtil.isNullOrEmpty(name) ? "" : name);
        builder.setEntityId(entityId);
        builder.setEntityType(entityType);
        builder.setMaxSoldierNum(maxSoldierNum);
        builder.setLevel(level);
        builder.setTechLv(techLv);
        return builder.build();
    }

    public void wrapDataPb(DataPb.LineEntity data) {
        this.soldierType = data.getSoldierType();
        Property property = new Property();
        property.unwrapDataPb(data.getProperty());
        this.baseProperty = property;
        BattleProperty battleProperty = new BattleProperty();
        battleProperty.unwrapDataPb(data.getProperty());
        this.battleProperty = battleProperty;
        this.name = data.getName();
        this.entityId = data.getEntityId();
        this.entityType = data.getEntityType();
        this.maxSoldierNum = data.getMaxSoldierNum();
        this.level = data.getLevel();
        this.techLv = data.getTechLv();
    }

    public int getSoldierNum() {
        return baseProperty.getSoldierNum();
    }

    public void setSoldierNum(int soldierNum) {
        baseProperty.setSoldierNum(soldierNum);
    }

    public int getAttack() {
        return baseProperty.getAttack();
    }

    public void setAttack(int attack) {
        baseProperty.setAttack(attack);
    }

    public int getDefence() {
        return baseProperty.getDefence();
    }

    public void setDefence(int defence) {
        baseProperty.setDefence(defence);
    }

    public int getStrongAttack() {
        return battleProperty.getStrongAttack();
    }

    public void setStrongAttack(int strongAttack) {
        battleProperty.setStrongAttack(strongAttack);
    }

    public int getStrongDefence() {
        return battleProperty.getStrongDefence();
    }

    public void setStrongDefence(int strongDefence) {
        battleProperty.setStrongDefence(strongDefence);
    }

    public int getHit() {
        return battleProperty.getHit();
    }

    public void setHit(int hit) {
        battleProperty.setHit(hit);
    }

    public int getMiss() {
        return battleProperty.getMiss();
    }

    public void setMiss(int miss) {
        battleProperty.setMiss(miss);
    }

    public int getCriti() {
        return battleProperty.getCriti();
    }

    public void setCriti(int criti) {
        battleProperty.setCriti(criti);
    }

    public int getTenacity() {
        return battleProperty.getTenacity();
    }

    public void setTenacity(int tenacity) {
        battleProperty.setTenacity(tenacity);
    }

    public String getName() {
        return name + ":" + entityId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
