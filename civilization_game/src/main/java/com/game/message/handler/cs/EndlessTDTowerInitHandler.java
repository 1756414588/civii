package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/10 9:52
 **/
public class EndlessTDTowerInitHandler extends ClientHandler {

    @Override
    public void action() {
        getService(TDService.class).endlessTDTowerInitRq(this);
    }
}
