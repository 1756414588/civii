package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticBeautyMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BeautyData;
import com.game.domain.p.Item;
import com.game.domain.s.StaticBeautyDateAward;
import com.game.domain.s.StaticBeautyDateSkills;
import com.game.domain.s.StaticProp;
import com.game.log.consumer.EventManager;
import com.game.log.domain.BeautyItemLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.pb.BeautyPb.*;
import com.game.pb.BeautyPb.UpNewBeautySkillRs.Builder;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 2020年5月29日
 *
 *    halo_game
 * <p>
 * BeautyService.java
 **/

@Service
public class BeautyService {

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private BeautyManager beautyManager;

    @Autowired
    private StaticBeautyMgr staticBeautyMgr;

    @Autowired
    private ItemManager itemManager;

    @Autowired
    private StaticPropMgr staticPropMgr;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private ActivityManager activityManager;
    @Autowired
    private DailyTaskManager dailyTaskManager;
    @Autowired
    private EventManager eventManager;

    @Autowired
    ActivityEventManager activityEventManager;

    private static class BeautyContainer {

        private static com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
    }

    public static com.game.log.LogUser getLogHelper() {
        return BeautyContainer.logUser;
    }


    /**
     * 查询玩家美女列表
     *
     * @param req
     * @param handler
     */
    public void getBeautyListRq(BeautyPb.NewGetBeautyListRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        BeautyPb.NewGetBeautyListRs.Builder builder = beautyManager.getBeautyList(player);
        handler.sendMsgToPlayer(BeautyPb.NewGetBeautyListRs.ext, builder.build());
    }

