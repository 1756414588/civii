package com.game.log.domain;

import lombok.Builder;

/**
 *
 * @date 2020/12/30 16:36
 * @description
 */
@Builder
public class ActivityLog {
    private int activityId; //活动ID
    private int awardId;    //奖励ID //礼包ID
    private String giftName;    //礼包名称
    private boolean isAward;    //领取or购买    //是幸运转盘还是至尊转盘
    private long roleId;    //玩家ID
    private int vip;
    private long costGold;  //转盘消耗钻石    //充值挡位
    private int channel;    //渠道

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(activityId).append(",");
        builder.append(awardId).append(",");
        builder.append(giftName).append(",");
        builder.append(isAward ? 0 : 1).append(",");
        builder.append(roleId).append(",");
        builder.append(vip).append(",");
        builder.append(costGold).append(",");
        builder.append(channel);
        return builder.toString();
    }
}
