package com.game.dao.p;

import java.util.List;

import com.game.servlet.domain.ServerRadio;

public interface ServerRadioDao {
    int deleteByPrimaryKey(Long keyid);

    int insert(ServerRadio record);

    int insertSelective(ServerRadio record);

    ServerRadio selectByPrimaryKey(Long keyid);

    int updateByPrimaryKeySelective(ServerRadio record);

    int updateByPrimaryKey(ServerRadio record);
    
    List<ServerRadio> selectAllServerRadio();
}