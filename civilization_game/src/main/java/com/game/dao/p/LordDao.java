package com.game.dao.p;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.game.domain.p.Lord;

public interface LordDao {

	public Lord selectLordById(Long lordId);

	public void updateLord(Lord lord);

	public List<Lord> load(@Param("curIndex") long curIndex, @Param("count") int count);

	int queryAutoIncrement(String database);
}
