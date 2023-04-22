package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.JourneyPb;
import com.game.service.JourneyService;

/**
*2020年8月17日
*@CaoBing
*halo_game
*BuyJourneyTimesHandler.java
**/
public class BuyJourneyTimesHandler extends ClientHandler {
    @Override
    public void action () {
    	JourneyService service = getService(JourneyService.class);
    	JourneyPb.BuyJourneyTimesRq req = msg.getExtension(JourneyPb.BuyJourneyTimesRq.ext);
        service.buyJourneyTimesRq(req, this);
    }

}
