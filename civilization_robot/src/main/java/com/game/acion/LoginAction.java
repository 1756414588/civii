package com.game.acion;

import com.game.acion.login.OnAllLoginReqSuccessEvent;
import com.game.cache.CacheManager;
import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 登录相关消息行为
 * @Date 2022/9/13 14:48
 **/

public class LoginAction implements IAction {

	protected int requestCode;
	protected int respondCode;
	protected Packet requestPacket;
	protected byte[] bytes;

	public LoginAction() {
	}

	public LoginAction(int respondCode, Packet packet) {
		this.requestCode = packet.getCmd();
		this.respondCode = respondCode;
		this.requestPacket = packet;
		this.bytes = packet.getBytes();
	}

	@Override
	public long getId() {
		return requestCode;
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();
		Base base = BasePbHelper.createBase(bytes);
		Packet packet = PacketCreator.create(base.toBuilder().setParam(eventId).build());
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[登录.消息] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, requestPacket.getCmd());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		CacheManager cacheManager = SpringUtil.getBean(CacheManager.class);

		// 所有的登录消息有返回处理
		if (!cacheManager.isContain(robot.getId())) {
			robot.setLogin(true);
			LogHelper.CHANNEL_LOGGER.info("【登录完成】 robot:{}", robot.getId());
			onLoginSuccess(robot);
		}
	}

	@Override
	public void registerEvent(Robot robot) {
		CacheManager cacheManager = SpringUtil.getBean(CacheManager.class);
		MessageEvent messageEvent = new MessageEvent(robot, this, getRemain());
		cacheManager.put(robot.getId(), messageEvent);
		// 添加事件执行
		TimerServer.getInst().addDelayEvent(messageEvent);
	}

	@Override
	public boolean isCompalte(Robot robot) {
		return false;
	}

	@Override
	public long getRemain() {
		return 10;
	}

	@Override
	public RobotMessage getRobotMessage() {
		return null;
	}

	public void onLoginSuccess(Robot robot) {
		OnAllLoginReqSuccessEvent loginAllReqSuccessEvent = SpringUtil.getBean(OnAllLoginReqSuccessEvent.class);
		TimerServer.getInst().addDelayEvent(new MessageEvent(robot, loginAllReqSuccessEvent, 10000L));
	}

}
