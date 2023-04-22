package com.game.dao.p;

import java.util.List;

import com.game.season.SeasonInfo;

public interface SeasonActDao {

	void insert(SeasonInfo seasonInfo);

	List<SeasonInfo> loadPlayerSeasonInfo(long roleId);

}
