package com.game.flame;

import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.domain.WorldData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticWorldActPlan;
import com.game.manager.ServerManager;
import com.game.manager.WorldManager;
import com.game.spring.SpringUtil;
import com.game.timer.TimerEvent;
import com.game.util.TimeHelper;

import java.util.Date;

public class ActFlameWarTimer extends TimerEvent {

	public ActFlameWarTimer() {
		super(-1, TimeHelper.SECOND_MS);
	}

	@Override
	public void action() {
		WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
			return;
		}

		FlameWarManager flameWarManager = SpringUtil.getBean(FlameWarManager.class);
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
		if (worldActPlan == null) {
			ServerManager bean = SpringUtil.getBean(ServerManager.class);
			Date openTime = bean.getServer().getOpenTime();
			int day = TimeHelper.whichDay(0, new Date(), openTime);
			// 开服一个月之后开启战火燎原
			if (day >= 30) {
				StaticWorldActPlanMgr staticWorldActPlanMgr = SpringUtil.getBean(StaticWorldActPlanMgr.class);
				StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(WorldActivityConsts.ACTIVITY_15);
				if (staticWorldActPlan != null) {
					flameWarManager.initFlameWar(staticWorldActPlan);
				}
			}
			return;
		}
		flameWarManager.checkActPlan(worldActPlan);
	}
}
