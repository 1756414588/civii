package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2021/3/2 13:48
 *
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
