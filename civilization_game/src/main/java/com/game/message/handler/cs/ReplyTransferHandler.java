package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class ReplyTransferHandler extends ClientHandler {
	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		WorldPb.ReplyTransferRq req = msg.getExtension(WorldPb.ReplyTransferRq.ext);
		service.replyTransfer(req, this);
	}
}
