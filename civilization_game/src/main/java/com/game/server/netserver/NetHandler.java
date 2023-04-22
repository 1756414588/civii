package com.game.server.netserver;

import com.game.network.ChannelAttr;
import com.game.network.ChannelUtil;
import com.game.network.INet;
import com.game.network.Net;
import com.game.network.NetManager;
import com.game.packet.Packet;

import com.game.pb.ChatPb.DoChatRq;
import com.game.pb.ChatPb.DoPersonChatRq;
import com.game.pb.ChatPb.DuelRq;
import com.game.pb.ChatPb.GetChatRq;
import com.game.pb.ChatPb.GetPersonChatRoomRq;
import com.game.pb.ChatPb.GetPersonChatRq;
import com.game.pb.ChatPb.PersonChatRemoveRq;
import com.game.pb.ChatPb.SeeManRq;
import com.game.pb.ChatPb.ShareMailRq;
import com.game.pb.ChatPb.SuggestRq;
import com.game.pb.RolePb.CreateRoleRq;
import com.game.pb.RolePb.UserLoginRq;
import com.game.network.IPacketHandler;
import com.game.server.exec.LoginExecutor;
import com.game.server.exec.MessageExecutor;
import com.game.server.exec.NonExecutor;
import com.game.server.work.MessageWork;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Author 陈奎
 * @Description 内部连接句柄
 * @Date 2022/9/9 11:30
 **/

public class NetHandler extends SimpleChannelInboundHandler<Packet> implements IPacketHandler {


	@Override
	public void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
		INet net = ChannelUtil.getAttribute(ctx, ChannelAttr.NET);
		net.messageReceived(ctx, packet);
	}

	@Override
	public void doPacket(ChannelHandlerContext ctx, Packet packet) {

		int cmd = packet.getCmd();
		long channelId = packet.getChannelId();
		long roleId = packet.getRoleId();
		if (!MessageFilter.isFilterPrint(cmd)) {
			LogHelper.CHANNEL_LOGGER.info("doPacket channelId:{} playerId:{} cmd:{}", channelId, roleId, cmd);
		}

		// 该消息不需要执行
		if (MessageFilter.isFilterExec(cmd)) {
			return;
		}

		if (cmd == UserLoginRq.EXT_FIELD_NUMBER || cmd == CreateRoleRq.EXT_FIELD_NUMBER) {
			LoginExecutor loginExecutor = SpringUtil.getBean(LoginExecutor.class);
			loginExecutor.add(new MessageWork(ctx, packet));
		} else if (cmd == GetChatRq.EXT_FIELD_NUMBER || cmd == DoChatRq.EXT_FIELD_NUMBER || cmd == ShareMailRq.EXT_FIELD_NUMBER || cmd == SeeManRq.EXT_FIELD_NUMBER
			|| cmd == SuggestRq.EXT_FIELD_NUMBER || cmd == DoPersonChatRq.EXT_FIELD_NUMBER || cmd == GetPersonChatRoomRq.EXT_FIELD_NUMBER || cmd == GetPersonChatRq.EXT_FIELD_NUMBER
			|| cmd == PersonChatRemoveRq.EXT_FIELD_NUMBER || cmd == DuelRq.EXT_FIELD_NUMBER) {
			NonExecutor nonExecutor = SpringUtil.getBean(NonExecutor.class);
			nonExecutor.add(new MessageWork(ctx, packet));
		} else {
			MessageExecutor taskExecutor = SpringUtil.getBean(MessageExecutor.class);
			taskExecutor.add(new MessageWork(ctx, packet));
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		Net net = new Net(ctx, this);
		ChannelUtil.setAttribute(ctx, ChannelAttr.NET, net);
		LogHelper.GAME_LOGGER.info("连接激活");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Net net = ChannelUtil.getAttribute(ctx, ChannelAttr.NET);
		NetManager.getInst().remove(net);
		LogHelper.GAME_LOGGER.info("连接失效");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LogHelper.ERROR_LOGGER.error("发生异常", cause.getMessage(), cause);
		Net net = ChannelUtil.getAttribute(ctx, ChannelAttr.NET);
		net.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				ctx.close();
			}
		}
	}
}
