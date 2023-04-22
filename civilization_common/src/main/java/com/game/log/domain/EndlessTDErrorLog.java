package com.game.log.domain;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/30 11:06
 **/
@Getter
@Setter
@Builder
public class EndlessTDErrorLog {
	private long lordId;// 玩家id
	private String nick; // 玩家昵称
	private int serverId; // 区服id
	private int wave; // 波次
	private int levelId; // 关卡实际id
	private int levelFraction; // 关卡上报分数
	private int maxLevelFraction; // 关卡最大分数
	private long levelTime; // 玩家通关时间
	private long limitTime; // 限制的最小通关时间
	private int newLifePoint; // 客户端上报的血量
	private int lifePoint; // 开始本关时的血量
	private String errorType; //1:分数异常 2:时间异常 3:血量异常   [1,2,3]
	private int startTime; // 挑战开始时间

	public EndlessTDErrorLog() {
	}

	public EndlessTDErrorLog(long lordId, String nick, int serverId, int wave, int levelId, int levelFraction, int maxLevelFraction, long levelTime,
		long limitTime, int newLifePoint, int lifePoint, String errorType, int startTime) {
		this.lordId = lordId;
		this.nick = nick;
		this.serverId = serverId;
		this.wave = wave;
		this.levelId = levelId;
		this.levelFraction = levelFraction;
		this.maxLevelFraction = maxLevelFraction;
		this.levelTime = levelTime;
		this.limitTime = limitTime;
		this.newLifePoint = newLifePoint;
		this.lifePoint = lifePoint;
		this.errorType = errorType;
		this.startTime = startTime;
	}

	@Override
	public String toString() {
		return new StringBuffer().append(lordId).append(",")
			.append(nick).append(",")
			.append(serverId).append(",")
			.append(wave).append(",")
			.append(levelId).append(",")
			.append(levelFraction).append(",")
			.append(maxLevelFraction).append(",")
			.append(levelTime).append(",")
			.append(limitTime).append(",")
			.append(newLifePoint).append(",")
			.append(lifePoint).append(",")
			.append(errorType.replace(",", ";")).append(",")
			.append(startTime).toString();
	}
}
