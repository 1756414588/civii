package com.game.server.thread;

import com.game.manager.ServerRadioManager;
import com.game.servlet.domain.ServerRadio;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @Description 全服通告数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveServerRadioThread extends SaveThread<ServerRadio> {

	// 命令执行队列
	private LinkedBlockingQueue<Long> serverRadio_queue = new LinkedBlockingQueue<Long>();

	private HashMap<Long, ServerRadio> serverRadio_map = new HashMap<Long, ServerRadio>();

	public SaveServerRadioThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !serverRadio_queue.isEmpty()) {
			ServerRadio ServerRadio = null;
			synchronized (this) {
				Long keyId = serverRadio_queue.poll();
				if (keyId != null) {
					ServerRadio = serverRadio_map.remove(keyId);
				}
			}
			if (ServerRadio == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage());
				}
			} else {
				try {
					ServerRadioManager radioManager = SpringUtil.getBean(ServerRadioManager.class);
					radioManager.updateServerRadio(ServerRadio);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("ServerRadio Exception:" + ServerRadio.getKeyId(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);

	}

	/**
	 * Overriding: add
	 *
	 * @param serverRadio
	 * @see com.game.server.thread.SaveThread#add(java.lang.Object)
	 */
	@Override
	public void add(ServerRadio serverRadio) {

		try {
			synchronized (this) {
				if (!serverRadio_map.containsKey(serverRadio.getKeyId())) {
					this.serverRadio_queue.add(serverRadio.getKeyId());
				}
				this.serverRadio_map.put(serverRadio.getKeyId(), serverRadio);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}
}
