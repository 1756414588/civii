package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.ChatService;

public class SuggestHandler extends ClientHandler {

	@Override
	public void action() {
		ChatPb.SuggestRq req = msg.getExtension(ChatPb.SuggestRq.ext);
		ChatService chatService = getService(ChatService.class);
		chatService.suggestRq(req, this);
	}
}
