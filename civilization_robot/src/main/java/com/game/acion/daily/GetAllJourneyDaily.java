package com.game.acion.daily;

import com.game.acion.MessageEvent;
import com.game.cache.UserJourneyCache;
import com.game.domain.Robot;
import com.game.domain.p.DailyMessage;
import com.game.domain.p.RobotData;
import com.game.pb.BasePb.Base;
import com.game.pb.JourneyPb.GetAllJourneyRs;

/**
 *
 * @Description远征关卡
 * @Date 2022/10/21 15:48
 **/

public class GetAllJourneyDaily extends AutoDaily {

	public GetAllJourneyDaily(DailyMessage dailyMessage) {
		super(dailyMessage);
	}

	@Override
	public void onResult(MessageEvent event, Robot robot, Base base) {
		UserJourneyCache userJourneyCache = robot.getCache().getJourneyCache();

		GetAllJourneyRs getAllJourneyRs = base.getExtension(GetAllJourneyRs.ext);
		if (getAllJourneyRs.hasJourney()) {
			userJourneyCache.setJourneyId(getAllJourneyRs.getJourney().getJourneyId());
		}

		RobotData robotDaily = robot.getData();
		robotDaily.setStatus(2);// 设置该操作完成
	}

}
