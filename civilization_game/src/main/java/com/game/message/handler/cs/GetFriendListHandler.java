package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.FriendService;

public class GetFriendListHandler extends ClientHandler {
    @Override
    public void action() {
        FriendService service = getService(FriendService.class);
        service.friendList(this);
    }
}
