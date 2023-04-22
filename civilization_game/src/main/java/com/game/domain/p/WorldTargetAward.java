package com.game.domain.p;


import com.game.pb.DataPb;

public class WorldTargetAward implements Cloneable {
    private int targetId;
    private int status;         // 状态: 0 未领取 1.已经领取
    private int country;

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataPb.WorldTargetAwardData.Builder writeData() {
        DataPb.WorldTargetAwardData.Builder builder = DataPb.WorldTargetAwardData.newBuilder();
        builder.setTargetId(targetId);
        builder.setStatus(status);
        builder.setCountry(country);
        return builder;
    }

    public void readData(DataPb.WorldTargetAwardData data) {
        setTargetId(data.getTargetId());
        setStatus(data.getStatus());
        setCountry(data.getCountry());
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    @Override
    public WorldTargetAward clone() {
        WorldTargetAward worldTargetAward = null;
        try {
            worldTargetAward = (WorldTargetAward) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return worldTargetAward;
    }
}
