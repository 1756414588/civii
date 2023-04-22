package com.game.server;

import com.game.Loading;
import com.game.domain.Player;
import com.game.log.consumer.LoggerConsumer;
import com.game.manager.*;
import com.game.message.pool.MessagePool;
import com.game.network.INet;
import com.game.network.NetManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.server.netserver.MessageFilter;
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
import lombok.Setter;

/**
 *
 * @Description 游戏服务器
 * @Date 2022/9/9 11:30
 **/
@Setter
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
			if (!MessageFilter.isFilterPrint(packet.getCmd())) {
				LogHelper.CHANNEL_LOGGER.info("sendToClient channelId:{} playerId:{} cmd:{}", packet.getChannelId(), packet.getRoleId(), packet.getCmd());
			}
		}
	}

	@Override
	public void run() {
		super.run();

		startOn();
		try {

			currentDay = TimeHelper.getCurrentDay();

			// 加载pb
			this.registerPbFile();

			//TODO 优化此处待优化
			ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
			mainLogicServer = new LogicServer(serverManager.getServer().getServerName(), 500, 100);

			// 数据存储服务
			dataFacedeServer = new DataFacedeServer();
			dataFacedeServer.start();

			// 加载服务
			Loading.getInst().load();

			// 网络服务
			hostServer = new NetServer();
			startServerThread(hostServer);
			startServerThread(mainLogicServer);

			// 兼容世界活动
			SpringUtil.getBean(WorldActPlanService.class).openWorldAct();

		} catch (Exception e) {
			LogHelper.GAME_LOGGER.error(e.getMessage(), e);
			startInterrupted();
			System.exit(-1);
		}
		LogHelper.GAME_LOGGER.info("GameServer " + SpringUtil.getBean(ServerManager.class).getServer().getServerName() + " Started SUCCESS");

		startComplete();
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


			mainLogicServer.shutDownGraceful();
			LogHelper.GAME_LOGGER.info("【业务线程】关闭..");

			// 优雅关闭数据服务
			dataFacedeServer.shutDownGraceful();
			LogHelper.GAME_LOGGER.info("【数据服务】关闭..");

			//loggerConsumer.stop();

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
		thread.setUncaughtExceptionHandler((t, e) -> {
			LogHelper.ERROR_LOGGER.error("uncaughtException", e);
			startInterrupted();
			System.exit(-1);
		});
		thread.start();
	}

	/**
	 * 启动开始
	 */
	private void startOn() {
		try {

			LogHelper.GAME_LOGGER.info("GAME 开始启动");

			String os = System.getProperty("os.name");
			if (os != null && os.toLowerCase().startsWith("linux")) {
				String[] command = {"/bin/sh", "-c", "echo 0 > pid"};
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
	 * 启动中止
	 */
	private void startInterrupted() {
		try {

			LogHelper.GAME_LOGGER.info("GAME 启动失败");

			String os = System.getProperty("os.name");
			if (os != null && os.toLowerCase().startsWith("linux")) {
				String[] command = {"/bin/sh", "-c", "echo 100 > pid"};
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
	 * 启动完成
	 */
	private void startComplete() {
		try {

			LogHelper.GAME_LOGGER.info("GAME 开始启动");

			String os = System.getProperty("os.name");
			if (os != null && os.toLowerCase().startsWith("linux")) {
				String[] command = {"/bin/sh", "-c", "echo 200 > pid",};
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


}
