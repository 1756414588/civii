package com.game.manager;

import com.game.server.exec.LoginExecutor;
import com.game.spring.SpringUtil;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.game.constant.MailId;
import com.game.service.WarningService;
import com.game.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.game.constant.MailConst;
import com.game.dao.p.SendMailDao;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMailDataMgr;
import com.game.domain.Player;
import com.game.domain.p.Mail;
import com.game.domain.s.StaticMail;
import com.game.pb.CommonPb;
import com.game.servlet.domain.MailType;
import com.game.servlet.domain.SendMail;
import com.game.util.LogHelper;

@Component
public class MailManager {

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticMailDataMgr staticMailDataMgr;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private SendMailDao sendMailMapper;
    @Autowired
    private WarningService warningService;

    private ConcurrentHashMap<Long, SendMail> allServerMail = new ConcurrentHashMap<Long, SendMail>();

    // 最近一次更新时间
    public long lastSaveTime = System.currentTimeMillis();

    /**
     * 给玩家添加邮件
     *
     * @param player
     * @param mailId
     * @param param
     * @return
     */
    public Mail addMail(Player player, int mailId, String... param) {
        StaticMail staticMail = staticMailDataMgr.getStaticMail(mailId);
        if (staticMail == null) {
            LogHelper.CONFIG_LOGGER.error("mail id = " + mailId + " is not exists, check it!");
            return null;
        }

        int type = staticMail.getType();
        long lordId = player.getLord().getLordId();
        Mail mail = new Mail(player.maxKey(), lordId, type, mailId, 0, System.currentTimeMillis());
        if (param != null && param.length != 0) {
            mail.setParam(param);

            int[] title = staticMailDataMgr.copyTitleParam(staticMail);
            if (title != null && title.length <= param.length) {
                mail.setTitle(title);
            }
        }

        // 战报邮件
        if (type == MailConst.REPORT_MAIL) {
            int limie = staticLimitMgr.getNum(165);
            int length = player.getRepots().size();
            if (length >= limie) {
                Integer delId = player.getRepots().poll();
                if (delId != null) {
                    player.removeMail(delId);
                }
            }
        } else if (type == MailConst.PERSON_MAIL) { // 私人邮件
            int limie = staticLimitMgr.getNum(166);
            int length = player.getPmails().size();
            if (length >= limie) {
                Integer delId = player.getPmails().poll();
                if (delId != null) {
                    player.removeMail(delId);
                }
            }
        }
        //活动奖励邮件
        if (mailId == MailId.ACTIVITY_MAIL_AWARD) {
            //找到所有1分钟内发过的活动奖励邮件
            List<Mail> activityMails = player.getMails().stream().filter(e -> {
                return player.getMailIds().contains(e.getKeyId());
            }).collect(Collectors.toList());
            if(param!=null && param.length>0){
                for (Mail m : activityMails) {
                    //活动名称相同
                    if (m.getParam()[0] == param[0]) {
                        //return掉 不给玩家奖励
                        SpringUtil.getBean(LoginExecutor.class).add(() -> {
                            //异步发送邮件预警
                            warningService.sendMail(param[0],mail);
                        });
                        return mail;
                    }
                }
            }
            player.getMailIds().add(mail.getKeyId());
        }

        player.addMail(mail);

        return mail;
    }

    /**
     * 添加附件到邮件
     *
     * @param mail
     * @param award
     */
    public void addMailAward(Mail mail, List<CommonPb.Award> award) {
        if (mail == null || award == null || award.isEmpty()) {
            return;
        }
        mail.setAward(award);
        mail.setAwardGot(0);
    }

    /**
     * 获取用户邮件
     *
     * @param lordId
     * @param keyId
     * @return
     */
    public Mail getTargetMail(long lordId, int keyId) {
        Player player = playerManager.getPlayer(lordId);
        if (player != null) {
            return player.getMail(keyId);
        }
        return null;
    }

    public StaticMail isShare(int mailId) {
        StaticMail staticMail = staticMailDataMgr.getStaticMail(mailId);
        if (staticMail == null) {
            return null;
        }
        if (staticMail.getShare() == 0) {
            return null;
        }
        return staticMail;
    }

    public void mailSet(Player player) {
        Iterator<Mail> it = player.getMails().iterator();
        while (it.hasNext()) {
            Mail mail = it.next();
            if (mail == null) {
                continue;
            }

            StaticMail staticMail = staticMailDataMgr.getStaticMail(mail.getMailId());
            if (staticMail == null) {
                continue;
            }
            if (staticMail.getType() == MailConst.REPORT_MAIL) {
                player.getRepots().add(mail.getKeyId());
            } else if (staticMail.getType() == MailConst.SYS_MAIL) {
                player.getSysmail().add(mail.getKeyId());
            } else if (staticMail.getType() == MailConst.PERSON_MAIL) {
                player.getPmails().add(mail.getKeyId());
            }
        }
    }

    @PostConstruct
    public void addAllServerMail() {
        List<SendMail> selectAllServerMail = sendMailMapper.selectAllServerMail();
        if (!CollectionUtils.isEmpty(selectAllServerMail)) {
            for (SendMail sendMail : selectAllServerMail) {
                if (!StringUtil.isNullOrEmpty(sendMail.getChannelList())) {
                    sendMail.setChannel(StringUtil.stringToList(sendMail.getChannelList()));
                }
                allServerMail.put(sendMail.getKeyId(), sendMail);
            }
        }
    }

    public ConcurrentHashMap<Long, SendMail> selectAllServerMail() {
        return allServerMail;
    }

    public Long addServerMail(SendMail sendMail) {
        sendMail.setCreateTime(new Date());
        if (sendMailMapper.insertSelective(sendMail) > 0) {
            if (sendMail.getType().intValue() == MailType.NATIVE_SERVER || sendMail.getType().intValue() == MailType.ALL_SERVER) {
                allServerMail.put(sendMail.getKeyId(), sendMail);
            }
        }
        return sendMail.getKeyId();
    }

    public void updateServerMail(SendMail sendMail) {
        sendMail.setUpdateTime(new Date());
        sendMailMapper.updateByPrimaryKeySelective(sendMail);
    }

    public SendMail findServerMail(Long keyId) {
        return allServerMail.get(keyId);
    }

    public void deleteSeverMail(Long keyId) {
        SendMail sendMail = allServerMail.get(keyId);
        if (null != sendMail) {
            sendMail.setRemove(SendMail.HAVE_REMOVE);
            sendMail.setDeleteTime(new Date());
        }
    }

    public SendMail findMailByKey(Long keyId) {
        return sendMailMapper.selectByPrimaryKey(keyId);
    }


    public String mailToString(Mail mail) {
        StaticMail staticMail = staticMailDataMgr.getStaticMail(mail.getMailId());
        String content = staticMail.getHead();
        String[] contentArr = content.split("%s");
        String[] parm = mail.getParam();
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < contentArr.length; index++) {
            builder.append(contentArr[index]);
            if (index < contentArr.length - 1) {
                if (parm == null || index >= parm.length) {
                    continue;
                }
                builder.append(parm[index]);
            }
        }
        for (CommonPb.Award award : mail.getAward()) {
            builder.append("奖励:");
            builder.append(award.getType()).append(":");
            builder.append(award.getId()).append(":");
            builder.append(award.getCount());
        }

        return builder.toString();
    }
}
