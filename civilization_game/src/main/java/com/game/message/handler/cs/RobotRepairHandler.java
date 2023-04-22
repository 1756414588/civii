package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MapInfoPb.GetMapNpcRq;
import com.game.pb.MapInfoPb.RobotRepairRq;
import com.game.service.MapInfoService;

public class RobotRepairHandler extends ClientHandler {

	@Override
	public void action() {
		MapInfoService service = getService(MapInfoService.class);
		RobotRepairRq req = msg.getExtension(RobotRepairRq.ext);
		service.robotRepairRq(req, this);
	}
}