package com.game.manager;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.UcCodeEnum;
import com.game.dao.p.BootstrapDao;
import com.game.domain.p.Bootstrap;
import com.game.service.UcHttpService;
import com.game.uc.Message;
import com.game.uc.Server;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 *
 * @date 2020/4/15 14:50
 * @description
 */
@Component
public class ServerManager {

	@Value("${serverId}")
	private int serverId;

	@Value("${accountServerUrl}")
	private String accountServerUrl;

	@Autowired
	private UcHttpService ucHttpService;

	@Value("${http.server.jetty.port}")
	private int httpPort;

	@Value("${net.server.port}")
	private int netPort;

	@Value("${payServerUrl}")
	private String payServerUrl;

	@Value("${jdbc.ini.url}")
	private String jdbcConfigUrl;


	//服务器信息
	private Server server;

	@Autowired
	private BootstrapDao bootstrapDao;

	private Bootstrap bootstrap;

	private Logger logger = LoggerFactory.getLogger(getClass());

//	@PostConstruct
//	public void initServer() {
//		try {
//			Message message = ucHttpService.getServer(serverId);
//			if (message.getCode() != UcCodeEnum.SUCCESS.getCode()) {
//				LogHelper.GAME_LOGGER.error("ServerManager initServer error msg {}", message.toString());
//				System.exit(-1);
//				return;
//			}
//			server = JSONObject.parseObject(message.getData(), Server.class);
//			if (server.getHttpPort() != httpPort) {
//				LogHelper.GAME_LOGGER.error("ServerManager http port error ucPort {},properties port {}", server.getPort(), httpPort);
//				System.exit(-1);
//				return;
//			}
//			//设置日志级别
//			//设置日志级别
////            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
////            if (loggerContext.getLogger("profile") != null) {
////                if (server.getServerType() == 0) {
////                    loggerContext.getLogger("profile").setLevel(Level.valueOf("DEBUG"));
////                } else {
////                    loggerContext.getLogger("profile").setLevel(Level.valueOf("ERROR"));
////                }
////            }
//			initBootStrap();
//			LogHelper.GAME_LOGGER.error("Server init Success : {}", server.toString());
//			LogHelper.GAME_LOGGER.error("database config url : {}", jdbcConfigUrl);
//		} catch (Exception e) {
//			logger.error("ServerManager initServer error  {}", e);
//			System.exit(-1);
//		}
//
//	}

	public void initBootStrap() {
		LogHelper.GAME_LOGGER.error("加载引导记录");
		bootstrap = bootstrapDao.selectBootstrap();
		if (bootstrap == null) {
			bootstrap = new Bootstrap(1);
			bootstrapDao.update(bootstrap);
		}
	}

	public void updateBootStrap(String model) {
		int time = TimeHelper.getCurrentDay();
		if ("user".equals(model)) {
			bootstrap.setUser(time);
		} else if ("activity".equals(model)) {
			bootstrap.setActivity(time);
		} else if ("courty".equals(model)) {
			bootstrap.setCourty(time);
		} else if ("world".equals(model)) {
			bootstrap.setWorld(time);
		}
		bootstrapDao.update(bootstrap);
	}


	public int getServerId() {
		return serverId;
	}

	public String getAccountServerUrl() {
		return accountServerUrl;
	}

	public Server getServer() {
		return server;
	}

	public void updateServer(Server server) {
		this.server = server;
	}

	public String getPayServerUrl() {
		return payServerUrl;
	}

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public int getNetPort() {
		return netPort;
	}

	public void setNetPort(int netPort) {
		this.netPort = netPort;
	}
}
