package com.game.domain.p;

import com.game.pb.CommonPb;

public class CityInfo implements Cloneable {
    private int actionType;
    private int soldierType;
    private int buildingId;
    private int buildingLevel;
    private long time;
    private int techType;
    private int techLv;
    private int equipId;
    private int materialId;
    private int materialNum;

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public int getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingLevel() {
        return buildingLevel;
    }

    public void setBuildingLevel(int buildingLevel) {
        this.buildingLevel = buildingLevel;
    }

    public void unwrapPb(CommonPb.CityInfo cityInfo) {
        actionType = cityInfo.getActionType();
        buildingId = cityInfo.getBuildingId();
        buildingLevel = cityInfo.getBuildingLevel();
        soldierType = cityInfo.getSoldierType();
        time = cityInfo.getTime();
        techType = cityInfo.getTechType();
        techLv = cityInfo.getTechLv();
        equipId = cityInfo.getEquipId();
        materialId = cityInfo.getMaterialId();
        materialNum = cityInfo.getMaterialNum();
    }


    public CommonPb.CityInfo.Builder wrapPb() {
        CommonPb.CityInfo.Builder builder = CommonPb.CityInfo.newBuilder();
        builder.setActionType(getActionType());
        builder.setBuildingId(getBuildingId());
        builder.setBuildingLevel(getBuildingLevel());
        builder.setSoldierType(getSoldierType());
        builder.setTime(getTime());
        builder.setTechType(getTechType());
        builder.setTechLv(getTechLv());
        builder.setEquipId(getEquipId());
        builder.setMaterialId(getMaterialId());
        builder.setMaterialNum(getMaterialNum());


        return builder;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getMaterialNum() {
        return materialNum;
    }

    public void setMaterialNum(int materialNum) {
        this.materialNum = materialNum;
    }

    @Override
    public CityInfo clone() {
        CityInfo cityInfo = null;
        try {
            cityInfo = (CityInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return cityInfo;
    }
}
