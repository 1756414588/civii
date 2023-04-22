package com.game.worldmap.fight.process;

import com.game.constant.LostTargetReason;
import com.game.constant.MarchState;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticWorldMonster;
import com.game.util.LogHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.EntityType;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Monster;
import com.game.worldmap.WorldLogic;
import com.game.worldmap.fight.IWar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "攻打野怪", warType = {}, marthes = {MarchType.AttackMonster})
@Component
public class MonsterWarProcess extends FightProcess {

	@Autowired
	private WorldLogic worldLogic;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		//注册行军
		registerMarch(MarchType.AttackMonster, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.AttackMonster, MarchState.Back, this::doFinishedMarch);
	}

	/**
	 * 行军抵达
	 *
	 * @param mapInfo
	 * @param march
	 */
	private void marchArrive(MapInfo mapInfo, March march) {
//		LogHelper.GAME_LOGGER.info("行军抵达");
		worldManager.synMarch(mapInfo.getMapId(), march);
		killMonster(mapInfo, march);
	}


	public void killMonster(MapInfo mapInfo, March march) {
//		LogHelper.GAME_LOGGER.info("击杀怪物");
		Entity entity = mapInfo.getEntity(march.getEndPos());
		if (entity == null) {
			// 目标点丢失, 发送邮件
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.MONSTER_ENTITY_NULL);
			return;
		}

		if (!(entity instanceof Monster)) {
			// 目标点丢失, 发送邮件
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.NOT_MONSTER_TYPE);
			return;
		}

		Monster monster = (Monster) entity;
		long lordId = march.getLordId();
		// 找到玩家
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("kill player is null!");
			return;
		}
		// 获取当前怪物的Id
		int monsterId = (int) monster.getId();
		// 需要找到真实的pve_monsterId
		StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster(monsterId);
		if (staticWorldMonster == null) {
			LogHelper.CONFIG_LOGGER.info("config error!");
			return;
		}

		if (staticWorldMonster.getType() == 1) {
			worldLogic.handleRebel(staticWorldMonster, player, monster, march, mapInfo);
		} else if (staticWorldMonster.getType() == EntityType.RIOT_MONSTER) {
			worldLogic.handleRoit(staticWorldMonster, player, monster, march, mapInfo);
		} else if (staticWorldMonster.getType() == 4) {
			worldLogic.handleActMonster(staticWorldMonster, player, monster, march, mapInfo);
		} else if (staticWorldMonster.getType() == 5) {
			worldLogic.handleKillCountryHero(staticWorldMonster, player, monster, march, mapInfo);
		} else if (staticWorldMonster.getType() == 6) {
			worldLogic.handleKillStaffMonster(staticWorldMonster, player, monster, march, mapInfo);
		}
	}


	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
	}
}
