package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zcp
 * @date 2021/3/2 13:48
 * 诵我真名者,永不见bug
 * 日常任务奖励
 */
@Getter
@Setter
public class StaticTaskDailyAward {
    private int id;
    private List<List<Integer>> award;
    private int needNum;
}
