package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class GetLevelAwardHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService service = getService(PlayerService.class);
        RolePb.GetLevelAwardRq req = msg.getExtension(RolePb.GetLevelAwardRq.ext);
        service.getLevelAward(req, this);
    }
}
