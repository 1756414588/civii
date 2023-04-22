package com.game.uc.dao.ifs.p;

import com.game.uc.Cdkey;

import java.util.List;

public interface CdkeyDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Cdkey record);

    int insertSelective(Cdkey record);

    Cdkey selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cdkey record);

    int updateByPrimaryKey(Cdkey record);

    List<Cdkey> selectCdkActivity(int rewardObjectId);
}