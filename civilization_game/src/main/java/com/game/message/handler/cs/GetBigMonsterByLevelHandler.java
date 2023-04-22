package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.BigMonsterService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/5/31 16:21
 **/
public class GetBigMonsterByLevelHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BigMonsterService.class).getBigMonsterByLevel(msg.getExtension(WorldPb.GetBigMonsterByLevelRq.ext), this);
    }
}
