package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticSkill;
import com.game.domain.s.StaticWashSkill;
import com.game.util.RandomHelper;
import com.google.common.collect.HashBasedTable;

@Component
@LoadData(name = "技能")
public class StaticSkillMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    //技能配置
    private Map<Integer, StaticSkill> skillMap = new HashMap<Integer, StaticSkill>();

    //技能等级&类型配置
    private Map<Integer,Map<Integer, StaticSkill>> skillWashMap = new HashMap<Integer,Map<Integer, StaticSkill>>();

    // 技能洗练概率配置
    private List<StaticWashSkill> washSkillList = new ArrayList<StaticWashSkill>();

    // 技能洗练
    private HashBasedTable<Integer, Integer, StaticWashSkill> goldwashSkillTable = HashBasedTable.create();
    private HashBasedTable<Integer, Integer, StaticWashSkill> freewashSkillTable = HashBasedTable.create();

    // 过滤出1级的skill
    private List<Integer> skillIds = new ArrayList<Integer>();

    @Override
    public void load() throws Exception {
        // s_skill_lv_rate
        skillMap = staticDataDao.selectSkillMap();
        washSkillList = staticDataDao.selectWashSkillRate();
        skillWashMap.clear();
        freewashSkillTable.clear();
        goldwashSkillTable.clear();
        skillIds.clear();
        makeWashMap();
        makeSkillIds();
    }

    @Override
    public void init() throws Exception{

    }

    public Map<Integer, StaticSkill> getSkillMap() {
        return skillMap;
    }

    public void setSkillMap(Map<Integer, StaticSkill> skillMap) {
        this.skillMap = skillMap;
    }

    public void makeSkillIds() {
        for (StaticSkill staticSkill : skillMap.values()) {
            if (staticSkill == null) {
                continue;
            }
            if (staticSkill.getLevel() == 1) {
                skillIds.add(staticSkill.getSkillId());
            }
        }
    }

    public StaticSkill getSkillId(int skillLevel, int skillType) {
        for (Map.Entry<Integer, StaticSkill> item : skillMap.entrySet()) {
            if (item == null) {
                continue;
            }

            StaticSkill staticSkill = item.getValue();
            if (staticSkill == null) {
                continue;
            }

            if(staticSkill.getLevel() == skillLevel &&
                staticSkill.getSkillType() == skillType) {
                return staticSkill;
            }
        }

        return null;
    }

    public StaticSkill getStaticSkill(int skillId) {
        return skillMap.get(skillId);
    }

    public void makeWashMap() {
        for (Map.Entry<Integer, StaticSkill> staticSkillEntry : skillMap.entrySet()) {
            if (staticSkillEntry == null)
                continue;
            StaticSkill staticSkill = staticSkillEntry.getValue();
            if (staticSkill == null)
                continue;

            Integer skillLevel = staticSkill.getLevel();
            //Map<Integer,Map<Integer, StaticSkill>> skillWashMap
            Map<Integer, StaticSkill> skillTypeMap = skillWashMap.get(skillLevel);
            if (skillTypeMap == null) {
                skillTypeMap = new HashMap<Integer, StaticSkill>();
                skillWashMap.put(skillLevel, skillTypeMap);
            }

            StaticSkill staticSkillInfo = skillTypeMap.get(staticSkill.getSkillType());
            if (staticSkillInfo == null) {
                skillTypeMap.put(staticSkill.getSkillType(), staticSkill);
            }

        }

        for (StaticWashSkill staticWashSkill : washSkillList) {
            if (staticWashSkill == null) {
                continue;
            }

            if (staticWashSkill.getWashType() == 1) {
                freewashSkillTable.put(staticWashSkill.getEquipQuality(), staticWashSkill.getSkillLv(), staticWashSkill);
            } else if(staticWashSkill.getWashType() == 2) {
                goldwashSkillTable.put(staticWashSkill.getEquipQuality(), staticWashSkill.getSkillLv(), staticWashSkill);
            }
        }

    }

    public StaticSkill getSkillByLvType(int skillLevel, int skillType) {
        Map<Integer, StaticSkill> skillTypeMap = skillWashMap.get(skillLevel);
        if (skillTypeMap != null) {
            return  skillTypeMap.get(skillType);
        }

        return null;
    }


    // 获取下一等级洗练配置
    public StaticWashSkill getWashSkillConfig(int washType, int equipQuality, int skillLv) {
        if (washType == 1) {
            return freewashSkillTable.get(equipQuality, skillLv);
        } else if (washType == 2) {
            return goldwashSkillTable.get(equipQuality, skillLv);
        }

        return null;
    }

    // 获取随机技能个数
    public List<Integer> getRandomSkills(int num) {
        List<Integer> randomSkill = new ArrayList<Integer>();
        for (int i = 1; i <= num; i++) {
            int randSkill = RandomHelper.threadSafeRand(1, skillIds.size());
            randomSkill.add(skillIds.get(randSkill-1));
        }
        return randomSkill;
    }
}
