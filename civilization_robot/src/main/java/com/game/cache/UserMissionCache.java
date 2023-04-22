package com.game.cache;

import com.game.domain.UserMission;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/10/21 18:02
 **/

@Getter
@Setter
public class UserMissionCache {

	Map<Integer, UserMission> missionMap = new HashMap<>();
	private int maxId;
	private int starThreeId;

	public UserMission getLast() {
		return missionMap.get(maxId);
	}

	public UserMission getStartMission() {
		return missionMap.get(starThreeId);
	}

	public void put(UserMission userMission) {
		missionMap.put(userMission.getId(), userMission);
		if (userMission.getId() > maxId) {
			maxId = userMission.getId();
		}

		// 3星通关
		if (userMission.getId() > starThreeId && userMission.getStar() >= 3) {
			this.starThreeId = userMission.getId();
		}
	}

}
