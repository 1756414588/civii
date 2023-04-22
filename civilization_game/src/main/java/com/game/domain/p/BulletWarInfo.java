package com.game.domain.p;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.pb.CommonPb;

public class BulletWarInfo {

	private int maxId;// 记录关卡id
	private Map<Integer, CommonPb.TwoInt> map = new ConcurrentHashMap<>();

	public int getMaxId() {
		return maxId;
	}

	public void setMaxId(int maxId) {
		this.maxId = maxId;
	}

	public Map<Integer, CommonPb.TwoInt> getMap() {
		return map;
	}

	public void setMap(Map<Integer, CommonPb.TwoInt> map) {
		this.map = map;
	}

	public CommonPb.BulletWarInfoPb encode() {
		CommonPb.BulletWarInfoPb.Builder builder = CommonPb.BulletWarInfoPb.newBuilder();
		builder.setMaxId(maxId);
		map.entrySet().forEach(x -> {
			CommonPb.BulletInfo.Builder builder1 = CommonPb.BulletInfo.newBuilder();
			builder1.setLevel(x.getKey());
			builder1.setInfo(x.getValue());
			builder.addBulletInfo(builder1);
		});
		return builder.build();
	}

	public void decode(CommonPb.BulletWarInfoPb info) {
		if (info != null) {
			this.maxId = info.getMaxId();
			List<CommonPb.BulletInfo> bulletInfoList = info.getBulletInfoList();
			if (bulletInfoList != null) {
				bulletInfoList.forEach(x -> {
					map.put(x.getLevel(), x.getInfo());
				});
			}
		}
	}
}
