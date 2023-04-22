package com.game.domain.s;

import java.util.List;

/**
 * @author jyb
 * @date 2020/4/28 19:10
 * @description
 */
public class StaticRebelZergDrop {
    private int lv;

    private int probability;

    private List<Integer> drop;

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public List<Integer> getDrop() {
        return drop;
    }

    public void setDrop(List<Integer> drop) {
        this.drop = drop;
    }
}
