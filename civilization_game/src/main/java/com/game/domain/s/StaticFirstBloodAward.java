package com.game.domain.s;

import java.util.List;

public class StaticFirstBloodAward {
    private int cityLevel;
    private String dsc;
    private List<List<Integer>> award;
    private int cityType;
    private int areaType;


    public int getCityLevel() {
        return cityLevel;
    }

    public void setCityLevel(int cityLevel) {
        this.cityLevel = cityLevel;
    }

    public String getDsc() {
        return dsc;
    }

    public void setDsc(String dsc) {
        this.dsc = dsc;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getCityType() {
        return cityType;
    }

    public void setCityType(int cityType) {
        this.cityType = cityType;
    }

    public int getAreaType() {
        return areaType;
    }

    public void setAreaType(int areaType) {
        this.areaType = areaType;
    }

    public StaticFirstBloodAward() {
    }

    public StaticFirstBloodAward(Integer cityLevel, String dsc, List<List<Integer>> award) {
        this.cityLevel = cityLevel;
        this.dsc = dsc;
        this.award = award;
    }
}
