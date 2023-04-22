package com.game.domain.s;

import java.util.List;
import java.util.Map;

public class StaticIniLord {
	private int keyId;
	private int level;
	private int vip;
	private int goldGive;
	private int ranks;
	private int command;
	private int fameLv;
	private int honour;
	private int combat;
	private int elite;
	private int prosMax;
	private int power;
	private long newState;
	private Map<Integer, Integer> tanks;
	private Map<Integer, Integer> props;
	private List<Integer> equips;
    private List<Integer> heros;
    private int stone;
	private int iron;
	private int oil;
	private int copper;
	private int silicon;
	private int washSkillTimes;
	private int washHeroTimes;
    private Map<Integer, Integer> openBuildings;



    public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public int getGoldGive() {
		return goldGive;
	}

	public void setGoldGive(int goldGive) {
		this.goldGive = goldGive;
	}

	public int getRanks() {
		return ranks;
	}

	public void setRanks(int ranks) {
		this.ranks = ranks;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	public int getFameLv() {
		return fameLv;
	}

	public void setFameLv(int fameLv) {
		this.fameLv = fameLv;
	}

	public int getHonour() {
		return honour;
	}

	public void setHonour(int honour) {
		this.honour = honour;
	}

	public int getCombat() {
		return combat;
	}

	public void setCombat(int combat) {
		this.combat = combat;
	}

	public int getElite() {
		return elite;
	}

	public void setElite(int elite) {
		this.elite = elite;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public long getNewState() {
		return newState;
	}

	public void setNewState(long newState) {
		this.newState = newState;
	}

	public int getProsMax() {
		return prosMax;
	}

	public void setProsMax(int prosMax) {
		this.prosMax = prosMax;
	}

	public Map<Integer, Integer> getTanks() {
		return tanks;
	}

	public void setTanks(Map<Integer, Integer> tanks) {
		this.tanks = tanks;
	}

	public int getStone() {
		return stone;
	}

	public void setStone(int stone) {
		this.stone = stone;
	}

	public int getIron() {
		return iron;
	}

	public void setIron(int iron) {
		this.iron = iron;
	}

	public int getOil() {
		return oil;
	}

	public void setOil(int oil) {
		this.oil = oil;
	}

	public int getCopper() {
		return copper;
	}

	public void setCopper(int copper) {
		this.copper = copper;
	}

	public int getSilicon() {
		return silicon;
	}

	public void setSilicon(int silicon) {
		this.silicon = silicon;
	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public Map<Integer, Integer> getProps() {
		return props;
	}

	public void setProps(Map<Integer, Integer> props) {
		this.props = props;
	}


	public int getWashSkillTimes() {
		return washSkillTimes;
	}

	public void setWashSkillTimes(int washSkillTimes) {
		this.washSkillTimes = washSkillTimes;
	}

    public int getWashHeroTimes () {
        return washHeroTimes;
    }

    public void setWashHeroTimes(int washHeroTimes) {
        this.washHeroTimes = washHeroTimes;
    }

    public List<Integer> getHeros () {
        return heros;
    }

    public void setHeros (List<Integer> heros) {
        this.heros = heros;
    }

    public List<Integer> getEquips () {
        return equips;
    }

    public void setEquips (List<Integer> equips) {
        this.equips = equips;
    }

    public Map<Integer, Integer> getOpenBuildings() {
        return openBuildings;
    }

    public void setOpenBuildings(Map<Integer, Integer> openBuildings) {
        this.openBuildings = openBuildings;
    }
}
