package com.game.uc.servlet.server;

import com.game.spring.SpringUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.game.uc.*;
import com.game.uc.manager.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.Base64;
import com.game.constant.ServerStatusConsts;
import com.game.constant.UcCodeEnum;
import com.game.pay.channel.BaseChanelConfig;
import com.game.pay.channel.SChannelConfig;
import com.game.uc.dao.ifs.p.AccountDao;
import com.game.uc.domain.p.ServerList;
import com.game.uc.domain.s.StaticVersion;
import com.game.uc.log.LogHelper;
import com.game.uc.log.domain.AccountloginLog;
import com.game.uc.service.ServerService;
import com.game.uc.service.VerifyLoginService;
import com.game.uc.util.IPUtils;
import com.game.util.EncryptionAndDecryptionUtil;
import com.game.util.Md5Util;

import io.netty.util.internal.StringUtil;

/**
 *
 * @date 2020/4/8 9:55
 * @description
 */
@Controller
public class ServerHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@RequestMapping(value = "/{head}s{end}/{param}/{param2}", method = RequestMethod.POST)
	public void serverList(@PathVariable("head") String head, @PathVariable("end") String end, @PathVariable("param") String param, @PathVariable("param2") String param2, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.error("serverList is " + request.getRequestURI() + "rul is" + request.getRequestURL());
		getServerList(request, response);
	}

	/**
	 * 特殊接口， 仅用作前端测试跳过sdk检测
	 *
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/account/PreventSdkserver.do", method = RequestMethod.POST)
	public void getPreventSdkServerList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Message message;
		ServletInputStream inputStream = request.getInputStream();
		if (null == inputStream) {
			message = new Message(UcCodeEnum.PARAM_ERROR);
			respMessage(message, response);
		}
		try {
			String parmas = IOUtils.toString(inputStream);
			String string = new String(Base64.decodeFast(parmas), "utf-8");

			LoginParams loginParams = JSON.parseObject(string, LoginParams.class);
			if (null == loginParams) {
				message = new Message(UcCodeEnum.PARAM_ERROR);
				respMessage(message, response);
			}
			String channelStr = loginParams.getChannel();
			String packageName = loginParams.getPackageName();
			String token = loginParams.getToken();
			String account = loginParams.getAccount();
			BaseChanelConfig baseChanelConfig = null;
			if (channelStr == null || channelStr.equals("NAN") || channelStr.trim().equals("")) {
				message = new Message(UcCodeEnum.PARAM_ERROR);
				respMessage(message, response);
				return;
			} else {
				if (!channelStr.trim().equals("1")) {
					baseChanelConfig = SpringUtil.getBean(VerifyLoginService.class).verifyChannelLogin(packageName, Integer.parseInt(channelStr));
					if (baseChanelConfig == null) {
						message = new Message(UcCodeEnum.SDK_LOGIN_ERROR);
						respMessage(message, response);
						return;
					} else {
						account = baseChanelConfig.getParent_type() + "_" + account;
					}
				}

				String version = loginParams.getAppVersion() == null ? "" : loginParams.getAppVersion();
				String imodel = loginParams.getImodel() == null ? "" : loginParams.getImodel();
				String imei = loginParams.getImei() == null ? "" : loginParams.getImei();
				String cpu = loginParams.getCpu() == null ? "" : loginParams.getCpu();
				String idfa = loginParams.getIdfa() == null ? "" : loginParams.getIdfa();
				String resolution = loginParams.getResolution() == null ? "" : loginParams.getResolution();
				String deviceUuid = loginParams.getDeviceUuid() == null ? "" : loginParams.getDeviceUuid();
				String ip = IPUtils.getIpAddress(request);
				String versionFile = loginParams.getVersionFile() == null ? "" : loginParams.getVersionFile();

				logger.info("ServerHandler getServerList : channel {} ,account {} ,version {},imodel{},imei{},cpu{},idfa{},resolution{},deviceUuid{},ip{},versionFile:{}", Integer.parseInt(channelStr), account, version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, versionFile);
				ServerService serverService = SpringUtil.getBean(ServerService.class);
				boolean flag = baseChanelConfig == null ? false : baseChanelConfig.getIs_review() == 1;
				ServerList serverList = serverService.getServerList(account, Integer.parseInt(channelStr), version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, flag, packageName, versionFile);
				serverList.setTeamNum(baseChanelConfig.getTeamNum());
				String str = JSONObject.toJSONString(serverList);
				logger.info("ServerHandler getServerList : ServerList {} ", str);
				message = new Message(UcCodeEnum.SUCCESS, str);
				respMessage(message, response);
				return;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			message = new Message(UcCodeEnum.PARAM_ERROR);
			respMessage(message, response);
		}
	}

	@RequestMapping(value = "/account/server.do", method = RequestMethod.POST)
	public void getServerList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Message message = null;
		ServletInputStream inputStream = request.getInputStream();
		if (null == inputStream) {
			message = new Message(UcCodeEnum.PARAM_ERROR);
			respMessage(message, response);
		}

		try {
			String parmas = IOUtils.toString(inputStream);
			String string = new String(Base64.decodeFast(parmas), "utf-8");

			LoginParams loginParams = JSON.parseObject(string, LoginParams.class);
			logger.info("raw {} json{} decode {} ", parmas, JSON.parseObject(string, LoginParams.class), string);
			if (null == loginParams) {
				message = new Message(UcCodeEnum.PARAM_ERROR);
				respMessage(message, response);
			}

			String channelStr = loginParams.getChannel();
			String packageName = loginParams.getPackageName();
			String token = loginParams.getToken();
			String account = loginParams.getAccount();
			BaseChanelConfig baseChanelConfig = null;
			if (channelStr == null || channelStr.equals("NAN") || channelStr.trim().equals("")) {
				message = new Message(UcCodeEnum.PARAM_ERROR);
				respMessage(message, response);
				return;
			} else {
				if (!channelStr.trim().equals("1")) {
					if (packageName == null || packageName.equals("") || channelStr == null || channelStr.equals("") || token == null || token.equals("") || account == null || account.equals("")) {
						message = new Message(UcCodeEnum.PARAM_ERROR);
						respMessage(message, response);
						return;
					}
					baseChanelConfig = SpringUtil.getBean(VerifyLoginService.class).verifyChannelLogin(packageName, Integer.parseInt(channelStr), account, token);
					if (baseChanelConfig == null) {
						message = new Message(UcCodeEnum.SDK_LOGIN_ERROR);
						respMessage(message, response);
						return;
					} else {
						account = baseChanelConfig.getParent_type() + "_" + account;
					}
				}

				String version = loginParams.getAppVersion() == null ? "" : loginParams.getAppVersion();
				String imodel = loginParams.getImodel() == null ? "" : loginParams.getImodel();
				String imei = loginParams.getImei() == null ? "" : loginParams.getImei();
				String cpu = loginParams.getCpu() == null ? "" : loginParams.getCpu();
				String idfa = loginParams.getIdfa() == null ? "" : loginParams.getIdfa();
				String resolution = loginParams.getResolution() == null ? "" : loginParams.getResolution();
				String deviceUuid = loginParams.getDeviceUuid() == null ? "" : loginParams.getDeviceUuid();
				String ip = IPUtils.getIpAddress(request);
				String versionFile = loginParams.getVersionFile() == null ? "" : loginParams.getVersionFile();

				logger.info("ServerHandler getServerList : channel {} ,account {} ,version {},imodel{},imei{},cpu{},idfa{},resolution{},deviceUuid{},ip{},versionFile:{}", Integer.parseInt(channelStr), account, version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, versionFile);
				ServerService serverService = SpringUtil.getBean(ServerService.class);

				boolean flag = baseChanelConfig == null ? false : baseChanelConfig.getIs_review() == 1;
				ServerList serverList = serverService.getServerList(account, Integer.parseInt(channelStr), version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, flag, packageName, versionFile);
				if (baseChanelConfig != null) {
					serverList.setTeamNum(baseChanelConfig.getTeamNum());
				} else {
					serverList.setTeamNum("D613uh9nKB93eMfwHubGuEa5i66rVe1i");
				}

				String str = JSONObject.toJSONString(serverList);
				logger.info("ServerHandler getServerList : ServerList {} ", str);

				message = new Message(UcCodeEnum.SUCCESS, str);
				respMessage(message, response);
				return;
			}
		} catch (Exception e) {
			message = new Message(UcCodeEnum.PARAM_ERROR);
			respMessage(message, response);
			logger.error(e.getMessage(), e);
		}
	}

	public void respMessage(Message message, HttpServletResponse response) throws IOException {
		String msg = JSONObject.toJSONString(message);
		System.out.println(msg);
		byte[] data = msg.getBytes(StandardCharsets.UTF_8);
		EncryptionAndDecryptionUtil.encryptCustom(data, EncryptionAndDecryptionUtil.getDefaultCustomEncryptionKeys());
		response.setContentType("text/html;charset=UTF-8");
		OutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			outputStream.write(data);
			outputStream.close();
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/account/verifyAccount.do", method = RequestMethod.POST)
	public Message VerifyAccount(int keyId, String token, int serverId, Integer channel) {
		try {
			logger.info("keyId {} , token {} ,serverId {}", keyId, token, serverId);
			AccountManager accountManager = SpringUtil.getBean(AccountManager.class);

			ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
			Server server = serverManager.getServerById(serverId);

			if (server == null) {
				return new Message(UcCodeEnum.SERVER_NOT_EXIST);
			}
			StaticVersion staticVersion = serverManager.getVersion(server.getVersion());

			Account account = accountManager.getByKey(keyId);
			if (account == null) {
				logger.info("account is null");
				return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
			}
			account.setLoginDate(new Date());
			accountManager.add(account);

			if (!token.equals(account.getToken())) {
				logger.info("account is null");
				return new Message(UcCodeEnum.TOKEN_IS_ERROR);
			}

			switch (server.getState()) {
				case ServerStatusConsts.SERVER_FLUENT:
					// do nothing
					break;
//			case ServerStatusConsts.SERVER_BLOCK:
//				// 没有角色 不能登录
//				if (!account.serverInfoList().contains(server.getServerId())) {
//					return new Message(UcCodeEnum.SERVER_BLOCK);
//				}
//				break;
				case ServerStatusConsts.SERVER_FULL:
					// do nothing
					break;
				case ServerStatusConsts.SERVER_MAINTAIN:
					// 未设置白名单 不能进
					if (server.getWhiteList() == null && server.getIpWhiteLists() == null) {
						return new Message(UcCodeEnum.SERVER_IS_MAINTAIN);
					} else {
						boolean inWhiteList = server.getWhiteList() != null && server.getWhiteList().contains(account.getKeyId()) || (server.getIpWhiteLists() != null && server.getIpWhiteLists().contains(account.getIp()));
						if (!inWhiteList) {
							return new Message(UcCodeEnum.SERVER_IS_MAINTAIN);
						}
					}
					break;
//			case ServerStatusConsts.SERVER_CLOSE:
//				// 白名单可进
//				if (server.getWhiteList() == null && server.getIpWhiteLists() == null) {
//					return new Message(UcCodeEnum.SERVER_CLOSE);
//				} else {
//					boolean inWhiteList = server.getWhiteList() != null && server.getWhiteList().contains(account.getKeyId()) || (server.getIpWhiteLists() != null && server.getIpWhiteLists().contains(account.getIp()));
//					if (!inWhiteList) {
//						return new Message(UcCodeEnum.SERVER_CLOSE);
//					}
//				}
//				break;
//			case ServerStatusConsts.SERVER_REVIEW:
//				// 只有评审包能看到 直接进吧
//				break;
			}

			String data = JSONObject.toJSONString(account);
//			boolean iswhite = server.getWhiteList() != null && server.getWhiteList().contains(account.getKeyId()) || server.getIpWhiteLists() != null && server.getIpWhiteLists().contains(account.getIp());
//			if (iswhite) {
//				SpringUtil.getBean(AccountManager.class).recordRecentServer(account, serverId);
//
//				/**
//				 * 账号登录日志埋点
//				 */
//				LogHelper logHelper = SpringUtil.getBean(LogHelper.class);
//				logHelper.accountLoginLog(new AccountloginLog(String.valueOf(account.getChannel()), account.getVersionNo(), serverId, account.getKeyId(), account.getLoginDate(), account.getIp(), account.getDeviceNo(), account.getIdfa(), account.getImei(), account.getImodel(), account.getResolution(), account.getCpu()));
//				SpringUtil.getBean(AccountDao.class).updateAccount(account);
//				return new Message(UcCodeEnum.SUCCESS, data);
//			}
			// 账号是否已封禁
			if (!accountManager.isAccountCanLogin(keyId, serverId)) {
				return new Message(UcCodeEnum.ACCOUNT_ISFORIBD);
			}
			// 角色是否已封禁
			if (!accountManager.isCloseRole(keyId, serverId)) {
				return new Message(UcCodeEnum.ROLE_ISFORIBD);
			}
			// IP是否已封禁
			if (accountManager.isCloseIp(account.getIp())) {
				return new Message(UcCodeEnum.ROLE_ISFORIBD);
			}
			// UUID是否已封禁
			if (accountManager.isCloseUuid(account.getDeviceUuid())) {
				return new Message(UcCodeEnum.ROLE_ISFORIBD);
			}

			if (new Date().before(server.getOpenTime())) {
				return new Message(UcCodeEnum.SERVER_NOT_OPEN);
			}
			SpringUtil.getBean(AccountManager.class).recordRecentServer(account, serverId);
			long endTime = accountManager.getCloseSpeak(keyId, serverId);
			account.setCloseSpeakTime(endTime);
			SpringUtil.getBean(AccountDao.class).updateAccount(account);
