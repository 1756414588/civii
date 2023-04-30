package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.JourneyPb;
import com.game.service.JourneyService;

/**
*2020年8月17日
*
*halo_game
*GetAllJourneyHandler.java
**/
public class GetAllJourneyHandler extends ClientHandler {
    @Override
    public void action () {
    	JourneyService service = getService(JourneyService.class);
    	JourneyPb.GetAllJourneyRq req = msg.getExtension(JourneyPb.GetAllJourneyRq.ext);
        service.getAllJourneyRq(req, this);
    }
}
