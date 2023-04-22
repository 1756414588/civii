package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ChatService;

public class GetChatHandler extends ClientHandler {

	@Override
	public void action() {
		ChatService chatService = getService(ChatService.class);
		chatService.getChat(this);
	}
}
