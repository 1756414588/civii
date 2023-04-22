package com.game.timer;

import com.game.constant.WorldActivityConsts;
import com.game.domain.WorldData;
import com.game.domain.p.WorldActPlan;
import com.game.manager.WorldManager;
import com.game.manager.ZergManager;
import com.game.service.WorldActPlanService;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 虫族主宰定时器
 */
public class ZergTimer extends TimerEvent {

	public ZergTimer() {
		super(-1, TimeHelper.SECOND_MS);
	}

	@Override
	public void action() {
		WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
		ZergManager zergManager = SpringUtil.getBean(ZergManager.class);

		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_13);
		if (worldActPlan == null) {
			boolean isFinishCon = zergManager.isFinsihCond(WorldActivityConsts.ACTIVITY_13);
			if (isFinishCon) {// 开启条件达成,则初始化
				SpringUtil.getBean(WorldActPlanService.class).openWorldAct();
			}
			return;
		}
//		zergManager.checkZergWorldActPlan(worldActPlan);// 更改worldActPlan状态，是否开启活动
//		zergManager.checkRound(worldActPlan);// 如果活动开启则进行轮询
	}
}
