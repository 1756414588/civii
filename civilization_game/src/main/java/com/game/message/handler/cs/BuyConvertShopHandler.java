package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb.BuyConvertShopRq;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/29 14:11
 **/
public class BuyConvertShopHandler extends ClientHandler {

	@Override
	public void action() {
		getService(TDService.class).buyConvertShopRq(this, msg.getExtension(BuyConvertShopRq.ext));
	}
}
