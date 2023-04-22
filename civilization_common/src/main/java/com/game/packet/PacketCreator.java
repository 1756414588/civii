package com.game.packet;

import com.game.pb.BasePb.Base;
import io.netty.buffer.ByteBuf;

/**
 * @Author 陈奎
 * @Description 服务器通信包创建器
 * @Date 2022/9/9 11:30
 **/

public class PacketCreator {

	public static Packet create(int cmd, byte[] bytes) {
		return create(cmd, 0, bytes);
	}

	public static Packet create(byte[] bytes) {
		Packet packet = new Packet();
		packet.bytes = bytes;
		return packet;
	}

	public static Packet create(byte[] bytes, long roleId, long channelId) {
		Packet packet = new Packet();
		packet.bytes = bytes;
		packet.roleId = roleId;
		packet.channelId = channelId;
		return packet;
	}

	public static Packet create(int cmd, int error, byte[] bytes) {
		Packet packet = new Packet();
		packet.cmd = cmd;
		packet.error = (short) error;
		packet.bytes = bytes;
		return packet;
	}

	public static Packet create(int cmd, byte[] bytes, long roleId, long channelId) {
		Packet packet = new Packet();
		packet.cmd = cmd;
		packet.bytes = bytes;
		packet.roleId = roleId;
		packet.channelId = channelId;
		return packet;
	}

	public static Packet create(Base msg) {
		Packet packet = new Packet();
		packet.cmd = msg.getCommand();
		packet.bytes = msg.toByteArray();
		return packet;
	}

	public static Packet create(Base msg, long roleId, long channelId) {
		Packet packet = new Packet();
		packet.roleId = roleId;
		packet.cmd = msg.getCommand();
		packet.bytes = msg.toByteArray();
		packet.channelId = channelId;
		return packet;
	}

	public static Packet create(Base msg, long roleId, long channelId, int seq, int req) {
		Packet packet = new Packet();
		packet.roleId = roleId;
		packet.channelId = channelId;
		packet.cmd = msg.getCommand();
		packet.bytes = msg.toByteArray();
		packet.seq = seq;
		packet.req = req;
		return packet;
	}

	public static Packet readFrom(ByteBuf buf, int len) {
		Packet packet = new Packet();
		packet.cmd = buf.readInt();//4
		packet.seq = buf.readInt();//4
		packet.channelId = buf.readLong();//8
		packet.roleId = buf.readLong();//8
		packet.error = buf.readShort();//2
		packet.callBack = buf.readByte();//1

		int preLen = 27;

		if (len - preLen > 0) {
			packet.bytes = new byte[len - preLen];
			buf.readBytes(packet.bytes, 0, packet.bytes.length);
		}
		return packet;
	}


}
