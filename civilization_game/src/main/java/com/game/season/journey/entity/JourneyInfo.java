package com.game.season.journey.entity;

import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;
import com.game.season.SeasonRankManager;
import com.game.spring.SpringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 赛季旅程
 */
public class JourneyInfo extends BaseModule {

	/**
	 * 活跃
	 */
	private int active;

	private int score;

	/**
	 * 任务完成状态
	 */
	private Map<Integer, Integer> taskCount = new HashMap<>();
	/**
	 * 任务领取状态
	 */
	private Map<Integer, Integer> taskState = new HashMap<>();
	/**
	 * 活跃奖励领取状态
	 */
	private Map<Integer, Integer> activeState = new HashMap<>();

	/**
	 * 当前展示得活动
	 */
	private Map<Integer, Integer> completeInfoMap = new HashMap<>();

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public Map<Integer, Integer> getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(Map<Integer, Integer> taskCount) {
		this.taskCount = taskCount;
	}

	public Map<Integer, Integer> getTaskState() {
		return taskState;
	}

	public void setTaskState(Map<Integer, Integer> taskState) {
		this.taskState = taskState;
	}

	public Map<Integer, Integer> getActiveState() {
		return activeState;
	}

	public void setActiveState(Map<Integer, Integer> activeState) {
		this.activeState = activeState;
	}

	private Map<JourneyType, BaseJourneyTask> actionMap = new HashMap<>();

	public JourneyInfo() {
		actionMap.put(JourneyType.HERO, this::set);
		actionMap.put(JourneyType.UP_LEVEL, this::set);
		actionMap.put(JourneyType.RESEARCH_TECH, this::set);
		actionMap.put(JourneyType.COMP, this::add);
		actionMap.put(JourneyType.SKILL_LEVEL, this::set);
		actionMap.put(JourneyType.AWARD, this::add);
	}

	public int getNeedTime(int key) {
		if (taskCount.containsKey(key)) {
			return taskCount.get(key);
		}
		return 0;
	}

	public int getTaskState(int key) {
		if (taskState.containsKey(key)) {
			return taskState.get(key);
		}
		return 0;
	}

	public int getActiveState(int key) {
		if (activeState.containsKey(key)) {
			return activeState.get(key);
		}
		return 0;
	}

	public void addScore(int score) {
		this.score += score;
	}

	public void addActive() {
		this.active += 1;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_2;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			byte[] info = seasonInfo.getInfo();
			SeasonActivityPb.JourneyInfoPb journeyInfoPb = SeasonActivityPb.JourneyInfoPb.parseFrom(info);
			this.score = journeyInfoPb.getScore();
			this.active = journeyInfoPb.getActive();
			List<CommonPb.TwoInt> taskCountList = journeyInfoPb.getTaskCountList();
			if (taskCountList != null) {
				taskCountList.forEach(x -> {
					taskCount.put(x.getV1(), x.getV2());
				});
			}
			List<CommonPb.TwoInt> taskStateList = journeyInfoPb.getTaskStateList();
			if (taskCountList != null) {
				taskStateList.forEach(x -> {
					taskState.put(x.getV1(), x.getV2());
				});
			}
			List<CommonPb.TwoInt> activeStateList = journeyInfoPb.getActiveStateList();
			if (activeStateList != null) {
				activeStateList.forEach(x -> {
					activeState.put(x.getV1(), x.getV2());
				});
			}

			List<CommonPb.TwoInt> completeInfoMapList = journeyInfoPb.getCompleteInfoMapList();
			if (completeInfoMapList != null) {
				completeInfoMapList.forEach(x -> {
					completeInfoMap.put(x.getV1(), x.getV2());
				});
			}
			SpringUtil.getBean(SeasonRankManager.class).initJourneyRank(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.JourneyInfoPb.Builder builder = SeasonActivityPb.JourneyInfoPb.newBuilder();
		builder.setActive(this.active);
		builder.setScore(this.score);
		taskCount.entrySet().forEach(x -> {
			builder.addTaskCount(CommonPb.TwoInt.newBuilder().setV1(x.getKey()).setV2(x.getValue()).build());
		});
		taskState.entrySet().forEach(x -> {
			builder.addTaskState(CommonPb.TwoInt.newBuilder().setV1(x.getKey()).setV2(x.getValue()).build());
		});
		activeState.entrySet().forEach(x -> {
			builder.addActiveState(CommonPb.TwoInt.newBuilder().setV1(x.getKey()).setV2(x.getValue()).build());
		});
		completeInfoMap.entrySet().forEach(x -> {
			builder.addCompleteInfoMap(CommonPb.TwoInt.newBuilder().setV1(x.getKey()).setV2(x.getValue()).build());
		});
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {
		this.score = 0;
		this.active = 0;
		this.activeState.clear();
		this.taskState.clear();
		this.taskCount.clear();
	}

	@Override
	public void clean(int actId) {

	}

	public void update(List<StaticSeasonJourney> seasonJourneyList, int count, JourneyType type, int heroId) {
		BaseJourneyTask baseJourneyTask = actionMap.get(type);
		if (baseJourneyTask != null) {
			baseJourneyTask.action(seasonJourneyList, count, type, heroId);
		}
	}

	public interface BaseJourneyTask {
		void action(List<StaticSeasonJourney> seasonJourneyList, int count, JourneyType taskId, int heroId);
	}

	private void set(List<StaticSeasonJourney> seasonJourneyList, int count, JourneyType taskId, int heroId) {
		seasonJourneyList.forEach(x -> {
			if (x.getType() == taskId.get()) {
				List<Integer> heroId1 = x.getHeroId();
				if (heroId1 != null && heroId1.get(0) == heroId && count >= heroId1.get(1)) {
					this.taskCount.put(x.getId(), 1);
				}
			}
		});
	}

	private void add(List<StaticSeasonJourney> seasonJourneyList, int count, JourneyType taskId, int heroId) {
		seasonJourneyList.forEach(x -> {
			if (x.getType() == taskId.get()) {
				getTaskCount().merge(x.getId(), count, (a, b) -> a + b);
			}
		});
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JourneyInfo)) {
			return false;
		}
		JourneyInfo pairo = (JourneyInfo) o;
		return getPlayer() == pairo.getPlayer();
	}

	public Map<Integer, Integer> getCompleteInfoMap() {
		return completeInfoMap;
	}

	public void setCompleteInfoMap(Map<Integer, Integer> completeInfoMap) {
		this.completeInfoMap = completeInfoMap;
	}

	public Map<JourneyType, BaseJourneyTask> getActionMap() {
		return actionMap;
	}

	public void setActionMap(Map<JourneyType, BaseJourneyTask> actionMap) {
		this.actionMap = actionMap;
	}
}
