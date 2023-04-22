package com.game.util.random;

/**
 * 圆桌判断因子对象
 * @author hero
 *
 */
public class RoundTableFactor {

	/** 物品ID*/
	private int itemId;
	
	/** 最小节点*/
	private int nodeMin;
	
	/** 最大节点*/
	private int nodeMax;

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getNodeMin() {
		return nodeMin;
	}

	public void setNodeMin(int nodeMin) {
		this.nodeMin = nodeMin;
	}

	public int getNodeMax() {
		return nodeMax;
	}

	public void setNodeMax(int nodeMax) {
		this.nodeMax = nodeMax;
	}

	@Override
	public String toString() {
		StringBuffer  buffer = new StringBuffer();
		buffer.append("[").append(itemId).append(",").append(nodeMin).append(",").append(nodeMax).append("]");
		return buffer.toString();
	}
	
	
}
