package com.game.uc.netty;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderedQueuePoolExecutor extends ThreadPoolExecutor {

	public OrderedQueuePoolExecutor(String name, int corePoolSize, int maxQueueSize) {
		super(corePoolSize, 2 * corePoolSize, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public void addTask(Runnable task) {
		execute(task);
	}
}
