package com.game.domain.p;

import com.game.pb.DataPb;

// pvp hero
public class PvpHero {
	private int heroId;          // 英雄
	private long lordId;         // 玩家
	private int rebornTimes;     // 复活次数(回城之后清零)
	private int mutilKill;       // 连杀的士兵个数[活动开启时清零, 回城后清零]
    private int placeId;         // 处于的位置: 0.回城 1.上 2.左 3.右 4.中
    private int country;         // 国家
    private int defenceTimes;    // 回防次数[回城之后清零]
    private int attackTimes;     // 偷袭次数[回城之后清零]
    private int soloTimes;       // 单挑次数[回城之后清零]
    private long deadTime;       // 死亡倒计时
    private int freeeRebornTimes;//免费复活次数

	public int getRebornTimes() {
		return rebornTimes;
	}

	public void setRebornTimes(int rebornTimes) {
		this.rebornTimes = rebornTimes;
	}

	public int getMutilKill() {
		return mutilKill;
	}

	public void setMutilKill(int mutilKill) {
		this.mutilKill = mutilKill;
	}

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public boolean isHeroInBattle(int paramHeroId, long paramLordId) {
	    return this.heroId == paramHeroId && this.lordId == paramLordId;
    }

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public DataPb.PvpHeroData.Builder writeData() {
        DataPb.PvpHeroData.Builder builder = DataPb.PvpHeroData.newBuilder();
        builder.setHeroId(heroId);
        builder.setLordId(lordId);
        builder.setRebornTimes(rebornTimes);
        builder.setMutilKill(mutilKill);
        builder.setPlaceId(placeId);
        builder.setCountry(country);
        builder.setDefenceTimes(defenceTimes);
        builder.setAttackTimes(attackTimes);
        builder.setSoloTimes(soloTimes);
        builder.setDeadTime(deadTime);
        builder.setFreeeRebornTimes(freeeRebornTimes);
        return builder;
    }

    public void readData(DataPb.PvpHeroData data) {
	    heroId = data.getHeroId();
	    lordId = data.getLordId();
	    rebornTimes = data.getRebornTimes();
	    mutilKill = data.getMutilKill();
	    placeId = data.getPlaceId();
	    country = data.getCountry();
	    defenceTimes = data.getDefenceTimes();
	    attackTimes = data.getAttackTimes();
	    soloTimes = data.getSoloTimes();
	    deadTime = data.getDeadTime();
	    freeeRebornTimes = data.getFreeeRebornTimes();
    }

    public int getDefenceTimes() {
        return defenceTimes;
    }

    public void setDefenceTimes(int defenceTimes) {
        this.defenceTimes = defenceTimes;
    }

    public int getAttackTimes() {
        return attackTimes;
    }

    public void setAttackTimes(int attackTimes) {
        this.attackTimes = attackTimes;
    }

    public boolean isEqual(PvpHero pvpHero) {
	    return this.heroId == pvpHero.getHeroId() && this.lordId == pvpHero.getLordId();
    }

    public int getSoloTimes() {
        return soloTimes;
    }

    public void setSoloTimes(int soloTimes) {
        this.soloTimes = soloTimes;
    }

    public long getDeadTime() {
        return deadTime;
    }

    public void setDeadTime(long deadTime) {
        this.deadTime = deadTime;
    }

    public int getFreeeRebornTimes() {
        return freeeRebornTimes;
    }

    public void setFreeeRebornTimes(int freeeRebornTimes) {
        this.freeeRebornTimes = freeeRebornTimes;
    }
}
