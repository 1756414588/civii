package com.game.domain.p;

import com.game.pb.SerializePb;
import com.game.pb.CommonPb.ReachBaitRecordPB;
import com.game.pb.SerializePb.SerReachBaitRecordPB;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReachBaitRecord {

    private int baitId;
    private long firstGainTime;

    public ReachBaitRecordPB.Builder encode() {
        ReachBaitRecordPB.Builder builder = ReachBaitRecordPB.newBuilder();
        builder.setBaitId(this.baitId);
        builder.setFirstGainTime(this.firstGainTime);
        return builder;
    }

    public void decode(ReachBaitRecordPB baitRecordPB) {
        this.baitId = baitRecordPB.getBaitId();
        this.firstGainTime = baitRecordPB.getFirstGainTime();
    }
}
