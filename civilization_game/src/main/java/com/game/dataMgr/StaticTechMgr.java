package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticTechInfo;
import com.game.domain.s.StaticTechType;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@LoadData(name = "科技配置模块")
public class StaticTechMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticTechType> techTypeMap = new HashMap<Integer, StaticTechType>();
    private Map<Integer, StaticTechInfo> techLevelsMap = new HashMap<Integer, StaticTechInfo>();
    //techType, level, process
    private HashBasedTable<Integer, Integer, StaticTechInfo> techInfoTable = HashBasedTable.create();
    // 前置科技
    private Map<Integer, List<Integer>> preTechs = new HashMap<Integer, List<Integer>>();

    @Override
    public void load() throws Exception {
        techTypeMap = staticDataDao.selectTechTypeMap();
        techLevelsMap = staticDataDao.selectTechLevelsMap();
        techInfoTable.clear();
        preTechs.clear();
        makeTechLvMap();
    }

    @Override
    public void init() throws Exception{
    }

    public void makeTechLvMap() {
        for (Map.Entry<Integer, StaticTechInfo> elem : getTechLevelsMap().entrySet()) {
            if(elem == null) {
                continue;
            }

            StaticTechInfo staticTechInfo = elem.getValue();
            if (staticTechInfo == null) {
                continue;
            }

            techInfoTable.put(staticTechInfo.getTechType(), staticTechInfo.getTechLv(), staticTechInfo);
            List<Integer> openCond = staticTechInfo.getOpenCond();
            if (openCond == null || openCond.size() != 2) {
                continue;
            }

            // 科技等级
            openCond.add(staticTechInfo.getTechLv());
            preTechs.put(staticTechInfo.getTechType(), openCond);
        }
    }


    public StaticTechType getStaticTechType(int techType) {
        return techTypeMap.get(techType);
    }

    // tech type, tech level, tech process
    public StaticTechInfo getStaticTechLevel(int techType, int level) {
        return techInfoTable.get(techType, level);
    }

    // get max process
    public Integer getMaxProcess(int techType, int level) {
        StaticTechInfo staticTechInfo = techInfoTable.get(techType, level);
        if (staticTechInfo == null) {
            return Integer.MIN_VALUE;
        }
        return staticTechInfo.getProcess();
    }

    public Map<Integer, StaticTechInfo> getTechLevelsMap () {
        return techLevelsMap;
    }

    public void setTechLevelsMap (Map<Integer, StaticTechInfo> techLevelsMap) {
        this.techLevelsMap = techLevelsMap;
    }

    public Map<Integer, List<Integer>> getPreTechs () {
        return preTechs;
    }

    public void setPreTechs (Map<Integer, List<Integer>> preTechs) {
        this.preTechs = preTechs;
    }

    public Map<Integer, StaticTechType> getTechTypeMap() {
        return techTypeMap;
    }

    public void setTechTypeMap(Map<Integer, StaticTechType> techTypeMap) {
        this.techTypeMap = techTypeMap;
    }
}
