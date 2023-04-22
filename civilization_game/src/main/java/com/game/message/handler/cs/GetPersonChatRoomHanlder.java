package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.ChatService;

/**
 * @author CaoBing
 * @date 2021/2/23 9:42
 */
public class GetPersonChatRoomHanlder extends ClientHandler {
    @Override
    public void action() {
        ChatService service = getService(ChatService.class);
        ChatPb.GetPersonChatRoomRq req = msg.getExtension(ChatPb.GetPersonChatRoomRq.ext);
        service.getPersonChatRoomRq(this);
    }
}
