package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class LoadTitleAwardHandler extends ClientHandler {
	@Override
	public void action() {
		getService(CountryService.class).loadTitleAwardInfo(msg.getExtension(CountryPb.TitleAwardRq.ext), this);
	}
}
