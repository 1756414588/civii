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

}
