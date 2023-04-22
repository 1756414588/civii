package com.game.domain.p;

import com.game.pb.DataPb;
import com.game.worldmap.March;


// 友军驻防
public class WallFriend implements Cloneable {
    private int keyId;
    private long lordId;
    private int lordLv;
    private String lordName;
    private int heroId;
    private int soldier;
    private int marchId;
    private long endTime;

    @Override
    public WallFriend clone() {
        WallFriend wallFriend = null;
        try {
            wallFriend = (WallFriend) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return wallFriend;
    }

    // 生成的驻防
    private March march = new March();

    public boolean isOk() {
        return getLordId() != 0;
    }


    public void readData(DataPb.WallFriendData builder) {
        setLordId(builder.getLordId());
        setLordLv(builder.getLordLv());
        setLordName(builder.getName());
        setHeroId(builder.getHeroId());
        setSoldier(builder.getHeroSoldier());
        setMarchId(builder.getMarchId());
        setKeyId(builder.getKeyId());
        setEndTime(endTime);

    }

    public DataPb.WallFriendData.Builder writeData() {
        DataPb.WallFriendData.Builder builder = DataPb.WallFriendData.newBuilder();
        builder.setLordId(getLordId());
        builder.setLordLv(getLordLv());
        builder.setName(getLordName());
        builder.setHeroId(getHeroId());
        builder.setHeroSoldier(getSoldier());
        builder.setMarchId(getMarchId());
        builder.setKeyId(getKeyId());
        endTime = getEndTime();

        return builder;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getLordLv() {
        return lordLv;
    }

    public void setLordLv(int lordLv) {
        this.lordLv = lordLv;
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

    public int getSoldier() {
        return soldier;
    }

    public void setSoldier(int soldier) {
        this.soldier = soldier;
    }

    public int getMarchId() {
        return marchId;
    }

    public void setMarchId(int marchId) {
        this.marchId = marchId;
    }


    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public March getMarch() {
        return march;
    }

    public void setMarch(March march) {
        this.march = march;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
