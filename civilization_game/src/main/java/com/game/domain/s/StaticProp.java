package com.game.domain.s;

import java.util.List;

public class StaticProp {
	private int propId;
	private String propName;
	private int propType;
	private int addType;
	private int shopType;
	private int price;
	private int canBuy;
	private int canUse;
	private List<List<Long>> effectValue;
	private int stackSize;
	private int canSell;
	private long sellIron;
	private int color;
	private int equipId;
	private int needNum;
	private int limitLevel;
	private int limitBattleScore;

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public int getPropId() {
		return propId;
	}

	public void setPropId(int propId) {
		this.propId = propId;
	}

	public int getPropType() {
		return propType;
	}

	public void setPropType(int propType) {
		this.propType = propType;
	}

	public int getAddType() {
		return addType;
	}

	public void setAddType(int addType) {
		this.addType = addType;
	}

	public int getShopType() {
		return shopType;
	}

	public void setShopType(int shopType) {
		this.shopType = shopType;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getCanBuy() {
		return canBuy;
	}

	public void setCanBuy(int canBuy) {
		this.canBuy = canBuy;
	}

	public int getCanUse() {
		return canUse;
	}

	public void setCanUse(int canUse) {
		this.canUse = canUse;
	}

	public List<List<Long>> getEffectValue() {
		return effectValue;
	}

	public void setEffectValue(List<List<Long>> effectValue) {
		this.effectValue = effectValue;
	}

	public int getStackSize() {
		return stackSize;
	}

	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}

	public int getCanSell() {
		return canSell;
	}

	public void setCanSell(int canSell) {
		this.canSell = canSell;
	}

	public long getSellIron() {
		return sellIron;
	}

	public void setSellIron(long sellIron) {
		this.sellIron = sellIron;
	}

    public int getColor () {
        return color;
    }

    public void setColor (int color) {
        this.color = color;
    }

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public int getNeedNum() {
        return needNum;
    }

    public void setNeedNum(int needNum) {
        this.needNum = needNum;
    }

	public int getLimitLevel() {
		return limitLevel;
	}

	public void setLimitLevel(int limitLevel) {
		this.limitLevel = limitLevel;
	}

	public int getLimitBattleScore() {
		return limitBattleScore;
	}

	public void setLimitBattleScore(int limitBattleScore) {
		this.limitBattleScore = limitBattleScore;
	}
}
