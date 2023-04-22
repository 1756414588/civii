package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticFriendMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.Player;
import com.game.domain.p.Friend;
import com.game.domain.p.MasterShop;
import com.game.domain.s.StaticApprenticeAward;
import com.game.domain.s.StaticFriendshipScoreShop;
import com.game.domain.s.StaticMentorAward;
import com.game.log.consumer.EventManager;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.FriendPb;
import com.game.pb.FriendPb.*;
import com.game.pb.FriendPb.ProcessAllRs.Builder;
import com.game.spring.SpringUtil;
import com.game.util.*;
import com.game.worldmap.*;
import com.google.common.collect.Lists;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 好友&师徒 1. 黑名单?
 */
@Service
public class FriendService {

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private StaticFriendMgr staticFriendMgr;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private WorldManager worldManager;

    /**
     * 好友列表
     */
    public void friendList(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int limitLv = getLimitLv(FriendLimit.OPEN_LV);
        if (limitLv == -1) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (player.getLevel() < limitLv) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        FriendListRs.Builder builder = FriendListRs.newBuilder();
        for (Map.Entry<Integer, List<Player>> playerEntry : getFriendList(player).entrySet()) {
            for (Player target : playerEntry.getValue()) {
                if (target == null) {
                    continue;
                }
                // 申请列表
                builder.addFriendList(PbHelper.createGood(target, playerEntry.getKey()));
            }
        }
        handler.sendMsgToPlayer(FriendListRs.ext, builder.build());
    }

