package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RankPb;
import com.game.pb.RankPb.GetAreaRankRq;
import com.game.service.RankService;

public class GetAchievementRankHandler extends ClientHandler {
	@Override
	public void action() {
		getService(RankService.class).getAchievementRank(msg.getExtension(RankPb.GetAchiRankRq.ext), this);
	}
}
