package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.ChatService;

public class ShareChatHandler extends ClientHandler {

    @Override
    public void action() {
        ChatPb.ShareChatRq req = msg.getExtension(ChatPb.ShareChatRq.ext);
        ChatService chatService = getService(ChatService.class);
        chatService.shareChatRq(req, this);
    }
}