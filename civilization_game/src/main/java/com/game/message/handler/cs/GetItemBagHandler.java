package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.PropPb;
import com.game.pb.PropPb.GetPropBagRq;
import com.game.service.ItemService;

public class GetItemBagHandler extends ClientHandler {
    @Override
    public void action () {
        ItemService service = getService(ItemService.class);
        GetPropBagRq req = msg.getExtension(PropPb.GetPropBagRq.ext);
        service.getItemBagRq(req, this);
    }
}
