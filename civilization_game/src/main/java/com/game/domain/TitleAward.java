package com.game.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.pb.CommonPb;

public class TitleAward {

	private volatile int recv;

	private Map<Integer, Integer> hisRecv = new HashMap<>();

	public int getRecv() {
		return recv;
	}

	public void setRecv(int recv) {
		this.recv = recv;
	}

	public Map<Integer, Integer> getHisRecv() {
		return hisRecv;
	}

	public void setHisRecv(Map<Integer, Integer> hisRecv) {
		this.hisRecv = hisRecv;
	}

	public CommonPb.TitleAwardInfo encode() {
		CommonPb.TitleAwardInfo.Builder builder1 = CommonPb.TitleAwardInfo.newBuilder();
		builder1.setRecv(this.recv);
		this.getHisRecv().entrySet().forEach(x -> {
			CommonPb.TwoInt.Builder builder2 = CommonPb.TwoInt.newBuilder();
			builder2.setV1(x.getKey());
			builder2.setV2(x.getValue());
			builder1.addHisRecv(builder2);
		});
		return builder1.build();
	}

	public void decode(CommonPb.TitleAwardInfo ctitleAward) {
		this.recv = ctitleAward.getRecv();
		List<CommonPb.TwoInt> hisRecvList = ctitleAward.getHisRecvList();
		hisRecvList.forEach(x -> {
			getHisRecv().put(x.getV1(), x.getV2());
		});
	}
}
