package com.game.log.domain;

import com.game.util.DateHelper;
import io.netty.util.internal.StringUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 体力使用记录
 */
public class EnergyLog {
    @Getter
    @Setter
    private long lordId;
    @Getter
    @Setter
    private String nick;
    @Getter
    @Setter
    private int lv;
    @Getter
    @Setter
    private int cost;   //消耗或增加的体力数量
    @Getter
    @Setter
    private int reason; //途径

    public EnergyLog() {

    }

    @Builder
    public EnergyLog(long lordId, String nick, int lv, int cost, int reason) {
        this.lordId = lordId;
        this.nick = nick;
        this.lv = lv;
        this.cost = cost;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return lordId + ","
                + nick + StringUtil.COMMA
                + lv + StringUtil.COMMA
                + cost + StringUtil.COMMA
                + reason;
    }
}
