package com.game.domain.p;

import com.game.domain.Award;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;

public class WsWaitQue implements Cloneable {
    private int index;
    private long startTime;
    private Award award = new Award();

    @Override
    public WsWaitQue clone() {
        WsWaitQue wsWaitQue = null;
        try {
            wsWaitQue = (WsWaitQue) super.clone();
            wsWaitQue.setAward(this.award.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return wsWaitQue;
    }

    public WsWaitQue() {
    }

    public WsWaitQue(int index, Award award) {
        this.index = index;
        this.award = award;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    public CommonPb.WsWaitQue.Builder wrapPb() {
        CommonPb.WsWaitQue.Builder builder = CommonPb.WsWaitQue.newBuilder();
        builder.setIndex(index);
        builder.setStartTime(startTime);
        builder.setAward(award.wrapPb());
        return builder;
    }

    public void unwrapPb(CommonPb.WsWaitQue wsWaitQue) {
        index = wsWaitQue.getIndex();
        startTime = wsWaitQue.getStartTime();
        award.unwrapPb(wsWaitQue.getAward());
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public void readData(DataPb.WsWaitQueData data) {
        if (data != null) {
            index = data.getIndex();
            startTime = data.getStartTime();
            award.readData(data.getAward());
        }

    }

    public DataPb.WsWaitQueData.Builder writeData() {
        DataPb.WsWaitQueData.Builder builder = DataPb.WsWaitQueData.newBuilder();
        builder.setIndex(index);
        builder.setAward(award.writeData());
        builder.setStartTime(startTime);
        return builder;
    }
}
