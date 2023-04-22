package com.game.domain.s;
/**
*2020年6月2日
*@CaoBing
*halo_game
*StaticBeautyClothes.java
**/
public class StaticBeautyClothes {
	//服装ID
	private int keyId;
	//服装名称
	private String name;
	//美女ID
	private int beautyId;
	public int getKeyId() {
		return keyId;
	}
	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getBeautyId() {
		return beautyId;
	}
	public void setBeautyId(int beautyId) {
		this.beautyId = beautyId;
	}
	@Override
	public String toString() {
		return "StaticBeautyClothes [keyId=" + keyId + ", name=" + name + ", beautyId=" + beautyId + "]";
	}
}
