package com.game.service;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.*;
import com.game.dao.p.AccountDao;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.domain.p.CtyGovern;
import com.game.log.LogUser;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.UseCdkHandler;
import com.game.network.ChannelAttr;
import com.game.network.ChannelUtil;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.*;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.*;
import com.game.server.GameServer;
import com.game.server.exec.HttpExecutor;
import com.game.server.exec.LoginExecutor;
import com.game.spring.SpringUtil;
import com.game.uc.CdkeyItem;
import com.game.uc.Message;
import com.game.util.*;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class AccountService {

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private CountryManager countryMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private StaticActivityMgr staticActivityMgr;

	@Autowired
	private UcHttpService serveice;

	@Autowired
	private ServerManager serverManager;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CityManager cityManager;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private JourneyManager journeyManager;

	@Autowired
	private CityGameService cityGameService;

	@Autowired
	private RiotManager riotManager;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private PersonalityManager personalityManager;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private SurpriseGiftManager surpriseGiftManager;

	@Autowired
	EventManager eventManager;
	@Autowired
	LogUser logUser;

	/**
	 * 客户端发过来的登陆验证请求，这里转发给账号服务器做验证
	 **/
	public void beginGame(UserLoginRq req, ClientHandler handler) {
		int keyId = req.getKeyId();
		String token = req.getToken();
		int serverId = req.getServerId();
		String curVersion = req.getClientVer();
		Message message;
		String deviceNo = req.getDeviceNo();
		//boolean reconnect = req.getReconnect();
		try {
			message = serveice.verifyToUc(keyId, token, serverId, req.getChannel());
			LogHelper.MESSAGE_LOGGER.info("beginGame verifyToUc:{}", message);
		} catch (Exception e) {
			message = new Message(UcCodeEnum.ERROR);
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
		if (message == null) {
			Base.Builder builder = Base.newBuilder();
			builder.setCommand(UserLoginRs.EXT_FIELD_NUMBER);
			builder.setCode(GameError.TOKEN_LOST.getCode());
			handler.sendMsgToPlayer(builder);
			return;
		}

		Long channelId = handler.getChannelId();
		//ChannelHandlerContext ctx = gameServer.userChannels.get(channelId);

		ChannelHandlerContext ctx = handler.getCtx();
		//玩家原来的ctx已经断了，需要重新登陆
		if (message.getCode() != UcCodeEnum.SUCCESS.getCode()) {
//			logger.error("beginGame error {} keyId {} token {} serverId {} ", message.toString(), keyId, token, serverId);
			if (message.getCode() != 38 && message.getCode() != 39) {
				Base.Builder builder = Base.newBuilder();
				builder.setCommand(UserLoginRs.EXT_FIELD_NUMBER);
				builder.setCode(message.getCode());
				handler.sendMsgToPlayer(builder);
			} else {
				UserLoginRs.Builder builder = UserLoginRs.newBuilder();
				switch (message.getCode()) {
					case 38:    //账号已封禁
						builder.setState(3);
						break;
					case 39://角色已封禁
						builder.setState(5);
						break;
				}
				Base.Builder baseBuilder = PbHelper.createRqBase(UserLoginRs.EXT_FIELD_NUMBER, null, UserLoginRs.ext, builder.build());
				handler.sendMsgToPlayer(baseBuilder);
			}
			return;
		}
		boolean flag = false;
		if (curVersion != null && message.getVersion() != null) {
			String trim = curVersion.trim();
			String trim1 = message.getVersion().trim();// 服务器记录版本
			if (trim.equals(trim1)) {
				flag = true;
			} else {
				String[] split = trim.split("\\.");
				String[] split1 = trim1.split("\\.");
				if (split.length == split1.length) {
					for (int i = 0; i < split.length; i++) {
						int i1 = Integer.parseInt(split[i]);
						int i2 = Integer.parseInt(split1[i]);
						if (i1 > i2) {
							flag = true;
						}
					}
				}
			}
		}
		if (req.getChannel() > 100 && message.getVersion() != null && !flag) {
			LogHelper.MESSAGE_LOGGER.info("版本异常 version:{}", message.getVersion());
			handler.sendErrorMsgToPlayer(GameError.VERSION_LOW);
			return;
		}
		UserLoginRs.Builder builder = UserLoginRs.newBuilder();
		Date now = new Date();
		Account account = playerManager.getAccount(serverId, keyId);
		if (account == null) {
			//最大注册人数
			Map<Integer, Map<Integer, Account>> accountCache = playerManager.getAccountCache();
			int sum = accountCache.values().stream().flatMapToInt(x -> IntStream.of(x.size())).sum();
			if (sum >= serverManager.getServer().getMaxRegisterNum()) {
				handler.sendErrorMsgToPlayer(GameError.MAX_REGISTER_NUM);
				return;
			}
			account = new Account();
			account.setServerId(serverId);
			account.setAccountKey(keyId);
			account.setDeviceNo(deviceNo);
			account.setLoginDays(1);
			account.setCreateDate(now);
			account.setLoginDate(now);
			account.setChannel(req.getChannel());
			account.setRegisterIp(handler.getIpAddress());
			accountDao.insertAccount(account);

			// 判断是否未为测试账号,测试账号给满级
			account.setTestAccount(SpringUtil.getBean(PlayerTestManager.class).isTestAccount(message));
		} else {
			Account dbAccount = accountDao.selectAccountByKeyId(account.getKeyId());
			if (dbAccount != null) {
				account.setIsGm(dbAccount.getIsGm());
				account.setIsGuider(dbAccount.getIsGuider());
				account.setWhiteName(dbAccount.getWhiteName());
				account.setLordId(dbAccount.getLordId());
				Date loginDate = account.getLoginDate();
				if (!DateHelper.isSameDate(now, loginDate)) {
					account.setLoginDays(account.getLoginDays() + 1);
					activityManager.reflushSeven(account.getLordId(), ActivityConst.ACT_LOGIN_SEVEN);
					activityManager.reflushSeven(account.getLordId(), ActivityConst.ACT_SEARCH);
				}
				account.setLastLoginIp(handler.getIpAddress());
			}
			account.setDeviceNo(deviceNo);
			account.setLoginDate(now);
			account.setIsDelete(0);
			playerManager.recordLogin(account);
		}
		if (AccountHelper.isForbid(account)) {
			builder.setState(3);
			Base.Builder baseBuilder = PbHelper.createRqBase(UserLoginRs.EXT_FIELD_NUMBER, null, UserLoginRs.ext, builder.build());
			handler.sendMsgToPlayer(baseBuilder);
			return;
		}
		if (account.getCreated() == 1) {// 角色已创建
			builder.setState(2);
		} else {
			builder.setState(1);
		}
		long lordId = account.getLordId();
		if (lordId != 0) {//如果已有玩家
			Player player = playerManager.loginLoadPlayer(account, lordId);
			if (player != null) {
				player.getPushPos().clear();

				// player 设置网关和channelId,便于后续维护
				String gateId = ChannelUtil.getAttribute(ctx, ChannelAttr.NET_SERVER_ID);
				player.setGateId(gateId);
				player.setChannelId(channelId);
			}
		}

		//玩家的channelId：accountKey
		playerManager.reAccount(channelId, account);

		int num = staticLimitMgr.getNum(80);
		Map<Long, Player> players = playerManager.getPlayers();
		// 是否奖励
		if (players.size() > num) {
			account.setMinCoutry(req.getMinCt());
			builder.setIsAward(true);
		} else {
			builder.setIsAward(false);
			account.setMinCoutry(0);
		}
		builder.setTime(System.currentTimeMillis());
		builder.setOffsetUTC(TimeHelper.zoneOffset());

		Base.Builder baseBuilder = PbHelper.createRsBase(GameError.OK, RolePb.UserLoginRs.ext, builder.build(), handler);
		baseBuilder.setCommand(UserLoginRs.EXT_FIELD_NUMBER);
		baseBuilder.setParam(handler.getMsg().getParam());

		Packet packet = PacketCreator.create(UserLoginRs.EXT_FIELD_NUMBER, baseBuilder.build().toByteArray(), account.getLordId(), channelId);
		ctx.writeAndFlush(packet);
		LogHelper.CHANNEL_LOGGER.info("sendToClient accountKey:{} channelId:{} playerId:{} cmd:{}", account.getAccountKey(), packet.getChannelId(), packet.getRoleId(), packet.getCmd());
	}

	/**
	 * 玩家登陆
	 **/
	public void roleLogin(ClientHandler handler, RolePb.RoleLoginRq req) {
		try {

			long channelId = handler.getChannelId();
			Integer accountKey = playerManager.getAccountSessionMap().get(channelId);
			if (accountKey == null) {
				handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
				return;
			}

			Account account = playerManager.getAccountByAccountKey(accountKey);
			if (account == null) {
				handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
				return;
			}

			Player player = playerManager.getPlayer(account.getLordId());
			if (player == null) {
				handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
				return;
			}

			if (player.account == null) {
				handler.sendErrorMsgToPlayer(GameError.ACCOUNT_NOT_CREATED);
				return;
			}

			synchronized (player) {
				if (player.account.getCreated() != 1) {
					handler.sendErrorMsgToPlayer(GameError.INVALID_PARAM);
					return;
				}
				String gateId = ChannelUtil.getAttribute(handler.getCtx(), ChannelAttr.NET_SERVER_ID);
				player.setGateId(gateId);
				player.setChannelId(handler.getChannelId());
				player.setEntering(true);
				if (player.isLogin) { // 若玩家已登陆
					if (player.getChannelId() != -1 && player.getChannelId() != handler.getChannelId()) {
						if (!req.getReconnect()) {
							synOffline(player, 1);
						}
					}
				} else { // 玩家未登陆
					playerManager.addOnline(player);
				}

				player.getPushPos().clear();
				//LogHelper.ERROR_LOGGER.error("player pos->[{}].[{}]", player.roleId, player.getPushPos().size());

				player.setChannelId(handler.getChannelId());
				CtyGovern govern = countryMgr.getGovern(player);
				journeyManager.getJourneyTimes(player);
				playerManager.initProtrait(player);
				RoleLoginRs.Builder builder = playerManager.wrapRoleLoginRs(player);
				player.setFlag(false);
				if (govern != null && !player.getLord().isGovernLogin()) {
					builder.setGovernLogin(true);
				}
				// 读取玩家身份
				builder.setIsGm(player.getAccount().getIsGm());
				builder.setIsGuider(player.getAccount().getIsGuider());
				//更新玩家当天首次登录时间
				checkFirstLoginDate(player);
				handler.sendMsgToPlayer(RoleLoginRs.ext, builder.build());
				//LogHelper.logLogin(player);
				player.setOnlineMessage(CommonPb.OnlineMessage.newBuilder());

				if (govern != null && !player.getLord().isGovernLogin()) {
					player.getLord().setGovernLogin(true);
					chatManager.governLoginChat(player, govern);
				}
				playerManager.refushWorms(player);
				taskManager.checkLineTask(player);
				personalityManager.initPlayer(player);
				/**
				 * 记录玩家角色登录日志
				 */
				logUser.roleLoginLog(player, handler.getIpAddress());
				eventManager.app_login(player);
				eventManager.ta_app_start(player);
				eventManager.record_userInfo(player, EventName.app_login);
			}
			activityManager.roleLoginActivity(player);
			player.getFullLoad().set(true);
		} catch (Exception ex) {
			LogHelper.ERROR_LOGGER.error(LogExceptionUtil.Log(ex));
		}

	}

	/**
	 * @Description 更新玩家当天首次登录时间
	 * @Param []
	 * @Return void
	 * @Date 2021/11/23 15:17
	 **/
	private void checkFirstLoginDate(Player player) {
		Date date = player.account.getFirstLoginDate();
		if (date == null || !TimeHelper.isSameDay(date.getTime())) {
			player.account.setFirstLoginDate(new Date());
			SpringUtil.getBean(LoginExecutor.class).add(() -> {
				accountDao.updateFirstLoginDate(player.account);
			});
		}
	}

	@Autowired
	WorldManager worldManager;

	public void enterGame(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		player.setLogin(true);
		EnterGameRs.Builder builder = EnterGameRs.newBuilder();
		handler.sendMsgToPlayer(GameError.OK, EnterGameRs.ext, builder.build());
		playerManager.recordLogin(player);
		//推送虫族入侵信息"SynRoitBuff",
		riotManager.synRiotBuff(player);
		//推送主城小游戏
		cityGameService.sendToPlayer(player, player.getSmallCityGame());
		HeroPb.SynScoreRs.Builder b = HeroPb.SynScoreRs.newBuilder();
		b.setScore(player.getMaxScore());
		Base.Builder msg1 = PbHelper.createSynBase(HeroPb.SynScoreRs.EXT_FIELD_NUMBER, HeroPb.SynScoreRs.ext, b.build());
		GameServer.getInstance().sendMsgToPlayer(player, msg1);
		personalityManager.checkIconOpen(player, 1);
		dailyTaskManager.record(DailyTaskId.LOGIN, player, 1);
		activityManager.enterActSeven(player, ActivityConst.TYPE_SET, 3001, 0, 1);
		activityManager.getPassPortTask(player);
		activityManager.enterPassPortTaskCond(player, ActPassPortTaskType.EVERY_DAY_LOGIN, 1);
		surpriseGiftManager.doSurpriseGift(player, SuripriseId.Login, player.account.getTimeFromCreat(), false);
		worldManager.sendWar(player);

		if (player.getBookFlush() == 1) {
			WarBookPb.ReFlushBookShopRq build = WarBookPb.ReFlushBookShopRq.newBuilder().build();
			BasePb.Base.Builder msg = PbHelper.createSynBase(WarBookPb.ReFlushBookShopRq.EXT_FIELD_NUMBER, WarBookPb.ReFlushBookShopRq.ext, build);
			GameServer.getInstance().sendMsgToPlayer(player, msg);
		}
	}

	/**
	 * @param player
	 * @param type   1, 您的账号已在异地登录  2 服务器已经维护 3, 您已经被封号
	 */
	public void synOffline(Player player, int type) {
		RolePb.SynOfflineRq.Builder builder = RolePb.SynOfflineRq.newBuilder();
		builder.setType(type);
		Base.Builder baseBuilder = PbHelper.createRqBase(RolePb.SynOfflineRq.EXT_FIELD_NUMBER, null, RolePb.SynOfflineRq.ext, builder.build());
//		if (player.getChannelId() != -1) {
//			player.ctx.writeAndFlush(baseBuilder.build());
//		}
		GameServer.getInstance().sendMsgToPlayer(player, baseBuilder);
	}


	/**
	 * 使用cdk
	 *
	 * @param handler
	 * @param req
	 */
	public void useCdk(UseCdkHandler handler, RolePb.UseCdkRq req) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		long lorldId = player.getLord().getLordId();
		int serverId = player.account.getServerId();
		int channel = player.account.getChannel();
		String cdk = req.getCdk();
		SpringUtil.getBean(HttpExecutor.class).add(() -> {
			Message message = serveice.getCdkAward(lorldId, channel, serverId, cdk, player.getLevel());
			if (message.getCode() != UcCodeEnum.SUCCESS.getCode() && message.getCode() != UcCodeEnum.CDK_LEVEL_NOT_ENOUTH.getCode()) {
				handler.sendErrorMsgToPlayer(GameError.CDK_IS_ERROR);
				return;
			}
			if (message.getCode() == UcCodeEnum.CDK_LEVEL_NOT_ENOUTH.getCode()) {
				int limit = 0;
				try {
					limit = Integer.parseInt(message.getData());
				} catch (Exception e) {
					handler.sendErrorMsgToPlayer(GameError.CDK_IS_ERROR);
					return;
				}
				RolePb.UseCdkRs.Builder builder = RolePb.UseCdkRs.newBuilder();
				builder.setLimitlevel(limit);
				handler.sendMsgToPlayer(RolePb.UseCdkRs.ext, builder.build());
				return;
			}
			List<CdkeyItem> cdkeyItems = JSONObject.parseArray(message.getData(), CdkeyItem.class);
			RolePb.UseCdkRs.Builder builder = RolePb.UseCdkRs.newBuilder();
			if (cdkeyItems != null && cdkeyItems.size() > 0) {
				for (CdkeyItem cdkItem : cdkeyItems) {
					int keyId = playerManager.addAward(player, cdkItem.getItemtype(), cdkItem.getItemid(), cdkItem.getItemnum(), Reason.USE_CDK_AWARD);
					CommonPb.Award.Builder award = CommonPb.Award.newBuilder();
					award.setCount(cdkItem.getItemnum());
					award.setId(cdkItem.getItemid());
					award.setType(cdkItem.getItemtype());
					award.setKeyId(keyId);
					builder.addAwards(award);
				}
			}
			handler.sendMsgToPlayer(RolePb.UseCdkRs.ext, builder.build());
		});
	}

	/**
	 * 获取人口数量的协议
	 *
	 * @param req
	 * @param
	 */
	public void getPeopleRq(GetPeopleRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		RolePb.GetPeopleRs.Builder builder = GetPeopleRs.newBuilder();
		builder.setPeople(player.getLord().getPeople());
		builder.setCountryPeople(cityManager.getCountryPeople(player.getCountry()));
		//logger.error("获取人口数量的协议>>>>>>>>getPeopleRq>>>>>>>>本城人口总数="+total);
		handler.sendMsgToPlayer(RolePb.GetPeopleRs.ext, builder.build());
	}
}