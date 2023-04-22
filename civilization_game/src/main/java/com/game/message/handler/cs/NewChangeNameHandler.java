package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class NewChangeNameHandler  extends ClientHandler {
    @Override
    public void action() {
        PlayerService service = getService(PlayerService.class);
        RolePb.NewChangeNameRq req = msg.getExtension(RolePb.NewChangeNameRq.ext);
        service.newChangeName(req, this);
    }
}
