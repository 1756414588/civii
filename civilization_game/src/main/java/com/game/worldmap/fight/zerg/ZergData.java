package com.game.worldmap.fight.zerg;

import com.game.domain.p.Team;
import com.game.worldmap.March;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZergData {

	private int recordDate;//上次开启记录
	private long startTime;// 开启时间
	private long endTime;//结束时间
	private int monsterId;// bossID
	private int status;// 状态
	private int step;
    private int stepFinish;
	private long stepStartTime;
	private long stepEndTime;
	private List<Long> stepParam;
	private int cityId;// 城池ID
	private long awardTime;// 结束奖励发放时间
	private int openTimes;// 开启次数
	private Team team;//boss数据
	private List<March> marches;

}
