package com.game.domain.s;

import java.util.List;

/**
 * 2020年6月6日
 * 
 *    halo_game StaticBeautyKills.java
 **/
public class StaticBeautySkills {
	private int keyId;// '索引ID',
	private int isUp;// '是否为可升级技能',
	private int skillType;// '技能的类型',
	private int skillLv;// '技能等级',
	private int expCost;// '技能升级消耗的经验',
	private String name;// '技能名',
	private Integer openCondition;// '开启条件',
	private List<Integer> effectValue;// '技能效果',
	private String upgradeLevelDesc;// '升级描述',
	private String upgradeEffectDesc;// '升级效果描述',
	private int beautyId;// '持有美女ID',
	private List<Integer> award;// '对应的奖励',
	public int getKeyId() {
		return keyId;
	}
	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}
	public int getIsUp() {
		return isUp;
	}
	public void setIsUp(int isUp) {
		this.isUp = isUp;
	}
	public int getSkillType() {
		return skillType;
	}
	public void setSkillType(int skillType) {
		this.skillType = skillType;
	}
	public int getSkillLv() {
		return skillLv;
	}
	public void setSkillLv(int skillLv) {
		this.skillLv = skillLv;
	}
	public int getExpCost() {
		return expCost;
	}
	public void setExpCost(int expCost) {
		this.expCost = expCost;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getOpenCondition() {
		return openCondition;
	}
	public void setOpenCondition(Integer openCondition) {
		this.openCondition = openCondition;
	}
	public List<Integer> getEffectValue() {
		return effectValue;
	}
	public void setEffectValue(List<Integer> effectValue) {
		this.effectValue = effectValue;
	}
	public String getUpgradeLevelDesc() {
		return upgradeLevelDesc;
	}
	public void setUpgradeLevelDesc(String upgradeLevelDesc) {
		this.upgradeLevelDesc = upgradeLevelDesc;
	}
	public String getUpgradeEffectDesc() {
		return upgradeEffectDesc;
	}
	public void setUpgradeEffectDesc(String upgradeEffectDesc) {
		this.upgradeEffectDesc = upgradeEffectDesc;
	}
	public int getBeautyId() {
		return beautyId;
	}
	public void setBeautyId(int beautyId) {
		this.beautyId = beautyId;
	}
	public List<Integer> getAward() {
		return award;
	}
	public void setAward(List<Integer> award) {
		this.award = award;
	}
	@Override
	public String toString() {
		return "StaticBeautySkills [keyId=" + keyId + ", isUp=" + isUp + ", skillType=" + skillType + ", skillLv=" + skillLv + ", expCost=" + expCost
				+ ", name=" + name + ", openCondition=" + openCondition + ", effectValue=" + effectValue + ", upgradeLevelDesc=" + upgradeLevelDesc
				+ ", upgradeEffectDesc=" + upgradeEffectDesc + ", beautyId=" + beautyId + ", award=" + award + "]";
	}
}
