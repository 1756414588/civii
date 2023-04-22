package com.game.network;

import com.game.packet.Packet;
import com.game.util.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RobotEncoder extends MessageToByteEncoder<Packet> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf byteBuf) throws Exception {
		byte[] msg = packet.getBytes();
		byteBuf.writeShort(msg.length);
		byteBuf.writeBytes(msg);

//		LogHelper.CHANNEL_LOGGER.info("RobotEncoder roleId:{} send:{}", ChannelUtil.getRoleId(ctx), packet.getCmd());
	}
}
