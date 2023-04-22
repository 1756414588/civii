package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class GiftCodeHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService service = getService(PlayerService.class);
        RolePb.GiftCodeRq req = msg.getExtension(RolePb.GiftCodeRq.ext);
        service.giftCode(req.getCode(), this);
    }
}
