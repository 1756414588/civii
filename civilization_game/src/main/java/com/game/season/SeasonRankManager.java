package com.game.season;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.domain.Player;
import com.game.manager.PlayerManager;
import com.game.season.journey.entity.JourneyInfo;
import com.game.season.seven.entity.DayScore;
import com.game.season.seven.entity.SevenInfo;

@Component
public class SeasonRankManager {

	@Autowired
	PlayerManager playerManager;

	private Map<Integer, AtomicInteger> journeyCampRank = new ConcurrentHashMap<>();
	private Map<Long, JourneyInfo> journeyPesRank = new ConcurrentHashMap<>();

	private Map<Integer, Map<Long, DayScore>> hashSetMap = new ConcurrentHashMap<>();
	private Map<Long, DayScore> dayRank = new ConcurrentHashMap<>();

	public void initJourneyRank(JourneyInfo actRecord) {
		if (actRecord.getScore() > 0) {
			Player player = actRecord.getPlayer();
			journeyPesRank.put(player.getRoleId(), actRecord);
			// Player player = playerManager.getPlayer(actRecord.getRoleId());
			AtomicInteger atomicInteger = journeyCampRank.computeIfAbsent(player.getCountry(), x -> new AtomicInteger(0));
			atomicInteger.addAndGet(actRecord.getScore());
		}
	}

	public void addJourneyScore(Player player, int score, JourneyInfo actRecord) {
		journeyPesRank.put(player.getRoleId(), actRecord);
		AtomicInteger atomicInteger = journeyCampRank.computeIfAbsent(player.getCountry(), x -> new AtomicInteger(0));
		atomicInteger.addAndGet(score);
	}

	public Map<Integer, AtomicInteger> getJourneyCampRank() {
		return journeyCampRank;
	}

	public void setJourneyCampRank(Map<Integer, AtomicInteger> journeyCampRank) {
		this.journeyCampRank = journeyCampRank;
	}

	public Map<Long, JourneyInfo> getJourneyPesRank() {
		return journeyPesRank;
	}

	public void setJourneyPesRank(Map<Long, JourneyInfo> journeyPesRank) {
		this.journeyPesRank = journeyPesRank;
	}

	public void initSevenRank(SevenInfo sevenInfo) {
		Iterator<Map.Entry<Integer, Map<Integer, Integer>>> iterator = sevenInfo.getScore().entrySet().iterator();
		int totalScore = 0;
		Player player = sevenInfo.getPlayer();
		while (iterator.hasNext()) {
			int sc = 0;
			Map.Entry<Integer, Map<Integer, Integer>> next = iterator.next();
			Integer day = next.getKey();
			Map<Integer, Integer> value = next.getValue();
			for (Integer integer : value.values()) {
				sc += integer;
				totalScore += integer;
			}
			Map<Long, DayScore> longDayScoreMap = hashSetMap.computeIfAbsent(day, x -> new HashMap<>());
			DayScore dayScore = new DayScore(player, day, sc);
			longDayScoreMap.put(player.getRoleId(), dayScore);
		}
		if (totalScore > 0) {
			DayScore dayScore = new DayScore(player, 0, totalScore);
			dayRank.put(player.getRoleId(), dayScore);
		}
	}

	public void addSevenRank(int day, Player player, int score) {
		Map<Long, DayScore> longDayScoreMap = hashSetMap.computeIfAbsent(day, x -> new HashMap<>());
		DayScore dayScore = longDayScoreMap.computeIfAbsent(player.getRoleId(), x -> new DayScore(player, day, 0));
		dayScore.addScore(score);

		DayScore dayScore1 = dayRank.computeIfAbsent(player.getRoleId(), x -> new DayScore(player, day, 0));
		dayScore1.addScore(score);
	}

	public Map<Integer, Map<Long, DayScore>> getHashSetMap() {
		return hashSetMap;
	}

	public void setHashSetMap(Map<Integer, Map<Long, DayScore>> hashSetMap) {
		this.hashSetMap = hashSetMap;
	}

	public Map<Long, DayScore> getDayRank() {
		return dayRank;
	}

	public void setDayRank(Map<Long, DayScore> dayRank) {
		this.dayRank = dayRank;
	}

	public Map<Long, DayScore> getDayRank(int day) {
		return hashSetMap.computeIfAbsent(day, x -> new ConcurrentHashMap<>());
	}

}
