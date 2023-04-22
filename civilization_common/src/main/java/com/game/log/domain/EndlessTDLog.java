package com.game.log.domain;

import com.google.common.collect.HashBasedTable;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/27 9:35
 **/
@Getter
@Setter
@Builder
public class EndlessTDLog {
	private long lordId;// 玩家id
	private String nick; // 玩家昵称
	private int serverId; // 区服id
	private int fraction; // 玩家成绩
	private Map<Integer, Long> levelTime; // 玩家通关时间
	private HashBasedTable<Integer, Integer, Integer> levelFraction; // 玩家关卡分数
	private int startTime; // 挑战开始时间
	private int endTime; // 挑战结束时间

	public EndlessTDLog() {
	}

	public EndlessTDLog(long lordId, String nick, int serverId, int fraction, Map<Integer, Long> levelTime, HashBasedTable<Integer, Integer, Integer> levelFraction, int startTime, int endTime) {
		this.lordId = lordId;
		this.nick = nick;
		this.serverId = serverId;
		this.fraction = fraction;
		this.levelTime = levelTime;
		this.levelFraction = levelFraction;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(lordId).append(",").append(nick).append(",").append(serverId).append(",").append(fraction).append(",").append(levelTime.toString().replace(",", ";")).append(",").append(levelFraction.toString().replace(",", ";")).append(",").append(startTime).append(",").append(endTime).toString();

	}
}
