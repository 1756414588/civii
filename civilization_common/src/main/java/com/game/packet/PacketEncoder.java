package com.game.packet;

import com.game.util.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author 陈奎
 * @Description 服务器通信包编码器
 * @Date 2022/9/9 11:30
 **/

public class PacketEncoder extends MessageToByteEncoder<Packet> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
		ByteBuf buf = Unpooled.buffer();
		buf.writeShort(29 + (packet.getBytes() != null ? packet.getBytes().length : 0));
		buf.writeInt(packet.getCmd());//4
		buf.writeInt(packet.getSeq());//4
		buf.writeLong(packet.getChannelId());//8
		buf.writeLong(packet.getRoleId());//8
		buf.writeShort(packet.getError());//2
		buf.writeByte(packet.getCallBack());//1

		if (packet.getBytes() != null && packet.getBytes().length > 0) {
			buf.writeBytes(packet.getBytes());
		}
		out.writeBytes(buf);
		if (packet.getCmd() == 201 || packet.getReq() == 30011) {
			return;
		}
//		LogHelper.CHANNEL_LOGGER.info("PacketEncoder channeId:{} roleId:{} req:{} cmd:{}", packet.getChannelId(), packet.getRoleId(), packet.getReq(), packet.getCmd());
	}
}
