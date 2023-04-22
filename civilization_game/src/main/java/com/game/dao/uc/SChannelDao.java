package com.game.dao.uc;
/**
 * 2020年5月19日
 *
 * @CaoBing halo_uc
 * StaticChanelConfigDao.java
 **/

import com.game.pay.channel.SChannelConfig;

import java.util.List;

public interface SChannelDao {
    public List<SChannelConfig> selectAllChannelConfig();
}
