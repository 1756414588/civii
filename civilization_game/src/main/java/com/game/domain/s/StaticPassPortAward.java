package com.game.domain.s;

import java.util.List;

/**
 * 通信证积分等级的奖励配置类
 * @author CaoBing
 * @date 2020/10/15 20:28
 */
public class StaticPassPortAward {
    /**
     * 主键ID
     */
    private int id;
    /**
     * 奖励ID
     */
    private int awardId;
    /**
     * 等级
     */
    private int lv;
    /**
     * 奖励类型(1.普通奖励,2.进阶奖励)
     */
    private int type;
    /**
     * 奖励列表
     */
    private List<List<Integer>> award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticPassPortAward{" +
               "id=" + id +
               ", awardId=" + awardId +
               ", lv=" + lv +
               ", type=" + type +
               ", award=" + award +
               '}';
    }
}
