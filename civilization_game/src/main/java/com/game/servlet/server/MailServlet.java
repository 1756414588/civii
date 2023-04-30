package com.game.servlet.server;

import com.game.spring.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.game.log.domain.MailLog;
import com.game.pb.BasePb;
import com.game.pb.MailPb;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.game.constant.MailId;
import com.game.constant.UcCodeEnum;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Mail;
import com.game.manager.MailManager;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.server.GameServer;
import com.game.servlet.domain.MailType;
import com.game.servlet.domain.SendMail;
import com.game.servlet.domain.SendTargetMail;
import com.game.uc.Message;
import com.game.util.PbHelper;

/**
 * 2020年4月27日
 *
 *    halo_game
 * <p>
 * MailServlet.java 邮件相关接口
 **/
@Controller
@RequestMapping("mail")
public class MailServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 发送邮件接口
     *
     * @param params
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/sendMail", method = RequestMethod.POST)
    public Message sendMail(String params) {

        long keyId = 0;
        try {
            logger.info("MailServlet sendMail : parames {}", params);

            if (!StringUtils.isNotEmpty(params)) {
                logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            /**
             * json字符串转java对象
             */
            SendMail sendMail = JSON.parseObject(params, SendMail.class);
            //转换下
            if (sendMail.getChannel() != null && !sendMail.getChannel().contains(null)) {
                sendMail.setChannelList(JSONArray.toJSONString(sendMail.getChannel()));
            }
            PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
            Player player = null;
            if (sendMail.getRoleId() != null) {
                player = playerManager.getPlayer(sendMail.getRoleId());
                if (player == null) {
                    logger.error("MailServlet roleGmComm : parames {},desc{}", params, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
                    return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
                }
                if (player.account != null && sendMail.getChannel() != null && !sendMail.getChannel().contains(null) && !sendMail.getChannel().contains(player.account.getChannel())) {
                    logger.error("MailServlet roleGmComm : parames {},desc{}", params, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
                    return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
                }
            }

            Integer type = sendMail.getType();
            MailManager mailManager = SpringUtil.getBean(MailManager.class);
            /**
             * 是否有奖励
             */
            List<CommonPb.Award> awardList = null;
            if (sendMail.getAwards() != null && sendMail.getAwards().size() > 0) {
                awardList = new ArrayList<CommonPb.Award>();
                List<Award> award = sendMail.getAwards();
                for (Award awardInfo : award) {
                    awardList.add(PbHelper.createAward(awardInfo.getType(), awardInfo.getId(), awardInfo.getCount()).build());
                }
                sendMail.setAwardList(JSON.toJSONString(sendMail.getAwards()));
            }
            sendMail.setMailId(MailId.CUSTOMIZE_MAIL);
            sendMail.setTitle(sendMail.getTitle().replaceAll("\r", ""));
            sendMail.setTitleContent(sendMail.getTitleContent().replaceAll("\r", ""));
            sendMail.setContent(sendMail.getContent().replaceAll("\r", ""));
            switch (type) {
                // 发送普通邮件
                case MailType.NOMAL:
                    if (player == null) {
                        logger.error("MailServlet roleGmComm : parames {},desc{}", params, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
                        return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
                    }
                    Mail nomalMail = mailManager.addMail(player, MailId.CUSTOMIZE_MAIL, sendMail.getTitle(), sendMail.getTitleContent(), sendMail.getContent(), "2");
                    if (nomalMail != null) {
                        nomalMail.setAwardGot(2);
                        if (player.isLogin) {
                            playerManager.synMailToPlayer(player, nomalMail);
                        }
                    }

                    if (null != nomalMail) {
                        sendMail.setStatus(SendMail.HAVE_SEND);
                        keyId = mailManager.addServerMail(sendMail);
                        nomalMail.setMailKey(keyId);
                    }
                    break;
                case MailType.ITEMS:
                    if (player == null) {
                        logger.error("MailServlet roleGmComm : parames {},desc{}", params, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
                        return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
                    }

                    if (awardList == null || awardList.size() == 0) {
                        logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
                        return new Message(UcCodeEnum.PARAM_ERROR);
                    }

                    Mail itemslMail = mailManager.addMail(player, MailId.CUSTOMIZE_MAIL, sendMail.getTitle(), sendMail.getTitleContent(), sendMail.getContent(), "1");
                    if (itemslMail != null) {
                        mailManager.addMailAward(itemslMail, awardList);
                        if (player.isLogin) {
                            playerManager.synMailToPlayer(player, itemslMail);
                        }
                        SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder()
                                .lordId(itemslMail.getLordId())
                                .mailId(itemslMail.getMailId())
                                .nick(player.getNick())
                                .vip(player.getVip())
                                .level(player.getLevel())
                                .msg(mailManager.mailToString(itemslMail))
                                .build());
                    }

                    if (null != itemslMail) {
                        sendMail.setStatus(SendMail.HAVE_SEND);
                        keyId = mailManager.addServerMail(sendMail);
                        itemslMail.setMailKey(keyId);
                    }
                    break;
                case MailType.NATIVE_SERVER:
                    if (null == sendMail.getStartTime() || null == sendMail.getEndTime()) {
                        logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
                        return new Message(UcCodeEnum.PARAM_ERROR);
                    }
                    if (null == sendMail.getVipType() || null == sendMail.getVip()) {
                        logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
                        return new Message(UcCodeEnum.PARAM_ERROR);
                    }
                    keyId = mailManager.addServerMail(sendMail);
                    break;
                /*
                 * case MailType.ALL_SERVER: break;
                 */
                default:
                    logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.MIAL_TYPE_NOT_EXIST.getDesc());
                    return new Message(UcCodeEnum.PARAM_ERROR, UcCodeEnum.MIAL_TYPE_NOT_EXIST.getDesc());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.SYS_ERROR.getDesc());
			logger.error(e.toString());
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS, String.valueOf(keyId));
	}


	/**
	 * 发送目标邮件
	 *
	 * @param params
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/sendTargetMail", method = RequestMethod.POST)
	public Message sendTargetMail(String params) {

		long keyId = 0;
		try {
			logger.info("MailServlet sendTargetMail : parames {}", params);

			if (!StringUtils.isNotEmpty(params)) {
				logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
				return new Message(UcCodeEnum.PARAM_ERROR);
			}

			/**
			 * json字符串转java对象
			 */
			SendTargetMail sendMail = JSON.parseObject(params, SendTargetMail.class);
			if (sendMail.getPlayerList() == null || sendMail.getPlayerList().isEmpty()) {
				logger.error("MailServlet roleGmComm : parames {},desc{}", params, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
				return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
			}

			PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);

			MailManager mailManager = SpringUtil.getBean(MailManager.class);
			/**
			 * 是否有奖励
			 */
			List<CommonPb.Award> awardList = new ArrayList<>();
			if (sendMail.getAwards() != null) {
				JSONArray items = (JSONArray) JSONArray.parse(sendMail.getAwards());
				for (int i = 0; i < items.size(); i++) {
					JSONArray item = items.getJSONArray(i);
					int type = (int) item.get(0);
					int id = (int) item.get(1);
					int count = (int) item.get(2);
					awardList.add(PbHelper.createAward(type, id, count).build());
				}
			}

			sendMail.setMailId(MailId.CUSTOMIZE_MAIL);
			sendMail.setTitle(sendMail.getTitle().replaceAll("\r", ""));
			sendMail.setTitleContent(sendMail.getTitleContent().replaceAll("\r", ""));
			sendMail.setContent(sendMail.getContent().replaceAll("\r", ""));

			int type = MailType.NOMAL;
			if (awardList != null && !awardList.isEmpty()) {
				type = MailType.ITEMS;
			}
			JSONArray playerList = (JSONArray) JSONArray.parse(sendMail.getPlayerList());
			for (int i = 0; i < playerList.size(); i++) {
				long roleId = playerList.getInteger(i);
				Player player = playerManager.getPlayer(roleId);
				if (player == null) {
					continue;
				}

				if (type == MailType.NOMAL) {
					Mail nomalMail = mailManager.addMail(player, MailId.CUSTOMIZE_MAIL, sendMail.getTitle(), sendMail.getTitleContent(), sendMail.getContent(), "2");
					if (nomalMail != null) {
						nomalMail.setAwardGot(2);
						if (player.isLogin) {
							playerManager.synMailToPlayer(player, nomalMail);
						}
					}
				} else if (type == MailType.ITEMS) {
					Mail itemslMail = mailManager.addMail(player, MailId.CUSTOMIZE_MAIL, sendMail.getTitle(), sendMail.getTitleContent(), sendMail.getContent(), "1");
					if (itemslMail != null) {
						mailManager.addMailAward(itemslMail, awardList);
						if (player.isLogin) {
							playerManager.synMailToPlayer(player, itemslMail);
						}
						SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder()
							.lordId(itemslMail.getLordId())
							.mailId(itemslMail.getMailId())
							.nick(player.getNick())
							.vip(player.getVip())
							.level(player.getLevel())
							.msg(mailManager.mailToString(itemslMail))
							.build());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("MailServlet sendTargetMail : parames {},desc{}", keyId, UcCodeEnum.SYS_ERROR.getDesc());
            logger.error(e.toString());
            return new Message(UcCodeEnum.SYS_ERROR);
        }
        return new Message(UcCodeEnum.SUCCESS, String.valueOf(keyId));
    }

    @ResponseBody
    @RequestMapping(value = "/deleteMail", method = RequestMethod.POST)
    public Message deleteMail(String keyId) {
        logger.info("MailServlet deleteMail : parames {}", keyId);
        try {

            if (!StringUtils.isNotEmpty(keyId)) {
                logger.error("MailServlet sendMail : parames {},desc{}", keyId, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            MailManager mailManager = SpringUtil.getBean(MailManager.class);
            SendMail findServerMail = mailManager.findServerMail(Long.parseLong(keyId));
            if (null != findServerMail) {
                mailManager.deleteSeverMail(Long.parseLong(keyId));
            }
            final long mailKeyId = Long.parseLong(keyId);
            if (null != findServerMail) {
                PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
                //查找所有有这个邮件的玩家
                Map<Long, Player> allPlayer = playerManager.getAllPlayer();
                allPlayer.forEach((e, f) -> {
                    recallPlayerMail(mailManager, f.roleId, mailKeyId);
                });
            } else {
                //删除玩家的邮件
                PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
                playerManager.getAllPlayer().values().forEach(e -> {
                    recallPlayerMail(mailManager, e.roleId, mailKeyId);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MailServlet sendMail : parames {},desc{}", keyId, UcCodeEnum.SYS_ERROR.getDesc());
            logger.error(e.toString());
            return new Message(UcCodeEnum.SYS_ERROR);
        }
        return new Message(UcCodeEnum.SUCCESS, String.valueOf(keyId));
    }


    @ResponseBody
    @RequestMapping(value = "/sendContent", method = RequestMethod.POST)
    public Message sendContent(String params) {
        long keyId = 0;
        logger.info("MailServlet sendContent : parames {}", params);
        try {

            if (!StringUtils.isNotEmpty(params)) {
                logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }
            JSONObject jsonParams = JSONObject.parseObject(params);
            PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
            MailManager mailManager = SpringUtil.getBean(MailManager.class);

            Player player = null;
            if (jsonParams.getLong("roleId") != null) {
                player = playerManager.getPlayer(jsonParams.getLong("roleId"));
            }

            if (player == null) {
                logger.error("MailServlet roleGmComm : parames {},desc{}", params, UcCodeEnum.ACCOUNT_NOT_EXIST.getDesc());
                return new Message(UcCodeEnum.ACCOUNT_NOT_EXIST);
            }

            Mail nomalMail = mailManager.addMail(player, MailId.FEEDBACK_MAIL, jsonParams.getString("titleContent"), jsonParams.getString("content"));
            if (nomalMail != null) {
                nomalMail.setAwardGot(2);
                if (player.isLogin) {
                    playerManager.synMailToPlayer(player, nomalMail);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MailServlet sendMail : parames {},desc{}", params, UcCodeEnum.SYS_ERROR.getDesc());
            logger.error(e.toString());
            return new Message(UcCodeEnum.SYS_ERROR);
        }
        return new Message(UcCodeEnum.SUCCESS, String.valueOf(keyId));
    }

    @ResponseBody
    @RequestMapping(value = "/recallMail", method = RequestMethod.POST)
    public Message recallMail(String keyId, String roleId) {
        MailManager mailManager = SpringUtil.getBean(MailManager.class);
        SendMail findServerMail = mailManager.findServerMail(Long.parseLong(keyId));
        if (null != findServerMail) {
            mailManager.deleteSeverMail(Long.parseLong(keyId));
            PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
            //查找所有有这个邮件的玩家
            Map<Long, Player> allPlayer = playerManager.getAllPlayer();
            allPlayer.forEach((e, f) -> {
                recallPlayerMail(mailManager, f.roleId, Long.parseLong(keyId));
            });
        } else {
            //删除玩家的邮件
            recallPlayerMail(mailManager, Long.parseLong(roleId), Long.parseLong(keyId));
        }
        return new Message(UcCodeEnum.SUCCESS, String.valueOf(keyId));
    }


    private void recallPlayerMail(MailManager mailManager, Long roldId, Long mailId) {
        SendMail nomalMail = mailManager.findMailByKey(mailId);
        if (nomalMail != null) {
            PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
            Player player = playerManager.getPlayer(roldId);
            Mail mail = player.recallMail(nomalMail.getKeyId().intValue());
            if (player.isLogin && mail != null) {
                MailPb.SynRecallMailRs.Builder builder = MailPb.SynRecallMailRs.newBuilder();
                builder.setRecallMail(mail.getKeyId());
                //推送在线玩家邮件删除信息
                BasePb.Base.Builder msg = PbHelper.createSynBase(MailPb.SynRecallMailRs.EXT_FIELD_NUMBER, MailPb.SynRecallMailRs.ext, builder.build());
                GameServer.getInstance().sendMsgToPlayer(player, msg);
            }
        }
    }
}
