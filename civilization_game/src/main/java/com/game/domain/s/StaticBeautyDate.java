package com.game.domain.s;

import java.util.List;

/**
 * @Description TODO
 * @Date 2021/3/29 9:22
 **/
public class StaticBeautyDate {
    private int star; //星级
    private int num;  //获得奖励数量
    List<List<Integer>> chance; //获得奖励权重
    List<Integer> award; //固定奖励

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public List<List<Integer>> getChance() {
        return chance;
    }

    public void setChance(List<List<Integer>> chance) {
        this.chance = chance;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticBeautyDate{" +
                "star=" + star +
                ", num=" + num +
                ", chance=" + chance +
                ", award=" + award +
                '}';
    }
}
