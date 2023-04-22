package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.FriendService;

public class SearchFriendHandler extends ClientHandler {
    @Override
    public void action() {
        FriendService service = getService(FriendService.class);
        service.searchFriend(this);
    }
}
