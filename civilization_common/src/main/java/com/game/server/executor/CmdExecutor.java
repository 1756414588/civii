package com.game.server.executor;

import com.game.server.ITask;
import com.game.server.thread.TaskThead;

/**
 *
 * @Description 命令处理器父类
 * @Date 2022/9/9 11:30
 **/

public abstract class CmdExecutor implements IExecutor {

	protected TaskThead[] executor;

	private IExecutorFactory factory;

	public int getThreadNum() {
		return factory.threadNum();
	}

	public String getThreadName() {
		return factory.threadName();
	}

	@Override
	public void init(IExecutorFactory factory) {
		this.factory = factory;
		if (factory.threadNum() <= 0) {
			return;
		}
		executor = new TaskThead[factory.threadNum()];
		for (int i = 0; i < factory.threadNum(); i++) {
			String name = factory.threadName() + "-" + (i + 1);
			executor[i] = new TaskThead(i, name);
		}
		start();
	}

	@Override
	public void start() {
		if (executor == null) {
			return;
		}
		for (int i = 0; i < executor.length; i++) {
			executor[i].start();
		}
	}

	@Override
	public void stopWhenEmpty() {
		if (executor == null) {
			return;
		}
		for (int i = 0; i < executor.length; i++) {
			executor[i].stopWhenEmpty();
		}
	}

	@Override
	public void stop() {
		if (executor == null) {
			return;
		}
		for (int i = 0; i < executor.length; i++) {
			executor[i].stopWhenEmpty();
		}
	}

	public void dispatch(int index, ITask task) {
		executor[index].add(task);
	}

	@Override
	public boolean checkRunning() {
		if (executor == null) {
			return false;
		}
		for (TaskThead t : executor) {
			if (t.running) {
				return true;
			}
		}
		return false;
	}
}
