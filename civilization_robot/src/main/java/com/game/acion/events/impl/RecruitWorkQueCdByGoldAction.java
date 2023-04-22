package com.game.acion.events.impl;

import com.game.acion.EventAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddGoldEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.pb.BasePb.Base;
import com.game.server.TimerServer;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/9/27 15:20
 **/

public class RecruitWorkQueCdByGoldAction extends EventAction {

	public RecruitWorkQueCdByGoldAction() {
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), 0, base.getCode());

		// 没有升级了,或者成功
		if (base.getCode() == 200) {
			return;
		}

		// 如果砖石不足,则使用添加砖石
		if (base.getCode() == GameError.NOT_ENOUGH_GOLD.getCode()) {
			AddGoldEvent addGoldEvent = new AddGoldEvent(robot, new EventAction(), 1000, 200L);
			TimerServer.getInst().addDelayEvent(addGoldEvent);

			// 添加砖石后继续秒CD
			messageEvent.reset(500);
			TimerServer.getInst().addDelayEvent(messageEvent);
		}
	}


}
