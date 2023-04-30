package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.pb.BeautyPb.PlaySGameRq;
import com.game.service.BeautyService;

/**
 * 2020年6月4日
 *
 *    halo_game PlaySGameHandler.java
 **/
public class PlaySGameHandler extends ClientHandler {
	@Override
	public void action() {
		BeautyService service = getService(BeautyService.class);
		BeautyPb.NewPlaySGameRq req = msg.getExtension(BeautyPb.NewPlaySGameRq.ext);
		service.playSGameRq(req, this);
	}
}
