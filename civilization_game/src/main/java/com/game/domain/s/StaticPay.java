package com.game.domain.s;

public class StaticPay {
	private int platNo;
	private int payId;
	private int monthCard;
	private int topup;
	private int extraGold;
	private int vipExp;
	private int money;

	public int getPlatNo() {
		return platNo;
	}

	public void setPlatNo(int platNo) {
		this.platNo = platNo;
	}

	public int getPayId() {
		return payId;
	}

	public void setPayId(int payId) {
		this.payId = payId;
	}

	public int getMonthCard() {
		return monthCard;
	}

	public void setMonthCard(int monthCard) {
		this.monthCard = monthCard;
	}

	public int getTopup() {
		return topup;
	}

	public void setTopup(int topup) {
		this.topup = topup;
	}

	public int getExtraGold() {
		return extraGold;
	}

	public void setExtraGold(int extraGold) {
		this.extraGold = extraGold;
	}

	public int getVipExp() {
		return vipExp;
	}

	public void setVipExp(int vipExp) {
		this.vipExp = vipExp;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	@Override
	public String toString() {
		return "StaticPay [platNo=" + platNo + ", payId=" + payId + ", monthCard=" + monthCard + ", topup=" + topup + ", extraGold=" + extraGold + ", vipExp="
				+ vipExp + "]";
	}
}
