package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class TrainCountryHeroHandler  extends ClientHandler {
    @Override
    public void action() {
        CountryPb.TrainCountryHeroRq req = msg.getExtension(CountryPb.TrainCountryHeroRq.ext);
        getService(CountryService.class).trainCountryHeroRq(req, this);
    }
}