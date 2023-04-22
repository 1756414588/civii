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
import com.game.util.Md5Util;
import com.game.util.SortUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class kuaiYHandler extends BaseLoginHandler {
	@Override
	public boolean login(BaseChanelConfig channelConfig, String account, String token) {
		KuaiYouConfig config = (KuaiYouConfig) channelConfig;
		Map<String, String> map = new HashMap<String, String>();
		map.put("userid", account);
		map.put("sessionid", token);
		map.put("appId", config.getGameChannelId());

		String formatUrlParam = SortUtils.formatUrlParam(map, "utf-8", false) + config.getAppKey();
		String sign = Md5Util.string2MD5(formatUrlParam);

		map.put("sign", sign);
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
		addHandler(ChannelConsts.KUAI_YOU_ID, this);

		addHandler(TypeIndef.KY_IOS_TEST.getPlat(), this);
		addHandler(TypeIndef.KY_IOS.getPlat(), this);
		addHandler(TypeIndef.KY_IOS_NEW.getPlat(), this);
		addHandler(TypeIndef.KY.getPlat(), this);
		addHandler(TypeIndef.KY_304.getPlat(), this);
		addHandler(TypeIndef.KY_305.getPlat(), this);
	}
}
