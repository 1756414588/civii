package com.game.domain.s;

/**
 * @filename
 * @author 陈奎
 * @version 1.0
 * @time 2017-4-12 下午5:24:36
 * @describe
 */
public class ActShopItem {

	private int type;
	private int id;
	private int count;
	private int price;
	private int probability;

	public ActShopItem() {
	}

	public ActShopItem(int type, int id, int count, int price, int probability) {
		this.type = type;
		this.id = id;
		this.count = count;
		this.price = price;
		this.probability = probability;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

}
