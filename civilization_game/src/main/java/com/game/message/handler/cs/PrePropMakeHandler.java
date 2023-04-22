package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorkShopPb;
import com.game.service.WorkShopService;


public class PrePropMakeHandler extends ClientHandler {
    @Override
    public void action () {
        WorkShopService service = getService(WorkShopService.class);
        WorkShopPb.PreMakeRq req = msg.getExtension(WorkShopPb.PreMakeRq.ext);
        service.prePropMake(req, this);

    }
}
