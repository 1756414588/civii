package com.game.server;


import com.game.network.GateNetContext;
import com.game.action.MessagePool;
import com.game.define.App;
import com.game.network.MessageDecoder;
import com.game.network.MessageEncoder;
import com.game.network.MessageHandler;
import com.game.network.RemoteNet;
import com.game.util.LogHelper;
import com.game.util.XProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author 陈奎
 * @Description 网关服务
 * @Date 2022/9/9 11:30
 **/

public class GateServer extends AbsServer {

	public static Logger START_LOGGER = LoggerFactory.getLogger("START");

	public static GateServer inst = new GateServer();

	public static GateServer getInst() {
		return inst;
	}

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	public GlobalTrafficShapingHandler trafficShapingHandler;
	ServerBootstrap bootstrap;
	private XProperties properties;

	// 定期服务
	private TimerServer timerServer;

	private int port;
	private String centerIp;
	private int centerPort;


	public volatile boolean ready = false;

	public GateServer() {
		super(App.GATE.getName());
	}

	public void start() {
		try {
			this.loadConf();
			this.init();
			this.registerPbFile();
			this.startBefore();
			this.listenPort();
			this.startTimerServer();
			this.startAfter();
			START_LOGGER.info("GATE SERVER START SUCCESS");
			START_LOGGER.info("listen on port:{} SUCCESS", port);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
			System.exit(1);
		}
	}

	@Override
	protected void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	private void loadConf() {
		properties = new XProperties();
		if (properties.loadFile("gate.properties")) {

		} else {
			properties = new XProperties();
			URL url = getClass().getClassLoader().getResource("server.properties");
			File file = new File(url.getFile());
			System.out.println(file.getPath());
			if (!properties.loadFile(file)) {
				throw new RuntimeException();
			}
		}
	}

	protected void init() {
		this.port = properties.getInteger("gate.port", 9501);
		this.centerIp = properties.getString("center.ip", "127.0.0.1");
		this.centerPort = properties.getInteger("center.port", 7777);
	}


	public void listenPort() {
		// 定义两个工作线程 bossGroup workerGroup 用于管理channel连接
		// 负责tcp客户端的连接请求
		bossGroup = new NioEventLoopGroup(1);
		// 真正负责IO读写的现场组
		workerGroup = new NioEventLoopGroup();
		bootstrap = new ServerBootstrap();
		trafficShapingHandler = new GlobalTrafficShapingHandler(workerGroup, 5000L);
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		// BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		// 通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(trafficShapingHandler);
				ch.pipeline().addLast(new IdleStateHandler(120, 0, 0, TimeUnit.SECONDS));
				ch.pipeline().addLast(new MessageDecoder());
				ch.pipeline().addLast(new MessageEncoder());
				ch.pipeline().addLast(new MessageHandler());
			}
		});

		// Netty4使用对象池，重用缓冲区
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		// 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);

		ChannelFuture f;
		try {
			// 绑定端口，同步等待成功
			LogHelper.CHANNEL_LOGGER.info("start gate success port:{}", port);
			f = bootstrap.bind(port).sync();
			// 等待服务端监听端口关闭
//			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			LogHelper.ERROR_LOGGER.error("ConnectServer", e);
		}
	}

	/**
	 * 开启之前
	 */
	public void startBefore() {
		MessagePool.getInst().init();
	}

	public void startAfter() {
		GateNetContext netHandler = new GateNetContext(App.GATE, centerIp, centerPort, "10101");
		RemoteNet remoteNet = new RemoteNet(netHandler, netHandler);
		remoteNet.startConnect();
		this.net = remoteNet;
	}


	/**
	 * 启动定时服务
	 */
	public void startTimerServer() {
		timerServer = new TimerServer("TimerServer");
		timerServer.registerAppTimer();
	}


	@Override
	public String getGameType() {
		return App.GATE.getName();
	}


}


