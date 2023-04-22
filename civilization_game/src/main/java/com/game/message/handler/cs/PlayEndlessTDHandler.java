package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb.PlayEndlessTDRq;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/10 9:34
 **/
public class PlayEndlessTDHandler extends ClientHandler {
	@Override
	public void action() {
		getService(TDService.class).playEndlessTDRq(this, msg.getExtension(PlayEndlessTDRq.ext));
	}
}
