package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.JourneyPb;
import com.game.service.JourneyService;

/**
*2020年8月17日
*
*halo_game
*SweepJourneyHandler.java
**/
public class SweepJourneyHandler extends ClientHandler {
    @Override
    public void action () {
    	JourneyService service = getService(JourneyService.class);
    	JourneyPb.SweepJourneyRq req = msg.getExtension(JourneyPb.SweepJourneyRq.ext);
        service.SweepJourneyRq(req, this);
    }
}
