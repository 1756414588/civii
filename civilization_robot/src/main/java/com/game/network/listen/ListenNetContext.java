package com.game.network.listen;

import com.game.cache.ConfigCache;
import com.game.define.App;
import com.game.manager.MessageManager;
import com.game.manager.RobotManager;
import com.game.manager.RobotNetManager;
import com.game.message.ListenPool;
import com.game.network.ChannelUtil;
import com.game.network.INetContext;
import com.game.network.INet;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.ListenEventRq;
import com.game.register.PBFile;
import com.game.server.AppPropertes;
import com.game.network.IPacketHandler;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;

/**
 * 监听玩家的连接处理器以及消息处理器
 */
public class ListenNetContext implements INetContext, IPacketHandler {

	private App app;
	private String host;
	private int port;
	private String identity;

	@Setter
	private INet net;

	public ListenNetContext(App app, String host, int port, String identity) {
		this.app = app;
		this.host = host;
		this.port = port;
		this.identity = identity;
	}

	@Override
	public void onSucess(ChannelHandlerContext ctx) {
		ConfigCache robotConfigData = SpringUtil.getBean(ConfigCache.class);

		ListenEventRq.Builder req = ListenEventRq.newBuilder();
		req.setEventId(App.ROBOT.getId());
		req.addEventparam(robotConfigData.getValueByKey("listen_user_id"));
		Base.Builder base = BasePbHelper.createRqBase(ListenEventRq.EXT_FIELD_NUMBER, ListenEventRq.ext, req.build());
		Packet packet = new Packet();
		packet.setCmd(ListenEventRq.EXT_FIELD_NUMBER);
		packet.setBytes(base.build().toByteArray());
		net.send(packet);
		LogHelper.CHANNEL_LOGGER.info("Robot服务器连接网关服务器成功,请求监听玩家");

		// 机器人连接管理
		RobotNetManager robotNetManager = SpringUtil.getBean(RobotNetManager.class);
		robotNetManager.listen(net, ctx);
	}

	@Override
	public void doPacket(ChannelHandlerContext ctx, Packet packet) {
		try {
			ListenPool listenPool = SpringUtil.getBean(ListenPool.class);

			Base base = Base.parseFrom(packet.getBytes(), PBFile.registry);
			int cmd = base.getCommand();
			if (listenPool.filter(cmd)) {// 过滤消息
				return;
			}

			AppPropertes appPropertes = SpringUtil.getBean(AppPropertes.class);

			// 开启自主机器人,除了应用间的信号,不处理其他的监听消息
			if (appPropertes.isRobotAuto() && !listenPool.isAppSignal(cmd)) {
				return;
			}

			// 录入玩家的请求指令[录入消息]
			if (appPropertes.isRecordCmd()) {
				SpringUtil.getBean(MessageManager.class).recordCmd(packet);
				return;
			}

			if (listenPool.pools.containsKey(cmd)) {// 监听消息如果注册，则在本机上处理
				Base msg = Base.parseFrom(packet.getBytes(), PBFile.registry);
				listenPool.handler(ctx, cmd, msg);
			} else {// 未注册的监听消息,则直接转发
				SpringUtil.getBean(RobotManager.class).broadcast(packet);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnect(ChannelHandlerContext ctx) {
		AppPropertes appProperty = SpringUtil.getBean(AppPropertes.class);
		appProperty.ready = false;

		// 移除连接管理
		RobotNetManager robotNetManager = SpringUtil.getBean(RobotNetManager.class);
		robotNetManager.remove(ChannelUtil.getNetId(ctx));
	}

	@Override
	public String host() {
		return host;
	}

	@Override
	public int port() {
		return port;
	}

	@Override
	public App app() {
		return app;
	}

}
