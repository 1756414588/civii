package com.game.constant;

import lombok.Getter;

/**
 *
 * @date 2021/6/17 14:52
 * 母巢之战战斗顺序
 */
@Getter
public class BroodWarRank {
    /**
     * 立即出击
     */
    public static final int ATTACK_NOW = 0;
    /**
     * 优先出击
     */
    public static final int ATTACK_FIRST = 1;
    /**
     * 普通出击
     */
    public static final int ATTACK_COMMON = 2;
}
