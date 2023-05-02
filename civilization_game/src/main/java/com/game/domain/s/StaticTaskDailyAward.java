package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @date 2021/3/2 13:48
 *
 * 日常任务奖励
 */
@Getter
@Setter
public class StaticTaskDailyAward {
    private int id;
    private List<List<Integer>> award;
    private int needNum;
}
