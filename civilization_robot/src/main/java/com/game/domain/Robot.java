package com.game.domain;

import com.game.cache.UserCache;
import com.game.network.ICallback;
import com.game.network.robot.RobotNet;
import com.game.packet.Packet;
import com.game.pb.CommonPb.Mail;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Robot {

	// 角色
	private Lord lord;
	// 登录服账号
	private LoginAccount loginAccount;

	// 机器人连接管理类
	private RobotNet robotNet;

	// 世界相关信息
	private World world = new World();

	// 玩家邮件
	private Map<Long, Mail> mails = new ConcurrentHashMap<>();

	// 用户缓存数据
	private UserCache cache = new UserCache();

	// 新手引导步骤
	private int guideKey;

	// 新手引导
	private int newStateId;

	//
	private boolean login;

	//
	private Record record;

	// 自增
	private AtomicLong incr = new AtomicLong(1L);

	public Robot(LoginAccount loginAccount) {
		this.loginAccount = loginAccount;
	}

	public int getId() {
		return loginAccount.getKeyId();
	}

	/**
	 * 监听到的消息
	 *
	 * @param packet
	 */
	public void listen(Packet packet) {
		sendPacket(packet);
	}

	public long getMsgSeq() {
		return incr.incrementAndGet();
	}

	public void sendPacket(Packet packet) {
		if (robotNet != null) {
			robotNet.send(packet);
		}
	}

	public void sendMsg(Packet packet, ICallback callback) {
		if (robotNet == null) {
			return;
		}
		robotNet.send(packet, callback);
	}

	public String getPos() {
		return lord.getPosX() + "," + lord.getPosY();
	}

	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

}
