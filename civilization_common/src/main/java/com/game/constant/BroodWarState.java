package com.game.constant;

import lombok.Getter;

/**
 *
 * @date 2021/6/17 14:52
 * 母巢之战阶段状态
 */
@Getter
public enum BroodWarState {
    //这个阶段可以购买增益
    OPEN_BUY(1, "开启增益购买"),
    //战斗阶段能做的事情
    BEGIN_WAR(2, "开始战斗"),
    //战斗结束后要做的事情
    END_WAR(3, "结束战斗"),
    //等待下一次开启
    WAIT(4, "等待"),
    ;

    BroodWarState(int val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    int val;
    String desc;

    public static BroodWarState get(int val) {
        for (BroodWarState state : values()) {
            if (state.val == val) {
                return state;
            }
        }
        return null;
    }
}
