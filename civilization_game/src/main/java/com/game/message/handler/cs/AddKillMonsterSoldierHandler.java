package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.AutoService;

/**
 * @author zcp
 * @date 2021/4/6 10:17
 * 诵我真名者,永不见bug
 */
public class AddKillMonsterSoldierHandler extends ClientHandler {
    @Override
    public void action() {
        getService(AutoService.class).addKillMonsterSoldierRq(msg.getExtension(WorldPb.AddKillMonsterSoldierRq.ext), this);
    }
}
