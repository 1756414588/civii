package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

// 城防军
public class WallDefender implements Cloneable {
    private int keyId;
    private int id;
    private int level;
    private int quality;
    private int soldier;
    private int soldierNum;   // 兵力


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public CommonPb.WallDefender.Builder wrapPb() {
        CommonPb.WallDefender.Builder builder = CommonPb.WallDefender.newBuilder();
        builder.setId(id);
        builder.setLevel(level);
        builder.setQuality(quality);
        builder.setSoldier(soldier);
        builder.setKeyId(keyId);
        builder.setSoliderNum(soldierNum);

        return builder;
    }

    public void unwrapPb(CommonPb.WallDefender builder) {
        id = builder.getId();
        level = builder.getLevel();
        quality = builder.getQuality();
        soldier = builder.getSoldier();
        keyId = builder.getKeyId();
        soldierNum = builder.getSoliderNum();
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getSoldier() {
        return soldier;
    }

    public void setSoldier(int soldier) {
        this.soldier = soldier;
    }


    public void readData(DataPb.WallDefenderData builder) {
        id = builder.getId();
        level = builder.getLevel();
        quality = builder.getQuality();
        soldier = builder.getSoldier();
        keyId = builder.getKeyId();
        soldierNum = builder.getSoliderNum();
    }

    public DataPb.WallDefenderData.Builder writeData() {
        DataPb.WallDefenderData.Builder builder = DataPb.WallDefenderData.newBuilder();
        builder.setId(id);
        builder.setLevel(level);
        builder.setQuality(quality);
        builder.setSoldier(soldier);
        builder.setKeyId(keyId);
        builder.setSoliderNum(soldierNum);
        return builder;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getSoldierNum() {
        return soldierNum;
    }

    public void setSoldierNum(int soldierNum) {
        this.soldierNum = soldierNum;
    }

    @Override
    public WallDefender clone() {
        WallDefender wallDefender = null;
        try {
            wallDefender = (WallDefender) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return wallDefender;
    }
}
