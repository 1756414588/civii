package com.game.worldmap;

import lombok.Getter;
import lombok.Setter;

/**
 * 炮塔
 *
 * @author zcp
 * @date 2021/7/9 17:04
 */
@Getter
@Setter
public class Turret extends BroodWar {

    /**
     * 下次炮火进攻时间
     */
    private long nextFireTime;
}
