package com.game.manager;

import com.game.domain.WorldData;
import com.game.domain.p.WorldTargetTask;
import com.game.domain.s.StaticWorldActPlan;
import com.game.domain.s.StaticWorldNewTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorldTargetManager {

	@Autowired
	private HeroManager heroManager;


	/**
	 * 是否完成目标任务
	 *
	 * @param staticWorldTarget
	 * @return
	 */
	public boolean isComplateTarget(WorldTargetTask task, StaticWorldNewTarget staticWorldTarget) {
		if (staticWorldTarget == null) {
			return false;
		}
		int targetType = staticWorldTarget.getTargetType();
		if (targetType == -1) {
			return false;
		}
		switch (targetType) {//
			case 4: {// 武将突破1阶
				int size = heroManager.getDivineAdvanceHeros().size();
				task.setNum(size);
				break;
			}
			case 5: {//武将突破2阶
				int size = (int) heroManager.getDivineAdvanceHeros().values().stream().filter(e -> e.getDiviNum() > 1).count();
				task.setNum(size);
				break;
			}

			default:
		}
		return task.getNum() >= staticWorldTarget.getWorldGoal();
	}

	public int getRoundType(WorldData worldData, StaticWorldActPlan worldActPlan) {
		if (worldData == null || worldData.getTarget() < 11) {
			return worldActPlan.getRoundType();
		}

		//虫族入侵
		if (worldActPlan.getId() == 8) {
			return worldActPlan.getRoundType() / 2;
		}

		//虫族主宰
		if (worldActPlan.getId() == 13) {
			return worldActPlan.getRoundType() / 2;
		}
		return worldActPlan.getRoundType();
	}
}
