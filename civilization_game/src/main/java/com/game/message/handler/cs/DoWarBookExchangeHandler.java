package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WarBookPb;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/28 14:42
 */
public class DoWarBookExchangeHandler  extends ClientHandler {
    @Override
    public void action () {
        WarBookService service = getService(WarBookService.class);
        WarBookPb.DoWarBookExchangeRq req = msg.getExtension(WarBookPb.DoWarBookExchangeRq.ext);
        service.doWarBookExchangeRq(req, this);
    }
}
