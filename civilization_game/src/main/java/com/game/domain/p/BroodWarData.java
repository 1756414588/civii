package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.worldmap.BroodWar;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zcp
 * @date 2021/7/29 15:38
 */
@Getter
@Setter
public class BroodWarData {
    private int cityId;
    private byte[] attackQueue;
    private byte[] defenceQueue;
    private int state;
    private long nextAttackTime;
    private long endTime;
    private int defenceCountry;
    private long openBuyBuffTime;
    private byte[] reports;
    private int rank;
    private int lastCountry;
    private long dictator;
    private byte[] occupyTime;
    private byte[] occupyPercentage;

    /**
     * 数据存储
     *
     * @param broodWar
     */
    public void dserData(BroodWar broodWar) {
        this.cityId = Long.valueOf(broodWar.getId()).intValue();
        if(broodWar.getState() != null){
            this.state = broodWar.getState().getVal();
        }
        this.nextAttackTime = broodWar.getNextAttackTime();
        this.endTime = broodWar.getEndTime();
        this.defenceCountry = broodWar.getDefenceCountry();
        this.openBuyBuffTime = broodWar.getOpenBuyBuffTime();
        this.rank = broodWar.getRank();
        this.lastCountry = broodWar.getLastCountry();
        this.dictator = broodWar.getDictator();
        DataPb.TeamList.Builder attack = DataPb.TeamList.newBuilder();
        broodWar.getAttackQueue().forEach(e -> {
            attack.addTeams(e.wrapPb());
        });
        this.attackQueue = attack.build().toByteArray();
//        DataPb.TeamList.Builder defence = DataPb.TeamList.newBuilder();
//        broodWar.getDefenceQueue().forEach(e -> {
//            defence.addTeams(e.wrapPb());
//        });
//        this.defenceQueue = defence.build().toByteArray();
//        DataPb.BroodWarReportDataList.Builder reports = DataPb.BroodWarReportDataList.newBuilder();
//        broodWar.getReports().forEach(e -> {
//            reports.addDatas(e.wrapPb());
//        });
//        this.reports = reports.build().toByteArray();
        DataPb.TwoIntList.Builder twoInts = DataPb.TwoIntList.newBuilder();
        broodWar.getOccupyTime().forEach((e, f) -> {
            twoInts.addTwoInts(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
        });
        this.occupyTime = twoInts.build().toByteArray();
        DataPb.TwoIntList.Builder occupyPercentageArr = DataPb.TwoIntList.newBuilder();
        broodWar.getOccupyPercentage().forEach((k, v) -> {
            occupyPercentageArr.addTwoInts(CommonPb.TwoInt.newBuilder().setV1(k).setV2(v).build());
        });
        this.occupyPercentage = occupyPercentageArr.build().toByteArray();
    }
}
