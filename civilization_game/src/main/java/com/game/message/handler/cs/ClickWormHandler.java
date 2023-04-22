package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.CityGameService;

/**
 * @author cpz
 * @date 2020/10/28 5:22
 * @description
 */
public class ClickWormHandler extends ClientHandler {
    @Override
    public void action() {

        CityGameService service = getService(CityGameService.class);
        BuildingPb.ClickWormRq req = msg.getExtension(BuildingPb.ClickWormRq.ext);
        service.clickWorms(this,req);
    }
}
