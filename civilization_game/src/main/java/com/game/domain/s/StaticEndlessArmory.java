package com.game.domain.s;

import com.game.util.RandomUtil;
import java.util.List;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/1 14:30
 **/
public class StaticEndlessArmory {
	private int id;
	private List<List<Integer>> prop;
	private int coin_price; // 兑换商店消耗塔防币
	private int price; // 军械商店消耗钻石
	private int buy_time;
	private List<List<Integer>> discount;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<List<Integer>> getProp() {
		return prop;
	}

	public void setProp(List<List<Integer>> prop) {
		this.prop = prop;
	}

	public int getCoin_price() {
		return coin_price;
	}

	public void setCoin_price(int coin_price) {
		this.coin_price = coin_price;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getBuy_time() {
		return buy_time;
	}

	public void setBuy_time(int buy_time) {
		this.buy_time = buy_time;
	}

	public List<List<Integer>> getDiscount() {
		return discount;
	}

	public void setDiscount(List<List<Integer>> discount) {
		this.discount = discount;
	}

	public int getGoodsDiscount() {
		int randomNumber = RandomUtil.getRandomNumber(getDiscount().stream().mapToInt(e->e.get(1)).sum()) ;
		int total = 0;
		for (List<Integer> list : getDiscount()) {
			total += list.get(1);
			if (total > randomNumber) {
				return list.get(0);
			}
		}
		return 0;
	}
}
