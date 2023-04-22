package com.game.action;


import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.register.PBFile;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.InvalidProtocolBufferException;

public abstract class PacketHandler implements IPacketHandler {

	protected int res;

	public PacketHandler() {
	}

	@Override
	public void setResponseCmd(int res) {
		this.res = res;
	}

	public Base getBase(Packet packet) {
		try {
			Base base = Base.parseFrom(packet.getBytes(), PBFile.registry);
			return base;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return null;
	}

	public <T> T getMsg(Packet packet, GeneratedExtension<Base, T> ext) {
		Base msg = getBase(packet);
		if (msg == null) {
			return null;
		}
		return msg.getExtension(ext);
	}


}
