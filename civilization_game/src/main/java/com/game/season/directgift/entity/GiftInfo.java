package com.game.season.directgift.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;

/**
 * 赛季 礼包
 */
public class GiftInfo extends BaseModule {

	private Map<Integer, Integer> map = new ConcurrentHashMap<>();

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_5;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			SeasonActivityPb.GiftInfoPb giftInfoPb = SeasonActivityPb.GiftInfoPb.parseFrom(seasonInfo.getInfo());
			giftInfoPb.getStateMapList().forEach(x -> {
				map.put(x.getV1(), x.getV2());
			});
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.GiftInfoPb.Builder builder = SeasonActivityPb.GiftInfoPb.newBuilder();
		for (Map.Entry<Integer, Integer> integerIntegerEntry : map.entrySet()) {
			builder.addStateMap(CommonPb.TwoInt.newBuilder().setV1(integerIntegerEntry.getKey()).setV2(integerIntegerEntry.getValue()).build());
		}
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {
		map.clear();
	}

	@Override
	public void clean(int actId) {

	}

	public int getRecount(int key) {
		return map.getOrDefault(key, 0);
	}

	public int add(int key) {
		return map.merge(key, 1, (a, b) -> a + b);
	}

}
