package com.game.uc.log.domain;
/**
*2020年5月14日
*
*halo_uc
*AccountloginLog.java
**/

import java.util.Date;

public class AccountloginLog {
	// 渠道
	private String channel;
	// 版本ID
	private String version;
	// 服务器ID
	private int serverId;
	// 帐号ID
	private long accountId;
	// 登录时间
	private Date loginTime;
	// 登入IP
	private String loginIp;
	// 登入设备UUID
	private String deviceUuid;

	private String idfa;

	private String imei;

	private String imodel;

	private String resolution;
	
	private String cpu;

	public AccountloginLog(String channel, String version, int serverId, long accountId, Date loginTime, String loginIp,
			String deviceUuid, String idfa, String imei, String imodel, String resolution, String cpu) {
		super();
		this.channel = channel;
		this.version = version;
		this.serverId = serverId;
		this.accountId = accountId;
		this.loginTime = loginTime;
		this.loginIp = loginIp;
		this.deviceUuid = deviceUuid;
		this.idfa = idfa;
		this.imei = imei;
		this.imodel = imodel;
		this.resolution = resolution;
		this.cpu = cpu;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public String getDeviceUuid() {
		return deviceUuid;
	}

	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getImodel() {
		return imodel;
	}

	public void setImodel(String imodel) {
		this.imodel = imodel;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	@Override
	public String toString() {
		return "AccountloginLog [channel=" + channel + ", version=" + version + ", serverId=" + serverId
				+ ", accountId=" + accountId + ", loginTime=" + loginTime + ", loginIp=" + loginIp + ", deviceUuid="
				+ deviceUuid + ", idfa=" + idfa + ", imei=" + imei + ", imodel=" + imodel + ", resolution=" + resolution
				+ ", cpu=" + cpu + "]";
	}
}
