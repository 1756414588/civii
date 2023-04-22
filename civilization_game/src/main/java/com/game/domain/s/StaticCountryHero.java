package com.game.domain.s;

import java.util.List;

public class StaticCountryHero {
	private int keyId;
	private int country;
	private int level;
	private int heroId;
	private int monsterId;
	private List<Integer> propId;
	private int propNum;
    private List<List<Integer>> getRate1;
    private List<List<Integer>> getRate2;
    private List<List<Integer>> activate;



    public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public List<Integer> getPropId() {
        return propId;
    }

    public void setPropId(List<Integer> propId) {
        this.propId = propId;
    }

    public int getPropNum() {
        return propNum;
    }

    public void setPropNum(int propNum) {
        this.propNum = propNum;
    }

    public List<List<Integer>> getGetRate1() {
        return getRate1;
    }

    public void setGetRate1(List<List<Integer>> getRate1) {
        this.getRate1 = getRate1;
    }

    public List<List<Integer>> getGetRate2() {
        return getRate2;
    }

    public void setGetRate2(List<List<Integer>> getRate2) {
        this.getRate2 = getRate2;
    }

    public List<List<Integer>> getActivate() {
        return activate;
    }

    public void setActivate(List<List<Integer>> activate) {
        this.activate = activate;
    }
}
