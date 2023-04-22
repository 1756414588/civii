package com.game.channel.xinkuai;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * @author zcp
 * @date 2021/6/29 9:51
 */
@Getter
@Setter
public class BaseXinkuai {
    public static Logger ERROR_LOGGER = LoggerFactory.getLogger("error");
    private int appId;
    private String userId;
    private String servrerId;
    private String roleId;
    private int vip;
    private long actTime;

    public BaseXinkuai(int appId, String userId, String servrerId, String roleId, int vip, long actTime) {
        this.appId = appId;
        this.userId = userId;
        this.servrerId = servrerId;
        this.roleId = roleId;
        this.vip = vip;
        this.actTime = actTime;
    }

    public TreeMap<String, Object> makeMap() {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("userId", this.userId);
        treeMap.put("appId", this.appId);
        treeMap.put("serverId", this.servrerId);
        treeMap.put("roleId", this.roleId);
        treeMap.put("vip", this.vip);
        treeMap.put("actTime", actTime);
        return treeMap;
    }
}

