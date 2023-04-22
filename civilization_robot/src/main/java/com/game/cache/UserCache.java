package com.game.cache;

import lombok.Getter;

/**
 *
 * @Description
 * @Date 2022/9/22 10:23
 **/

@Getter
public class UserCache {

	// 背包缓存
	private UserBagCache bagCache = new UserBagCache();

	// 任务缓存
	private UserTaskCache taskCache = new UserTaskCache();

	// 背包相关信息
	private UserHeroCache heroCache = new UserHeroCache();

	// 玩家地图相关
	private UserMapCache mapCache = new UserMapCache();

	// 远征相关
	private UserJourneyCache journeyCache = new UserJourneyCache();

	// 副本相关
	private UserMissionCache missionCache = new UserMissionCache();

	// 建筑缓存
	private UserBuildingCache buildingCache = new UserBuildingCache();

	// 世界战斗相关缓存
	private UserWarCache userWarCahce = new UserWarCache();

}
