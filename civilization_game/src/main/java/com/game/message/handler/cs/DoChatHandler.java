package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.server.exec.LoginExecutor;
import com.game.service.ChatService;
import com.game.spring.SpringUtil;

public class DoChatHandler extends ClientHandler {

    @Override
    public void action() {
        ChatPb.DoChatRq req = msg.getExtension(ChatPb.DoChatRq.ext);
        ChatService chatService = getService(ChatService.class);
        SpringUtil.getBean(LoginExecutor.class).add(() -> {
            chatService.doChat(req, this);
        });
    }
}
