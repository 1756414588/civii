package com.game.network;

import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.util.BasePbHelper;
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

		Base base = BasePbHelper.createBase(msg);
		LogHelper.PACKET_LOGGER.info("RobotEncoder roleId:{} cmd:{} eventId:{}", ChannelUtil.getRoleId(ctx), packet.getCmd(), base.getParam());
	}
}
