package com.game.domain.s;

import java.util.List;

/**
 * @author liyue
 */
public class StaticActEquipUpdate {
    public static final long WASH_CONUT = 101;
    public static final long PAY_CONUT = 102;
    /**奖励keyId*/
    private int keyId;
    /**奖励组Id*/
    private int awardId;
    /**排序*/
    private int sortId;
    /**条件*/
    private int cond;
    /**条件参数*/
    private String param;
    /**奖励*/
    private List<List<Integer>> awardList;
    /**表述*/
    private String desc;
    /**花费*/
    private int cost;
    /**条件类型*/
    private int type;

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

    public int getSortId() {
        return sortId;
    }

    public void setSortId(int sortId) {
        this.sortId = sortId;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public StaticActEquipUpdate() {
    }

    public StaticActEquipUpdate(int keyId, int awardId, int sortId, int cond, String param, List<List<Integer>> awardList, String desc, int cost, int type) {
        this.keyId = keyId;
        this.awardId = awardId;
        this.sortId = sortId;
        this.cond = cond;
        this.param = param;
        this.awardList = awardList;
        this.desc = desc;
        this.cost = cost;
        this.type = type;
    }
}
