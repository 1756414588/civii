package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.BigMonsterService;

/**
 *
 * @date 2021/3/23 17:22
 *
 */
public class GetBigMonsterActivityHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BigMonsterService.class).getBigMonsterActivityRq(msg.getExtension(WorldPb.GetBigMonsterActivityRq.ext), this);
    }
}
