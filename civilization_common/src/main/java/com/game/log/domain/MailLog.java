package com.game.log.domain;

import lombok.Builder;

/**
 *
 * @date 2021/2/2 14:12
 * @description
 */
@Builder
public class MailLog {
    private long lordId;
    private String nick;
    private int level;
    private int vip;
    private int mailId;// s_mail表的mailId
    private String msg;


    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(lordId).append(",");
        string.append(nick).append(",");
        string.append(level).append(",");
        string.append(vip).append(",");
        string.append(mailId).append(",");
        string.append(msg.replaceAll(",", "，"));
        return string.toString();
    }
}
