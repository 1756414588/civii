package com.game.season.turn.entity;

import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 赛季转盘
 */
public class TurnInfo extends BaseModule {

	private int count;// 转动次数
	private long time = 0;// 上次转的时间
	private int totalCount;// 累计转动次数
	private Map<Integer, Integer> stateMap = new HashMap<>();

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_3;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			SeasonActivityPb.TurnInfoPb turnInfoPb = SeasonActivityPb.TurnInfoPb.parseFrom(seasonInfo.getInfo());
			this.count = turnInfoPb.getCount();
			this.time = turnInfoPb.getTime();
			this.totalCount = turnInfoPb.getTotalCount();
			List<CommonPb.TwoInt> stateMapList = turnInfoPb.getStateMapList();
			stateMapList.forEach(x -> {
				stateMap.put(x.getV1(), x.getV2());
			});
		} catch (Exception ex) {

		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.TurnInfoPb.Builder builder = SeasonActivityPb.TurnInfoPb.newBuilder();
		builder.setCount(this.count);
		builder.setTime(this.time);
		builder.setTotalCount(this.totalCount);
		for (Map.Entry<Integer, Integer> integerIntegerEntry : stateMap.entrySet()) {
			builder.addStateMap(CommonPb.TwoInt.newBuilder().setV1(integerIntegerEntry.getKey()).setV2(integerIntegerEntry.getValue()).build());
		}
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {
		this.time = System.currentTimeMillis();
		this.count = 0;
		this.totalCount = 0;
		this.stateMap.clear();
	}

	@Override
	public void clean(int actId) {

	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getReciveState(int index) {
		return stateMap.getOrDefault(index, 0);
	}

	public void updateRec(int index) {
		stateMap.put(index, 1);
	}

	public void addTotalCount() {
		this.totalCount += 1;
	}
}
