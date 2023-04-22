package com.game.season.talent.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;

public class TalentInfo extends BaseModule {
	private int freeTime = 1;// 免费次数
	private int typeId;// 1.当前选择的type
	private int state = 1;// 2.已开启
	private int progress;
	private Map<Integer, Map<Integer, Integer>> map = new HashMap<>();

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_10;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			SeasonActivityPb.TalentInfoPb talentInfoPb = SeasonActivityPb.TalentInfoPb.parseFrom(seasonInfo.getInfo());
			this.freeTime = talentInfoPb.getFreeTime();
			this.typeId = talentInfoPb.getTypeId();
			this.state = talentInfoPb.getState();
			this.progress = talentInfoPb.getProgress();
			List<CommonPb.ThreeInt> scoreMapList = talentInfoPb.getScoreMapList();
			if (scoreMapList != null) {
				scoreMapList.forEach(x -> {
					Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(x.getV1(), y -> new HashMap<>());
					integerIntegerMap.put(x.getV2(), x.getV3());
				});
			}
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.TalentInfoPb.Builder builder = SeasonActivityPb.TalentInfoPb.newBuilder();
		builder.setFreeTime(this.freeTime);
		builder.setTypeId(this.typeId);
		builder.setState(this.state);
		builder.setProgress(this.progress);
		for (Map.Entry<Integer, Map<Integer, Integer>> integerMapEntry : map.entrySet()) {
			Map<Integer, Integer> value = integerMapEntry.getValue();
			for (Map.Entry<Integer, Integer> integerIntegerEntry : value.entrySet()) {
				CommonPb.ThreeInt.Builder builder1 = CommonPb.ThreeInt.newBuilder();
				builder1.setV1(integerMapEntry.getKey());
				builder1.setV2(integerIntegerEntry.getKey());
				builder1.setV3(integerIntegerEntry.getValue());
				builder.addScoreMap(builder1);
			}
		}
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {
		freeTime = 1;
		typeId = 0;
		state = 1;
		progress = 0;
		map.clear();
	}

	@Override
	public void clean(int actId) {

	}

	public int getFreeTime() {
		return freeTime;
	}

	public void setFreeTime(int freeTime) {
		this.freeTime = freeTime;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Map<Integer, Map<Integer, Integer>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, Map<Integer, Integer>> map) {
		this.map = map;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int addPro() {
		this.progress += 10;
		return this.progress;
	}

	public void updateTalentId(StaticCompTalentUp staticCompTalentUp) {
		Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(staticCompTalentUp.getChildType(), x -> new HashMap<>());
		integerIntegerMap.put(staticCompTalentUp.getTypeId(), staticCompTalentUp.getKeyId());
	}

	public int getTalentId(int childType, int typeId) {
		Map<Integer, Integer> integerIntegerMap = map.computeIfAbsent(childType, x -> new HashMap<>());
		return integerIntegerMap.getOrDefault(typeId, 0);
	}
}