    /**
     * 申请好友(发起)
     *
     * @param
     * @param
     */
    public void applyFriend(ApplyFriendRq req, ClientHandler handler) {
        int friendType = req.getFriendType();
        Player player = playerManager.getPlayer(handler.getRoleId());
        Player target = playerManager.getPlayer(req.getLordId());
        if (player == null || target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (player.roleId.longValue() == target.roleId.longValue()) {
            handler.sendErrorMsgToPlayer(GameError.IS_SELF);
            return;
        }
        int limitLv = getLimitLv(FriendLimit.OPEN_LV);
        if (limitLv == -1) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (player.getLevel() < limitLv) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        if (doApply(player, target, handler, friendType) == -1) {
            return;
        }

        ApplyFriendRs.Builder builder = ApplyFriendRs.newBuilder();
        builder.setResult(true);
        handler.sendMsgToPlayer(ApplyFriendRs.ext, builder.build());

        FriendPb.SynApplyMsgRs.Builder synApplyMsg = SynApplyMsgRs.newBuilder();
        synApplyMsg.setPlayer(PbHelper.createGood(player, friendType));
        SynHelper.synMsgToPlayer(target, SynApplyMsgRs.EXT_FIELD_NUMBER, SynApplyMsgRs.ext, synApplyMsg.build());
        SpringUtil.getBean(EventManager.class).ask_for_being_friends(player, Lists.newArrayList(target.roleId, target.getLevel(), "test"));
    }

    /**
     * 快速申请好友(发起)
     *
     * @param
     * @param
     */
    public void fastApplyFriend(FastApplyFriendRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        Player target = playerManager.getPlayer(req.getNick());
        if (player == null || target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (player.roleId.longValue() == target.roleId.longValue()) {
            handler.sendErrorMsgToPlayer(GameError.IS_SELF);
            return;
        }
        if (player.getLevel() < 20) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }
        if (doApply(player, target, handler, FriendType.APPLY) == -1) {
            return;
        }

        FastApplyFriendRs.Builder builder = FastApplyFriendRs.newBuilder();
        handler.sendMsgToPlayer(FastApplyFriendRs.ext, builder.build());

        FriendPb.SynApplyMsgRs.Builder synApplyMsg = SynApplyMsgRs.newBuilder();
        synApplyMsg.setPlayer(PbHelper.createGood(player, FriendType.APPLY));
        SynHelper.synMsgToPlayer(target, SynApplyMsgRs.EXT_FIELD_NUMBER, SynApplyMsgRs.ext, synApplyMsg.build());
    }

    private int doApply(Player player, Player target, ClientHandler handler, int friendType) {
        Map<Long, Friend> playerApply = getApply(player);
        Map<Long, Friend> targetApply = getApply(target);
        Map<Long, Friend> playerApprentice = getApprentice(player);
        Map<Long, Friend> targetApprentice = getApprentice(target);
        Map<Long, Friend> playerMaster = getMaster(player);
        Map<Long, Friend> targetMaster = getMaster(target);
        Map<Long, Friend> playerFriend = getFriend(player);
        Map<Long, Friend> targetFriend = getFriend(target);

        // 已经存在师徒关系
        if (friendType == FriendType.APPLY_MASTER) {
            if (player.getCountry() != target.getCountry()) {
                handler.sendErrorMsgToPlayer(GameError.NOT_SAME_COUNTRY);
                return -1;
            }
            if (playerApprentice.containsKey(target.roleId) || playerMaster.containsKey(target.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.MASTER_AND_APPRENTICE_REPEAT);
                return -1;
            }
            if (player.getSimpleData().getNextHaveMasterTime() > TimeHelper.curentTime()) {
                handler.sendErrorMsgToPlayer(GameError.FRI_APPLY_LOW_TIME);
                return -1;
            }
            // 多少天后可再次请求拜师
            Long aLong = target.getSimpleData().getApplyMasterRefuse().get(player.roleId);
            if (aLong != null) {
                int equation = TimeHelper.equation(aLong, System.currentTimeMillis());
                if (equation < 15) {
                    ApplyFriendRs.Builder builder = ApplyFriendRs.newBuilder();
                    builder.setResult(false);
                    builder.setLimitDays(15 - equation);
                    handler.sendMsgToPlayer(ApplyFriendRs.ext, builder.build());
                    return -1;
                } else {
                    target.getSimpleData().getApplyMasterRefuse().remove(player.roleId);
                }
            }
            if (getApplyMaster(target).containsKey(player.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.APPLY_MASTER_REPEAT);
                return -1;
            }
            int limitLv = getLimitLv(FriendLimit.OPEN_LV);
            int masterLvMax = getLimitLv(FriendLimit.GET_MASTER_LV);
            int apprenticeLvMin = getLimitLv(FriendLimit.GET_APPRENTICE_LV);
            int apprenticeCountMax = getLimitLv(FriendLimit.APPRENTICE_MAX);
            if (limitLv == -1 || masterLvMax == -1 || apprenticeLvMin == -1 || apprenticeCountMax == -1) {
                handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
                return -1;
            }

            if (player.getLevel() < limitLv) {
                handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
                return -1;
            }
            if (player.getLevel() > masterLvMax) {// 超过拜师等级
                handler.sendErrorMsgToPlayer(GameError.APPRENTICE_LV_NOT_ENOUGH);
                return -1;
            }
            if (target.getLevel() < apprenticeLvMin) {// 对方没达到收徒等级
                handler.sendErrorMsgToPlayer(GameError.TARGET_LORD_LV_NOT_ENOUGH);
                return -1;
            }

            if (target.getFriends().computeIfAbsent(FriendType.APPRENTICE, x -> new HashMap<>()).size() == apprenticeCountMax) {
                handler.sendErrorMsgToPlayer(GameError.TARGET_APPRENTICE_NUM_MAX);
                return -1;
            }

            if (playerMaster.size() > 0) {// 已有师父
                handler.sendErrorMsgToPlayer(GameError.ALREADY_HAVE_MASTER);
                return -1;
            }

            if (!playerFriend.containsKey(target.roleId.longValue())) {
                // 好友数量是否已到达上限
                int friendMax = getLimitLv(FriendLimit.FRIEND_MAX);
                if (friendMax == -1) {
                    handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
                    return -1;
                }
                if (playerFriend.size() + playerMaster.size() + playerApprentice.size() >= friendMax) {
                    handler.sendErrorMsgToPlayer(GameError.PLAYER_FIREND_NUM_MAX);
                    return -1;
                }
                if (targetFriend.size() + targetMaster.size() + targetApprentice.size() >= friendMax) {
                    handler.sendErrorMsgToPlayer(GameError.TARGET_FIREND_NUM_MAX);
                    return -1;
                }
            }
            getApplyMaster(target).put(player.roleId, new Friend(player.getLevel(), FriendType.APPLY_MASTER, player.roleId, new Date().getTime()));
        } else {
            // 双方是否存在对方的黑名单中
            // 双方是否存在对方的好友列表中
            if (playerFriend.containsKey(target.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.ALREADY_FRIEND);
                return -1;
            }
            // 双方是否存在对方的好友列表中
            if (playerApprentice.containsKey(target.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.ALREADY_APPRENTICE);
                return -1;
            }
            // 双方是否存在对方的好友列表中
            if (playerMaster.containsKey(target.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.ALREADY_MASTER);
                return -1;
            }

            // 好友数量是否已到达上限
            int limitLv = getLimitLv(FriendLimit.FRIEND_MAX);
            if (limitLv == -1) {
                handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
                return -1;
            }
            if (playerFriend.size() + playerMaster.size() + playerApprentice.size() >= limitLv) {
                handler.sendErrorMsgToPlayer(GameError.PLAYER_FIREND_NUM_MAX);
                return -1;
            }
            if (targetFriend.size() + targetMaster.size() + targetApprentice.size() >= limitLv) {
                handler.sendErrorMsgToPlayer(GameError.TARGET_FIREND_NUM_MAX);
                return -1;
            }

            // 双方是否存在对方的申请列表中
            if (playerApply.containsKey(target.roleId)) {
                // 对方申请过直同意
                playerApply.remove(target.roleId);
                playerFriend.put(target.roleId, new Friend(target.getLevel(), FriendType.FRIEND, target.roleId, new Date().getTime()));
                targetFriend.put(player.roleId, new Friend(player.getLevel(), FriendType.FRIEND, player.roleId, new Date().getTime()));
                return -1;
            }

            targetApply.put(player.roleId, new Friend(player.getLevel(), FriendType.APPLY, player.roleId, new Date().getTime()));
        }
        return 1;
    }

    /**
     * 同意/拒绝 申请
     */
    public void addFriend(AddFriendRq req, ClientHandler handler) {
        int friendType = req.getFriendType();
        Player player = playerManager.getPlayer(handler.getRoleId());
        Player target = playerManager.getPlayer(req.getLordId());
        if (player == null || target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 拜师申请
        if (friendType == FriendType.APPLY_MASTER) {
            GameError addapprentic = addApprentice(req.getAdd(), player, target);
            if (addapprentic == GameError.OK) {
                AddFriendRq.Builder builder = AddFriendRq.newBuilder();
                handler.sendMsgToPlayer(AddFriendRq.ext, builder.build());
                synFriendListRs(player);
                synFriendListRs(target);
            } else {
                handler.sendErrorMsgToPlayer(addapprentic);
            }
            return;
        }
        Map<Long, Friend> playerApply = getApply(player);
        Map<Long, Friend> playerApprentice = getApprentice(player);
        Map<Long, Friend> targetApprentice = getApprentice(target);
        Map<Long, Friend> playerMaster = getMaster(player);
        Map<Long, Friend> targetMaster = getMaster(target);
        Map<Long, Friend> playerFriend = getFriend(player);
        Map<Long, Friend> targetFriend = getFriend(target);
        if (player.roleId.longValue() == target.roleId.longValue()) {
            handler.sendErrorMsgToPlayer(GameError.IS_SELF);
            playerApply.remove(target.roleId);
            return;
        }
        // 双方是否存在对方的黑名单中

        // 双方是否存在对方的好友列表中
        if (playerFriend.containsKey(target.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_FRIEND);
            playerApply.remove(target.roleId);
            return;
        }
        // 双方是否存在对方的好友列表中
        if (playerApprentice.containsKey(target.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_APPRENTICE);
            playerApply.remove(target.roleId);
            return;
        }
        // 双方是否存在对方的好友列表中
        if (playerMaster.containsKey(target.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_MASTER);
            playerApply.remove(target.roleId);
            return;
        }
        int limitLv = getLimitLv(FriendLimit.FRIEND_MAX);
        if (limitLv == -1) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (req.getAdd() == 1) {// 同意
            // 好友数量是否已到达上限
            if (playerFriend.size() + playerMaster.size() + playerApprentice.size() >= limitLv) {
                handler.sendErrorMsgToPlayer(GameError.PLAYER_FIREND_NUM_MAX);
                return;
            }
            if (targetFriend.size() + targetMaster.size() + targetApprentice.size() >= limitLv) {
                handler.sendErrorMsgToPlayer(GameError.TARGET_FIREND_NUM_MAX);
                return;
            }

            // 是否在申请列表中
            if (!playerApply.containsKey(target.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.NOT_IN_APPLY_LIST);
                return;
            }
            playerApply.remove(target.roleId);
            playerFriend.put(target.roleId, new Friend(target.getLevel(), FriendType.FRIEND, target.roleId, new Date().getTime()));
            targetFriend.put(player.roleId, new Friend(player.getLevel(), FriendType.FRIEND, player.roleId, new Date().getTime()));
            FriendPb.SynApplyMsgRs.Builder synApplyMsg = SynApplyMsgRs.newBuilder();
            synApplyMsg.setPlayer(PbHelper.createGood(player, FriendType.FRIEND));
            SynHelper.synMsgToPlayer(target, SynApplyMsgRs.EXT_FIELD_NUMBER, SynApplyMsgRs.ext, synApplyMsg.build());
            SpringUtil.getBean(EventManager.class).allow_friends_ask(player, Lists.newArrayList(target.roleId, target.getLevel(), "test"));

            achievementService.addAndUpdate(player,AchiType.AT_63,playerFriend.size());
        } else {
            playerApply.remove(target.roleId);
        }
        AddFriendRs.Builder builder = AddFriendRs.newBuilder();
        handler.sendMsgToPlayer(AddFriendRs.ext, builder.build());
        synFriendListRs(player);
        synFriendListRs(target);
    }

    @Autowired
    AchievementService achievementService;
    /**
     * 删除好友
     *
     * @param
     * @param
     */
    public void removeFriend(RemoveFriendRq req, ClientHandler handler) {
        // 双方是否存在对方的好友列表中
        Player player = playerManager.getPlayer(handler.getRoleId());
        Player target = playerManager.getPlayer(req.getLordId());
        if (player == null || target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (player.roleId.longValue() == target.roleId.longValue()) {
            handler.sendErrorMsgToPlayer(GameError.IS_SELF);
//            return;
        }
        Map<Long, Friend> playerFriend = getFriend(player);
        Map<Long, Friend> targetFriend = getFriend(target);

        // 双方是否存在对方的黑名单中

        // 双方是否存在对方的好友列表中
        if (!playerFriend.containsKey(target.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.NOT_IN_FRIEND_LIST);
            return;
        }
        playerFriend.remove(target.roleId);
        targetFriend.remove(player.roleId);
        RemoveFriendRs.Builder builder = RemoveFriendRs.newBuilder();
        handler.sendMsgToPlayer(RemoveFriendRs.ext, builder.build());
        synFriendListRs(player);
        synFriendListRs(target);
    }

    /**
     * 查找好友
     */
    public void searchFriend(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (staticLimitMgr.getAddtion(232) == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        int max = player.getLevel() + staticLimitMgr.getAddtion(232).get(1);
        int min = player.getLevel() - staticLimitMgr.getAddtion(232).get(0);
        SearchFriendRs.Builder builder = SearchFriendRs.newBuilder();
        List<Player> strangerList = getStranger(player, max, min);
        if (strangerList.size() < 5 || strangerList.size() > 45) {
            player.getStranger().clear();
            strangerList = getStranger(player, max, min);
        }
        for (Player stranger : strangerList) {
            player.getStranger().add(stranger.roleId);
            builder.addPlayer(PbHelper.createGood(stranger, FriendType.STRANGER));
        }
        handler.sendMsgToPlayer(SearchFriendRs.ext, builder.build());
    }

    /**
     * 拜师同意
     *
     * @param
     * @return
     */
    public GameError addApprentice(int add, Player masterPlay, Player apprenticePlay) {
        if (add == 1) {
            if (masterPlay == null || apprenticePlay == null) {
                return GameError.PLAYER_NOT_EXIST;
            }
            if (apprenticePlay.roleId.longValue() == masterPlay.roleId.longValue()) {
                return GameError.IS_SELF;
            }
            if (apprenticePlay.getCountry() != masterPlay.getCountry()) {
                return GameError.NOT_SAME_COUNTRY;
            }
            Map<Long, Friend> targetApprentice = getApprentice(masterPlay);
            Map<Long, Friend> targetMaster = getMaster(masterPlay);
            if (targetMaster.containsKey(apprenticePlay.roleId) || targetApprentice.containsKey(apprenticePlay.roleId)) {
                return GameError.MASTER_AND_APPRENTICE_REPEAT;
            }
            Map<Long, Friend> playerMaster = getMaster(apprenticePlay);
            int limitLv = getLimitLv(FriendLimit.OPEN_LV);
            int masterLvMax = getLimitLv(FriendLimit.GET_MASTER_LV);
            int apprenticeLvMin = getLimitLv(FriendLimit.GET_APPRENTICE_LV);
            int apprenticeCountMax = getLimitLv(FriendLimit.APPRENTICE_MAX);
            if (limitLv == -1 || masterLvMax == -1 || apprenticeLvMin == -1 || apprenticeCountMax == -1) {
                return GameError.NO_CONFIG;
            }
            if (masterPlay.getLevel() < limitLv) {
                return GameError.LORD_LV_NOT_ENOUGH;
            }
            if (apprenticePlay.getLevel() > masterLvMax) {// 徒弟超过拜师等级
                return GameError.APPRENTICE_LV_NOT_ENOUGH;
            }
            if (masterPlay.getLevel() < apprenticeLvMin) {// 师傅没达到收徒等级
                return GameError.TARGET_LORD_LV_NOT_ENOUGH;
            }
            if (playerMaster.size() > 0) {// 已有师父
                return GameError.ALREADY_HAVE_MASTER;
            }
            if (targetApprentice.size() >= apprenticeCountMax) {// 徒弟数量上限
                return GameError.TARGET_APPRENTICE_NUM_MAX;
            }
            getFriend(masterPlay).remove(apprenticePlay.roleId);
            getFriend(apprenticePlay).remove(masterPlay.roleId);
            playerMaster.put(masterPlay.roleId, new Friend(masterPlay.getLevel(), FriendType.MASTER, masterPlay.roleId, new Date().getTime()));
            targetApprentice.put(apprenticePlay.roleId, new Friend(apprenticePlay.getLevel(), FriendType.APPRENTICE, apprenticePlay.roleId, new Date().getTime()));

            Map<Long, Map<Integer, Integer>> friAward = masterPlay.getFriAward();
            Map<Integer, Integer> integerIntegerMap = friAward.computeIfAbsent(apprenticePlay.roleId, x -> new HashMap<>());
            Map<Integer, StaticApprenticeAward> appMap = staticFriendMgr.getApprenticeAward();// 配置积分商城数据.
            appMap.values().forEach(x -> {
                Integer integer = integerIntegerMap.get(x.getCond());
                if (integer == null && apprenticePlay.getLevel() >= x.getCond()) {
                    integerIntegerMap.put(x.getCond(), 1);
                }
            });

            FriendPb.SynApplyMsgRs.Builder synApplyMsg = SynApplyMsgRs.newBuilder();
            synApplyMsg.setPlayer(PbHelper.createGood(masterPlay, FriendType.APPRENTICE));
            SynHelper.synMsgToPlayer(apprenticePlay, SynApplyMsgRs.EXT_FIELD_NUMBER, SynApplyMsgRs.ext, synApplyMsg.build());
            // 第一次拜师
            if (apprenticePlay.getSimpleData().getIsHaveMaster() == 0) {
                List<CommonPb.Award> masterAward = new ArrayList<>(); // 拜师奖励, 给徒弟player
                List<CommonPb.Award> apprenticeAward = new ArrayList<>();// 收徒奖励, 给师父target
                Map<Integer, StaticMentorAward> awardMap = staticFriendMgr.getMenterAwards();
                if (awardMap == null || awardMap.size() == 0) {
                    return GameError.NO_CONFIG;
                }
                for (List<Integer> award : awardMap.get(MailId.MASTER_SUCESS).getAward()) {// 拜师奖励, 给徒弟player
                    masterAward.add(PbHelper.createAward(award.get(0), award.get(1), award.get(2)).build());
                }
                for (List<Integer> award : awardMap.get(MailId.APPRENTICE_SUCESS).getAward()) {// 收徒奖励, 给师父target
                    apprenticeAward.add(PbHelper.createAward(award.get(0), award.get(1), award.get(2)).build());
                }
                playerManager.sendAttachPbMail(apprenticePlay, masterAward, MailId.MASTER_SUCESS, masterPlay.getNick());// player 徒弟
                playerManager.sendAttachPbMail(masterPlay, apprenticeAward, MailId.APPRENTICE_SUCESS, apprenticePlay.getNick());// target 师父
            } else {
                // 判断玩家是否拜过目标玩家为师 如果是 则移除
                //getOnceApprentice(masterPlay).remove(apprenticePlay.roleId);
            }
            // 导师排行榜记录值更新
            activityManager.updActMentorScore(masterPlay);
            // 添加任务
            taskManager.doTask(TaskType.LEARN_FROM_TEACHER, apprenticePlay);
            taskManager.doTask(TaskType.RECRUIT_STUDENTS, masterPlay);
            SpringUtil.getBean(EventManager.class).get_teacher(apprenticePlay, Lists.newArrayList(apprenticePlay.roleId, apprenticePlay.getNick(), apprenticePlay.getLevel(), "test", "主动"));
            SpringUtil.getBean(EventManager.class).get_student(masterPlay, Lists.newArrayList(masterPlay.roleId, masterPlay.getNick(), masterPlay.getLevel(), "test", "被动"));
        } else {
            masterPlay.getSimpleData().getApplyMasterRefuse().put(apprenticePlay.roleId, System.currentTimeMillis());
        }
        // 清除申请
        getApplyMaster(masterPlay).remove(apprenticePlay.roleId);
        return GameError.OK;
    }

    /**
     * 积分商店
     *
     * @param handler
     */
    public void masterShopAward(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {// 玩家不存在
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int limitLv = getLimitLv(FriendLimit.OPEN_LV);
        if (limitLv == -1) {// 没有配置
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (player.getLevel() < limitLv) {// 等级不足
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }
        MasterShopAwardRs.Builder builder = MasterShopAwardRs.newBuilder();
        Map<Integer, StaticFriendshipScoreShop> friendshipScoreShop = staticFriendMgr.getFriendshipScoreShop();// 配置积分商城数据
        if (friendshipScoreShop == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        MasterShop masterShop = player.getMasterShop();// 玩家自己的数据
        if (masterShop == null) {
            masterShop = new MasterShop();
        }
        Map<Integer, Integer> masterShopMap = masterShop.getMasterShopAward();
        if (masterShopMap == null) {
            masterShopMap = new HashMap<>();
        }
        for (StaticFriendshipScoreShop s : friendshipScoreShop.values()) {

            if (s.getMaxExchangeNum() != 0) {
                Integer count = masterShopMap.get(s.getId());
                if (count != null && count >= s.getMaxExchangeNum()) {
                    continue;
                }
            }
            if (player.getLevel() >= s.getOpenLevel()) {
                builder.addId(s.getId());
            }
        }
        builder.setScore(masterShop.getScore());
        handler.sendMsgToPlayer(MasterShopAwardRs.ext, builder.build());
    }

    /**
     * 积分商店领奖
     *
     * @param req
     * @param handler
     */
    public void getMasterShopAward(GetMasterShopAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {// 玩家不存在
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int limitLv = getLimitLv(FriendLimit.OPEN_LV);
        if (limitLv == -1) {// 没有配置
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (player.getLevel() < limitLv) {// 等级不足
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }
        Map<Integer, StaticFriendshipScoreShop> friendshipScoreShop = staticFriendMgr.getFriendshipScoreShop();// 配置积分商城数据
        if (friendshipScoreShop == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        StaticFriendshipScoreShop scoreShop = friendshipScoreShop.get(req.getId());
        if (scoreShop == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        List<Integer> award = scoreShop.getAward();
        if (award == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int type = award.get(0);
        int id = award.get(1);
        int count = award.get(2);

        // 玩家等级是否满足
        if (player.getLevel() < scoreShop.getNeedLevel()) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        // 玩家积分是否足够
        MasterShop masterShop = player.getMasterShop();// 玩家自己的数据
        if (masterShop == null) {
            masterShop = new MasterShop();
        }
        Map<Integer, Integer> masterShopMap = masterShop.getMasterShopAward();
        if (masterShopMap == null) {
            masterShopMap = new HashMap<>();
        }
        long score = masterShop.getScore();
        if (score < scoreShop.getNeedScore()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_FRIEND_SHOP_SCORE);
            return;
        }
        int pre = masterShopMap.get(req.getId()) == null ? 0 : masterShopMap.get(req.getId());
        // 兑换次数是否上限
        if (scoreShop.getMaxExchangeNum() != 0 && pre >= scoreShop.getMaxExchangeNum()) {
            handler.sendErrorMsgToPlayer(GameError.FRIEND_SHOP_EXCHANGE_NUM_NOT_ENOUGH);
            return;
        }
        masterShopMap.put(req.getId(), pre + 1);
        masterShop.setScore(score - scoreShop.getNeedScore());
        int keyId = playerManager.addAward(player, type, id, count, Reason.ADD_FRIEND_SHOP_AWARD);

        GetMasterShopAwardRs.Builder builder = GetMasterShopAwardRs.newBuilder();

        builder.setAward(PbHelper.createAward(player, type, id, count, keyId));
        handler.sendMsgToPlayer(GetMasterShopAwardRs.ext, builder.build());
    }

    /**
     * 师父奖励列表
     *
     * @param handler
     */
    public void masterAward(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {// 玩家不存在
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int limitLv = getLimitLv(FriendLimit.OPEN_LV);
        if (limitLv == -1) {// 没有配置
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (player.getLevel() < limitLv) {// 等级不足
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }
        Map<Integer, StaticApprenticeAward> apprenticeAward = staticFriendMgr.getApprenticeAward();// 配置积分商城数据
        if (apprenticeAward == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        MasterAwardRs.Builder builder = MasterAwardRs.newBuilder();
        Map<Integer, Integer> getAwardMap = player.getApprenticeAwardMap(); // 已领取数

        Map<Integer, Integer> apprenticeMap = new HashMap<>(); // 对应等级徒弟数
        for (StaticApprenticeAward s : apprenticeAward.values()) {
            if (s == null) {
                continue;
            }
            apprenticeMap.put(s.getCond(), 0);
            Map<Long, Map<Integer, Integer>> friAward = player.getFriAward();
            Collection<Map<Integer, Integer>> values = friAward.values();
            Iterator<Map<Integer, Integer>> iterator = values.iterator();
            while (iterator.hasNext()) {
                Map<Integer, Integer> next = iterator.next();
                Integer orDefault = next.getOrDefault(s.getCond(), 0);
                apprenticeMap.merge(s.getCond(), orDefault, (a, b) -> a + b);
            }
        }
        // List<Player> apprenticeList = getFriendList(player).computeIfAbsent(FriendType.APPRENTICE, x -> new ArrayList<>());
        // Map<Long, Friend> apprentices = getApprentice(player);
        // if (apprentices != null) {
        // for (Player apprentice : apprenticeList) {
        // if (apprentice == null) {
        // continue;
        // }
        // Friend friend = apprentices.get(apprentice.roleId);
        // if (friend == null) {
        // continue;
        // }
        // for (Map.Entry<Integer, Integer> apprenticeEntry : apprenticeMap.entrySet()) {
        // if (apprenticeEntry == null) {
        // continue;
        // }
        // int lv = apprentice.getLevel();// 当前等级
        // if (apprentice.getSimpleData().getIsHaveMaster() == 0 || apprentice.getSimpleData().getIsHaveMaster() == player.roleId) {
        // if (lv >= apprenticeEntry.getKey()) {
        // apprenticeEntry.setValue(apprenticeEntry.getValue() + 1);
        // }
        // // && friend.getLevel() < apprenticeEntry.getKey()
        // } else if (lv >= apprenticeEntry.getKey()) {
        // apprenticeEntry.setValue(apprenticeEntry.getValue() + 1);
        // }
        // }
        // }
        // }
        //
        //// 曾经拜师的徒弟也可以领奖
        // for (Friend friend : getOnceApprentice(player).values()) {
        // for (Map.Entry<Integer, Integer> apprenticeEntry : apprenticeMap.entrySet()) {
        // if (apprenticeEntry == null) {
        // continue;
        // }
        // int lv = friend.getOnceApprenticeLv();
        // if (lv >= apprenticeEntry.getKey()) {
        // apprenticeEntry.setValue(apprenticeEntry.getValue() + 1);
        // }
        // }
        // }
        for (StaticApprenticeAward s : apprenticeAward.values()) {
            if (s == null) {
                continue;
            }
            CommonPb.MasterAwardList.Builder award = CommonPb.MasterAwardList.newBuilder();
            award.setId(s.getId());
            int getCount = getAwardMap.get(s.getCond()) == null ? 0 : getAwardMap.get(s.getCond());
            int lvCount = apprenticeMap.get(s.getCond()) > s.getMaxNum() ? s.getMaxNum() : apprenticeMap.get(s.getCond());
            award.setCount(getCount);
            if (lvCount > getCount) {
                // 可领
                award.setStatus(2);
            } else if (getCount >= s.getMaxNum()) {
                // 已领取
                award.setStatus(3);
            } else {
                // 未达成
                award.setStatus(1);
            }
            builder.addList(award);
        }
        handler.sendMsgToPlayer(MasterAwardRs.ext, builder.build());
    }

    /**
     * 师父奖励列表领奖
     *
     * @param req
     * @param handler
     */
    public void getMasterAward(GetMasterAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {// 玩家不存在
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int limitLv = getLimitLv(FriendLimit.OPEN_LV);
        if (limitLv == -1) {// 没有配置
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (player.getLevel() < limitLv) {// 等级不足
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }
        Map<Integer, StaticApprenticeAward> apprenticeAward = staticFriendMgr.getApprenticeAward();// 配置积分商城数据
        if (apprenticeAward == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        StaticApprenticeAward staticApprenticeAward = apprenticeAward.get(req.getId());
        if (staticApprenticeAward == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        GetMasterAwardRs.Builder builder = GetMasterAwardRs.newBuilder();
        Map<Integer, Integer> getAwardMap = player.getApprenticeAwardMap(); // 已领取数
        if (getAwardMap == null) {
            getAwardMap = new HashMap<>();
        }

        Map<Integer, Integer> apprenticeMap = new HashMap<>(); // 对应等级徒弟数
        List<Player> apprenticeList = getFriendList(player).get(FriendType.APPRENTICE);
        for (StaticApprenticeAward s : apprenticeAward.values()) {
            if (s == null) {
                continue;
            }
            apprenticeMap.put(s.getCond(), 0);
            Map<Long, Map<Integer, Integer>> friAward = player.getFriAward();
            Collection<Map<Integer, Integer>> values = friAward.values();
            Iterator<Map<Integer, Integer>> iterator = values.iterator();
            while (iterator.hasNext()) {
                Map<Integer, Integer> next = iterator.next();
                Integer orDefault = next.getOrDefault(s.getCond(), 0);
                apprenticeMap.merge(s.getCond(), orDefault, (a, b) -> a + b);
            }
        }
        //Map<Long, Friend> apprentices = getApprentice(player);
        //for (Player apprentice : apprenticeList) {
        //	if (apprentice == null) {
        //		continue;
        //	}
        //	Friend friend = apprentices.get(apprentice.roleId);
        //	if (friend == null) {
        //		continue;
        //	}
        //	for (Map.Entry<Integer, Integer> apprenticeEntry : apprenticeMap.entrySet()) {
        //		if (apprenticeEntry == null) {
        //			continue;
        //		}
        //		int lv = apprentice.getLevel();
        //		if (apprentice.getSimpleData().getIsHaveMaster() == 0 || apprentice.getSimpleData().getIsHaveMaster() == player.roleId) {
        //			if (lv >= apprenticeEntry.getKey()) {
        //				apprenticeEntry.setValue(apprenticeEntry.getValue() + 1);
        //			}
        //		} else if (lv >= apprenticeEntry.getKey() && friend.getLevel() < apprenticeEntry.getKey()) {
        //			apprenticeEntry.setValue(apprenticeEntry.getValue() + 1);
        //		}
        //	}
        //}
        //// 曾经拜师的徒弟也可以领奖
        //for (Friend friend : getOnceApprentice(player).values()) {
        //	for (Map.Entry<Integer, Integer> apprenticeEntry : apprenticeMap.entrySet()) {
        //		if (apprenticeEntry == null) {
        //			continue;
        //		}
        //		int lv = friend.getOnceApprenticeLv();
        //		if (lv >= apprenticeEntry.getKey()) {
        //			apprenticeEntry.setValue(apprenticeEntry.getValue() + 1);
        //		}
        //	}
        //}

        int getCount = getAwardMap.get(staticApprenticeAward.getCond()) == null ? 0 : getAwardMap.get(staticApprenticeAward.getCond());
        int lvCount = apprenticeMap.get(staticApprenticeAward.getCond()) > staticApprenticeAward.getMaxNum() ? staticApprenticeAward.getMaxNum() : apprenticeMap.get(staticApprenticeAward.getCond());
        int multiple = 0;
        multiple = lvCount - getCount;
        if (multiple <= 0) {
            handler.sendErrorMsgToPlayer(GameError.APPRENTICE_NUM_NOT_ENOUGH);
            return;
        }
        getAwardMap.put(staticApprenticeAward.getCond(), lvCount);
        List<List<Integer>> awardList = staticApprenticeAward.getAward();
        for (List<Integer> award : awardList) {
            int type = award.get(0);
            int id = award.get(1);
            int count = award.get(2) * multiple;
            builder.addAward(PbHelper.createAward(type, id, count));
            playerManager.addAward(player, type, id, count, Reason.ADD_FRIEND_SHOP_AWARD);
        }
        handler.sendMsgToPlayer(GetMasterAwardRs.ext, builder.build());
    }

    private int getLimitLv(int lv) {
        List<Integer> additions = staticLimitMgr.getAddtion(231);// 0.好友和师徒开启等级 1.玩家拜师等级限制 2.收徒等级限制 3.拥有好友上限 4.拥有徒弟上限
        if (staticLimitMgr.getAddtion(231) == null) {
            return -1;
        }
        return additions.get(lv);
    }

    private Map<Long, Friend> getApply(Player player) {
        return getPlayerMap(player, FriendType.APPLY);
    }

    private Map<Long, Friend> getFriend(Player player) {
        return getPlayerMap(player, FriendType.FRIEND);
    }

    private Map<Long, Friend> getApprentice(Player player) {
        return getPlayerMap(player, FriendType.APPRENTICE);
    }

    public Map<Long, Friend> getMaster(Player player) {
        return getPlayerMap(player, FriendType.MASTER);
    }

    private Map<Long, Friend> getOnceApprentice(Player player) {
        return getPlayerMap(player, FriendType.ONCE_APPRENTICE);
    }

    private Map<Long, Friend> getApplyMaster(Player player) {
        return getPlayerMap(player, FriendType.APPLY_MASTER);
    }

    /**
     * 随机出5个陌生人
     *
     * @param player
     * @return
     */
    private List<Player> getStranger(Player player, int max, int min) {
        Set<Long> playerStrangerList = player.getStranger();
        List<Player> strangerList = new ArrayList<>();// 所有同阵营玩家
        List<Player> livelyList = new ArrayList<>();// 活跃玩家
        List<Player> noLivelyList = new ArrayList<>();// 不活跃玩家
        List<Player> targetList = new ArrayList<>();// 显示的玩家列表
        Map<Long, Player> playerMap = playerManager.getAllPlayer();

        Set<Long> allFriend = new HashSet<>();// 获取所有好友
        for (Long o : getFriend(player).keySet()) {
            allFriend.add(o);
        }
        for (Long o : getMaster(player).keySet()) {
            allFriend.add(o);
        }
        for (Long o : getApprentice(player).keySet()) {
            allFriend.add(o);
        }

        if (playerMap.values() == null) {
            return strangerList;
        }
        for (Player stranger : playerMap.values()) {
            if (player.roleId.longValue() == stranger.roleId.longValue()) {
                continue;
            }
            if (allFriend.contains(stranger.roleId)) {
                continue;
            }
            if (stranger.getLevel() > max || stranger.getLevel() < min) {
                continue;
            }
            if (stranger.getCountry() == player.getCountry()) {
                strangerList.add(stranger);
            }
            for (Long id : playerStrangerList) {
                if (id.longValue() == stranger.roleId.longValue()) {
                    strangerList.remove(stranger);
                }
            }
        }
        Date date = new Date();
        for (Player stranger : strangerList) {
            Date loginDate = stranger.account.getLoginDate();
            int day = DateHelper.dayiy(loginDate, date);
            if (day <= 3) {
                livelyList.add(stranger);
            } else {
                noLivelyList.add(stranger);
            }
        }
        int count = strangerList.size();
        int lconut = livelyList.size();
        int nlcount = noLivelyList.size();
        if (count <= 5) {
            return strangerList;
        }
        if (lconut <= 5) {
            targetList.addAll(livelyList);
            for (int i = 0; i < 5 - lconut; i++) {
                int index = RandomUtil.randomBetween(0, nlcount);
                targetList.add(noLivelyList.remove(index));
                nlcount = noLivelyList.size();
            }
            return targetList;
        }
        for (int i = 0; i < 5; i++) {
            int index = RandomUtil.randomBetween(0, lconut);
            targetList.add(livelyList.remove(index));
            lconut = livelyList.size();
        }
        return targetList;
    }

    private Map<Long, Friend> getPlayerMap(Player player, int type) {
        return player.getFriends().computeIfAbsent(type, x -> new ConcurrentHashMap<>());
    }

    private Map<Integer, List<Player>> getFriendList(Player player) {
        Map<Integer, List<Player>> playerMap = new HashMap<>();
        if (player.getFriends() == null) {
            player.setFriends(new HashMap<>());
        }
        for (Map.Entry<Integer, Map<Long, Friend>> entry : player.getFriends().entrySet()) {
            List<Player> playerList = new ArrayList<>();
            if (entry.getValue() == null || entry.getKey() == FriendType.ONCE_APPRENTICE) {
                continue;
            }
            Iterator<Map.Entry<Long, Friend>> it = entry.getValue().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, Friend> i = it.next();
                if (entry.getKey() == FriendType.APPLY) {
                    Date date = new Date();
                    if (entry.getValue().get(i.getKey()) != null) {
                        try {
                            date.setTime(entry.getValue().get(i.getKey()).getApplyTime());
                            int day = TimeHelper.whichDay(0, new Date(), date);
                            if (day > 3) {
                                it.remove();
                                continue;
                            }
                        } catch (Exception e) {
                            it.remove();
                            continue;
                        }
                    }
                }
                playerList.add(playerManager.getPlayer(i.getKey()));
            }

            playerMap.put(entry.getKey(), playerList);
        }
        return playerMap;
    }

    /**
     * @Description 获取邀请战友列表
     * @Param [rq, handler]
     * @Return void
     * @Date 2021/8/4 17:27
     **/
    public void getInviteCompanionList(GetInviteCompanionListRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int mapId = worldManager.getMapId(player.getPos());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }
        int warType = rq.getWarType();
        long warId = rq.getWarId();
        WarInfo warInfo = (WarInfo) mapInfo.getWarInfoByWarId(warId);
        if (warInfo == null || warInfo.getAttackerId() != player.roleId) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        List<CommonPb.Companion> list = logicInviteCompanionList(player, mapInfo, warInfo);
        Map<Integer, List<CommonPb.Companion>> collect = list.stream().collect(Collectors.groupingBy(CommonPb.Companion::getStatus));
        GetInviteCompanionListRs.Builder builder = GetInviteCompanionListRs.newBuilder();
        for (Integer integer : CompanionStatus.getKeyList()) {
            List<CommonPb.Companion> companions = collect.get(integer);
            if (companions == null) {
                continue;
            }
            companions.stream().sorted(Comparator.comparing(CommonPb.Companion::getFight).reversed()).collect(Collectors.toList()).forEach(e -> {
                builder.addCompanion(e);
            });
        }
        handler.sendMsgToPlayer(GetInviteCompanionListRs.ext, builder.build());
    }

    /**
     * @Description 邀请战友
     * @Param [rq, handler]
     * @Return void
     * @Date 2021/8/5 9:58
     **/
    public void doInviteCompanionRq(DoInviteCompanionRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int mapId = worldManager.getMapId(player.getPos());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }
        int warType = rq.getWarType();
        long warId = rq.getWarId();
        WarInfo warInfo = (WarInfo) mapInfo.getWarInfoByWarId(warId);
        if (warInfo == null || warInfo.getAttackerId() != player.roleId) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        HashMap<Long, CommonPb.Companion> map = new HashMap<>();
        // 玩家列表
        logicInviteCompanionList(player, mapInfo, warInfo).forEach(x -> {
            map.put(x.getLordId(), x);
        });

        CommonPb.PlayerCityWar.Builder playerCityWarPb = CommonPb.PlayerCityWar.newBuilder();
        if (warInfo.getWarType() == WarType.Attack_WARFARE || warInfo.getWarType() == WarType.ATTACK_FAR) {
            Player attacker = playerManager.getPlayer(warInfo.getAttackerId());
            Player defender = playerManager.getPlayer(warInfo.getDefencerId());
            // 显示城池为战争发生地的城池
            PlayerCity playerCity = mapInfo.getPlayerCity(warInfo.getDefencerPos());
            if (playerCity != null) {
                worldManager.handleWarSoldier(warInfo);// 统计双方兵力
                CommonPb.WarInfo.Builder warInfoPb = warInfo.wrapCountryPb(false);
                warInfoPb.setAttackerLevel(attacker.buildings.getCommandLv());
                warInfoPb.setDefenceLevel(defender.buildings.getCommandLv());
                playerCityWarPb.setWarInfo(warInfoPb);
                playerCityWarPb.setWorldEntity(playerCity.wrapPb());
                playerCityWarPb.setAttackerName(attacker.getNick());
                playerCityWarPb.setAttackerPos(attacker.getPos().wrapPb());
                playerCityWarPb.setAttackId(attacker.getLord().getLordId());
            }
        } else {
            playerCityWarPb.setWarInfo(warInfo.wrapCountryPb(false));
        }
        SynInviteCompanionRs.Builder builder = SynInviteCompanionRs.newBuilder();
        builder.setLordId(player.roleId).setNick(player.getNick()).setHeadImg(player.getLord().getPortrait()).setTitle(player.getLord().getTitle()).setLevel(player.getLevel()).setPlayerCityWar(playerCityWarPb);
        Map<Long, Player> companionMap = warInfo.getCompanionMap();
        for (Long lordId : rq.getLordIdList()) {
            CommonPb.Companion companion = map.get(lordId);
            if (companion == null || companion.getStatus() != CompanionStatus.canInvite.getKey()) {
                continue;
            }
            Player p = playerManager.getPlayer(lordId);
            if (p == null) {
                continue;
            }
            companionMap.put(p.roleId, p);
            SynHelper.synMsgToPlayer(p, SynInviteCompanionRs.EXT_FIELD_NUMBER, SynInviteCompanionRs.ext, builder.build());
        }

        DoInviteCompanionRs.Builder builderBack = DoInviteCompanionRs.newBuilder();
        List<CommonPb.Companion> list = logicInviteCompanionList(player, mapInfo, warInfo);
        Map<Integer, List<CommonPb.Companion>> collect = list.stream().collect(Collectors.groupingBy(CommonPb.Companion::getStatus));
        for (Integer integer : CompanionStatus.getKeyList()) {
            List<CommonPb.Companion> companions = collect.get(integer);
            if (companions == null) {
                continue;
            }
            companions.stream().sorted(Comparator.comparing(CommonPb.Companion::getFight).reversed()).collect(Collectors.toList()).forEach(e -> {
                builderBack.addCompanion(e);
            });
        }
        builderBack.setStatus(0);
        handler.sendMsgToPlayer(DoInviteCompanionRs.ext, builderBack.build());
    }

    public List<CommonPb.Companion> logicInviteCompanionList(Player player, MapInfo mapInfo, WarInfo warInfo) {
        // 仅显示当前地图区域同阵营在线且等级>=45的指挥官，战友显示数量最多为50个。
        List<Player> companionList = mapInfo.getPlayerCityMap().values().stream().map(x -> {
            return x.getPlayer();
        }).filter(x -> {
            return x.isLogin && x.getLevel() >= 45 && x.roleId != player.roleId && x.getCountry() == player.getCountry();
        }).collect(Collectors.toList());

        HashMap<Long, CommonPb.Companion> map = new HashMap<>();
        // 不管玩家是否在线 只要是以到达都显示已加入
        for (March march : warInfo.getAttackMarches()) {
            Player p = playerManager.getPlayer(march.getLordId());
            if (p == null || march == null || march.getState() != MarchState.Waiting || p.roleId == player.roleId) {
                continue;
            }
            map.put(p.roleId, p.warCompanion(CompanionStatus.alreadyJoin.getKey())); // 已加入
        }
        // 以前邀请过的玩家
        Map<Long, Player> companionMap = warInfo.getCompanionMap();
        int count = 0;
        for (Player p : companionList) {
            count++;
            if (count > staticLimitMgr.getNum(SimpleId.INVITE_COMPANION_LIST_NUM)) {
                break;
            }
            if (map.containsKey(p.roleId) || p.roleId == player.roleId) {
                continue;
            } else if (!companionMap.containsKey(p.roleId)) {
                map.put(p.roleId, p.warCompanion(CompanionStatus.canInvite.getKey())); // 可以邀请
            } else {
                map.put(p.roleId, p.warCompanion(CompanionStatus.alreadyInvitation.getKey())); // 已邀请
            }
        }
        return new ArrayList<>(map.values());
    }

    // 解除师徒关系
    public void removeMasterRq(FriendPb.RemoveMasterRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        Player target = playerManager.getPlayer(req.getLordId());
        if (player == null || target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 判断目标是否离线15天
        long OffTime = target.getLord().getOffTime();
        if (target.isLogin || (System.currentTimeMillis() - OffTime) / TimeHelper.DAY_MS < 7) {
            handler.sendErrorMsgToPlayer(GameError.LESS_THAN_15_DAYS_OFFLINE);
            return;
        }
        Map<Long, Friend> playerApprentice = getApprentice(player);
        Map<Long, Friend> playerMaster = getMaster(player);
        Map<Long, Friend> targetApprentice = getApprentice(target);
        Map<Long, Friend> targetMaster = getMaster(target);
        // 玩家为师傅,删除目标为徒弟
        boolean flag = false;
        if (playerApprentice.containsKey(target.roleId) || targetMaster.containsKey(player.getRoleId())) {
            flag = deleteMasterAndApprentice(player, target);
        } else if (playerMaster.containsKey(target.roleId) || targetApprentice.containsKey(player.roleId)) {
            // 玩家为徒弟,删除目标为师傅
            flag = deleteMasterAndApprentice(target, player);
        }
        handler.sendMsgToPlayer(RemoveMasterRs.ext, RemoveMasterRs.newBuilder().setFlag(flag).build());
        synFriendListRs(player);
        synFriendListRs(target);
    }

    // 删除师徒关系 建立好友关系 前一个player是师傅 后一个player是徒弟
    public boolean deleteMasterAndApprentice(Player masterPlayer, Player apprenticePlayer) {
        long masterId = masterPlayer.getRoleId();
        long apprenticeId = apprenticePlayer.getRoleId();
        // 移除双方师徒列表
        Friend apprenticePlayerRemove = getApprentice(masterPlayer).remove(apprenticeId);
        Friend masterPlayerRemove = getMaster(apprenticePlayer).remove(masterId);
        if (apprenticePlayerRemove == null || masterPlayerRemove == null) {
            return false;
        }
        // 师傅曾经的徒弟
        //Map<Long, Friend> onceApprentice = getOnceApprentice(masterPlayer);
        //apprenticePlayerRemove.setOnceApprenticeLv(apprenticePlayer.getLord().getLevel());
        //onceApprentice.put(apprenticePlayerRemove.getRolaId(), apprenticePlayerRemove);
        // 给徒弟做标记 已经拜过师了
        apprenticePlayer.getSimpleData().setIsHaveMaster(masterId);
        apprenticePlayer.getSimpleData().setNextHaveMasterTime(System.currentTimeMillis() + TimeHelper.DAY_MS);
        // 互相添加好友
        apprenticePlayerRemove.setType(FriendType.FRIEND);
        masterPlayerRemove.setType(FriendType.FRIEND);
        getFriend(masterPlayer).put(apprenticePlayerRemove.getRolaId(), apprenticePlayerRemove);
        getFriend(apprenticePlayer).put(masterPlayerRemove.getRolaId(), masterPlayerRemove);

        // 发送解除邮件
        playerManager.sendNormalMail(masterPlayer, MailId.REMOVE_MASTER, apprenticePlayer.getNick());//
        playerManager.sendNormalMail(apprenticePlayer, MailId.REMOVE_MASTER, masterPlayer.getNick());//

        return true;
    }

    public void synFriendListRs(Player player) {
        if (!player.isLogin) {
            return;
        }
        SynFriendListRs.Builder builder = SynFriendListRs.newBuilder();
        for (Map.Entry<Integer, List<Player>> playerEntry : getFriendList(player).entrySet()) {
            for (Player target : playerEntry.getValue()) {
                if (target == null) {
                    continue;
                }
                // 申请列表
                builder.addFriend(PbHelper.createGood(target, playerEntry.getKey()));
            }
        }
        SynHelper.synMsgToPlayer(player, SynFriendListRs.EXT_FIELD_NUMBER, SynFriendListRs.ext, builder.build());
    }

    /**
     * 一键处理 好友或者 师徒 申请
     */
    public void processAllRq(ClientHandler handler, ProcessAllRq processAllRq) {
        int processType = processAllRq.getProcessType();
        int friendType = processAllRq.getFriendType();
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        HashSet<String> set = new HashSet<>();
        if (friendType == FriendType.APPLY) {

        } else if (friendType == FriendType.APPLY_MASTER) {
            Iterator<Entry<Long, Friend>> iterator = getApplyMaster(player).entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Long, Friend> next = iterator.next();
                Player apprenticePlay = playerManager.getPlayer(next.getKey());
                if (apprenticePlay == null) {
                    continue;
                }
                GameError addApprentice = addApprentice(processType, player, apprenticePlay);
                if (addApprentice != GameError.OK) {
                    set.add(apprenticePlay.getNick());
                }
                synFriendListRs(apprenticePlay);
            }
        }
        synFriendListRs(player);
        Builder builder = ProcessAllRs.newBuilder();
        if (set.size() != 0) {
            builder.addAllNick(set);
            handler.sendMsgToPlayer(ProcessAllRs.ext, builder.setResult(false).build());
            return;
        }
        handler.sendMsgToPlayer(ProcessAllRs.ext, builder.setResult(true).build());

    }

    public void searchTeacher(SearchRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        Player target = playerManager.getNamePlayer().get(req.getNickName());
        if (player == null || target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
//		if(player.getCountry()!=target.getCountry()){
//			handler.sendErrorMsgToPlayer(GameError.NOT_SAME_COUNTRY);
//			return;
//		}
        if (player.roleId.longValue() == target.roleId.longValue()) {
            handler.sendErrorMsgToPlayer(GameError.IS_SELF);
            return;
        }
//		int limitLv = getLimitLv(FriendLimit.OPEN_LV);
//		if (limitLv == -1) {
//			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
//			return;
//		}
//		if (player.getLevel() < limitLv) {
//			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
//			return;
//		}

        SearchRs.Builder builder = SearchRs.newBuilder();
        CommonPb.Good.Builder builder1 = CommonPb.Good.newBuilder();
        builder1.setLordId(target.roleId);
        builder1.setNick(target.getNick());
        builder1.setCountry(target.getCountry());
        builder1.setPortrait(target.getPortrait());
        builder1.setTitle(target.getTitle());
        builder1.setLevel(target.getLevel());
        builder1.setCaculateScorce(target.getMaxScore());
        builder1.setOffice(target.getOfficerId());
        builder.setGood(builder1);
        handler.sendMsgToPlayer(SearchRs.ext, builder.build());
//		int i = doApply(player, target, handler, FriendType.APPLY_MASTER, false);
//		if (i != -1) {
//
//		}
    }
}
