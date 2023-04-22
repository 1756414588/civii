package com.game.log.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ManoeuvreLog {
    private long roleId;      // 角色id

    private String nick;      // 角色名称

    private int level;        // 角色等级

    private int vipLevel;     // vip等级

    private int changePoint;   // 变化积分

    private int itemId;       // 物品id

    private int itemNum;      // 物品数量

    private int source;       // 来源

    private int type;        // 类型  1=消耗   2= 获得

    private int point;      // 剩余积分


    @Override
    public String toString() {
        return new StringBuilder()
                .append(roleId).append(",")
                .append(nick).append(",")
                .append(level).append(",")
                .append(vipLevel).append(",")
                .append(changePoint).append(",")
                .append(itemId).append(",")
                .append(itemNum).append(",")
                .append(source).append(",")
                .append(type).append(",")
                .append(point).append(",")
                .toString();
    }


}
