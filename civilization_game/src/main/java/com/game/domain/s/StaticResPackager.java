package com.game.domain.s;

import com.game.domain.Award;

import java.util.List;

/**
 *
 * @date 2020/1/13 11:19
 * @description
 */
public class StaticResPackager {

    private int id;
    private int time;
    private int resType;
    private int resNum;
    private List<Integer> award;
    private int costGold;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getResType() {
        return resType;
    }

    public void setResType(int resType) {
        this.resType = resType;
    }

    public int getResNum() {
        return resNum;
    }

    public void setResNum(int resNum) {
        this.resNum = resNum;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getCostGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }


    public Award getResAward() {
        Award award = new Award();
        award.setType(this.award.get(0));
        award.setId(this.award.get(1));
        award.setCount(this.award.get(2));
        return award;
    }
}
