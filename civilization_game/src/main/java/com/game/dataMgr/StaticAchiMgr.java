package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticAchiAwardBox;
import com.game.domain.s.StaticAchiInfo;
import com.game.domain.s.StaticAchievement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@LoadData(name = "成就数据配置")
public class StaticAchiMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticAchievement> staticAchievementMap = new HashMap<>();

	private Map<Integer, StaticAchievement> firstAchiMap = new HashMap<>();

	private Map<Integer, List<StaticAchievement>> listMap = new HashMap<>();


	private Map<Integer, StaticAchiInfo> achiInfoHashMap = new HashMap<>();

	private Map<Integer, StaticAchiAwardBox> achiAwardBoxHashMap = new HashMap<>();
	//private HashBasedTable<Integer, Integer, List<StaticAchiAwardBox>> boxAwardMap =HashBasedTable.create();
	private Map<Integer, Map<Integer, List<StaticAchiAwardBox>>> boxAwardMap = new HashMap<>();

	@Override
	public void load(){
		this.staticAchievementMap = staticDataDao.loadAchievement();
		this.firstAchiMap = staticAchievementMap.values().stream().filter(x -> x.getFirst() == 0).collect(Collectors.toMap(StaticAchievement::getId, Function.identity()));
		this.listMap = staticAchievementMap.values().stream().collect(Collectors.groupingBy(StaticAchievement::getGenre));

		this.achiInfoHashMap = staticDataDao.loadAchievementType();

		this.achiAwardBoxHashMap = staticDataDao.loadStaticAchiAwardBox();
	}

	@Override
	public void init() throws Exception {
		achiAwardBoxHashMap.values().forEach(x -> {
			Map<Integer, List<StaticAchiAwardBox>> integerListMap = boxAwardMap.computeIfAbsent(x.getType(), y -> new HashMap<>());
			List<StaticAchiAwardBox> staticAchiAwardBoxes = integerListMap.computeIfAbsent(x.getChildType(), z -> new ArrayList<>());
			staticAchiAwardBoxes.add(x);
		});
	}

	public StaticAchievement getStaticAchievementById(int id) {
		return staticAchievementMap.get(id);
	}

	public List<StaticAchievement> getStaticAchievementByType(int type) {
		return listMap.get(type);
	}


	public StaticAchiAwardBox getStaticAchiAwardBoxById(int id) {
		return achiAwardBoxHashMap.get(id);
	}

	public List<StaticAchiAwardBox> getStaticAchiAwardBoxList(int type, int childType) {
		Map<Integer, List<StaticAchiAwardBox>> integerListMap = boxAwardMap.getOrDefault(type, new HashMap<>());
		return integerListMap.get(childType);
	}


	public Map<Integer, StaticAchievement> getStaticAchievementMap() {
		return staticAchievementMap;
	}

	public void setStaticAchievementMap(Map<Integer, StaticAchievement> staticAchievementMap) {
		this.staticAchievementMap = staticAchievementMap;
	}

	public Map<Integer, StaticAchievement> getFirstAchiMap() {
		return firstAchiMap;
	}

	public void setFirstAchiMap(Map<Integer, StaticAchievement> firstAchiMap) {
		this.firstAchiMap = firstAchiMap;
	}

	public Map<Integer, List<StaticAchievement>> getListMap() {
		return listMap;
	}

	public void setListMap(Map<Integer, List<StaticAchievement>> listMap) {
		this.listMap = listMap;
	}

	public Map<Integer, StaticAchiInfo> getAchiInfoHashMap() {
		return achiInfoHashMap;
	}

	public void setAchiInfoHashMap(Map<Integer, StaticAchiInfo> achiInfoHashMap) {
		this.achiInfoHashMap = achiInfoHashMap;
	}

	public Map<Integer, StaticAchiAwardBox> getAchiAwardBoxHashMap() {
		return achiAwardBoxHashMap;
	}

	public void setAchiAwardBoxHashMap(Map<Integer, StaticAchiAwardBox> achiAwardBoxHashMap) {
		this.achiAwardBoxHashMap = achiAwardBoxHashMap;
	}

	public Map<Integer, Map<Integer, List<StaticAchiAwardBox>>> getBoxAwardMap() {
		return boxAwardMap;
	}

	public void setBoxAwardMap(Map<Integer, Map<Integer, List<StaticAchiAwardBox>>> boxAwardMap) {
		this.boxAwardMap = boxAwardMap;
	}
}
