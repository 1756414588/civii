package com.game.uc.Ihandler;

import com.game.pay.channel.BaseChanelConfig;

public interface LoginHandler {

	boolean login(BaseChanelConfig channelConfig, String account, String token);



//    default Map<String, String> JsonToMap(String stObj) throws Exception {
//        Map<String, String> resultMap = new HashMap<>();
//        if (stObj == null || stObj.equals("")) {
//            return resultMap;
//        }
//        JSONObject parseObject = JSON.parseObject(stObj);
//        Set<String> keySet = parseObject.keySet();
//        for (String string : keySet) {
//            if (null != parseObject.get(string) && !parseObject.get(string).equals("")) {
//                resultMap.put(string, String.valueOf(parseObject.get(string)));
//            }
//        }
//        return resultMap;
//    }

}
