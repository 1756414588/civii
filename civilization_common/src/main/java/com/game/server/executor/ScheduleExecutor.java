package com.game.server.executor;

import com.game.timer.TimerEvent;
import com.game.util.LogHelper;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @Description 定时器调度器
 * @Date 2022/9/9 11:30
 **/

public class ScheduleExecutor {

	private ScheduledExecutorService service;

	public ScheduleExecutor(int pool) {
		service = Executors.newScheduledThreadPool(pool);
	}

	public void addTimer(TimerEvent timerEvent) {
		try {
			service.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					timerEvent.action();
				}
			}, timerEvent.getInterval(), timerEvent.getInterval(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public void addDelay(TimerEvent timerEvent) {
		service.schedule(new Runnable() {
			@Override
			public void run() {
				timerEvent.action();
			}
		}, timerEvent.getInterval(), TimeUnit.MILLISECONDS);
	}
}
