package com.game.server.executor;


import com.game.server.ITask;

/**
 * @Author 陈奎
 * @Description 处理器接口
 * @Date 2022/9/9 11:30
 **/

public interface IExecutor {

	public void init(IExecutorFactory factory);

	public void start();

	public void stopWhenEmpty();

	public void stop();

	public void add(ITask task);

	public boolean checkRunning();


}
