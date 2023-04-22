package com.game.domain.s;

import java.util.List;

/**
 * @Description TODO
 * @Date 2021/3/29 9:22
 **/
public class StaticBeautyDateAward {
    private int id;
    private List<Integer>  award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticBeautyDateAward{" +
                "id=" + id +
                ", award=" + award +
                '}';
    }
}
