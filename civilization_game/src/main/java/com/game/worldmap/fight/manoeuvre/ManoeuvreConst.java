package com.game.worldmap.fight.manoeuvre;

/**
 * 沙盘演武常量
 */
public interface ManoeuvreConst {

	/**
	 * 未开启
	 */
	int STATUS_NONE = 0;
	/**
	 * 报名阶段
	 */
	int STATUS_APPLY = 1;
	/**
	 * 准备阶段
	 */
	int STATUS_PREPARE = 2;
	/**
	 * 开始阶段
	 */
	int STATUS_START = 3;
	/**
	 * 结算阶段
	 */
	int STATUS_END = 4;


	/******第1回合(20分钟,准备阶段到开战)******/
	int STAGE_ONE = 1;
	/******第2回合******/
	int STAGE_TWO = 2;
	/******第3回合******/
	int STAGE_THRE = 3;
	/******结束结算阶段******/
	int STAGE_END = 4;

	/**
	 * 上路
	 */
	int LINE_ONE = 1;
	/**
	 * 中路
	 */
	int LINE_TWO = 2;
	/**
	 * 下路
	 */
	int LINE_THREE = 3;

	// 初始状态
	int COURSE_NONE = 0;
	// 开打中
	int COURSE_BEGIN = 1;
	// 已结束
	int COURSE_END = 2;

	long SECOND = 1000;

	// 赛程显示数量
	int SHOW_COURSE = 6;

	// 两连胜
	int TYPE_WIN_TWO = 2;
	// 三连胜
	int TYPE_WIN_THREE = 3;

	// 报名上限
	int LINE_FIGHTER_MAX = 25;

	// 平局
	int RESULT_QUITE = 0;
	// 左侧胜利
	int RESULT_LEFT_WIN = 1;
	// 右侧胜利
	int RESULT_RIGHT_WIN = 2;

	// 国家排名
	int TYPE_RANK_COUNTRY = 1;
	// 个人排名
	int TYPE_RANK_PERSON = 2;
	// 参与排名
	int TYPE_RANK_ATTEND = 3;

	int TYPE_RANK_PERSON_MAX = 2;

	// 报名限制等级
	int APPLY_LEVEL = 12;
}
