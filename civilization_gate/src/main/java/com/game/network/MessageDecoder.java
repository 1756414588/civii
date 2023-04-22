package com.game.network;

import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb;
import com.game.pb.BasePb.Base;
import com.game.register.PBFile;
import com.game.util.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 *
 * @Description 玩家消息解码器
 * @Date 2022/9/9 11:30
 **/

public class MessageDecoder extends ByteToMessageDecoder {

	private static final String DECODER_STATE_KEY = "DECODE_STATE";

	private static class DecoderState {

		public int length = -1;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		DecoderState state = ChannelUtil.getAttribute(ctx, DECODER_STATE_KEY);
		if (state == null) {
			state = new DecoderState();
			ChannelUtil.setAttribute(ctx, DECODER_STATE_KEY, state);
		}
		if (in.readableBytes() < 2 && state.length == -1) {
			return;
		}
		if (state.length != -1) {
			if (in.readableBytes() < state.length) {
				return;
			}
		} else {
			state.length = in.readShort();
			if (in.readableBytes() < state.length) {
				return;
			}
		}
		if (state.length < 2) {
			ctx.close();
		}

		ByteBuf obj = Unpooled.buffer();
		obj.writeBytes(in, state.length);
		int length = obj.readableBytes();
		byte[] bytes = new byte[obj.readableBytes()];
		obj.readBytes(bytes, 0, length);

		BasePb.Base msg = BasePb.Base.parseFrom(bytes, PBFile.registry);

		int command = msg.getCommand();
		long channelId = ChannelUtil.getChannelId(ctx);
		long roleId = ChannelUtil.getRoleId(ctx);

		Packet packet = PacketCreator.create(command, msg.toByteArray(), roleId, channelId);
		packet.setLength(state.length);
		out.add(packet);
		state.length = -1;

		if (command == 30011 || command == 201 || command == 30012 || command == 32401) {
			return;
		}

		LogHelper.CHANNEL_LOGGER.info("MessageDecoder channelId:{} roleId:{} cmd:{}", channelId, roleId, packet.getCmd());
	}
}
