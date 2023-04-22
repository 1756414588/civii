package com.game.server.exec;

import com.game.define.Exec;
import com.game.server.executor.CmdExecutor;
import com.game.server.ITask;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description 无序任务处理器
 * @Date 2022/9/9 11:30
 **/

@Component
@Exec(threadNum = 20, threadName = "NonExcetor")
public class NonExecutor extends CmdExecutor {

	private int taskCount = 0;

	@Override
	public void add(ITask task) {
		try {
			if (taskCount >= Integer.MAX_VALUE) {//防溢出
				taskCount = 0;
			}
			int index = taskCount++ % getThreadNum();
			dispatch(index, task);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}
}
