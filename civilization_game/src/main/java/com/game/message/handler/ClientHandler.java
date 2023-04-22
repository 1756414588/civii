package com.game.message.handler;

import com.game.constant.GameError;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.register.PBFile;
import com.game.server.GameServer;
import com.game.server.netserver.MessageFilter;
import com.game.util.LogHelper;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.InvalidProtocolBufferException;


abstract public class ClientHandler extends Handler {


	private Long roleId;
	private Packet packet;

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Packet getPacket() {
		return packet;
	}

	public void setPacket(Packet packet) {
		this.packet = packet;
		this.channelId = packet.getChannelId();
		try {
			if (packet.getBytes() != null && packet.getBytes().length > 0) {
				this.msg = Base.parseFrom(packet.getBytes(), PBFile.registry);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	@Override
	public DealType dealType() {
		return DealType.MAIN;
	}

	public void sendErrorMsgToPlayer(GameError gameError) {
		Base.Builder baseBuilder = createRsBase(gameError.getCode());
		sendMsgToPlayer(baseBuilder);
	}

	public <T> void sendMsgToPlayer(GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = createRsBase(GameError.OK, ext, msg);
		sendMsgToPlayer(baseBuilder);
	}

	public <T> void sendMsgToPlayer(GameError gameError, GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = createRsBase(gameError, ext, msg);
		sendMsgToPlayer(baseBuilder);
	}

	public void sendMsgToPlayer(Base.Builder builder) {
		Packet packet = PacketCreator.create(builder.build(), getRoleId(), getChannelId(), getPacket().getSeq(), getPacket().getCmd());
		getCtx().writeAndFlush(packet);
		if (!MessageFilter.isFilterPrint(packet.getCmd())) {
			LogHelper.CHANNEL_LOGGER.info("sendToClient channelId:{} playerId:{} cmd:{}", packet.getChannelId(), packet.getRoleId(), packet.getCmd());
		}
	}


}
