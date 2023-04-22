package com.game.season.talent.entity;

public enum EffectType {

	EFFECT_TYPE_1(1, "上阵英雄行军速度增加（百分比）"), // 完成

	EFFECT_TYPE2(2, "上阵英雄攻城属性增加攻击属性百分比（百分比）"),//完成

	EFFECT_TYPE3(3, "上阵英雄防御属性增加（固定数值）"),//增加基础数值 完成

	EFFECT_TYPE8(8, "城战减免体力（固定数值）"),// 完成

	EFFECT_TYPE9(9, "城战中，损兵恢复（百分比）"),//完成

	EFFECT_TYPE10(10, "城战时，强攻造成的伤害提高（百分比）"),//完成

	EFFECT_TYPE11(11, "城战时，5到8排受到的首次非技能伤害变为1"),//完成  //英雄的5-8排

	EFFECT_TYPE12(12, "训练士兵消耗食物减少（百分比）"),//完成

	EFFECT_TYPE13(13, "被驻防时，前来驻防的英雄到达后攻击增加（百分比）"),//面板属性 增加 //完成

	EFFECT_TYPE14(14, "被驻防时，前来驻防的英雄行军速度增加（百分比）"),//完成

	EFFECT_TYPE15(15, "城墙上的禁卫军攻击属性增加（百分比）"), //面板属性 增加 完成

	EFFECT_TYPE16(16, "城墙上的禁卫军防御属性增加（固定数值）"),////面板属性 增加 完成

	EFFECT_TYPE17(17, "战火燎原增益中，上阵英雄攻击增益效果增加（百分比）"),// 增益*（1+x） 完成

	EFFECT_TYPE18(18, "母巢争霸增益中，上阵英雄攻击增益效果增加（百分比）"), //增益*（1+x） 完成

	EFFECT_TYPE19(19, "沙盘演武积分获取数量增加（百分比）"),//完成

	EFFECT_TYPE20(20, "母巢争霸积分获取数量增加（百分比）"),//完成

	EFFECT_TYPE21(21, "战火燎原积分获取数量增加（百分比）"),//todo 部分完成，邮件得改

	EFFECT_TYPE22(22, "雇佣佃农/高级佃农的资源增长效果增加（百分比）"),//完成

	EFFECT_TYPE23(23, "雇佣铁匠减免武器打造的时间增加（固定数值）"),//完成

	EFFECT_TYPE24(24, "雇佣学者减免科技研究的时间增加（固定数值）"),//完成

	EFFECT_TYPE25(25, "建筑免费加速时间增加（百分比）"),//完成

	EFFECT_TYPE26(26, "市场兑换资源单次冷却时间降低（固定数值）"),//完成

	EFFECT_TYPE27(27, "采集获得资源增加（百分比）"),//完成

	EFFECT_TYPE28(28, "每天第一次免费购买体力时可额外获得（固定数值）"),//完成

	EFFECT_TYPE29(29, "仓库保护基础容量增加（百分比）"),//完成

	EFFECT_TYPE30(30, "船坞生产队列增加（固定数值）"),//完成

	EFFECT_TYPE31(31, "船坞贸易概率获得材料（百分比）（固定数值）"),//[31,100,1]//完成

	EFFECT_TYPE32(32, "步兵营募兵数增加（固定数值）"),//[32,1,100]//完成

	//EFFECT_TYPE33(33, "弓兵营募兵数增加（固定数值））"),
	//
	//EFFECT_TYPE34(34, "骑兵营募兵数增加（固定数值）"),

	//EFFECT_TYPE35(35, "预备兵营募兵数增加（固定数值）"),

	EFFECT_TYPE36(36, "与敌对指挥官作战伤害值提升（百分比）"),//每次攻击都要算//完成

	EFFECT_TYPE37(37, "与敌对指挥官作战受到的伤害值减少（百分比）"),//完成

	EFFECT_TYPE38(38, "与敌对指挥官作战兵种克制效果提升（百分比）"),//完成

	;

	private int effId;
	private String desc;

	EffectType(int effId, String desc) {
		this.effId = effId;
		this.desc = desc;
	}

	public int getEffId() {
		return effId;
	}

	public void setEffId(int effId) {
		this.effId = effId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
