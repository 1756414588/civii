package com.game.server.thread;

import com.game.domain.p.Item;
import com.game.manager.PlayerManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 陈奎
 * @Description 道具数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveItemThread extends SaveThread<Item> {

	/**
	 * 命令执行队列
	 */
	private LinkedBlockingQueue<Item> items_queue = new LinkedBlockingQueue<Item>();

	public SaveItemThread(String threadName) {
		super(threadName);
	}

	@Override
	public void run() {
		stop = false;
		done = false;
		while (!stop || !items_queue.isEmpty()) {
			Item item = null;
			synchronized (this) {
				item = items_queue.poll();
			}
			if (item == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}
			} else {
				try {
					PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
					playerManager.updateItem(item);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("Item [{}],[{}],[{}] Exception:[{}]", item.getLordId(), item.getItemId(), item.getItemNum(), e);
				}
			}
		}
		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);
	}

	/**
	 * Overriding: add
	 *
	 * @param item
	 * @see SaveThread#add(Object)
	 */
	@Override
	public void add(Item item) {
		try {
			synchronized (this) {
				this.items_queue.add(item);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
