package com.game.server.thread;

import com.game.dao.p.RobotDataDao;
import com.game.domain.p.RobotData;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 陈奎
 * @Description 机器人数据
 * @Date 2022/9/14 11:30
 **/

public class SaveRobotDataThread extends SaveThread<RobotData> {

	// 命令执行队列
	private LinkedBlockingQueue<Integer> queues = new LinkedBlockingQueue<Integer>();

	private HashMap<Integer, RobotData> record_map = new HashMap<Integer, RobotData>();

	public SaveRobotDataThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !queues.isEmpty()) {
			RobotData robotData = null;
			synchronized (this) {
				Integer o = queues.poll();
				if (o != null) {
					robotData = record_map.remove(o.intValue());
				}
			}
			if (robotData == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}
			} else {
				try {
					RobotDataDao robotDataDao = SpringUtil.getBean(RobotDataDao.class);
					robotDataDao.update(robotData);
					if (logFlag) {
						saveCount++;
					}
					LogHelper.SAVE_LOGGER.error("{} SAVE ACCOUNT_KEY:{}", threadName, robotData.getAccountKey());
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("RobotData Exception:{}", robotData.getAccountKey(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);
	}


	/**
	 * Overriding: add
	 *
	 * @param robotData
	 * @see SaveThread#add(Object)
	 */
	@Override
	public void add(RobotData robotData) {
		try {
			synchronized (this) {
				if (!record_map.containsKey(robotData.getAccountKey())) {
					this.queues.add(robotData.getAccountKey());
				}
				this.record_map.put(robotData.getAccountKey(), robotData);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
