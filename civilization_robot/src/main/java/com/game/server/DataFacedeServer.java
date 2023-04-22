package com.game.server;

import com.game.define.DataFacede;
import com.game.server.datafacede.SaveRobotDataServer;
import com.game.server.thread.SaveServer;
import com.game.spring.SpringUtil;
import com.game.util.ClassUtil;
import com.game.util.LogHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author 陈奎
 * @Description 数据存储服务
 * @Date 2022/9/9 11:30
 **/

public class DataFacedeServer {

	private List<SaveServer> serverList = new ArrayList<>();

	public DataFacedeServer() {
		Package pack = SaveRobotDataServer.class.getPackage();
		Set<Class<?>> allClasses = ClassUtil.getClasses(pack);

		for (Class<?> clazz : allClasses) {
			DataFacede dataFacede = clazz.getAnnotation(DataFacede.class);
			if (dataFacede != null) {
				SaveServer executor = (SaveServer) SpringUtil.getBean(clazz);
				serverList.add(executor);
			}
		}
	}

	/**
	 * 启动
	 */
	public void start() {
		for (SaveServer server : serverList) {
			startServerThread(server);
			LogHelper.GAME_LOGGER.info("DataFacede {} START~~~~", server.getName());
		}
	}

	/**
	 * 优雅的关闭服务，数据全部落地才真正关闭服务
	 */
	public void shutDownGraceful() {
		try {
			for (SaveServer saveServer : serverList) {
				saveServer.setLogFlag();
				saveServer.saveAll();
				saveServer.stop();
			}

			int sleepTime = 0;
			while (!(sleepTime > 300000L || allSaveDone())) {
				Thread.sleep(60);
				sleepTime += 60;
			}

			// 打印信息
			for (SaveServer saveServer : serverList) {
				LogHelper.SAVE_LOGGER.error(saveServer.serverName() + " has done with save :" + saveServer.allSaveCount());
			}

		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("DataFacede stop：{}", e.getMessage(), e);
		}
	}


	/**
	 * 所有的都存档了
	 *
	 * @return
	 */
	public boolean allSaveDone() {
		for (SaveServer server : serverList) {
			if (!server.saveDone()) {
				return false;
			}
		}
		return true;
	}

	private void startServerThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setUncaughtExceptionHandler((t, e) -> LogHelper.ERROR_LOGGER.error("uncaughtException", e));
		thread.start();
	}


}
