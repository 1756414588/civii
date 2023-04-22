package com.game.uc.service;

import com.alibaba.fastjson.JSONObject;
import com.game.uc.Message;
import com.game.uc.manager.AccountManager;
import com.game.uc.manager.ServerManager;
import com.game.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jyb
 * @date 2020/6/2 17:00
 * @description
 */
@Service
public class PlayerService {
    @Autowired
    private AccountManager accountManager;

    @Autowired
    private ServerManager serverManager;

    public Message closeRole(long roleId, int serverId) {
        String serverUrl = serverManager.getServerUrl(serverId);
        String url = serverUrl + "/" + "player/closeRole.do";
        Map<String, String> map = new HashMap<>();
        map.put("roleId", String.valueOf(roleId));
        String msg = HttpUtil.sendPost(url, map);
        Message message = JSONObject.parseObject(msg, Message.class);
        return message;
    }

    public Message openRole(long roleId, int serverId) {
        String serverUrl = serverManager.getServerUrl(serverId);
        String url = serverUrl + "/" + "player/openRole.do";
        Map<String, String> map = new HashMap<>();
        map.put("roleId", String.valueOf(roleId));
        String msg = HttpUtil.sendPost(url, map);
        Message message = JSONObject.parseObject(msg, Message.class);
        return message;
    }


    public Message closeSpeak(long roleId, int serverId,long endTime) {
        String serverUrl = serverManager.getServerUrl(serverId);
        String url = serverUrl + "/" + "player/closeSpeak.do";
        Map<String, String> map = new HashMap<>();
        map.put("roleId", String.valueOf(roleId));
        map.put("endTime", String.valueOf(endTime));
        String msg = HttpUtil.sendPost(url, map);
        Message message = JSONObject.parseObject(msg, Message.class);
        return message;
    }

    public Message openSpeak(long roleId, int serverId) {
        String serverUrl = serverManager.getServerUrl(serverId);
        String url = serverUrl + "/" + "player/openSpeak.do";
        Map<String, String> map = new HashMap<>();
        map.put("roleId", String.valueOf(roleId));
        String msg = HttpUtil.sendPost(url, map);
        Message message = JSONObject.parseObject(msg, Message.class);
        return message;
    }
}
