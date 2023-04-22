package com.game.server.thread;

import com.game.dao.p.RecordDao;
import com.game.domain.Record;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @Description 机器人操作记录
 * @Date 2022/9/14 11:30
 **/

public class SaveRecordThread extends SaveThread<Record> {

	// 命令执行队列
	private LinkedBlockingQueue<Integer> queues = new LinkedBlockingQueue<Integer>();

	private HashMap<Integer, Record> record_map = new HashMap<Integer, Record>();

	public SaveRecordThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !queues.isEmpty()) {
			Record record = null;
			synchronized (this) {
				Integer o = queues.poll();
				if (o != null) {
					record = record_map.remove(o.intValue());
				}
			}
			if (record == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}
			} else {
				try {
					RecordDao recordDao = SpringUtil.getBean(RecordDao.class);
					recordDao.update(record);
					if (logFlag) {
						saveCount++;
					}
					LogHelper.SAVE_LOGGER.error("{} SAVE ACCOUNT_KEY:{}", threadName, record.getAccountKey());
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("Role Exception:{}", record.getAccountKey(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save done :" + saveCount);
	}


	/**
	 * Overriding: add
	 *
	 * @param record
	 * @see SaveThread#add(Object)
	 */
	@Override
	public void add(Record record) {
		try {
			synchronized (this) {
				if (!record_map.containsKey(record.getAccountKey())) {
					this.queues.add(record.getAccountKey());
				}
				this.record_map.put(record.getAccountKey(), record);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
