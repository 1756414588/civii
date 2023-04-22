package com.game.uc;

public class LoginParams {

	private String appVersion;
	private String account;
	private String token;
	private String packageName;
	private String imodel;
	private String channel;
	private String deviceUuid;
	private String imei;
	private String cpu;
	private String idfa;
	private String resolution;
	private String extend;
	private String versionFile;

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getImodel() {
		return imodel;
	}

	public void setImodel(String imodel) {
		this.imodel = imodel;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getDeviceUuid() {
		return deviceUuid;
	}

	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public String getVersionFile() {
		return versionFile;
	}

	public void setVersionFile(String versionFile) {
		this.versionFile = versionFile;
	}

	@Override
	public String toString() {
		return "LoginParams [appVersion=" + appVersion + ", account=" + account + ", token=" + token + ", packageName="
				+ packageName + ", imodel=" + imodel + ", channel=" + channel + ", deviceUuid=" + deviceUuid + ", imei="
				+ imei + ", cpu=" + cpu + ", idfa=" + idfa + ", resolution=" + resolution + ", extend=" + extend + "]";
	}
}
