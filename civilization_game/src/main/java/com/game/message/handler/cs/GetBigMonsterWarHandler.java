package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.server.GameServer;
import com.game.service.BigMonsterService;

/**
 *
 * @date 2021/4/12 10:08
 *
 */
public class GetBigMonsterWarHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BigMonsterService.class).getBigMonsterWarRq(msg.getExtension(WorldPb.GetBigMonsterWarRq.ext), this);

    }
}
