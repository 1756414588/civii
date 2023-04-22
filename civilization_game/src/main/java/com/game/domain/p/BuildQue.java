package com.game.domain.p;

import com.game.pb.CommonPb;


public class BuildQue implements Cloneable {
    private int buildingId;
    private int reBuildingId;
    private volatile long endTime;
    private long period;
    private int freeTimes;       // 免费使用次数:0 未减免, 1: 减免
    private long primarySpeed;      // 初级资源点加速
    private int buildQueType;      // 免费建造，商业建造
    private boolean isRebuild;
    private int activityDerateCD;    //虫族加速活动（0无减免，1有减免）

    @Override
    public BuildQue clone() {
        BuildQue buildQue = null;
        try {
            buildQue = (BuildQue) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return buildQue;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getReBuildingId() {
        return reBuildingId;
    }

    public void setReBuildingId(int reBuildingId) {
        this.reBuildingId = reBuildingId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }


    public void setPeriod(int period) {
        this.period = period;
    }

    public CommonPb.BuildQue.Builder wrapPb() {
        CommonPb.BuildQue.Builder buildQue = CommonPb.BuildQue.newBuilder();
        buildQue.setBuildingId(buildingId);
        buildQue.setEndTime(endTime);
        buildQue.setPeriod(period);
        buildQue.setVipReduce(freeTimes);
        buildQue.setPrimarySpeed(primarySpeed);
        buildQue.setBuildQueType(buildQueType);
        buildQue.setReBuildingId(reBuildingId);
        buildQue.setActivityDerateCD(activityDerateCD);
        return buildQue;
    }

    public void unwrapPb(CommonPb.BuildQue builder) {
        if (builder.hasBuildingId())
            buildingId = builder.getBuildingId();
        if (builder.hasPeriod())
            period = builder.getPeriod();
        if (builder.hasEndTime())
            endTime = builder.getEndTime();
        if (builder.hasVipReduce())
            freeTimes = builder.getVipReduce();
        if (builder.hasBuildQueType())
            buildQueType = builder.getBuildQueType();
        if (builder.hasPrimarySpeed())
            primarySpeed = builder.getPrimarySpeed();
        if (builder.hasReBuildingId())
            reBuildingId = builder.getReBuildingId();
        if (builder.hasActivityDerateCD())
            activityDerateCD = builder.getActivityDerateCD();
    }


    public int getFreeTimes() {
        return freeTimes;
    }

    public void setFreeTimes(int freeTimes) {
        this.freeTimes = freeTimes;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getPrimarySpeed() {
        return primarySpeed;
    }

    public void setPrimarySpeed(long primarySpeed) {
        this.primarySpeed = primarySpeed;
    }

    public int getBuildQueType() {
        return buildQueType;
    }

    public void setBuildQueType(int buildQueType) {
        this.buildQueType = buildQueType;

    }

    public boolean isRebuild() {
        return isRebuild;
    }

    public void setRebuild(boolean rebuild) {
        isRebuild = rebuild;
    }

    public int getActivityDerateCD() {
        return activityDerateCD;
    }

    public void setActivityDerateCD(int activiDerateCD) {
        this.activityDerateCD = activiDerateCD;
    }


}

