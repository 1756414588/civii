package com.game.uc.dao.ifs.p;

import com.game.uc.Server;

import java.util.List;

/**
 * @author jyb
 * @date 2020/4/7 11:48
 * @description
 */
public interface ServerDao {
    /**
     * 查找所有服务器
     * @return
     */
    List<Server> getServerList();

    int insertSelective(Server record);

    Server selectByPrimaryKey(Integer serverid);

    int updateByPrimaryKeySelective(Server record);

}