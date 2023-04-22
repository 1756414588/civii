package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.ChatService;

public class ShareMailHandler extends ClientHandler {

	@Override
	public void action() {
		ChatPb.ShareMailRq req = msg.getExtension(ChatPb.ShareMailRq.ext);
		ChatService chatService = getService(ChatService.class);
		chatService.shareMailRq(req, this);
	}
}
