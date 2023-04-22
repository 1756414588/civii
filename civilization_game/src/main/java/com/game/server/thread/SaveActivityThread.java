package com.game.server.thread;

import com.game.domain.p.Activity;
import com.game.manager.ActivityManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 陈奎
 * @Description 活动数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveActivityThread extends SaveThread<Activity> {

	// 命令执行队列
	private LinkedBlockingQueue<Integer> activity_queue = new LinkedBlockingQueue<Integer>();

	private HashMap<Integer, Activity> activity_map = new HashMap<Integer, Activity>();

	public SaveActivityThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !activity_queue.isEmpty()) {
			Activity activity = null;
			synchronized (this) {
				Object o = activity_queue.poll();
				if (o != null) {
					int activityId = (Integer) o;
					activity = activity_map.remove(activityId);
				}
			}
			if (activity == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.SAVE_LOGGER.error(threadName + " Wait Exception:" + e.getMessage());
				}
			} else {
				try {
					ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
					activityManager.update(activity);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.SAVE_LOGGER.error("Activity Exception:" + activity.getActivityId(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);
	}

	/**
	 * Overriding: add
	 *
	 * @param activity
	 * @see com.game.server.thread.SaveThread#add(java.lang.Object)
	 */
	@Override
	public void add(Activity activity) {

		try {
			synchronized (this) {
				if (!activity_map.containsKey(activity.getActivityId())) {
					this.activity_queue.add(activity.getActivityId());
				}
				this.activity_map.put(activity.getActivityId(), activity);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
