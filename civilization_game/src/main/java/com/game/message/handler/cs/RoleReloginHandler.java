package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class RoleReloginHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService playerService = getService(PlayerService.class);
        RolePb.RoleReloginRq req = msg.getExtension(RolePb.RoleReloginRq.ext);
        playerService.roleReloginRq(req, this);
    }
}
