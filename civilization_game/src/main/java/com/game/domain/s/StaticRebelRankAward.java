package com.game.domain.s;

import java.util.List;

/**
 *
 * @date 2020/4/28 19:13
 * @description
 */
public class StaticRebelRankAward {
    private int id ;

    private int type;

    private List<Integer> bayssegmenting;

    private List<List<Integer>> award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getBayssegmenting() {
        return bayssegmenting;
    }

    public void setBayssegmenting(List<Integer> bayssegmenting) {
        this.bayssegmenting = bayssegmenting;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
