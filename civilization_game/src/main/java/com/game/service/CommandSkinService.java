package com.game.service;

import com.game.constant.GameError;
import com.game.constant.*;
import com.game.dataMgr.StaticBaseSkinMgr;
import com.game.domain.Player;
import com.game.domain.p.CommandSkin;
import com.game.domain.s.StaticBaseSkin;
import com.game.manager.CommandSkinManager;
import com.game.manager.HeroManager;
import com.game.manager.ItemManager;
import com.game.manager.PlayerManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.SkinPb;
import com.game.util.RandomHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.PlayerCity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author CaoBing
 * @date 2021/2/2 14:39
 */
@Service
public class CommandSkinService {
	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private CommandSkinManager commandSkinManager;

	@Autowired
	private StaticBaseSkinMgr staticBaseSkinMgr;

	@Autowired
	private ItemManager itemManager;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private WorldManager worldManager;

	/**
	 * 获取皮肤列表
	 *
	 * @param req
	 * @param handler
	 */
	public void GetCommandSkinRq(SkinPb.GetCommandSkinRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		SkinPb.GetCommandSkinRs.Builder commandSkinList = commandSkinManager.getCommandSkinList(player);
		handler.sendMsgToPlayer(SkinPb.GetCommandSkinRs.ext, commandSkinList.build());
	}

	/**
	 * 更换主城皮肤
	 *
	 * @param req
	 * @param handler
	 */
	public void changeCommandSkinRq(SkinPb.ChangeCommandSkinRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int keyId = req.getKeyId();
		Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();
		CommandSkin commandSkin = commandSkins.get(keyId);

		if (null == commandSkin) {
			handler.sendErrorMsgToPlayer(GameError.SKIN_NOT_EXISTS);
			return;
		}

		SkinPb.ChangeCommandSkinRs.Builder builder = SkinPb.ChangeCommandSkinRs.newBuilder();
		player.getLord().setSkin(keyId);
		builder.setKeyId(keyId);

		handler.sendMsgToPlayer(SkinPb.ChangeCommandSkinRs.ext, builder.build());
		heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
		// 删除实体
		int newMapId = worldManager.getMapId(player);
		MapInfo newMapInfo = worldManager.getMapInfo(newMapId);
		PlayerCity playerCity = newMapInfo.getPlayerCity(player.getPos());
		if (playerCity != null) {
			playerManager.getOnlinePlayer().forEach(e -> {
				worldManager.synEntityRq(playerCity, newMapInfo.getMapId(), player.getOldPos()); // 同步城池
			});
		}
	}

	/**
	 * 升级主城皮肤
	 *
	 * @param req
	 * @param handler
	 */
	public void upCommandSkinLevRq(SkinPb.UpCommandSkinLevRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int keyId = req.getKeyId();
		int itemNum = req.getItemNum();
		if (keyId == 0 || itemNum == 0) {
			handler.sendErrorMsgToPlayer(GameError.PROP_NOT_ENOUGH);
			return;
		}

		Map<Integer, CommandSkin> commandSkins = player.getCommandSkins();
		if (commandSkins.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.SKIN_NOT_EXISTS);
			return;
		}
		CommandSkin commandSkin = commandSkins.get(keyId);
		if (commandSkin == null) {
			handler.sendErrorMsgToPlayer(GameError.SKIN_NOT_EXISTS);
			return;
		}

		StaticBaseSkin staticBaseSkin = staticBaseSkinMgr.getStaticBaseSkin(keyId);
		int maxStar = staticBaseSkin.getMaxStar();
		int lev = commandSkin.getStar();
		if (maxStar < lev + 1) {
			handler.sendErrorMsgToPlayer(GameError.SKIN_LEV_EXISTS);
			return;
		}
		List<List<Integer>> needNum = staticBaseSkin.getNeedNum();
		if (needNum.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		List<Integer> needItem = needNum.get(lev);
		if (null == needItem || needItem.size() != 3) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		List<Integer> rand = staticBaseSkin.getRand();
		if (rand.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		Integer randNum = rand.get(lev);
		if (null == randNum) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		if (!itemManager.hasEnoughItem(player, needItem.get(1), itemNum)) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		playerManager.subAward(player, needItem.get(0), needItem.get(1), itemNum, Reason.COMMAND_SKIN);

		SkinPb.UpCommandSkinLevRs.Builder builder = SkinPb.UpCommandSkinLevRs.newBuilder();
		randNum = randNum * itemNum;
		if (RandomHelper.isHitRangeIn100(randNum)) {
			commandSkin.setStar(lev + 1);
			builder.setState(1);
		} else {
			builder.setState(0);
		}
		builder.setSkin(commandSkin.wrapPb().build());
		handler.sendMsgToPlayer(SkinPb.UpCommandSkinLevRs.ext, builder.build());
		heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
	}
}
