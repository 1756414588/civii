package com.game.domain.s;

import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Award;

import java.util.List;

public class StaticActCommand {
    public static final int MAIN_CTTY = 1;
    public static final int TEC_INSTITUTE = 2;
    public static final int PLAYER_LEVEL = 3;


    private int keyId;
    private int awardId;
    private int level;
    private String name;
    private List<List<Integer>> awardList;
    private String asset;
    private String desc;
    private int ratio;
    private String limitDesc;
    private List<Integer> Limit;

    private List<CommonPb.Award> awardPbList;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public String getLimitDesc() {
        return limitDesc;
    }

    public void setLimitDesc(String limitDesc) {
        this.limitDesc = limitDesc;
    }

    public List<Integer> getLimit() {
        return Limit;
    }

    public void setLimit(List<Integer> limit) {
        Limit = limit;
    }

    public List<Award> getAwardPbList() {
        return awardPbList;
    }

    public void setAwardPbList(List<Award> awardPbList) {
        this.awardPbList = awardPbList;
    }
}
