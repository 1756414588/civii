package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorkShopPb;
import com.game.service.WorkShopService;

public class MakePropDoneHandler extends ClientHandler {
    @Override
    public void action () {
        WorkShopService service = getService(WorkShopService.class);
        WorkShopPb.MakeDoneRq req = msg.getExtension(WorkShopPb.MakeDoneRq.ext);

        service.makePropDone(req, this);

    }
}
