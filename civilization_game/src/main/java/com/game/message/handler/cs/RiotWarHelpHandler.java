package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.RiotService;

public class RiotWarHelpHandler extends ClientHandler {
    @Override
    public void action () {
        RiotService service = getService(RiotService.class);
        service.riotWarHelpRq(this);
    }
}
