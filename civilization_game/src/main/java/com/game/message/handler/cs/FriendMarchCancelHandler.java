package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WallPb;
import com.game.service.WallService;

public class FriendMarchCancelHandler extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        WallPb.FriendMarchCancelRq req = msg.getExtension(WallPb.FriendMarchCancelRq.ext);
        service.friendMarchCancelRq(req, this);
    }
}