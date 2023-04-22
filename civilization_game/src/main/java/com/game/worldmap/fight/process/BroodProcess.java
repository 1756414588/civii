package com.game.worldmap.fight.process;

import com.game.constant.MarchState;
import com.game.constant.WarType;
import com.game.define.Fight;
import com.game.domain.p.WorldMap;
import com.game.manager.BroodWarManager;
import com.game.worldmap.MapInfo;
import com.game.worldmap.MarchType;
import com.game.worldmap.fight.IWar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "母巢之战", warType = {}, marthes = {MarchType.BROOD_WAR})
@Component
public class BroodProcess extends FightProcess {

	@Autowired
	private BroodWarManager broodWarManager;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		// 注册行军
		registerMarch(MarchType.BROOD_WAR, MarchState.Begin, broodWarManager::handleMarchArrive);
		registerMarch(MarchType.BROOD_WAR, MarchState.Back, this::doFinishedMarch);
	}


	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
	}
}
