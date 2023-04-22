package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb.UserLoginRq;
import com.game.service.AccountService;

public class BeginGameHandler extends ClientHandler {

	@Override
	public void action() {
		AccountService accountService = getService(AccountService.class);
		UserLoginRq req = msg.getExtension(UserLoginRq.ext);

		accountService.beginGame(req, this);
	}
}