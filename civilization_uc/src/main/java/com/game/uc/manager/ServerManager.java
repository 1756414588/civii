package com.game.uc.manager;

import com.game.uc.Server;
import com.game.uc.ServerZone;
import com.game.uc.dao.ifs.p.ChannelDao;
import com.game.uc.dao.ifs.p.ServerDao;
import com.game.uc.dao.ifs.p.ServerZoneDao;
import com.game.uc.dao.ifs.s.StaticVersionDao;
import com.game.uc.domain.s.Channel;
import com.game.uc.domain.s.StaticPackageConfig;
import com.game.uc.domain.s.StaticVersion;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @date 2020/4/7 13:53
 * @description
 */
@Service
@Transactional
public class ServerManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServerDao serverDao;
	@Autowired
	private ServerZoneDao serverZoneDao;
	@Autowired
	private StaticVersionDao versionDao;
	@Autowired
	private ChannelDao channelDao;

	private Map<Integer, Server> servers = new ConcurrentHashMap<>();
	private Map<Integer, StaticVersion> versionMap = new ConcurrentHashMap<>();
	private Map<String, StaticVersion> versionFiles = new ConcurrentHashMap<>();

	private Map<Integer, ServerZone> serverZones = new ConcurrentHashMap<>();
	private Map<Integer, Channel> channelMap = new ConcurrentHashMap<>();

	// private Map<String, StaticPackageConfig> pack = new HashMap<>();

//	@PostConstruct
	public void init() {
		// 初始化server
		List<Server> servers = serverDao.getServerList();
		servers.forEach(server -> this.servers.put(server.getServerId(), server));
		// 初始化分区
		List<ServerZone> serverZones = serverZoneDao.getServerZoneList();
		serverZones.forEach(serverZone -> this.serverZones.put(serverZone.getId(), serverZone));
		channelMap.clear();
		this.channelMap = channelDao.loadAll();
		logger.info("加载区服相关配置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		initVersion();
	}

	public void initVersion() {
		versionMap.clear();
		versionFiles.clear();
		List<StaticVersion> versions = versionDao.loadAll();
		versions.forEach(e -> {
			versionMap.put(e.getId(), e);
			versionFiles.put(e.getName() + ".php", e);
		});
	}

	/**
	 * 通过渠道号分区列表
	 *
	 * @param channel
	 * @return
	 */
	public List<ServerZone> getServerZoneByChannel(String channel) {
		List<ServerZone> zones = new ArrayList<>();
		serverZones.forEach((zoneId, serverZone) -> {
			if (serverZone.getChannel().contains(channel)) {
				zones.add(serverZone);
			}
		});
		return zones;
	}

	/**
	 * 通过渠道号拿服务器名称
	 *
	 * @param channel
	 * @return
	 */
	public List<Server> getServerByChannel(int channel) {
		List<Server> servers = new ArrayList<>();
		this.servers.forEach((serverId, server) -> {
			if (server.getChannels() == null || server.getChannels().contains(channel)) {
				servers.add(server);
			}
		});
		return servers;
	}

	public Server getServerById(Integer serverid) {
		Server server = servers.get(serverid);
		if (server != null) {
			return server;
		}
		return serverDao.selectByPrimaryKey(serverid);
	}

	public int updateServer(Server server) {
		int total = serverDao.updateByPrimaryKeySelective(server);
		if (total > 0) {
			servers.put(server.getServerId(), server);
		}
		return total;
	}

	public int addServer(Server server) {
		int total = serverDao.insertSelective(server);
		if (total > 0) {
			servers.put(server.getServerId(), server);
		}
		return total;
	}

	/**
	 * 拿到server的url
	 *
	 * @param serverId
	 * @return
	 */
	public String getServerUrl(int serverId) {
		Server server = servers.get(serverId);
		if (server == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("http://").append(server.getIp()).append(":").append(server.getHttpPort());
		return sb.toString();
	}

	public Map<Integer, Server> getAllServer() {
		return this.servers;
	}

	public StaticVersion getVersion(int key) {
		return versionMap.get(key);
	}

	public int getVersionByFile(String file) {
		int logOpen = 0;
		if (versionFiles.containsKey(file)) {
			return versionFiles.get(file).getLog_open() == 1 ? 1 : 0;
		}
		return logOpen;

	}

	public Channel getChannelName(int channel) {
		return channelMap.get(channel);
	}

//	public String getChannelName(int channel) {
//		String name = channelMap.get(channel).getPrefix();
//		if (StringUtil.isNullOrEmpty(name)) {
//			return "";
//		}
//		return name;
//	}

}
