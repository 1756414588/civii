package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/24 17:16
 */
public class RefreshWarBookShopHandler extends ClientHandler {
    @Override
    public void action () {
        WarBookService service = getService(WarBookService.class);
        service.refreshWarBookShopRq(this);
    }
}
