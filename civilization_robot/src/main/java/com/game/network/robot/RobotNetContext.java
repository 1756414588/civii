package com.game.network.robot;

import com.game.acion.MessageEvent;
import com.game.acion.login.BeginGameAction;
import com.game.define.App;
import com.game.domain.LoginAccount;
import com.game.domain.Robot;
import com.game.manager.RobotNetManager;
import com.game.network.ChannelUtil;
import com.game.network.INetContext;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.UserLoginRq;
import com.game.network.IPacketHandler;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

/**
 * 机器人连接处理器
 */
@Getter
@Setter
public class RobotNetContext implements INetContext {

	// 基础属性
	private String host;
	private int port;

	private IPacketHandler packetHandler;
	private Robot robot;

	public RobotNetContext(Robot robot, String host, int port, IPacketHandler packetHandler) {
		this.robot = robot;
		this.host = host;
		this.port = port;
		this.packetHandler = packetHandler;
	}

	@Override
	public App app() {
		return App.ROBOT_CLIENT;
	}

	@Override
	public String host() {
		return host;
	}

	@Override
	public int port() {
		return port;
	}

	@Override
	public void onSucess(ChannelHandlerContext ctx) {
		LoginAccount account = robot.getLoginAccount();

		// 绑定channel和robot关系
		ChannelUtil.setAccountKey(ctx, account.getKeyId());

		// 机器人连接管理
		RobotNetManager robotNetManager = SpringUtil.getBean(RobotNetManager.class);
		robotNetManager.listen(robot.getRobotNet(), ctx);

		// 开始游戏
		BeginGameAction beginGameAction = SpringUtil.getBean(BeginGameAction.class);
		MessageEvent messageEvent = new MessageEvent(robot, beginGameAction, 1);
		TimerServer.getInst().addDelayEvent(messageEvent);
	}

	@Override
	public void onDisconnect(ChannelHandlerContext ctx) {
		RobotNetManager robotNetManager = SpringUtil.getBean(RobotNetManager.class);
		robotNetManager.remove(ChannelUtil.getNetId(ctx));
	}

}
