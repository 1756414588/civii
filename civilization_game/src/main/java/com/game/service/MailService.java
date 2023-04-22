package com.game.service;


import com.alibaba.fastjson.JSON;
import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMailDataMgr;
import com.game.dataMgr.StaticSensitiveWordMgr;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.domain.Award;
import com.game.domain.p.Lord;
import com.game.domain.p.Mail;
import com.game.domain.s.StaticMail;
import com.game.log.constant.OperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.ChatLog;
import com.game.log.domain.MailLog;
import com.game.log.domain.ManoeuvreLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.MailPb;
import com.game.pb.MailPb.*;
import com.game.servlet.domain.SendMail;
import com.game.spring.SpringUtil;
import com.game.util.*;
import com.game.worldmap.fight.manoeuvre.ManoeuvreConst;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
public class MailService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MailManager mailManager;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticMailDataMgr staticMailDataMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private CountryManager countryManager;

	@Autowired
	private TechManager techManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private StaticSensitiveWordMgr staticSensitiveWordMgr;

	/**
	 * 获取邮件列表 分页处理
	 */
	public void getMailRq(MailPb.GetMailRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ConcurrentLinkedDeque<Mail> mails = player.getMails();
		GetMailRs.Builder builder = GetMailRs.newBuilder();
		long now = System.currentTimeMillis();

		if (!req.hasPage()) {
			Iterator<Mail> it = mails.iterator();
			while (it.hasNext()) {
				Mail mail = it.next();
				if (mail == null) {
					continue;
				}

				if (mail.getCreateTime() + TimeHelper.DAY_MS * 7 <= now) {
					it.remove();
					continue;
				}

				long replyLordId = mail.getReplyLordId();
				if (replyLordId != 0) {
					Player sendPlay = playerManager.getPlayer(replyLordId);

					if (sendPlay != null && sendPlay.account.getForbid() == 1) {
						continue;
					}
				}

				StaticMail staticMail = staticMailDataMgr.getStaticMail(mail.getMailId());
				if (staticMail == null) {
					continue;
				}

				List<CommonPb.Award> award = mail.getAward();
				if (award.isEmpty()) {
					mail.setAwardGot(2); // 如果为空则邮件got为2
				}

				builder.addMail(mail.serShow());
			}
		} else {
			List<Mail> targetMails = new ArrayList<Mail>();
			int targetType = req.getType();
			Iterator<Mail> it = mails.iterator();
			while (it.hasNext()) {
				Mail mail = it.next();
				if (mail == null) {
					continue;
				}

				if (mail.getCreateTime() + TimeHelper.DAY_MS * 7 <= now) {
					it.remove();
					continue;
				}

				StaticMail staticMail = staticMailDataMgr.getStaticMail(mail.getMailId());
				if (staticMail == null) {
					continue;
				}

				List<CommonPb.Award> award = mail.getAward();
				if (award.isEmpty()) {
					mail.setAwardGot(2); // 如果为空则邮件got为2
				}

				if (targetType != staticMail.getType()) {
					continue;
				}

				targetMails.add(mail);
			}

			int page = req.getPage();
			if (page <= 0) {
				page = 1;
			}

			int maxPage;
			if (!targetMails.isEmpty()) {
				maxPage = MathHelper.devide(targetMails.size(), 6);
				page = Math.max(1, page);
				page = Math.min(page, maxPage);
				for (int mailIndex = (page - 1) * 6 + 1; mailIndex <= page * 6 && mailIndex <= targetMails.size(); mailIndex++) {
					Mail next = targetMails.get(mailIndex - 1);
					if (next == null) {
						continue;
					}
					builder.addMail(next.serShow());
				}
			}
		}

		handler.sendMsgToPlayer(GetMailRs.ext, builder.build());
		//System.err.println(builder.build().toString());
	}

	/**
	 * 读取邮件
	 */
	public void mailReadRq(MailReadRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		long lordId = handler.getRoleId();
		if (req.hasLordId()) {
			lordId = req.getLordId();
		}
		int keyId = req.getKeyId();
		MailReadRs.Builder builder = MailReadRs.newBuilder();
		Mail mail = mailManager.getTargetMail(lordId, keyId);
		if (mail == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_EXIST_MAIL);
			return;
		}

		// 亲自阅读才会被
		if (mail.getState() == MailConst.UN_READ && mail.getLordId() == player.getLord().getLordId()) {
			mail.setState(MailConst.READ);
		}
		builder.setMail(mail.serDefault());
		builder.addAllNotRead(playerManager.getNotRead(player));

		handler.sendMsgToPlayer(MailReadRs.ext, builder.build());
	}

	/**
	 * 锁定邮件
	 */
	public void mailLockRq(MailLockRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {

			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int status = req.getState();
		if (status != MailConst.READ && status != MailConst.LOCK) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		int keyId = req.getKeyId();
		Mail mail = player.getMail(keyId);
		if (mail == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_EXIST_MAIL);
			return;
		}

		mail.setState(status);
		MailLockRs.Builder builder = MailLockRs.newBuilder();
		handler.sendMsgToPlayer(MailLockRs.ext, builder.build());
	}

	/**
	 * 删除邮件
	 *
	 * @param req
	 * @param handler
	 */
	public void mailRemoveRq(MailRemoveRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		List<Integer> removeList = req.getKeyIdList();
		for (Integer key : removeList) {
			player.removeMail(key);
		}

		MailRemoveRs.Builder builder = MailRemoveRs.newBuilder();
		builder.addAllNotRead(playerManager.getNotRead(player));
		handler.sendMsgToPlayer(MailRemoveRs.ext, builder.build());
	}

	/**
	 * 领取邮件奖励
	 *
	 * @param req
	 * @param handler
	 */
	public void mailAwardRq(MailAwardRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int keyId = req.getKeyId();

		Mail mail = mailManager.getTargetMail(handler.getRoleId(), keyId);
		if (mail == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 奖励已领取
		if (mail.getAwardGot() == MailConst.AWARD_GOT) {
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}

		List<CommonPb.Award> awardList = mail.getAward();
		if (playerManager.isEquipFulls(awardList, player)) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
			return;
		}

		MailAwardRs.Builder builder = MailAwardRs.newBuilder();
		mail.setAwardGot(MailConst.AWARD_GOT);

		for (CommonPb.Award award : awardList) {
			int awardId = playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.MAIL_AWARD);
			builder.addAward(PbHelper.createAward(player, award.getType(), award.getId(), award.getCount(), awardId));
			/**
			 * 领取邮件奖励日志埋点
			 */
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			//记录城战抢夺资源
			if (mail.getMailId() == MailId.CITY_WAR_WIN) {
				if (award.getType() == AwardType.RESOURCE) {
					logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
						player.getNick(),
						player.getLevel(),
						player.getTitle(),
						player.getHonor(),
						player.getCountry(),
						player.getVip(),
						player.account.getChannel(),
						0, award.getCount(), OperateType.ROBO.getValue()), award.getId());
				}
			}

			// 记录沙盘演武个人杀敌数--个人轮空--阵营排名积分奖励
			if (mail.getMailId() == MailId.MANOEUVRE_PERSON_AWARD || mail.getMailId() == MailId.MANOEUVRE_EMPTY_AWARD || mail.getMailId() == MailId.MANOEUVRE_CAMP_AWARD || mail.getMailId() == MailId.CUSTOMIZE_MAIL) {
				if (award.getType() == AwardType.MANOEUVRE_SCORE) {
//                    LogHelper.GAME_LOGGER.info("领取 {} {}",player.getNick(),award.getCount());
					logUser.manoeuvre_log(
						ManoeuvreLog.builder()
							.roleId(player.roleId)
							.nick(player.getNick())
							.level(player.getLevel())
							.vipLevel(player.getVip())
							.changePoint(award.getCount())
							.itemId(0)
							.itemNum(0)
							.source(mail.getMailId())
							.type(2) // type=2 为获得积分
							.point(player.getSimpleData().getManoeuvreScore())
							.build());
				}
			}

		}

		handler.sendMsgToPlayer(MailAwardRs.ext, builder.build());
	}

	/**
	 * @param req
	 * @param handler
	 */
	public void getMailReportRq(GetMailReportRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (req.getPage() < 0) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		long lordId = handler.getRoleId();
		if (req.hasLordId()) {
			lordId = req.getLordId();
		}

		int keyId = req.getKeyId();
		Mail mail = mailManager.getTargetMail(lordId, keyId);
		if (mail == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_EXIST_MAIL);
			return;
		}
		CommonPb.ReportMsg reportMsg = mail.getReportMsg();
		GetMailReportRs.Builder builder = GetMailReportRs.newBuilder();
		builder.setReportMsg(reportMsg);
		handler.sendMsgToPlayer(GetMailReportRs.ext, builder.build());
	}

	public int getTechType(int type) {

		int ret = 0;
		if (type == SoldierType.ROCKET_TYPE) {
			ret = TechType.ROCKET_ATTACK;
		} else if (type == SoldierType.TANK_TYPE) {
			ret = TechType.TANK_ATTACK;
		} else if (type == SoldierType.WAR_CAR) {
			ret = TechType.WARCAR_ATTACK;
		}
		return ret;

	}

	/**
	 * 给玩家发送邮件
	 *
	 * @param req
	 * @param handler
	 */
	public void sendMailRq(SendMailRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int vip = player.getLord().getVip();
		int minVip = staticLimitMgr.getNum(242);//开启聊天的最低VIP等级
		if (player.getLord().getOpenSpeak() != 1) {
			if (vip >= minVip) {
				player.getLord().setOpenSpeak(1);
			} else {
				if (player.getLevel() < SpringUtil.getBean(ChatService.class).getChatLimitLevel(SimpleId.CHAT_LEVEL)) {
					handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
					return;
				}
				player.getLord().setOpenSpeak(1);
			}
		}
		Player target = playerManager.getPlayer(req.getNick());
		if (target == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		if (!req.hasContent() || req.getContent().isEmpty() || req.getContent().length() > 100) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		// 替换敏感词
		String content = staticSensitiveWordMgr.replaceSensitiveWord(req.getContent(), "privateChatFilter");

		if (player.getLord().getLordId() == target.getLord().getLordId()) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		if (player.getCountry() != target.getCountry()) {
			if (player.getGold() < 2) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, 2, Reason.SEND_MAIL);
		}

		// 给目标发送邮件
		SendMailRs.Builder builder = SendMailRs.newBuilder();
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(SendMailRs.ext, builder.build());

		int country = player.getCountry();
		int level = player.getLevel();
		if (!target.getBlackList().contains(handler.getRoleId())) {
//            Mail toMail = mailManager.addMail(target, MailId.PLAYER_SEND_MAIL, String.valueOf(country), String.valueOf(level), player.getNick(), req.getContent());
			Mail toMail = mailManager.addMail(target, MailId.PLAYER_SEND_MAIL, String.valueOf(country), String.valueOf(level), player.getNick(), content);
			toMail.setReplyId(0);
			toMail.setReplyLordId(player.getLord().getLordId());
			toMail.setPortrait(player.getLord().getPortrait());
			playerManager.synMailToPlayer(target, toMail);
			/**
			 * 聊天日志埋点
			 */
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			logUser.chatLog(new ChatLog(player.account.getServerId(),
				player.roleId,
				player.getLord().getNick(),
				player.getLevel(),
				player.getVip(),
				player.account.getIsGm(),
				player.account.getChannel(),
				player.account.getAccountKey(),
				player.getCountry(),
				4,
				req.getContent()));
		}
	}

	/**
	 * 回复邮件
	 *
	 * @param req
	 * @param handler
	 */
	public void replyMailRq(ReplyMailRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 替换敏感词
		String content = staticSensitiveWordMgr.replaceSensitiveWord(req.getContent(), "privateChatFilter");

		Lord lord = player.getLord();
		int vip = player.getLord().getVip();
		int minVip = staticLimitMgr.getNum(242);//开启聊天的最低VIP等级
		if (player.getLord().getOpenSpeak() != 1) {
			if (vip >= minVip) {
				player.getLord().setOpenSpeak(1);
			} else {
				if (player.getLevel() < SpringUtil.getBean(ChatService.class).getChatLimitLevel(SimpleId.CHAT_LEVEL)) {
					handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
					return;
				}
				player.getLord().setOpenSpeak(1);
			}
		}
		long replyLordId = req.getLordId();// 邮件中的replyLordId

		Player target = playerManager.getPlayer(replyLordId);
		if (target == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (target.getBlackList().contains(handler.getRoleId())) {
			handler.sendErrorMsgToPlayer(GameError.BLACK);
			return;
		}

		if (!req.hasContent() || req.getContent().isEmpty() || req.getContent().length() > 100) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		if (lord.getLordId() == target.getLord().getLordId()) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		if (player.getCountry() != target.getCountry()) {
			if (player.getGold() < 2) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, 2, Reason.SEND_MAIL);
		}

		int country = player.getCountry();
		int level = player.getLevel();

//        String[] param = {String.valueOf(country), String.valueOf(level), player.getNick(), req.getContent()};
		String[] param = {String.valueOf(country), String.valueOf(level), player.getNick(), content};
		Mail mail = mailManager.addMail(target, MailId.PLAYER_SEND_MAIL, param);
		mail.setReplyId(0);
		mail.setReplyLordId(player.getLord().getLordId());
		mail.setPortrait(player.getLord().getPortrait());

		ReplyMailRs.Builder builder = ReplyMailRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setMail(mail.serShow());
		handler.sendMsgToPlayer(ReplyMailRs.ext, builder.build());

		playerManager.synMailToPlayer(target, mail);
	}

	/**
	 * @param req
	 * @param handler
	 */
	public void blackList(BlackListRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		List<Long> blackList = player.getBlackList();
		BlackListRs.Builder builder = BlackListRs.newBuilder();

		for (Long id : blackList) {
			Player target = playerManager.getPlayer(id);
			if (target == null) {
				continue;
			}
			int officerId = countryManager.getOfficeId(target);
			builder.addManBlack(PbHelper.createManBlack(target.getLord().getLordId(), target.getLevel(), target.getLord().getPortrait(), target.getCountry(),
				target.getNick(), officerId, target.getTitle(), target.getMaxScore()));
		}
		handler.sendMsgToPlayer(BlackListRs.ext, builder.build());
	}

	/**
	 * 拉黑洗白玩家
	 *
	 * @param req
	 * @param handler
	 */
	public void blackRq(BlackRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		List<Long> blackList = player.getBlackList();

		long lordId = req.getLordId();
		int power = 0;
		if (req.getBlack() == 1) {// 拉黑
			if (!blackList.contains(lordId)) {
				Player target = playerManager.getPlayer(lordId);
				if (target != null) {
					blackList.add(lordId);
					power = target.getMaxScore();
				}
			}
		} else {
			blackList.remove(lordId);
		}

		BlackRs.Builder builder = BlackRs.newBuilder();
		builder.setPower(power);
		handler.sendMsgToPlayer(BlackRs.ext, builder.build());
	}

	/**
	 * 一键已读
	 *
	 * @param req
	 * @param handler
	 */
	public void readAllRq(ReadAllRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int type = req.getTarget();

		Iterator<Mail> it = player.getMails().iterator();
		ReadAllRs.Builder builder = ReadAllRs.newBuilder();
		while (it.hasNext()) {
			Mail next = it.next();
			if (next == null) {
				continue;
			}
			int mailId = next.getMailId();
			StaticMail staticMail = staticMailDataMgr.getStaticMail(mailId);
			if (staticMail == null) {
				continue;
			}
			if (staticMail.getType() != req.getTarget()) {
				continue;
			}
			if (staticMail != null && staticMail.getType() == type) {
				next.setState(MailConst.READ);
			}

			// 只有系统奖励才会有奖励
			if (req.getIsAward() == 1 && req.getTarget() == 2) {
				// 奖励已领取
				if (next.getAwardGot() == MailConst.AWARD_GOT) {
					continue;
				}
				List<CommonPb.Award> awardList = next.getAward();
				if (playerManager.isEquipFulls(awardList, player)) {
					handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
					return;
				}
				for (CommonPb.Award award : awardList) {
					int awardId = playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.MAIL_AWARD);
					builder.addAward(PbHelper.createAward(player, award.getType(), award.getId(), award.getCount(), awardId));
					/**
					 * 邮件资源产出日志埋点
					 */
					com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
					// 记录沙盘演武个人杀敌数--个人轮空--阵营排名积分奖励
					if (mailId == MailId.MANOEUVRE_PERSON_AWARD || mailId == MailId.MANOEUVRE_EMPTY_AWARD || mailId == MailId.MANOEUVRE_CAMP_AWARD || mailId == MailId.CUSTOMIZE_MAIL) {
						if (award.getType() == AwardType.MANOEUVRE_SCORE) {
//                            LogHelper.GAME_LOGGER.info("领取 {} {}",player.getNick(),award.getCount());
							logUser.manoeuvre_log(
								ManoeuvreLog.builder()
									.roleId(player.roleId)
									.nick(player.getNick())
									.level(player.getLevel())
									.vipLevel(player.getVip())
									.changePoint(award.getCount())
									.itemId(0)
									.itemNum(0)
									.source(mailId)
									.type(2) // type=2 为获得积分
									.point(player.getSimpleData().getManoeuvreScore())
									.build());
						}
					}

				}
				next.setAwardGot(MailConst.AWARD_GOT);
			}

		}
		handler.sendMsgToPlayer(ReadAllRs.ext, builder.build());
	}

	/**
	 * 全服邮件定时任务执行
	 */
	public void sendTimerLogic() {
		Iterator<SendMail> iterator = mailManager.selectAllServerMail().values().iterator();

		while (iterator.hasNext()) {

			SendMail sendMail = iterator.next();
			try {
				long startTime = sendMail.getStartTime().getTime() / 1000;
				long endTime = sendMail.getEndTime().getTime() / 1000;
				long serverTime = DateHelper.getServerTime();

				if (sendMail.getStatus() == SendMail.UN_SEND && sendMail.getRemove() == SendMail.UN_REMOVE) {
					if (endTime < serverTime) {
						sendMail.setStatus(SendMail.HAVE_SEND);
					} else if (endTime >= serverTime && startTime <= serverTime) {

						List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
						if (sendMail.getAwardList() != null && !sendMail.getAwardList().equals("")) {
							List<Award> award = JSON.parseArray(sendMail.getAwardList(), Award.class);
							if (award.size() > 0) {
								for (Award awardInfo : award) {
									awardList.add(PbHelper.createAward(awardInfo.getType(), awardInfo.getId(), awardInfo.getCount()).build());
								}
							}
						}

						Map<Long, Player> allPlayer = playerManager.getAllPlayer();

						if (null != allPlayer && allPlayer.size() > 0) {
							Set<Entry<Long, Player>> entryPlayer = allPlayer.entrySet();
							for (Entry<Long, Player> entryPlay : entryPlayer) {
								Account account = entryPlay.getValue().account;
								if (sendMail.getChannel().size() > 0) {
									if (account != null && !sendMail.getChannel().contains(account.getChannel())) {
										continue;
									}
								}
								vipType(sendMail, entryPlay.getValue(), awardList);
							}
						}
						sendMail.setStatus(SendMail.HAVE_SEND);
					}
				}


			} catch (Exception e) {
				iterator.remove();
				logger.error("MailService sendTimerLogic : parames {},desc{}", sendMail, e);
			}
		}
	}


	/**
	 * 判断VIP类型
	 * <p>
	 * 1.最低等级 (表示VIP需要大于多少)
	 * <p>
	 * 2.区间 (VIP请用-隔开 如2-3表示只有)
	 * <p>
	 * 3.间隔(用逗号隔开)
	 *
	 * @param sendMail
	 * @param player
	 * @param awardList
	 */
	public void vipType(SendMail sendMail, Player player, List<CommonPb.Award> awardList) {
		Integer vipType = sendMail.getVipType();
		String tplId = "2";
		if (awardList.size() > 0) {
			tplId = "1";
		}
		switch (vipType) {

			case 1:
				int vipMin = Integer.parseInt(sendMail.getVip());
				if (player.getVip() >= vipMin) {
					Mail natSerMail;
					if (sendMail.getMailId() == MailId.CUSTOMIZE_MAIL) {
						natSerMail = mailManager.addMail(player, sendMail.getMailId(), sendMail.getTitle(), sendMail.getTitleContent(), sendMail.getContent(), tplId);
					} else {
						natSerMail = mailManager.addMail(player, sendMail.getMailId(), sendMail.getContent());
					}
					if (natSerMail != null) {
						natSerMail.setMailKey(sendMail.getKeyId());
						if (awardList.size() > 0 && sendMail.getMailId() != MailId.MAIL_SYS) {
							mailManager.addMailAward(natSerMail, awardList);
						} else {
							natSerMail.setAwardGot(2);
						}
						if (player.isLogin) {
							natSerMail.setMailKey(sendMail.getKeyId());
							playerManager.synMailToPlayer(player, natSerMail);
						}
						SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder()
							.lordId(natSerMail.getLordId())
							.mailId(natSerMail.getMailId())
							.nick(player.getNick())
							.vip(player.getVip())
							.level(player.getLevel())
							.msg(mailManager.mailToString(natSerMail))
							.build());
					}

				}
				break;
			case 2:
				String[] vipDuring = sendMail.getVip().split("-");

				if (player.getVip() >= Integer.parseInt(vipDuring[0]) && player.getVip() <= Integer.parseInt(vipDuring[1])) {
					Mail natSerMail;
					if (sendMail.getMailId() == MailId.CUSTOMIZE_MAIL) {
						natSerMail = mailManager.addMail(player, sendMail.getMailId(), sendMail.getTitle(), sendMail.getTitleContent(), sendMail.getContent(), tplId);
					} else {
						natSerMail = mailManager.addMail(player, sendMail.getMailId(), sendMail.getContent());
					}
					if (natSerMail != null) {
						natSerMail.setMailKey(sendMail.getKeyId());
						if (awardList.size() > 0 && sendMail.getMailId() != MailId.MAIL_SYS) {
							mailManager.addMailAward(natSerMail, awardList);
						} else {
							natSerMail.setAwardGot(2);
						}
						if (player.isLogin) {
							playerManager.synMailToPlayer(player, natSerMail);
						}
						SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder()
							.lordId(natSerMail.getLordId())
							.mailId(natSerMail.getMailId())
							.nick(player.getNick())
							.vip(player.getVip())
							.level(player.getLevel())
							.msg(mailManager.mailToString(natSerMail))
							.build());
					}

				}
				break;
			case 3:
				String[] vipGroup = sendMail.getVip().split(",");
				for (String vipGro : vipGroup) {
					if (player.getVip() == Integer.parseInt(vipGro)) {
						Mail natSerMail;
						if (sendMail.getMailId() == MailId.CUSTOMIZE_MAIL) {
							natSerMail = mailManager.addMail(player, sendMail.getMailId(), sendMail.getTitle(), sendMail.getTitleContent(), sendMail.getContent(), tplId);
						} else {
							natSerMail = mailManager.addMail(player, sendMail.getMailId(), sendMail.getContent());
						}
						if (natSerMail != null) {
							natSerMail.setMailKey(sendMail.getKeyId());
							if (awardList.size() > 0 && sendMail.getMailId() != MailId.MAIL_SYS) {
								mailManager.addMailAward(natSerMail, awardList);
							} else {
								natSerMail.setAwardGot(2);
							}
							if (player.isLogin) {
								playerManager.synMailToPlayer(player, natSerMail);
							}
							SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder()
								.lordId(natSerMail.getLordId())
								.mailId(natSerMail.getMailId())
								.nick(player.getNick())
								.vip(player.getVip())
								.level(player.getLevel())
								.msg(mailManager.mailToString(natSerMail))
								.build());
						}

					}
				}
				break;
		}
	}


	/**
	 * 拿到邮件信息
	 *
	 * @param handler
	 */
	public void getMailCount(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		//根据数据倒排
		List<Mail> mails = Lists.newArrayList(player.getMails()).stream().sorted(Comparator.comparingLong(Mail::getCreateTime).reversed()).collect(Collectors.toList());
//        ConcurrentLinkedDeque<Mail> mails = player.getMails();

		Map<Integer, Integer> mailCount = new HashMap<>();
		Iterator<Mail> it = mails.iterator();
		while (it.hasNext()) {
			Mail mail = it.next();
			if (mail == null) {
				continue;
			}
			StaticMail staticMail = staticMailDataMgr.getStaticMail(mail.getMailId());
			if (staticMail == null) {
				continue;
			}
			Integer num = mailCount.get(staticMail.getType());
			if (num == null) {
				mailCount.put(staticMail.getType(), 1);
			} else {
				mailCount.put(staticMail.getType(), num + 1);
			}
		}
		MailPb.GetMailCountRs.Builder builder = MailPb.GetMailCountRs.newBuilder();
		for (Map.Entry<Integer, Integer> entry : mailCount.entrySet()) {
			MailPb.MailCount.Builder count = MailPb.MailCount.newBuilder();
			count.setType(entry.getKey()).setCount(entry.getValue());
			builder.addMailCount(count);
		}
		handler.sendMsgToPlayer(MailPb.GetMailCountRs.ext, builder.build());
	}

	public void getCountryMailRq(GetCountryMailRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		GetCountryMailRs.Builder builder = GetCountryMailRs.newBuilder();
		builder.setCostGold(staticLimitMgr.getNum(SimpleId.ACT_COUNTRY_MAIL));
		handler.sendMsgToPlayer(MailPb.GetCountryMailRs.ext, builder.build());
	}

	public void sendCountryMailRq(SendCountryMailRq rq, ClientHandler handler) {
		SendCountryMailRs.Builder builder = SendCountryMailRs.newBuilder();
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (player.getLevel() < staticLimitMgr.getNum(SimpleId.COUNTRY_MAIL_LEVEL)) {
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}
		List<Lord> countryRankList = SpringUtil.getBean(RankManager.class).getCountryRankList(player.getLord().getCountry(), 0, staticLimitMgr.getNum(SimpleId.COUNTRY_MAIL_RANK_LIMIT));
		boolean flag = false;
		for (Lord lord : countryRankList) {
			if (lord.getLordId() == player.getLord().getLordId()) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			builder.setGold(player.getGold());
			builder.setCode(1);
			handler.sendMsgToPlayer(MailPb.SendCountryMailRs.ext, builder.build());
			return;
		}

		int costGold = staticLimitMgr.getNum(SimpleId.ACT_COUNTRY_MAIL);
		if (costGold > player.getGold()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		String mail = rq.getMail();
		playerManager.subGold(player, costGold, Reason.SEND_COUNTRY_GOLD);

		String level = String.valueOf(player.getLevel());
		String country = String.valueOf(player.getCountry());
		playerManager.getAllPlayer().values().forEach(p -> {
			if (p.getCountry() == player.getCountry()) {
				Mail toMail = mailManager.addMail(p, MailId.COUNTRY_MAIL, country, level, player.getNick(),
					mail);
				toMail.setReplyId(0);
				toMail.setReplyLordId(player.getLord().getLordId());
				toMail.setPortrait(player.getLord().getPortrait());
				playerManager.synMailToPlayer(p, toMail);
				SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder()
					.lordId(toMail.getLordId())
					.mailId(toMail.getMailId())
					.nick(player.getNick())
					.vip(player.getVip())
					.level(player.getLevel())
					.msg(mailManager.mailToString(toMail))
					.build());
			}
		});
		builder.setGold(player.getGold());
		builder.setCode(0);
		handler.sendMsgToPlayer(MailPb.SendCountryMailRs.ext, builder.build());
		eventManager.countryMail(player, Lists.newArrayList(
			costGold
		));
	}

	/**
	 * 获取单个私人邮件列表
	 *
	 * @param req
	 * @param handler
	 */
	public void GetPersonMailRq(MailPb.GetPersonMailRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		long targetLordId = req.getLordId();
		Player target = playerManager.getPlayer(targetLordId);
		if (target == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		MailPb.GetPersonMailRs.Builder builder = MailPb.GetPersonMailRs.newBuilder();
		ConcurrentLinkedDeque<Mail> mails = player.getMails();
		ConcurrentLinkedDeque<Mail> targetMails = target.getMails();
		List<Mail> personMails = new ArrayList<>();

		Iterator<Mail> iterator = mails.iterator();
		while (iterator.hasNext()) {
			Mail next = iterator.next();
			long replyLordId = next.getReplyLordId();
			if (replyLordId == targetLordId) {
				personMails.add(next);
			}
		}

		Iterator<Mail> iterator1 = targetMails.iterator();
		while (iterator1.hasNext()) {
			Mail next = iterator1.next();
			long replyLordId = next.getReplyLordId();
			if (replyLordId == handler.getRoleId()) {
				personMails.add(next);
			}
		}

		List<Mail> chatList = personMails.stream().sorted(Comparator.comparingLong(Mail::getCreateTime).reversed()).collect(Collectors.toList());
		for (Mail mail : chatList) {
			builder.addMail(mail.serShow());
		}
		handler.sendMsgToPlayer(MailPb.GetPersonMailRs.ext, builder.build());
	}
}
