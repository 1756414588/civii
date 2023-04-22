package com.game.domain.s;

import java.util.List;

/**
 * @author jyb
 * @date 2020/5/23 15:50
 * @description
 */
public class StaticWorldCityType {
    private int cityType;

    private int level ;

    private int period;

    private List<Integer> monsters;

    private List<List<Integer>> output;

    private int needTarget;


    public int getCityType() {
        return cityType;
    }

    public void setCityType(int cityType) {
        this.cityType = cityType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public List<Integer> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<Integer> monsters) {
        this.monsters = monsters;
    }

    public List<List<Integer>> getOutput() {
        return output;
    }

    public void setOutput(List<List<Integer>> output) {
        this.output = output;
    }

    public int getNeedTarget() {
        return needTarget;
    }

    public void setNeedTarget(int needTarget) {
        this.needTarget = needTarget;
    }


}
