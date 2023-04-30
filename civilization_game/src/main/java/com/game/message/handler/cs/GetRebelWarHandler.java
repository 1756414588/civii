package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldService;

/**
 *
 * @date 2020/5/13 9:30
 * @description
 */
public class GetRebelWarHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldService.class).getRebelWar(this);
    }
}
