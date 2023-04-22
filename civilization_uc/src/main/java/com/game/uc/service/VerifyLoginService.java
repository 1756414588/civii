package com.game.uc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.ChannelConsts;
import com.game.uc.Ihandler.BaseLoginHandler;
import com.game.uc.Ihandler.LoginHandler;
import com.game.uc.manager.ChannelConfigManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 2020年5月19日
 *
 * @CaoBing halo_uc ValidateLoginService.java
 **/
@Service
@Slf4j
public class VerifyLoginService {

	@Autowired
	private ChannelConfigManager channelConfigManager;

	public BaseChanelConfig verifyChannelLogin(String packageName, int channelId, String account, String token) {
		BaseChanelConfig channelConfig;
		try {
			LoginHandler action = BaseLoginHandler.map.get(channelId);
			if (action != null) {
				channelConfig = channelConfigManager.getChannelConfig(channelId);
				if (channelConfig == null) {
					log.error("渠道参数未配置 channel->[{}] package->[{}] map->[{}]", channelId, packageName, action);
					return channelConfig;
				}
				// 根据包名获取下有没有子包L
				if (channelId == ChannelConsts.KUAI_YOU_ID || channelId == ChannelConsts.NEW_KUAIYOU_ID) {
					BaseChanelConfig childConfig = channelConfigManager.getChannelConfigByPackageName(channelId, packageName);
					if (childConfig != null) {
						channelConfig = childConfig;
					}
				}
				Boolean login = action.login(channelConfig, account, token);
				if (login) {
					return channelConfig;
				}
			} else {
				log.error("渠道参数未配置 channel->[{}] package->[{}] map->[{}]", channelId, packageName, action);
			}
			return null;
		} catch (Exception e) {
			log.error("VerifyLoginService verifyChannelLogin : error{}", e);
		}
		return null;
	}

	public BaseChanelConfig verifyChannelLogin(String packageName, int channelId) {
		BaseChanelConfig channelConfig =null;
		try {
			LoginHandler action = BaseLoginHandler.map.get(channelId);
			if (action != null) {
				channelConfig = channelConfigManager.getChannelConfig(channelId);
				if (channelConfig == null) {
					log.error("渠道参数未配置 channel->[{}] package->[{}] map->[{}]", channelId, packageName, action);
					return channelConfig;
				}
				// 根据包名获取下有没有子包L
				if (channelId == ChannelConsts.KUAI_YOU_ID || channelId == ChannelConsts.NEW_KUAIYOU_ID) {
					BaseChanelConfig childConfig = channelConfigManager.getChannelConfigByPackageName(channelId, packageName);
					if (childConfig != null) {
						channelConfig = childConfig;
					}
				}
			} else {
				log.error("渠道参数未配置 channel->[{}] package->[{}] map->[{}]", channelId, packageName, action);
			}

		} catch (Exception e) {
			log.error("VerifyLoginService verifyChannelLogin : error{}", e);
		}
		return channelConfig;
	}
}
