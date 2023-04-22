package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TaskService;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/22 15:33
 * 获取兵书商城物品
 */
public class GetWarBookShopHandler extends ClientHandler {
    @Override
    public void action () {
        WarBookService service = getService(WarBookService.class);
        service.getWarBookShopRq(this);
    }
}
