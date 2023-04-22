package com.game.message.handler.register;

import com.game.define.App;
import com.game.message.handler.ClientHandler;
import com.game.network.ChannelAttr;
import com.game.network.ChannelUtil;
import com.game.network.Net;
import com.game.network.NetManager;
import com.game.pb.InnerPb.RegisterRq;
import com.game.pb.InnerPb.RegisterRs;
import com.game.util.LogHelper;
import java.util.UUID;


public class RegisterHandler extends ClientHandler {


	@Override
	public void action() {
		RegisterRq req = msg.getExtension(RegisterRq.ext);
		int appId = req.getAppId();
		if (appId == App.GATE.getId()) {
			registerGate(req);
		}
	}


	private void registerGate(RegisterRq req) {
		Net net = ChannelUtil.getAttribute(ctx, ChannelAttr.NET);
		net.setApp(App.GATE);

		String serverId = req.getServerId();
		if (null == serverId || "".equals(serverId)) {
			serverId = UUID.randomUUID().toString();
		}

		net.setId(serverId);
		ChannelUtil.setAttribute(ctx, ChannelAttr.NET_SERVER_ID, serverId);

		// 将消息
		NetManager.getInst().put(serverId, net);
		NetManager.getInst().putAppNet(App.GATE, net);

		// 返回消息
		RegisterRs.Builder builder = RegisterRs.newBuilder();
		builder.setState(1);
		builder.setServerId(serverId);
		sendMsgToPlayer(RegisterRs.ext, builder.build());

		LogHelper.CHANNEL_LOGGER.info("registerGate gateId:{}", serverId);
	}
}
