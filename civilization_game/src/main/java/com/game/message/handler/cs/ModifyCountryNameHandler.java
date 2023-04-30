package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

/**
 *
 * @date 2020/4/13 16:06
 * @description
 */
public class ModifyCountryNameHandler extends ClientHandler {

    @Override
    public void action() {
        CountryPb.ModifyCountryNameRq req = msg.getExtension(CountryPb.ModifyCountryNameRq.ext);
        getService(CountryService.class).modifyCountryName(req, this);
    }


}
