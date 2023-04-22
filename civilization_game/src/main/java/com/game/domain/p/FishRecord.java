package com.game.domain.p;

import com.game.pb.CommonPb.FishRecordPB;
import com.game.pb.SerializePb.SerFishRecordPB;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FishRecord {

    private int recordId;
    private int baitId;
    private int fishId;
    private long fishTime;
    private int count;
    private int size;
    private int exp;
    private int points;
    private int stage;


    public FishRecordPB.Builder encode() {

        FishRecordPB.Builder builder = FishRecordPB.newBuilder();
        builder.setRecordId(this.recordId);
        builder.setFishId(this.fishId);
        builder.setBaitId(this.baitId);
        builder.setFishTime(this.fishTime);
        builder.setCount(this.count);
        builder.setSize(this.size);
        builder.setExp(this.exp);
        builder.setPoints(this.points);
        builder.setStage(this.stage);

        return builder;
    }

    public void decode(FishRecordPB fishRecordPB) {
        this.recordId = fishRecordPB.getRecordId();
        this.fishId = fishRecordPB.getFishId();
        this.baitId = fishRecordPB.getBaitId();
        this.fishTime = fishRecordPB.getFishTime();
        this.count = fishRecordPB.getCount();
        this.size = fishRecordPB.getSize();
        this.exp = fishRecordPB.getExp();
        this.points = fishRecordPB.getPoints();
        this.stage = fishRecordPB.getStage();
    }
}
