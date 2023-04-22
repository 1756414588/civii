package com.game.server.structs;

import java.util.concurrent.ConcurrentHashMap;



public class OrderedQueuePool<V> {

	ConcurrentHashMap<Long, TasksQueue<V>> map = new ConcurrentHashMap<Long, TasksQueue<V>>();

	public OrderedQueuePool(int pool) {
		for (long i = 0; i < pool; i++) {
			TasksQueue<V> queue = new TasksQueue<V>();
			map.put(i, queue);
		}
	}

	/**
	 * 获得任务队列
	 *
	 * @param key
	 * @return
	 */
	public TasksQueue<V> getTasksQueue(Long key) {
		return map.get(key);
	}

	/**
	 * 获得全部任务队列
	 *
	 * @return
	 */
	public ConcurrentHashMap<Long, TasksQueue<V>> getTasksQueues() {
		return map;
	}

	/**
	 * 移除任务队列
	 *
	 * @param key
	 * @return
	 */
	public void removeTasksQueue(Long key) {
		map.remove(key);
	}
}
