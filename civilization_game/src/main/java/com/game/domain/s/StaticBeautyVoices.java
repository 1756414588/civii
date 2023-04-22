package com.game.domain.s;
/**
*2020年6月8日
*@CaoBing
*halo_game
*StaticBeautyVoices.java
**/
public class StaticBeautyVoices {
	private int type;
	
	private int beautyId;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBeautyId() {
		return beautyId;
	}

	public void setBeautyId(int beautyId) {
		this.beautyId = beautyId;
	}

	@Override
	public String toString() {
		return "StaticBeautyVoices [type=" + type + ", beautyId=" + beautyId + "]";
	}
}
