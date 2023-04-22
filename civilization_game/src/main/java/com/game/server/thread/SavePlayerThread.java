package com.game.server.thread;

import com.game.domain.Role;
import com.game.manager.PlayerManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @Description 玩家数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SavePlayerThread extends SaveThread<Role> {

	// 命令执行队列
	private LinkedBlockingQueue<Long> role_queue = new LinkedBlockingQueue<Long>();

	private HashMap<Long, Role> role_map = new HashMap<Long, Role>();

	public SavePlayerThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !role_queue.isEmpty()) {
			Role role = null;
			synchronized (this) {
				Long o = role_queue.poll();
				if (o != null) {
					role = role_map.remove(o.longValue());
				}
			}
			if (role == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}
			} else {
				try {
					PlayerManager playerDataManager = SpringUtil.getBean(PlayerManager.class);
					playerDataManager.updateRole(role);
					playerDataManager.saveUcServerInfos(role.getPlayer());
					if (logFlag) {
						saveCount++;
					}
					LogHelper.SAVE_LOGGER.error(threadName + " SAVE ROLE_ID:" + role.getRoleId());
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("Role Exception:" + role.getRoleId(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);
	}


	/**
	 * Overriding: add
	 *
	 * @param role
	 * @see com.game.server.thread.SaveThread#add(java.lang.Object)
	 */
	@Override
	public void add(Role role) {
		try {
			synchronized (this) {
				if (!role_map.containsKey(role.getRoleId())) {
					this.role_queue.add(role.getRoleId());
				}
				this.role_map.put(role.getRoleId(), role);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
