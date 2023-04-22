package com.game.domain.s;

import java.util.List;

/**
 * 通行证购买的计费点配置
 * @author CaoBing
 * @date 2020/10/22 19:00
 */
public class StaticPayPassPort {
    /**
     * 主键ID
     */
    private int keyId;   
    /**
     * 奖励ID
     */
    private int awardId;
    /**
     * 金额
     */
    private int money;
    /**
     * 限购数量
     */
    private int limit;
    /**
     * 售卖的物品
     */
    private List<List<Integer>> sellList;
    /**
     *  展示的物品
     */
    private List<List<Integer>> viewList;
    /**
     * 购买后获得VIP经验
     */
    private int vipExp;

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

    public List<List<Integer>> getViewList() {
        return viewList;
    }

    public void setViewList(List<List<Integer>> viewList) {
        this.viewList = viewList;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    @Override
    public String toString() {
        return "StaticPayPassPort{" +
               "keyId=" + keyId +
               ", awardId=" + awardId +
               ", money=" + money +
               ", limit=" + limit +
               ", sellList=" + sellList +
               ", viewList=" + viewList +
               ", vipExp=" + vipExp +
               '}';
    }
}
