package com.game.constant;

/**
 * @author cpz
 * @date 2021/1/5 17:48
 * @description
 */
public enum WorldBoxState {
    TURNED_ON(0),//已开启
    OPENING(1),//开启中
    WAIT(2),    //等待


    ;

    int val;

    WorldBoxState(int val) {
        this.val = val;
    }

    public int getVal() {
        return this.val;
    }
}
