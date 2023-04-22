package com.game.log.domain;

import com.game.util.DateHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 *
 * @date 2021/1/23 10:20
 * @description
 */
@Getter
@Setter
public class ActHopeLog {
    private int serverId;
    private int channelId;
    private Date startTime;
    private int level;
    private long lordId;
    private int vip;
    private int costGold;
    private int getGold;

    @Builder
    public ActHopeLog(int serverId, int channelId, Date startTime, int level, long lordId, int vip, int costGold, int getGold) {
        this.serverId = serverId;
        this.channelId = channelId;
        this.startTime = startTime;
        this.level = level;
        this.lordId = lordId;
        this.vip = vip;
        this.costGold = costGold;
        this.getGold = getGold;
    }


    @Override
    public String toString() {
        StringBuffer data = new StringBuffer();
        data.append(serverId).append(",");
        data.append(channelId).append(",");
        data.append(DateHelper.formatDateTime(startTime, DateHelper.format1)).append(",");
        data.append(level).append(",");
        data.append(lordId).append(",");
        data.append(vip).append(",");
        data.append(costGold).append(",");
        data.append(getGold);
        return data.toString();
    }
}
