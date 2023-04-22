package com.game.domain.p;

import com.game.pb.DataPb;

// 科技进度信息
public class TechQue implements Cloneable {
    private int techType;          // 科技type
    private int level;          // 科技level
    private long endTime;       // 科技结束时间, 如果endTime <= now表示已经完成
    private int speed;          // 被科技官加速过, 0: 未加速过 1.加速过
    private long speedTime;  // 已经免费加速的累加时间
    private int activityDerateCD; //虫族加速活动（0无减免，1有减免）

    public TechQue() {
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public DataPb.TechQueData.Builder wrapData() {
        DataPb.TechQueData.Builder builder = DataPb.TechQueData.newBuilder();
        builder.setTechType(techType);
        builder.setEndTime(endTime);
        builder.setLevel(level);
        builder.setSpeed(speed);
        builder.setSpeedTime(speedTime);
        return builder;
    }

    public void unwrapData(DataPb.TechQueData builder) {
        techType = builder.getTechType();
        level = builder.getLevel();
        endTime = builder.getEndTime();
        speed = builder.getSpeed();
        speedTime = builder.getSpeedTime();
    }

    public int getTechType() {
        return techType;
    }

    public void setTechType(int techType) {
        this.techType = techType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getSpeedTime() {
        return speedTime;
    }

    public void setSpeedTime(long speedTime) {
        this.speedTime = speedTime;
    }

    public int getActivityDerateCD() {
        return activityDerateCD;
    }

    public void setActivityDerateCD(int activityDerateCD) {
        this.activityDerateCD = activityDerateCD;
    }

    @Override
    public TechQue clone() {
        TechQue techQue = null;
        try {
            techQue = (TechQue) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return techQue;
    }
}
