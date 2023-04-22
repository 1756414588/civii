package com.game.server;


import com.game.StartRobotApp;
import com.game.define.Exec;
import com.game.define.ExecPool;
import com.game.server.executor.IExecutor;
import com.game.server.executor.IExecutorFactory;
import com.game.spring.SpringUtil;
import com.game.util.ClassUtil;
import com.game.util.LogHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExecServer {

	private Map<String, IExecutor> execMap = new HashMap<>();

	static ExecServer inst = new ExecServer();

	public static ExecServer getInst() {
		return inst;
	}

	public void init() {
		try {
			Package pack = StartRobotApp.class.getPackage();
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
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
			System.exit(0);
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
