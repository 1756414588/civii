package com.game.domain.p;

import com.game.pb.CommonPb;

// 参谋部
public class Market implements Cloneable {
    private BuildingBase base = new BuildingBase();

    @Override
    public Market clone() {
        Market market = null;
        try {
            market = (Market) super.clone();
            market.setBase(this.base.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return market;
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

    public CommonPb.Market.Builder wrapPb() {
        CommonPb.Market.Builder builder = CommonPb.Market.newBuilder();
        builder.setMarket(wrapBase());

        return builder;
    }

    public void unwrapPb(CommonPb.Market builder) {
        base.unwrapPb(builder.getMarket());
    }
}
