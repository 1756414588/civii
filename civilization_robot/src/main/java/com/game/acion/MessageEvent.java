package com.game.acion;

import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.timer.TimerEvent;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @Description 消息事件
 * @Date 2022/9/14 19:25
 **/

public class MessageEvent extends TimerEvent {

	protected long eventId;
	protected Robot robot;
	protected IAction action;
	protected Map<String, Object> param = new HashMap<>();

	public MessageEvent(Robot robot, IAction action, long delayTime) {
		super(1, delayTime);
		this.robot = robot;
		this.eventId = robot.getMsgSeq();
		this.action = action;
	}

	@Override
	public void action() {
		try {
			action.doAction(this, robot);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public Packet createPacket() {
		Base.Builder builder = BasePbHelper.createBase(action.getMessage()).toBuilder();
		builder.setParam(eventId);
		return PacketCreator.create(builder.build());
	}

	/**
	 * 事件完成
	 *
	 * @param robot
	 * @param base
	 */
	public void complate(Robot robot, Base base) {
	}

	public Robot getRobot() {
		return robot;
	}

	public long getEventId() {
		return eventId;
	}

	public IAction getAction() {
		return action;
	}

	public void setAction(IAction action) {
		this.action = action;
	}

	/**
	 * 事件重置
	 *
	 * @param delay
	 */
	public void reset(long delay) {
		this.eventId = robot.getMsgSeq();
		this.setEnd(System.currentTimeMillis() + delay);
	}

	public Map<String, Object> getParam() {
		return param;
	}

}
