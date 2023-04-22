package com.game.server.exec;

import com.game.define.Exec;
import com.game.server.executor.CmdExecutor;
import com.game.server.ITask;
import com.game.server.work.CmdWork;
import org.springframework.stereotype.Component;

@Component
@Exec(threadNum = 1, threadName = "RobotCMDExcetor")
public class RobotCmdExecutor extends CmdExecutor {

	@Override
	public void add(ITask task) {
		CmdWork work = (CmdWork) task;

		dispatch(0, work);
	}
}
