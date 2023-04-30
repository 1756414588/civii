package com.game.servlet.server;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.ServerStatusConsts;
import com.game.constant.UcCodeEnum;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.manager.ActivityManager;
import com.game.manager.PlayerManager;
import com.game.manager.ServerManager;
import com.game.server.GameServer;
import com.game.server.datafacede.SaveActivityServer;
import com.game.service.AccountService;
import com.game.service.MijiService;
import com.game.uc.Message;
import com.game.uc.Server;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @date 2019/12/21 14:39
 * @description
 */
@RestController
public class GameServerServlet {

	public GameServerServlet() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(GameServerServlet.class);

	@RequestMapping(value = "/closeGame")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws Exception {

//		String validateCode = request.getParameter("validateCode");
//		boolean success = validateCode != null
//			&& validateCode.equals(Md5Util.string2MD5(Md5Util.KEY));
//
//		if (!success) {
//			response.getOutputStream().write("OK".getBytes());
//			response.flushBuffer();
//			return;
//		}

		LogHelper.GAME_LOGGER.info("close game server start ....");

		response.getOutputStream().write("OK".getBytes());
		response.flushBuffer();

		// 关闭服务器
//		GameServer.getInstance().stop();

		Thread.sleep(1000);
		//SpringContextLoader.getContext().destroy();
		System.exit(0);
	}

	@RequestMapping(value = "/server/updateServer.do")
	public Message updateServer(String server) {
		log.info("GameServerServlet update  server {}", server);
		Server serverInfo = JSONObject.parseObject(server, Server.class);
		ServerManager serverManager = SpringUtil.getBean(ServerManager.class);

		Server serverBefore = serverManager.getServer();
		serverManager.updateServer(serverInfo);

		//重新加载活动信息
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		MijiService mijiService = SpringUtil.getBean(MijiService.class);
		try {
			if (serverBefore.getActMold() != serverInfo.getActMold()) {
				LogHelper.CONFIG_LOGGER.info("修改moldId重新加载活动配置");
				SpringUtil.getBean(SaveActivityServer.class).saveAll();
//				GameServer.getInstance().saveActivityServer.saveAllActivity();
				staticActivityMgr.init();
				activityManager.init();

			}

			if (serverInfo.getOpenTime().getTime() != serverBefore.getOpenTime().getTime()) {
				LogHelper.CONFIG_LOGGER.info("修改开服时间重新加载活动配置");
				SpringUtil.getBean(SaveActivityServer.class).saveAll();
//				GameServer.getInstance().saveActivityServer.saveAllActivity();
				staticActivityMgr.init();
				activityManager.init();

				LogHelper.CONFIG_LOGGER.info("修改开服时间清理世界进程数据");
				mijiService.clearWorldTarget();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("加载活动异常 cause:{}", e.getMessage(), e);
		}

		AccountService accountService = SpringUtil.getBean(AccountService.class);
		if (serverInfo.getState() == ServerStatusConsts.SERVER_MAINTAIN) {
			PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
			for (Player player : playerManager.getPlayers().values()) {
				if (player.getChannelId() != -1) {
					accountService.synOffline(player, 2);
				}
			}
		}

		return new Message(UcCodeEnum.SUCCESS);
	}
}
