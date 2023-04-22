package com.game.service;

import com.game.chat.domain.Chat;
import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.CtyGovern;
import com.game.domain.s.*;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.BigMonsterWar;
import com.game.pb.WorldPb;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.BigMonster;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.BigMonsterWarInfo;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zcp
 * @date 2021/3/23 14:04 诵我真名者,永不见bug
 */
@Service
public class BigMonsterService {

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private BigMonsterManager bigMonsterManager;
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private StaticWorldMgr staticWorldMgr;
	@Autowired
	private StaticLimitMgr staticLimitMgr;
	@Autowired
	private WarBookManager warBookManager;
	@Autowired
	private WarManager warManager;
	@Autowired
	private ChatManager chatManager;
	@Autowired
	private CountryManager countryManager;
	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	/**
	 * 巨型虫族活动
	 *
	 * @param rq
	 * @param handler
	 */
	public void getBigMonsterActivityRq(WorldPb.GetBigMonsterActivityRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		WorldPb.GetBigMonsterActivityRs.Builder builder = WorldPb.GetBigMonsterActivityRs.newBuilder();
		int mapId = worldManager.getMapId(player.getPos());
		StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
		StaticGiantZergBuff giantZergBuff = staticWorldMgr.getGiantZergBuffMap().get(staticWorldMap.getAreaType());

		Map<Integer, Integer> levelMap = new HashMap<>();
//        for (Integer tmpMapId : worldManager.getAllMap()) {
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		StaticWorldMap configWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
//        if (staticWorldMap.getAreaType() != configWorldMap.getAreaType()) {
//            continue;
//        }
		Map<Integer, List<BigMonster>> soldiers = mapInfo.getBigMonsterMap().values().stream().collect(Collectors.groupingBy(e -> e.getLevel()));
		soldiers.forEach((level, f) -> {
			levelMap.merge(level, f.size(), (a, b) -> a + b);
		});
		ArrayList<Integer> levelList = new ArrayList<>();
		ArrayList<Integer> list = (ArrayList<Integer>) Lists.newArrayList(levelMap.keySet()).stream().sorted(Comparator.comparingInt(e -> e.intValue())).collect(Collectors.toList());
		list.forEach(e -> {
			if (levelMap.get(e) != null) {
				levelList.add(levelMap.get(e));
			}
		});
//        }
//        List<Integer> levelList = Lists.newArrayList(levelMap.values());
		if (levelList.size() > 0) {
			builder.setSoldierOne(levelList.get(0));
		}
		if (levelList.size() > 1) {
			builder.setSoldierTwo(levelList.get(1));
		}
		if (levelList.size() > 2) {
			builder.setSoldierThree(levelList.get(2));
		}
		builder.setKill(bigMonsterManager.getBigMonsterKill(player.getCountry(), staticWorldMap.getMapId()));
		builder.setMaxKill(giantZergBuff.getNeedNum());
		builder.setRewardNum(player.getSimpleData().getBigMonsterReward());
		builder.setMaxReward(staticLimitMgr.getNum(SimpleId.BIG_MONSTER_REWARD));
		builder.setSpeed(bigMonsterManager.getMonsterBuff(mapId, player));
		handler.sendMsgToPlayer(WorldPb.GetBigMonsterActivityRs.ext, builder.build());
	}

