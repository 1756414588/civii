package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb;
import com.game.service.FriendService;

public class SearchFriHandler extends ClientHandler {
    @Override
    public void action() {
        FriendPb.SearchRq extension = msg.getExtension(FriendPb.SearchRq.ext);
        getService(FriendService.class).searchTeacher(extension,this);
        
    }
}
