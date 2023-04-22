package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

// 世界目标
public class WorldTarget {
    private int targetId;
    private int status;   // 状态: 0未达成 1达成 2.已经同步过
    private int country;
    private int times;

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

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public void readData(DataPb.WorldTargetData data) {
        targetId = data.getTargetId();
        status = data.getStatus();
        country = data.getCountry();
    }

    public DataPb.WorldTargetData.Builder writeData() {
        DataPb.WorldTargetData.Builder builder = DataPb.WorldTargetData.newBuilder();
        builder.setTargetId(targetId);
        builder.setStatus(status);
        builder.setCountry(country);

        return builder;
    }


    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public CommonPb.WorldTarget.Builder wrapPb() {
        CommonPb.WorldTarget.Builder builder = CommonPb.WorldTarget.newBuilder();
        builder.setTargetId(targetId);
        builder.setStatus(status);
        builder.setCountry(country);
        return builder;
    }
}
