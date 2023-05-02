package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @date 2021/7/5 17:49
 */
@Getter
@Setter
public class StaticBroodWarBuff {
    private int id;
    private List<List<Integer>> buff;
    private List<List<Long>> cost_res;
    private int gold_cost;
    private int lv;
    private int next_buffId;
    private int rate_res;
    private int type;
}
