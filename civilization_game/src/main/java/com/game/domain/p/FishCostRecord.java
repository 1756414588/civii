package com.game.domain.p;

import com.game.pb.CommonPb.FishCostRecordPB;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FishCostRecord {

    private int propId;
    private int count;


    public FishCostRecordPB.Builder encode() {
        FishCostRecordPB.Builder builder = FishCostRecordPB.newBuilder();
        builder.setPropId(this.propId);
        builder.setCount(this.count);
        return builder;
    }

    public void decode(FishCostRecordPB costRecordPB) {
        this.propId = costRecordPB.getPropId();
        this.count = costRecordPB.getCount();
    }
}
