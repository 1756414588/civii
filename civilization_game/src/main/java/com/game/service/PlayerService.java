package com.game.service;

import com.alibaba.fastjson.JSONArray;
import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticIniDataMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticLordDataMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Player;
import com.game.domain.Role;
import com.game.domain.p.Account;
import com.game.domain.Award;
import com.game.domain.p.Hero;
import com.game.domain.p.LevelAward;
import com.game.domain.p.Lord;
import com.game.domain.p.SimpleData;
import com.game.domain.p.Soldier;
import com.game.domain.p.Wall;
import com.game.domain.s.StaticEnergyPrice;
import com.game.domain.s.StaticLordLv;
import com.game.domain.s.StaticNewState;
import com.game.domain.s.StaticPortrait;
import com.game.domain.s.StaticVip;
import com.game.log.LogUser;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.RoleExpLog;
import com.game.log.domain.RoleGuideLog;
import com.game.log.domain.RoleTaskLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.DealType;
import com.game.message.handler.ServerHandler;
import com.game.message.handler.cs.RecordHandler;
import com.game.message.handler.cs.UpdateGuideHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.ChatPb;
import com.game.pb.CommonPb;
import com.game.pb.InnerPb.UseGiftCodeRs;
import com.game.pb.RolePb;
import com.game.pb.RolePb.CreateRoleRq;
import com.game.pb.RolePb.CreateRoleRs;
import com.game.pb.RolePb.GetLevelAwardRs;
import com.game.pb.RolePb.GiftCodeRs;
import com.game.pb.RolePb.RefreshDataRq;
import com.game.pb.RolePb.RefreshDataRs;
import com.game.pb.RolePb.RoleReloginRq;
import com.game.pb.RolePb.RoleReloginRs;
import com.game.server.GameServer;
import com.game.server.ICommand;
import com.game.server.exec.LoginExecutor;
import com.game.util.HttpUtils;
import com.game.util.LogExceptionUtil;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.StringUtil;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private LordManager lordManager;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private TechManager techManager;

	@Autowired
	private SoldierManager soldierManager;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private RankManager rankManager;

	@Autowired
	private StaticIniDataMgr staticIniDataMgr;

	@Autowired
	private BuildingManager buildingManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private ServerManager serverManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private DailyTaskManager dailyTaskManager;

	@Autowired
	private EventManager eventManager;
	@Autowired
	private StaticLordDataMgr staticLordDataMgr;
	@Autowired
	private TDManager tdManager;
	@Autowired
	private NickManager nickManager;

	@Autowired
	XinkuaiManager xinkuaiManager;
	@Autowired
	ActivityEventManager activityEventManager;
	@Value("${accountServerUrl}")
	String accountServerUrl;


	// 定时保存玩家数据
	//public void saveTimerLogic() {
	//	long s = System.currentTimeMillis();
	//	List<Long> list = Lists.newArrayList(playerManager.getLoginCache().keySet());
	//	Iterator<Long> it = list.iterator();
	//	while (it.hasNext()) {
	//		long roleId = it.next();
	//		Player player = playerManager.getPlayer(roleId);
	//		if (player != null) {
	//			playerManager.updateRole(new Role(player));
	//			//离线玩家移除
	//			if (!player.isLogin) {
	//				it.remove();
	//				//从队列中移除
	//				playerManager.getLoginCache().remove(roleId);
	//			}
	//		} else {
	//			LogHelper.CONFIG_LOGGER.info("save player error player is null ->[{}]", roleId);
	//		}
	//	}
	//}

	//public void cleanExpirePlayer() {
	//	long now = System.currentTimeMillis();
	//	long cleanAccountBegin = TimeHelper.getTimeOfDay(PlayerManager.CLEAN_ACCOUNT_BEGIN);
	//	long cleanAccountEnd = TimeHelper.getTimeOfDay(PlayerManager.CLEAN_ACCOUNT_END);
	//	//清理账号配置规则
	//	List<List<Integer>> cleanAccount = staticLimitMgr.getCleanAccount();
	//	//到了清理时间阶段
	//	if (null != cleanAccount && now <= cleanAccountEnd && now >= cleanAccountBegin) {
	//		List<Player> cleanList = playerManager.getPlayers().values().parallelStream().filter(player -> {
	//			return isClean(cleanAccount, player);
	//		}).collect(Collectors.toList());
	//		//有用户满足要求 移除
	//		if (cleanList.size() > 0) {
	//			cleanList.forEach(player -> {
	//				playerManager.cleanPlayer(player);
	//			});
	//		}
	//	}
	//}

	///**
	// * 根据规则判定改账号是否清理
	// *
	// * @param cleanAccount
	// * @param player
	// * @return
	// */
	//private boolean isClean(List<List<Integer>> cleanAccount, Player player) {
	//	Lord lord = player.getLord();
	//	Account account = player.account;
	//	if (lord == null || account == null) {
	//		return false;
	//	}
	//	for (List<Integer> clean : cleanAccount) {
	//		if (clean.size() >= 3) {
	//			int vip = lord.getVip();// VIP等级
	//			int commandLv = player.getBuildingLv(BuildingType.COMMAND);// 主城等级
	//			Date loginDate = account.getLoginDate();// 登录日期
	//			if (loginDate == null) {
	//				break;
	//			}
	//			//上次登录时间距离当前时间的小时
	//			int dacey = TimeHelper.getDifferHours(loginDate.getTime(), System.currentTimeMillis());
	//			//清理账号条件设置(addtion配置解释:主城等级(等于),离线时长(大于等于),vip等级(小于)]
	//			if (vip < clean.get(2) && commandLv == clean.get(0) && dacey >= clean.get(1)) {
	//				return true;
	//			}
	//		}
	//	}
	//	return false;
	//}


	// 玩家创建角色-逻辑
	public void createRole(CreateRoleRq req, ClientHandler handler) {
		try {
			// 1.成功  2.名字被占用  3.角色已创建(返回nick和portrait)
			CreateRoleRs.Builder builder = CreateRoleRs.newBuilder();
			int accountKey = playerManager.getAccountSessionMap().get(handler.getChannelId());
			if (accountKey == 0) {// 重新登录验证token
				builder.setState(3);
				handler.sendMsgToPlayer(GameError.ALREADY_CREATE, CreateRoleRs.ext, builder.build());
				LogHelper.CONFIG_LOGGER.info("player is created!");
				return;
			}

			Account account = playerManager.getAccountByAccountKey(accountKey);
			if(account == null){
				return;
			}
			// 玩家已经创建
			if (account != null && account.getCreated() == 1 && account.getLordId() != 0) {
				builder.setState(3);
				handler.sendMsgToPlayer(GameError.ALREADY_CREATE, CreateRoleRs.ext, builder.build());
				LogHelper.CONFIG_LOGGER.info("player is created!");
				return;
			}
			int country = req.getCountry();
			// 国家不合法
			if (country < 1 || country > 3) {
				LogHelper.CONFIG_LOGGER.info("country error, country = " + country);
				handler.sendErrorMsgToPlayer(GameError.COUNTRY_ERROR);
				return;
			}
			Player player = playerManager.createPlayer(account, country);
			if (playerManager.createFullPlayer(player, req)) {

				changeNewPlayer(player.roleId, handler);
				player.immediateSave = true;
				/**
				 * 记录玩家角色创建日志
				 */
				LogUser logUser = SpringUtil.getBean(LogUser.class);
				logUser.roleCreateLog(player);

				worldManager.playerBronPos(player);
				//修复初始位置是-1-1造成的玩家被积分显示的历史位置错误
				player.getOldPos().setPos(player.getPosX(), player.getPosY());

				playerManager.checkMapStatusByWorldTarget(player);
				// 同步玩家所有地图信息
				worldManager.synPlayerMapStatus(player);
				activityManager.reflushSeven(account.getLordId(), ActivityConst.ACT_LOGIN_SEVEN);
				activityManager.reflushSeven(account.getLordId(), ActivityConst.ACT_SEARCH);
				/**
				 * 玩家通过任务奖励获得的经验值日志埋点
				 */
				logUser.roleExpLog(RoleExpLog.builder()
					.channel(player.account.getChannel())
					.commandLevel(player.getCommandLv())
					.increaseExp(0)
					.country(player.getCountry())
					.energy(player.getEnergy())
					.exp(player.getExp())
					.reason(Reason.CREATE_ACCOUNT)
					.roleCreateTime(player.account.getCreateDate())
					.roleId(player.roleId)
					.rolelv(player.getLevel())
					.roleName(player.getNick())
					.techLevel(player.getTechLv())
					.title(player.getTitle())
					.vip(player.getVip())
					.build());

				/**
				 *	角色晋升军衔的日志埋点
				 */
				logUser.roleTitleLog(player, 0);
				/**
				 * 新手引导日志埋点
				 */
				logUser.roleGuideLog(new RoleGuideLog(player.getLord().getLordId(), serverManager.getServerId(), player.account.getChannel(), 100));
				/**
				 * 主线任务
				 */
				logUser.roleTaskLog(new RoleTaskLog(player.getLord().getLordId(), serverManager.getServerId(), player.account.getChannel(), 0));
				SpringUtil.getBean(EventManager.class).create_role(player);
				SpringUtil.getBean(EventManager.class).record_userInfo_once(player, EventName.create_role);
				tdManager.openBouns(player);

				// 测试账号
				if (account.isTestAccount()) {
					PlayerTestManager playerTestManager = SpringUtil.getBean(PlayerTestManager.class);
					playerTestManager.createTestPlayer(player);
				}

				/**
				 *	给账号服发送角色信息
				 */

				Player tmpPlayer = player;
				SpringUtil.getBean(LoginExecutor.class).add(() -> {
					playerManager.saveUcServerInfos(tmpPlayer);
					sentPost(tmpPlayer, 0);
				});
			} else {
				// 创建失败:异常的号会进入这里, TODO
				if (account != null) {
					account.setCreated(0);
				}
				handler.sendErrorMsgToPlayer(GameError.CREATE_PLAYER_FAILED);
				LogHelper.CONFIG_LOGGER.info("createFullPlayer {" + player.roleId + "} error");
			}
		} catch (Exception ex) {
			LogHelper.ERROR_LOGGER.error(LogExceptionUtil.Log(ex));
		}

	}

	private void changeNewPlayer(final long roleId, final ClientHandler handler) {
		Player newPlayer = playerManager.getPlayer(roleId);
		if (newPlayer == null) {
			LogHelper.CONFIG_LOGGER.info("changeNewPlayer {" + roleId + "} error");
			return;
		}

		playerManager.addPlayer(newPlayer);

		CreateRoleRs.Builder builder = CreateRoleRs.newBuilder();
		builder.setState(1);
		Lord lord = newPlayer.getLord();
		if (lord != null) {
			builder.setLordId(lord.getLordId());
			String nickName = lord.getNick();
			if (!StringUtil.isNullOrEmpty(nickName)) {
				builder.setNick(nickName);
			}
			int portrait = lord.getPortrait();
			builder.setPortrait(portrait);

		}
		builder.setCreateTime(newPlayer.account.getCreateDate().getTime() / 1000);
		handler.sendMsgToPlayer(GameError.OK, CreateRoleRs.ext, builder.build());
	}

	// 客户端获取服务器时间
	public void getTime(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player != null) {
			player.setLogin(true);
			playerManager.recordLogin(player);
		}
		RolePb.GetTimeRs.Builder builder = RolePb.GetTimeRs.newBuilder();
		builder.setTime(System.currentTimeMillis());
		builder.setOpenPay(true);
		builder.setOffsetUTC(TimeHelper.zoneOffset());
		handler.sendMsgToPlayer(RolePb.GetTimeRs.ext, builder.build());
	}

	// 恢复逻辑
	private void restoreLogic(Player player, long now) {
		playerManager.backEnergy(player, now);
		playerManager.backCollectTimes(player, now);
		playerManager.backPeople(player, now);
	}

	// 玩家断线重连协议
	public void roleReloginRq(RoleReloginRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {

			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		RoleReloginRs.Builder builder = playerManager.wrapRoleReLogin(player);
		handler.sendMsgToPlayer(RolePb.RoleReloginRs.ext, builder.build());
	}

	// 玩家0点刷新协议处理
	public void refreshDataRq(RefreshDataRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {

			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		//playerManager.getLoginQue().add(player);

		RefreshDataRs.Builder refreshData = playerManager.wrapRefreshData(player);
		refreshData.setRoleLoginRs(playerManager.wrapRoleLoginRs(player));
		handler.sendMsgToPlayer(RolePb.RefreshDataRs.ext, refreshData.build());
	}

	// 恢复能量和征收次数的定时器逻辑
	public void restoreDataTimerLogic() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		long now = System.currentTimeMillis();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			restoreLogic(player, now);
		}
	}

	// 恢复洗练次数
	public void recoverWashTimes() {
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		long now = System.currentTimeMillis();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			playerManager.recoverWashSkillTimes(player, now);
			playerManager.recoverWashHeroTimes(player, now);
		}
	}

	// 玩家购买体力值
	public void buyEnergyRq(RolePb.BuyEnergyRq req, ClientHandler handler) {
		// 检查购买次数
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查当前的购买次数够不够
		Lord lord = player.getLord();
		int vip = lord.getVip();
		StaticVip staticVip = staticVipMgr.getStaticVip(vip);
		if (staticVip == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_VIP_CONFIG);
			return;
		}

		// 检查lord的体力是否需要刷新, 条件是凌晨刷新
		// 每日刷新的时候需要特殊处理一下
		int day = GameServer.getInstance().currentDay;
		if (day != lord.getBuyEnergyTime()) {
			lord.setBuyEnergy(0);
			lord.setBuyEnergyTime(day);
		}

		int configTimes = staticVip.getBuyPower();
		int currentTimes = lord.getBuyEnergy();
		if (currentTimes >= configTimes) {
			handler.sendErrorMsgToPlayer(GameError.BUY_ENERGY_TIME_NOT_ENOUGH);
			return;
		}

		int buyTimes = lord.getBuyEnergy() + 1;
		StaticEnergyPrice staticEnergyPrice = staticLimitMgr.getStaticEnergyPrice(buyTimes);
		if (staticEnergyPrice == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_BUY_ENERGY_TIME_PPRICE_CONFIG);
			return;
		}

		int costGold = 0;
		// 首次免费
		if (buyTimes > 1) {
			int needGold = staticEnergyPrice.getPrice();
			int owned = player.getGold();
			if (owned < needGold) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			costGold = needGold;
			playerManager.subAward(player, AwardType.GOLD, 1, needGold, Reason.BUY_ENERGY);
		}

		lordManager.addEnergy(lord, 100, Reason.BUY_ENERGY);
		lord.setBuyEnergy(lord.getBuyEnergy() + 1);
		RolePb.BuyEnergyRs.Builder builder = RolePb.BuyEnergyRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setEnergy(player.getEnergy());
		builder.setEnergyCD(playerManager.getEnergyCD(player));
		builder.setEnergyBuy(player.getBuyEnergy());

		handler.sendMsgToPlayer(RolePb.BuyEnergyRs.ext, builder.build());
		activityEventManager.activityTip(EventEnum.BUY_ENERGY, player, 1, 0);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.BUY_ENERGY, 1);
		dailyTaskManager.record(DailyTaskId.BUY_ENERGY, player, 1);
		eventManager.buyEnergy(player, Lists.newArrayList(lord.getBuyEnergy(), costGold));
		achievementService.addAndUpdate(player, AchiType.AT_15, 1);
	}

	@Autowired
	AchievementService achievementService;

	// 　换头像
	public void setPortrait(RolePb.SetPortraitRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Lord lord = player.getLord();
		if (lord == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_LORD);
			return;
		}

		int portrait = req.getPortrait();
		StaticPortrait staticPortrait = staticIniDataMgr.getPortrait(portrait);
		if (staticPortrait == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		SimpleData simpleData = player.getSimpleData();
		HashMap<Integer, Integer> portraits = simpleData.getIcons();
		if (!portraits.containsKey(portrait)) {
			handler.sendErrorMsgToPlayer(GameError.PORTRAITS_NOT_OPEN);
			return;
		}
		lord.setPortrait(req.getPortrait());
		portraits.put(portrait, 1);

		RolePb.SetPortraitRs.Builder builder = RolePb.SetPortraitRs.newBuilder();
		handler.sendMsgToPlayer(RolePb.SetPortraitRs.ext, builder.build());
	}

	// 新手引导
	public void newStateRq(RolePb.NewStateRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int beforePosX = player.getPosX();
		Lord lord = player.getLord();
		if (lord == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_LORD);
			return;
		}

		int stateId = req.getNewStateId();
		lord.setNewState(stateId);

		// 触发新手引导任务
		taskManager.doTask(TaskType.NEW_STATE, player, null);

		// 检查下一个新手引导
		RolePb.NewStateRs.Builder builder = RolePb.NewStateRs.newBuilder();
		if (!player.isNewStateDone(stateId)) {
			// 检查这个新手引导时候开启建筑
			StaticNewState staticNewState = taskManager.getStaticNewState(stateId);
			if (staticNewState != null) {
				//taskManager.synBuildings(player, staticNewState.getOpenBuilding());
				buildingManager.synBuildings(player, staticNewState.getOpenBuilding());
				player.setAutoBuildTimes(lord.getAutoBuildTimes() + staticNewState.getAutoBuild());
				player.addNewState(stateId);
				if (staticNewState.getAutoBuild() > 0) {
					player.setOnBuild(0);
				}
				if (stateId == 14) {
					playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.EXP, staticLimitMgr.getNum(169), Reason.NEW_STATE);
					builder.setExp(staticLimitMgr.getNum(169));
					List<Integer> heroList = player.getEmbattleList();
					for (Integer heroId : heroList) {
						if (heroId == -1 || heroId == 0) {
							continue;
						}

						Hero hero = player.getHero(heroId);
						if (hero == null) {
							continue;
						}
						heroManager.addExp(hero, player, staticLimitMgr.getNum(170), Reason.NEW_STATE);
					}
					builder.setHeroExp(staticLimitMgr.getNum(170));
				}

			} else {
				LogHelper.CONFIG_LOGGER.info("stateId" + stateId);
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			}
		}

		CommonPb.Pos.Builder posBuilder = CommonPb.Pos.newBuilder();
		int afterPosX = player.getPosX();
		if (beforePosX != afterPosX) {
			posBuilder.setX(player.getPosX());
			posBuilder.setY(player.getPosY());
			builder.setPos(posBuilder);
			builder.setProtectedTime(player.getProectedTime());
		}

		builder.setAutoBuildTimes(lord.getAutoBuildTimes());
		handler.sendMsgToPlayer(RolePb.NewStateRs.ext, builder.build());
	}

	/**
	 * 开启自动
	 *
	 * @param req
	 * @param handler
	 */
	public void openAuto(RolePb.OpenAutoRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Lord lord = player.getLord();
		if (lord == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_LORD);
			return;
		}
		int autoId = req.getAutoId();
		if (autoId < 1 || autoId > 3) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		RolePb.OpenAutoRs.Builder builder = RolePb.OpenAutoRs.newBuilder();
		// 逻辑上解耦
		if (autoId == 1) {
			handleWallAuto(player);
			builder.setOnBuild(player.getWallAuto());
		} else if (autoId == 2) {
			handleAutoBuild(player);
			builder.setOnBuild(player.getOnBuild());
		} else if (autoId == 3) {
			boolean res = handleSoldierAuto(player, handler);
			if (!res) {
				return;
			}
			builder.setOnBuild(player.getSoldierAuto());
		}
		builder.setAutoId(autoId);
		handler.sendMsgToPlayer(RolePb.OpenAutoRs.ext, builder.build());
	}

	public boolean canAutoBuild(Player player) {
		List<Integer> taskBuild = taskManager.getTaskBuilding(player);
		// 找出剩余的建筑
		TreeSet<Integer> buildings = player.getBuildingIds();
		if (!taskBuild.isEmpty()) {
			buildings.removeAll(taskBuild);
		}

		return !taskBuild.isEmpty() || !buildings.isEmpty();

	}

	public void handleAutoBuild(Player player) {
		// 自动建造
		int onAutoBuild;
		boolean canAuto = canAutoBuild(player);

		if (player.getOnBuild() == 1) { // 关闭
			onAutoBuild = 0;
		} else {
			if (canAuto) { // 开启
				onAutoBuild = 1;
			} else {
				onAutoBuild = 0;
			}
		}

		player.setOnBuild(onAutoBuild);
	}

	public void handleWallAuto(Player player) {
		Wall wall = player.getWall();
		if (wall == null) {
			LogHelper.CONFIG_LOGGER.info("wall is null!");
			return;
		}

		if (wall.getLv() < 1) {
			LogHelper.CONFIG_LOGGER.info("wall.getLv() < 1!");
			return;
		}

		Lord lord = player.getLord();
		int onwall = lord.getOnWall();
		if (onwall == 0) {
			lord.setOnWall(1);
		} else if (onwall == 1) {
			lord.setOnWall(0);
		}
	}

	public boolean handleSoldierAuto(Player player, ClientHandler handler) {
		// 检查玩家的自动补兵功能是否开启
		int techAutoSoldier = techManager.getAutoSoldier(player);
		if (techAutoSoldier != 1) {
			handler.sendErrorMsgToPlayer(GameError.NOT_OPEN_SOLDIER_AUTO);
			return false;
		}

		if (player.getSoldierAuto() == 0) {
			player.setSoldierAuto(1);
		} else if (player.getSoldierAuto() == 1) {
			player.setSoldierAuto(0);
		}
		return true;
	}

	public void getLevelAward(RolePb.GetLevelAwardRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int type = req.getType();
		int level = req.getLevel();
		// 检查是否有奖励
		Map<Integer, LevelAward> levelAwardMap = player.getLevelAwardsMap();
		LevelAward levelAward = levelAwardMap.get(level);
		GetLevelAwardRs.Builder builder = GetLevelAwardRs.newBuilder();
		if (levelAward == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_LEVEL_AWARD);
			return;
		}
		switch (type) {
			case 1:
				if (levelAward.getStatus() == 1) {
					handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
					return;
				}
				levelAward.setStatus(1);
				List<Award> awards = levelAward.getAwards();
				if (awards == null) {
					handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
					return;
				}
				playerManager.addAward(player, awards, Reason.LEVEL_UP_LORD);
				builder.setAwards(levelAward.wrapPb());
				StaticLordLv staticLordLv = staticLordDataMgr.getStaticLordLv(levelAward.getLevel());
				if (staticLordLv != null) {
					LevelAward levelAward1 = levelAwardMap.get(staticLordLv.getNextLv());
					if (levelAward1 != null && levelAward1.getStatus() == 0) {
						builder.setLevel(levelAward1.getLevel());
					}
				}
				handler.sendMsgToPlayer(RolePb.GetLevelAwardRs.ext, builder.build());
				break;
			case 2:
				if (levelAward != null) {
					if (levelAward.getStatus() != 0) {
						handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
						return;
					}

					builder.setAwards(levelAward.wrapPb());
					builder.setLevel(levelAward.getLevel());
				}
				handler.sendMsgToPlayer(RolePb.GetLevelAwardRs.ext, builder.build());
				break;
			default:
				break;
		}
	}

	/**
	 * @param code
	 * @param handler
	 */
	public void giftCode(String code, ClientHandler handler) {
//		if (code.length() != 12) {
//			handler.sendErrorMsgToPlayer(GameError.GIFT_CODE_LENTH);
//			return;
//		}
//
//		Player player = playerManager.getPlayer(handler.getRoleId());
//
//		UseGiftCodeRq.Builder builder = UseGiftCodeRq.newBuilder();
//		builder.setCode(code);
//		builder.setLordId(player.roleId);
//		builder.setServerId(player.account.getServerId());
//		builder.setPlatNo(player.account.getChannel());
//
//		Base.Builder baseBuilder = PbHelper.createRqBase(UseGiftCodeRq.EXT_FIELD_NUMBER, 0L, UseGiftCodeRq.ext, builder.build());
//		handler.sendMsgToPublic(baseBuilder);
	}

	/**
	 * Method: useGiftCodeRs
	 *
	 * @param req
	 * @param handler
	 * @return void
	 * @throws
	 * @Description: 使用兑换码
	 */
	public void useGiftCodeRs(final UseGiftCodeRs req, final ServerHandler handler) {
		GameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
			@Override
			public void action() {

				long roleId = req.getLordId();
				String award = req.getAward();

				Player player = playerManager.getPlayer(roleId);
//				ChannelHandlerContext ctx = player.ctx;

				GiftCodeRs.Builder builder = GiftCodeRs.newBuilder();
				builder.setState(req.getState());

				int state = req.getState();
				if (state != 0) {
					if (player.isLogin && player.getChannelId() != -1) {
						Base.Builder baseBuilder = handler.createRsBase(GameError.OK, GiftCodeRs.ext, builder.build());
						GameServer.getInstance().sendMsgToPlayer(player, baseBuilder);
					}
					return;
				}

				try {
					JSONArray arrays = JSONArray.parseArray(award);
					for (int i = 0; i < arrays.size(); i++) {
						JSONArray array = arrays.getJSONArray(i);
						if (array.size() != 3) {
							continue;
						}
						int type = array.getInteger(0);
						int id = array.getInteger(1);
						int count = array.getInteger(2);
						int keyId = playerManager.addAward(player, type, id, count, Reason.GIFT_CODE);
						builder.addAward(PbHelper.createAward(player, type, id, count, keyId).build());

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (player.isLogin && player.getChannelId() != -1) {
					Base.Builder baseBuilder = handler.createRsBase(GameError.OK, GiftCodeRs.ext, builder.build());
					GameServer.getInstance().sendMsgToPlayer(player, baseBuilder);
				}
			}
		}, DealType.MAIN);

	}

	public void autoAddSoldier(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		if (player.getSoldierAuto() == 0) {
			handler.sendErrorMsgToPlayer(GameError.AUTO_SOLDIER_NOT_OPEN);
			return;
		}

		RolePb.AutoAddSoldierRs.Builder builder = RolePb.AutoAddSoldierRs.newBuilder();
		for (Integer heroId : player.getEmbattleList()) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}

			// 英雄正在行军之中
			if (player.isInMarch(hero)) {
				continue;
			}

			if (player.isInMass(heroId)) {
				continue;
			}

			if (player.hasPvpHero(heroId)) {
				continue;
			}
			heroManager.caculateProp(hero, player);
			soldierManager.autoHeroAdd(player, hero);

			// 武将兵力
			builder.addHeroChange(hero.createHeroChange());
		}

		// 玩家兵营剩余兵力
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		for (Map.Entry<Integer, Soldier> soldierElem : soldierMap.entrySet()) {
			Soldier soldier = soldierElem.getValue();
			if (soldier == null) {
				continue;
			}
			builder.addSoldierInfo(soldier.wrapPb());
		}

		handler.sendMsgToPlayer(RolePb.AutoAddSoldierRs.ext, builder.build());

	}

	public void newChangeName(RolePb.NewChangeNameRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int newStateId = req.getNewStateId();
		Lord lord = player.getLord();
		if (lord == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (lord.getNewState() >= newStateId) {
			handler.sendErrorMsgToPlayer(GameError.NEW_STATE_IS_DONE);
			return;
		}

		String nick = req.getNick();
		String newName = nick;
		String oldName = player.getNick();
		if (!nick.equals(player.getNick())) {
			if (!playerManager.isNickOk(nick)) {
				handler.sendErrorMsgToPlayer(GameError.NICK_NAME_ERROR);
				return;
			}

			// 昵称是否被占用
			if (playerManager.takeNick(nick)) {
				handler.sendErrorMsgToPlayer(GameError.SAME_NICK);
				return;
			}
		}

		player.addNewState(newStateId);
		player.setCreateState(2);
		nickManager.setPlayerNick(player.getLord(), nick);
		if (lord.getNewState() < newStateId) {
			lord.setNewState(newStateId);
		}
		RolePb.NewChangeNameRs.Builder builder = RolePb.NewChangeNameRs.newBuilder();
		handler.sendMsgToPlayer(RolePb.NewChangeNameRs.ext, builder.build());

		playerManager.caculateAllScore(player); // 计算玩家的战斗力
		try {
			rankManager.checkRankList(player.getLord()); // 检查排行榜
		} catch (Exception ex) {
			LogHelper.ERROR_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * 跟新新手引导步骤
	 *
	 * @param handler
	 * @param req
	 */
	public void updateGuide(UpdateGuideHandler handler, RolePb.UpdateGuideRq req) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int oldGuidKey = player.getLord().getGuideKey();
		player.getLord().setGuideKey(req.getGuideKey());
		RolePb.UpdateGuideRs.Builder builder = RolePb.UpdateGuideRs.newBuilder();
		handler.sendMsgToPlayer(RolePb.UpdateGuideRs.ext, builder.build());
		/**
		 * 新手引导日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleGuideLog(new RoleGuideLog(player.getLord().getLordId(), serverManager.getServerId(), player.account.getChannel(), req.getGuideKey()));

		SpringUtil.getBean(EventManager.class).guide(player, req.getGuideKey());
		SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.guide_step);
//        LogHelper.ERROR_LOGGER.error("新手引导 guidkey->[{}]", req.getGuideKey());
		//第一次跳过100-430
		if (oldGuidKey == 100 && req.getGuideKey() == 430) {
			eventManager.jumpCgPlane(player, Lists.newArrayList(oldGuidKey, req.getGuideKey()));
		}
		if (oldGuidKey == 590 && req.getGuideKey() == 710) {
			eventManager.jumpCgMonster(player, Lists.newArrayList(oldGuidKey, req.getGuideKey()));
		}
	}

	public void recordUI(RecordHandler handler, ChatPb.RecordRq recordRq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (!player.getRecordList().contains(recordRq.getParam())) {
			player.getRecordList().add(recordRq.getParam());
			activityManager.flushShowBeauty(player, recordRq.getParam());
//
		}
		//第一次打开秘书界面之后
		handler.sendMsgToPlayer(ChatPb.RecordRs.ext, ChatPb.RecordRs.newBuilder().setParam(recordRq.getParam()).build());
	}

	/**
	 * @Description 玩家角色创建成功给账号服发送角色信息
	 * @Date 2021/3/10 16:21
	 * @Param [player]
	 * @Return
	 **/
	public void sentPost(Player player, int num) {
		if (num > 5) {
			return;
		}
		String url = accountServerUrl + "/account/CreateRoleRq.do";
		String accountKey = String.valueOf(player.account.getAccountKey());
		String serverId = String.valueOf(player.account.getServerId());
		String country = String.valueOf(player.getCountry());
		String postBody = new StringBuffer()
			.append("accountKey=")
			.append(accountKey)
			.append("&serverId=")
			.append(serverId)
			.append("&country=")
			.append(country).toString();
		String str = null;
		try {
			str = HttpUtils.sentPost(url, postBody);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("createRole sentPost {}", e);
		}
		try {
			if (str != null) {
				if (str.indexOf("success") != -1) {
					return;
				}
			}
			int next = num + 1;
			SpringUtil.getBean(LoginExecutor.class).add(() -> {
				sentPost(player, next);
			});
		} catch (Exception e) {
			int next = num + 1;
			sentPost(player, next);
		}
	}

	public void guidler(RolePb.GuilderRq rq, ClientHandler clientHandler) {
//		String s = HeartBeatHelper.decodeHeartBeatData("cd51e9c41a19843f9d58a9478106b154", rq.getInput());
//		ChannelHandlerContext ctx = clientHandler.getCtx();
	}

}
