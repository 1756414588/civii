
package com.game.packet;

import com.game.network.ChannelUtil;
import com.game.util.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Description 服务器通信包解码器
 * @Date 2022/9/9 11:30
 **/

public class PacketDecoder extends ByteToMessageDecoder {

    protected int getUnsignedShort(short data) { // 将data字节型数据转换为0~65535 (0xFFFF
        return data & 0x0FFFF;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 2) {
            return;
        }
        in.markReaderIndex();
        int unsignedShort = getUnsignedShort(in.readShort());
        int length = unsignedShort - 2;
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        Packet packet = PacketCreator.readFrom(in, length);
        packet.setLength(length);
        out.add(packet);
        if (packet.getCmd() == 201 || packet.getCmd() == 30011 || packet.getCmd() == 30012) {
            return;
        }
//
//		LogHelper.CHANNEL_LOGGER.info("PacketDecoder channelId:{} roleId:{} req:{}", packet.getChannelId(), packet.getRoleId(), packet.getCmd());
    }
}
