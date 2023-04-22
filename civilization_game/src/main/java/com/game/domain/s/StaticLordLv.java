package com.game.domain.s;

import java.util.List;

public class StaticLordLv {
	private int lordLv;
	private int needExp;
    private int miss;
    private int hit;
    private int criti;
    private int tenacity;
    private List<List<Integer>> awards;
    private int nextLv;


	public int getLordLv() {
		return lordLv;
	}

	public void setLordLv(int lordLv) {
		this.lordLv = lordLv;
	}

	public int getNeedExp() {
		return needExp;
	}

	public void setNeedExp(int needExp) {
		this.needExp = needExp;
	}

    public int getMiss() {
        return miss;
    }

    public void setMiss(int miss) {
        this.miss = miss;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getCriti() {
        return criti;
    }

    public void setCriti(int criti) {
        this.criti = criti;
    }

    public int getTenacity() {
        return tenacity;
    }

    public void setTenacity(int tenacity) {
        this.tenacity = tenacity;
    }

    public List<List<Integer>> getAwards() {
        return awards;
    }

    public void setAwards(List<List<Integer>> awards) {
        this.awards = awards;
    }

    public int getNextLv() {
        return nextLv;
    }

    public void setNextLv(int nextLv) {
        this.nextLv = nextLv;
    }
}
