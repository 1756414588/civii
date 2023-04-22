package com.game.log.domain;

/**
 * @Description 新母巢之战占领相关log
 * @ProjectName halo_server
 * @Date 2021/9/14 23:48
 **/
public class NewBroodWarBattleLog {
	private long cityId; // 城市id
	private long beginTime; // 记录的开始时间
	private long endTime; // 记录的结束时间
	private int battleCount; // 战斗次数
	private int rank; // 当前母巢之战届数

	public NewBroodWarBattleLog() {
	}

	public NewBroodWarBattleLog(long cityId, long beginTime, long endTime, int battleCount, int rank) {
		this.cityId = cityId;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.battleCount = battleCount;
		this.rank = rank;
	}

	public long getCityId() {
		return cityId;
	}

	public void setCityId(long cityId) {
		this.cityId = cityId;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getBattleCount() {
		return battleCount;
	}

	public void setBattleCount(int battleCount) {
		this.battleCount = battleCount;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(cityId).append(",");
		stringBuffer.append(beginTime).append(",");
		stringBuffer.append(endTime).append(",");
		stringBuffer.append(battleCount).append(",");
		stringBuffer.append(rank).append(",");
		return stringBuffer.toString();
	}
}
