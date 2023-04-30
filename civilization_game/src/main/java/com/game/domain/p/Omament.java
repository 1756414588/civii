package com.game.domain.p;

import com.game.pb.CommonPb;

/**
 * 2020年8月1日
 *
 *    halo_game Omament.java
 * 配饰
 **/

public class Omament implements Cloneable {
    private BuildingBase base = new BuildingBase();
    private int omamentId; // 配饰ID
    private int count; // 配饰数量

    public BuildingBase getBase() {
        return base;
    }

    public void setBase(BuildingBase base) {
        this.base = base;
    }

    public int getOmamentId() {
        return omamentId;
    }

    public void setOmamentId(int omamentId) {
        this.omamentId = omamentId;
    }

    public Omament() {
        super();
    }

    public Omament(int omamentId, int count) {
        super();
        this.omamentId = omamentId;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Omament cloneInfo() {
        Omament omament = new Omament();
        omament.omamentId = omamentId;
        omament.count = count;
        return omament;
    }

    public CommonPb.Omament.Builder wrapPb() {
        CommonPb.Omament.Builder builder = CommonPb.Omament.newBuilder();
        builder.setOmamentId(omamentId);
        builder.setCount(count);
        return builder;
    }

    public void unwrapPb(CommonPb.Omament build) {
        omamentId = build.getOmamentId();
        count = build.getCount();
    }

    public void copyData(Omament omament) {
        omamentId = omament.getOmamentId();
        count = omament.getCount();
    }

    public CommonPb.Omament.Builder writeData() {
        CommonPb.Omament.Builder builder = CommonPb.Omament.newBuilder();
        builder.setOmamentId(omamentId);
        builder.setCount(count);

        return builder;
    }

    public void readData(CommonPb.Omament build) {
        omamentId = build.getOmamentId();
        count = build.getCount();
    }

    public void initBase(int buildId, int buildingLevel) {
        base.setBuildingId(buildId);
        base.setLevel(buildingLevel);
    }

    public int getLv() {
        return base.getLevel();
    }

    @Override
    public String toString() {
        return "Omament [omamentId=" + omamentId + ", count=" + count + "]";
    }

    @Override
    public Omament clone() {
        Omament omament = null;
        try {
            omament = (Omament) super.clone();
            omament.setBase(this.base.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return omament;
    }
}
