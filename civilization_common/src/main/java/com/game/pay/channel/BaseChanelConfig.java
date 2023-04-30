package com.game.pay.channel;

/**
 * 2020年5月19日
 *
 *    halo_common
 * BaseChanelConfig.java
 **/
public class BaseChanelConfig {
    //主键ID
    public int keyId;
    //渠道
    public int platType;
    //渠道的AppId
    public String gameChannelId;
    //备注名称
    public String name;
    //
    public String packageName;

    //是否评审 0非 1是
	public int is_review;

    public int parent_type;

    protected String teamNum;

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

    public String getGameChannelId() {
        return gameChannelId;
    }

    public void setGameChannelId(String gameChannelId) {
        this.gameChannelId = gameChannelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

	public int getIs_review() {
		return is_review;
	}

	public void setIs_review(int is_review) {
		this.is_review = is_review;
	}

    public int getParent_type() {
        return parent_type;
    }

    public void setParent_type(int parent_type) {
        this.parent_type = parent_type;
    }

    public String getTeamNum() {
        return teamNum;
    }

    public void setTeamNum(String teamNum) {
        this.teamNum = teamNum;
    }

    @Override
    public String toString() {
        return "BaseChanelConfig{" +
                "keyId=" + keyId +
                ", platType=" + platType +
                ", gameChannelId='" + gameChannelId + '\'' +
                ", name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", is_review=" + is_review +
                ", parent_type=" + parent_type +
                '}';
    }
}
