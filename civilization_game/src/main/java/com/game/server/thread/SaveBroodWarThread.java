package com.game.server.thread;

import com.game.dataMgr.StaticBroodWarMgr;
import com.game.domain.Brood;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @Description 母巢之战活动数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveBroodWarThread extends SaveThread<Brood> {

	/**
	 * 命令执行队列
	 */
	private LinkedBlockingQueue<Brood> data_queue = new LinkedBlockingQueue<Brood>();

	public SaveBroodWarThread(String threadName) {
		super(threadName);
	}

	@Override
	public void run() {
		stop = false;
		done = false;
		while (!stop || !data_queue.isEmpty()) {
			Brood data = null;
			synchronized (this) {
				data = data_queue.poll();
			}
			if (data == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage());
				}
			} else {
				try {

					StaticBroodWarMgr staticBroodWarMgr = SpringUtil.getBean(StaticBroodWarMgr.class);

					// 母巢和炮塔信息
					data.getBroodWarDataMap().forEach((e, f) -> {
						staticBroodWarMgr.replaceBroodWar(f);
					});

					// 任命信息
					if (!data.getAppoints().isEmpty()) {
						data.getAppoints().forEach(e -> {
							staticBroodWarMgr.replacePostion(e);
						});
					}

					// 战报信息
					if (!data.getBroodWarReportList().isEmpty()) {
						staticBroodWarMgr.clearReport();
						data.getBroodWarReportList().forEach(e -> {
							staticBroodWarMgr.saveReport(e);
						});
					}

					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("data [{}],[{}] Exception:[{}]", data, e);
				}
			}
		}
		done = true;
		LogHelper.ERROR_LOGGER.error(threadName + " stopped!! save done :" + saveCount);
	}

	/**
	 * Overriding: add
	 *
	 * @param brood
	 * @see SaveThread#add(Object)
	 */
	@Override
	public void add(Brood brood) {
		try {
			synchronized (this) {
				this.data_queue.add(brood);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage());
		}
	}
}
