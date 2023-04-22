package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CountryService;

/**
 * @author jyb
 * @date 2020/4/21 10:16
 * @description
 */
public class GetCountryNameHandler extends ClientHandler {
    @Override
    public void action() {
        getService(CountryService.class).getCountryName(this);
    }
}
