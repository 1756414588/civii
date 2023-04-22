package com.game.domain.p;

import com.game.pb.CommonPb;

public class SoloInfo {
    private String lordName;
    private int heroId;
    private int lost;
    private long time;

    public SoloInfo() {
        setTime(System.currentTimeMillis());
    }

    public CommonPb.SoloInfo.Builder wrapPb() {
        CommonPb.SoloInfo.Builder builder = CommonPb.SoloInfo.newBuilder();
        builder.setLordName(getLordName());
        builder.setHeroId(getHeroId());
        builder.setLost(getLost());
        builder.setTime(getTime());
        return builder;
    }


    public String getLordName() {
        return lordName;
    }

    public void setLordName(String lordName) {
        this.lordName = lordName;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
