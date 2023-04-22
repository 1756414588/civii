package com.game.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/9/21 20:16
 **/
@Getter
public class UserHeroCache {

	// 行军
	private Map<Integer, Integer> armys = new HashMap<>();
	// 上阵武将
	private List<Integer> embattles = new ArrayList<>();
	// 所有武将信息
	private Map<Integer, Integer> heroMap = new HashMap();


	/**
	 * 获取上阵的空闲武将
	 *
	 * @return
	 */
	public int getEmptyEmbattle() {
		for (int heroId : embattles) {
			if (heroId <= 0) {
				continue;
			}
			if (armys.containsKey(heroId)) {
				continue;
			}
			return heroId;
		}
		return 0;
	}

	public void putHero(int heroId) {
		heroMap.put(heroId, heroId);
	}

}
