package com.game.domain.p;

import com.game.domain.Award;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;

public class WsWorkQue implements Cloneable {
    private int keyId;
    private int index;
    private long period;      // 时长
    private long endTime;     // 结束时间
    private long configTime;  // 配置时间
    private long reduceTime;  // 上一次人口减少的时间
    private Award award = new Award();

    @Override
    public WsWorkQue clone() {
        WsWorkQue wsWorkQu = null;
        try {
            wsWorkQu = (WsWorkQue) super.clone();
            wsWorkQu.setAward(this.award.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return wsWorkQu;
    }

    public WsWorkQue() {
    }

    public WsWorkQue(int index, long period, long endTime, Award award) {
        this.index = index;
        this.period = period;
        this.endTime = endTime;
        this.award = award;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    public CommonPb.WsWorkQue.Builder wrapPb() {
        CommonPb.WsWorkQue.Builder builder = CommonPb.WsWorkQue.newBuilder();
        builder.setKeyId(keyId);
        builder.setIndex(index);
        builder.setPeriod(period);
        builder.setEndTime(endTime);
        builder.setAward(award.wrapPb());

        return builder;
    }

    public void unwrapPb(CommonPb.WsWorkQue workQue) {
        keyId = workQue.getKeyId();
        index = workQue.getIndex();
        period = workQue.getPeriod();
        endTime = workQue.getEndTime();
        award.unwrapPb(workQue.getAward());
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public void readData(DataPb.WsWorkQueData data) {
        keyId = data.getKeyId();
        index = data.getIndex();
        period = data.getPeriod();
        endTime = data.getEndTime();
        award.readData(data.getAward());
        configTime = data.getConfigTime();
        reduceTime = data.getReduceTime();
    }

    public DataPb.WsWorkQueData writeData() {
        DataPb.WsWorkQueData.Builder builder = DataPb.WsWorkQueData.newBuilder();
        builder.setKeyId(keyId);
        builder.setIndex(index);
        builder.setPeriod(period);
        builder.setEndTime(endTime);
        builder.setAward(award.writeData());
        builder.setConfigTime(configTime);
        builder.setReduceTime(reduceTime);
        return builder.build();
    }


    public long getConfigTime() {
        return configTime;
    }

    public void setConfigTime(long configTime) {
        this.configTime = configTime;
    }

    public long getReduceTime() {
        return reduceTime;
    }

    public void setReduceTime(long reduceTime) {
        this.reduceTime = reduceTime;
    }
}
