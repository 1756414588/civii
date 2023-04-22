package com.game.uc.handler;

import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.TypeIndef;
import com.game.uc.Ihandler.BaseLoginHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class A1567LoginHandler extends BaseLoginHandler {
	@Override
	public void register() {
		addHandler(TypeIndef.A1576_ANDROID.getPlat(), this);
        addHandler(TypeIndef.A1576_IOS.getPlat(), this);
	}

	@Override
	public boolean login(BaseChanelConfig channelConfig, String account, String token) {
		return true;
	}
}
