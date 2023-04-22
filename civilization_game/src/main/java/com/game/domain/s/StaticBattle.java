package com.game.domain.s;

import java.util.List;

public class StaticBattle {
    private int id;
    private List<Integer> floatFactor;
    private List<List<Integer>> restraintFactor;
    private int critiDamage;
    private int missFactor;
    private int minMiss;
    private int maxMiss;
    private int critiFactor;
    private int minCriti;
    private int maxCriti;


    public List<Integer> getFloatFactor() {
        return floatFactor;
    }

    public void setFloatFactor(List<Integer> floatFactor) {
        this.floatFactor = floatFactor;
    }

    public List<List<Integer>> getRestraintFactor() {
        return restraintFactor;
    }

    public void setRestraintFactor(List<List<Integer>> restraintFactor) {
        this.restraintFactor = restraintFactor;
    }

    public int getCritiDamage() {
        return critiDamage;
    }

    public void setCritiDamage(int critiDamage) {
        this.critiDamage = critiDamage;
    }

    public int getMinMiss() {
        return minMiss;
    }

    public void setMinMiss(int minMiss) {
        this.minMiss = minMiss;
    }

    public int getMaxMiss() {
        return maxMiss;
    }

    public void setMaxMiss(int maxMiss) {
        this.maxMiss = maxMiss;
    }

    public int getMinCriti() {
        return minCriti;
    }

    public void setMinCriti(int minCriti) {
        this.minCriti = minCriti;
    }

    public int getMaxCriti() {
        return maxCriti;
    }

    public void setMaxCriti(int maxCriti) {
        this.maxCriti = maxCriti;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMissFactor() {
        return missFactor;
    }

    public void setMissFactor(int missFactor) {
        this.missFactor = missFactor;
    }

    public int getCritiFactor() {
        return critiFactor;
    }

    public void setCritiFactor(int critiFactor) {
        this.critiFactor = critiFactor;
    }
}
