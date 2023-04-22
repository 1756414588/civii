package com.game.domain.s;

/**
 * 2020年8月17日
 * 
 * @CaoBing halo_game StaticJourneyPrice.java
 **/
public class StaticJourneyPrice {
	private int journeyTimes;	//次数					
	private int price;			//价格				
	public int getJourneyTimes() {
		return journeyTimes;
	}
	public void setJourneyTimes(int journeyTimes) {
		this.journeyTimes = journeyTimes;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "StaticJourneyPrice [journeyTimes=" + journeyTimes + ", price=" + price + "]";
	}
}	
