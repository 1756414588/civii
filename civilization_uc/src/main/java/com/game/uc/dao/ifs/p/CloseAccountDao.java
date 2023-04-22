package com.game.uc.dao.ifs.p;

import com.game.uc.CloseAccount;

import java.util.List;

public interface CloseAccountDao {

    int deleteByPrimaryKey(Integer accountKey);

    int insert(CloseAccount record);

    int insertSelective(CloseAccount record);

    CloseAccount selectByPrimaryKey(Integer accountKey);

    int updateByPrimaryKeySelective(CloseAccount record);

    int updateByPrimaryKey(CloseAccount record);

    List<CloseAccount> selectAll();
}