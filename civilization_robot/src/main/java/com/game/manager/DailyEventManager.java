package com.game.manager;

import com.game.acion.IAction;
import com.game.acion.facotry.DailyFactory;
import com.game.cache.RobotDataCache;
import com.game.define.LoadData;
import com.game.domain.p.DailyMessage;
import com.game.load.ILoadData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description机器人日常行为攻打野怪据点刷地图
 * @Date 2022/10/20 20:24
 **/

@Component
@LoadData(name = "日常事件管理", initSeq = 1000)
public class DailyEventManager implements ILoadData {

	private List<IAction> actionList = new ArrayList<>();
	private Map<Long, IAction> actionMap = new HashMap<>();
	private Map<Long, IAction> parentMap = new HashMap<>();

	@Autowired
	private MessageManager messageManager;
	@Autowired
	private DailyFactory dailyFactory;


	@Override
	public void load() {
	}

	@Override
	public void init() {
		List<DailyMessage> dailyMessagesList = messageManager.getDailyMessageList();
		for (DailyMessage message : dailyMessagesList) {
			IAction action = dailyFactory.createAction(message);
			actionList.add(action);

			// key:v
			actionMap.put(message.getKeyId(), action);

			parentMap.put(message.getParentId(), action);
		}
	}

	public IAction getAction(long messageId) {
		return actionMap.get(messageId);
	}

	public IAction getByChild(long messageId) {
		return parentMap.get(messageId);
	}

}
