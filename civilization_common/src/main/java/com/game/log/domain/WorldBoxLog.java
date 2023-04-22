package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author cpz
 * @date 2021/1/23 10:20
 * @description
 */
@Getter
@Setter
public class WorldBoxLog {
    private long lordId;
    private String nick;
    private int vip;
    private int level;
    private int count;  //贡献值
    private int reason; //来源
    private int cur;    //当前拥有
    private int num;    //消耗贡献时获得箱子数


    @Builder
    public WorldBoxLog(String nick, int count, int level, long lordId, int vip, int cur, int reason, int num) {
        this.lordId = lordId;
        this.nick = nick;
        this.vip = vip;
        this.level = level;
        this.count = count;
        this.reason = reason;
        this.cur = cur;
        this.num = num;
    }


    @Override
    public String toString() {
        StringBuffer data = new StringBuffer();
        data.append(lordId).append(",");
        data.append(nick).append(",");
        data.append(vip).append(",");
        data.append(level).append(",");
        data.append(count).append(",");
        data.append(reason).append(",");
        data.append(cur).append(",");
        data.append(num);
        return data.toString();
    }
}
