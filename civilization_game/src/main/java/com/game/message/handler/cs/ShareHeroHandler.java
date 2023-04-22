package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.ChatService;

public class ShareHeroHandler extends ClientHandler {
    @Override
    public void action() {
        ChatService service = getService(ChatService.class);
        ChatPb.ShareHeroRq req = msg.getExtension(ChatPb.ShareHeroRq.ext);
        service.shareHero(req, this);
    }
}
