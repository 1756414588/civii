package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb.GetMasterAwardRq;
import com.game.service.FriendService;

public class GetMasterAwardHandler extends ClientHandler {
    @Override
    public void action() {
        FriendService service = getService(FriendService.class);
        GetMasterAwardRq req = msg.getExtension(GetMasterAwardRq.ext);
        service.getMasterAward(req, this);
    }
}
