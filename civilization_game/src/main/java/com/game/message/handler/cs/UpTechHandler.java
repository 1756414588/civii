package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.TechPb;
import com.game.service.TechService;

public class UpTechHandler extends ClientHandler {
    @Override
    public void action () {
        TechService service = getService(TechService.class);
        TechPb.UpTechRq req = msg.getExtension(TechPb.UpTechRq.ext);
        service.upTech(req, this);
    }
}