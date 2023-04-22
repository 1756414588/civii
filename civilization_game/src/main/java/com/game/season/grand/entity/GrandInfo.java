package com.game.season.grand.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;
import com.game.util.TimeHelper;

/**
 * 宏伟宝库
 */
public class GrandInfo extends BaseModule {

	private int state = 1;// 1.等待生成 2.可生成奖励 3.已生成奖励
	private long nextTime = TimeHelper.getMilTime(System.currentTimeMillis(), 0, 7, 23, 59, 59);;// 下次生成时间
	private Map<Integer, Map<Integer, Integer>> map = new ConcurrentHashMap<>();// 记录完成度
	private Map<Integer, CommonPb.Award> award = new ConcurrentHashMap<>();// 记录生成奖励 key index val award

	// public interface BaseGrandInfoTask {
	// void action(Map<Integer, StaticSeasonTreasury> map, GrandType type, int taskType, int count);
	// }
	//
	// private Map<GrandType, BaseGrandInfoTask> actionMap = new HashMap<>();
	//
	// public void addScore()

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_1;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			SeasonActivityPb.GrandInfoPb grandInfoPb = SeasonActivityPb.GrandInfoPb.parseFrom(seasonInfo.getInfo());
			this.state = grandInfoPb.getState();
			this.nextTime = grandInfoPb.getNextTime();
			List<CommonPb.ThreeInt> scoreMapList = grandInfoPb.getScoreMapList();
			scoreMapList.forEach(x -> {
				Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(x.getV1(), y -> new HashMap<>());
				integerIntegerMap.put(x.getV2(), x.getV3());
			});
			List<SeasonActivityPb.GrandAwardPb> awardInfoList = grandInfoPb.getAwardInfoList();
			awardInfoList.forEach(x -> {
				award.put(x.getId(), x.getAward());
			});
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.GrandInfoPb.Builder builder = SeasonActivityPb.GrandInfoPb.newBuilder();
		builder.setState(this.state);
		builder.setNextTime(this.nextTime);
		for (Map.Entry<Integer, Map<Integer, Integer>> entry : map.entrySet()) {
			Map<Integer, Integer> value = entry.getValue();
			for (Map.Entry<Integer, Integer> integerEntry : value.entrySet()) {
				CommonPb.ThreeInt.Builder builder1 = CommonPb.ThreeInt.newBuilder();
				builder1.setV1(entry.getKey());
				builder1.setV2(integerEntry.getKey());
				builder1.setV3(integerEntry.getValue());
				builder.addScoreMap(builder1);
			}
		}
		for (Map.Entry<Integer, CommonPb.Award> entry : award.entrySet()) {
			SeasonActivityPb.GrandAwardPb.Builder builder1 = SeasonActivityPb.GrandAwardPb.newBuilder();
			builder1.setId(entry.getKey());
			builder1.setAward(entry.getValue());
			builder.addAwardInfo(builder1);
		}
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {
		this.state = 1;
		map.clear();
		award.clear();
		this.nextTime = TimeHelper.getMilTime(System.currentTimeMillis(), 0, 7, 23, 59, 59);
	}

	@Override
	public void clean(int actId) {

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getNextTime() {
		return nextTime;
	}

	public void setNextTime(long nextTime) {
		this.nextTime = nextTime;
	}

	public Map<Integer, Map<Integer, Integer>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, Map<Integer, Integer>> map) {
		this.map = map;
	}

	public Map<Integer, CommonPb.Award> getAward() {
		return award;
	}

	public void setAward(Map<Integer, CommonPb.Award> award) {
		this.award = award;
	}

	public int getScore(int type, int id) {
		Map<Integer, Integer> integerIntegerMap = map.get(type);
		if (integerIntegerMap != null) {
			return integerIntegerMap.getOrDefault(id, 0);
		}
		return 0;
	}

	public CommonPb.Award getAward(int id) {

		return award.getOrDefault(id, null);
	}

	public int addScore(int type, int taskT, int count) {
		Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(type, x -> new HashMap<>());
		Integer merge = integerIntegerMap.merge(taskT, count, (a, b) -> a + b);
		return merge;
	}

}
