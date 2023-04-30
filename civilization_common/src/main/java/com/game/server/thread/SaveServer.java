package com.game.server.thread;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @Description 数据存储服务
 * @Date 2022/9/9 11:30
 **/

public abstract class SaveServer<T> implements Runnable {

	private long createTime;

	protected HashMap<Integer, SaveThread> threadPool = new HashMap<Integer, SaveThread>();

	protected int threadNum;

	protected String name;

	public SaveServer(String name, int threadNum) {
		this.createTime = System.currentTimeMillis();
		this.name = name;
		this.threadNum = threadNum;

		createThreads();
		init();
	}

	public String serverName() {
		return name;
	}

	public void createThreads() {
		for (int i = 0; i < threadNum; i++) {
			threadPool.put(i, createThread(name + "-thread-" + i));
		}
	}

	public void init() {
	}

	abstract public void saveData(T t);

	abstract public void saveAll();

	public boolean saveDone() {
		Iterator<SaveThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			if (!it.next().workDone()) {
				return false;
			}
		}

		return true;
	}

	public int allSaveCount() {
		int saveCount = 0;
		Iterator<SaveThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			saveCount += it.next().getSaveCount();
		}
		return saveCount;
	}

	public void stop() {
		Iterator<SaveThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			it.next().stop(true);
		}
	}

	public void setLogFlag() {
		Iterator<SaveThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			it.next().setLogFlag();
		}
	}

	@Override
	public void run() {
		Iterator<SaveThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			it.next().start();
		}
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	abstract public SaveThread createThread(String name);

	public String getName() {
		return name;
	}
}
