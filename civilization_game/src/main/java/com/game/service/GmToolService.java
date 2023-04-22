package com.game.service;

import com.game.server.LogicServer;
import com.game.util.PbHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.constant.ActSevenConst;
import com.game.constant.ActivityConst;
import com.game.constant.AwardType;
import com.game.constant.ChatId;
import com.game.constant.ChatShowType;
import com.game.constant.LordPropertyType;
import com.game.constant.MailId;
import com.game.constant.Reason;
import com.game.constant.ResourceType;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticHeroMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.domain.p.Building;
import com.game.domain.p.Equip;
import com.game.domain.p.Hero;
import com.game.domain.p.HeroEquip;
import com.game.domain.p.Item;
import com.game.domain.p.Lord;
import com.game.domain.p.Resource;
import com.game.domain.p.Task;
import com.game.domain.s.StaticBuilding;
import com.game.domain.s.StaticHero;
import com.game.manager.ActivityManager;
import com.game.manager.ChatManager;
import com.game.manager.HeroManager;
import com.game.manager.LordManager;
import com.game.manager.PlayerManager;
import com.game.manager.TaskManager;
import com.game.manager.WorldManager;
import com.game.message.handler.DealType;
import com.game.message.handler.ServerHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb;
import com.game.pb.GmToolPb.AddItemRq;
import com.game.pb.GmToolPb.FakeRechargeRq;
import com.game.pb.GmToolPb.ForbiddenRq;
import com.game.pb.GmToolPb.GameMailRq;
import com.game.pb.GmToolPb.GetOnlinesRq;
import com.game.pb.GmToolPb.GetPersonRq;
import com.game.pb.GmToolPb.ModVipRq;
import com.game.pb.GmToolPb.ModifyRoleRq;
import com.game.pb.GmToolPb.NoticeRq;
import com.game.pb.GmToolPb.PersonMailRq;
import com.game.pb.GmToolPb.ReplyPersonMailRq;
import com.game.pb.GmToolPb.TaskJumpRq;
import com.game.pb.GmToolPb.TowValue;
import com.game.pb.InnerPb.RecOnlinesRq;
import com.game.pb.InnerPb.RecPersonRq;
import com.game.server.GameServer;
import com.game.server.ICommand;
import com.game.util.LogHelper;
import com.game.util.BasePbHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.Pos;

