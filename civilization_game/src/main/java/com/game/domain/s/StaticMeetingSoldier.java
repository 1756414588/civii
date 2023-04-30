package com.game.domain.s;

import java.util.Map;

/**
 *
 * @date 2019/12/11 15:31
 * @description
 */
public class StaticMeetingSoldier {
    private int id;
    private int level;
    private int soldier;
    private Map<Integer, Integer> effects;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSoldier() {
        return soldier;
    }

    public void setSoldier(int soldier) {
        this.soldier = soldier;
    }

    public Map<Integer, Integer> getEffects() {
        return effects;
    }

    public void setEffects(Map<Integer, Integer> effects) {
        this.effects = effects;
    }
}
