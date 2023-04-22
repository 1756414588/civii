package com.game.network.robot;

import com.game.message.MessagePool;
import com.game.network.ChannelUtil;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.register.PBFile;
import com.game.network.IPacketHandler;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 机器人的包处理器
 */
public class RobotPacketHandler implements IPacketHandler {

	public static Logger ROBOT_LOGGER = LoggerFactory.getLogger("ROBOT");

	@Override
	public void doPacket(ChannelHandlerContext ctx, Packet packet) {
		try {
			// TODO
			int cmd = packet.getCmd();
			int acccountKey = ChannelUtil.getAccountKey(ctx);

			Base base = Base.parseFrom(packet.getBytes(), PBFile.registry);
			ROBOT_LOGGER.info("[结果] accountKey:{} cmd:{} code:{}", acccountKey, cmd, base.getCode());

			MessagePool messagePool = SpringUtil.getBean(MessagePool.class);
			messagePool.handler(ctx, acccountKey, base);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
}
