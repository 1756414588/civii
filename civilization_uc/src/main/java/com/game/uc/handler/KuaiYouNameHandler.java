package com.game.uc.handler;

import org.springframework.stereotype.Component;

import com.game.pay.channel.BaseChanelConfig;
import com.game.uc.Ihandler.BaseLoginHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KuaiYouNameHandler extends BaseLoginHandler {

	@Override
	public boolean login(BaseChanelConfig channelConfig, String account, String token) {
		return false;
	}

	@Override
	public void register() {

	}
}
