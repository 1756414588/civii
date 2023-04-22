package com.game.dao.p;

import com.game.domain.p.PublicData;

public interface PublicDataDao {

	public PublicData queryPublicData(Integer id);

	public void update(PublicData publicData);
}
