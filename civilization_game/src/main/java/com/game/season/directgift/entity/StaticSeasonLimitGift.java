package com.game.season.directgift.entity;

import java.util.List;

public class StaticSeasonLimitGift {

    private int keyId;
    /**
     * 奖励id
     */
    private List<List<Integer>> awardList;
    /**
     * 礼包名称
     */
    private String name;
    /**
     * 总价值钻石数
     */
    private int display;
    /**
     * 价格（RMB）
     */
    private int money;
    /**
     * 限购次数
     */
    private int limit;
    /**
     * 限时时间（单位/秒）
     */
    private int time;
    /**
     * 描述
     */
    private String desc;
    /**
     * 背景图
     */
    private String asset;
    /**
     * 标题艺术字
     */
    private String icon;

    private int awardId;

    private int actId;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }


    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getActId() {
        return actId;
    }

    public void setActId(int actId) {
        this.actId = actId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
