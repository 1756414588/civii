package com.game.cache;

import com.google.common.collect.HashBasedTable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description用户战斗相关
 * @Date 2022/10/27 14:04
 **/

@Getter
@Setter
public class UserWarCache {

	// 国战
	private Map<Integer, Integer> warAttends = new HashMap<>();

	//巨型虫族
	private Map<Long, Integer> bigMonsterWar = new HashMap<>();

	public void put(int warId) {
		warAttends.put(warId, 1);
	}

	public boolean isAttend(int warId) {
		return warAttends.containsKey(warId);
	}


}
