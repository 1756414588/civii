package com.game.recharge.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.recharge.dao.ifs.s.ServerDao;
import com.game.uc.Server;

/**
 * @author jyb
 * @date 2020/4/15 14:50
 * @description
 */
@Component
public class ServerManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServerDao serverDao;

	private Map<Integer, Server> servers = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		// 初始化server
		List<Server> servers = serverDao.getServerList();
		servers.forEach(server -> this.servers.put(server.getServerId(), server));
		logger.info("加载区服相关配置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		for (Server server : servers) {
			System.out.println(server);
		}
	}

	public Server getServerById(Integer serverid) {
		Server server = servers.get(serverid);
		if (server != null) {
			return server;
		}

		Server selectByPrimaryKey = serverDao.selectByPrimaryKey(serverid);
		if (null != selectByPrimaryKey) {
			servers.put(selectByPrimaryKey.getServerId(), selectByPrimaryKey);
			return selectByPrimaryKey;
		} else {
			return null;
		}
	}

	public List<Server> getAll() {
		return Lists.newArrayList(servers.values());
	}
}
