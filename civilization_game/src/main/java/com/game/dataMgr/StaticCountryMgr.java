package com.game.dataMgr;

import com.game.define.LoadData;
import com.game.domain.s.StaticCountry;
import java.util.*;

import com.game.domain.s.StaticCountryHeroEscape;
import com.game.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticCountryBuild;
import com.game.domain.s.StaticCountryDaily;
import com.game.domain.s.StaticCountryGlory;
import com.game.domain.s.StaticCountryGovern;
import com.game.domain.s.StaticCountryHero;
import com.game.domain.s.StaticCountryLevel;
import com.game.domain.s.StaticCountryRank;
import com.game.domain.s.StaticCountryTask;
import com.game.domain.s.StaticCountryTitle;
import com.google.common.collect.HashBasedTable;

/**
 * @author 陈奎
 * @version 1.0
 * @filename
 * @time 2017-3-13 下午2:10:24
 * @describe 国家体系
 */
@Component
@LoadData(name = "国家")
public class StaticCountryMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;
    // 国家荣誉
    private Map<Integer, StaticCountryGlory> glorys = new HashMap<Integer, StaticCountryGlory>();
    // 国家任务
    private Map<Integer, StaticCountryTask> tasks = new HashMap<Integer, StaticCountryTask>();
    // 国家排行数据
    private Map<Integer, Map<Integer, StaticCountryRank>> ranks = new HashMap<Integer, Map<Integer, StaticCountryRank>>();
    // 国家日志dailyId,StaticCountryDaily
    private Map<Integer, StaticCountryDaily> records = new HashMap<Integer, StaticCountryDaily>();
    // 国家神将
    private Map<Integer, StaticCountryHero> heros = new HashMap<Integer, StaticCountryHero>();
    // 爵位title,StaticCountryTitle
    private Map<Integer, StaticCountryTitle> titles = new HashMap<Integer, StaticCountryTitle>();
    // 国家建设countryLv,count,StaticCountryBuild
    private Map<Integer, Map<Integer, StaticCountryBuild>> builds = new HashMap<Integer, Map<Integer, StaticCountryBuild>>();
    // 国家经验countryLv,StaticCountryLevel
    private Map<Integer, StaticCountryLevel> exps = new HashMap<Integer, StaticCountryLevel>();
    // 国家官员召唤
    private HashBasedTable<Integer, Integer, StaticCountryGovern> governs = HashBasedTable.create();
    // 国家官员能召唤的人数
    private Map<Integer, Integer> callCount = new HashMap<Integer, Integer>();
    // 国家任务
    private Map<Integer, List<StaticCountryTask>> countryTasks = new HashMap<Integer, List<StaticCountryTask>>();
    // 国家名将配置 [国家、等级、配置]
    private HashBasedTable<Integer, Integer, StaticCountryHero> countryHeroTable = HashBasedTable.create();
    // 国家名将-国家映射
    private HashMap<Integer, Integer> heroCountryMapper = new HashMap<Integer, Integer>();
    // 国家名将怪物Id - 武将Id
    private HashMap<Integer, Integer> monsterHeroMapper = new HashMap<Integer, Integer>();
    // 国家名将逃跑概率
    private List<StaticCountryHeroEscape> heroEscapeMap = new ArrayList<StaticCountryHeroEscape>();
    private HashBasedTable<Integer, Integer, Integer> heroEscapRate = HashBasedTable.create();
    private HashBasedTable<Integer, Integer, Integer> heroGetRate1 = HashBasedTable.create();
    private HashBasedTable<Integer, Integer, Integer> heroGetRate2 = HashBasedTable.create();

    private Map<Integer, StaticCountry> countryMap = new HashMap<>();

    @Override
    public void load() throws Exception {
        glorys = staticDataDao.selectCountryGlory();
        tasks = staticDataDao.selectCountryTask();
        titles = staticDataDao.selectCountryTitle();
        records = staticDataDao.selectCountryRecord();
        heros = staticDataDao.selectCountryHero();
        exps = staticDataDao.selectCountryLevel();
        ranks.clear();
        builds.clear();
        governs.clear();
        callCount.clear();
        countryTasks.clear();
        countryHeroTable.clear();
        heroCountryMapper.clear();
        monsterHeroMapper.clear();
        heroEscapRate.clear();
        heroGetRate1.clear();
        heroGetRate2.clear();
        iniRank();
        iniBuild();
        iniGovern();
        makeCountryTask();
        makeCountryHero();
        heroEscapeMap = staticDataDao.selectHeroEscape();
        makeHeroEscapeRate();
        makeGetRate();
        countryMap = staticDataDao.selectCountryMap();
    }

    @Override
    public void init() throws Exception{
    }

    public void makeHeroEscapeRate() {
        for (StaticCountryHeroEscape config : heroEscapeMap) {
            heroEscapRate.put(config.getEscapeType(), config.getLoyalty(), config.getRate());
        }
    }

    public void makeCountryHero() {
        for (StaticCountryHero hero : getHeros().values()) {
            if (hero == null) {
                continue;
            }

            countryHeroTable.put(hero.getCountry(), hero.getLevel(), hero);
            heroCountryMapper.put(hero.getHeroId(), hero.getCountry());
            monsterHeroMapper.put(hero.getMonsterId(), hero.getHeroId());
        }
    }

    private void iniRank() {
        List<StaticCountryRank> ls = staticDataDao.selectCountryRank();
        for (StaticCountryRank rk : ls) {
            int type = rk.getType();
            Map<Integer, StaticCountryRank> rkMap = ranks.get(type);
            if (rkMap == null) {
                rkMap = new HashMap<Integer, StaticCountryRank>();
                ranks.put(type, rkMap);
            }
            rkMap.put(rk.getRank(), rk);
        }
    }

    private void iniBuild() {
        List<StaticCountryBuild> buildList = staticDataDao.selectCountryBuild();
        for (StaticCountryBuild e : buildList) {
            int countryLv = e.getCountryLv();
            int count = e.getCount();
            Map<Integer, StaticCountryBuild> countMap = builds.get(countryLv);
            if (countMap == null) {
                countMap = new HashMap<Integer, StaticCountryBuild>();
                builds.put(countryLv, countMap);
            }
            countMap.put(count, e);
        }
    }

    private void iniGovern() {
        List<StaticCountryGovern> governList = staticDataDao.selectCountryGovern();
        for (StaticCountryGovern e : governList) {
            governs.put(e.getGovernId(), e.getCount(), e);
            callCount.put(e.getGovernId(), e.getPerson());
        }
    }

    public StaticCountryHero getCountryHero(int heroId) {
        return heros.get(heroId);
    }

    public StaticCountryGlory getCountryGlory(int gloryLv) {
        return glorys.get(gloryLv);
    }

    public Map<Integer, StaticCountryRank> getCountryRank(int type) {
        return ranks.get(type);
    }

    public StaticCountryRank getCountryRank(int type, int rank) {
        Map<Integer, StaticCountryRank> mp = getCountryRank(type);
        if (mp == null) {
            return null;
        }
        return mp.get(rank);
    }

    public StaticCountryTask getCountryTask(int taskId) {
        return tasks.get(taskId);
    }

    public StaticCountryDaily getCountryRecord(int recordId) {
        return records.get(recordId);
    }

    public StaticCountryTitle getCountryTitle(int titleId) {
        return titles.get(titleId);
    }

    public int maxTitle() {
        return titles.size();
    }

    public Map<Integer, StaticCountryGlory> getGlorys() {
        return glorys;
    }

    public Map<Integer, StaticCountryTask> getTasks() {
        return tasks;
    }

    public Map<Integer, Map<Integer, StaticCountryRank>> getRanks() {
        return ranks;
    }

    public Map<Integer, StaticCountryDaily> getRecords() {
        return records;
    }


    public Map<Integer, Map<Integer, StaticCountryBuild>> getBuilds() {
        return builds;
    }

    public StaticCountryBuild getCountryBuild(int countryLv, int count) {
        if (!builds.containsKey(countryLv)) {
            return null;
        }
        return builds.get(countryLv).get(count);
    }

    public StaticCountryLevel getCountryLvExp(int countryLv) {
        return exps.get(countryLv);
    }

    public int getMaxLevel() {
        return exps.size();
    }

    public long getMaxExp() {
        int maxLv = getMaxLevel();
        StaticCountryLevel config = exps.get(maxLv);
        if (config == null) {
            return Integer.MAX_VALUE;
        }
        return config.getNeedExp();
    }

    public StaticCountryGovern getGovern(int governId, int count) {
//        return governs.get(governId, count);
        return getGovern(governId);
    }

    public StaticCountryGovern getGovern(int governId) {
        Collection<StaticCountryGovern> values = governs.values();
        if (values.size() != 0) {
            Iterator<StaticCountryGovern> iterator = values.iterator();
            while (iterator.hasNext()) {
                StaticCountryGovern next = iterator.next();
                if (next != null && next.getGovernId() == governId) {
                    return next;
                }
            }
        }
        return null;
    }

    public int getCallCount(int id) {
        return callCount.get(id);
    }

    public Map<Integer, List<StaticCountryTask>> getCountryTasks() {
        return countryTasks;
    }

    public void setCountryTasks(Map<Integer, List<StaticCountryTask>> countryTasks) {
        this.countryTasks = countryTasks;
    }

    public void makeCountryTask() {
        for (StaticCountryTask staticTask : tasks.values()) {
            List<StaticCountryTask> config = countryTasks.get(staticTask.getCountryLv());
            if (config == null) {
                config = new ArrayList<StaticCountryTask>();
                countryTasks.put(staticTask.getCountryLv(), config);
            }
            config.add(staticTask);
        }
    }

    public List<StaticCountryTask> getCountryTaskByLv(int countryLv) {
        return countryTasks.get(countryLv);
    }

    // 国家刚升级的时候[方法慎用]
    public StaticCountryHero getCountryHero(int country, int countryLv) {
        return countryHeroTable.get(country, countryLv);
    }

    public Integer getCountryByHeroId(int heroId) {
        return heroCountryMapper.get(heroId);
    }

    public Integer getHeroIdByMonsterId(int monsterId) {
        return monsterHeroMapper.get(monsterId);
    }

    public Map<Integer, StaticCountryHero> getHeros() {
        return heros;
    }

    public void setHeros(Map<Integer, StaticCountryHero> heros) {
        this.heros = heros;
    }

    public Integer getEscapeRate(int escapeType, int loyalty) {
        return heroEscapRate.get(escapeType, loyalty);
    }

    // O(1)
    public int heroGetRate(int getType, int level, int heroId) {
        StaticCountryHero countryHero = heros.get(heroId);
        if (countryHero == null) {
            LogHelper.CONFIG_LOGGER.error("countryHero is null. heroId:{}", heroId);
            return 100;
        }

        Integer getRate = 0;
        if (getType == 1) {
            getRate = heroGetRate1.get(heroId, level);
        } else if (getType == 2) {
            getRate = heroGetRate2.get(heroId, level);
        }

        if (getRate == null) {
            LogHelper.CONFIG_LOGGER.error("getRate is null heroId = " + heroId + ", level =" + level);
            return 100;
        }

        return getRate;
    }

    public void makeGetRate() {
        for (StaticCountryHero countryHero : heros.values()) {
            if (countryHero == null) {
                continue;
            }

            List<List<Integer>> getRate1 = countryHero.getGetRate1();
            if (getRate1 == null) {
                LogHelper.CONFIG_LOGGER.info("getRate1 config error!");
                continue;
            }

            List<List<Integer>> getRate2 = countryHero.getGetRate2();
            if (getRate2 == null) {
                LogHelper.CONFIG_LOGGER.info("getRate2 config error!");
                continue;
            }

            for (List<Integer> elem : getRate1) {
                if (elem == null) {
                    continue;
                }
                heroGetRate1.put(countryHero.getHeroId(), elem.get(0), elem.get(1));
            }

            for (List<Integer> elem : getRate2) {
                if (elem == null) {
                    continue;
                }
                heroGetRate2.put(countryHero.getHeroId(), elem.get(0), elem.get(1));
            }
        }
    }

    public boolean isCountryMonster(int monsterId) {
        return monsterHeroMapper.containsKey(monsterId);
    }

    public Map<Integer, StaticCountry> getCountryMap() {
        return countryMap;
    }

    public void setCountryMap(Map<Integer, StaticCountry> countryMap) {
        this.countryMap = countryMap;
    }
}
