package com.game.server.exec;

import com.game.define.Exec;
import com.game.server.ITask;
import com.game.server.executor.CmdExecutor;
import com.game.server.work.BattleTask;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description 战斗任务处理器
 * @Date 2022/11/1 15:01
 **/

@Component
@Exec(threadNum = 1, threadName = "BattleExecutor")
public class BattleExecutor extends CmdExecutor {


	public void add(ITask task) {
		try {
			if (task instanceof BattleTask) {
				dispatch(0, task);
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}
}
