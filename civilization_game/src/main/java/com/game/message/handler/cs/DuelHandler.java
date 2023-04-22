package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb.DuelRq;
import com.game.service.ChatService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/31 16:59
 **/
public class DuelHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ChatService.class).duelRq(this, msg.getExtension(DuelRq.ext));
	}
}
