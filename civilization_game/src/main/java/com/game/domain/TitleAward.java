package com.game.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.pb.CommonPb;
import com.game.util.TimeHelper;

public class TitleAward {

    private volatile int recv = 0;

    private Map<Integer, Integer> hisRecv = new HashMap<>();

    private long recvTime = TimeHelper.curentTime();

    public int getRecv() {
        return recv;
    }

    public void setRecv(int recv) {
        this.recv = recv;
    }

    public Map<Integer, Integer> getHisRecv() {
        return hisRecv;
    }

    public void setHisRecv(Map<Integer, Integer> hisRecv) {
        this.hisRecv = hisRecv;
    }

    public CommonPb.TitleAwardInfo encode() {
        CommonPb.TitleAwardInfo.Builder builder1 = CommonPb.TitleAwardInfo.newBuilder();
        this.getHisRecv().entrySet().forEach(x -> {
            CommonPb.TwoInt.Builder builder2 = CommonPb.TwoInt.newBuilder();
            builder2.setV1(x.getKey());
            builder2.setV2(x.getValue());
            builder1.addHisRecv(builder2);
        });

        boolean sameDay = TimeHelper.isSameDay(this.recvTime);
        if (!sameDay) {
            this.recv = 0;
        }
        builder1.setRecv(this.recv);
        builder1.setRecvTime(this.recvTime);

        return builder1.build();
    }

    public void decode(CommonPb.TitleAwardInfo ctitleAward) {
        this.recv = ctitleAward.getRecv();
        List<CommonPb.TwoInt> hisRecvList = ctitleAward.getHisRecvList();
        hisRecvList.forEach(x -> {
            getHisRecv().put(x.getV1(), x.getV2());
        });
        this.recvTime = ctitleAward.getRecvTime();
    }

	public long getRecvTime() {
		return recvTime;
	}

	public void setRecvTime(long recvTime) {
		this.recvTime = recvTime;
	}
}
