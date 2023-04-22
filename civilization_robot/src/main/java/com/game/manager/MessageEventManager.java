package com.game.manager;

import com.game.acion.ActionFactory;
import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.define.LoadData;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.load.ILoadData;
import com.google.common.collect.HashBasedTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2022/9/9 17:50
 **/

@LoadData(name = "通信事件管理", initSeq = 10100)
@Component
public class MessageEventManager implements ILoadData {

	private Map<Long, IAction> actionMap = new HashMap<>();

	// accountKey,事件ID,action
	@Getter
	private HashBasedTable<Integer, Long, MessageEvent> listenEvents = HashBasedTable.create();

	@Autowired
	private MessageManager messageManager;
	@Autowired
	private RobotManager robotManager;
	@Autowired
	private ActionFactory actionFactory;

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
	 * 注册事件
	 *
	 * @param messageEvent
	 */
	public void registerEvent(MessageEvent messageEvent) {
		listenEvents.put(messageEvent.getRobot().getId(), messageEvent.getEventId(), messageEvent);
	}

	/**
	 * 注册事件
	 *
	 * @param robot
	 * @param eventId
	 * @param messageEvent
	 */
	public void registerEvent(Robot robot, long eventId, MessageEvent messageEvent) {
		listenEvents.put(robot.getId(), eventId, messageEvent);
	}

	/**
	 * 获取事件
	 *
	 * @param accountKey
	 * @param eventId
	 * @return
	 */
	public MessageEvent getEvent(int accountKey, long eventId) {
		return listenEvents.get(accountKey, eventId);
	}


	public void actionTimer() {
		List<Robot> list = robotManager.getRobotMap().values().stream().filter(e -> e.isLogin()).collect(Collectors.toList());
		list.forEach(robot -> {

			Record record = robot.getRecord();
			IAction action = actionMap.get(record.getRecordId());
			if (!action.isCompalte(robot)) {
				return;
			}

			// 获取下一步的指令
			long cmdKey = messageManager.getNext(record.getRecordId());
			IAction next = actionMap.get(cmdKey);
			if (next != null) {
				next.registerEvent(robot);
			}
		});
	}

}
