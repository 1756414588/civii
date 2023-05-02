package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @date 2021/3/15 14:30
 *
 * 巨型 虫族 buff
 */
@Getter
@Setter
public class StaticGiantZergBuff {
    private int id;
    private String name;
    private int needNum;
    private int value;
}
