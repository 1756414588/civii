package com.game.recharge.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.SChannelConfig;
import com.game.pay.domain.KuaiYouConfig;
import com.game.recharge.dao.ifs.s.StaticChanelConfigDao;

/**
 * 2020年7月4日
 *
 *    halo_recharge ChannelConfigManager.java
 **/
@Component
public class ChannelConfigManager {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StaticChanelConfigDao chanelConfigDao;

	private Map<Integer, BaseChanelConfig> chanelConfigsByAppId = new ConcurrentHashMap<>();
	private Map<String, BaseChanelConfig> PayConfigMap = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		// 初始化SDK渠道配置
		List<SChannelConfig> selectAllChannelConfig = chanelConfigDao.selectAllChannelConfig();
		logger.info("加载渠道相关配置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		for (SChannelConfig sChannelConfig : selectAllChannelConfig) {
			BaseChanelConfig kuaiouConfig = new KuaiYouConfig(sChannelConfig);
			logger.error("loading config ->[{}]", kuaiouConfig);
			chanelConfigsByAppId.put(sChannelConfig.getGameChannelId(), kuaiouConfig);
			if (sChannelConfig.getPayIndef() != null) {
				PayConfigMap.put(sChannelConfig.getPayIndef().trim(), kuaiouConfig);
			}
		}
	}

	public KuaiYouConfig getChanelConfigByAppId(Integer appId) {
		return (KuaiYouConfig) chanelConfigsByAppId.get(appId);
	}

	public KuaiYouConfig getChanelConfigByPayName(String payname) {
		return (KuaiYouConfig) PayConfigMap.get(payname);
	}
}
