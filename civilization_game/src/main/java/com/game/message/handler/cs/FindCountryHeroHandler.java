package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class FindCountryHeroHandler extends ClientHandler {
    @Override
    public void action() {
        CountryPb.FindCountryHeroRq req = msg.getExtension(CountryPb.FindCountryHeroRq.ext);
        getService(CountryService.class).findCountryHeroRq(req, this);
    }
}