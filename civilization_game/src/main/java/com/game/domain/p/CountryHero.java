package com.game.domain.p;

import com.game.pb.DataPb;
import com.game.worldmap.Pos;

// 国家名将[不能突破, 但是能进行神级突破]
public class CountryHero {
    private int  heroId;    // 英雄Id
    private long lordId;    // 所属玩家Id, lordId = 0, 表示没有归属人
    private int  state;     // 已激活0(玩家可以使用), 开启1(国家名将界面), 寻访2(玩家军事学院), 未激活3(国家升级或者逃跑)
    private int  fightTimes;// 寻访坐标保持次数
    private Pos  pos = new Pos();  // 寻访坐标,
    private int  heroLv;    // 英雄等级
    private int  occurRound;  // 武将击飞次数 occurRound % 2 == 1 当前地图[上一次出现的坐标], occurRound % 2 == 0 世界地图随机
    private long loyaltyEndTime; // 掉忠诚度的结束时间


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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getFightTimes() {
        return fightTimes;
    }

    public void setFightTimes(int fightTimes) {
        this.fightTimes = fightTimes;
    }

    public Pos getPos() {
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }

    public int getHeroLv() {
        return heroLv;
    }

    public void setHeroLv(int heroLv) {
        this.heroLv = heroLv;
    }

    public void subFightTimes(int fightTimes) {
        if (fightTimes <= 0) {
            return;
        }
        this.fightTimes -= fightTimes;
    }

    public int getOccurRound() {
        return occurRound;
    }

    public void setOccurRound(int flyTimes) {
        this.occurRound = flyTimes;
    }

    public void addOccurRound(int times) {
        this.occurRound += times;
    }

    public void subLv(int level) {
        heroLv -= level;
        heroLv = Math.max(1, heroLv);
    }

    public long getLoyaltyEndTime() {
        return loyaltyEndTime;
    }

    public void setLoyaltyEndTime(long loyaltyEndTime) {
        this.loyaltyEndTime = loyaltyEndTime;
    }

    public DataPb.CountryHeroData.Builder writeData() {
        DataPb.CountryHeroData.Builder builder = DataPb.CountryHeroData.newBuilder();
        builder.setHeroId(heroId);
        builder.setLordId(lordId);
        builder.setState(state);
        builder.setFightTimes(fightTimes);
        builder.setPos(pos.writeData());
        builder.setHeroId(heroLv);
        builder.setOccurRound(occurRound);
        builder.setLoyaltyEndTime(loyaltyEndTime);
        return builder;
    }

    public void readData(DataPb.CountryHeroData data) {
        heroId = data.getHeroId();
        lordId = data.getLordId();
        state = data.getState();
        fightTimes = data.getFightTimes();
        pos.readData(data.getPos());
        heroLv = data.getHeroLv();
        occurRound = data.getOccurRound();
        loyaltyEndTime = data.getLoyaltyEndTime();
    }
}
