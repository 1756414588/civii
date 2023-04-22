package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreBuyShopRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreBuyShopHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreBuyShopRq req = msg.getExtension(ActManoeuvreBuyShopRq.ext);
		service.actManoeuvreBuyShopRq(req, this);
	}
}
