package com.game.service;


import com.game.constant.GameError;
import com.game.constant.Reason;
import com.game.dataMgr.StaticHeroMgr;
import com.game.dataMgr.StaticLordDataMgr;
import com.game.domain.Player;
import com.game.domain.p.Soldier;
import com.game.domain.s.StaticHero;
import com.game.manager.LordManager;
import com.game.manager.PlayerManager;
import com.game.manager.SoldierManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.MapInfoPb.GetMapNpcRq;
import com.game.pb.MapInfoPb.GetMapNpcRs;
import com.game.pb.MapInfoPb.RobotRepairRq;
import com.game.pb.MapInfoPb.RobotRepairRs;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Monster;
import com.game.worldmap.Pos;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 地图信息
 */

@Service
public class MapInfoService {

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private LordManager lordManager;
	@Autowired
	private StaticLordDataMgr staticLordDataMgr;
	@Autowired
	private StaticHeroMgr staticHeroMgr;
	@Autowired
	private SoldierManager soldierManager;


	/**
	 * 机器人修正
	 *
	 * @param req
	 * @param handler
	 */
	public void robotRepairRq(RobotRepairRq req, ClientHandler handler) {
		if (!req.getCipherCode().equals("zzhy666")) {
			return;
		}
		long playerId = handler.getRoleId();
		Player player = playerManager.getPlayer(playerId);

		// 将机器人等级提升到X级
		if (req.hasLevel()) {
			addExp(player, req.getLevel());
		}

		// 奖励是否为空
		if (!req.getAwardsList().isEmpty()) {
			req.getAwardsList().forEach(e -> {
				playerManager.addAward(player, e.getType(), e.getId(), e.getCount(), Reason.ROBOT_ADD);
			});
		}

		// 补充兵量
		if (req.hasAddSoldierHeroId()) {
			addPlayerSoldier(player, req.getAddSoldierHeroId());
		}

		RobotRepairRs.Builder builder = RobotRepairRs.newBuilder();
		handler.sendMsgToPlayer(RobotRepairRs.ext, builder.build());
	}

	private void addExp(Player player, int level) {
		if (level <= player.getLevel() || level >= staticLordDataMgr.maxLevel()) {
			return;
		}

		int exp = 0;
		for (int i = player.getLevel(); i < level; i++) {
			int nextLv = i + 1;
			int needExp = staticLordDataMgr.getExp(nextLv);
			exp += needExp;
		}

		if (exp <= 0) {
			return;
		}
		lordManager.addExp(player, exp, Reason.ROBOT_ADD);
	}

	private void addPlayerSoldier(Player player, int heroId) {
		// 给英雄补兵
		StaticHero staticHero = staticHeroMgr.getStaticHero(heroId);
		if (staticHero == null) {
			return;
		}
		int soldierType = staticHero.getSoldierType();

		int currentSoldier = soldierManager.getSoldierNum(player, soldierType);
		int total = soldierManager.getTotalCapacity(player, soldierType);

		int addSoldier = Math.abs(total - currentSoldier);

		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		Soldier soldier = soldierMap.get(soldierType);
		soldier.setNum(soldier.getNum() + addSoldier);

	}

	/**
	 * 获取地图上的怪物信息
	 *
	 * @param req
	 * @param handler
	 */
	public void getMapNpcRq(GetMapNpcRq req, ClientHandler handler) {
		MapInfo mapInfo = worldManager.getMapInfo(req.getMapId());
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.MAP_ID_ERROR);
			return;
		}

		GetMapNpcRs.Builder builder = GetMapNpcRs.newBuilder();
		builder.setMapId(mapInfo.getMapId());

		Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
		monsterMap.values().forEach(e -> {
			long value = e.getLevel() * 1000000 + e.getPos().getX() * 1000 + e.getPos().getY();
			builder.addNpcList(value);
		});

		handler.sendMsgToPlayer(GetMapNpcRs.ext, builder.build());
	}
}
