package com.game.network;

import com.game.packet.Packet;
import com.game.pb.BasePb;
import com.game.register.PBFile;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author 陈奎
 * @Description 玩家消息编码器
 * @Date 2022/9/9 11:30
 **/

public class MessageEncoder extends MessageToByteEncoder<Packet> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf byteBuf) {
		byte[] msg = packet.getBytes();
		byteBuf.writeShort(msg.length);
		byteBuf.writeBytes(msg);

		if (packet.getCmd() == 201 || packet.getCmd() == 30011 || packet.getCmd() == 30012 || packet.getCmd() == 32402) {
			return;
		}
//		BasePb.Base base = BasePbHelper.createBase(packet.getBytes());
		LogHelper.CHANNEL_LOGGER.info("MessageEncoder channelId:{} roleId:{} msg:{}", packet.getChannelId(), packet.getRoleId(), packet.getCmd());
	}
}
