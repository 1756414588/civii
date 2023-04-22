package com.game.server.work;


import com.game.message.handler.ClientHandler;
import com.game.network.ChannelUtil;
import com.game.packet.Packet;
import com.game.server.GameServer;
import com.game.server.ITask;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

public class MessageWork implements ITask {

	private ChannelHandlerContext ctx;
	private Packet packet;

	public MessageWork(ChannelHandlerContext ctx, Packet packet) {
		this.ctx = ctx;
		this.packet = packet;
	}

	public long getPlayerId() {
		return this.packet.getRoleId();
	}

	public int getCmd() {
		return packet.getCmd();
	}

	@Override
	public void run() {
		int cmd = 0;
		ClientHandler handler = null;
		try {
			GameServer gameServer = GameServer.getInstance();
			cmd = packet.getCmd();
			handler = gameServer.messagePool.getClientHandler(cmd);
			if (handler == null) {
				return;
			}

			handler.setCtx(ctx);
			handler.setPacket(packet);
			handler.setRoleId(packet.getRoleId());

			long start = System.currentTimeMillis();
			handler.action();
			long end = System.currentTimeMillis();
			long timeCost = end - start;
			if (timeCost > 5) {
				String className = handler.getClass().getSimpleName();
				LogHelper.GAME_LOGGER.info("timeCost {} playerId:{} cmd:{} haust:{}", className, handler.getRoleId(), packet.getCmd(), timeCost);
			}

		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("MessageWork doHandler error channelId:{} cmd{} cause:{}", ChannelUtil.getRoleId(ctx), cmd, e.getMessage(), e);
		}
	}
}
