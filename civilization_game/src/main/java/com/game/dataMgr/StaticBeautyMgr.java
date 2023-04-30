package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 2020年5月29日
 *
 *    halo_game StaticBeautyMgr.java
 **/
@Component
@LoadData(name = "战斗数据")
public class StaticBeautyMgr extends BaseDataMgr {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StaticDataDao staticDataDao;

	// 美女的基础信息
	private List<StaticBeautyBase> allBeautyBaseList = new ArrayList<>();
	private Map<Integer, StaticBeautyBase> beautyBasesMap = new HashMap<>();

	// 美女星级技能信息
	private List<StaticBeautyDateSkills> allBeautyStarSkillList = new ArrayList<>();
	private Map<Integer, StaticBeautyDateSkills> starSkillMap = new HashMap<>(); // 技能id为主键
	private Map<Integer, List<StaticBeautyDateSkills>> beautyStarSkillMap = new HashMap<>(); // 美女id为map主键

	// 美女亲密度技能信息
	private List<StaticBeautyDateSkills> allBeautyDateSkillList = new ArrayList<>();
	private Map<Integer, StaticBeautyDateSkills> dateSkillMap = new HashMap<>(); // 技能id为map主键
	private Map<Integer, List<StaticBeautyDateSkills>> beautyDateSkillMap = new HashMap<>(); // 美女id为map主键

	// 美女约会奖励
	private List<StaticBeautyDate> allBeautyDate = new ArrayList<>();
	private Map<Integer, StaticBeautyDate> allBeautyDateMap = new HashMap<>(); // 星级为map主键

	// 美女约会奖励映射
	private List<StaticBeautyDateAward> allBeautyDateAward = new ArrayList<>();
	private Map<Integer, StaticBeautyDateAward> allBeautyDateAwardMap = new HashMap<>(); // 奖励id为map主键

	@Override
	public void load() throws Exception {
		clearConfig();
		initBaseBeauty();
		initBeautySkills();
		iniBeautyDate();
		allBeautyStarSkillList.clear();
		allBeautyDateSkillList.clear();
		allBeautyDate.clear();
		allBeautyDateAward.clear();
	}

	@Override
	public void init() throws Exception {
	}

	private void clearConfig() {
		// 美女的基础信息
		allBeautyBaseList.clear();
		beautyBasesMap.clear();
		// 美女星级技能信息
		allBeautyStarSkillList.clear();
		starSkillMap.clear();
		beautyStarSkillMap.clear();
		// 美女亲密度技能信息
		allBeautyDateSkillList.clear();
		dateSkillMap.clear();
		beautyDateSkillMap.clear();
		// 美女约会奖励信息
		allBeautyDate.clear();
		allBeautyDateAward.clear();
		// 美女约会奖励映射
		allBeautyDateAward.clear();
		allBeautyDateAwardMap.clear();
	}

	// 初始化美女的基础配置信息
	private void initBaseBeauty() {
		this.beautyBasesMap = staticDataDao.selectStaticBeautyBases();
	}

	// 初始化美女的技能信息
	private void initBeautySkills() {
		allBeautyStarSkillList = staticDataDao.selectStaticBeautyStarSkills();
		allBeautyStarSkillList.forEach(e -> {
			starSkillMap.put(e.getId(), e);
			List<StaticBeautyDateSkills> staticBeautyDateSkills = beautyStarSkillMap.computeIfAbsent(e.getBeautyId(), x -> new ArrayList<>());
			staticBeautyDateSkills.add(e);
		});

		allBeautyDateSkillList = staticDataDao.selectStaticBeautyDateSkills();
		allBeautyDateSkillList.forEach(e -> {
			dateSkillMap.put(e.getId(), e);
			List<StaticBeautyDateSkills> staticBeautyDateSkills = beautyDateSkillMap.computeIfAbsent(e.getBeautyId(), x -> new ArrayList<>());
			staticBeautyDateSkills.add(e);

		});
	}

	// 初始化美女的约会奖励
	private void iniBeautyDate() {
		allBeautyDate = staticDataDao.selectStaticBeautyDate();
		allBeautyDate.forEach(e -> {
			allBeautyDateMap.put(e.getStar(), e);

		});
		allBeautyDateAward = staticDataDao.selectStaticBeautyDateAward();
		allBeautyDateAward.forEach(e -> {
			allBeautyDateAwardMap.put(e.getId(), e);

		});
	}

	// 获取展示美女列表
	public List<StaticBeautyBase> getAllBeautyBaseList() {
		//return this.allBeautyBaseList;
		return new ArrayList<>(beautyBasesMap.values());
	}

	// 获取美女信息
	public StaticBeautyBase getStaticBeautyBase(int beautyId) {
		if (beautyBasesMap.size() > 0) {
			return beautyBasesMap.get(beautyId);
		} else {
			logger.error("staticBeauty is not exist : staticBeauty {}", beautyId);
		}
		return null;
	}

	// 获取单个星级技能信息
	public StaticBeautyDateSkills getStaticStarSkills(int beautyId, int star) {
		List<StaticBeautyDateSkills> staticBeautyDateSkills = beautyStarSkillMap.get(beautyId);
		if (staticBeautyDateSkills == null || staticBeautyDateSkills.isEmpty()) {
			return null;
		}
		for (StaticBeautyDateSkills staticBeautyDateSkill : staticBeautyDateSkills) {
			if (staticBeautyDateSkill.getStar() == star) {
				return staticBeautyDateSkill;
			}
		}
		return null;
	}

	// 获取美女对应的星级技能信息
	public List<StaticBeautyDateSkills> getStaticBeautStarSkills(int beautyId) {
		List<StaticBeautyDateSkills> list = beautyStarSkillMap.get(beautyId);
		return list == null ? null : list.stream().sorted(Comparator.comparingInt(x -> x.getNeedNum())).collect(Collectors.toList());
	}

	// 获取单个亲密度技能信息
	public StaticBeautyDateSkills getStaticDateSkills(int keyId) {
		if (dateSkillMap.size() > 0) {
			return dateSkillMap.get(keyId);
		} else {
			logger.error("skill is not exist : skill {}", keyId);
		}
		return null;
	}

	// 获取美女对应的亲密度技能信息
	public List<StaticBeautyDateSkills> getStaticBeautDateSkills(int beautyId) {
		if (beautyDateSkillMap.size() > 0) {
			List<StaticBeautyDateSkills> list = beautyDateSkillMap.get(beautyId);
			return list == null ? null : list.stream().sorted(Comparator.comparingInt(x -> x.getNeedNum())).collect(Collectors.toList());
		} else {
			logger.error("StaticBeautySkills is not exist : StaticBeautySkills {}", beautyId);
		}
		return null;
	}

	public Map<Integer, StaticBeautyDate> getAllBeautyDateMap() {
		return allBeautyDateMap;
	}

	public StaticBeautyDateAward getBeautyDateAwardById(int id) {
		return allBeautyDateAwardMap.get(id);
	}
}
