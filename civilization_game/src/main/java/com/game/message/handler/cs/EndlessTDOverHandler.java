package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb.EndlessTDOverRq;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/6 15:03
 **/
public class EndlessTDOverHandler extends ClientHandler {

	@Override
	public void action() {
		getService(TDService.class).endlessTDOverRq(this, msg.getExtension(EndlessTDOverRq.ext));
	}
}
