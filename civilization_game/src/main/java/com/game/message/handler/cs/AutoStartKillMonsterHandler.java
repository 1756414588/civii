package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.AutoService;

/**
 * @author zcp
 * @date 2021/4/6 9:28
 * 诵我真名者,永不见bug
 */
public class AutoStartKillMonsterHandler extends ClientHandler {
    @Override
    public void action() {
        getService(AutoService.class).autoKillMonsterStart(msg.getExtension(WorldPb.AutoStartKillMonsterRq.ext), this);
    }
}
