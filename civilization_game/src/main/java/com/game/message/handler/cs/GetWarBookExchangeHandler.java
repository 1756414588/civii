package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/24 16:35
 */
public class GetWarBookExchangeHandler extends ClientHandler {
    @Override
    public void action () {
        WarBookService service = getService(WarBookService.class);
        service.getWarBookExchangeRq(this);
    }
}
