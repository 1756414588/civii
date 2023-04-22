package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.PropPb;
import com.game.service.ItemService;

public class UseItemHandler extends ClientHandler {
    @Override
    public void action () {
        ItemService service = getService(ItemService.class);
        PropPb.UsePropRq req = msg.getExtension(PropPb.UsePropRq.ext);
        service.useItemRq(req, this);
    }
}
