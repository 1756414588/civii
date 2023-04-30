package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CastleService;

/**
 *
 * @date 2019/12/16 15:25
 * @description
 */
public class GetSoldierLineHandler extends ClientHandler {
    @Override
    public void action() {
        getService(CastleService.class).getSoldierLine(this);
    }
}
