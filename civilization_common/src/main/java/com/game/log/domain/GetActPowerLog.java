package com.game.log.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description 体力补领功能的埋点
 * @ProjectName halo_server
 * @Date 2021/11/23 14:19
 **/
public class GetActPowerLog {

	private Date operationTime; // 时间
	private long lordId; // 角色id
	private String nick; // 角色昵称
	private Date firstLoginTime; // 玩家首次登录时间

	public GetActPowerLog(Date operationTime, long lordId, String nick, Date firstLoginTime) {
		this.operationTime = operationTime;
		this.lordId = lordId;
		this.nick = nick;
		this.firstLoginTime = firstLoginTime;
	}

	public GetActPowerLog() {
	}

	/*
	 * 时间戳传化为时间
	 */
	private String timeStamp2Date(Date time) {
		String format = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(time);
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(timeStamp2Date(operationTime)).append(",");
		stringBuffer.append(lordId).append(",");
		stringBuffer.append(nick).append(",");
		stringBuffer.append(timeStamp2Date((firstLoginTime)));
		return stringBuffer.toString();
	}

	public Date getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(Date operationTime) {
		this.operationTime = operationTime;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public Date getFirstLoginTime() {
		return firstLoginTime;
	}

	public void setFirstLoginTime(Date firstLoginTime) {
		this.firstLoginTime = firstLoginTime;
	}
}
