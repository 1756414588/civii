package com.game.dao.p;

import com.game.domain.p.World;

public interface WorldDao {

    World selectWorld(int worldId);

    void insertWorld(World world);

    int updateWorld(World world);
}
