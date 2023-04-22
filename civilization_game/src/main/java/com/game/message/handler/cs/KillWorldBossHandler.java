package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.server.exec.LoginExecutor;
import com.game.service.WorldService;
import com.game.spring.SpringUtil;

public class KillWorldBossHandler extends ClientHandler {
    @Override
    public void action() {
        SpringUtil.getBean(LoginExecutor.class).add(() -> {
            WorldService service = getService(WorldService.class);
            WorldPb.KillWorldBossRq req = msg.getExtension(WorldPb.KillWorldBossRq.ext);
            service.killWorldBoss(req, this);
        });
    }
}
