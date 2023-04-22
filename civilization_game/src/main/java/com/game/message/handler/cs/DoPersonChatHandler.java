package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.pb.ChatPb;
import com.game.service.ActivityService;
import com.game.service.ChatService;

/**
 * @author CaoBing
 * @date 2021/2/22 18:04
 */
public class DoPersonChatHandler extends ClientHandler {
    @Override
    public void action() {
        ChatService service = getService(ChatService.class);
        ChatPb.DoPersonChatRq req = msg.getExtension(ChatPb.DoPersonChatRq.ext);
        service.doPersonChatRq(req, this);
    }
}
