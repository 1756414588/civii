//package com.game.season;
//
//import com.game.domain.Player;
//import com.game.season.SeasonAct;
//import com.game.season.journey.entity.JourneyInfo;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class JourneyAct extends SeasonActivity {
//
//	private Map<Integer, AtomicInteger> journeyCampRank = new ConcurrentHashMap<>();
//	private Map<Player, JourneyInfo> journeyPesRank = new ConcurrentHashMap<>();
//
//	public void addJourneyScore(Player player, int score, JourneyInfo actRecord) {
//		journeyPesRank.put(player, actRecord);
//		AtomicInteger atomicInteger = journeyCampRank.computeIfAbsent(player.getCountry(), x -> new AtomicInteger(0));
//		atomicInteger.addAndGet(score);
//	}
//
//	@Override
//	public SeasonAct getType() {
//		return SeasonAct.ACT_2;
//	}
//}
