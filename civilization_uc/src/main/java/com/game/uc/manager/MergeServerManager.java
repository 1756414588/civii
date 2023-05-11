package com.game.uc.manager;

import com.alibaba.fastjson.JSON;
import com.game.pay.channel.ChannelConsts;
import com.game.pay.channel.PlayerExist;
import com.game.uc.Message;
import com.game.uc.Server;
import com.game.uc.dao.ifs.p.MergeServerDao;
import com.game.uc.domain.p.MergeServer;
import com.game.uc.domain.s.CountryId;
import com.game.util.HttpUtil;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 合服服务器ip映射
 * @Date 2021/3/8 22:06
 **/
@Service
public class MergeServerManager {

	@Autowired
	private MergeServerDao mergeServerDao;

	@Autowired
	private ServerManager serverManager;

	private List<MergeServer> mergeServers = new ArrayList<>();

	private List<PlayerExist> playerExists = new ArrayList<>();

	// 合服信息  主键为serverId
	private Map<Integer, List<MergeServer>> mergeServerMap = new HashMap<>();
	// 合服映射 服ID:服阵营:主服ID
	private HashBasedTable<Integer, Integer, Integer> mergeMapping = HashBasedTable.create();

	//玩家信息  主键为accountKey
	private Map<Integer, List<PlayerExist>> playerExistMap = new HashMap<>();

	//玩家信息   主键serverId   内部map主键为countryId
	private Map<Integer, Map<Integer, List<PlayerExist>>> playerExistMapByServer = new HashMap<>();

//	@PostConstruct
	public void init() {
		mergeServers.clear();
		playerExists.clear();
		mergeServerMap.clear();
		playerExistMap.clear();
		playerExistMapByServer.clear();
		mergeMapping.clear();
		mergeServers = mergeServerDao.selectByMergeServer();
		Map<Integer, Server> servers = serverManager.getAllServer();
		for (MergeServer mergeServer : mergeServers) {
			Server server = servers.get(mergeServer.getMergerServerId());
			if (server != null) {
				mergeServer.setIp(server.getIp());
				mergeServer.setJettyPort(server.getHttpPort());
				mergeServer.setNetPort(server.getPort());
			}
			List<MergeServer> mergeServerList = mergeServerMap.computeIfAbsent(mergeServer.getServerId(), e -> new ArrayList<>());
			mergeServerList.add(mergeServer);

			// 记录映射关系
			mergeMapping.put(mergeServer.getServerId(), mergeServer.getCountryId(), mergeServer.getMergerServerId());
		}
		mergeServers.clear();

		playerExists = mergeServerDao.selectByPlayerExist();
		for (PlayerExist playerExist : playerExists) {
			insertPlayerExist(playerExist);
		}
		playerExists.clear();
	}


	public List<MergeServer> getMergeServerByServerId(int serverId) {
		ArrayList<MergeServer> list = new ArrayList<>();
		List<MergeServer> mergeServers = mergeServerMap.computeIfAbsent(serverId, e -> new ArrayList<>());
		list.addAll(mergeServers);
		return list;
	}

	public List<MergeServer> getMergeServers() {
		List<MergeServer> mergeServerList = new ArrayList<>();
		for (List<MergeServer> value : mergeServerMap.values()) {
			mergeServerList.addAll(value);
		}
		return mergeServerList;
	}


	public List<PlayerExist> getPlayerExistListByServerId(int serverId) {
		ArrayList<PlayerExist> list = new ArrayList<>();
		Map<Integer, List<PlayerExist>> integerListMap = playerExistMapByServer.computeIfAbsent(serverId, e -> new HashMap<>());
		for (List<PlayerExist> value : integerListMap.values()) {
			list.addAll(value);
		}
		return list;
	}

	public List<PlayerExist> getPlayerExistListByKey(int accountKey) {
		return playerExistMap.computeIfAbsent(accountKey, e -> new ArrayList<>());
	}

	public int getMinCountryId(int serverId) {
		HashMap<Integer, Integer> map = new HashMap<>();
		List<Integer> countryList = CountryId.getCountryList();
		Map<Integer, List<PlayerExist>> integerListMap = playerExistMapByServer.computeIfAbsent(serverId, e -> new HashMap<>());
		countryList.forEach(e -> {
			List<PlayerExist> country1List = integerListMap.computeIfAbsent(e, x -> new ArrayList<>());
			map.put(e, country1List.size());
		});
		int min = map.values().stream().sorted(Comparator.comparing(Integer::intValue)).collect(Collectors.toList()).get(0);
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			if (entry.getValue().intValue() == min) {
				return entry.getKey();
			}
		}
		Collections.shuffle(countryList);
		return countryList.get(0);
	}


	public boolean addPlayerExist(int accountKey, int serverId, int country) {
		List<PlayerExist> playerExists = playerExistMap.computeIfAbsent(accountKey, e -> new ArrayList<>());
		if (playerExists.stream().filter(e -> e.getServerId() == serverId).collect(Collectors.toList()).size() != 0) {
			return false;
		}
		PlayerExist playerExist = new PlayerExist(accountKey, serverId, country);
		insertPlayerExist(playerExist);
		Server server = serverManager.getServerById(serverId);
		try {
			if (server != null && server.getChannels().size() == 1 && server.getChannels().contains(ChannelConsts.KUAI_YOU_ID)) {
				mergeServerDao.insertPlayerExist(playerExist);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void insertPlayerExist(PlayerExist playerExist) {
		List<PlayerExist> playerExists1 = playerExistMap.computeIfAbsent(playerExist.getAccountKey(), e -> new ArrayList<>());
		playerExists1.add(playerExist);

		Map<Integer, List<PlayerExist>> integerListMap = playerExistMapByServer.computeIfAbsent(playerExist.getServerId(), e -> new HashMap<>());
		List<PlayerExist> playerExists = integerListMap.computeIfAbsent(playerExist.getCountry(), e -> new ArrayList<>());
		playerExists.add(playerExist);
	}


	public List<PlayerExist> selectByAccountKey(int accountKey) {
		List<PlayerExist> list = mergeServerDao.selectByAccountKey(accountKey);
		List<PlayerExist> result = new ArrayList<>();
		if (list != null && !list.isEmpty()) {
			for (PlayerExist playerExist : list) {
				if (isPlayerExists(playerExist)) {
					result.add(playerExist);
				}
			}
		}
		return result;
	}

	/**
	 * 修复u_serverinfos表的对应关系(合服清档)
	 *
	 * @param playerExist
	 */
	public boolean isPlayerExists(PlayerExist playerExist) {
		int serverId = playerExist.getServerId();
		int country = playerExist.getCountry();
		int accountKey = playerExist.getAccountKey();

		// 玩家角色所在的主服务器的ID（合服之后角色数据可能会迁移到其他服务器）
		int roleServerId = serverId;
		if (mergeMapping.contains(serverId, country)) {
			roleServerId = mergeMapping.get(serverId, country);
		}

		// 玩家角色所在服务器
		Server server = serverManager.getServerById(roleServerId);

		// 到主服中查询
		StringBuffer url = new StringBuffer();
		url.append("http://").append(server.getIp()).append(":").append(server.getHttpPort()).append("/player/playerExist.do");
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("serverId", String.valueOf(serverId));
		paramMap.put("accountKey", String.valueOf(accountKey));
		String message = HttpUtil.sendHttpPost(url.toString(), paramMap);
		Message mesg = JSON.parseObject(message, Message.class);
		if (mesg == null) {
			return true;
		}
		if ("1".equals(mesg.getData())) {
			return true;
		}
		return false;
	}
}