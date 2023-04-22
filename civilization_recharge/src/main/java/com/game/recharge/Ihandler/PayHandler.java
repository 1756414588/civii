package com.game.recharge.Ihandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.game.recharge.manager.ChannelConfigManager;
import com.game.recharge.service.PayOrderService;
import com.game.uc.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface PayHandler {

	Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager, String payName);



    default Map<String, String> JsonToMap(String stObj) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        if (stObj == null || stObj.equals("")) {
            return resultMap;
        }
        JSONObject parseObject = JSON.parseObject(stObj);
        Set<String> keySet = parseObject.keySet();
        for (String string : keySet) {
            if (null != parseObject.get(string) && !parseObject.get(string).equals("")) {
                resultMap.put(string, String.valueOf(parseObject.get(string)));
            }
        }
        return resultMap;
    }

}
