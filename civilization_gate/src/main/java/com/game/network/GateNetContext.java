package com.game.network;

import com.game.action.MessagePool;
import com.game.define.App;
import com.game.domain.UserClient;
import com.game.manager.UserClientManager;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.RegisterRq;
import com.game.pb.InnerPb.RegisterRs;
import com.game.register.PBFile;
import com.game.server.GateServer;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @Description gate服连接游戏服的连接处理器
 * @Date 2022/9/9 11:30
 **/

public class GateNetContext implements INetContext, IPacketHandler {

	private App app;
	private String host;
	private int port;
	private String identity;

	public GateNetContext(App app, String host, int port, String identity) {
		this.app = app;
		this.host = host;
		this.port = port;
		this.identity = identity;
	}

	@Override
	public void onSucess(ChannelHandlerContext ctx) {
		RemoteNet remoteNet = (RemoteNet) GateServer.getInst().getNet();

		// 注册packet
		RegisterRq.Builder req = RegisterRq.newBuilder();
		req.setAppId(App.GATE.getId());
		req.setServerName(App.GATE.getName());
		req.setServerId("");
		Base.Builder base = BasePbHelper.createRqBase(RegisterRq.EXT_FIELD_NUMBER, RegisterRq.ext, req.build());
		Packet packet = new Packet();
		packet.setCmd(RegisterRq.EXT_FIELD_NUMBER);
		packet.setBytes(base.build().toByteArray());

		// 开始注册
		remoteNet.send(packet, e -> {
			// 注册返回消息
			Packet response = (Packet) e;
			try {
				Base r = Base.parseFrom(response.getBytes(), PBFile.registry);
				RegisterRs registerRs = r.getExtension(RegisterRs.ext);
				if (registerRs.getState() == 1) {
					remoteNet.setId(registerRs.getServerId());
					GateServer.getInst().ready = true;
					LogHelper.CHANNEL_LOGGER.info("网关服注册成功 id:{}", registerRs.getServerId());
				}
			} catch (InvalidProtocolBufferException ex) {
				ex.printStackTrace();
			}
		});
	}

	@Override
	public void doPacket(ChannelHandlerContext ctx, Packet packet) {
		try {
			if (MessagePool.getInst().handler(ctx, packet)) {
				return;
			}
			UserClient client = UserClientManager.getInst().getChannel(packet.getChannelId());
			if (client != null) {
				client.sendPacket(packet);
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public void onDisconnect(ChannelHandlerContext ctx) {
		GateServer.getInst().ready = false;
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
