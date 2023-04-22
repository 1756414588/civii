package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb.RemoveFriendRq;
import com.game.service.FriendService;

public class RemoveFriendHandler extends ClientHandler {
    @Override
    public void action() {
        FriendService service = getService(FriendService.class);
        RemoveFriendRq req = msg.getExtension(RemoveFriendRq.ext);
        service.removeFriend(req, this);
    }
}
