package com.game.channel.xinkuai;

import com.game.util.Md5Util;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.TreeMap;

/**
 *
 * @date 2021/6/29 9:50
 */
@Getter
@Setter
public class PushActivity extends BaseXinkuai {
    private int activityId;
    private String activityType;
    private int activityAttendNum;
    private int roleLevel;
    private long roleCreateTime;

    @Builder
    PushActivity(int appId, String userId, String servrerId, String roleId, int vip, long actTime,
                 int activityId, String activityType, int activityAttendNum, int roleLevel, Date roleCreateTime) {
        super(appId, userId, servrerId, roleId, vip, actTime);
        this.activityId = activityId;
        this.activityType = activityType;
        this.activityAttendNum = activityAttendNum;
        this.roleLevel = roleLevel;
        this.roleCreateTime = roleCreateTime.getTime() / 1000L;
    }


    public TreeMap<String, Object> makeSign(String sign) {
        TreeMap<String, Object> treeMap = super.makeMap();
        treeMap.put("id", System.currentTimeMillis());
        treeMap.put("activityId", this.activityId);
        treeMap.put("activityType", this.activityType);
        treeMap.put("activityAttendNum", this.activityAttendNum);
        treeMap.put("roleLevel", this.roleLevel);
        treeMap.put("roleCreateTime", this.roleCreateTime);
        StringBuilder builder = new StringBuilder();
        treeMap.forEach((e, f) -> {
            builder.append(e).append("=").append(f).append("&");
        });
        String result = builder.toString();
        result = result.substring(0, result.length() - 1) + sign;
        treeMap.put("sign", Md5Util.string2MD5(result));
        ERROR_LOGGER.error("xinkuai push ->[{}] map->[{}]", result, treeMap);
        return treeMap;
    }
}
