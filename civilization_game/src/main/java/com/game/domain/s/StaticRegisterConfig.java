package com.game.domain.s;

import java.util.List;

/**
 * 注册奖励表
 */
public class StaticRegisterConfig {
    private int registerId;
    private List<List<Integer>> awardList;

    public int getRegisterId() {
        return registerId;
    }

    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }
}
