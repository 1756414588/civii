package com.game.uc.log.domain;
/**
*2020年5月14日
*@CaoBing
*halo_uc
*AccountCreateLog.java
**/

import java.util.Date;

public class AccountCreateLog {
	//渠道
	private String channel;
	//创建帐号时间
	private Date operationTime;
	//帐号
	private String account;
	//帐号ID
	private int accountKey;
	//机型
	private String imodel;
	
	private String imei;
	
	private String ip;
	//手机CPU型号
	private String cpu;
	//登入设备UUID
	private String deviceUuid;
	
	private String idfa;
	
	public AccountCreateLog(String channel, Date operationTime, String account, String imodel, String imei, String ip,
			String cpu, String deviceUuid, String idfa,int accountKey) {
		super();
		this.channel = channel;
		this.operationTime = operationTime;
		this.account = account;
		this.imodel = imodel.replace(",",".");
		this.imei = imei.replace(",",".");
		this.ip = ip;
		this.cpu = cpu.replace(",",".");
		this.deviceUuid = deviceUuid.replace(",",".");
		this.idfa = idfa.replace(",",".");
		this.accountKey = accountKey;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Date getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(Date operationTime) {
		this.operationTime = operationTime;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public long getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(int accountKey) {
		this.accountKey = accountKey;
	}

	public String getImodel() {
		return imodel;
	}

	public void setImodel(String imodel) {
		this.imodel = imodel;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
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

	@Override
	public String toString() {
		return "AccountCreateLog [channel=" + channel + ", operationTime=" + operationTime + ", account=" + account
				+ ", accountKey=" + accountKey + ", imodel=" + imodel + ", imei=" + imei + ", ip=" + ip + ", cpu=" + cpu
				+ ", deviceUuid=" + deviceUuid + ", idfa=" + idfa + "]";
	}
}
