package com.game.server.thread;

import com.game.domain.p.Chat;
import com.game.manager.ChatManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 陈奎
 * @Description 聊天数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveChatThread extends SaveThread<Chat> {

	// 命令执行队列
	private LinkedBlockingQueue<Chat> chat_queue = new LinkedBlockingQueue<Chat>();

	public SaveChatThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !chat_queue.isEmpty()) {
			Chat chat = null;
			synchronized (this) {
				chat = chat_queue.poll();
			}
			if (chat == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage());
				}
			} else {
				try {
					ChatManager chatManager = SpringUtil.getBean(ChatManager.class);
					chatManager.addServerChat(chat);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("chat Exception:" + chat.toString(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);

	}

	/**
	 * Overriding: add
	 *
	 * @param chat
	 * @see com.game.server.thread.SaveThread#add(java.lang.Object)
	 */
	@Override
	public void add(Chat chat) {
		try {
			synchronized (this) {
				this.chat_queue.add(chat);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}
}
