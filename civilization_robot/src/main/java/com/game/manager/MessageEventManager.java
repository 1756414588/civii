package com.game.manager;

import com.game.acion.facotry.ActionFactory;
import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.define.LoadData;
import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.load.ILoadData;
import com.game.pb.BasePb.Base;
import com.google.common.collect.HashBasedTable;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/9/9 17:50
 **/

@LoadData(name = "通信事件管理", initSeq = 10100)
@Component
public class MessageEventManager implements ILoadData {

	private Map<Long, IAction> actionMap = new HashMap<>();

	@Getter
	private Map<Integer, Map<Long, MessageEvent>> listenEvents = new ConcurrentHashMap<>();

	@Autowired
	private MessageManager messageManager;
	@Autowired
	private ActionFactory actionFactory;
	@Autowired
	private RobotManager robotManager;

	@Override
	public void load() {
	}


	@Override
	public void init() {
		List<RobotMessage> cmdList = messageManager.getRobotMessageList();
		for (RobotMessage robotMessage : cmdList) {
			IAction action = actionFactory.createAction(robotMessage);
			actionMap.put(robotMessage.getKeyId(), action);
		}
	}

	public IAction getAction(long id) {
		return actionMap.get(id);
	}


	/**
	 * 创建事件容器
	 *
	 * @param accountKey
	 */
	public void createEventContain(int accountKey) {
		listenEvents.put(accountKey, new HashMap<>());
	}

	/**
	 * 注册事件
	 *
	 * @param messageEvent
	 */
	public void registerEvent(MessageEvent messageEvent) {
		Map<Long, MessageEvent> messageEventMap = listenEvents.get(messageEvent.getRobot().getId());
		messageEventMap.put(messageEvent.getEventId(), messageEvent);
	}

	/**
	 * 注册事件
	 *
	 * @param robot
	 * @param eventId
	 * @param messageEvent
	 */
	public void registerEvent(Robot robot, long eventId, MessageEvent messageEvent) {
		Map<Long, MessageEvent> messageEventMap = listenEvents.get(robot.getId());
		messageEventMap.put(eventId, messageEvent);
	}

	/**
	 * 获取事件
	 *
	 * @param accountKey
	 * @param eventId
	 * @return
	 */
	public MessageEvent getEvent(int accountKey, long eventId) {
		Map<Long, MessageEvent> messageEventMap = listenEvents.get(accountKey);
		if (messageEventMap == null) {
			return null;
		}
		return messageEventMap.get(eventId);
	}

	/**
	 * 移除
	 *
	 * @param robot
	 */
	public void removeEvent(Robot robot) {
		Map<Long, MessageEvent> messageEventMap = listenEvents.get(robot.getId());
		messageEventMap.clear();
	}

	/**
	 * 调用事件
	 *
	 * @param ctx
	 * @param accountKey
	 * @param base
	 */
	public void callEvent(ChannelHandlerContext ctx, int accountKey, Base base) {
		long eventId = base.getParam();
		if (eventId == 0) {
			return;
		}

		MessageEvent messageEvent = getEvent(accountKey, eventId);
		if (messageEvent == null) {
			return;
		}

		Robot robot = robotManager.getRobotByKey(accountKey);
		messageEvent.getAction().onResult(messageEvent, robot, base);
		listenEvents.remove(accountKey, base.getParam());
	}

}
