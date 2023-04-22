package com.game.server.exec;

import com.game.define.Exec;
import com.game.server.executor.CmdExecutor;
import com.game.server.ITask;
import com.game.server.work.MessageWork;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description 消息任务处理器
 * @Date 2022/9/9 11:30
 **/

@Component
@Exec(threadNum = 10, threadName = "TaskExcetor")
public class MessageExecutor extends CmdExecutor {

	@Override
	public void add(ITask task) {
		try {
			MessageWork message = (MessageWork) task;
			if (message.getPlayerId() < 0) {//防负数溢出
				LogHelper.MESSAGE_LOGGER.info("playerId under zero!! channelId:{} playerId:{} cmd:{}", message.getChannelId(), message.getPlayerId(), message.getCmd());
				return;
			}
			int index = (int) (message.getPlayerId() % getThreadNum());
			dispatch(index, message);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}
}
