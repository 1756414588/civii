package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class VoteGovernHandler extends ClientHandler {
	@Override
	public void action() {
		CountryPb.VoteGovernRq req = msg.getExtension(CountryPb.VoteGovernRq.ext);
		getService(CountryService.class).voteGovernRq(req, this);
	}
}