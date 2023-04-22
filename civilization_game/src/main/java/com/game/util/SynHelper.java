package com.game.util;

import com.game.define.App;
import com.game.domain.Player;
import com.game.network.INet;
import com.game.network.NetManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb;
import com.game.server.GameServer;
import com.google.protobuf.GeneratedMessage;
import java.util.List;

public class SynHelper {

	public static <T> void synMsgToPlayer(Player player, int cmd, GeneratedMessage.GeneratedExtension<BasePb.Base, T> ext, T msg) {
		if (player != null && player.isLogin && player.getChannelId() != -1) {
			BasePb.Base.Builder baseMsg = PbHelper.createSynBase(cmd, ext, msg);
			GameServer.getInstance().sendMsgToPlayer(player, baseMsg);
		}
	}

	/**
	 * 广播消息
	 *
	 * @param cmd
	 * @param ext
	 * @param msg
	 * @param <T>
	 */
	public static <T> void broadcast(int cmd, GeneratedMessage.GeneratedExtension<BasePb.Base, T> ext, T msg) {
		BasePb.Base.Builder base = PbHelper.createSynBase(cmd, ext, msg);
		Packet packet = PacketCreator.create(base.build());

		// 将广播消息同步给网关,由网关同步给用户
		List<INet> gateNetList = NetManager.getInst().getByApp(App.GATE);
		if (gateNetList != null) {
			for (INet net : gateNetList) {
				net.send(packet);
			}
		}
	}

}
