package com.game.server.executor;

/**
 * @Author 陈奎
 * @Description 处理器工厂
 * @Date 2022/9/9 11:30
 **/

public interface IExecutorFactory {

	int threadNum();

	String threadName();


}
