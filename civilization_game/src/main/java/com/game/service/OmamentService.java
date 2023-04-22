package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.game.constant.*;
import com.game.log.consumer.EventManager;
import com.game.manager.*;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.dataMgr.StaticOpenManger;
import com.game.domain.Player;
import com.game.domain.p.Omament;
import com.game.domain.p.PlayerOmament;
import com.game.domain.p.Property;
import com.game.domain.s.StaticOmType;
import com.game.domain.s.StaticOmament;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.OmamentPb;
import com.game.pb.OmamentPb.CompoundOmamentRq;
import com.game.pb.OmamentPb.CompoundOmamentRs;
import com.game.pb.OmamentPb.GetOmamentBagRq;
import com.game.pb.OmamentPb.GetOmamentBagRs;
import com.game.pb.OmamentPb.GetOmamentDeressRq;
import com.game.pb.OmamentPb.GetOmamentDeressRs;
import com.game.pb.OmamentPb.TakeOffOmamentRq;
import com.game.pb.OmamentPb.TakeOffOmamentRs;
import com.game.pb.OmamentPb.WearOmamentRq;
import com.game.pb.OmamentPb.WearOmamentRs;
import com.game.server.GameServer;
import com.game.util.PbHelper;

/**
 * 2020年8月5日
 *
 * @CaoBing halo_game OmamentService.java
 **/
