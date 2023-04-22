package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.ChatService;

public class SeeManHandler extends ClientHandler {

	@Override
	public void action() {
		ChatPb.SeeManRq req = msg.getExtension(ChatPb.SeeManRq.ext);
		ChatService chatService = getService(ChatService.class);
		chatService.seeManRq(req, this);
	}
}
