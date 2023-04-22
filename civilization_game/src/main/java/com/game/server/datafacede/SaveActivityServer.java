package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.server.thread.SaveServer;
import com.game.spring.SpringUtil;
import java.util.Iterator;

import com.game.domain.ActivityData;
import com.game.domain.p.Activity;
import com.game.manager.ActivityManager;
import com.game.server.thread.SaveActivityThread;
import com.game.server.thread.SaveThread;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Author 陈奎
 * @Description 活动数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "活动存储")
@Service
public class SaveActivityServer extends SaveServer<Activity> {

	public SaveActivityServer() {
		super("SAVE_ACTIVITY_SERVER", 2);
	}

	public SaveThread createThread(String name) {
		return new SaveActivityThread(name);
	}

	@Override
	public void saveData(Activity activity) {
		SaveThread thread = threadPool.get(activity.getActivityId() % threadNum);
		thread.add(activity);
	}

	@Override
	public void saveAll() {
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		Iterator<ActivityData> iterator = activityManager.getActivityMap().values().iterator();
		while (iterator.hasNext()) {
			try {
				ActivityData activityData = iterator.next();
				saveData(activityData.copyData());
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error("SAVE_ACTIVITY_SERVER:{}", e.getMessage(), e);
			}
		}
	}
}
