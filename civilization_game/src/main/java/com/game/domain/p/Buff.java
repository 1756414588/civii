package com.game.domain.p;


import com.game.pb.CommonPb;
import com.game.pb.DataPb;

// 玩家道具buff
public class Buff {
    private int buffId;    // buffId, 1. 伤害减免 2. 伤害加深 3. 部队加速 4.部队重建
    private long period;   // buff时长
    private long endTime;  // buff结束时间
    private int value;     // buff效果 / 100

    public int getBuffId() {
        return buffId;
    }

    public void setBuffId(int buffId) {
        this.buffId = buffId;
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

    public float getValue() {
        return (float) value / 100.0f;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public CommonPb.Buff wrapPb() {
        CommonPb.Buff.Builder builder = CommonPb.Buff.newBuilder();
        builder.setBuffId(buffId);
        builder.setPeriod(period);
        builder.setEndTime(endTime);
        builder.setValue(value);
        return builder.build();
    }

    public DataPb.BuffData.Builder writeData() {
        DataPb.BuffData.Builder builder = DataPb.BuffData.newBuilder();
        builder.setBuffId(buffId);
        builder.setPeriod(period);
        builder.setEndTime(endTime);
        builder.setValue(value);
        return builder;
    }


    public void readData(DataPb.BuffData data) {
        buffId = data.getBuffId();
        period = data.getPeriod();
        endTime = data.getEndTime();
        value = data.getValue();
    }

}
