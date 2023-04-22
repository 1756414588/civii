package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb.AddFriendRq;
import com.game.service.FriendService;

public class AddFriendHandler extends ClientHandler {
    @Override
    public void action() {
        getService(FriendService.class).
                addFriend(msg.getExtension(AddFriendRq.ext),this);
    }
}