@Service
public class GmToolService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private LordManager lordManager;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private StaticVipMgr staticVipDataMgr;

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private StaticHeroMgr staticHeroMgr;

	@Autowired
	private HeroManager heroManager;
	@Autowired
	LogicServer logicServer;

	// http://106.75.138.179:9400/redalert_account/account/gm.do?toolId=item&sid=1&type=101&id=7&count=30&name=zl011
	public void addItem(final AddItemRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				addItemTo(req);
			}
		}, DealType.MAIN);
	}

	public void getPersonRq(final GetPersonRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				String marking = req.getMarking();
				String callMethod = req.getCallMethod();
				int serverId = req.getServerId();
				long lordId = req.getLordId();

				getPersonLogic(marking, callMethod, serverId, lordId);
			}
		}, DealType.MAIN);
	}

	public void modVip(final ModVipRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				long lordId = req.getLordId();
				int type = req.getType();
				int value = req.getValue();
				modVipLogic(lordId, type, value);
			}
		}, DealType.MAIN);

	}

	public void forbidden(final ForbiddenRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				int forbiddenId = req.getForbiddenId();
				long lordId = req.getLordId();
				forbiddenLogic(forbiddenId, lordId);

			}
		}, DealType.MAIN);

	}

	public void sendMail(final GameMailRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				String making = req.getMarking();
				int mailId = req.getMailId();
				int channelNo = req.getChannelNo();// 0全体 1-N渠道
				int country = 0;
				if (req.hasCountry()) {
					country = req.getCountry();
				}
				List<String> paramList = req.getParamList();
				List<CommonPb.Award> awardList = req.getAwardList();
				gameMailLogic(making, channelNo, country, mailId, paramList, awardList);
			}
		}, DealType.MAIN);
	}

	public void personMailRq(final PersonMailRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				String marking = req.getMarking();
				int mailId = req.getMailId();
				long lordId = req.getLordId();
				List<CommonPb.Award> awardList = req.getAwardList();
				personMailLogic(marking, lordId, mailId, awardList);
			}
		}, DealType.MAIN);

	}

	public void replyPersonMailRq(final ReplyPersonMailRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				String marking = req.getMarking();
				int mailId = req.getMailId();
				long lordId = req.getLordId();
				String title = req.getTitle();
				String content = req.getContent();
				int count = req.getAwardCount();
				if (count <= 0) {
					replyPersonMailLogic(marking, lordId, mailId, title, content);
				} else {
					replyPersonMailLogic(marking, lordId, req.getAwardList(), mailId, title, content);
				}
			}
		}, DealType.MAIN);

	}

	public void sendNotice(final NoticeRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				sendNoticeLogic(req.getContent());
			}
		}, DealType.MAIN);
	}

	public void fakeRecharge(final FakeRechargeRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				long lordId = req.getLordId();
				int amount = req.getAmount();
				int giftId = req.getGiftId();
				fakeRechargeLogic(lordId, amount, giftId);
			}
		}, DealType.MAIN);
	}

	public void getOnlinesRq(final GetOnlinesRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				String marking = req.getMarking();
				int serverId = req.getServerId();
				String callMthod = req.getCallMethod();
				getOnlinesLogic(marking, serverId, callMthod);
			}
		}, DealType.MAIN);
	}
