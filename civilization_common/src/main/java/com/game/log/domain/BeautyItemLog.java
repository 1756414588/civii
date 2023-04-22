package com.game.log.domain;

import java.util.Date;

public class BeautyItemLog {
	private Date createDate; // 创建时间
	private long roleId; // 角色id
	private int type; //1亲密度 2魅力值 3经验值
	private int beautyId; //美女id
	private int changeNumber; //亲密度魅力值和经验值的变化数量
	private int itemId; //道具id
	private int itemAdd; //道具消耗数量
	private int itemSource; //消耗来源（亲密度：1.免费猜拳 2.半价猜拳 3.全价猜拳 4.赠送鲜花 5赠送钻戒；魅力值：1.赠送耳环 2.赠送香水；经验值：0为约会，消耗时填升级技能id）
	private int state; //产出/消耗 0产出 1消耗
	
	public BeautyItemLog(){}
	
public BeautyItemLog(long roleId, int type, int beautyId, int changeNumber, int itemId, int itemAdd, int itemSource, int state) {
		
		this.roleId = roleId;
		this.type = type;
		this.beautyId = beautyId;
		this.changeNumber = changeNumber;
		this.itemId = itemId;
		this.itemAdd = itemAdd;
		this.itemSource = itemSource;
		this.state = state;
	}
	
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getBeautyId() {
		return beautyId;
	}
	public void setBeautyId(int beautyId) {
		this.beautyId = beautyId;
	}
	public int getChangeNumber() {
		return changeNumber;
	}
	public void setChangeNumber(int changeNumber) {
		this.changeNumber = changeNumber;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getItemAdd() {
		return itemAdd;
	}
	public void setItemAdd(int itemAdd) {
		this.itemAdd = itemAdd;
	}
	public int getItemSource() {
		return itemSource;
	}
	public void setItemSource(int itemSource) {
		this.itemSource = itemSource;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "BeautyItemLog [createDate=" + createDate + ", roleId=" + roleId + ", type=" + type + ", beautyId=" + beautyId + ", changeNumber=" + changeNumber
				+ ", itemId=" + itemId + ", itemAdd=" + itemAdd + ", itemSource=" + itemSource + ", state=" + state + "]";
	}
	
	
	
	
}
