package com.game.domain.s;

import com.game.domain.p.ActRecord;
import com.game.pb.CommonPb.ActivityCond;
import com.game.pb.CommonPb.Award;
import java.util.List;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/25 10:47
 **/
public class StaticTDSevenBoxAward {
	private int keyId;
	private int cond;
	private List<List<Integer>> awardList;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

	public ActivityCond.Builder warp(ActRecord actRecord) {
		ActivityCond.Builder builder = ActivityCond.newBuilder();
		builder.setKeyId(keyId);
		builder.setCond(cond);
		awardList.forEach(e -> {
			if (e.size() == 3) {
				builder.addAward(Award.newBuilder().setType(e.get(0)).setId(e.get(1)).setCount(e.get(2)).build());
			}
		});
		if (actRecord.getReceived().containsKey(keyId)) {
			builder.setIsAward(1);
		} else {
			builder.setIsAward(0);
		}
		return builder;
	}

	@Override
	public String toString() {
		return "StaticTDSevenTaskAward{" + "keyId=" + keyId + ", cond=" + cond + ", awardList=" + awardList + '}';
	}
}
