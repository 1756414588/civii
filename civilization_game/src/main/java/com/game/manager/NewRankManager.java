package com.game.manager;

import com.game.Loading;
import com.game.define.LoadData;
import com.game.rank.RebelScoreRankMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @date 2020/5/6 19:05
 * @description
 */
@Service
@LoadData(name = "叛军排行", type = Loading.LOAD_USER_DB, initSeq = 3000)
public class NewRankManager extends BaseManager {

	@Autowired
	private RebelScoreRankMgr rebelScoreRankMgr;

	@Override
	public void init() throws Exception {
		rebelScoreRankMgr.init();
	}

}
