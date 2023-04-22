package com.game.domain.p;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.game.domain.Player;
import com.game.pb.DataPb;

public class CountryRank {

	// 类型 1.城战 2.国战 3.建设
	private int type;

	// 排行数据
	private LinkedList<CtyRank> rankList = new LinkedList<CtyRank>();

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void iniRank(List<DataPb.CtyRank> list) {
		if (list == null) {
			return;
		}
		for (DataPb.CtyRank e : list) {
			if (e.getV() <= 0) {
				continue;
			}
			CtyRank ctyRank = new CtyRank(e);
			rankList.add(ctyRank);
		}
		Collections.sort(rankList, new CtyRankDesc());
	}

	/**
	 * 获取页面数
	 * 
	 * @param page
	 * @param size
	 * @return
	 */
	public List<CtyRank> getTopRankList() {
		List<CtyRank> rtList = new ArrayList<CtyRank>();
		for (int i = 0; i < 5 && i < rankList.size(); i++) {
			CtyRank ctyRank = rankList.get(i);
			ctyRank.setRank(i + 1);
			rtList.add(ctyRank);
		}
		return rtList;
	}

	// public LinkedList<CtyRank> getRankList() {
	// return rankList;
	// }

	public void clearRankList() {
		synchronized (rankList) {
			if (!rankList.isEmpty())
				rankList.clear();
		}
	}

	/**
	 * 添加排行
	 * 
	 * @param player
	 * @param value
	 */
	public synchronized void addRank(Player player, int value) {
		long lordId = player.roleId;
		int size = rankList.size();
		long time = System.currentTimeMillis();
		if (rankList.isEmpty()) {
			CtyRank ctyRank = new CtyRank(lordId, value, time);
			rankList.add(ctyRank);
		} else if (size < 5) {
			boolean flag = false;
			Iterator<CtyRank> it = rankList.iterator();
			while (it.hasNext()) {
				CtyRank next = it.next();
				if (next.getLordId() == lordId) {
					next.setV(value);
					next.setTime(time);
					flag = true;
					break;
				}
			}
			if (!flag) {
				CtyRank ctyRank = new CtyRank(lordId, value, time);
				rankList.add(ctyRank);
			}
			Collections.sort(rankList, new CtyRankDesc());
		} else {
			CtyRank compareRank = rankList.getLast();
			if (compareRank.getV() >= value) {
				return;
			}

			boolean flag = false;
			Iterator<CtyRank> it = rankList.iterator();
			while (it.hasNext()) {
				CtyRank next = it.next();
				if (next.getLordId() == lordId) {
					next.setV(value);
					next.setTime(time);
					flag = true;
					break;
				}
			}

			if (!flag) {
				CtyRank ctyRank = new CtyRank(lordId, value, time);
				rankList.add(ctyRank);
			}

			Collections.sort(rankList, new CtyRankDesc());

			if (rankList.size() > 5) {
				rankList.removeLast();
			}
		}
	}

	public DataPb.CountryRankData ser() {
		DataPb.CountryRankData.Builder builder = DataPb.CountryRankData.newBuilder();
		builder.setType(type);
		List<CtyRank> topList = getTopRankList();
		for (CtyRank ctyRank : topList) {
			builder.addCtyRank(ctyRank.ser());
		}
		return builder.build();
	}
}
