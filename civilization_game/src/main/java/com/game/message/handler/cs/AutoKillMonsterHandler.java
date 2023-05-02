package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.AutoService;

/**
 *
 * @date 2021/4/6 9:27
 *
 */
public class AutoKillMonsterHandler extends ClientHandler {
    @Override
    public void action() {
        getService(AutoService.class).autoKillMonster(msg.getExtension(WorldPb.AutoKillMonsterRq.ext), this);
    }
}
