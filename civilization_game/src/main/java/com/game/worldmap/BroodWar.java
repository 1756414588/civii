package com.game.worldmap;

import com.game.constant.BroodWarState;
import com.game.domain.p.BroodWarData;
import com.game.domain.p.BroodWarReport;
import com.game.domain.p.Team;
import com.game.pb.DataPb;
import com.game.timer.TimerEvent;
import com.game.util.LogHelper;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zcp
 * @date 2021/6/17 14:59 母巢之战实体类
 */
@Getter
@Setter
public class BroodWar extends CityInfo {

	/**
	 * 普通进攻队列
	 */
	private LinkedList<Team> attackQueue = new LinkedList();

	/**
	 * 优先防守
	 */
//	private LinkedList<Team> defenceQueue = new LinkedList();

	/**
	 * 当前状态
	 */
	private volatile BroodWarState state;

	/**
	 * 下次攻击间隔时间
	 */
	private long nextAttackTime;

	/**
	 * 本次活动结束时间
	 */
	private long endTime;

	/**
	 * 防守阵营
	 */
	private int defenceCountry;

	/**
	 * 活动定时器
	 */
	private TimerEvent event;

	/**
	 * 购买增益开启时间
	 */
	private long openBuyBuffTime;

	/**
	 * 战报信息
	 */
	private List<BroodWarReport> reports = new LinkedList<>();

	/**
	 * 当前是第几届
	 */
	private int rank;
	/**
	 * 上次赢了的阵营
	 */
	private int lastCountry;

	/**
	 * 独裁者用户ID
	 */
	private long dictator;

	/**
	 * 个阵营占领时间  时长/3
	 */
	private Map<Integer, Integer> occupyTime = new HashMap<>();

	/**
	 * 个阵营占领进度
	 */
	private Map<Integer, Integer> occupyPercentage = new HashMap<>();


	/**
	 * 记录时间戳
	 */
	private Map<Integer, Long> occupyPercentageTime = new HashMap<>();

	/**
	 * 开战前
	 */
	public void beforeStart() {
		reports.clear();
		attackQueue.clear();
//		defenceQueue.clear();
	}

	/**
	 * 结束后
	 */
	public void afterEnd() {
		occupyTime.clear();
		occupyPercentage.clear();
		occupyPercentageTime.clear();
	}


	/**
	 * 重新加载数据
	 *
	 * @param data
	 */
	public void reLoadData(BroodWarData data) {
		if (data == null) {
			return;
		}
		this.state = BroodWarState.get(data.getState());
		this.nextAttackTime = data.getNextAttackTime();
		this.endTime = data.getEndTime();
		this.defenceCountry = data.getDefenceCountry();
		this.openBuyBuffTime = data.getOpenBuyBuffTime();
		this.rank = data.getRank();
		this.lastCountry = data.getLastCountry();
		this.dictator = data.getDictator();
		try {
			DataPb.TeamList teamList = DataPb.TeamList.parseFrom(data.getAttackQueue());
			teamList.getTeamsList().stream().forEach(e -> {
				Team team = new Team();
				team.unWrapPb(e);
				this.attackQueue.add(team);
			});
		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error("load broodWarData error cause:{}", e.getMessage(), e);
		}
//		try {
//			DataPb.TeamList teamList = DataPb.TeamList.parseFrom(data.getDefenceQueue());
//			teamList.getTeamsList().stream().forEach(e -> {
//				Team team = new Team();
//				team.unWrapPb(e);
//				this.defenceQueue.add(team);
//			});
//		} catch (InvalidProtocolBufferException e) {
//			LogHelper.ERROR_LOGGER.error("load broodWarData error");
//		}
		try {
			DataPb.TwoIntList dataList = DataPb.TwoIntList.parseFrom(data.getOccupyTime());
			dataList.getTwoIntsList().stream().forEach(e -> {
				this.occupyTime.put(e.getV1(), e.getV2());
			});
		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error("load broodWarData error cause:{}", e.getMessage(), e);
		}
		try {
			if (data.getOccupyPercentage() != null) {
				DataPb.TwoIntList dataList = DataPb.TwoIntList.parseFrom(data.getOccupyPercentage());
				dataList.getTwoIntsList().stream().forEach(e -> {
					this.occupyPercentage.put(e.getV1(), e.getV2());
				});
			}
		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error("load broodWarData error cause:{}", e.getMessage(), e);
		}

	}
}
