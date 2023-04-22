package com.game.domain.s;

import java.util.List;

/**
 *
 * @date 2020/10/29 18:29
 * @description
 */
public class StaticCityGame {

    private int id;
    private int type;
    private List<List<Integer>> award;
    private int maxTimes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getMaxTimes() {
        return maxTimes;
    }

    public void setMaxTimes(int maxTimes) {
        this.maxTimes = maxTimes;
    }
}
