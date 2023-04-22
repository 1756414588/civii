package com.game.acion.login;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.acion.login.OnAllLoginReqSuccessEvent;
import com.game.domain.Robot;
import com.game.manager.MessageEventManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.EnterGameRs;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 登录成功后，进行相关角色的数据拉取行为
 * @Date 2022/9/13 14:48
 **/

public class EnterGameAction implements IAction {

	protected int requestCode;
	protected int respondCode;
	protected Packet requestPacket;
	protected byte[] bytes;

	public EnterGameAction() {
	}

	public EnterGameAction(int respondCode, Packet packet) {
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
		LogHelper.CHANNEL_LOGGER.info("[角色信息.消息] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, requestPacket.getCmd());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		long eventId = messageEvent.getEventId();
		LogHelper.CHANNEL_LOGGER.info("[角色信息.返回] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, requestPacket.getCmd());
		if (base.getCommand() == EnterGameRs.EXT_FIELD_NUMBER) {
			robot.setLogin(true);

			loginCompate(robot);
		}
	}

	@Override
	public void registerEvent(Robot robot) {
		MessageEvent messageEvent = new MessageEvent(robot, this, getRemain());
		MessageEventManager messageEventManager = SpringUtil.getBean(MessageEventManager.class);
		messageEventManager.registerEvent(robot, messageEvent.getEventId(), messageEvent);
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
	public byte[] getMessage() {
		return null;
	}

	public void loginCompate(Robot robot) {
		LogHelper.CHANNEL_LOGGER.info("拉取角色信息完毕,一分钟后开始自动");
		OnAllLoginReqSuccessEvent loginAllReqSuccessEvent = SpringUtil.getBean(OnAllLoginReqSuccessEvent.class);
		TimerServer.getInst().addDelayEvent(new MessageEvent(robot, loginAllReqSuccessEvent, 60000L));
	}

	@Override
	public int getGroup() {
		return 0;
	}

}
