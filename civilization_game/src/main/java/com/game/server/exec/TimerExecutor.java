package com.game.server.exec;

import com.game.define.Exec;
import com.game.server.executor.IExecutor;
import com.game.server.executor.IExecutorFactory;
import com.game.server.ITask;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description 定时任务处理器
 * @Date 2022/9/9 11:30
 **/

@Component
@Exec(threadNum = 1, threadName = "TimerExcetor")
public class TimerExecutor implements IExecutor {

	@Override
	public void init(IExecutorFactory factory) {
	}

	@Override
	public void start() {
	}

	@Override
	public void stopWhenEmpty() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void add(ITask task) {
	}

	@Override
	public boolean checkRunning() {
		return false;
	}


}
