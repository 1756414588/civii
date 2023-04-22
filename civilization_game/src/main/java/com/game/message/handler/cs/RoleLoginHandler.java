package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.pb.RolePb;
import com.game.service.AccountService;

public class RoleLoginHandler extends ClientHandler {

	@Override
	public void action() {
		// TODO Auto-generated method stub
		AccountService accountService = getService(AccountService.class);
		RolePb.RoleLoginRq req =  msg.getExtension(RolePb.RoleLoginRq.ext);
		accountService.roleLogin(this,req);
	}
}
