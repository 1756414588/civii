package com.game.dao.p;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.game.domain.p.Detail;

public interface DetailDao {
	public Detail selectDetail(Long lordId);

    public Detail selectDetail(long lordId);

    public void updateDetail(Detail detail);

    void insertDetail(Detail detail);
    
    public List<Detail> loadDetail(@Param("curIndex") long curIndex, @Param("count") int count);
}
