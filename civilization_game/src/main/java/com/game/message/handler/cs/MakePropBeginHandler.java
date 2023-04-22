package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorkShopPb;
import com.game.service.WorkShopService;

public class MakePropBeginHandler extends ClientHandler {
    @Override
    public void action () {
        WorkShopService service = getService(WorkShopService.class);
        WorkShopPb.MakePropRq req = msg.getExtension(WorkShopPb.MakePropRq.ext);
        service.makePropBegin(req, this);
    }
}



