package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/29 14:18
 **/
public class QuartermasterWarehouseHandler extends ClientHandler {

    @Override
    public void action() {
        getService(TDService.class ).quartermasterWarehouseRq(this);
    }
}
