package com.game.dataMgr;

import com.game.define.LoadData;
import com.game.domain.s.*;
import com.google.common.collect.HashBasedTable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.ResourceType;
import com.game.dao.s.StaticDataDao;
import com.game.util.LogHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@LoadData(name = "Limit配置")
public class StaticLimitMgr extends BaseDataMgr {
    final static public int SECOND_MS = 1000;

    @Autowired
    private StaticDataDao staticDataDao;

    private StaticLimit staticLimit = new StaticLimit();
    private Map<Integer, StaticEnergyPrice> staticEnergyPriceMap = new HashMap<Integer, StaticEnergyPrice>();
    private Map<Integer, StaticSimpleConfig> simpleConfig = new HashMap<Integer, StaticSimpleConfig>();
    private Map<Integer, StaticRegisterConfig> registerConfig = new HashMap<Integer, StaticRegisterConfig>();
    private Map<Integer, Long> resourceLimit = new HashMap<Integer, Long>();
    @Getter
    @Setter
    private Map<Integer, StaticMailAward> staticMailMap = new ConcurrentHashMap<>();

    private HashBasedTable<Integer, Integer, StaticGroup> groupMap = HashBasedTable.create();


    @Override
    public void load() throws Exception {
        staticLimit = staticDataDao.selectLimit();
        if (staticLimit == null) {
            throw new NullPointerException("staticLimit is null!");
        }
        staticEnergyPriceMap = staticDataDao.selectEnergyPrice();
        //配置检测
        if(staticLimit.getCollectInterval() == 0) {
            LogHelper.CONFIG_LOGGER.info("CollectInterval err, value = " + staticLimit.getCollectInterval());
            staticLimit.setCollectInterval(3600);
        }

        simpleConfig = staticDataDao.selectSimpleConfig();
        registerConfig = staticDataDao.selectRegisterConfig();
        makeResourceLimit();
        initStaticMailAward();
        initGroup();
    }

    @Override
    public void init() throws Exception{

    }

    private void initStaticMailAward(){
        staticMailMap = staticDataDao.loadStaticMailAward();
    }

    public int getInitEquipSlot() {
        return staticLimit.getInitEquipSlot();
    }

    public int getEquipSlotNum() {
        return staticLimit.getEquipSlotNum();
    }

    public int getInitSlot() {
        return staticLimit.getInitEquipSlot();
    }

    public StaticLimit getStaticLimit() {
        return staticLimit;
    }

    public int getSoldierLines() {
        return staticLimit.getSoliderLines();
    }

    public int getCollectInterval() {
        return staticLimit.getCollectInterval() * SECOND_MS;
    }

    public int getMaxCollectTimes() {
        return staticLimit.getMaxCollectTimes();
    }

    public int getEnergyInterval () {
        return staticLimit.getEnergyInterval() * SECOND_MS;
    }

    public int getMaxEnegy () {
        return staticLimit.getMaxEnegy();
    }

    public int getMaxUseEnergy() {
        return staticLimit.getMaxUseEnergy();
    }



    public long getLimitNum(int resType) {
        return this.resourceLimit.get(resType);
    }

    public int minBuildTeams() {
        return staticLimit.getMinBuildTeams();
    }


    public long getMaxHonor () {
        return staticLimit.getMaxHonor();
    }

    public int getBuildTimePrice () {
        return staticLimit.getBuildTimePrice();
    }

    public StaticEnergyPrice getStaticEnergyPrice(int buyEnergyTimes) {
        return staticEnergyPriceMap.get(buyEnergyTimes);
    }

    public int getNum(int id) {
        StaticSimpleConfig config = simpleConfig.get(id);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.error("StaticSimpleConfig is null in getNum, id = " + id);
            return 0;
        }

        return config.getConfigNum();
    }

    public Map<Integer, Long> getResourceLimit() {
        return resourceLimit;
    }

    public void setResourceLimit(Map<Integer, Long> resourceLimit) {
        this.resourceLimit = resourceLimit;
    }

    public void makeResourceLimit() {
        this.resourceLimit.put(ResourceType.IRON, staticLimit.getMaxIron());
        this.resourceLimit.put(ResourceType.COPPER, staticLimit.getMaxCopper());
        this.resourceLimit.put(ResourceType.OIL, staticLimit.getMaxOil());
        this.resourceLimit.put(ResourceType.STONE, staticLimit.getMaxStone());
    }

    public boolean isSimpleWarOpen() {
        return false;
    }

    public List<Integer> getAddtion(int id) {
        StaticSimpleConfig config = simpleConfig.get(id);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.error("StaticSimpleConfig is null in getNum, id = " + id);
            return null;
        }

        return config.getAddition();
    }
    public List<List<Integer>> getRegisterAdditions(int id) {
        StaticRegisterConfig config = registerConfig.get(id);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.error("StaticRegisterConfig is null in getNum, id = " + id);
            return null;
        }

        return config.getAwardList();
    }
    
    public boolean isCloseCtyHero() {
        return getNum(152) == 1;
    }

    public float getResFactor() {
        return (float)getNum(168) / 100.0f;
    }

    public float getCenterResFactor() {
        return (float)getNum(252) / 100.0f;
    }

    public int getMaxWashHero() {
        Integer num = getNum(172);
        return num;
    }

    public int getMaxWashEquip() {
        Integer num = getNum(173);
        return num;
    }

    public List<List<Integer>> getCleanAccount(){
        return staticLimit.getCleanAccount();
    }

    public List<List<Integer>> getWarBookShop(){
        return staticLimit.getWarbookShop();
    }

    public void initGroup() {
        groupMap.clear();
        List<StaticGroup> staticGroups = staticDataDao.queryGroup();
        staticGroups.forEach(x -> {
            groupMap.put(x.getChannel(), x.getCountry(), x);
        });
    }

    public StaticGroup getStaticGroup(int channel, int country) {
        return groupMap.get(channel, country);
    }
}
