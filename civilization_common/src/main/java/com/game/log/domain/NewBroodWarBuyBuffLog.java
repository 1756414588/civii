package com.game.log.domain;

/**
 * @Description 新母巢buff购买log
 * @ProjectName halo_server
 * @Date 2021/9/14 23:42
 **/
public class NewBroodWarBuyBuffLog {
	private long lordId; // 角色id
	private int count; // 玩家第几次购买
	private int buffBuyType; // 购买类型 1:资源 2:钻石
	private int buffLv; // 本次购买后增益的等级
	private int vip; // 玩家vip等级
	private int rank;// 当前母巢之战届数
	private int buffType; // Buff类型
	private int gold; // 玩家剩余钻石

	public NewBroodWarBuyBuffLog() {
	}

	public NewBroodWarBuyBuffLog(long lordId, int count, int buffBuyType, int buffLv, int vip, int rank, int buffType, int gold) {
		this.lordId = lordId;
		this.count = count;
		this.buffBuyType = buffBuyType;
		this.buffLv = buffLv;
		this.vip = vip;
		this.rank = rank;
		this.buffType = buffType;
		this.gold = gold;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getBuffBuyType() {
		return buffBuyType;
	}

	public void setBuffBuyType(int buffBuyType) {
		this.buffBuyType = buffBuyType;
	}

	public int getBuffLv() {
		return buffLv;
	}

	public void setBuffLv(int buffLv) {
		this.buffLv = buffLv;
	}

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getBuffType() {
		return buffType;
	}

	public void setBuffType(int buffType) {
		this.buffType = buffType;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(lordId).append(",");
		stringBuffer.append(count).append(",");
		stringBuffer.append(buffBuyType).append(",");
		stringBuffer.append(buffLv).append(",");
		stringBuffer.append(vip).append(",");
		stringBuffer.append(rank).append(",");
		stringBuffer.append(buffType).append(",");
		stringBuffer.append(gold);
		return stringBuffer.toString();
	}
}
