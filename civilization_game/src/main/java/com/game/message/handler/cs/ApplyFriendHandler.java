package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb;
import com.game.service.FriendService;

public class ApplyFriendHandler  extends ClientHandler {
    @Override
    public void action() {
        FriendService friendService = getService(FriendService.class);
        FriendPb.ApplyFriendRq req = msg.getExtension(FriendPb.ApplyFriendRq.ext);
        friendService.applyFriend(req,this);
    }
}
