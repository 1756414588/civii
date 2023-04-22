package com.game.worldmap.worldActPlan;

import com.game.domain.p.WorldActPlan;

public interface IWorldActPlan {

	/**
	 * 世界活动的业务逻辑
	 *
	 * @param worldActPlan
	 */
	void doLogic(WorldActPlan worldActPlan);

	/**
	 * 开启活动
	 *
	 * @param targetId
	 */
	void openAct(int targetId);

}
