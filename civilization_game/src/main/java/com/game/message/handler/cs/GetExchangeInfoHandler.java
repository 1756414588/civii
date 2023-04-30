package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.RebelService;

/**
 *
 * @date 2020/5/23 15:24
 * @description
 */
public class GetExchangeInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(RebelService.class).getExchangeInfo(this);
    }
}
