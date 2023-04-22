package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zcp
 * @date 2021/3/2 13:48
 * 诵我真名者,永不见bug
 * 日常任务
 */
@Getter
@Setter
public class StaticTaskDaily {
    private int id;
    private String name;
    private int needTime;
    private int award;
    private String asset;
}
