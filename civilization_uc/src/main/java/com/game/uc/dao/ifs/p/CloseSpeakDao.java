package com.game.uc.dao.ifs.p;

import com.game.uc.CloseSpeak;

import java.util.List;

public interface CloseSpeakDao {
    int deleteByPrimaryKey(Long roleid);

    int insert(CloseSpeak record);

    int insertSelective(CloseSpeak record);

    CloseSpeak selectByPrimaryKey(Long roleid);

    int updateByPrimaryKeySelective(CloseSpeak record);

    int updateByPrimaryKey(CloseSpeak record);

    List<CloseSpeak> selectAll();
}