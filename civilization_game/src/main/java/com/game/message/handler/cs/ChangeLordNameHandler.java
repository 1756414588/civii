package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.PropPb;
import com.game.service.ItemService;

public class ChangeLordNameHandler extends ClientHandler {
    @Override
    public void action () {
        ItemService service = getService(ItemService.class);
        PropPb.ChangeLordNameRq req = msg.getExtension(PropPb.ChangeLordNameRq.ext);
        service.changeLordName(req, this);
    }
}
