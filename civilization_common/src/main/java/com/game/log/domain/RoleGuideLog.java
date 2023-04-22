package com.game.log.domain;

import lombok.Data;

/**
 * 2020年6月1日
 * 
 * @CaoBing halo_game RoleGuideLog.java
 **/
@Data
public class RoleGuideLog {
	// 玩家ID
	private long roleId;
	// 玩家区服
	private int server;
	// 玩家渠道
	private int channel;
	// 新手引导步骤ID
	private int guideKey;
	
	public RoleGuideLog() {
		super();
	}

	public RoleGuideLog(long roleId, int server, int channel, int guideKey) {
		super();
		this.roleId = roleId;
		this.server = server;
		this.channel = channel;
		this.guideKey = guideKey;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getServer() {
		return server;
	}

	public void setServer(int server) {
		this.server = server;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getGuideKey() {
		return guideKey;
	}

	public void setGuideKey(int guideKey) {
		this.guideKey = guideKey;
	}

	@Override
	public String toString() {
		return "RoleGuideLog [roleId=" + roleId + ", server=" + server + ", channel=" + channel + ", guideKey=" + guideKey + "]";
	}
}
