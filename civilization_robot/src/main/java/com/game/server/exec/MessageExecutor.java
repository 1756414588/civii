package com.game.server.exec;

import com.game.define.Exec;
import com.game.server.ITask;
import com.game.server.executor.CmdExecutor;
import com.game.server.work.CmdWork;
import com.game.server.work.MessageTask;
import org.springframework.stereotype.Component;

@Component
@Exec(threadNum = 30, threadName = "MessageExecutor")
public class MessageExecutor extends CmdExecutor {

	@Override
	public void add(ITask task) {
		MessageTask messageTask = (MessageTask) task;
		int index = (int) (messageTask.getId() % getThreadNum());
		dispatch(index, messageTask);
	}
}
