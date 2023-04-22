package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TechPb;
import com.game.service.TechService;

public class HireResearcherHandler  extends ClientHandler {
    @Override
    public void action () {
        TechService service = getService(TechService.class);
        TechPb.HireResearcherRq req = msg.getExtension(TechPb.HireResearcherRq.ext);
        service.hireResearcherRq(req, this);
    }
}