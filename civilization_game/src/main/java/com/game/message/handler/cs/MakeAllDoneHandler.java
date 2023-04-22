package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorkShopPb;
import com.game.service.WorkShopService;

/**
 * @author jyb
 * @date 2020/6/9 15:39
 * @description
 */
public class MakeAllDoneHandler  extends ClientHandler {
    @Override
    public void action() {
        WorkShopService service = getService(WorkShopService.class);
        WorkShopPb.MakeAllDoneRq req = msg.getExtension(WorkShopPb.MakeAllDoneRq.ext);
        service.makePropAllDone(req, this);
    }
}
