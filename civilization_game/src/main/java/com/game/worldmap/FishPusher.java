package com.game.worldmap;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FishPusher {

	private long playerId;
	// teamId,日期
	private Map<Integer, Integer> pushData = new HashMap<>();


	public boolean isPushToday(int teamId, int today) {
		if (!pushData.containsKey(teamId)) {
			return false;
		}
		int record = pushData.get(teamId);
		if (record != today) {
			return false;
		}
		return true;
	}

	public void pushToday(int teamId, int today) {
		pushData.put(teamId, today);
	}


}
