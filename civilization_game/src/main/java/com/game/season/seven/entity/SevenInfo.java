package com.game.season.seven.entity;

import com.game.domain.Player;
import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;
import com.game.season.SeasonRankManager;
import com.game.spring.SpringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 赛季七日
 */
public class SevenInfo extends BaseModule {

	private Map<Integer, Map<Integer, Integer>> score = new HashMap<>();

	private int totalScore;

	private Map<Integer, Integer> state = new HashMap<>();// 领取状态

	private Map<Integer, Integer> total = new HashMap<>();// 每日总的

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_4;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			SeasonActivityPb.SevenInfoPb sevenInfoPb = SeasonActivityPb.SevenInfoPb.parseFrom(seasonInfo.getInfo());
			this.totalScore = sevenInfoPb.getScore();
			List<CommonPb.ThreeInt> stateMapList = sevenInfoPb.getScoreMapList();
			stateMapList.forEach(x -> {
				Map<Integer, Integer> integerIntegerMap = score.computeIfAbsent(x.getV1(), y -> new HashMap<>());
				integerIntegerMap.put(x.getV2(), x.getV3());
			});
			List<CommonPb.TwoInt> stateMapList1 = sevenInfoPb.getStateMapList();
			stateMapList1.forEach(x -> {
				state.put(x.getV1(), x.getV2());
			});
			List<CommonPb.TwoInt> totalList = sevenInfoPb.getTotalList();
			totalList.forEach(x -> {
				total.put(x.getV1(), x.getV2());
			});
			SpringUtil.getBean(SeasonRankManager.class).initSevenRank(this);
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.SevenInfoPb.Builder builder = SeasonActivityPb.SevenInfoPb.newBuilder();
		builder.setScore(this.totalScore);
		for (Map.Entry<Integer, Map<Integer, Integer>> integerMapEntry : score.entrySet()) {
			Map<Integer, Integer> value = integerMapEntry.getValue();
			for (Map.Entry<Integer, Integer> integerIntegerEntry : value.entrySet()) {
				CommonPb.ThreeInt.Builder builder1 = CommonPb.ThreeInt.newBuilder();
				builder1.setV1(integerMapEntry.getKey());
				builder1.setV2(integerIntegerEntry.getKey());
				builder1.setV3(integerIntegerEntry.getValue());
				builder.addScoreMap(builder1);
			}
		}
		Iterator<Map.Entry<Integer, Integer>> iterator = state.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer> next = iterator.next();
			builder.addStateMap(CommonPb.TwoInt.newBuilder().setV1(next.getKey()).setV2(next.getValue()).build());
		}

		for (Map.Entry<Integer, Integer> integerIntegerEntry : total.entrySet()) {
			builder.addTotal(CommonPb.TwoInt.newBuilder().setV1(integerIntegerEntry.getKey()).setV2(integerIntegerEntry.getValue()).build());
		}
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {
		score.clear();
		this.totalScore = 0;
		state.clear();
		total.clear();
	}

	@Override
	public void clean(int actId) {

	}

	public void addScore(Player player, int day, int taskType, int sc) {
		Map<Integer, Integer> integerIntegerMap = score.computeIfAbsent(day, x -> new HashMap<>());
		integerIntegerMap.merge(taskType, sc, (a, b) -> a + b);
		this.totalScore += sc;
		total.merge(day, sc, (a, b) -> a + b);
		SpringUtil.getBean(SeasonRankManager.class).addSevenRank(day, player, sc);
	}

	public int getScore(int day, int taskType) {
		Map<Integer, Integer> integerIntegerMap = score.computeIfAbsent(day, x -> new HashMap<>());
		return integerIntegerMap.getOrDefault(taskType, 0);
	}

	public Map<Integer, Map<Integer, Integer>> getScore() {
		return score;
	}

	public void setScore(Map<Integer, Map<Integer, Integer>> score) {
		this.score = score;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public Map<Integer, Integer> getState() {
		return state;
	}

	public void setState(Map<Integer, Integer> state) {
		this.state = state;
	}

	public int getDayTotalScore(int day) {
		return total.getOrDefault(day, 0);
	}

	public boolean isGet(int id) {
		Integer orDefault = state.getOrDefault(id, 0);
		return orDefault == 1;
	}

	public void updateState(int id) {
		state.put(id, 1);
	}

	public int getState(int id) {
		return state.getOrDefault(id, 0);
	}

	public Map<Integer, Integer> getTotal() {
		return total;
	}

	public void setTotal(Map<Integer, Integer> total) {
		this.total = total;
	}
}
