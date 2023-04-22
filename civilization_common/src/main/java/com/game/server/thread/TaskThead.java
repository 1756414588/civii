package com.game.server.thread;

import com.game.server.ITask;
import com.game.util.LogHelper;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskThead {

	protected LinkedBlockingQueue<ITask> queues = new LinkedBlockingQueue<ITask>();
//	protected ArrayBlockingQueue<ITask> queues = new ArrayBlockingQueue<ITask>();

	private int id;
	private String threadName;
	private Thread t;

	// 线程状态
	public volatile boolean running = false;
	// 停止线程
	protected AtomicBoolean stopWork = new AtomicBoolean(true);
	// 完成任务
	protected AtomicBoolean workDone = new AtomicBoolean(false);

	public TaskThead(int id, String threadName) {
		this.id = id;
		this.threadName = threadName;
	}

	public int getId() {
		return id;
	}

	public String getThreadName() {
		return threadName;
	}

	public boolean add(ITask task) {
		if (stopWork.compareAndSet(true, true)) {
			LogHelper.SAVE_LOGGER.info("id:{} threadName:{} 线程已停止", id, threadName);
			return false;
		}

		if (!running) {
			LogHelper.SAVE_LOGGER.info("id:{} threadName:{} 线程已停止", id, threadName);
			return false;
		}

		if (queues.remainingCapacity() < 1) {
			LogHelper.SAVE_LOGGER.info("id:{} threadName:{} 线程队列已满,不可继续消耗消息", id, threadName);
			return false;
		}

		try {
			queues.put(task);
		} catch (InterruptedException e) {
			LogHelper.GAME_LOGGER.error(e.getMessage(), e);
		}
		return true;
	}

	public boolean start() {
		if (!stopWork.compareAndSet(true, false)) {
			return false;
		}
		running = true;
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					try {
						if (stopWork.get() && queues.isEmpty()) {
							running = false;
							continue;
						}

						final ITask task = queues.poll(50L, TimeUnit.MILLISECONDS);
						if (task != null) {
							task.run();
						}

					} catch (InterruptedException e) {
						LogHelper.GAME_LOGGER.error(e.getMessage(), e);
					} catch (Exception e) {
						LogHelper.GAME_LOGGER.error(e.getMessage(), e);
					} finally {
					}
				}
				LogHelper.GAME_LOGGER.error(threadName + " stopped!!!");
				workDone.set(true);
			}
		}, threadName);
		t.start();
		return true;
	}

	public void stopWhenEmpty() {
		stopWork.compareAndSet(false, true);
	}

}
