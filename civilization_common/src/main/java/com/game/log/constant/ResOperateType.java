package com.game.log.constant;

/**
 * 2020年4月27日
 * 
 * @CaoBing halo_game ResourceType.java
 * 
 * 资源产出和消耗的类型
 **/
public enum ResOperateType {
	
	RES_BUILDING_COLLECT_IN(1, "资源建筑征收产出资源"),

	WORLD_MOSTER_IN(2, "攻打世界怪物产出资源"),

	WORLD_COLLECT_IN(3, "采集世界资源产出资源"),

	SHOP_BUY_IN(4, "商店购买产出资源"),

	MISSION_DONE_IN(5, "副本推图产出资源"),

	RES_MISSION_IN(6, "资源副本产出资源"),

	TASK_AWARD_IN(7, "任务奖励产出资源"),

	ACT_AWARD_IN(8, "活动奖励产出资源"),
	
	SHOP_EXCHANGE_IN(9, "市场兑换产出资源"),

	MAIL_IN(10, "邮件奖励产出资源"),

	PROP_IN(11, "道具使用产出资源"),

	SHOP_FLIP_IN(12, "市场翻牌产出数量"),

	RECHARGE_IN(12, "充值产出数量"),

	WORLD_PROCESS_IN(13, "世界进程产出数量"),


	
	BUILDING_UP_OUT(1, "建筑升级消耗资源"),

	TEC_RESEARCH_OUT(2, "科技研究消耗资源"),

	WORKSHOP_MAKE_OUT(3, "作坊生产消耗资源"),

	MAKE_EQUIP_OUT(4, "生产装备消耗资源"),

	UP_PRO_SKILL_OUT(5, "提升产能消耗资源"),

	UP_TITLE_OUT(6, "晋升军衔消耗资源"),

	SHOP_BUY_OUT(7, "市场购买消耗资源"),

	SHOP_EXCHANGE_OUT(8, "市场兑换消耗资源"),

	SHOP_PACK_OUT(9, "市场打包消耗资源"),
	
	DRILL_OUT(10, "训练士兵消耗资源"),

	MARCH_OUT(11, "部队行军消耗资源"),
	
	UP_KILL_EQUIP_OUT(12, "神器升级消耗资源");

	
	private int infoType;

	private String desc;

	ResOperateType(int infoType, String desc) {

		this.infoType = infoType;
		this.desc = desc;
	}

	public int getInfoType() {
		return infoType;
	}

	public void setInfoType(int infoType) {
		this.infoType = infoType;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
