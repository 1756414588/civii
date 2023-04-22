package com.game.log.domain;

import lombok.Data;

/**
*2020年6月1日
*@CaoBing
*halo_game
*RoleMissonLog.java
**/
@Data
public class RoleMissonLog {
	//玩家角色ID
	private long roleId;
	//区服ID
	private int server;
	//渠道
	private int channel;
	//关卡ID
	private int missonId;
	
	
	public RoleMissonLog() {
		super();
	}
	public RoleMissonLog(long roleId, int server, int channel, int missonId) {
		super();
		this.roleId = roleId;
		this.server = server;
		this.channel = channel;
		this.missonId = missonId;
	}
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
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
	public int getMissonId() {
		return missonId;
	}
	public void setMissonId(int missonId) {
		this.missonId = missonId;
	}
	@Override
	public String toString() {
		return "RoleMissonLog [roleId=" + roleId + ", server=" + server + ", channel=" + channel + ", missonId=" + missonId + "]";
	}
}
