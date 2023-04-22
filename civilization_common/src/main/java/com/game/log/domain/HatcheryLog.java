package com.game.log.domain;

import io.netty.util.internal.StringUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author cpz
 * @date 2021/1/4 10:23
 * @description
 */
@Getter
@Setter
@Builder
public class HatcheryLog {
    public static final int TOTAL = 0;
    public static final int MULTI = 1;
    public static final int SCORE_MAIL = 2;
    public static final int EXCHANGE = 3;
    public static final int DIG_PAPER = 4;

    private long lordId;
    private String nick;
    private int lv;
    private int title;
    private int point;  //积分变化+加-扣
    private int source;  //来源杀敌 连杀 母巢购买

    @Override
    public String toString() {
        return new StringBuilder()
                .append(lordId).append(",")
                .append(nick).append(",")
                .append(lv).append(",")
                .append(title).append(",")
                .append(point).append(",")
                .append(source)
                .toString();
    }
}
