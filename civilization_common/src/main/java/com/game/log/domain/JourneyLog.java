package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 远征通关日志
 */
@Getter
@Setter
@Builder
public class JourneyLog {
    private long lordId;
    private String nick;
    private int level;
    private int vip;
    /**
     * 关卡
     */
    private int journeyId;
    /**
     * 结果
     */
    private int result;
    /**
     * 最大关卡
     */
    private int maxJourneyLog;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lordId).append(",");
        builder.append(nick).append(",");
        builder.append(level).append(",");
        builder.append(vip).append(",");
        builder.append(result).append(",");
        builder.append(maxJourneyLog);
        return builder.toString();
    }
}
