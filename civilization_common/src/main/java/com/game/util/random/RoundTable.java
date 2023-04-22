package com.game.util.random;

import java.util.List;


public class RoundTable {
	/** 圆桌因子*/
	private List<RoundTableFactor> tableFactor;
	
	/** 圆桌最大节点*/
	private int nodeMax;
	
	public List<RoundTableFactor> getTableFactor() {
		return tableFactor;
	}

	public void setTableFactor(List<RoundTableFactor> tableFactor) {
		this.tableFactor = tableFactor;
	}

	public int getNodeMax() {
		return nodeMax;
	}

	public void setNodeMax(int nodeMax) {
		this.nodeMax = nodeMax;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{ max = ").append(nodeMax).append(";");
		for (RoundTableFactor roundTableFactor : tableFactor) {
			buf.append(roundTableFactor.toString());
		}
		buf.append("}");
		return super.toString();
	}
	
}
