package com.game.util;

import com.game.pb.BasePb.Base;
import com.game.register.PBFile;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.InvalidProtocolBufferException;

public class BasePbHelper {

	public static byte[] putShort(short s) {
		byte[] b = new byte[2];
		b[0] = (byte) (s >> 8);
		b[1] = (byte) (s >> 0);
		return b;
	}

	static public short getShort(byte[] b, int index) {
		return (short) (((b[index + 1] & 0xff) | b[index + 0] << 8));
	}

	static public Base parseFromByte(byte[] result) throws InvalidProtocolBufferException {
		short len = getShort(result, 0);
		byte[] data = new byte[len];
		System.arraycopy(result, 2, data, 0, len);
		Base rs = Base.parseFrom(data, PBFile.registry);
		return rs;
	}

	static public Base createRsBase(int cmd, int code, long index) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		baseBuilder.setCode(code);
		baseBuilder.setIndex(index);
		return baseBuilder.build();
	}

	static public <T> Base.Builder createRqBase(int cmd, Long param, GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		if (param != null) {
			baseBuilder.setParam(param);
		}

		baseBuilder.setExtension(ext, msg);
		return baseBuilder;
	}

	static public <T> Base.Builder createRqBase(int cmd, GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		baseBuilder.setExtension(ext, msg);
		return baseBuilder;
	}

	static public <T> Base.Builder createSynBase(int cmd, GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		if (msg != null) {
			baseBuilder.setExtension(ext, msg);
		}
		return baseBuilder;
	}

	static public Base createBase(byte[] bytes) {
		try {
			Base msg = Base.parseFrom(bytes, PBFile.registry);
			return msg;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return null;
	}

	static public <T> T createPb(GeneratedExtension<Base, T> ext, byte[] bytes) {
		try {
			Base msg = Base.parseFrom(bytes, PBFile.registry);
			return msg.getExtension(ext);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return null;
	}

//	static public <T> Packet createPacket(int cmd, GeneratedExtension<Base, T> ext, T msg) {
//		Base.Builder baseBuilder = Base.newBuilder();
//		baseBuilder.setCommand(cmd);
//		baseBuilder.setExtension(ext, msg);
//		return PacketCreator.create(baseBuilder.build());
//	}
}
