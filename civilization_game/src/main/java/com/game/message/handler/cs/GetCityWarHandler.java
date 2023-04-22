package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.service.CountryService;

public class GetCityWarHandler  extends ClientHandler {
    @Override
    public void action() {
        getService(CountryService.class).getCityWarRq(this);
    }
}