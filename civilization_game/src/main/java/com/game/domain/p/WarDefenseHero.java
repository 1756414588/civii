package com.game.domain.p;

/**
 *
 * @date 2019/12/18 11:12
 * @description 参谋部提供的城墙英雄
 */
public class WarDefenseHero implements Cloneable {

    private int heroId;//英雄id
    private long lastRefreshTime;//上次恢复的兵力的时间
    private boolean isAddSoldier = false;//是否正在补兵

    public boolean isAddSoldier() {
        return isAddSoldier;
    }

    public void setAddSoldier(boolean addSoldier) {
        isAddSoldier = addSoldier;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }


    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    public void setLastRefreshTime(long lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public WarDefenseHero(int heroId, int soldierNum, long lastRefreshTime) {
        this.heroId = heroId;
        this.lastRefreshTime = lastRefreshTime;
    }

    public WarDefenseHero() {
    }
    public void reset(int heroId){
        this.heroId = heroId;
        this.lastRefreshTime = System.currentTimeMillis();
        this.isAddSoldier = false;
    }



    @Override
    public WarDefenseHero clone() {
        WarDefenseHero warDefenseHero = null;
        try {
            warDefenseHero = (WarDefenseHero) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return warDefenseHero;
    }
}
