//package com.game.pay.domain;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.game.pay.channel.BaseChanelConfig;
//import com.game.pay.channel.SChannelConfig;
//import com.game.pay.domain.KuaiYouConfig;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class KuaiYou2318Config extends KuaiYouConfig {
//
//    private String secret_key;
//
//    public KuaiYou2318Config(SChannelConfig sChannelConfig) {
//        super(sChannelConfig);
//        JSONObject login = JSON.parseObject(sChannelConfig.getLoginConfig());
//        this.secret_key = login.getString("secret_key");
//    }
//}
