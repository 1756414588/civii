package com.game.uc.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.TypeIndef;
import com.game.pay.domain.KuaiYouConfig;
import com.game.uc.Ihandler.BaseLoginHandler;
import com.game.util.HttpUtil;
import com.game.util.Md5Util;
import com.game.util.SortUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KwLoginHandler extends BaseLoginHandler {

	@Override
	public boolean login(BaseChanelConfig channelConfig, String account, String token) {

		KuaiYouConfig config = (KuaiYouConfig) channelConfig;
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", account);
		map.put("session_id", token);
		map.put("game_id", config.getGameChannelId());
		log.error("user_id ={},session_id={},game_id={}", account, token, config.getGameChannelId());
		String formatUrlParam = SortUtils.formatUrlParam(map, "utf-8", false);
		log.error("formatUrlParam={}", formatUrlParam);
		String sign = Md5Util.string2MD5(Md5Util.string2MD5(formatUrlParam) + "|" + config.getAppKey());
		log.error("sign={}", sign);
		map.put("sign", sign);
		String msg = HttpUtil.sendPost(config.getLoginUrl(), map);
		JSONObject parseObject = JSON.parseObject(msg);
		int code = (int) parseObject.get("code");
		if (code != 1) {
			log.error("VerifyLoginService verifyChannelLogin : channelId{},LoginUrl {},parmas {},msg {}", channelConfig.getPlatType(), config.getLoginUrl(), map, msg);
			return false;
		}
		return true;
	}

	@Override
	public void register() {
		addHandler(TypeIndef.KW.getPlat(), this);
		addHandler(TypeIndef.KW_IOS.getPlat(), this);
		addHandler(TypeIndef.KW_IOS_RELEASE.getPlat(), this);
	}
}
