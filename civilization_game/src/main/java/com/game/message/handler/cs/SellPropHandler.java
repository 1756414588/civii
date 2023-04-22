package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PropPb;
import com.game.service.ItemService;

public class SellPropHandler extends ClientHandler {
    @Override
    public void action () {
        ItemService service = getService(ItemService.class);
        PropPb.SellPropRq req = msg.getExtension(PropPb.SellPropRq.ext);
        service.selItem(req, this);
    }
}
