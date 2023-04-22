package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/10/10 14:29
 *
 * s_act__pay_arms 军备促销物品的计费点
 */
public class StaticActPayArms {
    /**
     * 主键
     */
    private int payArmsId;
    /**
     * 奖励ID
     */
    private int awardId;
    /**
     * 原价
     */
    private int original;
    /**
     * 消耗的钻石
     */
    private int topup;
    /**
     * 限制数量
     */
    private int limit;
    /**
     * 售卖的物品集合
     */
    private List<List<Integer>> sellList;
    /**
     * 阵营积分
     */
    private int score;
    /**
     * 描述
     */
    private String desc;

    public int getPayArmsId() {
        return payArmsId;
    }

    public void setPayArmsId(int payArmsId) {
        this.payArmsId = payArmsId;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getOriginal() {
        return original;
    }

    public void setOriginal(int original) {
        this.original = original;
    }

    public int getTopup() {
        return topup;
    }

    public void setTopup(int topup) {
        this.topup = topup;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticActPayArms{" +
               "payArmsId=" + payArmsId +
               ", awardId=" + awardId +
               ", original=" + original +
               ", topup=" + topup +
               ", limit=" + limit +
               ", sellList=" + sellList +
               ", score=" + score +
               ", desc='" + desc + '\'' +
               '}';
    }
}
