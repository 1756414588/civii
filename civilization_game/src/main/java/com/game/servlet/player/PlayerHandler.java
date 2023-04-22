package com.game.servlet.player;

import com.game.constant.UcCodeEnum;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.manager.*;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Chat;
import com.game.service.AccountService;
import com.game.spring.SpringUtil;
import com.game.uc.Message;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * @author jyb
 * @date 2020/6/2 17:51
 * @description
 */
@Controller
public class PlayerHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @ResponseBody
    @RequestMapping(value = "/player/closeRole.do", method = RequestMethod.POST)
    public Message closeRole(long roleId) {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        Player player = playerManager.getPlayer(roleId);
        if (player == null) {
            return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
        }
        // 设置封号
        player.account.setForbid(1);
        if (player.getChannelId() != -1) {
            AccountService accountService = SpringUtil.getBean(AccountService.class);
            accountService.synOffline(player, 3);
        }
        logger.info("PlayerHandler closeRole roleId {} playerName {} 已经被封号", roleId, player.getLord().getNick());
        deleteViolationChat(player);
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/closeSpeak.do", method = RequestMethod.POST)
    public Message closeSpeak(long roleId, long endTime) {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        Player player = playerManager.getPlayer(roleId);
        if (player == null) {
            return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
        }
        player.account.setCloseSpeakTime(endTime);
        logger.info("PlayerHandler closeRole roleId {} playerName {} 已经被禁言 时间 {}", roleId, player.getLord().getNick(), TimeHelper.getFormatData(new Date(endTime)));
        deleteViolationChat(player);
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/openSpeak.do", method = RequestMethod.POST)
    public Message openSpeak(long roleId) {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        Player player = playerManager.getPlayer(roleId);
        if (player == null) {
            return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
        }
        player.account.setCloseSpeakTime(0);
        return new Message(UcCodeEnum.SUCCESS);
    }

    /**
     * 修改角色昵称接口
     *
     * @param roleId
     * @param newNick
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/player/updateNick.do", method = RequestMethod.POST)
    public Message updateNick(Long roleId, String newNick) {

        try {
            if (null == roleId || null == newNick) {
                logger.error("PlayerHandler updateNick : desc : {}", UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
            Player player = playerManager.getPlayer(roleId);
            if (player == null) {
                logger.error("PlayerHandler updateNick : desc : {}", UcCodeEnum.PLAYER_IS_NOT_EXIST.getDesc());
                return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
            }

            SpringUtil.getBean(NickManager.class).setPlayerNick(player.getLord(), newNick);
        } catch (Exception e) {
            logger.error("PlayerHandler updateNick : desc : {}", UcCodeEnum.SYS_ERROR.getDesc());
            e.printStackTrace();
            return new Message(UcCodeEnum.SYS_ERROR);
        }
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/openRole.do", method = RequestMethod.POST)
    public Message openRole(long roleId) {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        Player player = playerManager.getPlayer(roleId);
        if (player == null) {
            return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
        }
        // 设置解封
        player.account.setForbid(0);
        logger.info("PlayerHandler closeRole roleId {} playerName {} 已经被封号", roleId, player.getLord().getNick());
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/refeshBattleRank.do", method = RequestMethod.POST)
    public Message refeshBattleRank() {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        HeroManager heroManager = SpringUtil.getBean(HeroManager.class);
        BuildingManager buildingManager = SpringUtil.getBean(BuildingManager.class);
        RankManager rankManager = SpringUtil.getBean(RankManager.class);
        Player p = playerManager.getPlayer("白老板帅呆了");
        // System.out.println("rest:" + p);
        if (p != null) {
            p.getPlayerDailyTask().reset();
            p.getSimpleData().setBigMonsterReward(0);
            p.getSimpleData().setFirstBigMonsterReward(false);
            // System.out.println("restPlayer:" + p.roleId);
        }
        return new Message(UcCodeEnum.SUCCESS);
    }

    // 删除聊天列表中被封禁玩家 或者禁言玩家的聊天信息
    public void deleteViolationChat(Player player) {
        ChatManager chatManager = SpringUtil.getBean(ChatManager.class);
        ConcurrentLinkedDeque<Chat> worlds = chatManager.getWorld();
        Iterator<Chat> iterator = worlds.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getLordId() == player.roleId) {
                iterator.remove();
            }
        }

        ConcurrentLinkedDeque<Chat> chats = chatManager.getCountrys().get(player.getCountry());
        if (chats != null) {
            Iterator<Chat> iterator1 = chats.iterator();
            while (iterator1.hasNext()) {
                if (iterator1.next().getLordId() == player.roleId) {
                    iterator1.remove();
                }
            }
        }

        for (Integer mapId : SpringUtil.getBean(StaticWorldMgr.class).getWorldMap().keySet()) {
            ConcurrentLinkedDeque<CommonPb.Chat> mapChatList = chatManager.getMapChat().get(mapId);
            if (mapChatList != null && !mapChatList.isEmpty()) {
                Iterator<Chat> iterator2 = mapChatList.iterator();
                while (iterator2.hasNext()) {
                    if (iterator2.next().getLordId() == player.roleId) {
                        iterator2.remove();
                    }
                }
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/player/calculateBattleScore.do", method = RequestMethod.POST)
    public Message calculateBattleScore(long roleId) {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        Player player = playerManager.getPlayer(roleId);
        if (player == null) {
            return new Message(UcCodeEnum.PLAYER_IS_NOT_EXIST);
        }
        if (player.isLogin()) {
            return new Message(UcCodeEnum.PLAYER_ONLINE);
        }
        HeroManager heroManager = SpringUtil.getBean(HeroManager.class);
        // 计算玩家战斗力 上阵武将 + 建筑战斗力
        int battleScore = heroManager.caculateBattleScore(player);
        // 向玩家推送武将列表
        heroManager.checkHeroList(player, player.getAllHeroList());
        // 返回玩家战斗力
        return new Message(String.valueOf(battleScore));
    }

    @ResponseBody
    @RequestMapping(value = "/player/playerExist.do", method = RequestMethod.POST)
    public Message playerExist(int serverId, int accountKey) {
        LogHelper.GAME_LOGGER.info("playerExist serverId:{} accountKey:{}", serverId, accountKey);

        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);

        Account account = playerManager.getAccount(serverId, accountKey);
        if (account == null) {
            return new Message(String.valueOf(-1));
        }

        return new Message(String.valueOf(account.getCreated()));
    }
}
