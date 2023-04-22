package com.game.server.work;

import com.game.message.MessagePool;
import com.game.network.ChannelUtil;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.server.ITask;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author 陈奎
 * @Description消息处理任务
 * @Date 2022/9/23 11:46
 **/

public class MessageTask implements ITask {

	public static Logger ROBOT_LOGGER = LoggerFactory.getLogger("ROBOT");

	private ChannelHandlerContext ctx;
	private Packet packet;

	public long getId() {
		return packet.getRoleId();
	}

	public MessageTask(ChannelHandlerContext ctx, Packet packet) {
		this.ctx = ctx;
		this.packet = packet;
	}

	@Override
	public void run() {
		int cmd = packet.getCmd();
		int acccountKey = ChannelUtil.getAccountKey(ctx);

		Base base = BasePbHelper.createBase(packet.getBytes());
		if (base.getCode() != 0 && base.getCode() != 200) {
			ROBOT_LOGGER.info("[结果] accountKey:{} cmd:{} code:{}", acccountKey, cmd, base.getCode());
		}

		MessagePool messagePool = SpringUtil.getBean(MessagePool.class);
		messagePool.handler(ctx, acccountKey, base);

		LogHelper.PACKET_LOGGER.info("RobotDecoder roleId:{} cmd:{} eventId:{} code:{}", packet.getRoleId(), base.getCommand(), base.getParam(), base.getCode());
	}
}
