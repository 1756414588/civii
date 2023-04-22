package com.game.server.structs;

import java.util.concurrent.LinkedBlockingQueue;

public class TasksQueue<V> {

	private final LinkedBlockingQueue<V> tasksQueue = new LinkedBlockingQueue<V>();

	private boolean processingCompleted = true;

	public V poll() {
		return tasksQueue.poll();
	}

	public boolean add(V value) {
		return tasksQueue.add(value);
	}

	public void clear() {
		tasksQueue.clear();
	}

	public int size() {
		return tasksQueue.size();
	}

	public boolean isProcessingCompleted() {
		return processingCompleted;
	}

	public void setProcessingCompleted(boolean processingCompleted) {
		this.processingCompleted = processingCompleted;
	}

}
