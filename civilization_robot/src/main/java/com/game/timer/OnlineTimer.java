package com.game.timer;

import com.game.define.AppTimer;
import com.game.manager.LoginManager;
import com.game.manager.MessageManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AppTimer(desc = "在线空闲机器人")
public class OnlineTimer extends TimerEvent {

	public static Logger ONLINE_LOGGER = LoggerFactory.getLogger("ONLINE");

	public OnlineTimer() {
		super(-1, 3000);
	}

	@Override
	public void action() {
		MessageManager messageManager = SpringUtil.getBean(MessageManager.class);
		LoginManager loginManager = SpringUtil.getBean(LoginManager.class);

		int today = TimeHelper.getCurrentDay();
		int maxGuildId = (int) messageManager.getMaxGuildId();
		int maxDailyId = (int) messageManager.getMaxDailyId();

		long count = loginManager.getOnlineMap().values().stream().filter(e -> e.getOnline() == 1 && e.isSleep(today, maxGuildId, maxDailyId)).count();

		ONLINE_LOGGER.info("当前在线可用机器人{}", count);

	}


}
