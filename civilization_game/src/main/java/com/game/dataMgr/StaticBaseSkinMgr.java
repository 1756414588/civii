package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticBaseSkin;
import com.game.domain.s.StaticSkinSkill;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author CaoBing
 * @date 2021/1/28 14:10
 */
@Component
@LoadData(name = "皮肤")
public class StaticBaseSkinMgr extends BaseDataMgr {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private List<StaticBaseSkin> staticBaseSkins = new ArrayList<>();

	private Map<Integer, StaticBaseSkin> staticBaseSkinMap = new HashedMap();
	/**
	 * 按照兵书基础属性类型AND等级存放
	 */
	private HashBasedTable<Integer, Integer, StaticSkinSkill> staticBaseSkinSkillMap = HashBasedTable.create();

	@Autowired
	private StaticDataDao staticDataDao;

	@Override
	public void load() throws Exception {
		cleanConfig();
		initBaseSkin();
		initSkinSkill();
	}

	@Override
	public void init() throws Exception {
	}

	public void cleanConfig() {
		staticBaseSkins.clear();
		staticBaseSkinMap.clear();
		staticBaseSkinSkillMap.clear();
	}

	public void initBaseSkin() {
		this.staticBaseSkins = staticDataDao.selectStaticBaseSkin();
		if (staticBaseSkins != null && staticBaseSkins.size() > 0) {
			for (StaticBaseSkin staticBaseSkin : staticBaseSkins) {
				if (null != staticBaseSkin) {
					staticBaseSkinMap.put(staticBaseSkin.getKeyId(), staticBaseSkin);
				}
			}
		}
	}


	public void initSkinSkill() {
		List<StaticSkinSkill> staticSkinSkills = staticDataDao.selectStaticSkinSkills();
		if (staticSkinSkills != null && staticSkinSkills.size() > 0) {
			for (StaticSkinSkill staticSkinSkill : staticSkinSkills) {
				staticBaseSkinSkillMap.put(staticSkinSkill.getBaseId(), staticSkinSkill.getLevel(), staticSkinSkill);
			}
		}
	}

	public List<StaticBaseSkin> getStaticBaseSkinList() {
		return staticBaseSkins;
	}

	public StaticBaseSkin getStaticBaseSkin(int skinId) {
		return staticBaseSkinMap.get(skinId);
	}

	public StaticSkinSkill getStaticSkinSkill(int baseId, int lev) {
		return staticBaseSkinSkillMap.get(baseId, lev);
	}
}
