package com.game.flame.entity;

import java.util.List;

public class StaticFlameBuild {

	private int id;
	private int buildId;
	private String name;
	private int type;
	private List<List<Integer>> buff;
	private int x;
	private int y;
	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private long firstCamp;// 个人首占资源
	private long firstPerson;// 阵营首占资源
	private long continueCampTime;// 多长时间产出物质
	private long continueCampAmount;// 多长时间产出物质
	private long continuePersonTime;// 多长时间产出物质
	private long continuePersonAmount;// 多长时间产出物质
	private long occupyTime;// 攻占变占领所需时间
	private long protectTime;// 多长时间变成中立
	private long extraCamp;// 额外奖励
	private int occupyChat;
	private List<List<Integer>> award;// 产出物质
	private long  productionTime;
	private int safeId;
	private int level;
	private int protectChat;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBuildId() {
		return buildId;
	}

	public void setBuildId(int buildId) {
		this.buildId = buildId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<List<Integer>> getBuff() {
		return buff;
	}

	public void setBuff(List<List<Integer>> buff) {
		this.buff = buff;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

	public long getFirstCamp() {
		return firstCamp;
	}

	public void setFirstCamp(long firstCamp) {
		this.firstCamp = firstCamp;
	}

	public long getFirstPerson() {
		return firstPerson;
	}

	public void setFirstPerson(long firstPerson) {
		this.firstPerson = firstPerson;
	}

	public long getContinueCampTime() {
		return continueCampTime;
	}

	public void setContinueCampTime(long continueCampTime) {
		this.continueCampTime = continueCampTime;
	}

	public long getContinueCampAmount() {
		return continueCampAmount;
	}

	public void setContinueCampAmount(long continueCampAmount) {
		this.continueCampAmount = continueCampAmount;
	}

	public long getContinuePersonTime() {
		return continuePersonTime;
	}

	public void setContinuePersonTime(long continuePersonTime) {
		this.continuePersonTime = continuePersonTime;
	}

	public long getContinuePersonAmount() {
		return continuePersonAmount;
	}

	public void setContinuePersonAmount(long continuePersonAmount) {
		this.continuePersonAmount = continuePersonAmount;
	}

	public long getOccupyTime() {
		return occupyTime;
	}

	public void setOccupyTime(long occupyTime) {
		this.occupyTime = occupyTime;
	}

	public long getProtectTime() {
		return protectTime;
	}

	public void setProtectTime(long protectTime) {
		this.protectTime = protectTime;
	}

	public long getExtraCamp() {
		return extraCamp;
	}

	public void setExtraCamp(long extraCamp) {
		this.extraCamp = extraCamp;
	}

	public int getOccupyChat() {
		return occupyChat;
	}

	public void setOccupyChat(int occupyChat) {
		this.occupyChat = occupyChat;
	}

	public List<List<Integer>> getAward() {
		return award;
	}

	public void setAward(List<List<Integer>> award) {
		this.award = award;
	}

	public long getProductionTime() {
		return productionTime;
	}

	public void setProductionTime(long productionTime) {
		this.productionTime = productionTime;
	}

	public int getSafeId() {
		return safeId;
	}

	public void setSafeId(int safeId) {
		this.safeId = safeId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getProtectChat() {
		return protectChat;
	}

	public void setProtectChat(int protectChat) {
		this.protectChat = protectChat;
	}
}
