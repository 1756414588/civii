package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 * @author jyb
 * @date 2020/6/28 15:22
 * @description
 */
public class FindNearMonsterHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService worldService = getService(WorldService.class);
        WorldPb.FindNearMonsterRq req = msg.getExtension(WorldPb.FindNearMonsterRq.ext);
        worldService.findNearMonster(req,this);
    }
}
