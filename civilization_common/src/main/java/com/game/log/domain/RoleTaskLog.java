package com.game.log.domain;

import lombok.Data;

/**
 * 2020年6月1日
 * 
 * @CaoBing halo_game RoleTaskLog.java
 **/
@Data
public class RoleTaskLog {
	// 玩家ID
	private long roleId;
	// 玩家区服
	private int server;
	// 玩家渠道
	private int channel;
	// 任务ID
	private int taskId;
	
	
	public RoleTaskLog() {
		super();
	}
	public RoleTaskLog(long roleId, int server, int channel, int taskId) {
		super();
		this.roleId = roleId;
		this.server = server;
		this.channel = channel;
		this.taskId = taskId;
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
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	@Override
	public String toString() {
		return "RoleTaskLog [roleId=" + roleId + ", server=" + server + ", channel=" + channel + ", taskId=" + taskId + "]";
	}
}
