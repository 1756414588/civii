package com.game.domain.s;

import java.util.List;

public class StaticVip {
	private int vip;// vip等级
	private int vipExp;
	private int topup;// 充值金币
	private int buyPower;// 买体力次数
	private int freeTime;// 免费时间
	private int autoBuild;// 自动建造次数
	private int skipFight;// 跳过战斗
	private int callArmy;// 免费招回行军次
	private int speedCollect;// 武卒官加速募兵 50为50%
	private int wipeCombat;// 0不可 1通过即可扫荡副本
	private int primeCost;// 原价
	private int price;// VIP礼包价格
	private List<List<Integer>> giftList;// vip礼包
	private List<List<Integer>> specialGift;
	private int appointmentBuytime;// 约会可购买次数
	private List<Integer> appointmentPrice;// 约会购买次数对应的价格 [首次购买，第二次购买，第三次购买....]
	private int gameTime;// 小游戏次数
	private int gameBuytime;// 小游戏购买次数
	private List<Integer> gamePrice;// 小游戏购买价格
	private int journeyBuyTime;//征途购买次数

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public int getTopup() {
		return topup;
	}

	public void setTopup(int topup) {
		this.topup = topup;
	}

	public int getBuyPower() {
		return buyPower;
	}

	public void setBuyPower(int buyPower) {
		this.buyPower = buyPower;
	}

	public int getFreeTime() {
		return freeTime;
	}

	public void setFreeTime(int freeTime) {
		this.freeTime = freeTime;
	}

	public int getAutoBuild() {
		return autoBuild;
	}

	public void setAutoBuild(int autoBuild) {
		this.autoBuild = autoBuild;
	}

	public int getSkipFight() {
		return skipFight;
	}

	public void setSkipFight(int skipFight) {
		this.skipFight = skipFight;
	}

	public int getCallArmy() {
		return callArmy;
	}

	public void setCallArmy(int callArmy) {
		this.callArmy = callArmy;
	}

	public int getSpeedCollect() {
		return speedCollect;
	}

	public void setSpeedCollect(int speedCollect) {
		this.speedCollect = speedCollect;
	}

	public int getWipeCombat() {
		return wipeCombat;
	}

	public void setWipeCombat(int wipeCombat) {
		this.wipeCombat = wipeCombat;
	}

	public int getPrimeCost() {
		return primeCost;
	}

	public void setPrimeCost(int primeCost) {
		this.primeCost = primeCost;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public List<List<Integer>> getGiftList() {
		return giftList;
	}

	public void setGiftList(List<List<Integer>> giftList) {
		this.giftList = giftList;
	}

	public int getVipExp() {
		return vipExp;
	}

	public void setVipExp(int vipExp) {
		this.vipExp = vipExp;
	}

	public int getAppointmentBuytime() {
		return appointmentBuytime;
	}

	public void setAppointmentBuytime(int appointmentBuytime) {
		this.appointmentBuytime = appointmentBuytime;
	}

	public List<Integer> getAppointmentPrice() {
		return appointmentPrice;
	}

	public void setAppointmentPrice(List<Integer> appointmentPrice) {
		this.appointmentPrice = appointmentPrice;
	}

	public int getGameTime() {
		return gameTime;
	}

	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
	}

	public int getGameBuytime() {
		return gameBuytime;
	}

	public void setGameBuytime(int gameBuytime) {
		this.gameBuytime = gameBuytime;
	}

	public List<Integer> getGamePrice() {
		return gamePrice;
	}

	public void setGamePrice(List<Integer> gamePrice) {
		this.gamePrice = gamePrice;
	}

	public List<List<Integer>> getSpecialGift() {
		return specialGift;
	}

	public void setSpecialGift(List<List<Integer>> specialGift) {
		this.specialGift = specialGift;
	}

	@Override
	public String toString() {
		return "StaticVip [vip=" + vip + ", vipExp=" + vipExp + ", topup=" + topup + ", buyPower=" + buyPower + ", freeTime=" + freeTime + ", autoBuild="
				+ autoBuild + ", skipFight=" + skipFight + ", callArmy=" + callArmy + ", speedCollect=" + speedCollect + ", wipeCombat=" + wipeCombat
				+ ", primeCost=" + primeCost + ", price=" + price + ", giftList=" + giftList + ", appointmentBuytime=" + appointmentBuytime
				+ ", appointmentPrice=" + appointmentPrice + ", gameTime=" + gameTime + ", gameBuytime=" + gameBuytime + ", gamePrice=" + gamePrice + "]";
	}

	public int getJourneyBuyTime() {
		return journeyBuyTime;
	}

	public void setJourneyBuyTime(int journeyBuyTime) {
		this.journeyBuyTime = journeyBuyTime;
	}
}
