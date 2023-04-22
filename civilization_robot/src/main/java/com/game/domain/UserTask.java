package com.game.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTask {

	private int taskId;// 任务编号, s_task表中的taskId
	private int taskType;// 任务类型, 1: 主线 2:支线 3 4活动任务
	private long process;// 进度值
	private int status;// 0不可领奖 1可以领奖 2.已经领完
	private int cond;// 条件完成状态 1个条件 0 2个条件 0,0 3个 0,0,0
}
