package com.game.constant;

public interface CountryConst {
	int DAILY_MAX = 10;

	// 下面Id和配置保持一致
	int GOVERN_KING = 1;// 主席
	int GOVERN_PRIME = 2;// 总理
	int GOVERN_ADVISER = 3;// 国务卿
	int GOVERN_GENERAL = 4;// 元帅

	int VOTE_NO = 0;// 初始化状态
	int VOTE_ING = 1;// 选举中
	int VOTE_END = 2;// 选举结束
	int VOTE_PREPREA = 3;// 选举准备

	int GENERAL_MAX = 8;// 将军最大数目

	int RANK_CITY = 1;// 城战
	int RANK_STATE = 2;// 国战
	int RANK_BUILD = 3;// 建设

	int FREE_CALL = 1;// 官员的免费召唤次数
	int CALL_TOTAL = 2;// 官员总召唤次数
}
