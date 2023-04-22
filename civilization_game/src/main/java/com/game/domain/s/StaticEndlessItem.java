package com.game.domain.s;

import java.util.List;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/8 11:28
 **/
public class StaticEndlessItem {
	private int propId;
	private int tdType;
	private String typeDesc;
	private List<List<Integer>> effect;
	private String desc1;
	private String desc2;
	private Integer color;

	public int getPropId() {
		return propId;
	}

	public void setPropId(int propId) {
		this.propId = propId;
	}

	public int getTdType() {
		return tdType;
	}

	public void setTdType(int tdType) {
		this.tdType = tdType;
	}

	public String getTypeDesc() {
		return typeDesc;
	}

	public void setTypeDesc(String typeDesc) {
		this.typeDesc = typeDesc;
	}

	public List<List<Integer>> getEffect() {
		return effect;
	}

	public void setEffect(List<List<Integer>> effect) {
		this.effect = effect;
	}

	public String getDesc1() {
		return desc1;
	}

	public void setDesc1(String desc1) {
		this.desc1 = desc1;
	}

	public String getDesc2() {
		return desc2;
	}

	public void setDesc2(String desc2) {
		this.desc2 = desc2;
	}

	public Integer getColor() {
		return color;
	}

	public void setColor(Integer color) {
		this.color = color;
	}

	@Override
	public String toString() {
		return "StaticEndlessItem{" +
			"propId=" + propId +
			", tdType=" + tdType +
			", typeDesc='" + typeDesc + '\'' +
			", effect=" + effect +
			", desc1='" + desc1 + '\'' +
			", desc2='" + desc2 + '\'' +
			", color='" + color + '\'' +
			'}';
	}
}
