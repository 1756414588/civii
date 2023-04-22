package com.game.domain.p;

import com.game.pb.DataPb;

public class Shop implements Cloneable {

    private int propId;
    private int free;
    private int buyCount;
    private int time;

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Shop() {

    }

    public Shop(DataPb.ShopData pb) {
        this.propId = pb.getPropId();
        this.free = pb.getFree();
        this.buyCount = pb.getCount();
        this.time = pb.getTime();
    }

    public DataPb.ShopData serDb() {
        DataPb.ShopData.Builder builder = DataPb.ShopData.newBuilder();
        builder.setPropId(propId);
        builder.setCount(buyCount);
        builder.setFree(free);
        builder.setTime(time);
        return builder.build();
    }

    @Override
    public Shop clone() {
        Shop shop = null;
        try {
            shop = (Shop) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return shop;
    }
}
