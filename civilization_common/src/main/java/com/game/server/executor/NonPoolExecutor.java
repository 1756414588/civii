package com.game.server.executor;

import com.game.define.ExecPool;
import com.game.server.ICommand;
import com.game.server.ITask;
import com.game.util.LogHelper;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @Description 处理器工厂
 * @Date 2022/9/9 11:30
 **/

@ExecPool(coreSize = 5, queueSize = 10, poolName = "executor-pool-")
public class NonPoolExecutor implements IExecutor {

	private ThreadPoolExecutor threadPoolExecutor;
	private IExecutorFactory factory;

	/**
	 * 此处需要做队列,避免阻塞
	 *
	 * @param command
	 */
	public void execute(ICommand command) {
		threadPoolExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					long start = System.currentTimeMillis();
					command.action();
					long end = System.currentTimeMillis();
					if (end - start > 5L) {
						LogHelper.GAME_LOGGER.error(command.getClass().getSimpleName() + " run:" + (end - start));
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void init(IExecutorFactory factory) {
		threadPoolExecutor = new ThreadPoolExecutor(factory.threadNum(), factory.threadNum() * 2, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
	}

	@Override
	public void start() {
	}

	@Override
	public void stopWhenEmpty() {
	}

	@Override
	public void stop() {
		threadPoolExecutor.shutdown();
	}

	@Override
	public void add(ITask task) {

	}

	@Override
	public boolean checkRunning() {
		return false;
	}


}
