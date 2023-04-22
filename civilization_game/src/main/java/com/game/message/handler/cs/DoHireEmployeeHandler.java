package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;


public class DoHireEmployeeHandler extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.HireOfficerRq req = msg.getExtension(BuildingPb.HireOfficerRq.ext);
        service.doHireEmployee(req, this);
    }
}