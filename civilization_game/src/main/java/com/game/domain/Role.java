package com.game.domain;

import com.game.spring.SpringUtil;
import java.util.List;

import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.p.Detail;
import com.game.domain.p.Lord;
import com.game.domain.p.Mail;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Role {
    private long roleId;
    private Lord lord;
    private Detail detail;
    private Player player;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public Lord getLord() {
        return lord;
    }

    public void setLord(Lord lord) {
        this.lord = lord;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Role(Player player) {
        getMailCount(player);
        roleId = player.roleId;
        detail = player.serDetail();
        setPlayer(player);
        List<Integer> payStatus = player.getLord().getPayStatusList();
        if (null != payStatus && payStatus.size() > 0) {
            StringBuilder payStatusStr = new StringBuilder();
            payStatusStr.append("[");
            for (Integer integer : payStatus) {
                if (payStatus.indexOf(integer) == payStatus.size() - 1) {
                    payStatusStr.append(integer);
                } else {
                    payStatusStr.append(integer);
                    payStatusStr.append(",");
                }
            }
            payStatusStr.append("]");
            player.getLord().setPayStatus(payStatusStr.toString());
        }
        lord = (Lord) player.getLord().clone();

    }


    public void getMailCount(Player player) {//战斗邮件大于100时删除多余邮件
        try {
            ConcurrentLinkedDeque<Mail> mails = player.getMails();
            StaticLimitMgr staticLimitMgr = SpringUtil.getBean(StaticLimitMgr.class);
            int maxnum = staticLimitMgr.getNum(240);//最大邮件数量
            List<Integer> list = staticLimitMgr.getAddtion(239);

            if (null != mails && mails.size() > 0 && null != list && list.size() > 0 && maxnum > 0) {
                int tempNum = 0; //计入战斗邮件数量
                Iterator<Mail> itMail = mails.iterator();
                while (itMail.hasNext()) {
                    Mail mail = itMail.next();
                    for (int i = 0; i < list.size(); i++) {
                        if (mail.getMailId() == list.get(i)) {
                            tempNum++;
                        }

                        if (tempNum > maxnum) {
                            itMail.remove();
                            tempNum--;
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
