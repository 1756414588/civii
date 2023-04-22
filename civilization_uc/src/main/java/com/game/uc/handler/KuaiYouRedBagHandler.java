package com.game.uc.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.ChannelConsts;
import com.game.pay.domain.KuaiYouConfig;
import com.game.uc.Ihandler.BaseLoginHandler;
import com.game.util.HttpUtil;
import com.game.util.Md5Util;
import com.game.util.SortUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KuaiYouRedBagHandler extends BaseLoginHandler {


	@Override
	public boolean login(BaseChanelConfig channelConfig, String account, String token) {
		KuaiYouConfig kuaiYou2318Config = (KuaiYouConfig) channelConfig;
		Map<String, String> map = new HashMap<String, String>();
		map.put("userid", account);
		map.put("time", String.valueOf(System.currentTimeMillis() / 1000));
		map.put("appId", kuaiYou2318Config.getGameChannelId());

		String formatUrlParam = SortUtils.formatUrlParam(map, "utf-8", false) + kuaiYou2318Config.getAppKey();
		String sign = Md5Util.string2MD5(formatUrlParam);
		map.put("sign", sign);

		Map<String, String> headMap = new HashMap<String, String>();
		headMap.put("Content-Type", "application/x-www-form-urlencoded");
		headMap.put("Authorization", token);
		String msg = HttpUtil.sendHttpPost(kuaiYou2318Config.getLoginUrl(), map, headMap);
		JSONObject parseObject = JSON.parseObject(msg);
		int code = (int) parseObject.get("result");
		if (code != 0) {
			//log.error("VerifyLoginService verifyChannelLogin : channelId{},LoginUrl {},parmas {},headParmas,msg {}", ChannelConsts.KUAI_YOU__2318_RED_BAG_ID, kuaiYou2318Config.getLoginUrl(), map, headMap, msg);
			return false;
		}
		return true;
	}

	@Override
	public void register() {
		addHandler(ChannelConsts.KUAI_YOU__2318_RED_BAG_ID, this);
	}
}
