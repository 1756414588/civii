package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.pb.WorldPb;
import com.game.service.CityService;
import com.game.service.StaffService;

/**
 * @author CaoBing
 * @date 2020/11/15 0:04
 */
public class GetStealCityAwardHandler  extends ClientHandler {
    @Override
    public void action() {
        // TODO Auto-generated method stub
        CityService service = getService(CityService.class);
        WorldPb.GetStealCityAwardRq req = msg.getExtension(WorldPb.GetStealCityAwardRq.ext);
        service.getStealCityAward(req,this);
    }
}
