package com.game.domain.p;

import com.game.constant.BuildingType;
import com.game.pb.CommonPb.Building;
import com.game.pb.DataPb;

// 建筑基础信息
public class BuildingBase implements Cloneable {
    private int buildingId;
    private int level;


    public BuildingBase() {

    }

    public BuildingBase(int buildingId, int level) {
        setBuildingId(buildingId);
        setLevel(level);
    }

    public Building.Builder wrapPb() {
        Building.Builder builder = Building.newBuilder();
        builder.setBuildingId(getBuildingId());
        builder.setLv(getLevel());
        return builder;
    }

    public void unwrapPb(Building building) {
        setBuildingId(building.getBuildingId());
        setLevel(building.getLv());
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void incrementLevel() {
        level += 1;
    }

    public DataPb.BuildingData.Builder readData() {
        DataPb.BuildingData.Builder builder = DataPb.BuildingData.newBuilder();
        builder.setBuildingId(getBuildingId());
        builder.setLv(getLevel());
        return builder;
    }

    public void readData(DataPb.BuildingData building) {
        setBuildingId(building.getBuildingId());
        setLevel(building.getLv());
    }

    @Override
    public BuildingBase clone() {
        BuildingBase buildingBase = null;
        try {
            buildingBase = (BuildingBase) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return buildingBase;
    }
}
