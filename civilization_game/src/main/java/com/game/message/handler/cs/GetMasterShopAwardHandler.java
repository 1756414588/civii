package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb.GetMasterShopAwardRq;
import com.game.service.FriendService;

public class GetMasterShopAwardHandler extends ClientHandler {
    @Override
    public void action() {
        FriendService service = getService(FriendService.class);
        GetMasterShopAwardRq req = msg.getExtension(GetMasterShopAwardRq.ext);
        service.getMasterShopAward(req, this);
    }
}
