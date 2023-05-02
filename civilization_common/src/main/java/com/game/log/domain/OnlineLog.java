package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2021/5/27 14:43
 *
 */
@Getter
@Setter
public class OnlineLog {
    private int serverId;
    private int channel;
    private int onlineNum;
    private int registerNum;

    public OnlineLog() {

    }

    @Builder
    public OnlineLog(int serverId, int channel, int onlineNum, int registerNum) {
        this.serverId = serverId;
        this.channel = channel;
        this.onlineNum = onlineNum;
        this.registerNum = registerNum;
    }
}
