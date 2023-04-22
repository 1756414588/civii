package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.BigMonsterService;

/**
 * @author zcp
 * @date 2021/3/23 17:23
 * 诵我真名者,永不见bug
 */
public class GetBigMonsterInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BigMonsterService.class).getBigMonsterInfoRq(msg.getExtension(WorldPb.GetBigMonsterInfoRq.ext), this);
    }
}
