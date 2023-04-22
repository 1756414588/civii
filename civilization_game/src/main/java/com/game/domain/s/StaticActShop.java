package com.game.domain.s;

import java.util.ArrayList;
import java.util.List;

import com.game.util.RandomHelper;

/**
 * @filename
 * @author 陈奎
 * @version 1.0
 * @time 2017-3-7 下午7:27:18
 * @describe
 */
public class StaticActShop {

	private int shopId;
	private int awardId;
	private int discount;
	private String param;
	private int grid;
	private List<List<Integer>> sellList;

	private int probability;
	private List<ActShopItem> actShopItems = new ArrayList<ActShopItem>();

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getDiscount() {
		return discount;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public int getGrid() {
		return grid;
	}

	public void setGrid(int grid) {
		this.grid = grid;
	}

	public List<List<Integer>> getSellList() {
		return sellList;
	}

	public void setSellList(List<List<Integer>> sellList) {
		this.sellList = sellList;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public List<ActShopItem> getActShopItems() {
		return actShopItems;
	}

	public void setActShopItems(List<ActShopItem> actShopItems) {
		this.actShopItems = actShopItems;
	}

	public ActShopItem randomItem() {
		int random = RandomHelper.randomInSize(this.probability);
		for (ActShopItem shopItem : actShopItems) {
			if (random <= shopItem.getProbability()) {
				return shopItem;
			}
		}
		return actShopItems.get(0);
	}
}
