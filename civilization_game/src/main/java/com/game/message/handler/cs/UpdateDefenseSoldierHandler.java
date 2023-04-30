package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CastleService;

/**
 *
 * @date 2019/12/19 15:17
 * @description
 */
public class UpdateDefenseSoldierHandler extends ClientHandler {
    @Override
    public void action() {
        getService(CastleService.class).updateDefenseSoldier(this);
    }
}
