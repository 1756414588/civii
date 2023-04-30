package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.service.BeautyService;

/**
 * 2020年5月29日
 * 
 *    halo_game GetBeautyListHanlder.java
 **/
public class GetBeautyListHanlder extends ClientHandler {

	@Override
	public void action() {
		BeautyService service = getService(BeautyService.class);
		BeautyPb.NewGetBeautyListRq req = msg.getExtension(BeautyPb.NewGetBeautyListRq.ext);
		service.getBeautyListRq(req, this);
	}

}
