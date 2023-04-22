package com.game.network;

import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb;
import com.game.register.PBFile;
import com.game.util.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;


public class RobotDecoder extends ByteToMessageDecoder {

	private static final String DECODER_STATE_KEY = "DECODE_STATE";

	private static class DecoderState {

		public int length = -1;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		try {

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

			long channelId = ChannelUtil.getChannelId(ctx) == null ? 0 : ChannelUtil.getChannelId(ctx);
			long roleId = ChannelUtil.getRoleId(ctx) == null ? 0L : ChannelUtil.getRoleId(ctx);


			Packet packet = PacketCreator.create(msg.getCommand(), msg.toByteArray(), roleId, channelId);
			packet.setLength(state.length);
			out.add(packet);
			state.length = -1;

			LogHelper.CHANNEL_LOGGER.info("RobotDecoder msg:{}", msg.getCommand());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
