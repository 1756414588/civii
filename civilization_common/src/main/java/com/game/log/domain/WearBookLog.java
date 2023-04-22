package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WearBookLog {
    private long lordId;
    private String nick;
    private int level;
    private int vip;
    private String bookName;    //兵书名称

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lordId).append(",");
        builder.append(nick).append(",");
        builder.append(level).append(",");
        builder.append(vip).append(",");
        builder.append(bookName);
        return builder.toString();
    }
}
