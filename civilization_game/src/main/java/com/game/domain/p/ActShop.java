package com.game.domain.p;

import java.util.HashMap;
import java.util.Map;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-3-8 上午10:35:23
 * @describe
 */
public class ActShop {

	private int activityId;
	private int refresh;
	private int refreshTime;
	private int beginTime;
	private int buyCount;
	private int goldRefresh;

	private Map<Integer, ActShopProp> shops = new HashMap<Integer, ActShopProp>();

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getRefresh() {
		return refresh;
	}

	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}

	public int getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}

	public int getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(int beginTime) {
		this.beginTime = beginTime;
	}

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}

	public int getGoldRefresh() {
		return goldRefresh;
	}

	public void setGoldRefresh(int goldRefresh) {
		this.goldRefresh = goldRefresh;
	}

	public Map<Integer, ActShopProp> getShops() {
		return shops;
	}

	public void setShops(Map<Integer, ActShopProp> shops) {
		this.shops = shops;
	}

	public ActShopProp getShopPropByGrid(int grid) {
		return this.shops.get(grid);
	}

}
