package com.game.dataMgr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticBuyEqupSlot;
import com.game.domain.s.StaticEquip;

@Component
public class StaticEquipDataMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    //装备配置
    private Map<Integer, StaticEquip> equipMap = new HashMap<Integer, StaticEquip>();
    //购买背包格价格
    private Map<Integer, StaticBuyEqupSlot> buyEquipSlotMap = new HashMap<Integer, StaticBuyEqupSlot>();


    @Override
    public void init() throws Exception{
        equipMap = staticDataDao.selectEquipMap();
        buyEquipSlotMap = staticDataDao.selectBuyEquipSlot();

    }

    public int maxBuyEquipSlotTimes() {
        return buyEquipSlotMap.size();
    }

    public int getBuySlotPrice(int buyTimes) {
        StaticBuyEqupSlot buyEqupSlot = buyEquipSlotMap.get(buyTimes);
        if (buyEqupSlot == null) {
            return Integer.MAX_VALUE;
        }

        return buyEqupSlot.getPrice();
    }

    public int getEquipType(int equipId) {
        StaticEquip staticEquip = getEquipMap().get(equipId);
        if (staticEquip == null) {
            return Integer.MAX_VALUE;
        }

        return staticEquip.getEquipType();
    }


    public Map<Integer, StaticEquip> getEquipMap() {
        return equipMap;
    }

    public void setEquipMap(Map<Integer, StaticEquip> equipMap) {
        this.equipMap = equipMap;
    }

    public StaticEquip getStaticEquip(int equipId) {
        return equipMap.get(equipId);
    }

}
