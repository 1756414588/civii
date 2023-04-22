package com.game.domain.p;

import com.game.pb.DataPb;

/**
 * 国家官员信息
 */
public class CtyGovern {
    private int governId; // 官员Id
    private long lordId; // 玩家Id
    private long fight;// 战斗力
    private int vote; // 投票

    // private int refreshTime;

    public CtyGovern(long lordId) {
        this.lordId = lordId;
    }

    public CtyGovern(long lordId, long fight) {
        this.lordId = lordId;
        this.fight = fight;
    }

    public CtyGovern(DataPb.CountryGovernData data) {
        this.governId = data.getGovernId();
        this.lordId = data.getLordId();
        this.vote = data.getVote();
        // this.refreshTime = data.getRefreshTime();
    }

    public int getGovernId() {
        return governId;
    }

    public void setGovernId(int governId) {
        this.governId = governId;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public long getFight() {
        return fight;
    }

    public void setFight(long fight) {
        this.fight = fight;
    }

    public DataPb.CountryGovernData ser() {
        DataPb.CountryGovernData.Builder builder = DataPb.CountryGovernData.newBuilder();
        builder.setLordId(this.lordId);
        builder.setGovernId(this.governId);
        builder.setVote(this.vote);
        return builder.build();
    }

    @Override
    public String toString() {
        return "governId:" + governId +
                ",lordId:" + lordId +
                ",fight:" + fight +
                ",vote:" + vote;
    }
}
