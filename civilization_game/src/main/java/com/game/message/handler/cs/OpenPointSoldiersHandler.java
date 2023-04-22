package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CastleService;

/**
 * @author jyb
 * @date 2019/12/16 9:47
 * @description
 */
public class OpenPointSoldiersHandler extends ClientHandler {
    @Override
    public void action() {
        getService(CastleService.class).openPointSoldiers(this);
    }
}
