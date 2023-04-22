package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description机器人数据
 * @Date 2022/10/20 18:04
 **/

@Getter
@Setter
public class RobotData {

	private long keyId;
	private String account;
	private int accountKey;
	private String token;
	private int serverId;
	private int country;
	private String pos;
	private int commandLv;
	private int online;
	private long messageId;
	private int status;
	private long guildId;
	private int guildState;
	private int chatShow;
	private int attackCity;
	private int dailyDate;
	private int createDate;
	private int loginDate;
	private int logoutDate;
	private long lastSaveTime;
	private long loginCDTime;

	private String nick;
	private long roleId;
	private boolean flag = true;


	public boolean isSleep(int today, int maxGid, int maxDailyId) {
		if (createDate == today) {
			return guildId >= maxGid;
		} else {
			return dailyDate == today && messageId >= maxDailyId;
		}
	}

}
