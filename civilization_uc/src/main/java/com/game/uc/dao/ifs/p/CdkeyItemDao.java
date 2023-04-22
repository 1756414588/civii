package com.game.uc.dao.ifs.p;

import com.game.uc.CdkeyItem;

import java.util.List;

public interface CdkeyItemDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CdkeyItem record);

    int insertSelective(CdkeyItem record);

    CdkeyItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CdkeyItem record);

    int updateByPrimaryKey(CdkeyItem record);

    List<CdkeyItem> selectAll();
}