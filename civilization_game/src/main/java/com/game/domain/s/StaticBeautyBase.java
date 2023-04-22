package com.game.domain.s;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020年5月29日
 *
 * @CaoBing halo_game StaticBeautyBase.java
 * <p>
 * 美女的配置信息
 **/
public class StaticBeautyBase {

	// keydId
	public int keyId;
	// 美女名称
	private String name;

	private List<Integer> param = new ArrayList<>();

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

	public List<Integer> getParam() {
		return param;
	}

	public void setParam(List<Integer> param) {
		this.param = param;
	}

	@Override
	public String toString() {
		return "StaticBeautyBase{" +
			"keyId=" + keyId +
			", name='" + name + '\'' +
			'}';
	}
}
