package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OmamentLog {
    private long lordId;
    private String nick;
    private int level;
    private int vip;
    /**
     * 勋章ID
     */
    private int omamentId;
    /**
     * 勋章名称
     */
    private String omamentName;
    /**
     * 远征or合成
     */
    private int reason;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lordId).append(",");
        builder.append(nick).append(",");
        builder.append(level).append(",");
        builder.append(vip).append(",");
        builder.append(omamentId).append(",");
        builder.append(omamentName).append(",");
        builder.append(reason);
        return builder.toString();
    }
}
