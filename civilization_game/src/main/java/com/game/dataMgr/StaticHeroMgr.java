package com.game.dataMgr;


import com.game.dao.s.StaticDataDao;
import com.game.domain.p.ConfigException;
import com.game.domain.p.Property;
import com.game.domain.p.PropertyFactor;
import com.game.domain.s.StaticHerDiviConfig;
import com.game.domain.s.StaticHero;
import com.game.domain.s.StaticHeroAdvance;
import com.game.domain.s.StaticHeroLv;
import com.game.domain.s.StaticHeroProp;
import com.game.domain.s.StaticHeroTalent;
import com.game.domain.s.StaticHeroWash;
import com.game.domain.s.StaticLootHero;
import com.game.util.LogHelper;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StaticHeroMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

    private Map<Integer, StaticHero> heroMap = new HashMap<Integer, StaticHero>();
	// heroType,StaticHero
	private Map<Integer, List<StaticHero>> heroTypeList = new HashMap<>();
	private List<StaticHeroLv> heroExpList = new ArrayList<StaticHeroLv>();
    private List<StaticHeroProp> heroPropList = new ArrayList<StaticHeroProp>();
    private Map<Integer, StaticHeroWash> heroWashMap = new HashMap<Integer, StaticHeroWash>();

    //key1:quality, key2:level, value:need exp
	private Map<Integer, Map<Integer, Integer>> heroExpMap = new HashMap<Integer, Map<Integer, Integer>>();

    //key1:level, key2:heroType value: StaticHeroProp
    private Map<Integer, Map<Integer, PropertyFactor>> heroPropMap = new HashMap<Integer, Map<Integer, PropertyFactor>>();
    private Map<Integer, StaticLootHero> lootHeroMap = new HashMap<Integer, StaticLootHero>();
    private Map<Integer, StaticHeroAdvance> heroAdvanceMap = new HashMap<Integer, StaticHeroAdvance>();
    private int maxLevel;
    private HashBasedTable<Integer, Integer, Boolean> checkHeroPro = HashBasedTable.create();

    private Map<Integer, StaticHerDiviConfig> heroDivMap = new HashMap<>();
    private HashBasedTable<Integer, Integer, Map<Integer, StaticHeroTalent>> heroTalentMap = HashBasedTable.create();

	@Override
    public void init() throws Exception {
        heroMap      = staticDataDao.selectHeroMap();
        heroExpList  = staticDataDao.selectHeroExpList();
        heroPropList = staticDataDao.selectHeroPropList();
        heroWashMap  = staticDataDao.selectStaticHeroWash();
        lootHeroMap  = staticDataDao.selectStaticLootHero();
        heroAdvanceMap  = staticDataDao.selectStaticHeroAdvance();
        heroExpMap.clear();
        checkHeroPro.clear();
        initExpMap();
        initMaxLevel();
        initHeroPropMap();

        //runAllTest();

        //check config
        check();
        checkHeroConfig();
        heroDivMap = staticDataDao.selectStaticDivi();
        initHeroTalent();
		initHeroTypeMap();
	}

	private void initHeroTypeMap() {
		for (StaticHero staticHero : heroMap.values()) {
			List<StaticHero> mpList = heroTypeList.get(staticHero.getHeroType());
			if (mpList == null) {
				mpList = new ArrayList<>();
				heroTypeList.put(staticHero.getHeroType(), mpList);
			}
			mpList.add(staticHero);
		}
    }

    public void checkHeroConfig() throws ConfigException {
        for (StaticHero hero : heroMap.values()) {
            if (hero == null) {
                continue;
            }

            if (hero.getAttack() == 0 || hero.getDefence() == 0 || hero.getSoldierCount() == 0) {
                    throw new ConfigException("hero config error!");
            }
        }
    }

    public void check() throws Exception {
        for (Map.Entry<Integer, StaticHeroWash> staticHeroWash : heroWashMap.entrySet()) {
            if (staticHeroWash.getValue() == null) {
                throw new ConfigException("staticHeroWash is null, index = " + staticHeroWash.getKey());
            }

            List<List<Integer>> washRateList = staticHeroWash.getValue().getWashRate();

            for (List<Integer> itemRate : washRateList) {
                if (itemRate.size() != 2) {
                    throw new ConfigException("itemRate.size() != 2");
                }
            }
        }

    }

	public StaticHero getStaticHero(int heroId) {
		return heroMap.get(heroId);
	}

	public int getHeroType(int heroId) {
        StaticHero staticHero =getStaticHero(heroId);
		if (staticHero == null) {
            return 0;
		}
        return  staticHero.getHeroType();
    }

	public int maxLevel() {
	    return maxLevel;
    }

    public void initExpMap() {
        for (StaticHeroLv item : heroExpList) {
            int quality = item.getQuality();
            Map<Integer, Integer> needExpMap = heroExpMap.get(quality);
            if (needExpMap != null) {
                needExpMap.put(item.getHeroLv(), item.getNeedExp());
            } else {
                needExpMap = new HashMap<Integer, Integer>();
                needExpMap.put(item.getHeroLv(), item.getNeedExp());
                heroExpMap.put(quality, needExpMap);
            }
        }
        //释放内存
        heroExpList.clear();
        //showExpMap();
    }

    public void initMaxLevel() {
        Map<Integer, Integer> levelMap = heroExpMap.get(1);
        if(levelMap != null) {
            maxLevel = levelMap.size();
        } else {
            maxLevel = 120;
        }
    }

    public void showExpMap() {
        for (Map.Entry<Integer, Map<Integer, Integer>> qualityItem : heroExpMap.entrySet()) {
            if(qualityItem == null) {
                continue;
            }

            Map<Integer, Integer> needExp = qualityItem.getValue();
            for (Map.Entry<Integer, Integer> expItem : needExp.entrySet()) {
                String info = qualityItem.getKey() + ", " + expItem.getKey() + ", " + expItem.getValue();
            }

        }
    }

    public int getExp(int level, int quality) {
        Map<Integer, Integer> needExpMap = heroExpMap.get(quality);
        if (needExpMap == null) {
            LogHelper.CONFIG_LOGGER.error("no this quality in config, quality = " + quality);
            return Integer.MAX_VALUE;
        }

        Integer needExp = needExpMap.get(level);
        if (needExp == null) {
            LogHelper.CONFIG_LOGGER.trace("no this level in config quility:{} level:{}", quality, level);
            return Integer.MAX_VALUE;
        }

        return needExp;
    }

    public void initHeroPropMap() {
        heroPropMap.clear();
        for (StaticHeroProp staticHeroProp : heroPropList) {
            if (staticHeroProp == null) {
                LogHelper.CONFIG_LOGGER.error("static hero prop is null!");
                continue;
            }

            int heroLevel = staticHeroProp.getHeroLv();
            Map<Integer, PropertyFactor> propertyFactorMap = heroPropMap.get(heroLevel);
            if (propertyFactorMap == null) {
                propertyFactorMap = new HashMap<Integer, PropertyFactor>();
                heroPropMap.put(heroLevel, propertyFactorMap);
            }

            int heroType = staticHeroProp.getHeroType();
            PropertyFactor propertyAdd = propertyFactorMap.get(heroType);
            if (propertyAdd == null) {
                propertyAdd = new PropertyFactor();
                propertyAdd.initFactor(staticHeroProp);
            }

            propertyFactorMap.put(heroType, propertyAdd);

            Boolean hasSame = checkHeroPro.get(heroLevel, heroType);
            if (hasSame != null) {
                LogHelper.CONFIG_LOGGER.error("initHeroPropMap hasSame error heroLevel = " + heroLevel + ", heroType = " + heroType);
            }
            checkHeroPro.put(heroLevel, heroType, true);
        }
    }


    public Map<Integer, StaticHero> getHeroMap() {
        return heroMap;
    }

    public void setHeroMap(Map<Integer, StaticHero> heroMap) {
        this.heroMap = heroMap;
    }

    public Map<Integer, StaticHeroWash> getHeroWashMap() {
        return heroWashMap;
    }

    public void setHeroWashMap(Map<Integer, StaticHeroWash> heroWashMap) {
        this.heroWashMap = heroWashMap;
    }

    public StaticHeroWash getWashRate(int process, int washType) {
        for (Map.Entry<Integer, StaticHeroWash> item : heroWashMap.entrySet()) {
            StaticHeroWash staticHeroWash = item.getValue();
            if (staticHeroWash.getStart() <= process &&
                    process <= staticHeroWash.getEnd()
                    && washType == staticHeroWash.getWashType()) {
                //System.out.println("washId = " + staticHeroWash.getWashId());
                return staticHeroWash;
            }

            if (process == staticHeroWash.getEnd()
                    && staticHeroWash.getEnd() == 1000
                    && washType == staticHeroWash.getWashType()) {
                //System.out.println("washId = " + staticHeroWash.getWashId());
                return staticHeroWash;
            }
        }
        return null;
    }

    public PropertyFactor getFactor(int level, int quality) {
        Map<Integer, PropertyFactor> qualityMap = heroPropMap.get(level);
        if (qualityMap == null) {
            LogHelper.CONFIG_LOGGER.error("getFactor:no this level in config, level = " + level);
            return new PropertyFactor();
        }
        PropertyFactor property = qualityMap.get(quality);
        if (property == null) {
            LogHelper.CONFIG_LOGGER.error("getFactor:no this quality in config, quality = " + quality);
            return new PropertyFactor();
        }
        return property;
    }

    // 获取英雄基础属性
    public Property getBaseProperty(int heroId) {
        StaticHero staticHero = heroMap.get(heroId);
        if (staticHero == null) {
            throw new NullPointerException("staticHero is null, heroId = " + heroId);
        }

        Property property = new Property();
        property.setAttack(staticHero.getBaseAttack());
        property.setDefence(staticHero.getBaseDefence());
        property.setSoldierNum(staticHero.getBaseSoldierCount());
        return property;
    }


    // 获取武将寻访的配置
    public StaticLootHero getStaticLootHero(int lootId) {
        return lootHeroMap.get(lootId);
    }

    // 获取武将突破配置
    public StaticHeroAdvance getStaticHeroAdvance(int type) {
        return heroAdvanceMap.get(type);
    }

    public Map<Integer, Map<Integer, PropertyFactor>> getHeroPropMap() {
        return heroPropMap;
    }

    public void setHeroPropMap(Map<Integer, Map<Integer, PropertyFactor>> heroPropMap) {
        this.heroPropMap = heroPropMap;
    }

    public int getQuality(int heroId) {
        StaticHero staticHero = heroMap.get(heroId);
        if (staticHero != null) {
            return staticHero.getQuality();
        }

        LogHelper.CONFIG_LOGGER.error("StaticHero is null heroId:{}" + heroId);

        return 0;
    }

    public PropertyFactor getPropertyFactor(int heroLv, int heroType) {
        Map<Integer, PropertyFactor> propertyFactorMap = heroPropMap.get(heroLv);
        if (propertyFactorMap == null) {
            return new PropertyFactor();
        }

        PropertyFactor propertyFactor = propertyFactorMap.get(heroType);
        if (propertyFactor == null) {
            return new PropertyFactor();
        }

        return propertyFactor;
    }

    public int getSoldierType(int heroId) {
        StaticHero staticHero = heroMap.get(heroId);
        if (staticHero == null) {
            return 0;
        }
        return staticHero.getSoldierType();
    }

    public StaticHerDiviConfig getStaticHerDiviConfig(int id) {
        return heroDivMap.get(id);
    }


    public void initHeroTalent() {
        heroTalentMap.clear();
        List<StaticHeroTalent> staticHeroTalents = staticDataDao.selectHeroTalent();
        staticHeroTalents.forEach(x -> {
            Map<Integer, StaticHeroTalent> integerStaticHeroTalentMap = heroTalentMap.get(x.getTalentType(), x.getLevel());
            if (integerStaticHeroTalentMap == null) {
                heroTalentMap.put(x.getTalentType(), x.getLevel(), integerStaticHeroTalentMap = new ConcurrentHashMap<>());
            }
            integerStaticHeroTalentMap.put(x.getSoldierType(), x);
        });
    }
    public StaticHeroTalent getStaticHeroTalent(int type, int level, int stype) {
        Map<Integer, StaticHeroTalent> integerStaticHeroTalentMap = heroTalentMap.get(type, level);
        if (integerStaticHeroTalentMap != null) {
            return integerStaticHeroTalentMap.get(stype);
        }
        return null;
    }

	public List<StaticHero> getHerTypeList(int heroType) {
		return heroTypeList.get(heroType);
	}

}
