package com.game.domain.p;

import com.game.domain.s.StaticBeautyBase;
import com.game.pb.DataPb;
import com.game.util.TimeHelper;

/**
 * 2020年5月29日
 *
 * @CaoBing halo_game
 * BeautyData.java
 **/

public class BeautyData implements Cloneable {
    private int keyId;
    private int intimacyValue;// 亲密度
    private int star;// 星级
    private int killId;// 亲密度技能id
    private int seekingTimes; // 美女约会次数
    private long freeSeekingEndTime; // 上一次约会次数重置的时间
    private int isUnlock; // 是否解锁  0未解锁  1 已解锁
    private int clickCount; // 点击美女次数

    /**
     * 启动初始化
     *
     * @param
     */
    public BeautyData(DataPb.BeautyData beautyData) {
        this.keyId = beautyData.getKeyId();
        this.intimacyValue = beautyData.getIntimacyValue();
        this.star = beautyData.getStar();
        this.killId = beautyData.getKillId();
        this.seekingTimes = beautyData.getSeekingTimes();
        this.freeSeekingEndTime = beautyData.getFreeSeekingEndTime();
        this.isUnlock = beautyData.getIsUnlock();
        this.clickCount = beautyData.getClickCount();
    }

    public BeautyData(StaticBeautyBase staticBeautyBase) {
        this.keyId = staticBeautyBase.getKeyId();
        this.isUnlock = 0;
        this.star = 0;
        this.intimacyValue = 0;
    }

    public BeautyData() {
    }


    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getIntimacyValue() {
        return intimacyValue;
    }

    public void setIntimacyValue(int intimacyValue) {
        this.intimacyValue = intimacyValue;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getSeekingTimes() {
        return seekingTimes;
    }

    public void setSeekingTimes(int seekingTimes) {
        this.seekingTimes = seekingTimes;
    }

    public long getFreeSeekingEndTime() {
        return freeSeekingEndTime;
    }

    public void setFreeSeekingEndTime(long freeSeekingEndTime) {
        this.freeSeekingEndTime = freeSeekingEndTime;
    }

    public int getKillId() {
        return killId;
    }

    public void setKillId(int killId) {
        this.killId = killId;
    }

    public int getIsUnlock() {
        return isUnlock;
    }

    public void setIsUnlock(int isUnlock) {
        this.isUnlock = isUnlock;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    @Override
    public BeautyData clone() {
        BeautyData beautyData = null;
        try {
            beautyData = (BeautyData) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return beautyData;
    }
}
