package com.game.domain.p;

import com.game.pb.CommonPb;

// 参谋部
public class Staff implements Cloneable {
    private BuildingBase base = new BuildingBase();

    @Override
    public Staff clone() {
        Staff staff = null;
        try {
            staff = (Staff) super.clone();
            staff.setBase(this.base.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return staff;
    }

    public BuildingBase getBase() {
        return base;
    }

    public void setBase(BuildingBase base) {
        this.base = base;
    }

    public int getLv() {
        return base.getLevel();
    }

    public int getBuildingId() {
        return base.getBuildingId();
    }

    public void incrementLevel() {
        base.incrementLevel();
    }

    // building wrap
    public CommonPb.Building.Builder wrapBase() {
        return base.wrapPb();
    }

    public void initBase(int buildId, int buildingLevel) {
        base.setBuildingId(buildId);
        base.setLevel(buildingLevel);
    }

    public CommonPb.Staff.Builder wrapPb() {
        CommonPb.Staff.Builder builder = CommonPb.Staff.newBuilder();
        builder.setStaff(wrapBase());

        return builder;
    }

    public void unwrapPb(CommonPb.Staff builder) {
        base.unwrapPb(builder.getStaff());
    }
}
