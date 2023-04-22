package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StaticFishLv {

    private int id;
    private int lv;
    private String name;
    private int exp;
    private String desc;
    private Map<Integer, StaticFishLevelCritBuff> hit;
    private int speed;
    private StaticFishLevelSizeBuff size;

    public void setHit(List<List<Integer>> hit) {
        Map<Integer, StaticFishLevelCritBuff> hitMap = new HashMap<>();
        // 遍历List
        for (List<Integer> list : hit) {
            StaticFishLevelCritBuff staticFishLevelCritBuff = new StaticFishLevelCritBuff();
            staticFishLevelCritBuff.setMultiple(list.get(0));
            staticFishLevelCritBuff.setProbability(list.get(1));
            hitMap.put(list.get(0), staticFishLevelCritBuff);
        }
        this.hit = hitMap;
    }

    public void setSize(List<Integer> size) {
        StaticFishLevelSizeBuff staticFishLevelSizeBuff = new StaticFishLevelSizeBuff();
        staticFishLevelSizeBuff.setMin(size.get(0));
        staticFishLevelSizeBuff.setMax(size.get(1));
        this.size = staticFishLevelSizeBuff;
    }
}
