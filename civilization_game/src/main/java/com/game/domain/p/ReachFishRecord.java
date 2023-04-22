package com.game.domain.p;

import com.game.pb.CommonPb.ReachFishRecordPB;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReachFishRecord {

    private int fishId;
    private long firstGainTime;
    private int maxSize;
    private int awardStatus;
    private int count;


    public ReachFishRecordPB.Builder encode() {
        ReachFishRecordPB.Builder builder = ReachFishRecordPB.newBuilder();
        builder.setFishId(this.fishId);
        builder.setFirstGainTime(firstGainTime);
        builder.setMaxSize(this.maxSize);
        builder.setAwardStatus(this.awardStatus);
        return builder;
    }

    public void decode(ReachFishRecordPB fishRecordPB) {
        this.fishId = fishRecordPB.getFishId();
        this.firstGainTime = fishRecordPB.getFirstGainTime();
        this.maxSize = fishRecordPB.getMaxSize();
        this.awardStatus = fishRecordPB.getAwardStatus();
    }
}
