package com.game.channel.xinkuai;

import com.game.util.Md5Util;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.TreeMap;

/**
 *
 * @date 2021/6/29 9:50
 */
@Getter
@Setter
public class PushGold extends BaseXinkuai {
    private int id;
    private int coinNum;

    @Builder
    PushGold(int appId, String userId, String servrerId, String roleId, int vip, long actTime,
             int coinNum) {
        super(appId, userId, servrerId, roleId, vip, actTime);
        this.coinNum = coinNum;
    }

    public TreeMap<String, Object> makeSign(String sign) {
        TreeMap<String, Object> treeMap = super.makeMap();
        treeMap.put("id", System.currentTimeMillis());
        treeMap.put("coinNum", this.coinNum);
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
