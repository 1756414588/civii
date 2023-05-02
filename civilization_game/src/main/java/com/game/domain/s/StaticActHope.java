package com.game.domain.s;

/**
 *
 */
public class StaticActHope {
    /**
     *许愿池等级
     */
    private Integer level;
    /**
     * 花费钻石
     */
    private Integer cost;
    /**
     * 最小获得数量
     */
    private Integer mixAward;
    /**
     * 最大获得数量
     */
    private Integer maxAward;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getMixAward() {
        return mixAward;
    }

    public void setMixAward(Integer mixAward) {
        this.mixAward = mixAward;
    }

    public Integer getMaxAward() {
        return maxAward;
    }

    public void setMaxAward(Integer maxAward) {
        this.maxAward = maxAward;
    }

    public StaticActHope() {
    }

    public StaticActHope(Integer level, Integer cost, Integer mixAward, Integer maxAward) {
        this.level = level;
        this.cost = cost;
        this.mixAward = mixAward;
        this.maxAward = maxAward;
    }
}
