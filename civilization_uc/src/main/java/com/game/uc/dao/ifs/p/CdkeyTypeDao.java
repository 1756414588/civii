package com.game.uc.dao.ifs.p;

import com.game.uc.CdkeyType;

import java.util.List;

public interface CdkeyTypeDao {
    int deleteByPrimaryKey(Integer autoid);

    int insert(CdkeyType record);

    int insertSelective(CdkeyType record);

    CdkeyType selectByPrimaryKey(Integer autoid);

    int updateByPrimaryKeySelective(CdkeyType record);

    int updateByPrimaryKey(CdkeyType record);

    List<CdkeyType> selectAll();
}