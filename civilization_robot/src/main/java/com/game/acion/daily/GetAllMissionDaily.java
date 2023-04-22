package com.game.acion.daily;

import com.game.acion.MessageEvent;
import com.game.cache.UserMissionCache;
import com.game.domain.Robot;
import com.game.domain.UserMission;
import com.game.domain.p.DailyMessage;
import com.game.domain.p.RobotData;
import com.game.pb.BasePb.Base;
import com.game.pb.MissionPb.GetAllMissionRs;

/**
 * @Author 陈奎
 * @Description请求关卡信息
 * @Date 2022/10/21 15:33
 **/

public class GetAllMissionDaily extends AutoDaily {

	public GetAllMissionDaily(DailyMessage dailyMessage) {
		super(dailyMessage);
	}

	@Override
	public void onResult(MessageEvent event, Robot robot, Base base) {
		UserMissionCache missionCache = robot.getCache().getMissionCache();

		GetAllMissionRs getAllMissionRs = base.getExtension(GetAllMissionRs.ext);
		getAllMissionRs.getMissionList().forEach(e -> {
			UserMission userMission = new UserMission();
			userMission.setId(e.getId());
			userMission.setStar(e.getStar());
			userMission.setResourceTime(e.getResourceTime());
			userMission.setFightTimes(e.getFightTimes());
			userMission.setBuyTimes(e.getBuyTimes());
			userMission.setCountryPropNum(e.getCountryPropNum());
			userMission.setHeroBuy(e.getIsHeroBuy());
			userMission.setResourceLandNum(e.getResourceLandNum());
			userMission.setBuyEquipPaperTimes(e.getBuyEquipPaperTimes());
			userMission.setState(e.getState());
			missionCache.put(userMission);
		});

		RobotData robotDaily = robot.getData();
		robotDaily.setStatus(2);
	}

}
