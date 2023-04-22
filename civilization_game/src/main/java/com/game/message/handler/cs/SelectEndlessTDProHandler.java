package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb.SelectEndlessTDProRq;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/10 9:47
 **/
public class SelectEndlessTDProHandler extends ClientHandler {

	@Override
	public void action() {
		getService(TDService.class).selectEndlessTDProRq(this,msg.getExtension(SelectEndlessTDProRq.ext));
	}
}
