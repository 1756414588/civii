package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 阵营日志
 *
 * @author zcp
 * @date 2021/8/19 16:19
 */
@Getter
@Setter
public class GloverLog {
    /**
     * 角色ID
     */
    private long lordId;
    private String nick;
    private int vip;
    private int lv;
    private int channel;
    /**
     * 选票变化
     */
    private int ticket;
    /**
     * 变化后的选票
     */
    private int vote;

    public GloverLog() {
    }

    @Builder
    public GloverLog(long lordId, String nick, int vip, int lv, int channel, int ticket, int vote) {
        this.lordId = lordId;
        this.nick = nick;
        this.vip = vip;
        this.lv = lv;
        this.channel = channel;
        this.ticket = ticket;
        this.vote = vote;
    }

    @Override
    public String toString() {
        StringBuffer data = new StringBuffer();
        data.append(lordId).append(",");
        data.append(nick).append(",");
        data.append(vip).append(",");
        data.append(lv).append(",");
        data.append(channel).append(",");
        data.append(ticket).append(",");
        data.append(vote);
        return data.toString();
    }
}
