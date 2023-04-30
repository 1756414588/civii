package com.game.uc.service;

import com.game.spring.SpringUtil;
import java.util.*;
import java.util.stream.Collectors;

import com.game.uc.domain.s.Channel;
import com.game.uc.manager.PackageConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.ServerStatusConsts;
import com.game.pay.channel.PlayerExist;
import com.game.uc.Account;
import com.game.uc.Message;
import com.game.uc.Server;
import com.game.uc.dao.ifs.p.AccountDao;
import com.game.uc.domain.p.MergeServer;
import com.game.uc.domain.p.ServerCountryMapping;
import com.game.uc.domain.p.ServerInfo;
import com.game.uc.domain.p.ServerList;
import com.game.uc.domain.s.CountryId;
import com.game.uc.log.LogHelper;
import com.game.uc.log.domain.AccountCreateLog;
import com.game.uc.log.domain.AccountloginLog;
import com.game.uc.manager.AccountManager;
import com.game.uc.manager.MergeServerManager;
import com.game.uc.manager.ServerManager;
import com.game.uc.util.RandomHelper;
import com.game.util.HttpUtil;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2020/4/8 15:24 服务器列表
 */
@Service
public class ServerService {

	@Getter
	@Setter
	@Autowired
	private ServerManager serverManager;
	@Getter
	@Setter
	@Autowired
	private AccountDao accountDao;
	@Getter
	@Setter
	@Autowired
	private AccountManager accountManager;
	@Getter
	@Setter
	@Autowired
	private MergeServerManager mergeServerManager;

	@Autowired
	PackageConfigManager packageConfigManager;

	public ServerList getServerList(String accountId, int channel, String version, String imodel, String imei, String cpu, String idfa, String resolution, String deviceUuid, String ip, boolean isReview, String packageName, String versionFile) {
		ServerList serverList = new ServerList();
		List<ServerInfo> serverInfos = new ArrayList<>();
		List<Server> servers = serverManager.getServerByChannel(channel);
		Channel channelName = serverManager.getChannelName(channel);
		servers = servers.stream().sorted(Comparator.comparing(Server::getServerId)).collect(Collectors.toList());
		serverList.setOpen(packageConfigManager.getOpen(packageName));
		serverList.setIsOpenPackageLog(serverManager.getVersionByFile(versionFile));
		serverList.setLoginLayout(packageConfigManager.getLoginLayout(packageName));
		String prefix = "";
		if (channelName != null) {
			prefix = channelName.getPrefix();
		}
		Account account = getAccount(accountId, channel, version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, serverList, serverInfos, packageName);
		// 玩家角色存在的服务器
		List<PlayerExist> playerExistServerList = mergeServerManager.selectByAccountKey(account.getKeyId());
		long now = System.currentTimeMillis();
		int index = 1;
		for (Server server : servers) {
			String serverName = server.getServerType() == Server.SERVER_TYPE_ACTIVE ? prefix + index + server.getServerName() : server.getServerName();
			ServerInfo serverInfo = new ServerInfo(server, serverName);
			refreshServerInfo(serverInfo, playerExistServerList, channel);
			if (server.getServerType() == Server.SERVER_TYPE_TEST) {
				if (server.getWhiteList() != null && server.getWhiteList().contains(account.getKeyId())) {
					serverInfo.setJoin(true);
					serverInfos.add(serverInfo);
					continue;
				}

				if (server.getIpWhiteLists() != null && server.getIpWhiteLists().contains(ip)) {
					serverInfo.setJoin(true);
					serverInfos.add(serverInfo);
					continue;
				}
				if (server.getIpWhiteLists() != null && server.getIpWhiteLists().contains(ip)) {
					serverInfo.setJoin(true);
					serverInfos.add(serverInfo);
					continue;
				}

				if (!server.isVisable(version)) {
					continue;
				}
			} else if (server.getServerType() == Server.SERVER_TYPE_ACTIVE) {
				index++;
				switch (server.getState()) {
				default:
					break;
				case ServerStatusConsts.SERVER_FLUENT:
				case ServerStatusConsts.SERVER_FULL:
					if (server.getOpenTime().getTime() > now) {
						continue;
					}
					serverInfo.setJoin(true);
					serverInfos.add(serverInfo);
					break;
				case ServerStatusConsts.SERVER_BLOCK:
					boolean join = false;
					for (PlayerExist playerExist : playerExistServerList) {
						// 判断玩家在这个区服有没有角色
						if (playerExist.getServerId() == server.getServerId()) {
							join = true;
							break;
						}
					}
					serverInfo.setJoin(join);
					serverInfos.add(serverInfo);
					break;
				case ServerStatusConsts.SERVER_MAINTAIN:
					if (!isReview) {
						boolean iswhite = server.getWhiteList() != null && server.getWhiteList().contains(account.getKeyId()) || server.getIpWhiteLists() != null && server.getIpWhiteLists().contains(account.getIp());
						serverInfo.setJoin(iswhite);
						serverInfos.add(serverInfo);
					} else {
						index--;
					}
					break;
				case ServerStatusConsts.SERVER_CLOSE:
					// 白名单可进
					boolean iswhite = server.getWhiteList() != null && server.getWhiteList().contains(account.getKeyId()) || server.getIpWhiteLists() != null && server.getIpWhiteLists().contains(account.getIp());
					if (iswhite) {
						if (!isReview) {
							serverInfo.setJoin(true);
							serverInfos.add(serverInfo);
						} else {
							index--;
						}
					} else {
						index--;
					}
					break;
				case ServerStatusConsts.SERVER_REVIEW:
					if (isReview) {
						serverInfo.setJoin(true);
						serverInfos.add(serverInfo);
					} else {
						index--;
					}
					break;
				}
			}
		}
		return serverList;
	}

