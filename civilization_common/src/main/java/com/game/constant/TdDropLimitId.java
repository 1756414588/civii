package com.game.constant;

public interface TdDropLimitId {
	// 无尽模式军械商店刷新时价格增加规则[价格，每次刷新价格增加]
	int ENDLESS_ARMORY_SHOP_PRICE = 1;
	// 无尽模式每日刷新商品规则[刷新后出现商品个数,出现折扣商品个数]
	int ENDLESS_ARMORY_SHOP_QUANTITY = 2;
	// 无尽模式中需要后端处理的道具列表
	int SERVER_LOGIC_LIST = 3;
	// 在怪物减速大于等于30%后，无法掉落选定的道具
	int MONSTER_SLOW_DOWN = 4;
	// 使用该道具后，奖励掉落池改为更好掉落池
	int INCREASE_LUCK = 5;
	// 使用该道具后，关卡结束时随机奖励的道具增加到4
	int INCREASE_PROP = 6;
	// 整局游戏里存在获取次数限制的道具[道具id, 获取次数]
	int UNIQUE = 7;
	// 无尽模式中只生效一关得道具
	int ONCE_LEVEL_EFFECT = 8;
	// 关卡小结奖励数量
	int NUMBER_OF_AWARDS = 9;
	// 排行榜最大显示数量
	int RANK_COUNT = 10;
	// 每关是否固定掉落道具，如果是，那么掉落什么道具
	int TEST_IDENTICAL_PROP = 11;
	// 当升级效率大于80后无法选择的道具
	int TOWER_UPGRADE_LIMIT = 12;

}
