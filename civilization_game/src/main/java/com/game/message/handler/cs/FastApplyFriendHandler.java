package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb.FastApplyFriendRq;
import com.game.service.FriendService;

public class FastApplyFriendHandler extends ClientHandler {
    @Override
    public void action() {
        getService(FriendService.class).
                fastApplyFriend(msg.getExtension(FastApplyFriendRq.ext),this);
    }
}
