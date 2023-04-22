package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author cpz
 * @date 2021/2/4 14:27
 * @description
 */
@Builder
@Getter
@Setter
public class SeekLog {
    private long lordId;
    private int level;
    private String nick;
    private int vip;
    private int serarchType;
    private int searchNum;
    private int costGold;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lordId).append(",");
        builder.append(level).append(",");
        builder.append(nick).append(",");
        builder.append(vip).append(",");
        builder.append(serarchType).append(",");
        builder.append(searchNum).append(",");
        builder.append(costGold).append(",");
        return builder.toString();
    }
}
