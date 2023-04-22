package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WarBookPb;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/24 17:10
 */
public class DoWarBookShopHandler extends ClientHandler {
    @Override
    public void action () {
        WarBookService service = getService(WarBookService.class);
        WarBookPb.DoWarBookShopRq req = msg.getExtension(WarBookPb.DoWarBookShopRq.ext);
        service.doWarBookShopRq(req, this);
    }
}
