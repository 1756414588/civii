package com.game.server;


import com.game.StartRobotApp;
import com.game.define.App;
import com.game.network.listen.ListenNetContext;
import com.game.network.listen.ListenNet;
import com.game.network.NetworkHandler;
import com.game.network.RobotDecoder;
import com.game.network.RobotEncoder;
import com.game.spring.SpringUtil;
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

/**
 * 监听服务器
 */
public class RobotServer extends AbsServer {

	public static RobotServer inst = new RobotServer();

	public static RobotServer getInst() {
		return inst;
	}

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	public GlobalTrafficShapingHandler trafficShapingHandler;
	ServerBootstrap bootstrap;

	private DataFacedeServer dataFacedeServer;

	private AppPropertes property;

	private boolean ready;

	public RobotServer() {
		super(App.GATE.getName());
	}


	@Override
	public void run() {
		super.run();
		this.start();
	}

	public void start() {
		this.loadConf();
		this.startBefore();
		this.loadData();
		this.startServer();
		this.listenPort();
		this.startAfter();
		this.registerCorn();
	}

	@Override
	protected void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		ExecServer.getInst().shutDownGraceful();
		dataFacedeServer.shutDownGraceful();
	}

	private void loadConf() {
		try {
			XProperties properties = new XProperties();
			if (properties.loadFile("gate.properties")) {

			} else {
				properties = new XProperties();
				URL url = getClass().getClassLoader().getResource("gameServer.properties");
				File file = new File(url.getFile());
				System.out.println(file.getPath());
				if (!properties.loadFile(file)) {
					throw new RuntimeException();
				}
			}

			this.property = SpringUtil.getBean(AppPropertes.class);
			property.readConfig(properties);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
			System.exit(0);
		}
	}


	/**
	 * 开启之前
	 */
	public void startBefore() {
		this.registerPbFile();
	}


	/**
	 * 加载数据
	 */
	public void loadData() {
		AppInit.load(StartRobotApp.class.getPackage());

	}

	/**
	 * 启动内部服务
	 */
	private void startServer() {
		ExecServer.getInst().init();
		dataFacedeServer = new DataFacedeServer();
		dataFacedeServer.start();

		TimerServer.getInst().registerAppTimer();
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
				// TODO Auto-generated method stub
				ch.pipeline().addLast(new IdleStateHandler(600, 0, 0));
//				ch.pipeline().addLast(new HeartbeatHandler());
				ch.pipeline().addLast(new RobotDecoder());
				ch.pipeline().addLast(new RobotEncoder());
				ch.pipeline().addLast(new NetworkHandler());
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
			LogHelper.CHANNEL_LOGGER.info("start robot success {}", property.getPort());
			f = bootstrap.bind(property.getPort()).sync();
			// 等待服务端监听端口关闭
//			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			LogHelper.ERROR_LOGGER.error("ConnectServer", e);
		}
	}


	public void startAfter() {
		// 创建连接,监听玩家行为
		ListenNetContext listenConnector = new ListenNetContext(App.GATE, property.getGateIp(), property.getGatePort(), "10101");
		ListenNet listenNet = new ListenNet();
		listenNet.setConnect(listenConnector);
		listenNet.setPacketHandler(listenConnector);
		listenConnector.setNet(listenNet);
		listenNet.startConnect();
		this.net = listenNet;
	}

	public void registerCorn() {
	}

	@Override
	public String getGameType() {
		return App.GATE.getName();
	}

	public void setReady(boolean ready){
		this.ready = ready;
	}

	public boolean isReady() {
		return ready;
	}
}


