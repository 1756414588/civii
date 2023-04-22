package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.AccountService;

/**
*2020年6月20日
*@CaoBing
*halo_game
*GetPepoleHandler.java
**/
public class GetPepoleHandler extends ClientHandler{
	@Override
    public void action() {
		AccountService service = getService(AccountService.class);
		RolePb.GetPeopleRq req = msg.getExtension(RolePb.GetPeopleRq.ext);
        service.getPeopleRq(req,this);
    }
}