	/**
	 * 巨型虫族信息
	 *
	 * @param rq
	 * @param handler
	 */
	public void getBigMonsterInfoRq(WorldPb.GetBigMonsterInfoRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		// 目标点
		CommonPb.Pos pos = rq.getPos();
		Pos targetPos = new Pos(pos.getX(), pos.getY());
		int mapId = worldManager.getMapId(rq.getPos());
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		BigMonster bigMonster = mapInfo.getBigMonsterMap().get(targetPos);
		if (bigMonster == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.WORLD_MONSTER_NOT_FOUND);
			return;
		}
		StaticGiantZerg staticGiantZerg = staticWorldMgr.getGiantZergMap().get(bigMonster.getId());
		WorldPb.GetBigMonsterInfoRs.Builder builder = WorldPb.GetBigMonsterInfoRs.newBuilder();
		staticGiantZerg.getAward().forEach(e -> {
			builder.addAwards(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
		});
		staticGiantZerg.getFirstAward().forEach(e -> {
			builder.addFirstAwards(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
		});

		builder.setMonsterId(bigMonster.getId());
		builder.setName(staticGiantZerg.getName());
		builder.setPos(rq.getPos());

		IWar war = mapInfo.getWarMap().values().stream().filter(e -> e.getWarType() == WarType.BIGMONSTER_WAR).filter(e -> {
			BigMonsterWarInfo waInfo = (BigMonsterWarInfo) e;
			if (waInfo.getCountry() == player.getCountry() && waInfo.getPos().equals(targetPos)) {
				return true;
			}
			return false;
		}).findFirst().orElse(null);
		if (war != null) {
			int roleNum = war.getAttacker().getMarchList().stream().filter(e -> e.getState() == MarchState.Waiting).collect(Collectors.groupingBy(e -> e.getLordId())).size();
			builder.setMarch(roleNum);
		} else {
			builder.setMarch(0);
		}
		long period = worldManager.getPeriod(player, player.getPos(), targetPos, 0F);
		builder.setMarchTime(period);
		builder.setLeaveTime(bigMonster.getLeaveTime() - System.currentTimeMillis());
		builder.setLessHp(bigMonster.getTeam().getLessSoldier());
		int total = 0;
		for (BattleEntity entity : bigMonster.getTeam().getAllEnities()) {
			StaticMonster ts = staticMonsterMgr.getStaticMonster(entity.getEntityId());
			if (ts != null) {
				total += ts.getSoldierCount();
			}
		}
		builder.setTotalHp(total);
		builder.setRewardNum(player.getSimpleData().getBigMonsterReward());
		builder.setMaxReward(staticLimitMgr.getNum(SimpleId.BIG_MONSTER_REWARD));
		builder.setIsFirstReward(player.getSimpleData().isFirstBigMonsterReward());

		handler.sendMsgToPlayer(WorldPb.GetBigMonsterInfoRs.ext, builder.build());
	}


	/**
	 * 巨型虫族战斗
	 *
	 * @param rq
	 * @param handler
	 */
	public void getBigMonsterWarRq(WorldPb.GetBigMonsterWarRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		WorldPb.GetBigMonsterWarRs.Builder builder = WorldPb.GetBigMonsterWarRs.newBuilder();
		player.getWarInfos().getInfos().stream().filter(x -> x.getWarType() == WarType.BIGMONSTER_WAR).forEach(war -> {
			builder.addWarInfo(wrap(war, player));
		});
		handler.sendMsgToPlayer(WorldPb.GetBigMonsterWarRs.ext, builder.build());
	}

	private CommonPb.BigMonsterWar wrap(WarInfo warInfo, Player self) {
		Player player = playerManager.getPlayer(warInfo.getAttackerId());
		CommonPb.BigMonsterWar.Builder builder = CommonPb.BigMonsterWar.newBuilder();
		worldManager.handleBigMonsterWarSoldier(warInfo);
		builder.setWarInfo(warInfo.wrapCountryPb(warInfo.isJoin(self)));
		builder.setAttackerName(player.getNick());
		builder.setHeadImg(player.getLord().getPortrait());
		builder.setAttackId(warInfo.getAttackerId());
		builder.setDefId(warInfo.getDefencerId());
		builder.setTitle(player.getTitle());
		CtyGovern govern = countryManager.getGovern(player);
		if (govern != null) {
			builder.setPost(govern.getGovernId());
		}
		builder.setIsIn(warInfo.isJoin(self));
		return builder.build();
	}

	private WarInfo getWar(long keyId) {
		for (int mapId : worldManager.getAllMap()) {
			MapInfo mapInfo = worldManager.getMapInfo(mapId);
			if (mapInfo.getWarMap().containsKey(keyId)) {
				return (WarInfo) mapInfo.getWarMap().get(keyId);
			}
		}
		return null;
	}

	public void fightHelpRq(WorldPb.BigMonsterFightHelpRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int mapId = worldManager.getMapId(player.getPos());
		long warId = req.getKeyId();
		WarInfo warInfo = getWar(warId);
		if (warInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.WAR_END_OR_NOT_EXIST);
			return;
		}
		long date = System.currentTimeMillis();
		if (!warInfo.getAttackMarches().stream().anyMatch(e -> e.getLordId() == player.roleId)) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		if (date < (player.getLastHelpTime() + 30 * TimeHelper.SECOND_MS)) {
			handler.sendErrorMsgToPlayer(GameError.REQUEST_LIMIT);
			return;
		}

		StaticWorldMonster staticMonster = staticWorldMgr.getMonster((int) warInfo.getDefencerId());
		Pos pos = warInfo.getDefencerPos();
		String tarPos = String.format("%s,%s", pos.getX(), pos.getY());
		String p[] = {staticMonster.getName(), tarPos};
		Chat chat = chatManager.createManShare(player, ChatId.BIG_MONSTER_HELP, p);
		chatManager.sendCountryShare(player.getCountry(), chat);
		warInfo.setAttackerHelpTime(warInfo.getAttackerHelpTime() + 1);
		player.setLastHelpTime(System.currentTimeMillis());

		WorldPb.BigMonsterFightHelpRs.Builder builder = WorldPb.BigMonsterFightHelpRs.newBuilder();
		handler.sendMsgToPlayer(WorldPb.BigMonsterFightHelpRs.ext, builder.build());
	}

	/**
	 * @Description 获取当前地图对用等级的巨型虫族的位置
	 * @Param [req, handler]
	 * @Return void
	 * @Date 2021/5/31 16:29
	 **/
	public void getBigMonsterByLevel(WorldPb.GetBigMonsterByLevelRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int mapId = worldManager.getMapId(player.getPos());
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.WAR_END_OR_NOT_EXIST);
			return;
		}
		Map<Pos, BigMonster> bigMonsterMap = mapInfo.getBigMonsterMap();
		WorldPb.GetBigMonsterByLevelRs.Builder builder = WorldPb.GetBigMonsterByLevelRs.newBuilder();
		Iterator<Map.Entry<Pos, BigMonster>> iterator = bigMonsterMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Pos, BigMonster> next = iterator.next();
			StaticGiantZerg staticGiantZerg = staticWorldMgr.getGiantZergMap().get(next.getValue().getId());
			if (staticGiantZerg != null && staticGiantZerg.getLevel() == req.getMonsterLvel()) {
				CommonPb.Pos.Builder pos = CommonPb.Pos.newBuilder();
				pos.setX(next.getKey().getX());
				pos.setY(next.getKey().getY());
				builder.addPos(pos);
			}
		}
		handler.sendMsgToPlayer(WorldPb.GetBigMonsterByLevelRs.ext, builder.build());
	}
}
