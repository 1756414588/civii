package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.pb.BeautyPb.PlayBeautyGiftRq;
import com.game.service.BeautyService;

/**
 *2020年6月6日
 *
 *halo_game
 *PlayBeautyGiftHandler.java
 **/
public class PlayBeautyGiftHandler extends ClientHandler {
	@Override
	public void action() {
		BeautyService service = getService(BeautyService.class);
		BeautyPb.NewPlayBeautyGiftRq req = msg.getExtension(BeautyPb.NewPlayBeautyGiftRq.ext);
		service.playBeautyGiftRq(req, this);
	}
}
