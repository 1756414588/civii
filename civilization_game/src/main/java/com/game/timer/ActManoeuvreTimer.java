package com.game.timer;

import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.domain.WorldData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticWorldActPlan;
import com.game.manager.ActManoeuvreManager;
import com.game.manager.WorldManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 沙盘演武定时器
 */
public class ActManoeuvreTimer extends TimerEvent {

	public ActManoeuvreTimer() {
		super(-1, TimeHelper.SECOND_MS);
	}

	@Override
	public void action() {
		WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
		ActManoeuvreManager actManoeuvreManager = SpringUtil.getBean(ActManoeuvreManager.class);
		StaticWorldActPlanMgr staticWorldActPlanMgr = SpringUtil.getBean(StaticWorldActPlanMgr.class);

		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_14);
		if (worldActPlan == null) {
			StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(WorldActivityConsts.ACTIVITY_14);
			if (worldData.getTarget() < staticWorldActPlan.getTargetId()) {
				return;
			}
			actManoeuvreManager.initWorldActPlan(staticWorldActPlan);
			return;
		}
		actManoeuvreManager.checkActPlan(worldActPlan);// 更改worldActPlan状态，是否开启活动

	}
}
