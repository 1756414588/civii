package com.game.domain;

import com.game.packet.Packet;
import com.game.pb.BasePb;
import com.game.register.PBFile;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description 机器人服务器监听
 * @Date 2022/9/9 11:30
 **/

@Getter
@Setter
public class RobotListen {

	private long listenUid;
	private List<String> params;

	private ChannelHandlerContext ctx;

	private List<Integer> exitCmd = new ArrayList<>();

	public RobotListen(long listenUid, ChannelHandlerContext ctx) {
		this.listenUid = listenUid;
		this.ctx = ctx;
	}

	public void listenPacket(Packet packet) {
		if (packet.getRoleId() != listenUid) {
			return;
		}
		int cmd = packet.getCmd();
		if (exitCmd.contains(cmd)) {
			return;
		}
		ctx.writeAndFlush(packet);
	}

}
