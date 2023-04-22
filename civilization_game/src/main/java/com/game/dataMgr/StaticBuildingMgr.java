package com.game.dataMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.dao.s.StaticDataDao;
import com.game.domain.p.ConfigException;
import com.game.domain.s.StaticPropBuilding;
import com.game.domain.s.StaticWare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.domain.s.StaticBuilding;
import com.game.domain.s.StaticBuildingLv;
import com.game.domain.s.StaticBuildingType;
import com.game.domain.s.StaticEmployee;
import com.game.util.LogHelper;

@Component
public class StaticBuildingMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;
    // type, level, score
    private Map<Integer, Map<Integer, StaticBuildingLv>> buildingLvMap = new HashMap<Integer, Map<Integer, StaticBuildingLv>>();
    private Map<Integer, StaticBuildingLv> buildingLvsMap = new HashMap<Integer, StaticBuildingLv>();

    private Map<Integer, StaticEmployee> employeeMap = new HashMap<Integer, StaticEmployee>();
    private Map<Integer, StaticBuildingType> buildingTypeMap = new HashMap<Integer, StaticBuildingType>();
    private Map<Integer, StaticBuilding> buildingMap = new HashMap<Integer, StaticBuilding>();
    private Map<Integer, StaticPropBuilding> staticPropBuildingMap = new HashMap<Integer, StaticPropBuilding>();
    private Map<Integer, StaticWare> staticWareMap = new HashMap<Integer, StaticWare>();

    @Override
    public void init() throws Exception{
        buildingLvsMap = staticDataDao.selectBuildingLvMap();
        employeeMap = staticDataDao.selectStaticEmployee();
        buildingTypeMap = staticDataDao.selectStaticBuilding();
        setBuildingMap(staticDataDao.selectBuildingMap());
        staticPropBuildingMap = staticDataDao.selectStaticPropBuilding();
        makeBuildinglvMap();
        staticWareMap = staticDataDao.selectWareMap();
    }

    public void makeBuildinglvMap() throws Exception{
        for (Map.Entry<Integer, StaticBuildingLv> elem : buildingLvsMap.entrySet()) {
            if (elem == null) {
                continue;
            }

            StaticBuildingLv staticBuildingLv = elem.getValue();

            if (staticBuildingLv == null) {
//                LogHelper.CONFIG_LOGGER.error("staticBuildingLv == null");
//                continue;
                throw new ConfigException("staticBuildingLv == null");
            }

            int buildingId = staticBuildingLv.getBuildingType();
            if (staticBuildingLv.getLevel() == -1) {
//                LogHelper.CONFIG_LOGGER.trace("building level = -1, building Id = " + buildingId);
//                continue;
                throw new ConfigException("building level = -1, building Id = " + buildingId);
            }

          /*  List<Integer> award = staticBuildingLv.getAward();
            if (award == null || award.size() != 3) {
                try {
                    throw new ConfigException("award error!");
                } catch (ConfigException e) {
                    e.printStackTrace();
                }
            }*/

            if (!buildingLvMap.containsKey(staticBuildingLv.getBuildingType())) {
                buildingLvMap.put(staticBuildingLv.getBuildingType(), new HashMap<Integer, StaticBuildingLv>());
            }

            Map<Integer, StaticBuildingLv> innerMap = buildingLvMap.get(staticBuildingLv.getBuildingType());
            if (!innerMap.containsKey(staticBuildingLv.getLevel())) {
                innerMap.put(staticBuildingLv.getLevel(), staticBuildingLv);
            }
        }
    }

    // buildingType, getBuildingLv
    // 读取resourceOut
    public Long getResource(int buildingType, int buildingLv) {
        Map<Integer, StaticBuildingLv> buildingMaps = buildingLvMap.get(buildingType);
        if (buildingMaps == null) {
            LogHelper.CONFIG_LOGGER.trace("buildingMaps == null buildingType:{}", buildingType);
            return 0L;
        }

        StaticBuildingLv staticBuildingLv = buildingMaps.get(buildingLv);
        if (staticBuildingLv == null) {
            LogHelper.CONFIG_LOGGER.trace("staticBuildingLv == null buildingType:{} buildingLv:{}", buildingType, buildingLv);
            return 0L;
        }

        List<Long> resourceOut = staticBuildingLv.getResourceOut();
        if (resourceOut.size() != 2) {
            LogHelper.CONFIG_LOGGER.trace("resourceOut size error buildingType:{} buildingLv:{}", buildingType, buildingLv);
            return 0L;
        }

        return resourceOut.get(1);
    }

    public int getResourceType(int buildingType) {
        StaticBuildingType staticBuildingType = buildingTypeMap.get(buildingType);
        if (staticBuildingType == null) {
            LogHelper.CONFIG_LOGGER.trace("staticBuildingType is null buildingType:{}", buildingType);
            return -1;
        }

        return staticBuildingType.getResourceType();
    }

    public StaticEmployee getEmployee(int employeeId) {
        return employeeMap.get(employeeId);
    }

    public int getBuildingType(int buildingId) {
        StaticBuilding staticBuilding = getBuildingMap().get(buildingId);
        if (staticBuilding != null) {
            return staticBuilding.getBuildingType();
        }
        return Integer.MIN_VALUE;
    }

    public int maxBuildLv(int buildingType) {
        StaticBuildingType staticBuildingType = buildingTypeMap.get(buildingType);
        if (staticBuildingType != null) {
            return staticBuildingType.getMaxLv();
        }
        return Integer.MIN_VALUE;
    }

    public StaticBuildingLv getBuildingLv(int buildingType, int buildingLv) {
        Map<Integer, StaticBuildingLv> lvMap = buildingLvMap.get(buildingType);
        if (lvMap == null) {
            return null;
        }
        StaticBuildingLv staticBuildingLv = lvMap.get(buildingLv);
        return staticBuildingLv;
    }

    public void setBuildingMap(Map<Integer, StaticBuilding> buildingMap) {
        this.buildingMap = buildingMap;
    }

    public StaticPropBuilding getStaticPropBuilding(int buildingId) {
        return staticPropBuildingMap.get(buildingId);
    }

    public StaticWare getStaticWare(int wareLv) {
        return staticWareMap.get(wareLv);
    }

    public StaticBuilding getStaticBuilding(int buildingId) {
        return getBuildingMap().get(buildingId);
    }

    public Map<Integer, Map<Integer, StaticBuildingLv>> getBuildingLvMap() {
        return buildingLvMap;
    }

    public int getBattlScore(int buildingId, int buildingLv) {
        // 获得当前建筑的type
        int buildingType = getBuildingType(buildingId);
        if (buildingType == Integer.MIN_VALUE) {
            return 0;
        }

        Map<Integer, StaticBuildingLv> config = buildingLvMap.get(buildingType);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.error("config error,no buildingId = " + buildingType + " staticBuildingLv");
            return 0;
        }

        StaticBuildingLv staticBuildingLv = config.get(buildingLv);
        if (staticBuildingLv == null) {
            LogHelper.CONFIG_LOGGER.error("config error,no buildingLv = " + buildingLv + " staticBuildingLv" + ", buildingId = " + buildingId);
            return 0;
        }

        return staticBuildingLv.getBattleScore();
    }

    public Map<Integer, StaticBuilding> getBuildingMap() {
        return buildingMap;
    }

    public StaticBuildingType getStaticBuildingType(int buildingType) {
        return buildingTypeMap.get(buildingType);
    }

    public List<Integer> getRebuildInfo(int buildId, int rebuildId) {
        StaticBuilding staticBuilding = getStaticBuilding(buildId);
        List<Integer> result = staticBuilding.getRebuildInfo(rebuildId);
        if (result != null) {
            return result;
        }
        for (StaticBuilding s : buildingMap.values()) {
            if (s.getRebuildingId() == null) {
                continue;
            }
            List<List<Integer>> ss = s.getRebuildingId();
            // 反向查找
            boolean isBuildId = false;
            boolean isRebuildId = false;
            int type = 0;
            for (List<Integer> info : ss) {
                if (info.get(1).intValue() == buildId) {
                    isBuildId = true;
                }
                if (rebuildId == s.getBuildingId()) {
                    type = s.getBuildingType();
                    isRebuildId = true;
                } else if (info.get(1).intValue() == rebuildId) {
                    type = info.get(0).intValue();
                    isRebuildId = true;
                }
                if (isRebuildId && isBuildId) {
                    result = new ArrayList<>();
                    result.add(type);
                    result.add(buildId);
                    return result;
                }
            }

        }
        return null;
    }

    /**
     * @return
     */
    public List<Integer> getBuildIds() {
        List<Integer> builds = new ArrayList<>();
        for (StaticBuilding staticBuilding : buildingMap.values()) {
            if (staticBuilding.getBuildingId() > 80) {
                continue;
            }
            builds.add(staticBuilding.getBuildingId());
        }
        return builds;
    }

}
