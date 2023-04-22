package com.game.dao.p;

import com.game.domain.Record;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;

public interface RecordDao {


	@MapKey("accountKey")
	public Map<Integer, Record> load();

	public void insert(Record record);

	public void update(Record record);

}
