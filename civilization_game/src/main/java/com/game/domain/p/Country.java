package com.game.domain.p;

/**
 * 国家存盘信息
 */
public class Country {
	private int countryId;
	private int level;
	private long exp;
	private String announcement;
	private String publisher;
	private long taskTime;
	private long voteTime;
	private long voteHour;
	private int voteState;
	private int appoint;
	private long rankTime;
	private byte[] hero;
	private byte[] daily;
	private byte[] govern;
	private byte[] tempGovern;//(官员选举期间的临时官员数据)
	private byte[] glory;
	private byte[] countryRank;
	private byte[] countryHero;
	private int soldierNum;
	private int killNum;
	private String countryName;
	private int checkState; //1 审核中 2 审核通过
	private int modifyTime;// 1 审核的次数
	private long modifyPlayer;//上一次更改的玩家id
	private String modifyName ;//更改的名字
	
	public Country() {
		super();
	}

	public Country(int countryId,int level) {
		super();
		this.countryId = countryId;
		this.level = level;
	}

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	public String getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public long getVoteTime() {
		return voteTime;
	}

	public void setVoteTime(long voteTime) {
		this.voteTime = voteTime;
	}

	public long getTaskTime() {
		return taskTime;
	}

	public void setTaskTime(long taskTime) {
		this.taskTime = taskTime;
	}

	public long getVoteHour() {
		return voteHour;
	}

	public void setVoteHour(long voteHour) {
		this.voteHour = voteHour;
	}

	public int getVoteState() {
		return voteState;
	}

	public void setVoteState(int voteState) {
		this.voteState = voteState;
	}

	public int getAppoint() {
		return appoint;
	}

	public void setAppoint(int appoint) {
		this.appoint = appoint;
	}

	public long getRankTime() {
		return rankTime;
	}

	public void setRankTime(long rankTime) {
		this.rankTime = rankTime;
	}

	public byte[] getHero() {
		return hero;
	}

	public void setHero(byte[] hero) {
		this.hero = hero;
	}

	public byte[] getDaily() {
		return daily;
	}

	public void setDaily(byte[] daily) {
		this.daily = daily;
	}

	public byte[] getGovern() {
		return govern;
	}

	public void setGovern(byte[] govern) {
		this.govern = govern;
	}

	public byte[] getGlory() {
		return glory;
	}

	public void setGlory(byte[] glory) {
		this.glory = glory;
	}

	public byte[] getCountryRank() {
		return countryRank;
	}

	public void setCountryRank(byte[] countryRank) {
		this.countryRank = countryRank;
	}

	public byte[] getCountryHero() {
		return countryHero;
	}

    public void setCountryHero(byte[] countryHero) {
        this.countryHero = countryHero;
    }

    public int getSoldierNum() {
        return soldierNum;
    }

    public void setSoldierNum(int soldierNum) {
        this.soldierNum = soldierNum;
    }

	public int getKillNum() {
		return killNum;
	}

	public void setKillNum(int killNum) {
		this.killNum = killNum;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public int getCheckState() {
		return checkState;
	}

	public void setCheckState(int checkState) {
		this.checkState = checkState;
	}

	public int getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(int modifyTime) {
		this.modifyTime = modifyTime;
	}

	public long getModifyPlayer() {
		return modifyPlayer;
	}

	public void setModifyPlayer(long modifyPlayer) {
		this.modifyPlayer = modifyPlayer;
	}

	public String getModifyName() {
		return modifyName;
	}

	public void setModifyName(String modifyName) {
		this.modifyName = modifyName;
	}

	public byte[] getTempGovern() {
		return tempGovern;
	}

	public void setTempGovern(byte[] tempGovern) {
		this.tempGovern = tempGovern;
	}
}
