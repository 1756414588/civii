package com.game.worldmap.fight.process;

import com.game.constant.MarchState;
import com.game.define.Fight;
import com.game.domain.p.WorldMap;
import com.game.service.SuperResService;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.fight.IWar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "大型矿点", warType = {}, marthes = {MarchType.SUPER_ATTACK, MarchType.SUPER_ASSIST, MarchType.SUPER_COLLECT})
@Component
public class SuperResourceProcess extends FightProcess {

	@Autowired
	private SuperResService superResService;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		//注册行军
		registerMarch(MarchType.SUPER_ATTACK, MarchState.Begin, this::superResAttack);
		registerMarch(MarchType.SUPER_ASSIST, MarchState.Begin, this::marchEndHelpSuperMineLogic);
		registerMarch(MarchType.SUPER_ASSIST, MarchState.CityAssist, this::retreatHelpArmy);
		registerMarch(MarchType.SUPER_COLLECT, MarchState.Begin, this::marchEndcollectSuperMineLogic);

		registerMarch(MarchType.SUPER_ATTACK, MarchState.Back, this::doFinishedMarch);
		registerMarch(MarchType.SUPER_ASSIST, MarchState.Back, this::doFinishedMarch);
		registerMarch(MarchType.SUPER_COLLECT, MarchState.Back, this::doFinishedMarch);
	}

	private void superResAttack(MapInfo mapInfo, March march) {
		superResService.superResAttack(march, mapInfo);
	}

	private void marchEndHelpSuperMineLogic(MapInfo mapInfo, March march) {
		superResService.marchEndHelpSuperMineLogic(mapInfo, march);
	}

	private void retreatHelpArmy(MapInfo mapInfo, March march) {
		superResService.retreatHelpArmy(mapInfo, march);
	}

	private void marchEndcollectSuperMineLogic(MapInfo mapInfo, March march) {
		superResService.marchEndcollectSuperMineLogic(mapInfo, march);
	}


	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {

	}
}
