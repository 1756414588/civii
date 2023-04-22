package com.game.domain.p;

import com.game.domain.Award;
import com.game.pb.CommonPb;

/**
 * @author 陈奎
 * @version 1.0
 * @filename
 * @time 2016-12-18 下午11:25:30
 */
public class WorkQue implements Cloneable {
    private int keyId;
    private int buildingId;
    private long period;
    private long endTime;
    private int employWork;  //雇佣减CD(0没有 1减少过)
    private Award award = new Award();
    private long primarySpeed;  // 初级资源点加速
    private long oil;       // 募兵消耗的石油
    private long speedTime;  // 已经免费加速的累加时间
    private long iron;       // 募兵消耗的生铁
    private int activityDerateCD;    //虫族加速活动（0无减免，1有减免）

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getEmployWork() {
        return employWork;
    }

    public void setEmployWork(int employWork) {
        this.employWork = employWork;
    }

    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    public WorkQue() {
        super();
    }

    public CommonPb.WorkQue.Builder wrapPb() {
        CommonPb.WorkQue.Builder builder = CommonPb.WorkQue.newBuilder();
        builder.setKeyId(getKeyId());
        builder.setBuildingId(getBuildingId());
        builder.setPeriod(getPeriod());
        builder.setEndTime(getEndTime());
        builder.setEmployWork(getEmployWork());
        builder.setAward(getAward().wrapPb());
        builder.setPrimarySpeed(primarySpeed);
        builder.setOil(oil);
        builder.setSpeedTime(speedTime);
        builder.setIron(iron);
        builder.setActivityDerateCD(activityDerateCD);
        return builder;
    }

    public void unwrapPb(CommonPb.WorkQue builder) {
        keyId = builder.getKeyId();
        buildingId = builder.getBuildingId();
        period = builder.getPeriod();
        endTime = builder.getEndTime();
        employWork = builder.getEmployWork();
        if (builder.hasAward()) {
            award.unwrapPb(builder.getAward());
        }
        primarySpeed = builder.getPrimarySpeed();
        oil = builder.getOil();
        speedTime = builder.getSpeedTime();
        iron = builder.getIron();
        activityDerateCD = builder.getActivityDerateCD();
    }


    public long getPrimarySpeed() {
        return primarySpeed;
    }

    public void setPrimarySpeed(long primarySpeed) {
        this.primarySpeed = primarySpeed;
    }

    public long getOil() {
        return oil;
    }

    public void setOil(long oil) {
        this.oil = oil;
    }

    public long getSpeedTime() {
        return speedTime;
    }

    public void setSpeedTime(long speedTime) {
        this.speedTime = speedTime;
    }

    public long getIron() {
        return iron;
    }

    public void setIron(long iron) {
        this.iron = iron;
    }

    public int getActivityDerateCD() {
        return activityDerateCD;
    }

    public void setActivityDerateCD(int activityDerateCD) {
        this.activityDerateCD = activityDerateCD;
    }

    @Override
    public WorkQue clone() {
        WorkQue workQue = null;
        try {
            workQue = (WorkQue) super.clone();
            workQue.setAward(this.award.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return workQue;
    }
}
