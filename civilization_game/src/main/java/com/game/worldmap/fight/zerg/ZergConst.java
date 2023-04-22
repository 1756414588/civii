package com.game.worldmap.fight.zerg;

public interface ZergConst {

	// 无处理
	public int NONE = 0;
	// 提前结束
	public int AHEAD_END = 1;
	// 结束
	public int END = 2;

	// 初始化阶段
	int STEP_INIT = 0;
	// 进攻阶段
	public int STEP_ATTACK = 1;
	// 防守阶段
	public int STEP_DEFEND = 2;

	// 阶段切换时间
	public int PERIOD = 600000;


	// 胜利奖励积分
	int SCORE_WIN = 1000;
	// 击杀参与奖励
	int SCORE_KILL = 2500;
	// 活动参与积分奖励
	int SCORE_ATTEND = 200;
	// 防守胜利积分奖励
	int SCORE_DEFEND_WIN = 300;

	// 击杀600兑换1积分
	int KILL_NUM_CONVERT_SCORE = 600;
	// 击杀600积分
	int DEAD_NUM_CONVERT_SCORE = 100;
}
