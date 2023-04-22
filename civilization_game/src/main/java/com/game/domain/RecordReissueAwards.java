package com.game.domain;


import com.alibaba.fastjson.annotation.JSONType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description TODO
 * @Date 2021/3/19 17:43
 **/
@Setter
@Getter
@JSONType(orders = {"lordId", "accountKey", "nick", "activityId", "activityName", "footId", "awardId", "type", "name", "state"})
public class RecordReissueAwards implements Serializable {
    private long lordId;
    private int accountKey;
    private String nick;
    private int activityId;
    private String activityName;
    private int footId;
    private int awardId;
    private int type;
    private String name;
    private int state;  //基金活动参与了多少天

    public RecordReissueAwards(long lordId, int accountKey, String nick, int activityId, String activityName, int footId, int awardId, int type, String name, int state) {
        this.lordId = lordId;
        this.accountKey = accountKey;
        this.nick = nick;
        this.activityId = activityId;
        this.activityName = activityName;
        this.footId = footId;
        this.awardId = awardId;
        this.type = type;
        this.name = name;
        this.state = state;
    }

    public RecordReissueAwards() {
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lordId).append(",");
        builder.append(accountKey).append(",");
        builder.append(nick).append(",");
        builder.append(activityId).append(",");
        builder.append(activityName).append(",");
        builder.append(footId).append(",");
        builder.append(awardId).append(",");
        builder.append(type).append(",");
        builder.append(name).append(",");
        builder.append(state);
        return builder.toString();
    }
}
