package com.game.cache;

import com.game.domain.UserTask;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * @Author 陈奎
 * @Description 任务缓存
 * @Date 2022/9/22 10:20
 **/

@Getter
public class UserTaskCache {

	// 玩家任务
	private Map<Integer, UserTask> userTaskMap = new HashMap<>();
}
