package com.game.server;

import com.game.domain.Player;
import com.game.log.consumer.LoggerConsumer;
import com.game.manager.*;
import com.game.message.pool.MessagePool;
import com.game.network.INet;
import com.game.network.NetManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.season.ModuleMgr;
import com.game.season.SeasonManager;
import com.game.server.netserver.NetServer;
import com.game.service.WorldActPlanService;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Description 游戏服务器
 * @Date 2022/9/9 11:30
 **/

public class GameServer extends AbsServer {

	public NetServer hostServer;
	/**
	 * 所有的线程都是放在这个主线程里面运行
	 */
	public LogicServer mainLogicServer;

	// 数据存储服务
	public DataFacedeServer dataFacedeServer;

	public LoggerConsumer loggerConsumer;
	/**
	 * 当天日期 转点重新设置
	 */
	public int currentDay;

	public MessagePool messagePool = new MessagePool();

	public ConcurrentHashMap<Long, ChannelHandlerContext> userChannels = new ConcurrentHashMap<Long, ChannelHandlerContext>();

	private GameServer() {
		super("GameServer");
	}

	private static GameServer gameServer = new GameServer();

	public static GameServer getInstance() {
		return gameServer;
	}

	public void sendMsgToPlayer(Player player, Base.Builder msg) {
		if (player.getGateId() == null) {
			return;
		}
		Packet packet = PacketCreator.create(msg.build(), player.getRoleId(), player.getChannelId());
		INet net = NetManager.getInst().get(player.getGateId());
		if (net != null) {
			net.send(packet);
		}
	}

	@Override
	public void run() {
		super.run();
		currentDay = TimeHelper.getCurrentDay();
		PlayerManager playerDataManager = SpringUtil.getBean(PlayerManager.class);
		try {
			this.registerPbFile();
			SpringUtil.getBean(BattleManager.class).init();
			ModuleMgr.init();
			playerDataManager.init();
			SpringUtil.getBean(ActivityManager.class).init();
			SpringUtil.getBean(HeroManager.class).init();

		} catch (Exception e) {
			LogHelper.GAME_LOGGER.error(e.getMessage(), e);
			System.exit(-1);
		}
		loggerConsumer = SpringUtil.getBean(LoggerConsumer.class);
		//loggerConsumer.init();

		hostServer = new NetServer();
		mainLogicServer = new LogicServer(SpringUtil.getBean(ServerManager.class).getServer().getServerName(), 500, 100);

		try {
			startServerThread(hostServer);
			startServerThread(mainLogicServer);
			dataFacedeServer = new DataFacedeServer();

			// 兼容世界活动
			SpringUtil.getBean(WorldActPlanService.class).openWorldAct();
			SpringUtil.getBean(BroodWarManager.class).init();
			SpringUtil.getBean(ZergManager.class).init();
			SpringUtil.getBean(BroodWarManager.class).initBroodWar();
			SpringUtil.getBean(ActManoeuvreManager.class).init();
			// 合服后处理数据
			SpringUtil.getBean(WorldManager.class).iniMergeServerPlayer();
			SpringUtil.getBean(PublicDataManager.class).ini();
			SpringUtil.getBean(SeasonManager.class).init();

		} catch (Exception e) {
			LogHelper.GAME_LOGGER.error(e.getMessage(), e);
			System.exit(-1);
		}
		LogHelper.GAME_LOGGER.error("GameServer " + SpringUtil.getBean(ServerManager.class).getServer().getServerName() + " Started SUCCESS");

		try {
			// 执行run
			String os = System.getProperty("os.name");
			if (os != null && os.toLowerCase().startsWith("linux")) {
				String[] command = {"/bin/sh", "-c", "rm -rf c_pid"};
				Runtime.getRuntime().exec(command);
			} else if (os != null && os.toLowerCase().startsWith("windows")) {
				System.out.println("当前为window系统");
			} else {// 其他系统
				System.out.println("当前为其他系统");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Overriding: stop
	 *
	 * @see AbsServer#stop()
	 */
	@Override
	public void stop() {
		LogHelper.GAME_LOGGER.info("GAME SERVER STOP..");
		hostServer.stop();
		try {

			// 优雅关闭任务执行器
//			ExecServer.getInst().shutDownGraceful();
//			LogHelper.GAME_LOGGER.info("【任务执行器】关闭..");

			mainLogicServer.shutDownGraceful();
			LogHelper.GAME_LOGGER.info("【业务线程】关闭..");

			// 优雅关闭数据服务
			dataFacedeServer.shutDownGraceful();
			LogHelper.GAME_LOGGER.info("【数据服务】关闭..");

			loggerConsumer.stop();

			URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
			String runname = ManagementFactory.getRuntimeMXBean().getName();
			String pid = runname.substring(0, runname.indexOf("@"));
			if (dataFacedeServer.allSaveDone()) {
				LogHelper.SAVE_LOGGER.error("GameServer-->" + location + "|" + runname + "|" + pid + "|" + "all saved!");
			} else {
				LogHelper.SAVE_LOGGER.error("GameServer-->" + location + "|" + runname + "|" + pid + "|" + "part saved!");
			}

		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Overriding: getGameType
	 *
	 * @return
	 * @see AbsServer#getGameType()
	 */
	@Override
	public String getGameType() {
		return "game";
	}


	private void startServerThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setUncaughtExceptionHandler((t, e) -> LogHelper.ERROR_LOGGER.error("uncaughtException", e));
		thread.start();
	}

}
