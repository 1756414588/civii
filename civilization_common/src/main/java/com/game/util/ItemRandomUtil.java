package com.game.util;


import com.game.util.random.BaseBoxItem;
import com.game.util.random.RoundTable;
import com.game.util.random.RoundTableFactor;

import java.util.ArrayList;
import java.util.List;

public class ItemRandomUtil {

	public static int get(RoundTable table) {
		int randomNumber = RandomUtil.randomBetween(1, table.getNodeMax() + 1);
		for (int j = 0; j < table.getTableFactor().size(); j++) {
			if (randomNumber >= table.getTableFactor().get(j).getNodeMin() && randomNumber <= table.getTableFactor().get(j).getNodeMax()) {
				return table.getTableFactor().get(j).getItemId();
			}
		}
		return -1;
	}

	/**
	 * 圆桌判断后获得箱子物品(不能重复选)
	 * 
	 * @param box
	 *            需要圆桌判断的箱子
	 * @param num
	 *            获取箱子物品个数
	 * @return
	 */
	public static <T extends BaseBoxItem> List<T> 	getBoxItem(List<T> box, int num) {
		List<T> boxCopy = new ArrayList<>();
		boxCopy.addAll(box);
		RoundTable table = getTable(boxCopy);// 圆桌分段
		List<T> itemList = new ArrayList<>();// 随机出来的物品列表
		for (int i = 0; i < num; i++) {
			int randomNumber = RandomUtil.randomBetween(1, table.getNodeMax() + 1);
			for (int j = 0; j < table.getTableFactor().size(); j++) {
				if (randomNumber >= table.getTableFactor().get(j).getNodeMin() && randomNumber <= table.getTableFactor().get(j).getNodeMax()) {
					itemList.add(boxCopy.remove(j));
					table = getTable(boxCopy);
					break;
				}
			}
		}
		return itemList;
	}

	/**
	 * 获取圆桌对象(箱子物品几率)
	 * 
	 * @param box
	 *            箱子
	 * @return
	 */
	public static <T extends BaseBoxItem> RoundTable getTable(List<T> box) {
		RoundTable table = new RoundTable();// 圆桌对象
		List<RoundTableFactor> tableFactorList = new ArrayList<RoundTableFactor>();// 圆桌因子列表
		int nodeMax = 0;// 圆桌最大节点
		for (int i = 0; i < box.size(); i++) {
			nodeMax += box.get(i).getProbability();// 计算圆桌最大节点

			RoundTableFactor tableFactor = new RoundTableFactor();// 圆桌因子
			tableFactor.setItemId(box.get(i).getMarkId());// 物品ID
			if (i == 0) {
				tableFactor.setNodeMin(1);
				tableFactor.setNodeMax(box.get(i).getProbability());
			} else {
				tableFactor.setNodeMin(tableFactorList.get(i - 1).getNodeMax() + 1);
				tableFactor.setNodeMax(tableFactorList.get(i - 1).getNodeMax() + box.get(i).getProbability());
			}
			tableFactorList.add(tableFactor);
		}
		table.setTableFactor(tableFactorList);
		table.setNodeMax(nodeMax);
		return table;
	}

	/**
	 * 圆桌判断后获得箱子物品（能重复选）
	 * 
	 * @param box
	 *            需要圆桌判断的箱子
	 * @param num
	 *            获取箱子物品个数
	 * @return
	 */
	public static <T extends BaseBoxItem> List<T> getBoxItemRepeat(List<T> box, int num) {
		List<T> boxCopy = new ArrayList<>();
		boxCopy.addAll(box);
		RoundTable table = getTable(boxCopy);// 圆桌分段
		List<T> itemList = new ArrayList<>();// 随机出来的物品列表
		for (int i = 0; i < num; i++) {
			int randomNumber = RandomUtil.randomBetween(1, table.getNodeMax() + 1);
			for (int j = 0; j < table.getTableFactor().size(); j++) {
				if (randomNumber >= table.getTableFactor().get(j).getNodeMin() && randomNumber <= table.getTableFactor().get(j).getNodeMax()) {
					itemList.add(boxCopy.get(j));
					table = getTable(boxCopy);
					break;
				}
			}
		}
		return itemList;
	}
}
