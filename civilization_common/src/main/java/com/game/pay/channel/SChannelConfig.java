package com.game.pay.channel;

/**
 * 2020年5月19日
 * 
 *    halo_common SChannelConfig.java
 **/
public class SChannelConfig {
	private int keyId;

	private int platType;

	private int gameChannelId;

	private String name;

	private String loginConfig;

	private String payConfig;

	private String packageName;

	private int is_review;

	private int parent_type;

	private String payIndef;

	private String teamNum;

	public int getIs_review() {
		return is_review;
	}

	public void setIs_review(int is_review) {
		this.is_review = is_review;
	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getPlatType() {
		return platType;
	}

	public void setPlatType(int platType) {
		this.platType = platType;
	}

	public int getGameChannelId() {
		return gameChannelId;
	}

	public void setGameChannelId(int gameChannelId) {
		this.gameChannelId = gameChannelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoginConfig() {
		return loginConfig;
	}

	public void setLoginConfig(String loginConfig) {
		this.loginConfig = loginConfig;
	}

	public String getPayConfig() {
		return payConfig;
	}

	public void setPayConfig(String payConfig) {
		this.payConfig = payConfig;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getParent_type() {
		return parent_type;
	}

	public void setParent_type(int parent_type) {
		this.parent_type = parent_type;
	}

	public String getPayIndef() {
		return payIndef;
	}

	public void setPayIndef(String payIndef) {
		this.payIndef = payIndef;
	}

	public String getTeamNum() {
		return teamNum;
	}

	public void setTeamNum(String teamNum) {
		this.teamNum = teamNum;
	}

	@Override
	public String toString() {
		return "SChannelConfig{" + "keyId=" + keyId + ", platType=" + platType + ", gameChannelId=" + gameChannelId + ", name='" + name + '\'' + ", loginConfig='" + loginConfig + '\'' + ", payConfig='" + payConfig + '\'' + ", packageName='" + packageName + '\'' + ", is_review=" + is_review + ", parent_type=" + parent_type + ", payIndef='" + payIndef + '\'' + '}';
	}
}