//
//	/**
//	 * 修改用户
//	 *
//	 * @param req
//	 */
	public void modifyUser(ModifyRoleRq req) {
		Player player = playerManager.getPlayer(req.getLordId());
		if (player == null) {
			return;
		}
		Lord lord = player.getLord();
		List<TowValue> towList = req.getTowValueList();
		for (TowValue towValue : towList) {
			int type = towValue.getType();
			int value = towValue.getValue();
			switch (type) {
			case 1:// vip等级
				lord.setVip(value);
				break;
			case 2:// vip经验值
				lord.setTopup(value);
				break;
			case 3:// 等级
				lord.setLevel(value);
				break;
			case 4:// 金币
				lord.setGold(value);
				break;
			case 5: {// GM
				Account account = player.account;
				if (value >= 0 && value <= 1) {
					account.setIsGm(value);
				}
				break;
			}
			case 6: {// 新手指导员
				Account account = player.account;
				if (value >= 0 && value <= 1) {
					account.setIsGuider(value);
				}
				break;
			}
			default:
				break;
			}
		}
	}

	// public void forbidUser(ForbidRoleRq req) {
	// Player player = playerManager.getPlayer(req.getLordId());
	// if (player == null) {
	// return;
	// }
	// if (req.hasSay()) {
	// int say = req.getSay();
	// if (say >= 0 && say <= 1) {
	// player.getLord().setSay(req.getSay());
	// }
	// }
	// if (req.hasAccount()) {
	// int forbid = req.getAccount();
	// if (forbid >= 0 && forbid <= 1) {
	// player.account.setForbid(req.getAccount());
	// }
	// }
	// // if (req.hasOnline()) {
	// // int online = req.getOnline();
	// //
	// // }
	//
	// }

	// http://106.75.138.179:9400/redalert_account/account/gm.do?toolId=item&sid=1&taskId=3&name=认真的奥利弗
	// http://192.168.0.199:8080/redalert_account/account/gm.do?toolId=task&srcert=pp12dss&sid=1&taskId=0&name=认真的奥利弗
	// http://120.92.133.216:9400/redalert_account/account/gm.do?toolId=task&srcert=pp12dss&sid=1&taskId=0&name=认真的奥利弗

	public void jumpTask(final TaskJumpRq req, final ServerHandler handler) {
		 logicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				jumpTaskTo(req);
			}
		}, DealType.MAIN);
	}

	// 跳转玩家任务
	public void jumpTaskTo(TaskJumpRq req) {
		// 所有建筑满级，人物满级，金币1000000万
		if (req.getTaskId() != 0) {
			String nick = req.getNick();
			if (nick == null || nick.isEmpty()) {
				LogHelper.CONFIG_LOGGER.info("nick is null or empty!");
				return;
			}

			Player player = playerManager.getPlayer(nick);
			if (player == null) {
				LogHelper.CONFIG_LOGGER.info("player is null");
				return;
			}

			playerManager.addAward(player, AwardType.RESOURCE, ResourceType.IRON, 10000000, Reason.GM_TOOL);
			playerManager.addAward(player, AwardType.RESOURCE, ResourceType.COPPER, 20000000, Reason.GM_TOOL);
			playerManager.addAward(player, AwardType.RESOURCE, ResourceType.OIL, 20000000, Reason.GM_TOOL);
			playerManager.addAward(player, AwardType.RESOURCE, ResourceType.STONE, 20000000, Reason.GM_TOOL);
		} else {
			greedIsGood(req);
		}

	}

	public void addItemTo(AddItemRq req) {
		String nick = req.getNick();
		Player target = playerManager.getPlayer(nick);
		if (target == null) {
			LogHelper.CONFIG_LOGGER.info("name = " + nick + " not found!");
			return;
		}
		int type = req.getItemType();
		int id = req.getItemId();
		int count = req.getItemCount();
		if (type == AwardType.PROP && count <= 0) {
			LogHelper.CONFIG_LOGGER.info("prop count <= 0");
			return;
		}

		if (type == AwardType.LORD_PROPERTY && id == LordPropertyType.VIP_LEVEL) {
			lordManager.setVipLevel(target.getLord(), count, Reason.GM_TOOL);
		} else {
			playerManager.addAward(target, type, id, count, Reason.GM_TOOL);
		}
	}

	public void greedIsGood(TaskJumpRq req) {
		String nick = req.getNick();
		if (nick == null || nick.isEmpty()) {
			LogHelper.CONFIG_LOGGER.info("nick is null or empty!");
			return;
		}

		Player player = playerManager.getPlayer(nick);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null");
			return;
		}

		Map<Integer, StaticBuilding> allBuilding = staticBuildingMgr.getBuildingMap();
		List<Integer> openBuildingId = new ArrayList<Integer>();
		for (StaticBuilding building : allBuilding.values()) {
			openBuildingId.add(building.getBuildingId());
		}

		taskManager.synBuildings(player, openBuildingId);
		player.getLord().setNewState(150);
		Map<Integer, Task> taskMap = player.getTaskMap();
		taskMap.clear();
		taskManager.addTask(200, taskMap);
		Building buildings = player.buildings;
		for (Integer buildingId : openBuildingId) {
			int buildingType = staticBuildingMgr.getBuildingType(buildingId);
			buildings.setLevel(buildingType, buildingId, 1);
		}
		player.getLord().setLevel(50);
		player.getLord().setVip(3);
		player.getLord().setGold(2000);
		player.addSystemGold(2000);
		Pos pos = player.getPos();
		if (pos.isError()) {
			worldManager.randerPlayerPos(player,true);
		}
		playerManager.checkStatus(player);
		// 给玩家添加所有的英雄
		Map<Integer, StaticHero> heroConfig = staticHeroMgr.getHeroMap();
		for (StaticHero staticHero : heroConfig.values()) {
			if (staticHero == null) {
				continue;
			}

			if (staticHero.getHeroId() >= 301 && staticHero.getHeroId() <= 315) {
				continue;
			}

			if (heroManager.hasHeroType(player, staticHero.getHeroId())) {
				continue;
			}
			heroManager.addHero(player, staticHero.getHeroId(), Reason.GM_TOOL);
		}

		player.setMaxMonsterLv(22);
		// 英雄等级设置为150
		// Map<Integer, Hero> heroMap = player.getHeros();
		// for (Hero hero : heroMap.values()) {
		// if (hero != null) {
		// hero.setHeroLv(150);
		// // 英雄装备都是紫色
		// Map<Integer, Equip> equips = player.getEquips();
		// for (int equipId = 31; equipId <= 36; equipId++) {
		// // 增加英雄身上装备
		// if (hero.hasEquip(equipId)) {
		// continue;
		// }
		// Equip equip = equipManager.addEquip(player, equipId, Reason.GM_TOOL);
		// HeroEquip heroEquip = new HeroEquip();
		// StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equipId);
		// if (staticEquip == null) {
		// continue;
		// }
		// heroEquip.setPos(staticEquip.getEquipType());
		// heroEquip.setEquip(equip.cloneInfo());
		// hero.addHeroEquip(heroEquip);
		// equips.remove(equip.getKeyId());
		// }
		// }
		// }
	}

	public boolean getPersonLogic(String marking, String callMethod, int serverId, long lordId) {
		Player player = playerManager.getPlayer(lordId);
		RecPersonRq.Builder builder = RecPersonRq.newBuilder();
		builder.setMarking(marking);
		builder.setServerId(serverId);
		builder.setCallMethod(callMethod);
		if (player == null) {
			builder.setLordId(0);
//			Base.Builder baseBuilder = PbHelper.createRqBase(RecPersonRq.EXT_FIELD_NUMBER, null, RecPersonRq.ext,
//					builder.build());
//			GameServer.getInstance().sendMsgToPublic(baseBuilder);
			return true;
		}
		// 资源
		Resource resource = player.getResource();
		CommonPb.Resource.Builder resourcePb = CommonPb.Resource.newBuilder();
		resourcePb.setIron(resource.getIron());
		resourcePb.setCopper(resource.getCopper());
		resourcePb.setOil(resource.getOil());
		resourcePb.setStone(resource.getStone());
		builder.setResource(resourcePb.build());

		// 武将
		Iterator<Hero> heros = player.getHeros().values().iterator();
		while (heros.hasNext()) {
			Hero hero = heros.next();
			CommonPb.SimpleHero.Builder heroPb = CommonPb.SimpleHero.newBuilder();
			heroPb.setHeroId(hero.getHeroId());
			heroPb.setHeroLv(hero.getHeroLv());
			heroPb.setQuality(staticHeroMgr.getQuality(hero.getHeroId()));
			// 武将身上的装备
			for (HeroEquip equip : hero.getHeroEquips()) {
				CommonPb.SimpleEquip.Builder equipPb = CommonPb.SimpleEquip.newBuilder();
				equipPb.setPos(equip.getPos());
				equipPb.setKeyId(equip.getEquip().getKeyId());
				equipPb.setEquipId(equip.getEquip().getEquipId());
				heroPb.addSimpleEquip(equipPb.build());
			}
			builder.addHero(heroPb.build());
		}

		// 道具
		Iterator<Item> items = player.getItemMap().values().iterator();
		while (items.hasNext()) {
			Item item = items.next();
			CommonPb.Prop.Builder itemPb = CommonPb.Prop.newBuilder();
			itemPb.setPropId(item.getItemId());
			itemPb.setPropNum(item.getItemNum());
			builder.addProp(itemPb.build());
		}

		// 装备
		Iterator<Equip> equips = player.getEquips().values().iterator();
		while (equips.hasNext()) {
			Equip equip = equips.next();
			CommonPb.Equip.Builder equipPb = CommonPb.Equip.newBuilder();
			equipPb.setEquipId(equip.getEquipId());
			equipPb.setKeyId(equip.getKeyId());
		}

		Base.Builder baseBuilder = PbHelper.createRqBase(RecPersonRq.EXT_FIELD_NUMBER, null, RecPersonRq.ext,
				builder.build());
//		GameServer.getInstance().sendMsgToPublic(baseBuilder);
		return true;
	}

	public boolean modVipLogic(long lordId, int type, int value) {
		Player player = playerManager.getPlayer(lordId);
		//System.out.println("lordId" + lordId + "type" + type + "value" + value);
		if (type == 1) {// 修改VIP
			if (player != null && value >= 0 && value <= 12) {
				player.getLord().setVip(value);
			}
		} else if (type == 2) {
			if (player != null && value >= 0) {
				player.getLord().setVipExp(value);
			}
		}
		return true;
	}

	public boolean forbiddenLogic(int forbiddenId, long lordId) {
		if (forbiddenId == 1) {// 禁言
			Player player = playerManager.getPlayer(lordId);
			if (player != null && player.account.getIsGm() == 0) {
				player.getLord().setSilence(1);
			}
		} else if (forbiddenId == 2) {// 解禁
			Player player = playerManager.getPlayer(lordId);
			if (player != null && player.account.getIsGm() == 0) {
				player.getLord().setSilence(0);
			}
		} else if (forbiddenId == 3) {// 封号
			Player player = playerManager.getPlayer(lordId);
			if (player != null && player.account.getIsGm() == 0) {
				player.account.setForbid(1);
				if (player.isLogin && player.account.getIsGm() == 0 && player.getChannelId() != -1) {
//					player.ctx.close();
				}
			}
		} else if (forbiddenId == 4) {// 解封
			Player player = playerManager.getPlayer(lordId);
			if (player != null && player.account.getIsGm() == 0) {
				player.account.setForbid(0);
			}
		} else if (forbiddenId == 5) {// 踢下线
			Player player = playerManager.getPlayer(lordId);
			if (player != null && player.account.getIsGm() == 0) {
				if (player.isLogin && player.account.getIsGm() == 0 && player.getChannelId() != -1) {
//					player.ctx.close();
				}
			}
		}
		return true;
	}

	/**
	 * 给服务器发放邮件
	 * 
	 * @param marking
	 * @param channelNo
	 * @param mailId
	 * @param paramList
	 * @param awardList
	 * @return
	 */
	public boolean gameMailLogic(String marking, int channelNo, int country, int mailId, List<String> paramList,
			List<CommonPb.Award> awardList) {
		String[] param = new String[paramList.size()];
		for (int i = 0; i < paramList.size(); i++) {
			param[i] = paramList.get(i);
		}

		Iterator<Player> it = playerManager.getPlayers().values().iterator();
		while (it.hasNext()) {
			Player player = (Player) it.next();
			if (player == null || player.account == null || !player.isActive() || player.getLord() == null) {
				continue;
			}
			if (country != 0 && player.getLord().getCountry() != country) {
				continue;
			}

			Date loginDate = player.account.getLoginDate();
			if (loginDate == null) {
				continue;
			}

			// 10天未登录,则不发放邮件
			if (TimeHelper.equation(loginDate.getTime(),System.currentTimeMillis()) >= 10) {
				continue;
			}

			if (channelNo == 0) {
				playerManager.sendAttachPbMail(player, awardList, mailId, param);
			}
		}

		return true;
	}

	/**
	 * 个人邮件
	 * 
	 * @param marking
	 * @param lordId
	 * @param mailId
	 * @param awardList
	 * @return
	 */
	public boolean personMailLogic(String marking, long lordId, int mailId, List<CommonPb.Award> awardList) {
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return false;
		}

		playerManager.sendAttachPbMail(player, awardList, mailId, new String[0]);
		return true;
	}

	/**
	 * gm回复建议
	 * 
	 * @param marking
	 * @param lordId
	 * @param mailId
	 * @param title
	 * @param content
	 * @return
	 */
	public boolean replyPersonMailLogic(String marking, long lordId, int mailId, String title, String content) {
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return false;
		}
		playerManager.sendNormalMail(player, mailId, title, content);
		return true;
	}

	/**
	 * gm回复
	 * 
	 * @param marking
	 * @param lordId
	 * @param awardList
	 * @param mailId
	 * @param title
	 * @param content
	 * @return
	 */
	public boolean replyPersonMailLogic(String marking, long lordId, List<CommonPb.Award> awardList, int mailId,
			String title, String content) {
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return false;
		}
		playerManager.sendAttachPbMail(player, awardList, mailId, title, content);
		return true;
	}

	public boolean sendNoticeLogic(String content) {
		chatManager.sendWorldChat(ChatId.GM_NOTICE, content);
		return true;
	}

	public boolean fakeRechargeLogic(long lordId, int amount, int giftId) {
//		Player player = playerManager.getPlayer(lordId);
//
//		if (player == null) {
//			return false;
//		}
//
//		Lord lord = player.getLord();
//		int oldVip = lord.getVip();
//
//		int topup = amount * 10;
//		if (topup <= 0) {
//			return false;
//		}
//
//		int platNo = player.account.getPlatNo();
//		int extraGold = staticVipDataMgr.getExtraGold(topup, platNo);
//		int firstPay = lord.getFirstPay();
//		// 充值额度和vip点数
//		lord.setTopup(lord.getTopup() + amount);
//		playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.VIP_EXP, topup, Reason.FAKE_PAY);
//
//		if (giftId != 0) {// 充值礼包
//			boolean flag = activityManager.actPayGift(player, giftId);
//			if (!flag) {// 礼包充值失败,则正常发货
//				lord.setGold(lord.getGold() + topup + extraGold);
//				lord.setGoldGive(lord.getGoldGive() + topup + extraGold);
//				lord.setTopup(lord.getTopup() + amount);
//			}
//		} else {// 正常充值
//			lord.setGold(lord.getGold() + topup + extraGold);
//			lord.setGoldGive(lord.getGoldGive() + topup + extraGold);
//			lord.setTopup(lord.getTopup() + amount);
//			playerManager.sendNormalMail(player, MailId.RECHARGE_MAIL, String.valueOf(topup));
//		}
//
//		if (lord.getVip() > oldVip) {// 世界分享
//			try {
//				chatManager.updateChatShow(ChatShowType.VIP_LEVEL, lord.getVip(), player);
//			} catch (Exception ex) {
//				LogHelper.CONFIG_LOGGER.info(ex.getMessage());
//			}
//		}
//
//		if (firstPay == 0) {
//			player.getLord().setFirstPay(1);
//			player.getLord().setTvip(3);
//		}
//
//		activityManager.updActPerson(player, ActivityConst.ACT_TOPUP_RANK, topup, 0);
//		activityManager.updActPerson(player, ActivityConst.ACT_PAY_FIRST, amount, 0);
//		activityManager.updActPerson(player, ActivityConst.ACT_DAY_PAY, topup, 0);
//		activityManager.actPayEveryDay(player, topup);
//		activityManager.updActPerson(player, ActivityConst.ACT_TOPUP_PERSON, topup, 0);
//		activityManager.updActServerReMoney(player, ActivityConst.ACT_SER_PAY, topup, 0);
//		activityManager.updActServer(ActivityConst.ACT_TOPUP_SERVER, topup, 0);
//		activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.TOPUP, 0, topup);

		return true;
	}

	/**
	 * 在线人数
	 * 

	 * @return
//	 */
	public boolean getOnlinesLogic(String making, int serverId, String callMthod) {
		RecOnlinesRq.Builder builder = RecOnlinesRq.newBuilder();
		builder.setMarking(making);
		builder.setServerId(serverId);
		int online = playerManager.getOnlinePlayer().size();
		builder.setCount(online);
		builder.setCallMethod(callMthod);
		Base.Builder baseBuilder = PbHelper.createRqBase(RecOnlinesRq.EXT_FIELD_NUMBER, null, RecOnlinesRq.ext,
				builder.build());
//		GameServer.getInstance().sendMsgToPublic(baseBuilder);
		return true;
	}

}
