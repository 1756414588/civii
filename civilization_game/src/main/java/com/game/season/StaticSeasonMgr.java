package com.game.season;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.game.dao.s.StaticDataDao;
import com.game.season.grand.entity.StaticSeasonTreasury;
import com.game.season.hero.StaticComProf;
import com.game.season.hero.StaticComSkill;
import com.game.season.journey.entity.*;
import com.game.season.seven.entity.StaticSeasonSeven;
import com.game.season.seven.entity.StaticSeasonSevenAward;
import com.game.season.seven.entity.StaticSeasonSevenRank;
import com.game.season.seven.entity.StaticSeasonSevenType;
import com.game.season.talent.entity.StaticCompTalent;
import com.game.season.talent.entity.StaticCompTalentType;
import com.game.season.talent.entity.StaticCompTalentUp;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.dataMgr.BaseDataMgr;
import com.game.season.directgift.entity.StaticSeasonLimitGift;
import com.game.season.turn.entity.StaticTurn;
import com.game.season.turn.entity.StaticTurnAward;
import com.game.season.turn.entity.StaticTurnConfig;

@Component
public class StaticSeasonMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, List<StaticCompPlan>> comPlanList = new HashMap<>();
	// 赛季旅程
	private List<StaticSeasonJourney> seasonJourneyList = new ArrayList<>();
	private Map<Integer, List<StaticSeasonJourney>> seasonJourney = new HashMap<>();
	private Map<Integer, StaticSeasonJourney> staticSeasonJourneyHashMap = new HashMap<>();
	private Map<Integer, Map<Integer, List<StaticSeasonJourney>>> integerListHashMap = new HashMap<>();
	// private HashBasedTable<Integer, Integer, StaticSeasonJourney> integerListHashMap = HashBasedTable.create();

	private List<StaticJourneyAward> staticJourneyAwards = new ArrayList<>();
	private Map<Integer, List<StaticJourneyAward>> seasonJourneyAwardMap = new HashMap<>();
	private Map<Integer, StaticJourneyAward> staticJourneyAwardMap = new HashMap<>();

	private List<StaticJourneyCamp> staticJourneyCamps = new ArrayList<>();
	private Map<Integer, List<StaticJourneyCamp>> seasonJourneyCampMap = new HashMap<>();
	private Map<Integer, StaticJourneyCamp> staticJourneyCampMap = new HashMap<>();

	private List<StaticJourneyPerson> staticJourneyPeople = new ArrayList<>();
	private Map<Integer, List<StaticJourneyPerson>> seasonJourneyPersonMap = new HashMap<>();
	private Map<Integer, StaticJourneyPerson> staticJourneyPersonMap = new HashMap<>();

	private Map<Integer, HashBasedTable<Integer, Integer, StaticJourneyRankOfficer>> rankOfficerHashBasedTable = new HashMap<>();

	// 赛季转盘
	private Map<Integer, List<StaticTurn>> seasonTurnMap = new HashMap<>();
	private Map<Integer, StaticTurn> seasonTurnMaps = new HashMap<>();

	private Map<Integer, List<StaticTurnAward>> seasonTurnAwardMap = new HashMap<>();
	private Map<Integer, StaticTurnAward> seasonTurnAwardMaps = new HashMap<>();
	private StaticTurnConfig staticTurnConfig;

	// 直升礼包
	private HashBasedTable<Integer, Integer, StaticSeasonLimitGift> limitGiftHashBasedTable = HashBasedTable.create();
	private Map<Integer, StaticSeasonLimitGift> giftMap = new HashMap<>();

	// 赛季礼包
	// private HashBasedTable<Integer, Integer, StaticSeasonPayGift> seasonPayGiftHashBasedTable = HashBasedTable.create();
	// private Map<Integer, StaticSeasonPayGift> seasonPayGiftHashMap = new HashMap<>();
	// 七日
	private Map<Integer, Map<Integer, Map<Integer, StaticSeasonSeven>>> sevenMap = new HashMap<>();
	private Map<Integer, StaticSeasonSevenType> staticSeasonSevenTypeMap = new HashMap<>();

	private Map<Integer, Map<Integer, List<StaticSeasonSevenAward>>> sevenAwardMap = new HashMap<>();
	private Map<Integer, StaticSeasonSevenAward> staticSeasonSevenAwardHashMap = new HashMap<>();

	private Map<Integer, Map<Integer, List<StaticSeasonSevenRank>>> sevenRankMap = new HashMap<>();

	// 宏伟宝库
	private Map<Integer, StaticSeasonTreasury> staticSeasonTreasuryMap = new HashMap<>();
	private HashBasedTable<Integer, Integer, StaticSeasonTreasury> treasuryMap = HashBasedTable.create();

	// 英雄技能
	private HashBasedTable<Integer, Integer, StaticComSkill> skillHashBasedTable = HashBasedTable.create();
	private Map<Integer, StaticComSkill> staticComSkillMap = new HashMap<>();
	// 英雄军职
	private Map<Integer, StaticComProf> staticComProfHashMap = new HashMap<>();

	// 赛季天赋
	private StaticCompTalent staticCompTalent;
	private Map<Integer, StaticCompTalentUp> staticCompTalentUpMap = new HashMap<>();
	private Map<Integer, List<StaticCompTalentUp>> integerListMap = new HashMap<>();

	private Map<Integer, StaticCompTalentType> staticCompTalentTypeMap = new HashMap<>();

	@Override
	public void init() throws Exception {
		initPlan();
		initJourneyConfig();
		initTurnConfig();
		initSeasonLimitGift();
		// seasonPayGiftHashMap();
		initSeasonSeven();
		initTreasury();
		initSkill();
		initSeasonTalent();
	}

	public void initPlan() {
		comPlanList.clear();
		List<StaticCompPlan> staticCompPlans = staticDataDao.loadComPlan();
		staticCompPlans.forEach(x -> {
			List<StaticCompPlan> staticCompPlans1 = comPlanList.computeIfAbsent(x.getMergeNum(), a -> new ArrayList<>());
			staticCompPlans1.add(x);
		});
	}

	public void initJourneyConfig() {
		// 赛季旅程
		this.seasonJourneyList = staticDataDao.loadSeasonJourney();
		this.seasonJourney = seasonJourneyList.stream().collect(Collectors.groupingBy(e -> e.getAwardId()));
		staticSeasonJourneyHashMap = seasonJourneyList.stream().collect(Collectors.toMap(StaticSeasonJourney::getId, Function.identity()));
		// integerListHashMap = seasonJourneyList.stream().collect(Collectors.groupingBy(e -> e.getIsIniTask()));
		seasonJourneyList.forEach(x -> {
			Map<Integer, List<StaticSeasonJourney>> integerListMap = integerListHashMap.computeIfAbsent(x.getAwardId(), y -> new HashMap<>());
			List<StaticSeasonJourney> staticSeasonJourneys = integerListMap.computeIfAbsent(x.getIsIniTask(), z -> new ArrayList<>());
			staticSeasonJourneys.add(x);
		});

		this.staticJourneyAwards = staticDataDao.loadJourneyAward();
		staticJourneyAwardMap = staticJourneyAwards.stream().collect(Collectors.toMap(StaticJourneyAward::getId, Function.identity()));
		seasonJourneyAwardMap = staticJourneyAwards.stream().collect(Collectors.groupingBy(e -> e.getAwardId()));

		this.staticJourneyCamps = staticDataDao.loadJourneyCamp();
		staticJourneyCampMap = staticJourneyCamps.stream().collect(Collectors.toMap(StaticJourneyCamp::getId, Function.identity()));
		seasonJourneyCampMap = staticJourneyCamps.stream().collect(Collectors.groupingBy(e -> e.getAwardId()));

		this.staticJourneyPeople = staticDataDao.loadJourneyPerson();
		staticJourneyPersonMap = staticJourneyPeople.stream().collect(Collectors.toMap(StaticJourneyPerson::getId, Function.identity()));
		seasonJourneyPersonMap = staticJourneyPeople.stream().collect(Collectors.groupingBy(e -> e.getAwardId()));

		List<StaticJourneyRankOfficer> staticJourneyRankOfficers = staticDataDao.loadJourneyRankOff();
		staticJourneyRankOfficers.forEach(x -> {
			HashBasedTable<Integer, Integer, StaticJourneyRankOfficer> integerIntegerStaticJourneyRankOfficerHashBasedTable = rankOfficerHashBasedTable.computeIfAbsent(x.getAwardId(), y -> HashBasedTable.create());
			integerIntegerStaticJourneyRankOfficerHashBasedTable.put(x.getRank(), x.getType(), x);
		});

	}

	public void initTurnConfig() {
		// 赛季转盘
		List<StaticTurn> staticTurns = staticDataDao.loadStaticTurn();
		this.seasonTurnMap = staticTurns.stream().collect(Collectors.groupingBy(e -> e.getAwardId()));
		this.seasonTurnMaps = staticTurns.stream().collect(Collectors.toMap(StaticTurn::getId, Function.identity()));

		List<StaticTurnAward> staticTurnAwards = staticDataDao.loadStaticTurnAward();
		this.seasonTurnAwardMap = staticTurnAwards.stream().collect(Collectors.groupingBy(e -> e.getAwardId()));
		this.seasonTurnAwardMaps = staticTurnAwards.stream().collect(Collectors.toMap(StaticTurnAward::getId, Function.identity()));
		staticTurnConfig = staticDataDao.loadStaticTurnConfig();
	}

	public void initSeasonLimitGift() {
		this.giftMap = staticDataDao.loadStaticSeasonLimitGift();
		giftMap.values().forEach(x -> {
			limitGiftHashBasedTable.put(x.getAwardId(), x.getKeyId(), x);
		});

	}

	// public void seasonPayGiftHashMap() {
	// this.seasonPayGiftHashMap = staticDataDao.loadStaticSeasonPayGift();
	// seasonPayGiftHashMap.values().forEach(x -> {
	// seasonPayGiftHashBasedTable.put(x.getAwardId(), x.getPayMoneyId(), x);
	// });
	// }

	public void initSeasonSeven() {
		List<StaticSeasonSeven> staticSeasonSevens = staticDataDao.loadStaticSeasonSeven();
		staticSeasonSevens.forEach(x -> {
			Map<Integer, Map<Integer, StaticSeasonSeven>> integerMapMap = sevenMap.computeIfAbsent(x.getAwardId(), y -> new HashMap<>());
			Map<Integer, StaticSeasonSeven> integerStaticSeasonSevenMap = integerMapMap.computeIfAbsent(x.getDay(), z -> new HashMap<>());
			integerStaticSeasonSevenMap.put(x.getTaskType(), x);
		});

		List<StaticSeasonSevenType> staticSeasonSevenTypes = staticDataDao.loadStaticSeasonSevenType();
		this.staticSeasonSevenTypeMap = staticSeasonSevenTypes.stream().collect(Collectors.toMap(StaticSeasonSevenType::getTaskType, Function.identity()));

		this.staticSeasonSevenAwardHashMap = staticDataDao.loadStaticSeasonSevenAward();
		staticSeasonSevenAwardHashMap.values().forEach(x -> {
			Map<Integer, List<StaticSeasonSevenAward>> integerListMap = sevenAwardMap.computeIfAbsent(x.getAwardId(), y -> new HashMap<>());
			List<StaticSeasonSevenAward> staticSeasonSevenAwards1 = integerListMap.computeIfAbsent(x.getDay(), z -> new ArrayList<>());
			staticSeasonSevenAwards1.add(x);
		});

		List<StaticSeasonSevenRank> staticSeasonSevenRanks = staticDataDao.loadStaticSeasonSevenRank();
		staticSeasonSevenRanks.forEach(x -> {
			Map<Integer, List<StaticSeasonSevenRank>> integerListMap = sevenRankMap.computeIfAbsent(x.getAwardId(), y -> new HashMap<>());
			List<StaticSeasonSevenRank> staticSeasonSevenRanks1 = integerListMap.computeIfAbsent(x.getRankType(), z -> new ArrayList<>());
			staticSeasonSevenRanks1.add(x);
		});

	}

	public void initTreasury() {
		this.staticSeasonTreasuryMap = staticDataDao.loadStaticSeasonTreasury();
		staticSeasonTreasuryMap.values().forEach(x -> {
			treasuryMap.put(x.getAwardId(), x.getTaskId(), x);
		});
	}

	public void initSkill() {
		staticComSkillMap = staticDataDao.loadStaticSeasonSkill();
		staticComSkillMap.values().forEach(x -> {
			skillHashBasedTable.put(x.getType(), x.getSkillLv(), x);
		});
		this.staticComProfHashMap = staticDataDao.loadStaticComProf();

	}

	public void initSeasonTalent() {
		staticCompTalent = staticDataDao.loadStaticCompTalent();
		staticCompTalentUpMap = staticDataDao.loadStaticCompTalentUp();
		staticCompTalentTypeMap = staticDataDao.loadStaticCompTalentType();

		staticCompTalentUpMap.values().stream().filter(x -> x.getNextId() == 0 && x.getChildType() == 1).forEach(x -> {
			List<StaticCompTalentUp> staticCompTalentUps = integerListMap.computeIfAbsent(x.getTalentType(), y -> new ArrayList<>());
			staticCompTalentUps.add(x);
		});

	}

	// 赛季旅程
	public List<StaticCompPlan> getPlanList(int season) {
		return comPlanList.get(season);
	}

	public StaticSeasonJourney getStaticSeasonJourney(int id) {
		return staticSeasonJourneyHashMap.get(id);
	}

	public StaticJourneyAward getStaticJourneyAward(int id) {
		return staticJourneyAwardMap.get(id);
	}

	public List<StaticSeasonJourney> getSeasonJourneyList(int awardId) {
		return seasonJourney.get(awardId);
	}

	public List<StaticJourneyAward> getStaticJourneyAwards(int awardId) {
		return seasonJourneyAwardMap.get(awardId);
	}

	// 赛季转盘
	public StaticTurnConfig getStaticTurnConfig() {
		return staticTurnConfig;
	}

	public List<StaticTurn> getStaticTurns(int awardId) {
		return seasonTurnMap.get(awardId);
	}

	public List<StaticTurnAward> getStaticTurnAwards(int awardId) {
		return seasonTurnAwardMap.get(awardId);
	}

	public StaticTurnAward getStaticTurnAward(int id) {
		return seasonTurnAwardMaps.get(id);
	}

	public StaticTurn getRandStaticTurns(int awardId) {
		List<StaticTurn> staticTurns = seasonTurnMap.get(awardId);
		int i = new Random().nextInt(10000);
		int count = 0;
		for (StaticTurn staticTurn : staticTurns) {
			count += staticTurn.getWeight();
			if (i < count) {
				return staticTurn;
			}
		}
		return null;
	}

	public Map<Integer, StaticSeasonLimitGift> getGiftMap(int awardId) {
		return limitGiftHashBasedTable.row(awardId);
	}

	public StaticSeasonLimitGift getGiftById(int id) {
		return giftMap.get(id);
	}

	// public Map<Integer, StaticSeasonPayGift> getPayGiftMap(int awardId) {
	// return seasonPayGiftHashBasedTable.row(awardId);
	// }
	// public StaticSeasonPayGift getPayGiftById(int id) {
	// return seasonPayGiftHashMap.get(id);
	// }
	public Map<Integer, StaticSeasonSeven> getStaticSeasonSeven(int awardId, int day) {
		Map<Integer, Map<Integer, StaticSeasonSeven>> integerMapMap = sevenMap.get(awardId);
		if (integerMapMap != null) {
			return integerMapMap.get(day);
		}
		return null;
	}

	public StaticSeasonSevenType getStaticSeasonSevenType(int taskType) {
		return staticSeasonSevenTypeMap.get(taskType);
	}

	public List<StaticSeasonSevenAward> getSevenAwardList(int awardId, int day) {
		Map<Integer, List<StaticSeasonSevenAward>> integerListMap = sevenAwardMap.get(awardId);
		if (integerListMap != null) {
			return integerListMap.get(day);
		}
		return null;
	}

	public StaticSeasonSevenAward getSevenAward(int id) {
		return staticSeasonSevenAwardHashMap.get(id);
	}

	public StaticJourneyPerson getStaticJourneyPerson(int awardId, int index) {
		List<StaticJourneyPerson> staticJourneyPeople = seasonJourneyPersonMap.get(awardId);
		if (staticJourneyPeople != null) {
			for (int i = 0; i < staticJourneyPeople.size(); i++) {
				StaticJourneyPerson staticJourneyPerson = staticJourneyPeople.get(i);
				if (index < staticJourneyPerson.getRank()) {
					return staticJourneyPeople.get(i);
				}
			}
		}
		return null;
	}

	public Map<Integer, StaticSeasonTreasury> getStaticSeasonTreasury(int awardId) {
		return treasuryMap.row(awardId);
	}

	public StaticComSkill getSkill(int id) {
		return staticComSkillMap.get(id);
	}

	//
	public StaticComSkill getSkillByTypeAndLevel(int type, int level) {
		return skillHashBasedTable.get(type, level);
	}

	public StaticComProf getStaticComProf(int id) {
		return staticComProfHashMap.get(id);
	}

	public List<StaticSeasonSevenRank> getSevenRack(int awardId, int rankType) {
		Map<Integer, List<StaticSeasonSevenRank>> integerListMap = sevenRankMap.get(awardId);
		if (integerListMap != null) {
			return integerListMap.get(rankType);
		}
		return null;
	}

	public List<StaticSeasonJourney> getSeasonJourneyListByIsInTask(int awardId) {
		Map<Integer, List<StaticSeasonJourney>> integerListMap = integerListHashMap.get(awardId);
		if (integerListMap != null) {
			return integerListMap.get(1);
		}
		return null;
	}

	public StaticCompTalent getStaticCompTalent() {
		return staticCompTalent;
	}

	public StaticCompTalentType getStaticCompTalentType(int type) {
		return staticCompTalentTypeMap.get(type);
	}

	public StaticCompTalentUp getStaticCompTalentUp(int id) {
		return staticCompTalentUpMap.get(id);
	}

	public List<StaticCompTalentUp> getStaticCompTalentUps(int talentType) {
		return integerListMap.get(talentType);
	}

	public StaticJourneyRankOfficer getRankOff(int awardId, int rank, int type) {
		HashBasedTable<Integer, Integer, StaticJourneyRankOfficer> officerHashBasedTable = rankOfficerHashBasedTable.get(awardId);
		if (officerHashBasedTable == null) {
			return null;
		}
		return officerHashBasedTable.get(rank, type);
	}

}