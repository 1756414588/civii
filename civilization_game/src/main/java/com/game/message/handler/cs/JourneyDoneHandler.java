package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.JourneyPb;
import com.game.service.JourneyService;

/**
*2020年8月17日
*
*halo_game
*JourneyDoneHandler.java
**/
public class JourneyDoneHandler extends ClientHandler {
    @Override
    public void action () {
    	JourneyService service = getService(JourneyService.class);
    	JourneyPb.JourneyDoneRq req = msg.getExtension(JourneyPb.JourneyDoneRq.ext);
        service.journeyDoneRq(req, this);
    }

}
