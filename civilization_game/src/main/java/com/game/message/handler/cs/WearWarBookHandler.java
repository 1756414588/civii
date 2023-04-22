package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WarBookPb;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/14 9:36
 */
public class WearWarBookHandler extends ClientHandler {
    @Override
    public void action() {
        WarBookService service = getService(WarBookService.class);
        WarBookPb.WearWarBookRq req = msg.getExtension(WarBookPb.WearWarBookRq.ext);
        service.wearWarBookRq(req, this);
    }
}
