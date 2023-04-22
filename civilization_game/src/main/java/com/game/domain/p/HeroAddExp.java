package com.game.domain.p;

import java.util.HashMap;
import java.util.Map;

// 英雄增加的经验
public class HeroAddExp {
    private long lordId;
    private Map<Integer, Integer> heroAddExp = new HashMap<Integer, Integer>();

    public HeroAddExp() {
    }

    public HeroAddExp(long lordId) {
        this.lordId = lordId;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public Map<Integer, Integer> getHeroAddExp() {
        return heroAddExp;
    }

    public void setHeroAddExp(Map<Integer, Integer> heroAddExp) {
        this.heroAddExp = heroAddExp;
    }

    public void updateExp(long id, int heroId, int exp) {
        if (lordId != id) {
            return;
        }

        Integer curExp = heroAddExp.get(heroId);
        if (curExp == null) {
            heroAddExp.put(heroId, exp);
        } else {
            heroAddExp.put(heroId, curExp + exp);

        }

    }
}
