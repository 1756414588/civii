package com.game.recharge.dao.ifs.s;
/**
*2020年5月19日
*@CaoBing
*halo_uc
*StaticChanelConfigDao.java
**/

import java.util.List;

import com.game.pay.channel.SChannelConfig;

public interface StaticChanelConfigDao {
	public List<SChannelConfig> selectAllChannelConfig();
	
	public SChannelConfig selectSChannelConfigByAppId(int gameChannelId);
	
	public SChannelConfig selectSChannelConfigByPackName(String packageName);
}
