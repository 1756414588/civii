package com.game.domain.s;

/**
 * @author CaoBing
 * @date 2020/10/20 10:02
 */
public class StaticPassPortLv {
    private int lv;

    private int score;

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "StaticPassPortLv{" +
               "lv=" + lv +
               ", score=" + score +
               '}';
    }
}
