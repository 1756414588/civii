package com.game.log.domain;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2021/1/9 10:22
 * @description
 */
@Getter
@Setter
public class DrawCardLog {
    private long lordId;
    private String nick;
    private int lv;
    private int state;//抽卡方式
    private String reward;  //抽卡奖励

    @Override
    public String toString() {
        return new StringBuilder()
                .append(lordId).append(",")
                .append(nick).append(",")
                .append(lv).append(",")
                .toString();
    }
}