    /**
     * @Description 美女解锁
     * @Date 2021/3/30 21:00
     * @Param [req, handler]
     * @Return
     **/
    public void UnlockingBeautyRq(UnlockingBeautyRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
		LogHelper.MESSAGE_LOGGER.info("美女 UnlockingBeautyRq playerId:{} beautyId:{}", player.getRoleId(), req.getBeautyId());
        int beautyId = req.getBeautyId();
        BeautyData beautyData = beautyManager.getBeautyInfo(player, beautyId);
        if (null == beautyData) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }
        if (beautyData.getIsUnlock() == 1) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_UNLOCKED);
            return;
        }
        beautyData.setIsUnlock(1);
        UnlockingBeautyRs.Builder builder = UnlockingBeautyRs.newBuilder();
        builder.setIsUnlock(beautyData.getIsUnlock());
        builder.setBeautyId(beautyId);
        handler.sendMsgToPlayer(UnlockingBeautyRs.ext, builder.build());
		LogHelper.MESSAGE_LOGGER.info("美女 UnlockingBeautyRs playerId:{} beautyId:{} isUnlock:{} ", player.getRoleId(), req.getBeautyId(), beautyData.getIsUnlock());
    }


    /**
     * 开始小游戏
     *
     * @param req
     * @param handler
     */
    public void playSGameRq(BeautyPb.NewPlaySGameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int beautyId = req.getBeautyId();// 美女ID
        int count = req.getCount();// 小游戏次数
        int result = req.getResult();// 游戏结果  0猜错了  1 猜对了
        BeautyData beautyData = beautyManager.getBeautyInfo(player, beautyId);
        if (null == beautyData) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }
        if (beautyData.getIsUnlock() == 0) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NOT_UNLOCKED);
            return;
        }

        int sGameTimes = player.getLord().getsGameTimes();
        if (count > sGameTimes || count < 1) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GAMES);
            return;
        }
        List<Integer> addtion = staticLimitMgr.getAddtion(SimpleId.WIN_OR_LOSE_THE_GAME);
        if (addtion.size() != 2) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }
        int intimacyAward = 0;
        if (result == 1) {
            intimacyAward = addtion.get(0) * count;
        } else {
            intimacyAward = addtion.get(1) * count;
        }
        int beforentimacyValue = beautyData.getIntimacyValue();
        beautyManager.addIntimacyValue(player, beautyId, intimacyAward, Reason.BEAUTY_INTIMACY_VALUEE);
        int afterIntimacyValue = beautyData.getIntimacyValue();
        if (afterIntimacyValue - beforentimacyValue == 0) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_INTIMACY_IS_FULL);
            return;
        }

        //游戏消耗数据埋点
        BeautyService.getLogHelper()
            .beautyItemLog(new BeautyItemLog(player.roleId, 3, beautyId, afterIntimacyValue - beforentimacyValue, 0, count, 1, 1));
        playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.BEAUTY_SGAMETIMES, count, Reason.BEAUTY_GAME);// 扣除小游戏次数

        long firstPlaySGameTime = player.getLord().getFirstPlaySGameTime();
        if (firstPlaySGameTime == 0) {
            player.getLord().setFirstPlaySGameTime(new Date().getTime());
        }

        BeautyPb.NewPlaySGameRs.Builder builder = BeautyPb.NewPlaySGameRs.newBuilder();
        builder.setAddValue(afterIntimacyValue - beforentimacyValue);
        builder.setBeautyId(beautyId);
        builder.setGameTimes(player.getLord().getsGameTimes());
        builder.setIntimacyValue(afterIntimacyValue);
        builder.setIsUnlockSkill(beautyManager.newKillDateActivation(beautyData, beforentimacyValue, afterIntimacyValue, player));
        dailyTaskManager.record(DailyTaskId.PLAY_GAME, player, count);
        taskManager.doTask(TaskType.REAUTY_SGAME, player);
        handler.sendMsgToPlayer(BeautyPb.NewPlaySGameRs.ext, builder.build());
        eventManager.beautyPlayGame(player, Lists.newArrayList(
            beautyId,
            builder.getAddValue(),
            afterIntimacyValue
        ));
    }


    /**
     * 开始约会
     *
     * @param req
     * @param handler
     */
    public void playSeekingRq(BeautyPb.NewPlaySeekingRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int beautyId = req.getBeautyId();// 美女ID
        BeautyData beautyInfo = beautyManager.getBeautyInfo(player, beautyId);
        if (null == beautyInfo) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }
        if (beautyInfo.getIsUnlock() == 0) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NOT_UNLOCKED);
            return;
        }
        int seekingTimes = beautyInfo.getSeekingTimes();
        if (seekingTimes < 1) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_SEEKING_TIMES_ERROR);
            return;
        }
        BeautyPb.NewPlaySeekingRs.Builder builder = BeautyPb.NewPlaySeekingRs.newBuilder();
        List<List<Integer>> randomAwards = new ArrayList<>();
        if (beautyInfo.getStar() >= 0) {
            randomAwards = beautyManager.getRandomAward(beautyInfo.getStar());
        }
        if (randomAwards == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        ArrayList<Award> awards = new ArrayList<>();
        for (List<Integer> list : randomAwards) {
            if (list == null) {
                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                return;
            }
            if (list.size() == 3) {
                Award award = new Award(list.get(0), list.get(1), list.get(2));
                awards.add(award);
                builder.addAward(award.wrapPb());
            }
            if (list.size() == 2) {
                StaticBeautyDateAward beautyDateAwardById = staticBeautyMgr.getBeautyDateAwardById(list.get(0));
                if (beautyDateAwardById != null) {
                    List<Integer> award = beautyDateAwardById.getAward();
                    if (award.size() == 3) {
                        Award awardTemp = new Award(award.get(0), award.get(1), award.get(2));
                        builder.addAward(awardTemp.wrapPb());
                        awards.add(awardTemp);
                    }
                }
            }
        }
        if (!awards.isEmpty()) {
            playerManager.addAward(player, awards, Reason.BEAUTY_APPOINTMENT);
        }
        int beforentimacyValue = beautyInfo.getIntimacyValue();
        int num = staticLimitMgr.getNum(SimpleId.APPOINTMENT_INTIMACYVALUE);
        beautyManager.addIntimacyValue(player, beautyId, num, Reason.BEAUTY_INTIMACY_VALUEE);
        beautyInfo.setFreeSeekingEndTime(System.currentTimeMillis());
        int afterIntimacyValue = beautyInfo.getIntimacyValue();
        if (afterIntimacyValue - beforentimacyValue == 0) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_INTIMACY_IS_FULL);
            return;
        }
        beautyInfo.setSeekingTimes(seekingTimes - 1);
        builder.setAddValue(afterIntimacyValue - beforentimacyValue);
        builder.setIsUnlockSkill(beautyManager.newKillDateActivation(beautyInfo, beforentimacyValue, afterIntimacyValue, player));
        builder.setIntimacyValue(afterIntimacyValue);
        builder.setBeautyId(beautyId);
        builder.setSeekingTimes(beautyInfo.getSeekingTimes());
        taskManager.doTask(TaskType.REAUTY_SEEKING, player);
        activityEventManager.activityTip(EventEnum.BREAUTY_SEEKING, player, 1, 0);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.BREAUTY_SEEKING, 1);
        dailyTaskManager.record(DailyTaskId.APPOINTMENT, player, 1);
        handler.sendMsgToPlayer(BeautyPb.NewPlaySeekingRs.ext, builder.build());
        eventManager.beautySeek(player, Lists.newArrayList(
            beautyId,
            builder.getAddValue(),
            afterIntimacyValue,
            awards
        ));
    }

    /**
     * 开始送礼 *@param req
     *
     * @param handler
     */
    public void playBeautyGiftRq(BeautyPb.NewPlayBeautyGiftRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int beautyId = req.getBeautyId();// 美女ID
        int propId = req.getPropId(); // 物品ID
        int num = req.getNum(); //物品数量

        Item item = itemManager.getItem(player, propId);
        if (item == null || num > item.getItemNum()) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_GIFT_COUNT_ERROR);
            return;
        }

        BeautyData beautyData = beautyManager.getBeautyInfo(player, beautyId);
        if (null == beautyData) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }

        StaticProp staticProp = staticPropMgr.getStaticProp(propId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        int beforentimacyValue = beautyData.getIntimacyValue();
        List<List<Long>> effectValue = staticProp.getEffectValue();
        for (int i = 0; i < num; i++) {
            for (List<Long> list : effectValue) {
                if (list.size() == 3) {
                    playerManager.addAward(player, beautyData.getKeyId(), list.get(0).intValue(), list.get(1).intValue(), list.get(2),
                        Reason.BEAUTY_GIVING_GIFTS);
                    int after = beautyData.getIntimacyValue();
                    if (after - beforentimacyValue == 0) {
                        handler.sendErrorMsgToPlayer(GameError.BEAUTY_INTIMACY_IS_FULL);
                        return;
                    }
                    playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.BEAUTY_GIVING_GIFTS);
                }
            }
        }
        int afterIntimacyValue = beautyData.getIntimacyValue();
        BeautyPb.NewPlayBeautyGiftRs.Builder builder = BeautyPb.NewPlayBeautyGiftRs.newBuilder();
        builder.setBeautyId(beautyId);
        builder.setIntimacyValue(afterIntimacyValue);
        builder.setIsUnlockSkill(beautyManager.newKillDateActivation(beautyData, beforentimacyValue, afterIntimacyValue, player));
        builder.setAddValue(afterIntimacyValue - beforentimacyValue);
        dailyTaskManager.record(DailyTaskId.GIFTS, player, num);
        handler.sendMsgToPlayer(BeautyPb.NewPlayBeautyGiftRs.ext, builder.build());
        eventManager.beautyGift(player, Lists.newArrayList(
            beautyId,
            builder.getAddValue(),
            afterIntimacyValue
        ));

        achievementService.addAndUpdate(player,AchiType.AT_53,num);
    }
    @Autowired
    AchievementService achievementService;
    /**
     * 获取美女技能列表 *@param req
     *
     * @param handler
     */
    public void getNewBeautySkillRq(GetNewBeautySkillRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int beautyId = req.getBeautyId();
        int skillType = req.getSkillType();
        GetNewBeautySkillRs.Builder builder = beautyManager.getBeautySkillList(player, beautyId, skillType);
        if (null == builder) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }
        handler.sendMsgToPlayer(BeautyPb.GetNewBeautySkillRs.ext, builder.build());
    }

    /**
     * 升级星级技能
     *
     * @param req
     * @param handler
     */
    public void upBeautySkillRq(UpNewBeautySkillRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int beautyId = req.getBeautyId();
        int upBeautyStar = 1;
        BeautyData beautyInfo = beautyManager.getBeautyInfo(player, beautyId);
        if (null == beautyInfo) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }
        upBeautyStar += beautyInfo.getStar();
        List<StaticBeautyDateSkills> staticBeautStarSkillList = staticBeautyMgr.getStaticBeautStarSkills(beautyId);
        if (staticBeautStarSkillList == null) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_UPLV_ERROR);
            return;
        }
        if (upBeautyStar > staticBeautStarSkillList.size() || upBeautyStar <= beautyInfo.getStar()) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_SKILL_LV_MAX);
            return;
        }
        StaticBeautyDateSkills staticBeautyDateSkill = null;
        for (StaticBeautyDateSkills staticBeautyDateSkills : staticBeautStarSkillList) {
            if (staticBeautyDateSkills.getStar() == upBeautyStar) {
                staticBeautyDateSkill = staticBeautyDateSkills;
            }
        }
        if (staticBeautyDateSkill == null) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_UPLV_ERROR);
            return;
        }
        Item item = player.getItem(ItemId.JEWELLERY);
        if (item == null || item.getItemNum() < staticBeautyDateSkill.getNeedNum()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_JEWELLERY_TIEM);
            return;
        }
        playerManager.subAward(player, AwardType.PROP, item.getItemId(), staticBeautyDateSkill.getNeedNum(), Reason.UP_BEAUTY_KILL);
        beautyManager.addStarValue(player, beautyId, 1, Reason.UP_BEAUTY_KILL);
        Builder builder = UpNewBeautySkillRs.newBuilder();
        builder.setPropNum(item.getItemNum());
        builder.setBeautyStar(beautyInfo.getStar());
        handler.sendMsgToPlayer(UpNewBeautySkillRs.ext, builder.build());
        eventManager.beautyUp(player, Lists.newArrayList(
            beautyId,
            upBeautyStar,
            staticBeautyDateSkill.getNeedNum()
        ));
    }

    /**
     * @Description 点击美女获取亲密度
     * @Date 2021/4/7 17:41
     * @Param [req, handler]
     * @Return
     **/
    public void clickBeautyRq(ClickBeautyRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int beautyId = req.getBeautyId();
        BeautyData beautyData = beautyManager.getBeautyInfo(player, beautyId);
        if (null == beautyData) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }
        List<Integer> addtion = staticLimitMgr.getAddtion(SimpleId.CLICK_BEAUTY);
        if (addtion.size() != 3) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        if (beautyData.getClickCount() >= addtion.get(1)) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_CLICK_TIMES_NOT_ENOUGH);
            return;
        }
        int beforentimacyValue = beautyData.getIntimacyValue();
        beautyManager.addIntimacyValue(player, beautyId, addtion.get(2), Reason.BEAUTY_INTIMACY_VALUEE);
        int afterIntimacyValue = beautyData.getIntimacyValue();
        if (afterIntimacyValue - beforentimacyValue == 0) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_INTIMACY_IS_FULL);
            return;
        }
        beautyData.setClickCount(beautyData.getClickCount() + 1);
        ClickBeautyRs.Builder builder = ClickBeautyRs.newBuilder();
        builder.setBeautyId(beautyId);
        builder.setClickCount(beautyData.getClickCount());
        builder.setAddValue(afterIntimacyValue - beforentimacyValue);
        builder.setIsUnlockSkill(beautyManager.newKillDateActivation(beautyData, beforentimacyValue, afterIntimacyValue, player));
        handler.sendMsgToPlayer(ClickBeautyRs.ext, builder.build());

    }

    /**
     * 使用技能
     *
     * @param req
     * @param handler
     *//*

    public void employSkillRq(EmploySkillRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int beautyId = req.getBeautyId();
        int skillType = req.getSkillType();
        int skilllev = req.getSkilllev();

        BeautyRecord beautyInfo = beautyManager.getBeautyInfo(player, beautyId);
        if (null == beautyInfo) {
            handler.sendErrorMsgToPlayer(GameError.BEAUTY_NO_HAVE_ERROR);
            return;
        }

        StaticBeautySkills skillByType = staticBeautyMgr.getSkillByTypeAndLev(Integer.parseInt(String.valueOf(skillType) + (String.valueOf(skilllev))));
        int keyId = skillByType.getKeyId();

        Map<Integer, Integer> skills = beautyInfo.getSkills();
        skills.put(keyId, 1);
    }


    *//**
     * 获取玩家所有美女的技能列表
     * *@param req
     *
     * @param handler
     *//*

    public void getPlayerBeautySkill(GetPlayerBeautySkillRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        BeautyPb.GetPlayerBeautySkillRs.Builder builder = GetPlayerBeautySkillRs.newBuilder();
        Map<Integer, BeautyRecord> beautys = player.getBeautys();
        if (null != beautys && beautys.size() > 0) {
            Set<Entry<Integer, BeautyRecord>> entrySetWomen = beautys.entrySet();

            for (Entry<Integer, BeautyRecord> entry : entrySetWomen) {
                BeautyRecord beautyInfo = beautyManager.getBeautyInfo(player, entry.getKey());

                Map<Integer, Integer> skills = beautyInfo.getSkills();
                if (null != skills && skills.size() > 0) {

                    Set<Entry<Integer, Integer>> entrySetSkill = skills.entrySet();
                    for (Entry<Integer, Integer> skill : entrySetSkill) {
                        CommonPb.BeautySkills.Builder sbuild = BeautySkills.newBuilder();
                        StaticBeautySkills staticSkills = staticBeautyMgr.getStaticSkills(skill.getKey());

                        sbuild.setSkilllev(staticSkills.getSkillLv());
                        sbuild.setSkillType(staticSkills.getSkillType());
                        sbuild.setSkillState(skill.getValue());

                        builder.addSkills(sbuild);
                    }
                }
            }
        }
        handler.sendMsgToPlayer(GetPlayerBeautySkillRs.ext, builder.build());
    }

*//**
     * 刷新美女最新的CD参数数据
     *
     * @param req
     * @param handler
     *//*

    public void refreshBeautyPar(RefreshBeautyParRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        BeautyPb.RefreshBeautyParRs.Builder builder = RefreshBeautyParRs.newBuilder();

        long freeSeekingEndTime = player.getLord().getFreeSeekingEndTime();//24点重置约会次数
        long currentSecond = System.currentTimeMillis();
        if (freeSeekingEndTime <= currentSecond) {
            //I am not sure if we need this, but too scared to delete.
//			player.getLord().setFreeSeekingEndTime(new Date().getTime() + 12*TimeHelper.HOUR_S*TimeHelper.SECOND_MS);
            player.getLord().setSeekingTimes(1);
            player.getLord().setBuySeekingTimes(0);
        }

        builder.setSeekingTimes(player.getLord().getSeekingTimes());
        builder.setFreeSeekingCD(player.getLord().getFreeSeekingEndTime());

        handler.sendMsgToPlayer(RefreshBeautyParRs.ext, builder.build());
    }*/

}
