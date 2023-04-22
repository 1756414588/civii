package com.game.server;


import com.game.define.Exec;
import com.game.define.ExecPool;
import com.game.server.exec.MessageExecutor;
import com.game.server.executor.IExecutor;
import com.game.server.executor.IExecutorFactory;
import com.game.util.ClassUtil;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author 陈奎
 * @Description 线程处理器服务
 * @Date 2022/9/9 11:30
 **/

public class ExecServer {

	private Map<String, IExecutor> execMap = new HashMap<>();

	static ExecServer inst = new ExecServer();

	public static ExecServer getInst() {
		return inst;
	}

	public void init() {
		Package pack = MessageExecutor.class.getPackage();
		Set<Class<?>> allClasses = ClassUtil.getClasses(pack);

		for (Class<?> clazz : allClasses) {
			Exec exec = clazz.getAnnotation(Exec.class);
			if (exec != null) {
				IExecutor executor = (IExecutor) SpringUtil.getBean(clazz);
				executor.init(new IExecutorFactory() {
					@Override
					public int threadNum() {
						return exec.threadNum();
					}

					@Override
					public String threadName() {
						return exec.threadName();
					}
				});
				execMap.put(clazz.getName(), executor);
				continue;
			}

			ExecPool execPool = clazz.getAnnotation(ExecPool.class);
			if (execPool != null) {

			}
		}
	}

	public void shutDownGraceful() {
		try {
			execMap.forEach((e, f) -> {
				f.stopWhenEmpty();
			});

			while (true) {
				if (isAllShutdown()) {
					break;
				}
				Thread.sleep(60);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isAllShutdown() {
		for (IExecutor executor : execMap.values()) {
			if (executor.checkRunning()) {
				return false;
			}
		}
		return true;
	}

	public IExecutor getExecutor(Class<?> clazz) {
		return execMap.get(clazz.getName());
	}

}
