package com.game.pay.channel;


import java.util.Date;

public class PlayerExist {

	private int id;
	private int accountKey;
	private int serverId;
	private int country;
	private String nick;
	private int level;
	private int portrait;
	private long lordId;
	private Date createDate;

	public PlayerExist(int accountKey, int serverId, int country) {
		this.accountKey = accountKey;
		this.serverId = serverId;
		this.country = country;
	}

	public PlayerExist() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(int accountKey) {
		this.accountKey = accountKey;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getPortrait() {
		return portrait;
	}

	public void setPortrait(int portrait) {
		this.portrait = portrait;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Override
	public String toString() {
		return "PlayerExist{" +
			"accountKey=" + accountKey +
			", serverId=" + serverId +
			", country=" + country +
			", nick='" + nick + '\'' +
			", level=" + level +
			", portrait=" + portrait +
			", lordId=" + lordId +
			", createDate=" + createDate +
			'}';
	}
}
