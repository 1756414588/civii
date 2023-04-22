package com.game.domain.p;

import com.game.pb.DataPb;

// 城池怪物信息
public class CityMonsterInfo {
    private int monsterId;
    private int soldier;
    private int maxSoldier;   // if soldier < maxSoldier 说明被打过


    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public int getSoldier() {
        return soldier;
    }

    public void setSoldier(int soldier) {
        this.soldier = soldier;
    }

    public int getMaxSoldier() {
        return maxSoldier;
    }

    public void setMaxSoldier(int maxSoldier) {
        this.maxSoldier = maxSoldier;
    }

    public DataPb.CityMonsterInfo.Builder writeData() {
        DataPb.CityMonsterInfo.Builder builder = DataPb.CityMonsterInfo.newBuilder();
        builder.setMonsterId(monsterId);
        builder.setSoldier(soldier);
        builder.setMaxSoldier(maxSoldier);
        return builder;
    }

    public void readData(DataPb.CityMonsterInfo data) {
        monsterId = data.getMonsterId();
        soldier = data.getSoldier();
        maxSoldier = data.getMaxSoldier();
    }

}
