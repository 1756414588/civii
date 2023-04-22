package com.game.season.seasongift.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;

/**
 * 赛季礼包
 */
public class SeasonGIftInfo extends BaseModule {
	private Map<Integer, Map<Integer, Integer>> map = new HashMap<>();

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_7;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			SeasonActivityPb.PayGiftInfoPb payGiftInfoPb = SeasonActivityPb.PayGiftInfoPb.parseFrom(seasonInfo.getInfo());
			List<CommonPb.ThreeInt> stateMapList = payGiftInfoPb.getStateMapList();
			stateMapList.forEach(x -> {
				Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(x.getV1(), y -> new HashMap<>());
				integerIntegerMap.put(x.getV2(), x.getV3());
			});
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.PayGiftInfoPb.Builder builder = SeasonActivityPb.PayGiftInfoPb.newBuilder();
		for (Map.Entry<Integer, Map<Integer, Integer>> integerMapEntry : map.entrySet()) {
			Integer key = integerMapEntry.getKey();
			Map<Integer, Integer> value = integerMapEntry.getValue();
			for (Map.Entry<Integer, Integer> integerIntegerEntry : value.entrySet()) {
				CommonPb.ThreeInt.Builder builder1 = CommonPb.ThreeInt.newBuilder();
				builder1.setV1(key);
				builder1.setV2(integerIntegerEntry.getKey());
				builder1.setV3(integerIntegerEntry.getValue());
				builder.addStateMap(builder1);
			}
		}
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {
		Map<Integer, Integer> integerIntegerMap = map.get(SeasonAct.ACT_7.getActId());
		if (integerIntegerMap != null) {
			integerIntegerMap.clear();
		}
	}

	@Override
	public void clean(int actId) {
		Map<Integer, Integer> integerIntegerMap = map.get(actId);
		if (integerIntegerMap != null) {
			integerIntegerMap.clear();
		}
	}

	public int getRecount(int actId, int key) {
		Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(actId, x -> new HashMap<>());
		return integerIntegerMap.getOrDefault(key, 0);
	}

	public int add(int actId, int key) {
		Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(actId, x -> new HashMap<>());
		return integerIntegerMap.merge(key, 1, (a, b) -> a + b);
	}
}
