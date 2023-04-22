package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.BigMonsterService;

/**
 * @author zcp
 * @date 2021/3/23 17:22
 * 诵我真名者,永不见bug
 */
public class GetBigMonsterActivityHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BigMonsterService.class).getBigMonsterActivityRq(msg.getExtension(WorldPb.GetBigMonsterActivityRq.ext), this);
    }
}
