package com.game.manager;

import com.game.constant.LostTargetReason;
import com.game.constant.MarchState;
import com.game.constant.Quality;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.pb.WorldPb;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WallManager {

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private WorldLogic worldLogic;

	@Autowired
	private MarchManager marchManager;

	@Autowired
	private WarManager warManager;


	public void handleAssist(March march, MapInfo mapInfo) {
		// 检查目标点是否存在，如果不存在则进行回城
		// 被驻防的lordId, 行军和被驻防的player没有关系
		long lordId = march.getAssistId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("player is not exists!");
			return;
		}

		long marchLordId = march.getLordId();
		// 行军的玩家
		Player marchPlayer = playerManager.getPlayer(marchLordId);
		if (marchPlayer == null) {
			LogHelper.CONFIG_LOGGER.error("marchPlayer is null!");
			return;
		}

		// 当前驻防的玩家pos != march.getEndPos坐标，则进行回城
		if (!player.getPos().isEqual(march.getEndPos())) {
			int mapId = worldManager.getMapId(marchPlayer);
			MapInfo marchMapInfo = worldManager.getMapInfo(mapId);
			if (marchMapInfo == null) {
				LogHelper.CONFIG_LOGGER.error("marchMapId is null.");
				return;
			}
			warManager.handleLostTarget(march, marchMapInfo, LostTargetReason.ASSIS_LOST_TARGET);
			return;
		}

		// 驻防等待, 直接清除这支队伍, 特殊处理
		march.setState(MarchState.Done);
		worldManager.synMarch(mapInfo.getMapId(), march);

		Wall wall = player.getWall();
		if (wall == null) {
			LogHelper.CONFIG_LOGGER.error("wall is not exists!");
			return;
		}

		List<Integer> heroIds = march.getHeroIds();
		// 紧接着发synMarch Remove
		playerManager.synRemoveMarch(marchPlayer, worldManager.wrapMarchPb(march).build());
		// 从玩家marchPlayer身上删除这个行军
		playerManager.handlerMarch(marchPlayer);
		// 从世界地图删除这个行军
		worldManager.removeMarch(mapInfo.getMapId(), march);

		HashMap<Integer, WallFriend> wallFriends = wall.getWallFriends();

		// 生成多个武将驻防
		for (Integer heroId : heroIds) {
			Hero hero = marchPlayer.getHero(heroId);
			if (hero == null) {
				continue;
			}

			WallFriend wallFriend = new WallFriend();
			int keyId = marchPlayer.maxKey();
			wallFriend.setKeyId(keyId);
			wallFriend.setLordId(marchLordId);
			wallFriend.setLordLv(marchPlayer.getLevel());
			wallFriend.setLordName(marchPlayer.getNick());
			wallFriend.setHeroId(heroId);
			wallFriend.setSoldier(hero.getCurrentSoliderNum());
			// 生成行军
			March newMarch = createWallMarch(marchPlayer, heroId, march.getEndPos());
			wallFriend.setMarch(newMarch);
			wallFriend.setMarchId(newMarch.getKeyId());
			wallFriend.setEndTime(System.currentTimeMillis() + TimeHelper.HOUR_MS * 8);
			marchPlayer.addMarch(newMarch);
			newMarch.setMarchType(MarchType.CityFriendAssist);
			newMarch.setAssistId(lordId);

			// 添加行军到玩家身上, 加到世界地图中
			worldManager.addMarch(mapInfo.getMapId(), newMarch);

			WorldPb.SynMarchRq.Builder builder = WorldPb.SynMarchRq.newBuilder();
			builder.setMarch(worldManager.wrapMarchPb(newMarch));

			playerManager.synMarchToPlayer(marchPlayer, builder.build());
			wallFriends.put(keyId, wallFriend);
		}
		//TODO jyb 推送 同步对方的城墙信息
		playerManager.synWallInfo(player);

	}

	public March createWallMarch(Player player, int heroId, Pos targetPos) {
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		List<Integer> marchHero = march.getHeroIds();
		marchHero.add(heroId);
		march.setState(MarchState.CityAssist);
		march.setEndPos(targetPos);
		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());
		march.setStartPos(playerPos);
		int configNum = staticLimitMgr.getNum(58);
		long period = configNum * TimeHelper.HOUR_MS;
		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
		march.setCountry(player.getCountry());
		return march;
	}


	public int randQuality(int wallLv, Map<Integer, WallDefender> wallDefenders) {
		// 每10级最多一个
		int maxGoldHero = wallLv / 10 + (wallLv % 10 == 0 ? 0 : 1);
		int quality;
		// 计算当前金色将领个数
		int currentNum = 0;
		for (WallDefender defender : wallDefenders.values()) {
			if (defender.getQuality() >= Quality.GOLD.get()) {
				currentNum += 1;
			}
		}

		// 总权重
		int blueRate = staticLimitMgr.getNum(30);
		int greenRate = staticLimitMgr.getNum(31);
		int goldRate = staticLimitMgr.getNum(32);

		int totalRate = blueRate + greenRate + goldRate;
		if (wallLv < 10) {
			totalRate -= goldRate;
		} else if (wallLv > 20) {
			totalRate -= blueRate;
		}

		if (currentNum >= maxGoldHero) {
			totalRate -= goldRate;
			quality = lootQuality(wallLv, totalRate, blueRate, greenRate);
		} else {
			quality = lootQuality(wallLv, totalRate, blueRate, greenRate);
		}

		return quality;

	}

	public int lootQuality(int wallLv, int totalRate, int blueRate, int greenRate) {
		int quality = Quality.BLUE.get();
		int randNum = RandomHelper.threadSafeRand(1, totalRate);
		if (wallLv < 10) { // blue, green
			if (randNum < blueRate) {
				quality = Quality.BLUE.get();
			} else {
				quality = Quality.GREEN.get();
			}

		} else if (wallLv > 20) { // green,gold
			if (randNum < greenRate) {
				quality = Quality.GREEN.get();
			} else {
				quality = Quality.GOLD.get();
			}
		} else {
			if (randNum < blueRate) {
				quality = Quality.BLUE.get();
			} else if (randNum < blueRate + greenRate) {
				quality = Quality.GREEN.get();
			} else if (randNum < totalRate) {
				quality = Quality.GOLD.get();
			}
		}

		return quality;
	}

}
