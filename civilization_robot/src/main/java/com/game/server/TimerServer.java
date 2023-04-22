package com.game.server;

import com.game.StartRobotApp;
import com.game.define.AppTimer;
import com.game.server.executor.ScheduleExecutor;
import com.game.timer.TimerEvent;
import com.game.util.ClassUtil;
import com.game.util.LogHelper;
import java.util.Set;

/**
 * @Author 陈奎
 * @Description 定时服务
 * @Date 2022/9/15 11:30
 **/

public class TimerServer {

	public static TimerServer inst = new TimerServer();

	private ScheduleExecutor scheduleExecutor = new ScheduleExecutor(50);

	public static TimerServer getInst() {
		return inst;
	}

	protected TimerServer() {
	}

	/**
	 * 注册应用定时器
	 */
	public void registerAppTimer() {
		try {
			Set<Class<?>> allClasses = ClassUtil.getClasses(StartRobotApp.class.getPackage());

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

	public void addDelayEvent(TimerEvent timerEvent) {
		scheduleExecutor.addDelay(timerEvent);
	}

}
