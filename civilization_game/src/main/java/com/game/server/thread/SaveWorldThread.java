package com.game.server.thread;

import com.game.domain.p.World;
import com.game.manager.WorldManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 陈奎
 * @Description 世界数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveWorldThread extends SaveThread<World> {

	// 命令执行队列
	private LinkedBlockingQueue<Integer> world_queue = new LinkedBlockingQueue<Integer>();
	private HashMap<Integer, World> worldMap = new HashMap<Integer, World>();

	public SaveWorldThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !world_queue.isEmpty()) {

			World world = null;
			synchronized (this) {
				Integer keyId = world_queue.poll();
				if (keyId != null) {
					world = worldMap.remove(keyId);
				}
			}

			if (world == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}
			} else {
				try {
					WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
					worldManager.updateWorld(world);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("City Exception:" + world.getKeyId(), e);
				}
			}
		}
		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save count :" + saveCount);
	}

	@Override
	public void add(World world) {
		try {
			synchronized (this) {
				if (!worldMap.containsKey(world.getKeyId())) {
					this.world_queue.add(world.getKeyId());
				}
				this.worldMap.put(world.getKeyId(), world);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}
}
