package com.game.server;

import com.game.StartGate;
import com.game.define.AppTimer;
import com.game.server.executor.ScheduleExecutor;
import com.game.timer.TimerEvent;
import com.game.util.ClassUtil;
import com.game.util.LogHelper;
import java.util.Set;

/**
 * @Author 陈奎
 * @Description 定时服务
 * @Date 2022/9/9 11:30
 **/

public class TimerServer extends AbsServer {

	private ScheduleExecutor scheduleExecutor;

	protected TimerServer(String name) {
		super(name);
		initExecutor();
	}

	private void initExecutor() {
		scheduleExecutor = new ScheduleExecutor(1);
	}

	/**
	 * 注册应用定时器
	 */
	public void registerAppTimer() {
		try {
			Set<Class<?>> allClasses = ClassUtil.getClasses(StartGate.class.getPackage());

			for (Class<?> clazz : allClasses) {
				AppTimer clazzAnnotation = clazz.getAnnotation(AppTimer.class);
				if (clazzAnnotation != null) {
					TimerEvent timerEvent = (TimerEvent) clazz.newInstance();
					scheduleExecutor.addTimer(timerEvent);
				}
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
			System.exit(0);
		}
	}

	@Override
	public String getGameType() {
		return null;
	}

	@Override
	protected void stop() {
	}
}
