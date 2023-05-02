package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.AutoService;

/**
 *
 * @date 2021/4/6 10:17
 *
 */
public class AddKillMonsterSoldierHandler extends ClientHandler {
    @Override
    public void action() {
        getService(AutoService.class).addKillMonsterSoldierRq(msg.getExtension(WorldPb.AddKillMonsterSoldierRq.ext), this);
    }
}
