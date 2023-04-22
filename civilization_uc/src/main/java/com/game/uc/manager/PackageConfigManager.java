package com.game.uc.manager;

import com.game.uc.dao.ifs.p.ChannelDao;
import com.game.uc.domain.s.StaticPackageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PackageConfigManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ChannelDao channelDao;

	private Map<String, StaticPackageConfig> allPackageConfig = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {

		logger.info("加载所有客户端包相关配置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		allPackageConfig = channelDao.loadPack();

	}

	public int getVerifyMode(String packageName) {
		StaticPackageConfig packageConfig = allPackageConfig.get(packageName);
		return packageConfig.getVerifyMode();
	}

	public int getOpen(String packageName) {
		return allPackageConfig.get(packageName) == null ? 1 : allPackageConfig.get(packageName).getOpen();
	}

	// final String defaultLayout = "[[1,1],[2,1],[3,1],[4,1],[5,1],[6,0],[7,1],[8,1],[9,1],[10,1],[11,1]]";
	final String defaultLayout = "[[1,0],[2,0],[3,1],[4,1],[5,1],[6,0],[7,1],[8,1],[9,1],[10,1],[11,0]]";

	public String getLoginLayout(String packageName) {
		return allPackageConfig.get(packageName) == null ? defaultLayout : allPackageConfig.get(packageName).getLoginLayout();
	}
}
