package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WallService;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/9 14:38
 */
public class GetWarBookBagHandler extends ClientHandler {
    @Override
    public void action () {
        WarBookService service = getService(WarBookService.class);
        service.getWarBookBagRq(this);
    }
}
