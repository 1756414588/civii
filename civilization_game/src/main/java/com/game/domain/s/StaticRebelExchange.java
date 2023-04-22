package com.game.domain.s;

/**
 * @author jyb
 * @date 2020/4/28 19:30
 * @description
 */
public class StaticRebelExchange {
    private int id;

    private String name;

    private int awardType;


    private int awardId;

    private int awardNum;

    private int needNum;

    private int limitLv;

    private int maxExNum;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAwardType() {
        return awardType;
    }

    public void setAwardType(int awardType) {
        this.awardType = awardType;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getAwardNum() {
        return awardNum;
    }

    public void setAwardNum(int awardNum) {
        this.awardNum = awardNum;
    }

    public int getNeedNum() {
        return needNum;
    }

    public void setNeedNum(int needNum) {
        this.needNum = needNum;
    }

    public int getLimitLv() {
        return limitLv;
    }

    public void setLimitLv(int limitLv) {
        this.limitLv = limitLv;
    }

    public int getMaxExNum() {
        return maxExNum;
    }

    public void setMaxExNum(int maxExNum) {
        this.maxExNum = maxExNum;
    }
}
