package com.game.domain.s;

// 活动刷怪表
public class StaticActMonster {
    private int keyId;
    private int monsterLv;
    private int flushType;
    private String beginTime;
    private int lastTime;
    private int maxNum;


    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getMonsterLv() {
        return monsterLv;
    }

    public void setMonsterLv(int monsterLv) {
        this.monsterLv = monsterLv;
    }

    public int getFlushType() {
        return flushType;
    }

    public void setFlushType(int flushType) {
        this.flushType = flushType;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }
}
