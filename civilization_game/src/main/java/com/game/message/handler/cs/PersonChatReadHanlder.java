package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.pb.ChatPb;
import com.game.service.BeautyService;
import com.game.service.ChatService;

/**
 * @author CaoBing
 * @date 2021/2/25 10:15
 */
public class PersonChatReadHanlder extends ClientHandler {
    @Override
    public void action() {
        ChatService service = getService(ChatService.class);
        ChatPb.PersonChatReadRq req = msg.getExtension(ChatPb.PersonChatReadRq.ext);
        service.personChatReadRq(req, this);
    }
}
