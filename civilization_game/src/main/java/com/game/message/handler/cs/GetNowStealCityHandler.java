package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.pb.WorldPb;
import com.game.service.CityService;
import com.game.service.OmamentService;

/**
 * @author CaoBing
 * @date 2020/11/21 18:29
 */
public class GetNowStealCityHandler extends ClientHandler {

    @Override
    public void action() {
        CityService service = getService(CityService.class);
        WorldPb.GetNowStealCityRq req = msg.getExtension(WorldPb.GetNowStealCityRq.ext);
        service.getNowStealCityRq(req, this);
    }
}
