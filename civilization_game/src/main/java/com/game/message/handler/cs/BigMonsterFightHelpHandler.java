package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.BigMonsterService;

/**
 * @author zcp
 * @date 2021/4/12 10:29
 * 诵我真名者,永不见bug
 */
public class BigMonsterFightHelpHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BigMonsterService.class).fightHelpRq(msg.getExtension(WorldPb.BigMonsterFightHelpRq.ext), this);
    }
}
