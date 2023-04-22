package com.game.manager;

import com.game.rank.RebelScoreRankMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jyb
 * @date 2020/5/6 19:05
 * @description
 */
@Service
public class NewRankManager {

    @Autowired
    private RebelScoreRankMgr rebelScoreRankMgr;

    public void init() {
        rebelScoreRankMgr.init();
    }

}
