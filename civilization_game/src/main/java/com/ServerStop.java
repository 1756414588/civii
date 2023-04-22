package com;

import com.game.util.HttpUtil;
import com.game.util.LogHelper;
import com.game.util.XProperties;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class ServerStop {

	public static void main(String[] args) {
		LogHelper.GAME_LOGGER.info("close game server start ....");
		ServerStop serverStop = new ServerStop();

		// 读取配置文件
		XProperties properties = serverStop.loadConf();

		int port = properties.getInteger("http.server.jetty.port", 0);
		if (port == 0) {
			LogHelper.GAME_LOGGER.info("未读取到服务端端口");
			return;
		}
		// STOP 指令
		String url = "http://127.0.0.1:" + port + "/closeGame";

		HttpUtil.sendHttpPost(url, new HashMap<>());
		LogHelper.GAME_LOGGER.info("game server stop!!! ....");
	}

	private XProperties loadConf() {
		XProperties properties = new XProperties();
		if (properties.loadFile("gameServer.properties")) {

		} else {
			properties = new XProperties();
			URL url = getClass().getClassLoader().getResource("gameServer.properties");
			File file = new File(url.getFile());
//			System.out.println(file.getPath());
			if (!properties.loadFile(file)) {
				throw new RuntimeException();
			}
		}
		return properties;
	}

}
