package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WarBookLog {
    private long lordId;
    private String nick;
    private int level;
    private int vip;
    private String bookName;    //兵书名称
    private int reason;      //来源 道具兑换 分解获得
    private int cost;           //兑换消耗

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lordId).append(",");
        builder.append(nick).append(",");
        builder.append(level).append(",");
        builder.append(vip).append(",");
        builder.append(bookName).append(",");
        builder.append(reason).append(",");
        builder.append(cost);
        return builder.toString();
    }
}
