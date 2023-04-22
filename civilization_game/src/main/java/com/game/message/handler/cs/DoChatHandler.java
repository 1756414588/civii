package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.ChatService;

public class DoChatHandler extends ClientHandler {

    @Override
    public void action() {
        ChatPb.DoChatRq req = msg.getExtension(ChatPb.DoChatRq.ext);
        ChatService chatService = getService(ChatService.class);
        chatService.doChat(req, this);

    }
}
