package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FirstBloodPb.CityFirstBloodRq;
import com.game.service.FirstBloodService;

/**
 * @author liyue
 */
public class GetFirstBloodInfoHandler extends ClientHandler {
    @Override
    public void action () {
        getService(FirstBloodService.class).getFirstBloodInfo(msg.getExtension(CityFirstBloodRq.ext), this);
    }
}