	public void refreshServerInfo(ServerInfo serverInfo, List<PlayerExist> playerExistServerList, int channel) {
		List<ServerCountryMapping> serverCountryMappingList = new ArrayList<>();
		boolean isExistRole = false;
		boolean isMergeServer = false;
		PlayerExist playerExist = new PlayerExist();
		for (PlayerExist exist : playerExistServerList) {
			if (serverInfo.getServerId() == exist.getServerId()) {
				isExistRole = true;
				playerExist = exist;
				serverInfo.setPlayerInfo(exist, channel);
				break;
			}
		}
		List<MergeServer> mergeServers = mergeServerManager.getMergeServerByServerId(serverInfo.getServerId());
		if (!mergeServers.isEmpty()) {
			isMergeServer = true;
		}
		// 已经合服且已有角色
		if (isMergeServer && isExistRole) {
			for (MergeServer mergeServer : mergeServers) {
				if (mergeServer.getCountryId().intValue() == playerExist.getCountry()) {
					serverInfo.setIp(mergeServer.getIp());
					serverInfo.setPort(mergeServer.getNetPort());
					break;
				}
			}
		}
		// 已经合服且没有角色
		if (isMergeServer && !isExistRole) {
			for (MergeServer mergeServer : mergeServers) {
				serverCountryMappingList.add(new ServerCountryMapping(mergeServer.getCountryId(), mergeServer.getIp(), mergeServer.getNetPort()));
			}
		}
		// 没有合服且没有角色
		if (!isMergeServer && !isExistRole) {
			for (Integer integer : CountryId.getCountryList()) {
				serverCountryMappingList.add(new ServerCountryMapping(integer, serverInfo.getIp(), serverInfo.getPort()));
			}
		}
		serverCountryMappingList = serverCountryMappingList.stream().sorted(Comparator.comparingInt(ServerCountryMapping::getCountyId)).collect(Collectors.toList());
		serverInfo.setExistRole(isExistRole);
		serverInfo.setServerCountryMappingList(serverCountryMappingList);
		serverInfo.setMinCountry(mergeServerManager.getMinCountryId(serverInfo.getServerId()));
	}

	private Account getAccount(String accountId, int channel, String version, String imodel, String imei, String cpu, String idfa, String resolution, String deviceUuid, String ip, ServerList serverList, List<ServerInfo> serverInfos, String packageName) {
		Account account = accountManager.get(accountId);
		String token = RandomHelper.generateToken();
		Date now = new Date();
		if (account == null) {
			account = registerAccount(accountId, channel, version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, token, now);
		}
		account.setToken(token);
		account.setVersionNo(version);
		account.setLoginDate(now);
		account.setDeviceNo(deviceUuid);
		account.setLoginDate(new Date());
		account.setImodel(imodel);
		account.setImei(imei);
		account.setIp(ip);
		account.setCpu(cpu);
		account.setDeviceUuid(deviceUuid);
		account.setIdfa(idfa);
		account.setResolution(resolution);
		account.setPackageName(packageName);

		serverList.setServers(serverInfos);
		serverList.setLastLogin(accountManager.getRecentServers(account));
		serverList.setKeyId(account.getKeyId());
		serverList.setToken(token);
		accountManager.add(account);
		return account;
	}

	private Account registerAccount(String accountId, int channel, String version, String imodel, String imei, String cpu, String idfa, String resolution, String deviceUuid, String ip, String token, Date now) {
		Account account;
		account = new Account();
		account.setAccount(accountId);
		account.setBaseVersion("NULL");
		account.setVersionNo(version);
		account.setToken(token);
		account.setDeviceNo(deviceUuid);
		account.setLoginDate(now);
		account.setCreateDate(now);
		account.setChannel(channel);
		account.setCreate(true);
		accountDao.insertWithAccount(account);
		// 账号创建日志埋点
		LogHelper logHelper = SpringUtil.getBean(LogHelper.class);
		logHelper.accountCreateLog(new AccountCreateLog(String.valueOf(channel), account.getCreateDate(), accountId, imodel, imei, ip, cpu, deviceUuid, idfa, account.getKeyId()));
		logHelper.accountLoginLog(new AccountloginLog(String.valueOf(channel), version, 0, account.getKeyId(), new Date(), ip, deviceUuid, idfa, imei, imodel, resolution, cpu));
		return account;
	}

	public Server getServer(Integer serverid) {
		return serverManager.getServerById(serverid);
	}

	public int updateServer(Server server) {
		return serverManager.updateServer(server);
	}

	public int addServer(Server server) {
		server.setCreateTime(new Date());
		return serverManager.addServer(server);
	}

	/**
	 * 服务器信息更新
	 *
	 * @return Message
	 */
	public Message postUpdateServer(Server server) {
		String serverUrl = serverManager.getServerUrl(server.getServerId());
		String url = serverUrl + "/" + "server/updateServer.do";
		Map<String, String> map = new HashMap<>(8);
		map.put("server", JSONObject.toJSONString(server));
		String msg = HttpUtil.sendPost(url, map);
		return JSONObject.parseObject(msg, Message.class);
	}
}
