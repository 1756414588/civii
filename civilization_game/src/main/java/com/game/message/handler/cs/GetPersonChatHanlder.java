package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.pb.MailPb;
import com.game.service.ChatService;
import com.game.service.MailService;

/**
 * @author CaoBing
 * @date 2021/2/23 9:43
 */
public class GetPersonChatHanlder extends ClientHandler {
    @Override
    public void action() {
        ChatService service = getService(ChatService.class);
        ChatPb.GetPersonChatRq req = msg.getExtension(ChatPb.GetPersonChatRq.ext);
        service.getPersonChatRq(req, this);
    }
}
