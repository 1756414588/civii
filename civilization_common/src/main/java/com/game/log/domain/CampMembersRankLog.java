package com.game.log.domain;

import lombok.Builder;

/**
 * @Description TODO
 * @Date 2021/3/22 19:45
 **/
@Builder
public class CampMembersRankLog {
    private int activityId; //活动ID
    private long roleId;    //玩家ID
    private int vip;
    private int channel;//渠道
    private int rank;    //排名

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(activityId).append(",");
        builder.append(roleId).append(",");
        builder.append(vip).append(",");
        builder.append(channel).append(",");
        builder.append(rank);
        return builder.toString();
    }
}