@Service
public class OmamentService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private OmamentManager omamentManager;

	@Autowired
	private HeroManager heroDataManager;

	@Autowired
	private StaticOpenManger staticOpenManger;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private EventManager eventManager;

	/**
	 * 获取配饰背包物品列表
	 *
	 * @param req
	 * @param handler
	 */
	public void getOmamentBagRq(GetOmamentBagRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		OmamentPb.GetOmamentBagRs.Builder builder = OmamentPb.GetOmamentBagRs.newBuilder();
		Map<Integer, Omament> omaments = player.getOmaments();
		if (omaments.size() > 0) {
			Set<Entry<Integer, Omament>> entrySet = omaments.entrySet();
			for (Entry<Integer, Omament> entry : entrySet) {
				CommonPb.Omament.Builder omament = CommonPb.Omament.newBuilder();
				omament.setOmamentId(entry.getKey());
				if (null != entry.getValue()) {
					omament.setCount(entry.getValue().getCount());
				}
				builder.addOmamentItem(omament);
			}
		}
		handler.sendMsgToPlayer(GetOmamentBagRs.ext, builder.build());
	}

	/**
	 * 获取玩家配饰穿戴列表
	 *
	 * @param req
	 * @param handler
	 */
	public void getOmamentDeressRq(GetOmamentDeressRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		OmamentPb.GetOmamentDeressRs.Builder builder = OmamentPb.GetOmamentDeressRs.newBuilder();
		Map<Integer, PlayerOmament> playerOmaments = player.getPlayerOmaments();
		if (playerOmaments.size() > 0) {
			Set<Entry<Integer, PlayerOmament>> entrySet = playerOmaments.entrySet();
			for (Entry<Integer, PlayerOmament> entry : entrySet) {
				CommonPb.PlayerOmament.Builder playerOmament = CommonPb.PlayerOmament.newBuilder();
				playerOmament.setPos(entry.getKey());
				if (null != entry.getValue()) {
					playerOmament.setOmamentId(entry.getValue().getOmamentId());
				}
				builder.addPlayerOmament(playerOmament);
			}
		}
		handler.sendMsgToPlayer(GetOmamentDeressRs.ext, builder.build());
	}

	/**
	 * 穿戴配饰请求
	 *
	 * @param req
	 * @param handler
	 */
	public void wearOmamentRq(WearOmamentRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (!staticOpenManger.isOpen(OpenConsts.OPEN_11, player)) {// 开放条件40级
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}
		int pos = req.getPos();// 槽位
		int omamentId = req.getOmamentId();// 装备ID

		int playerLevel = player.getLevel();
		StaticOmType staticOmType = omamentManager.findOmTypeConfig(pos);
		if (null == staticOmType) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		if (playerLevel < staticOmType.getLevel()) {// 判断槽位是否开启
			handler.sendErrorMsgToPlayer(GameError.POS_NOT_OPEN);
			return;
		}

		StaticOmament staticOmament = omamentManager.findOmamentConfig(omamentId);
		if (null == staticOmament) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		if (staticOmType.getType() != staticOmament.getType()) {
			handler.sendErrorMsgToPlayer(GameError.POS_WEAR_TYPE_ERROR);
			return;
		}

		boolean hasEnoughOmament = omamentManager.hasEnoughOmament(player, omamentId, 1);
		if (!hasEnoughOmament) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
			return;
		}

		Property findOmamentProperty = omamentManager.findOmamentProperty(omamentId);
		if (null == findOmamentProperty) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		OmamentPb.WearOmamentRs.Builder builder = WearOmamentRs.newBuilder();
		PlayerOmament findPlayerOmament = omamentManager.findPlayerOmament(player, pos);
		if (null != findPlayerOmament) {
			int oldomamentId = findPlayerOmament.getOmamentId();
			if (omamentId == oldomamentId) {
				handler.sendErrorMsgToPlayer(GameError.POS_WEAR_SAME_ERROR);
				return;
			}

			findPlayerOmament.setOmamentId(omamentId);
			if (omamentManager.findOmamentBag(player, omamentId) != null) {
				builder.setRemoveOmament(omamentManager.findOmamentBag(player, omamentId).wrapPb());
			}
			playerManager.subAward(player, AwardType.OMAMENT, omamentId, 1, Reason.SUB_OMAMENT);
			playerManager.addAward(player, AwardType.OMAMENT, oldomamentId, 1, Reason.COMPOSE_OMAMENTE);

			builder.setPlayerOmament(findPlayerOmament.wrapPb());
			if (omamentManager.findOmamentBag(player, oldomamentId) != null) {
				builder.setAddOmament(omamentManager.findOmamentBag(player, oldomamentId).wrapPb());
			}
		} else {
			findPlayerOmament = new PlayerOmament(pos, omamentId);
			player.getPlayerOmaments().put(pos, findPlayerOmament);
			builder.setRemoveOmament(omamentManager.findOmamentBag(player, omamentId).wrapPb());
			playerManager.subAward(player, AwardType.OMAMENT, omamentId, 1, Reason.SUB_OMAMENT);
		}

		Omament findOmamentBag = omamentManager.findOmamentBag(player, omamentId);
		if (null != findOmamentBag && findOmamentBag.getCount() == 0) {
			omamentManager.removeOmament(player, omamentId);
		}
		CommonPb.PlayerOmament.Builder playerOmament = CommonPb.PlayerOmament.newBuilder();
		playerOmament.setOmamentId(findPlayerOmament.getOmamentId());
		playerOmament.setPos(findPlayerOmament.getPos());

		builder.setOmamentProperty(findOmamentProperty.wrapPb());
		builder.setPlayerOmament(playerOmament);
		handler.sendMsgToPlayer(WearOmamentRs.ext, builder.build());

		taskManager.doTask(TaskType.OMAMENT_WEARER, player);// 穿戴任务
		heroDataManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
		eventManager.wearOmament(player, Lists.newArrayList(staticOmament.getName(), staticOmament.getLevel()));
	}

	/**
	 * 脱配饰
	 *
	 * @param req
	 * @param handler
	 */
	public void takeOffOmamentRq(TakeOffOmamentRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int pos = req.getPos();// 槽位
		int omamentId = req.getOmamentId();// 装备ID

		PlayerOmament findPlayerOmament = omamentManager.findPlayerOmament(player, pos);
		if (null == findPlayerOmament) {
			handler.sendErrorMsgToPlayer(GameError.POS_NOT_OPEN);
			return;
		}

		if (findPlayerOmament.getOmamentId() != omamentId) {
			handler.sendErrorMsgToPlayer(GameError.POS_NOT_HAVE_OMAMENT);
			return;
		}

		Property findOmamentProperty = omamentManager.findOmamentProperty(omamentId);
		if (null == findOmamentProperty) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		findPlayerOmament.setOmamentId(0);
		playerManager.addAward(player, AwardType.OMAMENT, omamentId, 1, Reason.COMPOSE_OMAMENTE);

		Omament findOmamentBag = omamentManager.findOmamentBag(player, omamentId);
		OmamentPb.TakeOffOmamentRs.Builder builder = TakeOffOmamentRs.newBuilder();
		builder.setAddOmament(findOmamentBag.wrapPb());
		builder.setPlayerOmament(findPlayerOmament.wrapPb());
		builder.setOmamentProperty(findOmamentProperty.wrapPb());

		handler.sendMsgToPlayer(TakeOffOmamentRs.ext, builder.build());
	}

	/**
	 * 合成配饰
	 *
	 * @param req
	 * @param handler
	 */
	public void compoundOmamentRq(CompoundOmamentRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (!staticOpenManger.isOpen(OpenConsts.OPEN_11, player)) {// 开放条件40级
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}
		int omamentId = req.getOmamentId();// 装备ID
		int omamentType = req.getOmamentType();// 根据佩饰类型判断是1单合成，2组合成，3还是一键合成
		StaticOmament findOmamentConfig = omamentManager.findOmamentConfig(omamentId);
		if (findOmamentConfig == null) {
			handler.sendErrorMsgToPlayer(GameError.OMAMENT_NOT_FOUND);
			return;
		}
		if (findOmamentConfig.getComposeId() == 0) {
			handler.sendErrorMsgToPlayer(GameError.SUMMIT_OMAMENT);
			return;
		}
		int compositionNum = 0;// 用于一键合成判断合成次数
		OmamentPb.CompoundOmamentRs.Builder builder = CompoundOmamentRs.newBuilder();
		if (omamentType == 1) {
			int needNum = findOmamentConfig.getNeedNum();
			Omament findOmamentBag = omamentManager.findOmamentBag(player, omamentId);
			if (findOmamentBag == null) {
				handler.sendErrorMsgToPlayer(GameError.OMAMENT_NOT_FOUND);
				return;
			}
			if (findOmamentBag.getCount() < needNum) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
				return;
			}

			// 计入配饰组合合成前的数量
			// OmamentService.getLogHelper().omamentItemLog(new OmamentItemLog(player.roleId,omamentId,findOmamentBag.getCount(),needNum,Reason.SUB_OMAMENT));
			playerManager.subAward(player, AwardType.OMAMENT, omamentId, needNum, Reason.SUB_OMAMENT);
			int upgradeOmamentNum = 0;
			if (omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()) != null) {
				upgradeOmamentNum = omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()).getCount();// 计入升级的前的配饰数量
			}
			// OmamentService.getLogHelper().omamentItemLog(new OmamentItemLog(player.roleId,findOmamentConfig.getComposeId(),upgradeOmamentNum,1,Reason.ADD_OMAMENTE));
			playerManager.addAward(player, AwardType.OMAMENT, findOmamentConfig.getComposeId(), 1, Reason.COMPOSE_OMAMENTE);
			builder.setAddOmament(omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()).wrapPb());
			builder.setRemoveOmament(omamentManager.findOmamentBag(player, omamentId).wrapPb());

		} else if (omamentType == 2) {// 2代表组合合成

			compositionNum = groupCompoundOmamentRq(player, req, handler, omamentId, compositionNum);
			if (compositionNum != 0) {

				builder.setAddOmament(omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()).wrapPb());
				builder.setRemoveOmament(omamentManager.findOmamentBag(player, omamentId).wrapPb());
			}

		} else if (omamentType == 3) {// 3代表一键合成
			omamentId = omamentManager.findBagLowestOmament(player, omamentId, handler);
			if (omamentId == 0) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
				return;
			}

			if (omamentId > 1) {
				oneKeyCompoundOmamentRq(player, req, handler, omamentId);
				Map<Integer, Omament> omaments = player.getOmaments();
				if (omaments.size() > 0) {
					Set<Entry<Integer, Omament>> entrySet = omaments.entrySet();
					for (Entry<Integer, Omament> entry : entrySet) {
						CommonPb.Omament.Builder omament = CommonPb.Omament.newBuilder();
						omament.setOmamentId(entry.getKey());
						if (null != entry.getValue()) {
							omament.setCount(entry.getValue().getCount());

						}
						builder.addOmamentBag(omament);
					}
				}
			}
		}

		Omament findOmamentBagAfter = omamentManager.findOmamentBag(player, omamentId);
		if (null != findOmamentBagAfter && findOmamentBagAfter.getCount() == 0) {
			omamentManager.removeOmament(player, omamentId);
		}
		handler.sendMsgToPlayer(CompoundOmamentRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(EventEnum.COMPOUND_OMAMENT, player, 1, 0);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.OMAMENT_COMPOUND, 1);
		dailyTaskManager.record(DailyTaskId.COMPOSE, player, 1);
		eventManager.omamentCompound(player, Lists.newArrayList(omamentType, findOmamentConfig.getComposeId(), findOmamentConfig.getName()));

	}

	/**
	 * 组合成配饰
	 *
	 * @param req
	 * @param handler
	 */
	public int groupCompoundOmamentRq(Player player, CompoundOmamentRq req, ClientHandler handler, int omamentId, int compositionNum) {

		StaticOmament findOmamentConfig = omamentManager.findOmamentConfig(omamentId);// 判断配置表中是否存在

		if (findOmamentConfig == null) {
			handler.sendErrorMsgToPlayer(GameError.OMAMENT_NOT_FOUND);
			return compositionNum;
		}
		int needNum = findOmamentConfig.getNeedNum();
		Omament findOmamentBag = omamentManager.findOmamentBag(player, omamentId);
		if (findOmamentBag == null) {// 背包中没有配饰
			handler.sendErrorMsgToPlayer(GameError.OMAMENT_NOT_FOUND);
			return compositionNum;
		}
		int omamentNum = findOmamentBag.getCount();
		if (omamentNum < needNum) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
			return compositionNum;
		}
		while (omamentNum >= needNum) {
			compositionNum++;

			// 计入配饰组合合成前的数量
			// OmamentService.getLogHelper().omamentItemLog(new OmamentItemLog(player.roleId,omamentId,findOmamentBag.getCount(),needNum,Reason.SUB_OMAMENT));
			playerManager.subAward(player, AwardType.OMAMENT, omamentId, needNum, Reason.SUB_OMAMENT);
			int upgradeOmamentNum = 0;
			if (omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()) != null) {
				upgradeOmamentNum = omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()).getCount();// 计入升级的前的配饰数量
			}
			// OmamentService.getLogHelper().omamentItemLog(new OmamentItemLog(player.roleId,findOmamentConfig.getComposeId(),upgradeOmamentNum,1,Reason.ADD_OMAMENTE));
			playerManager.addAward(player, AwardType.OMAMENT, findOmamentConfig.getComposeId(), 1, Reason.COMPOSE_OMAMENTE);
			omamentNum = omamentNum - needNum;
		}
		return compositionNum;
	}

	/**
	 * 一键合成配饰
	 *
	 * @param req
	 * @param handler
	 */
	public void oneKeyCompoundOmamentRq(Player player, CompoundOmamentRq req, ClientHandler handler, int omamentId) {

		StaticOmament findOmamentConfig = omamentManager.findOmamentConfig(omamentId);// 获取配置表中的配饰信息
		if (findOmamentConfig == null) {
			handler.sendErrorMsgToPlayer(GameError.OMAMENT_NOT_FOUND);
			return;
		}
		int needNum = findOmamentConfig.getNeedNum();// 需要合成配饰的数量

		Omament findOmamentBag = omamentManager.findOmamentBag(player, omamentId);// 查找背包中是否由此配饰

		Map<Integer, Omament> omaments = player.getOmaments();// 获取背包中所有配饰

		if (omaments.size() < 1) {// 背包中如果没有配饰就直接提示配饰数量不足
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
			return;
		}
		int omamentNum = 0;
		if (findOmamentBag == null) {
//			if (findOmamentConfig.getComposeId() == 0 ) {//判断是否是最高级别并看它的合成次数是否为0
//				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
//				return ;
//			}
			if (findOmamentConfig.getComposeId() != 0) {
				oneKeyCompoundOmamentRq(player, req, handler, findOmamentConfig.getComposeId());
			}

		} else {
//			if(omamentId==0) {
//				return ;
//			}
			if (findOmamentBag.getCount() < needNum && omamentId != 0) {// 当前要合成的配饰数量要是小于最小配饰数量就进入下次循环
				oneKeyCompoundOmamentRq(player, req, handler, findOmamentConfig.getComposeId());
			}
			omamentNum = findOmamentBag.getCount();

		}

		if (omamentNum < needNum) {
			// handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
			return;
		}
		while (omamentNum >= needNum && omamentNum != 0 && needNum != 0) {// 把能合成的配饰都合成下一个等级的配饰

			// 计入配饰组合合成前的数量
			// OmamentService.getLogHelper().omamentItemLog(new OmamentItemLog(player.roleId,omamentId,findOmamentBag.getCount(),needNum,Reason.SUB_OMAMENT));
			playerManager.subAward(player, AwardType.OMAMENT, omamentId, needNum, Reason.SUB_OMAMENT);
			int upgradeOmamentNum = 0;
			if (omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()) != null) {
				upgradeOmamentNum = omamentManager.findOmamentBag(player, findOmamentConfig.getComposeId()).getCount();// 计入升级的前的配饰数量
			}
			// OmamentService.getLogHelper().omamentItemLog(new OmamentItemLog(player.roleId,findOmamentConfig.getComposeId(),upgradeOmamentNum,1,Reason.ADD_OMAMENTE));
			playerManager.addAward(player, AwardType.OMAMENT, findOmamentConfig.getComposeId(), 1, Reason.COMPOSE_OMAMENTE);
			omamentNum = omamentNum - needNum;
		}
		if (findOmamentConfig.getComposeId() != 0) {

			oneKeyCompoundOmamentRq(player, req, handler, findOmamentConfig.getComposeId());
		}

	}

	/**
	 * 在饰品栏上直接合成配饰
	 *
	 * @param req
	 * @param handler
	 */
	public void baublesCompoundOmamentRq(OmamentPb.BaublesCompoundOmamentRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int pos = req.getPos();// 槽位
		int omamentId = req.getOmamentId();// 装备ID

		StaticOmament findOmamentConfig = omamentManager.findOmamentConfig(omamentId);
		if (findOmamentConfig == null) {
			handler.sendErrorMsgToPlayer(GameError.OMAMENT_NOT_FOUND);
			return;
		}
		int needNum = findOmamentConfig.getNeedNum() - 1;
		PlayerOmament findPlayerOmament = omamentManager.findPlayerOmament(player, pos);
		Omament findOmamentBag = omamentManager.findOmamentBag(player, omamentId);
		if (findOmamentBag.getCount() < needNum) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OMAMENT);
			return;
		}
		// 移除背包原有配饰
		playerManager.subAward(player, AwardType.OMAMENT, omamentId, needNum, Reason.SUB_OMAMENT);

		// 设置穿戴的为下一级的id
		findPlayerOmament.setOmamentId(findOmamentConfig.getComposeId());

		OmamentPb.BaublesCompoundOmamentRs.Builder builder = OmamentPb.BaublesCompoundOmamentRs.newBuilder();
		if (findOmamentConfig.getComposeId() == 0) {
			return;
		}
		CommonPb.PlayerOmament.Builder playerOmament = CommonPb.PlayerOmament.newBuilder();
		playerOmament.setOmamentId(findPlayerOmament.getOmamentId());
		playerOmament.setPos(findPlayerOmament.getPos());
		builder.setPlayerOmament(playerOmament);
		builder.setRemoveOmament(CommonPb.Omament.newBuilder().setOmamentId(findOmamentBag.getOmamentId()).setCount(findOmamentBag.getCount()).build());

//        Omament findOmamentBagAfter = omamentManager.findOmamentBag(player, omamentId);
//        if (null != findOmamentBagAfter && findOmamentBagAfter.getCount() == 0) {
//            omamentManager.removeOmament(player, omamentId);
//        }
		handler.sendMsgToPlayer(OmamentPb.BaublesCompoundOmamentRs.ext, builder.build());
		dailyTaskManager.record(DailyTaskId.COMPOSE, player, 1);
		eventManager.upOmament(player, Lists.newArrayList(findOmamentConfig.getName(), findOmamentConfig.getLevel()));
		heroDataManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
	}
}
