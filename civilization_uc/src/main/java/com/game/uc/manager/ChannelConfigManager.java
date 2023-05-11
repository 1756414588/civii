package com.game.uc.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.ChannelConsts;
import com.game.pay.channel.SChannelConfig;
import com.game.pay.domain.KuaiYouConfig;
import com.game.uc.dao.ifs.p.StaticChanelConfigDao;

/**
 * 2020年5月19日
 *
 *    halo_uc ChannelConfigManager.java
 **/
@Service
public class ChannelConfigManager {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StaticChanelConfigDao channelConfigDao;

	/**
	 * 存放渠道对应渠道ID 配置
	 */
	private Map<Integer, BaseChanelConfig> channelConfigMap = new ConcurrentHashMap<>();
	/**
	 * gameChannelId 配置
	 */
	private Map<Integer, BaseChanelConfig> platConfigMap = new ConcurrentHashMap<>();

	private List<SChannelConfig> selectAllChannelConfig = new ArrayList<>();

//	@PostConstruct
	public void init() {
		// 初始化SDK渠道配置
		selectAllChannelConfig = channelConfigDao.selectAllChannelConfig();
		logger.info("加载渠道相关配置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		for (SChannelConfig config : selectAllChannelConfig) {
			BaseChanelConfig kuaiouConfig = new KuaiYouConfig(config);
			channelConfigMap.put(config.getPlatType(), kuaiouConfig);
			platConfigMap.put(config.getGameChannelId(), kuaiouConfig);
			logger.info(config.toString());
		}
		logger.info("加载渠道相关配置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

	public BaseChanelConfig getChannelConfig(Integer channel) {
		return channelConfigMap.get(channel);
	}

	public BaseChanelConfig getChannelConfigByPackageName(int channel, String packageName) {
		for (BaseChanelConfig config : platConfigMap.values()) {
			// 只有101和201的同渠道
			if (config.getPlatType() == ChannelConsts.NEW_KUAIYOU_ID || config.getPlatType() == ChannelConsts.KUAI_YOU_ID) {
				if (packageName.equals(config.getPackageName()) && config.getPlatType() == channel) {
					return config;
				}
			}
		}
		return null;
	}

	public BaseChanelConfig getChanelConfigByAppId(int appId) {
		return platConfigMap.get(appId);
	}

	public List<SChannelConfig> selectAllChannelConfig() {
		return selectAllChannelConfig;
	}
}
