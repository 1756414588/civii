package com.game.domain.p;

import java.util.HashMap;

// 记录击杀的实体的等级和数量
public class RecordEntity {
    // quality, exp
    private HashMap<Integer, Integer> entityMap = new HashMap<Integer, Integer>();
    public RecordEntity() {
    }

    public RecordEntity(int quality, int num) {
        update(quality, num);
    }

    public void update(int quality, int num) {
        Integer value = entityMap.get(quality);
        if (value == null) {
            entityMap.put(quality, num);
        } else {
            entityMap.put(quality, value + num);
        }
    }

    public HashMap<Integer, Integer> getEntityMap() {
        return entityMap;
    }

    public void setEntityMap(HashMap<Integer, Integer> entityMap) {
        this.entityMap = entityMap;
    }
}
