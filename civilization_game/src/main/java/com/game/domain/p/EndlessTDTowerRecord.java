package com.game.domain.p;

import com.game.pb.CommonPb.EndlessTDTowerPosRecord;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/15 13:32
 **/
public class EndlessTDTowerRecord {
	private int pos;
	private boolean isExist;
	private int towerType;
	private int towerId;
	private int lv;
	private int tempLv;

	public EndlessTDTowerRecord() {
	}

	public EndlessTDTowerRecord(int pos) {
		this.pos = pos;
		this.isExist = false;
		this.towerType = 0;
		this.towerId = 0;
		this.lv = 0;
		this.tempLv = 0;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public boolean isExist() {
		return isExist;
	}

	public void setExist(boolean exist) {
		isExist = exist;
	}

	public int getTowerType() {
		return towerType;
	}

	public void setTowerType(int towerType) {
		this.towerType = towerType;
	}

	public int getTowerId() {
		return towerId;
	}

	public void setTowerId(int towerId) {
		this.towerId = towerId;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getTempLv() {
		return tempLv;
	}

	public void setTempLv(int tempLv) {
		this.tempLv = tempLv;
	}

	@Override
	public String toString() {
		return "EndlessTDTowerRecord{" + "pos=" + pos + ", isExist=" + isExist + ", towerType=" + towerType + ", towerId=" + towerId + ", lv=" + lv + ", tempLv=" + tempLv + '}';
	}

	public EndlessTDTowerRecord(EndlessTDTowerPosRecord builder) {
		pos = builder.getPos();
		isExist = builder.getIsExist();
		if (isExist) {
			towerType = builder.getTowerType();
			towerId = builder.getTowerId();
			lv = builder.getLv();
			tempLv = builder.getTempLv();
		}
	}

	public EndlessTDTowerPosRecord wrapPb() {
		EndlessTDTowerPosRecord.Builder builder = EndlessTDTowerPosRecord.newBuilder();
		builder.setPos(pos);
		builder.setIsExist(isExist);
		if (!isExist) {
			return builder.build();
		}
		builder.setTowerType(towerType);
		builder.setTowerId(towerId);
		builder.setLv(lv);
		builder.setTempLv(tempLv);
		return builder.build();
	}
}
