package com.game.server.thread;

import com.game.util.LogHelper;

/**
 *
 * @Description 数据存储线程
 * @Date 2022/9/9 11:30
 **/

public abstract class SaveThread<T> extends Thread {

	// 运行标志
	protected boolean stop;

	protected boolean done;

	protected boolean logFlag = false;

	protected int saveCount = 0;

	// 线程名称
	protected String threadName;

	protected SaveThread(String threadName) {
		super(threadName);
		this.threadName = threadName;
	}

//	abstract public void run();

	abstract public void add(T t);

	public boolean workDone() {
		return done;
	}

	public int getSaveCount() {
		return saveCount;
	}

	public void setSaveCount(int saveCount) {
		this.saveCount = saveCount;
	}

	public void setLogFlag() {
		logFlag = true;
	}

	public void stop(boolean flag) {
		stop = flag;
		try {
			synchronized (this) {
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}
}
