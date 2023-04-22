package com.game.uc.dao.ifs.p;

import com.game.uc.domain.s.Channel;
import com.game.uc.domain.s.StaticPackageConfig;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

/**
 * @author zcp
 * @date 2021/6/7 17:58
 * 诵我真名者,永不见bug
 */
public interface ChannelDao {

    //List<Channel> loadAll();

    @MapKey("channel")
    Map<Integer,Channel> loadAll();

    @MapKey("packageName")
    Map<String, StaticPackageConfig> loadPack();
}
