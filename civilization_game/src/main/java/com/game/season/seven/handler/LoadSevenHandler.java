package com.game.season.seven.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class LoadSevenHandler extends ClientHandler {
    @Override
    public void action() {
        getService(SeasonService.class).loadSevenInfo(msg.getExtension(SeasonActivityPb.LoadSevenRq.ext),this);
    }
}
