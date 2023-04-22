package com.game.uc.dao.ifs.p;

import com.game.uc.CloseRole;
import com.game.uc.CloseRoleKey;

import java.util.List;

public interface CloseRoleDao {
    int deleteByPrimaryKey(CloseRoleKey key);

    int insert(CloseRole record);

    int insertSelective(CloseRole record);

    CloseRole selectByPrimaryKey(CloseRoleKey key);

    int updateByPrimaryKeySelective(CloseRole record);

    int updateByPrimaryKey(CloseRole record);

    List<CloseRole> selectAll();
}