package com.game.dataMgr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.PropertyType;
import com.game.dao.s.StaticDataDao;
import com.game.domain.p.ConfigException;
import com.game.domain.p.Property;
import com.game.domain.s.StaticKillEquip;
import com.game.domain.s.StaticKillEquipLevel;
import com.game.domain.s.StaticKillEquipRate;
import com.game.util.LogHelper;

@Component
public class StaticKillEquipMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticKillEquip> killEquipMap = new HashMap<Integer, StaticKillEquip>();
    private Map<Integer, StaticKillEquipLevel> killEquipLevelMap = new HashMap<Integer, StaticKillEquipLevel>();
    private Map<Integer, StaticKillEquipRate> killEquipRateMap = new HashMap<Integer, StaticKillEquipRate>();
    // 道具=杀器索引
    private Map<Integer, Integer> killProp = new HashMap<Integer, Integer>();

    // equipId, level
    private Map<Integer, Map<Integer, StaticKillEquipLevel>> killEquipLevelKeymap = new HashMap<Integer, Map<Integer, StaticKillEquipLevel>>();

    @Override
    public void init() throws Exception {
        setKillEquipMap(staticDataDao.selectStaticKillEquip());
        killEquipLevelMap = staticDataDao.selectStaticKillEquipLevel();
        setKillEquipRateMap(staticDataDao.selectStaticKillEquipRate());
        killEquipLevelKeymap.clear();
        makeEquipLevelKeyMap();
        killProp.clear();
        makeKillProp();
        checkCompund();
        checkCritiRate();
        checkProperty();
    }

    public void makeKillProp() {
        for (StaticKillEquip staticKillEquip : killEquipMap.values()) {
            if (staticKillEquip == null) {
                continue;
            }

            List<List<Integer>> compund = staticKillEquip.getCompound();
            if (compund == null) {
                LogHelper.CONFIG_LOGGER.error("staticKillEquip compund is null, equipId:{}", staticKillEquip.getEquipId());
                continue;
            }

            if (compund.size() < 1) {
                LogHelper.CONFIG_LOGGER.error("staticKillEquip compund size < 1, equipId:{}", staticKillEquip.getEquipId());
                continue;
            }

            List<Integer> propConfig = compund.get(0);
            if (propConfig == null) {
                LogHelper.CONFIG_LOGGER.error("staticKillEquip propConfig is null ,equipId:{}", staticKillEquip.getEquipId());
                continue;
            }

            if (propConfig.size() != 3) {
                LogHelper.CONFIG_LOGGER.error("staticKillEquip proconfig size() < 1,equipId:{}", staticKillEquip.getEquipId());
                continue;
            }

            killProp.put(propConfig.get(1), staticKillEquip.getEquipId());
        }
    }

    // 根据道具Id找到杀器需要的数量
    public int getKillNeedCount(int propId) {
        Integer killId = killProp.get(propId);
        if (killId == null) {
            LogHelper.CONFIG_LOGGER.error("killProp killId = 0, propId :{}", propId);
            return 0;
        }

        StaticKillEquip staticKillEquip = getStaticKillEquip(killId);
        if (staticKillEquip == null) {
            LogHelper.CONFIG_LOGGER.error("staticKillEquip is null, killId:{} ", killId);
            return 0;
        }

        List<List<Integer>> compund = staticKillEquip.getCompound();
        List<Integer> item = compund.get(0);
        if (item == null) {
            LogHelper.CONFIG_LOGGER.error("staticKillEquip compund propConfig is null,killId:{}", killId);
            return 0;
        }

        if (item.size() != 3) {
            LogHelper.CONFIG_LOGGER.error("staticKillEquip compund proconfig size() < 1,killId:{}", killId);
            return 0;
        }

        return item.get(2);
    }


    public Map<Integer, StaticKillEquip> getKillEquipMap() {
        return killEquipMap;
    }

    public void setKillEquipMap(Map<Integer, StaticKillEquip> killEquipMap) {
        this.killEquipMap = killEquipMap;
    }


    public Map<Integer, StaticKillEquipRate> getKillEquipRateMap() {
        return killEquipRateMap;
    }

    public void setKillEquipRateMap(Map<Integer, StaticKillEquipRate> killEquipRateMap) {
        this.killEquipRateMap = killEquipRateMap;
    }

    public StaticKillEquip getStaticKillEquip(int equipId) {
        return killEquipMap.get(equipId);
    }

    public void checkCompund() throws ConfigException {
        for (StaticKillEquip staticKillEquip : killEquipMap.values()) {
            if (!isCompundOk(staticKillEquip)) {
                throw new ConfigException("kill equip exception!");
            }
        }
    }

    public boolean isCompundOk(StaticKillEquip staticKillEquip) {
        List<List<Integer>> compound = staticKillEquip.getCompound();
        if (compound == null) {
            return false;
        }

        if (compound.size() <= 0) {
            return false;
        }

        for (List<Integer> need : compound) {
            if (need == null) {
                LogHelper.CONFIG_LOGGER.error("compund is null!");
                return false;
            }

            if (need.size() != 3) {
                LogHelper.CONFIG_LOGGER.error("need size != 3");
                return false;
            }

        }

        return true;
    }

    public boolean isCritiRate() {
        for (StaticKillEquipRate staticKillEquipRate : killEquipRateMap.values()) {
            if (staticKillEquipRate == null) {
                LogHelper.CONFIG_LOGGER.error("staticKillEquipRate == null");
                return false;
            }
            List<Integer> rateList = staticKillEquipRate.getRate();
            if (rateList == null) {
                LogHelper.CONFIG_LOGGER.error("rateList == null");
                return false;
            }

            int criti = staticKillEquipRate.getCriti();
            if (criti != rateList.size()) {
                LogHelper.CONFIG_LOGGER.error("criti != rateList.size()");
                return false;
            }

            int toatl = 0;
            for (Integer num : rateList) {
                toatl += num;
            }

            if (toatl != 1000) {
                LogHelper.CONFIG_LOGGER.error("toatl != 1000");
                return false;
            }
        }

        return true;
    }

    public void checkCritiRate() throws ConfigException {
        if (!isCritiRate()) {
            throw new ConfigException("Criti Rate Config Error!");
        }
    }

    public StaticKillEquipRate getCritiConfig(int criti) {
        return killEquipRateMap.get(criti);
    }

    public void makeEquipLevelKeyMap() {
        for (StaticKillEquipLevel config : killEquipLevelMap.values()) {
            if (config == null) {
                continue;
            }

            Map<Integer, StaticKillEquipLevel> levelKey = getKillEquipLevelKeymap().get(config.getEquipId());
            if (levelKey == null) {
                levelKey = new HashMap<Integer, StaticKillEquipLevel>();
                levelKey.put(config.getLevel(), config);
                getKillEquipLevelKeymap().put(config.getEquipId(), levelKey);
            } else {
                levelKey.put(config.getLevel(), config);
            }
        }
    }

    public int getStoneCost(int equipId, int level) {
        Map<Integer, StaticKillEquipLevel> levelKey = getKillEquipLevelKeymap().get(equipId);
        if (levelKey == null) {
            LogHelper.CONFIG_LOGGER.error("levelKey is null!");
            return Integer.MAX_VALUE;
        }

        StaticKillEquipLevel staticKillEquipLevel = levelKey.get(level);
        if (staticKillEquipLevel == null) {
            LogHelper.CONFIG_LOGGER.error("staticKillEquipLevel is null!");
            return Integer.MAX_VALUE;
        }

        return staticKillEquipLevel.getStone();
    }

    public Map<Integer, Map<Integer, StaticKillEquipLevel>> getKillEquipLevelKeymap() {
        return killEquipLevelKeymap;
    }

    public void setKillEquipLevelKeymap(Map<Integer, Map<Integer, StaticKillEquipLevel>> killEquipLevelKeymap) {
        this.killEquipLevelKeymap = killEquipLevelKeymap;
    }

    public Property getKillProperty(int equipId, int equipLv) {
        Map<Integer, StaticKillEquipLevel> equipLevelMap = killEquipLevelKeymap.get(equipId);
        Property property = new Property();
        StaticKillEquipLevel staticKillEquipLevel = equipLevelMap.get(equipLv);
        List<List<Integer>> config = staticKillEquipLevel.getProperty();
        for (List<Integer> elem : config) {
            int id = elem.get(0);
            int count = elem.get(1);
            if (id == PropertyType.ATTCK) {
                property.setAttack(property.getAttack() + count);
            } else if (id == PropertyType.DEFENCE) {
                property.setDefence(property.getDefence() + count);
            } else if (id == PropertyType.SOLDIER_NUM) {
                property.setSoldierNum(property.getSoldierNum() + count);
            }
        }

        return property;
    }

    public boolean isPropertyOk() {
        for (Map<Integer, StaticKillEquipLevel> killEquipLevelMap : killEquipLevelKeymap.values()) {
            if (killEquipLevelMap == null) {
                return false;
            }

            for (StaticKillEquipLevel config : killEquipLevelMap.values()) {
                if (config == null) {
                    return false;
                }

                List<List<Integer>> property = config.getProperty();
                if (property == null || property.size() <= 0) {
                    return false;
                }

                for (List<Integer> one : property) {
                    if (one == null || one.size() != 2) {
                        return false;
                    }
                }
            }

        }

        return true;
    }

    public void checkProperty() throws ConfigException {
        if (!isPropertyOk()) {
            throw new ConfigException("kill equip property is error!");
        }
    }


}
