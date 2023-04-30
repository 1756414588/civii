package com.game.domain.p;

/**
 *
 * @date 2020/1/6 9:36
 * @description
 */
public class SurHeroSoldier implements Comparable<SurHeroSoldier> {
    private int heroId;

    private float surPercent;

    public SurHeroSoldier(int heroId, float surPercent) {
        this.heroId = heroId;
        this.surPercent = surPercent;
    }

    public int getHeroId() {

        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }


    @Override
    public int compareTo(SurHeroSoldier o) {
        if (this.surPercent > o.surPercent) {
            return 1;
        }
        if (this.surPercent < o.surPercent) {

            return -1;
        }
        return 0;
    }
}
