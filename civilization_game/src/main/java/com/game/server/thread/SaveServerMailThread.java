package com.game.server.thread;

import com.game.manager.MailManager;
import com.game.servlet.domain.SendMail;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @Description 邮件数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveServerMailThread extends SaveThread<SendMail> {

	// 命令执行队列
	private LinkedBlockingQueue<Long> serverMail_queue = new LinkedBlockingQueue<Long>();

	private HashMap<Long, SendMail> serverMail_map = new HashMap<Long, SendMail>();

	public SaveServerMailThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !serverMail_queue.isEmpty()) {
			SendMail sendMail = null;
			synchronized (this) {
				Long keyId = serverMail_queue.poll();
				if (keyId != null) {
					sendMail = serverMail_map.remove(keyId);
				}
			}
			if (sendMail == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage());
				}
			} else {
				try {
					MailManager mailManager = SpringUtil.getBean(MailManager.class);
					mailManager.updateServerMail(sendMail);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("ServerMail Exception:" + sendMail.getKeyId(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);

	}

	/**
	 * Overriding: add
	 *
	 * @param sendMail
	 * @see com.game.server.thread.SaveThread#add(java.lang.Object)
	 */
	@Override
	public void add(SendMail sendMail) {

		try {
			synchronized (this) {
				if (!serverMail_map.containsKey(sendMail.getKeyId())) {
					this.serverMail_queue.add(sendMail.getKeyId());
				}
				this.serverMail_map.put(sendMail.getKeyId(), sendMail);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}


}
