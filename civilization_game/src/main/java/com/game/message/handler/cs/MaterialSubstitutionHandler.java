package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.MaterialSubstitutionRq;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/27 17:22
 **/
public class MaterialSubstitutionHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).materialSubstitution(this, msg.getExtension(MaterialSubstitutionRq.ext));
	}
}
