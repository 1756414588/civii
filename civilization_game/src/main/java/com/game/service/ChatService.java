package com.game.service;

import com.game.chat.domain.Chat;
import com.game.chat.domain.ManChat;
import com.game.chat.domain.ManShare;
import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMeetingTaskMgr;
import com.game.domain.CountryData;
import com.game.domain.PersonChat;
import com.game.domain.PersonChatRoom;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticChat;
import com.game.domain.s.StaticMail;
import com.game.log.LogUser;
import com.game.log.domain.ChatLog;
import com.game.log.domain.PersonalSignatureLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.ChatPb;
import com.game.pb.ChatPb.*;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.HeroInfo;
import com.game.pb.CommonPb.SeeProperty;
import com.game.server.GameServer;
import com.game.util.EmojiHelper;
import com.game.util.EmojiUtil;
import com.game.util.PbHelper;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.game.worldmap.Pos;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.game.dataMgr.StaticSensitiveWordMgr;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private MailManager mailManager;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private ItemManager itemManager;

	@Autowired
	private CityManager cityManager;

	@Autowired
	private SuggestManager suggestManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private CountryManager countryManager;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private StaticMeetingTaskMgr staticMeetingTaskMgr;

	@Autowired
	private TechManager techManager;

	@Autowired
	private CountryManager ctManger;

	@Autowired
	private KillEquipManager killEquipManager;
	@Autowired
	private WarBookManager warBookManager;
	@Autowired
	private BattleMgr battleMgr;
	@Autowired
	private BattleMailManager battleMailManager;
	@Autowired
	private StaticSensitiveWordMgr staticSensitiveWordMgr;

	/**
	 * 获取聊天记录
	 *
	 * @param handler
	 */
	public void getChat(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			return;
		}
		GetChatRs.Builder builder = GetChatRs.newBuilder();
		// 全服
		List<CommonPb.Chat> list = chatManager.getLimitWorld();
		for (CommonPb.Chat e : list) {
			if (e.hasTime()) {
				builder.addChat(e);
			}
		}
		// 全国
		List<Long> blackList = player.getBlackList();
		ConcurrentLinkedDeque<CommonPb.Chat> countryList = chatManager.getCountryChat(player.getCountry());
		if (countryList != null) {
			for (CommonPb.Chat e : countryList) {
				if (blackList.contains(e.getLordId())) {
					continue;
				}

				if (e.getCountry() != player.getCountry()) {
					continue;
				}

				if (e.hasTime()) {
					builder.addChat(e);
				}
			}
		}

		// 区域发送
		int mapId = worldManager.getMapId(player);
		if (mapId != 0) {
			ConcurrentLinkedDeque<CommonPb.Chat> mapChatList = chatManager.getMapChat(mapId);
			if (mapChatList != null) {
				for (CommonPb.Chat e : mapChatList) {
					if (e == null) {
						continue;
					}
					if (blackList.contains(e.getLordId())) {
						continue;
					}

					// %s国%s对我国的%s%s发起了国战。敌国来犯，我城势单力孤，还请诸位同胞申出援手 ok [区域, finshed,测试ok]
					if (e.getChatId() == ChatId.COUNTRY_ATTACK) {
						List<String> paramList = e.getParamList();
						if (paramList != null && !paramList.isEmpty()) {
							String callerCountry = String.valueOf(paramList.get(0));
							if (callerCountry == null) {
								continue;
							}

							// 别的国家打我的国家
							if (callerCountry.equals(String.valueOf(player.getCountry()))) {
								continue;
							}

							// 不是我国的城池也不要发
							if (paramList.size() >= 4) {
								Integer cityId = Integer.valueOf(paramList.get(3));
								if (cityId == null) {
									continue;
								}

								int country = cityManager.getCityCoutry(cityId);
								if (country != player.getCountry()) {
									continue;
								}
							}
						}
					} else if (e.getChatId() == ChatId.ATTACK_COUNTRY) { // 我国的%s对%s%s%s发起了国战。养兵千日，用兵一时，一鸣惊人正在今朝，请给位主公加入 ok [区域,测试1ok，测试2ok]
						List<String> paramList = e.getParamList();
						if (paramList != null && !paramList.isEmpty()) {
							String nick = paramList.get(0);
							int country = 0;
							if (nick != null) {
								Player target = playerManager.getPlayer(nick);
								if (target != null) {
									country = target.getCountry();
								}
							}

							if (country != player.getCountry()) {
								continue;
							}
						}
					} else if (e.getChatId() == ChatId.HELP_REBLE_WAR || e.getChatId() == ChatId.SHARE_REBLE_WAR || e.getChatId() == ChatId.RIOT_DEFENCE || e.getChatId() == ChatId.CITY_ATTACK || e.getChatId() == ChatId.ATTACK_CITY) {
						if (e.getCountry() != player.getCountry()) {
							continue;
						}
					}

					if (e.hasTime()) {
						builder.addChat(e);
					}

				}
			}
		}

		// VIP频道
		ConcurrentLinkedDeque<CommonPb.Chat> vipChatList = chatManager.getVipChat();
		for (CommonPb.Chat e : vipChatList) {
			if (e.hasTime()) {
				builder.addChat(e);
			}
		}

		// 按照时间将聊天排序
		List<CommonPb.Chat> chatList = new LinkedList<>(builder.getChatList());
		builder.clearChat().addAllChat(chatList);
		builder.addAllParams(player.getRecordList());
		handler.sendMsgToPlayer(GetChatRs.ext, builder.build());
	}

	/**
	 * 聊天
	 *
	 * @param req
	 * @param handler
	 */
	public void doChat(DoChatRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			// LogHelper.ERROR_LOGGER.error("dochat nul!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
			// + handler.getRoleId());
			return;
		}

		boolean gmFlag = mijiService.mijiResult(req.getMsg(), player);
		// gm命令不发聊天中
		if (gmFlag) {
			DoChatRs.Builder builder = DoChatRs.newBuilder();
			handler.sendMsgToPlayer(DoChatRs.ext, builder.build());
			return;
		}

		int vip = player.getLord().getVip();
		int minVip = staticLimitMgr.getNum(242);// 开启聊天的最低VIP等级
		if (player.getLord().getOpenSpeak() != 1) {
			if (vip >= minVip) {
				player.getLord().setOpenSpeak(1);
			} else {
				if (player.getLevel() < getChatLimitLevel(SimpleId.CHAT_LEVEL)) {
					handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
					return;
				}
				player.getLord().setOpenSpeak(1);
			}
		}

		// 禁言时间
		if (player.account.getCloseSpeakTime() > System.currentTimeMillis()) {
			handler.sendErrorMsgToPlayer(GameError.CHAT_SILENCE);
			return;
		}

		int now = TimeHelper.getCurrentSecond();
		if (now - player.chatTime < 5) {
			handler.sendErrorMsgToPlayer(GameError.CHAT_CD);
			return;
		}

		String msg = req.getMsg();

		//替换敏感词
		if (req.getStyle() > 0) {
			// 区域聊天
			msg = staticSensitiveWordMgr.replaceSensitiveWord(msg, "areaChatFilter");
		} else {
			// 阵营聊天
			msg = staticSensitiveWordMgr.replaceSensitiveWord(msg, "countryChatFilter");
		}

		if (msg.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.INVALID_PARAM);
			return;
		}

		if (msg.length() > 40) {
			handler.sendErrorMsgToPlayer(GameError.MAX_CHAT_LENTH);
			return;
		}

		msg = EmojiHelper.filterEmoji(msg);
		int officerId = countryManager.getOfficeId(player);
		ManChat chat = chatManager.createManChat(player, msg);
		chat.setStyle(req.getStyle());
		chat.setChatType(req.getRegion() ? 1 : 0);
		DoChatRs.Builder builder = DoChatRs.newBuilder();
		if (req.getStyle() > 0) {
			sendWorldChat(chat, req.getStyle(), officerId);
			Item item = itemManager.getItem(player, 39);
			if (item != null && item.getItemNum() > 0) {
				item = itemManager.subItem(player, item.getItemId(), 1, Reason.CHAT);
				builder.setProp(PbHelper.createItemPb(item.getItemId(), item.getItemNum()));
			} else if (player.getGold() >= 500) {
				playerManager.subGoldOk(player, 500, Reason.CHAT);
			} else {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
		} else {
			builder.setGold(player.getGold());
			if (req.getIsPaiPai() == 1) {
				Long targetId = Long.parseLong(msg);
				Player target = playerManager.getPlayer(targetId);
				if (target == null) {
					handler.sendErrorMsgToPlayer(GameError.INVALID_PARAM);
					return;
				}
				chatManager.sendCountryChat(player.getCountry(), ChatId.PAI_PAI, player.getLord().getNick(), target.getLord().getNick());
				// player.chatTime = now;
				return;
			}

			if (gmFlag) {
				sendCountryChat(player.getCountry(), officerId, chat, player.getLord().getLordId());
			} else {
				sendCountryChat(player.getCountry(), officerId, chat, 0);
			}
		}
		/**
		 * 聊天日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.chatLog(new ChatLog(player.account.getServerId(), player.roleId, player.getLord().getNick(), player.getLevel(), player.getVip(), player.account.getIsGm(), player.account.getChannel(), player.account.getAccountKey(), player.getCountry(), req.getStyle(), msg));

		player.chatTime = now;

		handler.sendMsgToPlayer(DoChatRs.ext, builder.build());
		taskManager.doTask(TaskType.SAY_A_WORD, player, null);
		return;
	}

	@Autowired
	private MijiService mijiService;
	@Autowired
	private ServerManager serverManager;

	/**
	 * 拿到一个玩家限制聊天的等级
	 *
	 * @return
	 */
	public int getChatLimitLevel(int simpId) {
		List<Integer> addtions = staticLimitMgr.getAddtion(simpId);
		int day = TimeHelper.passNowDay(serverManager.getServer().getOpenTime().getTime());
		if (day - 1 <= 0) {
			return addtions.get(0);
		} else if (day - 1 >= 7) {
			return addtions.get(6);
		} else {
			return addtions.get(day - 1);
		}
	}

	/**
	 * 分享邮件
	 *
	 * @param req
	 * @param handler
	 */
	public void shareMailRq(ShareMailRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		// 判断聊天等级是否达到
		int vip = player.getLord().getVip();
		int minVip = staticLimitMgr.getNum(242);// 开启聊天的最低VIP等级
		if (player.getLord().getOpenSpeak() != 1) {
			if (vip >= minVip) {
				player.getLord().setOpenSpeak(1);
			} else {
				if (player.getLevel() < getChatLimitLevel(SimpleId.CHAT_LEVEL)) {
					handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
					return;
				}
				player.getLord().setOpenSpeak(1);
			}
		}
		// 判断聊天是否频繁
		Lord lord = player.getLord();
		int now = TimeHelper.getCurrentSecond();
		if (now - player.chatTime < 5) {
			handler.sendErrorMsgToPlayer(GameError.CHAT_CD);
			return;
		}
		// 判断分享邮件是否存在
		int keyId = req.getMailKeyId();
		Mail mail = player.getMail(keyId);
		if (mail == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_EXIST_MAIL);
			return;
		}
		// 检查配置开启分享
		StaticMail staticMail = mailManager.isShare(mail.getMailId());
		if (staticMail == null || staticMail.getShare() == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_SHARE);
			return;
		}
		// 获取分享参数
		String[] param = new String[staticMail.getShareIndex().size()];
		for (int i = 0; i < param.length; i++) {
			int index = staticMail.getShareIndex().get(i);
			if (index == -1) {
				param[i] = player.getNick();
			} else {
				if (mail.getParam() == null || index >= mail.getParam().length) {
					handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
					return;
				}
				param[i] = mail.getParam()[index];
			}
		}

		int day = GameServer.getInstance().currentDay;
		if (day != lord.getMailShareDay()) {
			lord.setMailShareDay(day);
			lord.setMailTimes(0);
		} else {
			lord.setMailTimes(lord.getMailTimes());
		}
		if (lord.getMailTimes() >= 3 && lord.getGold() < 10) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		if (lord.getMailTimes() < 3) {
			lord.setMailTimes(lord.getMailTimes() + 1);
		} else {
			playerManager.subGoldOk(player, 10, Reason.SHARE_MAIL);
		}

		ManShare chat = chatManager.createManShare(player, staticMail.getShare(), mail.getKeyId(), param);
		int officerId = countryManager.getOfficeId(player);// 获取军衔id
		sendCountryChat(player.getLord().getCountry(), officerId, chat, 0);

		player.chatTime = now;
		ShareMailRs.Builder builder = ShareMailRs.newBuilder();
		builder.setShareMailTimes(lord.getMailTimes());
		builder.setGold(lord.getGold());
		handler.sendMsgToPlayer(ShareMailRs.ext, builder.build());
		return;
	}

	/**
	 * 查看玩家信息
	 *
	 * @param req
	 * @param handler
	 */
	public void seeManRq(SeeManRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		long targetId = req.getLordId();

		Player target = playerManager.getPlayer(targetId);
		if (target == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (!target.isLogin) {
			heroManager.caculateBattleScore(target);
		}
		Lord lord = target.getLord();
		SeeManRs.Builder builder = SeeManRs.newBuilder();
		// 个性签名
		String personalSignature = target.getPersonalSignature();
		builder.setPersonalSignature(personalSignature.isEmpty() ? PersonalSignature.PERSONALSIGNATURE : personalSignature);
		// 聊天框
		builder.setPersonalFrame(lord.getChatIndex());

		builder.setLordId(lord.getLordId());
		builder.setNick(lord.getNick());
		builder.setLevel(lord.getLevel());
		builder.setPortrait(lord.getPortrait());
		builder.setCountry(lord.getCountry());
		builder.setCommandLv(target.getCommand().getLv());
		builder.setTitle(lord.getTitle());
		builder.setHonor(lord.getHonor());
		builder.setMapId(playerManager.getMapId(target));
		builder.setOffice(countryManager.getOfficeId(target));
		builder.setCaculateScorce(lord.getAllScore());
		builder.setClothes(lord.getClothes());
		City cityOwn = cityManager.getCity(target.getCityId());
		if (cityOwn != null && cityOwn.getLordId() == lord.getLordId()) {
			builder.setCityId(target.getCityId());
		} else {
			builder.setCityId(0);
		}

		/**
		 * 我方上阵英雄信息
		 */
		List<Integer> embattleListMy = player.getEmbattleList();

		/**
		 * 对方上阵英雄信息
		 */
		List<CommonPb.HeroInfo> heroInfoList = new ArrayList<CommonPb.HeroInfo>();
		List<Integer> embattleListTarget = target.getEmbattleList();
		if (!embattleListTarget.isEmpty()) {
			for (Integer integer : embattleListTarget) {
				Hero hero = target.getHeros().get(integer);
				if (null != hero) {
					CommonPb.HeroInfo.Builder heroInfo = HeroInfo.newBuilder();
					heroInfo.setHeroId(integer);
					heroInfo.setLv(hero.getHeroLv());
					heroInfo.setDivNum(hero.getDiviNum());
					heroInfoList.add(heroInfo.build());
				}
			}
		}

		List<CommonPb.SeeProperty> SeeProList = new ArrayList<CommonPb.SeeProperty>();
		// 我方战斗力信息
		CommonPb.SeeProperty.Builder seeProMy = caculateScorce(player, embattleListMy);
		// 对方战斗力信息
		CommonPb.SeeProperty.Builder seeProTar = caculateScorce(target, embattleListTarget);

		SeeProList.add(seeProMy.build());
		SeeProList.add(seeProTar.build());

		builder.addAllHeroInfo(heroInfoList);
		builder.addAllSeeProperty(SeeProList);
		handler.sendMsgToPlayer(SeeManRs.ext, builder.build());

		return;
	}

	// 计算战斗力
	private CommonPb.SeeProperty.Builder caculateScorce(Player player, List<Integer> embattleList) {
		CommonPb.SeeProperty.Builder seePro = SeeProperty.newBuilder();
		int basecaBattleScore = 0;
		int equipBattleScore = 0;
		int ctBattleScore = 0;
		int titleBattleScore = 0;
		int tecBattleScore = 0;
		int omamentScore = 0;
		int bookScore = 0;
		int beautyScore = 0;

		if (embattleList.size() > 0) {
			for (Integer heroId : embattleList) {
				Hero hero = player.getHeros().get(heroId);
				if (null != hero) {
					// 基础信息
					Property baseAdd = heroManager.getBaseProperty(hero);
					int soldierLines = playerManager.getSoldierLine(player) + staticMeetingTaskMgr.soldierNumByHero(player, heroId);
					int baseAddSoldier = (int) ((float) (baseAdd.getSoldierNum()) / 4.0f * (float) soldierLines);
					baseAdd.setSoldierNum(baseAddSoldier);
					// 资质
					Property qulifyAdd = heroManager.getQulifyProperty(hero, soldierLines);
					// 神级突破
					Property special = hero.getSpecialProp();
					// 英雄战斗力计算：基础属性+等级资质+传奇突破
					basecaBattleScore += heroManager.caculateBattleScore(baseAdd) + heroManager.caculateBattleScore(qulifyAdd) + heroManager.caculateBattleScore(special);

					// 装备属性, 直接加
					Property equipProperty = heroManager.getEquipProperty(hero);
					int equipPropertySoldier = (int) ((float) (equipProperty.getSoldierNum()) / 4.0f * (float) soldierLines);
					equipProperty.setSoldierNum(equipPropertySoldier);
					equipBattleScore += heroManager.caculateBattleScore(equipProperty);

					// 国器
					Property killEquipProperty = killEquipManager.getAllProperty(player);
					int killEquipPropertySoldier = (int) ((float) (killEquipProperty.getSoldierNum()) / 4.0f * (float) soldierLines);
					killEquipProperty.setSoldierNum(killEquipPropertySoldier);
					ctBattleScore += heroManager.caculateBattleScore(killEquipProperty);

					// 科技加攻击
					Property heroProperty = techManager.getHeroProperty(player, heroManager.getSoldierType(heroId), new Property());
					tecBattleScore += heroManager.caculateBattleScore(heroProperty);

					// 兵书加成
					Property warBookProperty = heroManager.getWarBookProperty(hero);
					int warBookPropertySoldier = (int) ((float) (warBookProperty.getSoldierNum()) / 4.0f * (float) soldierLines);
					warBookProperty.setSoldierNum(warBookPropertySoldier);
					bookScore += (heroManager.caculateBattleScore(warBookProperty));
					BattleProperty bookBattleProperty = warBookManager.getBookBattleProperty(hero);
					double critiFactor = (double) staticLimitMgr.getNum(272) / DevideFactor.PERCENT_NUM;
					double missFactor = (double) staticLimitMgr.getNum(273) / DevideFactor.PERCENT_NUM;
					bookScore += bookBattleProperty.getCriti() / 10 * critiFactor + bookBattleProperty.getMiss() / 10 * missFactor;

					// 配饰
					if (player.getEmbattleList().contains(hero.getHeroId())) {
						Property ommentProperty = heroManager.getOmmentProperty(player);
						int ommentPropertySoldier = (int) ((float) (ommentProperty.getSoldierNum()) / 4.0f * (float) soldierLines);
						ommentProperty.setSoldierNum(ommentPropertySoldier);
						omamentScore += heroManager.caculateBattleScore(ommentProperty);
					}

					// 爵位
					Property titleAttack = ctManger.getTitleAttack(player);
					int i = (int) ((float) (titleAttack.getSoldierNum()) / 4.0f * (float) soldierLines);
					titleAttack.setSoldierNum(i);
					titleBattleScore += heroManager.caculateBattleScore(titleAttack);

					// 美女
					Property property = new Property();
					int beautyAddition = heroManager.getBeautyAddition(hero, player, qulifyAdd, property);
					int baseSo = (int) Math.floor(baseAdd.getSoldierNum() * beautyAddition / DevideFactor.PERCENT_NUM);
					int v = (int) Math.floor(qulifyAdd.getSoldierNum() * beautyAddition / DevideFactor.PERCENT_NUM);
					property.addSoldierNumValue(baseSo);
					property.addSoldierNumValue(v);
					beautyScore += heroManager.caculateBattleScore(property);

				}
			}
		}
		seePro.setLordId(player.getLord().getLordId());
		seePro.setBasePro(basecaBattleScore);
		seePro.setEquipPro(equipBattleScore);
		seePro.setCtPro(ctBattleScore);
		seePro.setTitlePro(titleBattleScore);
		seePro.setTecPro(tecBattleScore);
		seePro.setBuildingPro(player.getBuildingScore());
		seePro.setOmamentPro(omamentScore);
		seePro.setBookPro(bookScore);
		seePro.setBeayty(beautyScore);
		return seePro;
	}

	/**
	 * 游戏内部提建议
	 *
	 * @param req
	 * @param handler
	 */
	public void suggestRq(SuggestRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		String content = req.getContent();
		if (content == null || content.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		// 长度不可超过250
		if (content.length() > 250) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		SuggestRs.Builder builder = SuggestRs.newBuilder();

		// 建议
		if (!suggestManager.addSuggest(player, content)) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_COUNT);
			return;
		}

		handler.sendMsgToPlayer(SuggestRs.ext, builder.build());
		return;
	}

	public void sendChat(Chat chat, StaticChat staticChat) {
		CommonPb.Chat b = chatManager.addWorldChat(chat);

		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);

		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());

		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next.isLogin && !next.getBlackList().contains(b.getLordId()) && staticChat != null && next.getLevel() >= staticChat.getLimitLevel()) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
			}
		}
	}

	public void sendWorldChat(Chat chat, int style, int officerId) {
		CommonPb.Chat b = chat.ser(style, officerId);

		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);
		chatManager.getWorld().add(b);

		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());

		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next.isLogin && !next.getBlackList().contains(b.getLordId())) {
//				ctx = next.ctx;
//				if (ctx != null) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
//				}
			}
		}
	}

	// vip频道
	public void sendVipChat(Chat chat, StaticChat staticChat) {
		CommonPb.Chat b = chatManager.addVipChat(chat);
		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);
		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());
		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next.isLogin && !next.getBlackList().contains(b.getLordId()) && next.getLevel() >= staticChat.getLimitLevel()) {
//				ctx = next.ctx;
//				if (ctx != null) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
//				}
			}
		}
	}

	/**
	 * @param country
	 * @param officerId
	 * @param chat
	 * @param playerId  为0 的时候表示全区域都发 不为0 给当前playerId 发
	 */
	private void sendCountryChat(int country, int officerId, Chat chat, long playerId) {

		CommonPb.Chat b = chatManager.addCountryChat(country, officerId, chat, playerId);

		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);
		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());

		if (playerId != 0) {
			Player player = playerManager.getPlayers().get(playerId);
			if (player != null) {
				if (player.isLogin && !player.getBlackList().contains(b.getLordId())) {
//					ChannelHandlerContext ctx;
//					ctx = player.ctx;
					if (player != null) {
						GameServer.getInstance().sendMsgToPlayer(player, msg);
					}
				}
			}
			return;
		}

		// 全区广播
		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next.getLord().getCountry() != country) {
				continue;
			}
			if (next.isLogin && !next.getBlackList().contains(b.getLordId())) {
//				ctx = next.ctx;
//				if (ctx != null) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
//				}
			}
		}
	}

	/**
	 * 分享坐标
	 *
	 * @param req
	 * @param handler
	 */
	public void shareChatRq(ShareChatRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int vip = player.getLord().getVip();
		int minVip = staticLimitMgr.getNum(242);// 开启聊天的最低VIP等级
		if (player.getLord().getOpenSpeak() != 1) {
			if (vip >= minVip) {
				player.getLord().setOpenSpeak(1);
			} else {
				if (player.getLevel() < getChatLimitLevel(SimpleId.CHAT_LEVEL)) {
					handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
					return;
				}
				player.getLord().setOpenSpeak(1);
			}
		}

		int now = TimeHelper.getCurrentSecond();
		if (now - player.chatTime < 5) {
			handler.sendErrorMsgToPlayer(GameError.CHAT_CD);
			return;
		}

		int chatId = req.getId();

		String[] param = req.getParamsList().toArray(new String[req.getParamsList().size()]);

		ManShare chat = chatManager.createManShare(player, chatId, param);

		int governId = 0;
		CountryData country = countryManager.getCountry(player.getLord().getCountry());
		if (null != country) {
			CtyGovern king = country.getCtyGovernOffer(player.getLord().getLordId());
			if (null != king) {
				governId = king.getGovernId();
			}
		}
		sendCountryChat(player.getLord().getCountry(), governId, chat, 0);

		player.chatTime = now;
		ShareChatRs.Builder builder = ShareChatRs.newBuilder();
		handler.sendMsgToPlayer(ShareChatRs.ext, builder.build());
		return;
	}

	/**
	 * @Description 修改个性签名
	 * @Date 2021/1/25 15:39
	 * @Param [handler]
	 * @Return
	 **/
	public void updateSignatureRq(UpdateSignatureRq rq, ClientHandler handler) {
		String personalSignature = rq.getPersonalSignature();
		personalSignature = EmojiUtil.emojiChange(personalSignature);

		// 判断个性签名是否为空
		if (personalSignature.replaceAll(" ", "").length() < 1) {
			personalSignature = "";
		}

		// 判断个性签名是否超过了30个字
		if (personalSignature.length() > 30) {
			handler.sendErrorMsgToPlayer(GameError.SIGNATURE_MORE_THAN_30_WORDS);
			return;
		}

		if (staticSensitiveWordMgr.containSensitiveWord(personalSignature, "privateChatFilter")) {
			handler.sendErrorMsgToPlayer(GameError.SENSITIVE_WORD);
			return;
		}

		Player player = playerManager.getPlayer(handler.getRoleId());

		// 判断个性签名是否为做修改
		String oldPersonalSignature = player.getPersonalSignature();
		if (personalSignature.equals(oldPersonalSignature)) {
			handler.sendErrorMsgToPlayer(GameError.SIGNATURE_SAME_AS_THE_OLD_ONE);
			return;
		}

		// 更新个性签名
		player.setPersonalSignature(personalSignature);

		// 个性签名埋点
		SpringUtil.getBean(LogUser.class).personalSignatureLog(new PersonalSignatureLog(player.account.getAccountKey(), player.account.getServerId(), player.getLord().getLordId(), player.getLord().getLevel(), personalSignature, player.getCountry(), player.getVip(), player.account.getChannel()));

		UpdateSignatureRs.Builder builder = UpdateSignatureRs.newBuilder();
		// 0:修改成功 1:修改失败
		builder.setState(0);
		handler.sendMsgToPlayer(UpdateSignatureRs.ext, builder.build());
	}

	/**
	 * 发送私人聊天
	 *
	 * @param req
	 * @param handler
	 */
	public void doPersonChatRq(DoPersonChatRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 禁言时间
		if (player.account.getCloseSpeakTime() > System.currentTimeMillis()) {
			handler.sendErrorMsgToPlayer(GameError.CHAT_SILENCE);
			return;
		}

		long lordId = req.getLordId();
		Player target = playerManager.getPlayer(lordId);
		if (null == target) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		String msg = req.getMsg();
		if (null == msg || msg.equals("")) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 直接替换敏感词
		msg = staticSensitiveWordMgr.replaceSensitiveWord(msg, "privateChatFilter");

		if (player.getLevel() < getChatLimitLevel(SimpleId.PER_CHAT_LEVEL)) {
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}
		// 判断聊天等级是否达到
		int vip = player.getLord().getVip();
		int minVip = staticLimitMgr.getNum(242);// 开启聊天的最低VIP等级
		if (player.getLord().getOpenSpeak() != 1) {
			if (vip >= minVip) {
				player.getLord().setOpenSpeak(1);
			} else {
				if (player.getLevel() < getChatLimitLevel(SimpleId.CHAT_LEVEL)) {
					handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
					return;
				}
				player.getLord().setOpenSpeak(1);
			}
		}
		// 判断聊天是否频繁
		int now = TimeHelper.getCurrentSecond();
		if (now - player.chatTime < 5) {
			handler.sendErrorMsgToPlayer(GameError.CHAT_CD);
			return;
		}
		if (target.getBlackList().contains(handler.getRoleId())) {
			handler.sendErrorMsgToPlayer(GameError.BLACK);
			return;
		}
		if (player.getCountry() != target.getCountry()) {
			if (player.getGold() < 2) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, 2, Reason.SEND_MAIL);
		}

		ConcurrentHashMap<Long, PersonChatRoom> personChatRoom = player.getPersonChatRoom();
		ConcurrentHashMap<Long, PersonChatRoom> tarPersonChatRoom = target.getPersonChatRoom();

		PersonChatRoom chatRoom = null;
		PersonChatRoom tarChatRoom = null;
		if (personChatRoom.size() == 0) {
			chatRoom = new PersonChatRoom();
			chatRoom.setRoomId(player.maxKey());
			chatRoom.setLordId(player.getLord().getLordId());
			chatRoom.setReplayLordId(target.getLord().getLordId());
			personChatRoom.put(chatRoom.getRoomId(), chatRoom);
		} else {
			Iterator<PersonChatRoom> iterator = personChatRoom.values().iterator();
			while (iterator.hasNext()) {
				PersonChatRoom next = iterator.next();
				if (next.getReplayLordId() == target.getLord().getLordId()) {
					chatRoom = next;
					break;
				}
			}
		}

		if (null == chatRoom) {
			chatRoom = new PersonChatRoom();
			chatRoom.setRoomId(player.maxKey());
			chatRoom.setLordId(player.getLord().getLordId());
			chatRoom.setReplayLordId(target.getLord().getLordId());
			personChatRoom.put(chatRoom.getRoomId(), chatRoom);
		}

		PersonChat personChat = PersonChat.builder().lordId(player.roleId).createTime(now).roomId(chatRoom.getRoomId()).msg(msg).build();
		personChat.setState(MailConst.READ);
		List<PersonChat> chats1 = chatRoom.getChats();
		chats1.add(personChat);
		if (chats1.size() > 20) {
			chats1.remove(0);
		}

		if (tarPersonChatRoom.size() == 0) {
			tarChatRoom = new PersonChatRoom();
			tarChatRoom.setRoomId(target.maxKey());
			tarChatRoom.setLordId(target.getLord().getLordId());
			tarChatRoom.setReplayLordId(player.getLord().getLordId());
			tarChatRoom.setReplayRoomId(chatRoom.getRoomId());
			chatRoom.setReplayRoomId(tarChatRoom.getRoomId());
			tarPersonChatRoom.put(tarChatRoom.getRoomId(), tarChatRoom);
		} else {
			Iterator<PersonChatRoom> iterator = tarPersonChatRoom.values().iterator();
			while (iterator.hasNext()) {
				PersonChatRoom next = iterator.next();
				if (next.getReplayLordId() == player.getLord().getLordId()) {
					tarChatRoom = next;
					break;
				}
			}
		}

		if (null == tarChatRoom) {
			tarChatRoom = new PersonChatRoom();
			tarChatRoom.setRoomId(target.maxKey());
			tarChatRoom.setLordId(target.getLord().getLordId());
			tarChatRoom.setReplayLordId(player.getLord().getLordId());
			tarChatRoom.setReplayRoomId(chatRoom.getRoomId());
			chatRoom.setReplayRoomId(tarChatRoom.getRoomId());
			tarPersonChatRoom.put(tarChatRoom.getRoomId(), tarChatRoom);
		}

		PersonChat toPersonChat = PersonChat.builder().lordId(player.roleId).createTime(now).roomId(tarChatRoom.getRoomId()).msg(msg).build();
		tarChatRoom.getChats().add(toPersonChat);
		if (tarChatRoom.getChats().size() > 20) {
			tarChatRoom.getChats().remove(0);
		}
		playerManager.synPersonChatToPlayer(target, toPersonChat);

		ChatPb.DoPersonChatRs.Builder builder = ChatPb.DoPersonChatRs.newBuilder();
		CommonPb.PersonChat.Builder chatPb = toPersonChat.serShow();
		if (target != null) {
			chatPb.setOtherHeadSculpture(target.getLord().getHeadIndex());
		}
		builder.setChat(chatPb);
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(DoPersonChatRs.ext, builder.build());
	}

	public void getPersonChatRoomRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ChatPb.GetPersonChatRoomRs.Builder builder = ChatPb.GetPersonChatRoomRs.newBuilder();
		ConcurrentHashMap<Long, PersonChatRoom> personChatRooms = player.getPersonChatRoom();
		if (personChatRooms.size() != 0) {
			Iterator<PersonChatRoom> iterator = personChatRooms.values().iterator();
			while (iterator.hasNext()) {
				PersonChatRoom next = iterator.next();
				if (next != null) {
					CommonPb.PersonChatRoom.Builder personChatRoom = CommonPb.PersonChatRoom.newBuilder();
					personChatRoom.setRoomId(next.getRoomId());
					personChatRoom.setReplayRoomId(next.getReplayRoomId());
					personChatRoom.setLordId(next.getLordId());
					personChatRoom.setReplayLordId(next.getReplayLordId());

					Player managerPlayer = playerManager.getPlayer(next.getReplayLordId());
					if (managerPlayer == null) {
						continue;
					}
					personChatRoom.setPortrait(managerPlayer.getLord().getPortrait());
					personChatRoom.setCountry(managerPlayer.getLord().getCountry());
					personChatRoom.setLevle(managerPlayer.getLord().getLevel());
					personChatRoom.setNick(managerPlayer.getLord().getNick());
					personChatRoom.setHeadSculpture(managerPlayer.getLord().getHeadIndex());

					List<PersonChat> chats = next.getChats();
					int count = 0;
					for (PersonChat chat : chats) {
						if (chat.getState() == MailConst.UN_READ) {
							count++;
							personChatRoom.setNotRead(count);
						}
					}
					List<PersonChat> chatList = next.getChats();

					if (chatList.size() > 0) {
						PersonChat personChat = chatList.get(chatList.size() - 1);
						CommonPb.PersonChat.Builder chatPb = personChat.serShow();
						Player target = playerManager.getPlayer(next.getReplayLordId());
						if (target != null) {
							chatPb.setOtherHeadSculpture(target.getLord().getHeadIndex());
						}
						personChatRoom.setLastChat(chatPb);
						if (chatList.size() > 20) {
							chatList.remove(chatList.size() - 1);
						}
					}
					builder.addChatRoom(personChatRoom);
				}
			}
		}
		handler.sendMsgToPlayer(GetPersonChatRoomRs.ext, builder.build());
	}

	public void getPersonChatRq(GetPersonChatRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		long chatRoomId = req.getChatRoomId();
		ConcurrentHashMap<Long, PersonChatRoom> personChatRoom = player.getPersonChatRoom();
		PersonChatRoom chatRoom = personChatRoom.get(chatRoomId);
		if (null == chatRoom) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		ChatPb.GetPersonChatRs.Builder builder = ChatPb.GetPersonChatRs.newBuilder();
		List<PersonChat> chats = chatRoom.getChats();
		Player target = playerManager.getPlayer(chatRoom.getReplayLordId());
		for (PersonChat chat : chats) {
			CommonPb.PersonChat.Builder chatPb = chat.serShow();
			if (target != null) {
				chatPb.setOtherHeadSculpture(target.getLord().getHeadIndex());
			}
			builder.addChat(chatPb);
		}

		handler.sendMsgToPlayer(GetPersonChatRs.ext, builder.build());
	}

	public void shareHero(ShareHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Hero hero = player.getHero(req.getHeroId());
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_HERO);
			return;
		}
		String[] p = new String[]{String.valueOf(player.roleId), String.valueOf(hero.getHeroId())};
		Chat chat = chatManager.createManShare(player, ChatId.SHARE_HERO, p);
		chatManager.sendCountryShare(player, chat);
		ShareHeroRs.Builder builder = ShareHeroRs.newBuilder();
		handler.sendMsgToPlayer(ShareHeroRs.ext, builder.build());
	}

	public void personChatReadRq(PersonChatReadRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		List<Long> roomIdList = req.getRoomIdList();
		/* long roomId = req.getRoomId(); */
		if (roomIdList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		ConcurrentHashMap<Long, PersonChatRoom> personChatRoom = player.getPersonChatRoom();
		if (null == personChatRoom || personChatRoom.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		for (Long roomId : roomIdList) {
			PersonChatRoom chatRoom = personChatRoom.get(roomId);
			if (null == chatRoom) {
				continue;
			}

			List<PersonChat> chats = chatRoom.getChats();
			for (PersonChat chat : chats) {
				if (null != chat) {
					if (chat.getState() == MailConst.UN_READ) {
						chat.setState(MailConst.READ);
					}
				}
			}
		}
		ChatPb.PersonChatReadRs.Builder builder = ChatPb.PersonChatReadRs.newBuilder();
		builder.setNotRead(playerManager.getPersonChatNotRead(player));
		handler.sendMsgToPlayer(ChatPb.PersonChatReadRs.ext, builder.build());
	}

	public void personChatRemoveRq(PersonChatRemoveRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		List<Long> roomIdList = req.getRoomIdList();
		if (roomIdList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		ConcurrentHashMap<Long, PersonChatRoom> personChatRoom = player.getPersonChatRoom();
		if (null == personChatRoom || personChatRoom.size() == 0) {
			ChatPb.PersonChatRemoveRs.Builder builder = ChatPb.PersonChatRemoveRs.newBuilder();
			builder.setNotRead(playerManager.getPersonChatNotRead(player));
			handler.sendMsgToPlayer(ChatPb.PersonChatRemoveRs.ext, builder.build());
			return;
		}

		for (Long roomId : roomIdList) {
			personChatRoom.remove(roomId);
		}

		ChatPb.PersonChatRemoveRs.Builder builder = ChatPb.PersonChatRemoveRs.newBuilder();
		builder.setNotRead(playerManager.getPersonChatNotRead(player));
		handler.sendMsgToPlayer(ChatPb.PersonChatRemoveRs.ext, builder.build());
	}

	/**
	 * 发送对应渠道走马灯
	 *
	 * @param channel
	 * @param chat
	 */
	public void sendChannelChat(int channel, Chat chat, StaticChat staticChat) {
		CommonPb.Chat b = chatManager.addWorldChat(chat);

		SynChatRq.Builder builder = SynChatRq.newBuilder();
		builder.setChat(b);

		Base.Builder msg = PbHelper.createSynBase(SynChatRq.EXT_FIELD_NUMBER, SynChatRq.ext, builder.build());

		Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
		ChannelHandlerContext ctx;
		while (it.hasNext()) {
			Player next = it.next();
			if (next.account != null && next.account.getChannel() != channel) {
				continue;
			}
			if (next.isLogin && !next.getBlackList().contains(b.getLordId()) && next.getLevel() >= staticChat.getLimitLevel()) {
//				ctx = next.ctx;
//				if (ctx != null) {
				GameServer.getInstance().sendMsgToPlayer(next, msg);
//				}
			}
		}
	}

	/**
	 * @Description 切磋
	 * @Param [handler, rq]
	 * @Return void
	 * @Date 2021/12/31 14:38
	 **/
	public void duelRq(ClientHandler handler, DuelRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		Player targetPlayer = playerManager.getPlayer(rq.getTargetId());
		if (player == null || targetPlayer == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (player.getLevel() < staticLimitMgr.getNum(SimpleId.DUEL_LIMIT)) {
			handler.sendErrorMsgToPlayer(GameError.LIVE_NO_ENOUGH);
			return;
		}
		if (player.getCountry() != targetPlayer.getCountry()) {
			handler.sendErrorMsgToPlayer(GameError.DIFFERENT_COUNTRIES);
			return;
		}
		List<Integer> targetEmbattleList = targetPlayer.getEmbattleList();
		if (targetEmbattleList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.TARGET_HERO_IS_EMPTY);
			return;
		}
		List<Integer> heroIdList = rq.getHeroIdList();
		ArrayList<Integer> heroIdListCopy = Lists.newArrayList(heroIdList);
		ArrayList<Integer> embattleList = Lists.newArrayList(player.getEmbattleList());
		heroIdListCopy.sort(Integer::compareTo);
		embattleList.sort(Integer::compareTo);
		// 校验上阵英雄
		if (!heroIdListCopy.toString().equals(embattleList.toString())) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		Team playerTeam = battleMgr.initPvePlayerTeam(player, heroIdList, BattleEntityType.HERO);
		Team targetTeam = battleMgr.initPvePlayerTeam(targetPlayer, targetEmbattleList, BattleEntityType.HERO);
		CommonPb.FightBefore.Builder fightBefore = CommonPb.FightBefore.newBuilder();
		// 玩家
		ArrayList<BattleEntity> playerEntities = playerTeam.getAllEnities();
		for (BattleEntity battleEntity : playerEntities) {
			fightBefore.addLeftEntities(battleEntity.wrapPb());
		}
		// 对手
		ArrayList<BattleEntity> monsterEntities = targetTeam.getAllEnities();
		for (BattleEntity battleEntity : monsterEntities) {
			fightBefore.addRightEntities(battleEntity.wrapPb());
		}
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, targetTeam, RandomUtil.random, !ActPassPortTaskType.IS_WORLD_WAR);
		// 战中信息
		CommonPb.FightIn.Builder fightIn = CommonPb.FightIn.newBuilder();
		// 玩家
		ArrayList<AttackInfo> playerAttackInfos = playerTeam.getAttackInfos();
		for (AttackInfo attackInfo : playerAttackInfos) {
			fightIn.addLeftInfo(attackInfo.wrapPb());
		}
		// 对手
		ArrayList<AttackInfo> monsterAttackInfos = targetTeam.getAttackInfos();
		for (AttackInfo attackInfo : monsterAttackInfos) {
			fightIn.addRightInfo(attackInfo.wrapPb());
		}
		Report report = new Report();
		report.setKeyId(System.currentTimeMillis());
		report.setResult(playerTeam.isWin());
		report.setLeftHead(battleMailManager.createPlayerReportHead(player, playerTeam, new Pos()));
		report.setRightHead(battleMailManager.createPlayerReportHead(targetPlayer, targetTeam, new Pos()));
		report.setLeftAttender(battleMailManager.createRebelAttender(playerTeam));
		report.setRightAttender(battleMailManager.createRebelAttender(targetTeam));
		DuelRs.Builder builder = DuelRs.newBuilder();
		builder.setReport(report.wrapPb());
		builder.setFightBefore(fightBefore);
		builder.setFightIn(fightIn);
		handler.sendMsgToPlayer(DuelRs.ext, builder.build());
	}
}
