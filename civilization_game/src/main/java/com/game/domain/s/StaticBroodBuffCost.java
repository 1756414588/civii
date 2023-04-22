package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zcp
 * @date 2021/7/20 9:51
 */
@Getter
@Setter
public class StaticBroodBuffCost {
    private int id;
    private int type;
    private int start_index;
    private int end_index;
    private int cost_gold;
}
