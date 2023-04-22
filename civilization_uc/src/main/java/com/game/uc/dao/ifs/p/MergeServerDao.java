package com.game.uc.dao.ifs.p;

import com.game.pay.channel.PlayerExist;
import com.game.uc.domain.p.MergeServer;

import java.util.List;

public interface MergeServerDao {

    List<MergeServer> selectByMergeServer();

    List<PlayerExist> selectByPlayerExist();

    void insertPlayerExist(PlayerExist playerExist);


    List<PlayerExist> selectByAccountKey(Integer accountKey);
}
