package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ShopPb;
import com.game.pb.WarBookPb;
import com.game.service.ShopService;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/11 16:10
 */
public class LockWarBookHandler extends ClientHandler {
    @Override
    public void action() {
        WarBookService service = getService(WarBookService.class);
        WarBookPb.LockWarBookRq req = msg.getExtension(WarBookPb.LockWarBookRq.ext);
        service.lockWarBookRq(req, this);
    }
}
