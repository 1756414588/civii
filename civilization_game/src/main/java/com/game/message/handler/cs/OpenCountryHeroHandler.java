package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class OpenCountryHeroHandler  extends ClientHandler {
    @Override
    public void action() {
        CountryPb.OpenCountryHeroRq req = msg.getExtension(CountryPb.OpenCountryHeroRq.ext);
        getService(CountryService.class).openCountryHeroRq(req, this);
    }
}