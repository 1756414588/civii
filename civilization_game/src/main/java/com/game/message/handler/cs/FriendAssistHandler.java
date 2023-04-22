package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WallPb;
import com.game.service.WallService;

public class FriendAssistHandler extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        WallPb.FriendAssistRq req = msg.getExtension(WallPb.FriendAssistRq.ext);
        service.friendAssist(req, this);
    }
}



