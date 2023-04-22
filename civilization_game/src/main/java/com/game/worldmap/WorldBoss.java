package com.game.worldmap;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

public class WorldBoss {
    private int country;
	private int monsterId;
	private int soldier;
	private int maxSoldier;
	private boolean isKilled;

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


	public WorldBoss() {
	}

	public WorldBoss(DataPb.WorldBossData bossData) {
        this.country = bossData.getCountry();
		this.monsterId = bossData.getMonsterId();
		this.soldier = bossData.getSoldier();
		this.maxSoldier = bossData.getMaxSoldier();
		this.isKilled = bossData.getIsKilled();
	}

	public void readData(DataPb.WorldBossData builder) {
	    country = builder.getCountry();
	    monsterId = builder.getMonsterId();
	    soldier = builder.getSoldier();
	    maxSoldier = builder.getMaxSoldier();
	    isKilled = builder.getIsKilled();
    }

    public DataPb.WorldBossData.Builder writeData() {
        DataPb.WorldBossData.Builder builder = DataPb.WorldBossData.newBuilder();
        builder.setMonsterId(monsterId);
        builder.setSoldier(soldier);
        builder.setCountry(country);
        builder.setMaxSoldier(maxSoldier);
        builder.setIsKilled(isKilled);
        return builder;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public CommonPb.WorldBoss.Builder wrapPb() {
        CommonPb.WorldBoss.Builder builder = CommonPb.WorldBoss.newBuilder();
        builder.setBossId(monsterId);
        builder.setBossSoldier(soldier);
        return builder;
    }

    public int getMaxSoldier() {
	    return maxSoldier;
    }

    public void setMaxSoldier(int maxSoldier) {
        this.maxSoldier = maxSoldier;
    }

    public boolean isKilled() {
        return isKilled;
    }

    public void setKilled(boolean killed) {
        isKilled = killed;
    }
}
