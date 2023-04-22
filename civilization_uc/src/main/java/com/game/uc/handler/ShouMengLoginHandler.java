package com.game.uc.handler;

import java.util.HashMap;
import java.util.Map;

import com.game.pay.channel.TypeIndef;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.ChannelConsts;
import com.game.pay.domain.KuaiYouConfig;
import com.game.uc.Ihandler.BaseLoginHandler;
import com.game.util.HttpUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ShouMengLoginHandler extends BaseLoginHandler {

	@Override
	public boolean login(BaseChanelConfig channelConfig, String account, String token) {
		KuaiYouConfig config = (KuaiYouConfig) channelConfig;
		Map<String, String> map = new HashMap<String, String>();
		map.put("login_account", account);
		map.put("session_id", token);
		String msg = HttpUtil.sendPost(config.getLoginUrl(), map);
		JSONObject parseObject = JSON.parseObject(msg);
		int code = (int) parseObject.get("result");
		if (code != 1) {
			log.error("VerifyLoginService verifyChannelLogin : channelId{},LoginUrl {},parmas {},msg {}", channelConfig.getPlatType(), config.getLoginUrl(), map, msg);
			return false;
		}
		return true;
	}

	@Override
	public void register() {
		addHandler(TypeIndef.SM.getPlat(), this);
	}
}
