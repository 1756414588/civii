package com.game.uc.dao.ifs.s;

import com.game.uc.domain.s.StaticVersion;

import java.util.List;

/**
 *
 * @date 2020/12/18 11:27
 * @description
 */
public interface StaticVersionDao {
    List<StaticVersion> loadAll();
}
