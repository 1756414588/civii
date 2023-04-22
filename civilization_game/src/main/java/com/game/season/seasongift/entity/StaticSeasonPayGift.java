package com.game.season.seasongift.entity;

import java.util.Date;
import java.util.List;

public class StaticSeasonPayGift {
    private int payMoneyId;

    private int awardId;

    private String name;

    private int money;

    private int limit;

    private List<List<Integer>> sellList;

    private String desc;

    private int vipExp;

    private String percent;

    private int sort;
    private int nextJump;
    private int position;
    private String asset;
    private int openBegin;
    private int openDays;
    private Date beginTime;
    private Date endTime;
    private int limitDate;
    private int illustration;// 是否显示指引
    private int levelDisplay;// 显示等级,0为无限制

    public int getPayMoneyId() {
        return payMoneyId;
    }

    public void setPayMoneyId(int payMoneyId) {
        this.payMoneyId = payMoneyId;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<List<Integer>> getSellList() {
        return sellList;
    }

    public void setSellList(List<List<Integer>> sellList) {
        this.sellList = sellList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getNextJump() {
        return nextJump;
    }

    public void setNextJump(int nextJump) {
        this.nextJump = nextJump;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public int getOpenBegin() {
        return openBegin;
    }

    public void setOpenBegin(int openBegin) {
        this.openBegin = openBegin;
    }

    public int getOpenDays() {
        return openDays;
    }

    public void setOpenDays(int openDays) {
        this.openDays = openDays;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getLimitDate() {
        return limitDate;
    }

    public void setLimitDate(int limitDate) {
        this.limitDate = limitDate;
    }

    public int getIllustration() {
        return illustration;
    }

    public void setIllustration(int illustration) {
        this.illustration = illustration;
    }

    public int getLevelDisplay() {
        return levelDisplay;
    }

    public void setLevelDisplay(int levelDisplay) {
        this.levelDisplay = levelDisplay;
    }
}
