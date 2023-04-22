package com.game.domain;

import com.game.constant.CityType;
import com.game.domain.p.BroodWarData;
import com.game.domain.p.BroodWarPosition;
import com.game.domain.p.BroodWarReport;
import com.game.worldmap.BroodWar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Brood {

	// 任命信息
	private List<BroodWarPosition> appoints = new ArrayList<>();
	// 战报信息
	private List<BroodWarReport> broodWarReportList = new ArrayList<>();
	// cityId,
	private Map<Integer, BroodWarData> broodWarDataMap = new HashMap<>();


	public void addBroodWar(int cityType, BroodWar broodWar) {
		BroodWarData data = new BroodWarData();
		data.dserData(broodWar);
		broodWarDataMap.put(data.getCityId(), data);

		if (cityType == CityType.WORLD_FORTRESS) {
			broodWarReportList.addAll(broodWar.getReports());
		}
	}

	public void addBroodWarPosition(BroodWarPosition broodWarPosition) {
		appoints.add(broodWarPosition);
	}


}
