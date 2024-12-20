package com.game.pay.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.SChannelConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * 2020年5月19日
 *
 *    halo_common
 * KaiYouConfig.java
 * <p>
 * 新快SDK的配置信息
 **/

public class KuaiYouConfig extends BaseChanelConfig {
    private String appKey;
    //登录url
    private String loginUrl;

    private String pay_config;

    public KuaiYouConfig() {
        super();
    }

    public KuaiYouConfig(SChannelConfig sChannelConfig) {
        super();
        JSONObject login = JSON.parseObject(sChannelConfig.getLoginConfig());
        this.appKey = login.getString("appkey");
        this.loginUrl = login.getString("loginUrl");
        this.pay_config = sChannelConfig.getPayConfig();
        this.keyId = sChannelConfig.getKeyId();
        this.platType = sChannelConfig.getPlatType();
        this.gameChannelId = String.valueOf(sChannelConfig.getGameChannelId());
        this.name = sChannelConfig.getName();
        this.packageName = sChannelConfig.getPackageName();
        this.is_review = sChannelConfig.getIs_review();
		this.parent_type = sChannelConfig.getParent_type();
		this.teamNum = sChannelConfig.getTeamNum();
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getPay_config() {
        return pay_config;
    }

    public void setPay_config(String pay_config) {
        this.pay_config = pay_config;
    }
}
