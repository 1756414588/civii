package com.game.domain.p;

public class Friend {
	int type;// 好友类型
	long rolaId;// 好友id
	long applyTime;// 时间
	int level; // 创建关系时玩家等级
	int onceApprenticeLv = 0;// 解除师徒关系时徒弟的等级

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getRolaId() {
		return rolaId;
	}

	public void setRolaId(long rolaId) {
		this.rolaId = rolaId;
	}

	public long getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(long applyTime) {
		this.applyTime = applyTime;
	}

	public int getOnceApprenticeLv() {
		return onceApprenticeLv;
	}

	public void setOnceApprenticeLv(int onceApprenticeLv) {
		this.onceApprenticeLv = onceApprenticeLv;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Friend(int level, int type, long rolaId, long applyTime) {
		this.type = type;
		this.rolaId = rolaId;
		this.applyTime = applyTime;
		this.level = level;
	}

	public Friend(int type, long rolaId, long applyTime) {
		this.type = type;
		this.rolaId = rolaId;
		this.applyTime = applyTime;
	}

	public Friend(int type, long rolaId, long applyTime, int onceApprenticeLv, int level) {
		this.type = type;
		this.rolaId = rolaId;
		this.applyTime = applyTime;
		this.onceApprenticeLv = onceApprenticeLv;
		this.level = level;
	}

	public Friend() {
	}
}
