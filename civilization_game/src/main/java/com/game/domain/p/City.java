package com.game.domain.p;

import java.util.Arrays;

import com.game.worldmap.Pos;
public class City {
	private int cityId; // 城池Id
	private int country; // 国家
	private long lordId; // 城主
	private long endTime; // 城主任期结束时间
	private long protectedTime; // 城池保护时间
	private int isDestroyed; // 是否被攻破过
	private long lastSaveTime; // 上次存盘时间
	private byte[] electionData; // 选举的数据
	private long electionEndTime; // 选举的结束时间
	private byte[] monsterData; // 野怪兵力
	private long sendAwardTime; // 给城主发送邮件的结束时间
	private long makeItemTime; // 生产物品的结束时间
	private byte awardNum; // 生产的物品
	private byte[] warAttender; // 参战人员
	private int people; // 国家人口
	private long nextAttackTime; // 下一次禁卫军的攻击间隔
	private int state; // 名城状态: 0 正常生产 1.红色图纸生产中 2.红色图纸生产完成
	private long breakTime; // 城池攻破时间
	private int cityType;
	private int cityLv;
	private int exp;
	private String cityName;
	private boolean refreshProtected = true; // 本次保护罩结束服务器是否同步信息
	private long cityTime;
	private int flush = 1;
	private Pos pos;
	private int mapId;

	public boolean isRefreshProtected() {
		return refreshProtected;
	}

	public void setRefreshProtected(boolean refreshProtected) {
		this.refreshProtected = refreshProtected;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getProtectedTime() {
		return protectedTime;
	}

	public void setProtectedTime(long protectedTime) {
		this.protectedTime = protectedTime;
		setRefreshProtected(false);
	}

	public int getIsDestroyed() {
		return isDestroyed;
	}

	public void setIsDestroyed(int isDestroyed) {
		this.isDestroyed = isDestroyed;
	}

	public int getCityType() {
		return cityType;
	}

	public void setCityType(int cityType) {
		this.cityType = cityType;
	}

//    public static City clone(City city) {
//        City info = new City();
//        info.setCityId(city.getCityId());
//        info.setCountry(city.getCountry());
//        info.setLordId(city.getLordId());
//        info.setEndTime(city.getEndTime());
//        info.setProtectedTime(city.getProtectedTime());
//        info.setIsDestroyed(city.getIsDestroyed());
//        info.setLastSaveTime(city.getLastSaveTime());
//        byte[] srcElection = city.getElectionData();
//        if (srcElection != null) {
//            byte[] dstData = copyData(srcElection);
//            info.setElectionData(dstData);
//        }
//
//        info.setElectionEndTime(city.getElectionEndTime());
//        byte[] monsterData = city.getMonsterData();
//        if (monsterData != null) {
//            byte[] dstData = copyData(monsterData);
//            info.setMonsterData(dstData);
//        }
//
//        info.setSendAwardTime(city.getSendAwardTime());
//        info.setCityLv(city.getCityLv());
//        info.setMakeItemTime(city.getMakeItemTime());
//        info.setAwardNum(city.getAwardNum());
//        byte[] attenderData = city.getWarAttender();
//        if (attenderData != null) {
//            byte[] dstData = copyData(attenderData);
//            info.setWarAttender(dstData);
//        }
//        info.setPeople(city.getPeople());
//        info.setNextAttackTime(city.getNextAttackTime());
//        info.setState(city.getState());
//        info.setBreakTime(city.getBreakTime());
//        info.setCityType(city.cityType);
//        return info;
//    }

	public static byte[] copyData(byte[] srcData) {
		if (srcData == null) {
			return new byte[0];
		}
		byte[] dstData = new byte[srcData.length];
		System.arraycopy(srcData, 0, dstData, 0, srcData.length);
		return dstData;
	}

	public long getLastSaveTime() {
		return lastSaveTime;
	}

	public void setLastSaveTime(long lastSaveTime) {
		this.lastSaveTime = lastSaveTime;
	}

	public byte[] getElectionData() {
		return electionData;
	}

	public void setElectionData(byte[] electionData) {
		this.electionData = electionData;
	}

	public long getElectionEndTime() {
		return electionEndTime;
	}

	public void setElectionEndTime(long electionEndTime) {
		this.electionEndTime = electionEndTime;
	}

	public byte[] getMonsterData() {
		return monsterData;
	}

	public void setMonsterData(byte[] monsterData) {
		this.monsterData = monsterData;
	}

	public long getSendAwardTime() {
		return sendAwardTime;
	}

	public void setSendAwardTime(long sendAwardTime) {
		this.sendAwardTime = sendAwardTime;
	}

	public long getMakeItemTime() {
		return makeItemTime;
	}

	public void setMakeItemTime(long makeItemTime) {
		this.makeItemTime = makeItemTime;
	}

	public byte getAwardNum() {
		return awardNum;
	}

	public void setAwardNum(byte awardNum) {
		this.awardNum = awardNum;
	}

	public byte[] getWarAttender() {
		return warAttender;
	}

	public void setWarAttender(byte[] warAttender) {
		this.warAttender = warAttender;
	}

	public int getPeople() {
		return people;
	}

	public void setPeople(int people) {
		this.people = people;
	}

	public long getNextAttackTime() {
		return nextAttackTime;
	}

	public void setNextAttackTime(long nextAttackTime) {
		this.nextAttackTime = nextAttackTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getBreakTime() {
		return breakTime;
	}

	public void setBreakTime(long breakTime) {
		this.breakTime = breakTime;
	}

	public int getCityLv() {
		return cityLv;
	}

	public void setCityLv(int cityLv) {
		this.cityLv = cityLv;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public long getCityTime() {
		return cityTime;
	}

	public void setCityTime(long cityTime) {
		this.cityTime = cityTime;
	}

	public int getFlush() {
		return flush;
	}

	public void setFlush(int flush) {
		this.flush = flush;
	}

	public void reset() {
		this.country = 0;
		this.lordId = 0;
		this.endTime = System.currentTimeMillis();
		this.monsterData = null;
		//this.protectedTime = System.currentTimeMillis();
	}

	public Pos getPos() {
		return pos;
	}

	public void setPos(Pos pos) {
		this.pos = pos;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	@Override
	public String toString() {
		return "City{" + "cityId=" + cityId + ", country=" + country + ", lordId=" + lordId + ", endTime=" + endTime + ", protectedTime=" + protectedTime + ", isDestroyed=" + isDestroyed + ", lastSaveTime=" + lastSaveTime + ", electionData=" + Arrays.toString(electionData) + ", electionEndTime=" + electionEndTime + ", monsterData=" + Arrays.toString(monsterData) + ", sendAwardTime=" + sendAwardTime + ", cityLv=" + cityLv + ", makeItemTime=" + makeItemTime + ", awardNum=" + awardNum + ", warAttender=" + Arrays.toString(warAttender) + ", people=" + people + ", nextAttackTime=" + nextAttackTime + ", state=" + state + ", breakTime=" + breakTime + '}';
	}
}
