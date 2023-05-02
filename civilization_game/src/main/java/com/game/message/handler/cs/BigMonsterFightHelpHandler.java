package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.BigMonsterService;

/**
 *
 * @date 2021/4/12 10:29
 *
 */
public class BigMonsterFightHelpHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BigMonsterService.class).fightHelpRq(msg.getExtension(WorldPb.BigMonsterFightHelpRq.ext), this);
    }
}
