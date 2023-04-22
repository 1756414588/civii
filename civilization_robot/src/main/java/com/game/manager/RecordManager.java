package com.game.manager;

import com.game.dao.p.RecordDao;
import com.game.define.LoadData;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.load.ILoadData;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2022/9/9 17:50
 **/

@LoadData(name = "记录", initSeq = 10000)
@Component
public class RecordManager implements ILoadData {

	// 机器人指令记录
	private Map<Integer, Record> recordMap = new ConcurrentHashMap<>();

	@Autowired
	private RecordDao recordDao;

	@Override
	public void load() {
		recordMap = recordDao.load();
	}

	@Override
	public void init() {
	}

	public Record getRecord(int accountKey) {
		return recordMap.get(accountKey);
	}

	public void insert(Record record) {
		recordDao.insert(record);
	}

	public Map<Integer, Record> getRecordMap() {
		return recordMap;
	}
}
