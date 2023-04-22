package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class MakeEquipHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.MakeEquipRq req = msg.getExtension(ActivityPb.MakeEquipRq.ext);
        service.makeEquip(req, this);
    }
}
