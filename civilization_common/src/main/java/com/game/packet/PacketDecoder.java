
package com.game.packet;

import com.game.network.ChannelUtil;
import com.game.util.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 *
 * @Description 服务器通信包解码器
 * @Date 2022/9/9 11:30
 **/

public class PacketDecoder extends ByteToMessageDecoder {

	protected static final String DECODER_STATE_KEY = "DECODE_STATE";

	protected static class DecodeState {

		public DecodeState() {
		}

		public int length = -1;
	}

	protected int getUnsignedShort(short data) { // 将data字节型数据转换为0~65535 (0xFFFF
		return data & 0x0FFFF;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		DecodeState state = ChannelUtil.getAttribute(ctx, DECODER_STATE_KEY);
		if (state == null) {
			state = new DecodeState();
			ChannelUtil.setAttribute(ctx, DECODER_STATE_KEY, state);
		}

		if (in.readableBytes() < 2 && state.length == -1) {
			return;
		}

		if (state.length != -1) {
			if (in.readableBytes() < state.length - 2) {
				return;
			}
		} else {
			state.length = getUnsignedShort(in.readShort());
			if (in.readableBytes() < state.length - 2) {
				return;
			}
		}

		// ByteBuf bb = Unpooled.buffer();
		int length = state.length - 2;
		// bb.writeBytes(in, length);

		Packet packet = PacketCreator.readFrom(in, length);
		packet.setLength(state.length);

		out.add(packet);
		state.length = -1;

		if (packet.getCmd() == 201 || packet.getCmd() == 30011 || packet.getCmd() == 30012) {
			return;
		}
//
//		LogHelper.CHANNEL_LOGGER.info("PacketDecoder channelId:{} roleId:{} req:{}", packet.getChannelId(), packet.getRoleId(), packet.getCmd());
	}
}
