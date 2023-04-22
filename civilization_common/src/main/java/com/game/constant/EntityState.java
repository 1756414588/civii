package com.game.constant;

/**
 * 虫子状态
 */
public enum EntityState {
    SURVIVAL(0),
    DEATH(1),
    ;

    EntityState(int val) {
        this.val = val;
    }

    public int get() {
        return val;
    }

    int val;
}
