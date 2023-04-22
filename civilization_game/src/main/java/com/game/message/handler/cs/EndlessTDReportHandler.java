package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb.EndlessTDReportRq;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/10 9:43
 **/
public class EndlessTDReportHandler extends ClientHandler {

	@Override
	public void action() {
		getService(TDService.class).endlessTDReportRq(this, msg.getExtension(EndlessTDReportRq.ext));
	}
}
