package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CastleService;

/**
 *
 * @date 2019/12/16 19:13
 * @description
 */
public class OpenNextStepHandler extends ClientHandler {
    @Override
    public void action() {
        getService(CastleService.class).OpenNextStep(this);
    }
}
