package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.pb.BeautyPb.PlaySeekingRq;
import com.game.service.BeautyService;

/**
 *2020年6月5日
 *@CaoBing
 *halo_game
 *PlaySeekingHandler.java
 **/
public class PlaySeekingHandler extends ClientHandler {
	@Override
	public void action() {
		BeautyService service = getService(BeautyService.class);
		BeautyPb.NewPlaySeekingRq req = msg.getExtension(BeautyPb.NewPlaySeekingRq.ext);
		service.playSeekingRq(req, this);
	}
}