//        /**
//         * 账号登录日志埋点
//         */
			LogHelper logHelper = SpringUtil.getBean(LogHelper.class);
			logHelper.accountLoginLog(new AccountloginLog(String.valueOf(account.getChannel()), account.getVersionNo(), serverId, account.getKeyId(), account.getLoginDate(), account.getIp(), account.getDeviceNo(), account.getIdfa(), account.getImei(), account.getImodel(), account.getResolution(), account.getCpu()));
			logger.info("data {}", data);
			if (staticVersion != null) {
				return new Message(UcCodeEnum.SUCCESS, staticVersion.getCur_version(), data);
			} else {
				return new Message(UcCodeEnum.SUCCESS, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 添加区服
	 *
	 * @param server
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/server/addServer.do", method = RequestMethod.POST)
	public Message addServer(String server) {
		try {
			/**
			 * 判断参数
			 */
			if (null == server || server.equals("")) {
				logger.error("ServerHandler addServer : server {},desc{}}", server, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}

			/**
			 * json转成对象
			 */
			Server serverInfo = JSON.parseObject(server, Server.class);
			ServerService serverService = SpringUtil.getBean(ServerService.class);
			int total = serverService.addServer(serverInfo);
			/**
			 * 插入失败
			 */
			if (total == 0) {
				logger.error("ServerHandler addServer : server {},desc{}}", server, UcCodeEnum.SYS_ERROR.getDesc());
				return new Message(UcCodeEnum.SYS_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ServerHandler addServer : server {},desc{}}", server, UcCodeEnum.SYS_ERROR.getDesc());
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS);
	}

	/**
	 * 修改区服信息
	 *
	 * @param server
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/server/updateServer.do", method = RequestMethod.POST)
	public Message updateServer(String server) {
		try {
			/**
			 * 判断参数
			 */
			if (null == server || server.equals("")) {
				logger.error("ServerHandler updateServer : server {},desc{}}", server, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}

			/**
			 * json转成对象
			 */
			Server serverInfo = JSON.parseObject(server, Server.class);
			ServerService serverService = SpringUtil.getBean(ServerService.class);
			int total = serverService.updateServer(serverInfo);

			/**
			 * 修改失败
			 */
			if (total == 0) {
				logger.error("ServerHandler updateServer : server {},desc{}}", server, UcCodeEnum.SERVER_NOT_EXIST.getDesc());
				return new Message(UcCodeEnum.SERVER_NOT_EXIST);
			}
			serverService.postUpdateServer(serverInfo);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ServerHandler updateServer : server {},desc{}", server, UcCodeEnum.SYS_ERROR.getDesc());
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS);
	}

	/**
	 * 查询区服相关信息
	 *
	 * @param serverId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/server/findServer.do", method = RequestMethod.POST)
	public Message findServer(String serverId) {
		Server serveInfo = null;
		try {
			/**
			 * 判断参数
			 */
			if (null == serverId || serverId.equals("")) {
				logger.error("ServerHandler findServer : server {},desc{}}", serverId, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}
			ServerService serverService = SpringUtil.getBean(ServerService.class);
			serveInfo = serverService.getServer(Integer.parseInt(serverId));
			if (serveInfo == null) {
				logger.error("ServerHandler updateServer : server {},desc{}}", serverId, UcCodeEnum.SERVER_NOT_EXIST.getDesc());
				return new Message(UcCodeEnum.SERVER_NOT_EXIST);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ServerHandler updateServer : server {},desc{}", serverId, UcCodeEnum.SYS_ERROR.getDesc());
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS, JSON.toJSONString(serveInfo));
	}

	@ResponseBody
	@RequestMapping(value = "/server/findAllServer", method = RequestMethod.POST)
	public Message findAllServer() {
		Map<Integer, Server> allServer = null;
		try {
			ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
			allServer = serverManager.getAllServer();
			if (allServer == null) {
				logger.error("ServerHandler findAllServer is null");
				return new Message(UcCodeEnum.SERVER_NOT_EXIST);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ServerHandler updateServer :desc{}", UcCodeEnum.SYS_ERROR.getDesc());
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS, JSON.toJSONString(allServer));
	}

	/**
	 * 获取所有区服
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/channel/findAllChannel", method = RequestMethod.POST)
	public Message findAllChannel() {
		List<SChannelConfig> selectAllChannelConfig = null;

		try {
			ChannelConfigManager channelConfigManager = SpringUtil.getBean(ChannelConfigManager.class);
			selectAllChannelConfig = channelConfigManager.selectAllChannelConfig();
			if (selectAllChannelConfig == null) {
				logger.error("ServerHandler findAllChannel is null");
				return new Message(UcCodeEnum.SERVER_NOT_EXIST);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ServerHandler findAllChannel :desc{}", UcCodeEnum.SYS_ERROR.getDesc());
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS, JSON.toJSONString(selectAllChannelConfig));
	}

	@RequestMapping(value = "/closeGame")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String validateCode = request.getParameter("validateCode");
		boolean success = validateCode != null && validateCode.equals(Md5Util.string2MD5(Md5Util.KEY));
		if (success) {
			response.getOutputStream().write("OK".getBytes());
			response.flushBuffer();

			logger.info("close game server start ....");
			logger.info("close game server end ");
			Thread.sleep(1000);
			// SpringContextLoader.getContext().destroy();
			System.exit(0);
			return;
		} else {
			logger.warn("invalidate code");
			response.getOutputStream().write("invalidate code".getBytes());
			return;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/account/updateServerInfos.do", method = RequestMethod.POST)
	public static Message updateServerInfos(int keyId, String token, int serverId) {
		AccountManager accountManager = SpringUtil.getBean(AccountManager.class);

		ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
		Server server = serverManager.getServerById(serverId);

		if (server == null) {
			return new Message(UcCodeEnum.SERVER_NOT_EXIST);
		}

		Account account = accountManager.getByKey(keyId);
		if (account == null) {
			return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
		}
		String serverInfos = account.getServerInfos();
		JSONArray array = new JSONArray();
		if (!StringUtil.isNullOrEmpty(serverInfos)) {
			array = JSONArray.parseArray(serverInfos);
		}
		array.add(serverId);
		account.setServerInfos(array.toJSONString());
		return new Message(UcCodeEnum.SUCCESS);
	}

	public static void main(String[] args) {
		System.out.println(JSONObject.toJSONString(new LoginParams()));
	}

	@ResponseBody
	@RequestMapping(value = "/initChannelConfig.do")
	public Message doInitChannelConfig(HttpServletRequest request, HttpServletResponse response) {
		try {
			ChannelConfigManager manager = SpringUtil.getBean(ChannelConfigManager.class);
			manager.init();
			return new Message(UcCodeEnum.SUCCESS);
		} catch (Exception e) {
			return new Message(UcCodeEnum.SYS_ERROR);
		}
	}

	/**
	 * 修改区服信息
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/server/updateVersion.do", method = RequestMethod.POST)
	public Message updateVersion() {
		try {
			ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
			serverManager.init();
		} catch (Exception e) {
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS);
	}

	/**
	 * @Description 创建角色, 登录服作记录
	 * @Date 2021/3/10 15:15
	 * @Param [accountKey, serverId, country]
	 * @Return
	 **/
	@ResponseBody
	@RequestMapping(value = "/account/CreateRoleRq.do", method = RequestMethod.POST)
	public Message VerifyAccount(int accountKey, int serverId, int country) {
		logger.info("{},{},{}", accountKey, serverId, country);
		AccountManager accountManager = SpringUtil.getBean(AccountManager.class);
		Account account = accountManager.getByKey(accountKey);
		if (account != null) {
			MergeServerManager mergeServerManager = SpringUtil.getBean(MergeServerManager.class);
			boolean isOk = mergeServerManager.addPlayerExist(accountKey, serverId, country);
			if (isOk) {
				return new Message(UcCodeEnum.SUCCESS);
			}
		}
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/player/refreshMergeServer.do", method = RequestMethod.POST)
	public Message refreshMergeServer() {
		SpringUtil.getBean(MergeServerManager.class).init();
		return new Message(UcCodeEnum.SUCCESS);
	}

	@ResponseBody
	@RequestMapping(value = "/account/robotCreate.do", method = RequestMethod.POST)
	public Message robotCreate(String channel, String account) throws Exception {

		if (!"1".equals(channel)) {
			return new Message(UcCodeEnum.PARAM_ERROR);
		}

		if (account == null || "".equals(account)) {
			return new Message(UcCodeEnum.PARAM_ERROR);
		}

		String version = "1.0.001";
		String imodel = "";
		String imei = "";
		String cpu = "";
		String idfa = "";
		String resolution = "720_1280";
		String deviceUuid = account;
		String ip = "127.0.0.1";
		String versionFile = "version.php";
		String packageName = "com.package.acd1";

		ServerService serverService = SpringUtil.getBean(ServerService.class);

		ServerList serverList = serverService.getServerList(account, Integer.valueOf(channel), version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, false, packageName, versionFile);
		String str = JSONObject.toJSONString(serverList);

		return new Message(UcCodeEnum.SUCCESS, str);
	}

	@ResponseBody
	@RequestMapping(value = "/server/updateClientPackageConfig.do", method = RequestMethod.POST)
	public Message updateClientPackageConfig() {
		SpringUtil.getBean(PackageConfigManager.class).init();
		return new Message(UcCodeEnum.SUCCESS);
	}


	@ResponseBody
	@RequestMapping(value = "/server/clientPackageConfig.do", method = RequestMethod.POST)
	public Message clientPackageConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Message message = null;
		ServletInputStream inputStream = request.getInputStream();
		if (null == inputStream) {
			message = new Message(UcCodeEnum.PARAM_ERROR);
			respMessage(message, response);
		}

		try {
			String parmas = IOUtils.toString(inputStream);
			String string = new String(Base64.decodeFast(parmas), "utf-8");

			VerifyModeParams verifyModeParams = JSON.parseObject(string, VerifyModeParams.class);
			String packageName = verifyModeParams.getPackageName();

			if (null == packageName) {
				message = new Message(UcCodeEnum.PARAM_ERROR);
				respMessage(message, response);
			}

			PackageConfigManager packageConfigManager = SpringUtil.getBean(PackageConfigManager.class);
			int value = packageConfigManager.getVerifyMode(packageName);

//			StaticVerifyMode verifyMode = new StaticVerifyMode();
//			verifyMode.setVerifyMode(value);
//			String str = JSONObject.toJSONString(verifyMode);
//			message = new Message(UcCodeEnum.SUCCESS, str);

			message = new Message(UcCodeEnum.SUCCESS, String.valueOf(value));

			respMessage(message, response);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			message = new Message(UcCodeEnum.PARAM_ERROR);
			respMessage(message, response);
		}
		return new Message(UcCodeEnum.SUCCESS);
	}

}
