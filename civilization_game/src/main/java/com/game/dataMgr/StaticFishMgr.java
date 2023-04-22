package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.p.ConfigException;
import com.game.domain.s.*;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Component
public class StaticFishMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticFish> fishMap;
    private Map<Integer, StaticFishBait> fishBaitMap;
    private Map<Integer, StaticFishHeroGroup> fishHeroGroupMap;
    private Map<Integer, StaticFishLv> fishLvMap;
    private Map<Integer, StaticFishShop> fishShopMap;

    @Override
    public void init() throws Exception {

        fishMap = staticDataDao.loadStaticFish();
        fishBaitMap = staticDataDao.loadStaticFishBait();
        fishHeroGroupMap = staticDataDao.loadStaticFishHeroGroup();
        fishLvMap = staticDataDao.loadStaticFishLv();
        fishShopMap = staticDataDao.loadStaticFishShop();

        this.check();
    }

    public void check() throws ConfigException {
        if (fishMap.isEmpty() || fishBaitMap.isEmpty() || fishHeroGroupMap.isEmpty() || fishLvMap.isEmpty() || fishShopMap.isEmpty()) {
            throw new ConfigException("钓鱼相关配置为空,数据异常");
        }
    }

}
