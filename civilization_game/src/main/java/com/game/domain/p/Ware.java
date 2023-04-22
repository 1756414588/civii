package com.game.domain.p;

import com.game.pb.CommonPb;

// 仓库
public class Ware implements Cloneable {
    private BuildingBase base = new BuildingBase();

    public Ware() {

    }

    @Override
    public Ware clone() {
        Ware ware = null;
        try {
            ware = (Ware) super.clone();
            ware.setBase(this.base.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return ware;
    }

    public int getLv() {
        return getBase().getLevel();
    }

    public int getBuildingId() {
        return getBase().getBuildingId();
    }

    public void incrementLevel() {
        getBase().incrementLevel();
    }


    public BuildingBase getBase() {
        return base;
    }

    public void setBase(BuildingBase base) {
        this.base = base;
    }

    //building wrap
    public CommonPb.Building.Builder wrapBase() {
        return base.wrapPb();
    }

    public void initBase(int buildId, int buildingLevel) {
        base.setBuildingId(buildId);
        base.setLevel(buildingLevel);
    }


    public CommonPb.Ware.Builder wrapPb() {
        CommonPb.Ware.Builder builder = CommonPb.Ware.newBuilder();
        builder.setWare(wrapBase());

        return builder;
    }


    public void unwrapPb(CommonPb.Ware builder) {
        base.unwrapPb(builder.getWare());
    }


}
