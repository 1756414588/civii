package com.game.server;

import com.game.util.XProperties;
import org.springframework.stereotype.Component;

@Component
public class AppPropertes {

	private int port;
	private String gateIp;
	private int gatePort;
	public String accountServerUrl;
	public boolean ready;

	private boolean robotAuto;
	private boolean recordCmd;

	/**
	 * 读取配置文件
	 *
	 * @param properties
	 */
	public void readConfig(XProperties properties) {
		this.port = properties.getInteger("robot.port", 9501);
		this.gateIp = properties.getString("gate.ip", "127.0.0.1");
		this.gatePort = properties.getInteger("gate.port", 7777);
		this.accountServerUrl = properties.getString("account_server_url", "http://192.168.2.100:7777");
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getGateIp() {
		return gateIp;
	}

	public void setGateIp(String gateIp) {
		this.gateIp = gateIp;
	}

	public int getGatePort() {
		return gatePort;
	}

	public void setGatePort(int gatePort) {
		this.gatePort = gatePort;
	}

	public String getAccountServerUrl() {
		return accountServerUrl;
	}

	public void setAccountServerUrl(String accountServerUrl) {
		this.accountServerUrl = accountServerUrl;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isRobotAuto() {
		return robotAuto;
	}

	public void setRobotAuto(boolean robotAuto) {
		this.robotAuto = robotAuto;
	}

	public boolean isRecordCmd() {
		return recordCmd;
	}

	public void setRecordCmd(boolean recordCmd) {
		this.recordCmd = recordCmd;
	}
}
