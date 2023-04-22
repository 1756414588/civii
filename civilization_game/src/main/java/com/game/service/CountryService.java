package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticCountryMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticSensitiveWordMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.CountryData;
import com.game.domain.Nation;
import com.game.domain.Player;
import com.game.domain.TitleAward;
import com.game.domain.WarAssemble;
import com.game.domain.Award;
import com.game.domain.p.City;
import com.game.domain.p.CountryHero;
import com.game.domain.p.CtyDaily;
import com.game.domain.p.CtyGlory;
import com.game.domain.p.CtyGovern;
import com.game.domain.p.CtyRank;
import com.game.domain.p.CtyTask;
import com.game.domain.p.Hero;
import com.game.domain.p.Item;
import com.game.domain.p.Lord;
import com.game.domain.p.Mail;
import com.game.domain.p.Resource;
import com.game.domain.s.StaticCountryBuild;
import com.game.domain.s.StaticCountryGlory;
import com.game.domain.s.StaticCountryGovern;
import com.game.domain.s.StaticCountryHero;
import com.game.domain.s.StaticCountryTask;
import com.game.domain.s.StaticCountryTitle;
import com.game.domain.s.StaticGroup;
import com.game.domain.s.StaticWorldCity;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.GloverLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.ActivityManager;
import com.game.manager.ChatManager;
import com.game.manager.CityManager;
import com.game.manager.CountryManager;
import com.game.manager.DailyTaskManager;
import com.game.manager.HeroManager;
import com.game.manager.ItemManager;
import com.game.manager.MailManager;
import com.game.manager.PlayerManager;
import com.game.manager.RankManager;
import com.game.manager.WorldBoxManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.GetCountryNameHandler;
import com.game.message.handler.cs.ModifyCountryNameHandler;
import com.game.pb.CommonPb;
import com.game.pb.CountryPb;
import com.game.pb.CountryPb.AppointGeneralRq;
import com.game.pb.CountryPb.AppointGeneralRs;
import com.game.pb.CountryPb.CountryBuildRs;
import com.game.pb.CountryPb.CountryGloryAwardRs;
import com.game.pb.CountryPb.CountryTaskAwardRq;
import com.game.pb.CountryPb.CountryTaskAwardRs;
import com.game.pb.CountryPb.DoCountryPublishRq;
import com.game.pb.CountryPb.DoCountryPublishRs;
import com.game.pb.CountryPb.FindCountryHeroRs;
import com.game.pb.CountryPb.GetAppointRs;
import com.game.pb.CountryPb.GetCountryCityRs;
import com.game.pb.CountryPb.GetCountryDailyRs;
import com.game.pb.CountryPb.GetCountryGloryRs;
import com.game.pb.CountryPb.GetCountryHeroRs;
import com.game.pb.CountryPb.GetCountryRs;
import com.game.pb.CountryPb.GetCountryTaskRs;
import com.game.pb.CountryPb.GetCountryWarRs;
import com.game.pb.CountryPb.GetGloryRankRs;
import com.game.pb.CountryPb.GetGovernRs;
import com.game.pb.CountryPb.OpenCountryHeroRs;
import com.game.pb.CountryPb.RevokeGeneralRq;
import com.game.pb.CountryPb.RevokeGeneralRs;
import com.game.pb.CountryPb.SynVoteGovernRq;
import com.game.pb.CountryPb.TitleUpRs;
import com.game.pb.CountryPb.TrainCountryHeroRs;
import com.game.pb.CountryPb.VoteGovernRq;
import com.game.pb.CountryPb.VoteGovernRs;
import com.game.server.GameServer;
import com.game.util.EmojiUtil;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.PlayerCity;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.google.common.collect.Lists;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 国家系统控制器
 */

@Service
public class CountryService {

    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private CountryManager countryManager;
    @Autowired
    private ItemManager itemManager;
    @Autowired
    private HeroManager heroManager;
    @Autowired
    private RankManager rankManager;
    @Autowired
    private CityManager cityManager;
    @Autowired
    private WorldManager worldManager;
    @Autowired
    private StaticCountryMgr staticCountryMgr;
    @Autowired
    private StaticLimitMgr staticLimitMgr;
    @Autowired
    private StaticWorldMgr worldMgr;
    @Autowired
    private ChatManager chatManager;
    @Autowired
    private ActivityManager activityManager;
    @Autowired
    private WorldBoxManager worldBoxManager;
    @Autowired
    private MailManager mailManager;
    @Autowired
    private DailyTaskManager dailyTaskManager;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private StaticMonsterMgr staticMonsterMgr;
    @Autowired
    private StaticSensitiveWordMgr staticSensitiveWordMgr;
    @Autowired
    ActivityEventManager activityEventManager;
    @Test
    public void testZeroTime() {
        long zeroTime = TimeHelper.getZeroOfDay();
        // System.out.println("zeroTime = " + zeroTime);
    }

    // 投票系统相关逻辑
    public void timerCountryLogic() {
        // 今天的凌晨时间
        long zeroTime = TimeHelper.getZeroOfDay();

        // 周荣誉排行发放选票
        countryRankTimer(zeroTime);

        // 投票选票
        voteTimer(zeroTime);
    }

    /**
     * 国家建设,城战,国战排行每周四23:59:59秒清理
     */
    public void countryRankTimer(long zeroTime) {
        try {
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (today != Calendar.FRIDAY) {
                return;
            }

            int date = GameServer.getInstance().currentDay;
            Iterator<CountryData> it = countryManager.getCountrys().values().iterator();
            while (it.hasNext()) {
                CountryData country = it.next();
                if (country.getRankTime() == date) {
                    continue;
                }
                countryManager.changeRankToVote(country);
                country.setRankTime(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 选举
     */
    public void voteTimer(long zeroTime) {
        long currentTime = System.currentTimeMillis();
        // 选举持续时间
        long voteTime = staticLimitMgr.getNum(SimpleId.VOTE_TIME);
        voteTime = voteTime == 0 ? 64800000l : voteTime * TimeHelper.SECOND_MS;
        try {
            Iterator<CountryData> it = countryManager.getCountrys().values().iterator();
            while (it.hasNext()) {
                CountryData next = it.next();
                // 选举功能尚未开启
                if (next.voteState == CountryConst.VOTE_NO) {
                    continue;
                }
                // 选举结束状态,且时间小于14天
                if (next.voteState == CountryConst.VOTE_END && zeroTime < next.getVoteTime()) {
                    continue;
                }
                if (next.voteState == CountryConst.VOTE_PREPREA) {
                    next.voteState = CountryConst.VOTE_END;
                    next.setVoteTime(zeroTime);
                }
                // 遇到选举准备状态,官职任命已超过14天,进入新一轮的选举
                if (next.voteState == CountryConst.VOTE_END && zeroTime >= next.getVoteTime()) {
                    // 如果官员选举开启时间超过了持续时间，则变为第二天开启。
                    if (currentTime >= (next.getVoteTime() + voteTime)) {
                        next.setVoteTime(next.getVoteTime() + TimeHelper.DAY_MS);
                        continue;
                    }
                    next.restartVote();// 开始选举(清理相关数据)
                    // 排名前11的国家玩家入选,参加王位争夺
                    LinkedList<CtyGovern> voteList = new LinkedList<CtyGovern>();
                    List<Lord> rankList = rankManager.getCountryRankList(next.getCountryId(), 0, 11);
                    int size = rankList.size();
                    for (int i = 0; i < 11 && i < size; i++) {
                        long lordId = rankList.get(i).getLordId();
                        CtyGovern govern = new CtyGovern(lordId);
                        next.getGoverns().put(lordId, govern);
                        voteList.add(govern);
                    }

                    next.voteState = CountryConst.VOTE_ING;
                    next.setVoteTime(zeroTime);

                    playerManager.synVote(next.getCountryId(), CountryConst.VOTE_ING, zeroTime, voteList);
                    playerManager.sendCountryVoteMail(next.getCountryId());
                }
                // 官员选举结束。
                else if (next.voteState == CountryConst.VOTE_ING && currentTime > (next.getVoteTime() + voteTime)) {

                    // System.out.println("***************************开启结束***************************");

                    LinkedList<CtyGovern> rankList = new LinkedList<CtyGovern>();
                    Iterator<CtyGovern> voteList = next.getGoverns().values().iterator();
                    while (voteList.hasNext()) {
                        CtyGovern vote = voteList.next();

                        Player target = playerManager.getPlayer(vote.getLordId());
                        if (target == null) {
                            continue;
                        }

                        vote.setFight(target.getBattleScore());
                        rankList.add(vote);
                    }

                    Collections.sort(rankList, new ComparatorVote());

                    // 排序分配官职
                    int c = 0;
                    for (CtyGovern e : rankList) {
                        if (++c <= 3) {
                            e.setGovernId(c);
                        } else {
                            e.setGovernId(CountryConst.GOVERN_GENERAL);
                        }

                        try {
                            // 当选官员之后重新同步城池信息
                            StaticCountryGovern staticGovern = staticCountryMgr.getGovern(c, 2);
                            int callCount = 0;
                            if (staticGovern != null) {
                                callCount = staticGovern.getPerson();
                            }
                            Player target = playerManager.getPlayer(e.getLordId());
                            int mapId = playerManager.getMapId(target);
                            MapInfo mapInfo = worldManager.getMapInfo(mapId);
                            Pos pos = target.getPos();
                            PlayerCity playerCity = mapInfo.getPlayerCityMap().get(pos);
                            playerCity.setCallCount(callCount);
                            worldManager.SynPlayerCityCallSingleRq(playerCity, mapId, target);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    c = 0;
                    for (CtyGovern e : rankList) {
                        Player target = playerManager.getPlayer(e.getLordId());
                        chatManager.governVoteChat(next.getCountryId(), target.getNick(), e);
                        if (++c >= 3) {
                            break;
                        }
                    }
                    next.voteState = CountryConst.VOTE_END;

                    int count = staticLimitMgr.getNum(245) == 0 ? 14 : staticLimitMgr.getNum(245);
                    next.setVoteTime(next.getVoteTime() + count * 24 * 3600 * 1000L);

                    next.setModifyTime(0);
                    playerManager.synVote(next.getCountryId(), CountryConst.VOTE_END, next.getVoteTime(), rankList);

                    next.successVote();// 选举完成
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取国家信息
     */
    public void getCountry(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 获取玩家当前的国家
        int country = player.getCountry();
        if (country <= 0) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            LogHelper.CONFIG_LOGGER.info("player country <= 0.");
            return;
        }

        // 获取当前玩家国家的信息
        CountryData countryData = countryManager.getCountry(country);
        if (countryData == null) {// 国家不存在
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_DATA_NOT_EXISTS);
            return;
        }

        // 获取玩家相关国家信息
        Nation nation = countryManager.getNation(player);
        GetCountryRs.Builder builder = GetCountryRs.newBuilder();
        builder.setLevel(countryData.getLevel());
        builder.setExp(countryData.getExp());
        // builder.setRank(nation.getRank());
        builder.setRank(rankManager.getPersonRank(country, player));
        builder.setBuild(nation.getBuild());
        if (countryData.getAnnouncement() != null) {
            builder.setAnnouncement(countryData.getAnnouncement());
            builder.setPublisher(countryData.getPublisher());
        }
        builder.setWorldBoss1Killed(worldManager.isWorldBossKilled(player.getCountry()));
        builder.setAppoint(countryData.getAppoint());
        builder.setCheckState(countryData.getCheckState());
        builder.setModifyTime(countryData.getModifyTime());
        if (countryData.getCountryName() != null) {
            builder.setCountryName(countryData.getCountryName());
        }
        StaticGroup staticGroup = staticLimitMgr.getStaticGroup(player.account.getChannel(), player.getCountry());
        if (staticGroup != null) {
            builder.setTeamNum(staticGroup.getTeamNum());
        }
        handler.sendMsgToPlayer(GetCountryRs.ext, builder.build());
    }

    /**
     * 国家建设
     */
    public void countryBuild(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        CountryData countryData = countryManager.getCountry(player.getCountry());
        if (countryData == null) {// 国家不存在
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 每日刷新
        Nation nation = countryManager.getNation(player);
        if (nation.getBuild() >= staticLimitMgr.getNum(118)) {
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_BUILD_COUNTRY);
            return;
        }

        int buildCount = nation.getBuild();
        StaticCountryBuild costBd = staticCountryMgr.getCountryBuild(countryData.getLevel(), buildCount + 1);
        if (costBd == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        Resource resource = player.getResource();

        if (resource.getIron() < costBd.getIron()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_IRON);
            return;
        }
        if (resource.getCopper() < costBd.getCopper()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_COPPER);
            return;
        }
        if (resource.getOil() < costBd.getOil()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_OIL);
            return;
        }

        int code = countryManager.addCountryExp(countryData, costBd.getCountryExp());
        if (code == -1) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        if (code == 1) {
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_COUNTRY_LEVEL);
            return;
        }

        playerManager.subIron(player, costBd.getIron(), Reason.COUNTRY_BUILD);
        playerManager.subCopper(player, costBd.getCopper(), Reason.COUNTRY_BUILD);
        playerManager.subOil(player, costBd.getOil(), Reason.COUNTRY_BUILD);
        playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, costBd.getPrestige(), Reason.COUNTRY_BUILD);

        CountryBuildRs.Builder builder = CountryBuildRs.newBuilder();

        // 建设荣誉
        countryManager.updCountryHoror(player, CountryConst.RANK_BUILD);

        builder.setCopper(resource.getCopper());
        builder.setIron(resource.getIron());
        builder.setOil(resource.getOil());
        builder.setBuild(nation.getBuild());
        builder.setHonor(player.getHonor());
        builder.setExp(countryData.getExp());
        builder.setCountryLv(countryData.getLevel());
        handler.sendMsgToPlayer(CountryBuildRs.ext, builder.build());

        activityManager.updActPerson(player, ActivityConst.ACT_BUILD_RANK, 1, 0);
        // countryManager.flushCountryHero(countryData);

        // TODO
        activityEventManager.activityTip(EventEnum.COUNTRY_BUILD, player, 1, 0);
        // 更新通行证活动进度
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MAKE_COUNTRY, 1);
        worldBoxManager.calcuPoints(WorldBoxTask.BUILD_COUNTRY, player, 1);
        dailyTaskManager.record(DailyTaskId.BUILD_COUNTRY, player, 1);
        eventManager.countryBuild(player, Lists.newArrayList(player.getCountry(), costBd.getIron(), costBd.getCount()));
        achievementService.addAndUpdate(player, AchiType.AT_42,1);
    }

    @Autowired
    AchievementService achievementService;

    /**
     * 提升爵位
     */
    public void titleUpRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        Lord lord = player.getLord();
        int currentTitle = lord.getTitle();
        int maxTitle = staticCountryMgr.maxTitle();
        if (currentTitle >= maxTitle) {
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_TITLE);
            return;
        }

        StaticCountryTitle staticTitle = staticCountryMgr.getCountryTitle(currentTitle + 1);
        if (staticTitle == null) {
            LogHelper.CONFIG_LOGGER.info("lord tile = " + (currentTitle + 1));
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        // 威望不足
        if (lord.getHonor() < staticTitle.getPrestige()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_HONOR);
            return;
        }

        // 银币不足
        if (player.getIron() < staticTitle.getIron()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_IRON);
            return;
        }

        // 道具不足
        List<List<Integer>> propList = staticTitle.getPropList();
        for (List<Integer> e : propList) {
            int itemId = e.get(1);
            int itemCount = e.get(2);
            Item item = player.getItem(itemId);
            if (item == null || item.getItemNum() < itemCount) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                return;
            }
        }

        playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, staticTitle.getPrestige(), Reason.UP_TITLE);
        playerManager.subResource(player, ResourceType.IRON, staticTitle.getIron(), Reason.UP_TITLE);

        TitleUpRs.Builder builder = TitleUpRs.newBuilder();
        for (List<Integer> e : propList) {
            int itemId = e.get(1);
            int itemCount = e.get(2);
            Item item = itemManager.subItem(player, itemId, itemCount, Reason.UP_TITLE);
            if (item != null) {
                builder.addProp(item.wrapPb());
            }
        }

        lord.setTitle(lord.getTitle() + 1);
        rankManager.checkRankList(lord); // 检查排行榜
        heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
        builder.setHonor(lord.getHonor());
        builder.setTitle(lord.getTitle());
        builder.setIron(player.getIron());
        staticTitle.getUpgradeAward().forEach(e -> {
            playerManager.addAward(player, e.get(0), e.get(1), e.get(2), Reason.UP_TITLE);
            builder.addAwards(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
        });
        player.getTitleAward().getHisRecv().put(lord.getTitle(), 0);
        handler.sendMsgToPlayer(TitleUpRs.ext, builder.build());
        SpringUtil.getBean(EventManager.class).military_rank_level_up(player, staticTitle.getPrestige());
        SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.military_rank_level_up);

        /**
         * 角色晋升军衔的日志埋点
         */
        com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
        logUser.roleTitleLog(player, staticTitle.getPrestige());

        countryManager.sendChatProMili(player, lord.getTitle());

        /**
         * 晋升军衔消耗资源日志埋点
         */
        logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.IRON), RoleResourceLog.OPERATE_OUT, ResourceType.IRON, ResOperateType.UP_TITLE_OUT.getInfoType(), staticTitle.getIron(), player.account.getChannel()));

        logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, staticTitle.getIron(), IronOperateType.UP_TITLE_OUT.getInfoType()), 1);

    }

    /**
     * 国家任务
     */
    public void getCountryTaskRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        GetCountryTaskRs.Builder builder = GetCountryTaskRs.newBuilder();

        //int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        //if (today == Calendar.FRIDAY) {
        //	handler.sendMsgToPlayer(GetCountryTaskRs.ext, builder.build());
        //	return;
        //}
        Nation nation = countryManager.getNation(player);
        LocalDateTime now = LocalDateTime.now();
        int day = now.getDayOfWeek().getValue();
        int hour = now.getHour();
        if (day == 5 || hour < 9 || hour >= 21) {
            int days = 0;
            if (day == 5 || hour >= 21) {
                days = 1;
            }
            long countryTime = TimeHelper.getCountryTime(days, 9);
            nation.setTaskTime(countryTime);
        }

        // 获取玩家国家任务
        Map<Integer, CtyTask> ctyTaskMap = countryManager.getCountryTask(player);
        //Nation nation = countryManager.getNation(player);
        builder.setTaskTime(nation.getTaskTime());

        if (ctyTaskMap != null) {
            Iterator<CtyTask> it = ctyTaskMap.values().iterator();
            while (it.hasNext()) {
                CtyTask e = it.next();
                builder.addCountryTask(PbHelper.createTask(e).build());
            }
        }
        handler.sendMsgToPlayer(GetCountryTaskRs.ext, builder.build());
    }

    /**
     * 国家任务领奖
     */
    public void countryTaskAwardRq(CountryTaskAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int taskId = req.getTaskId();

        StaticCountryTask staticCountryTask = staticCountryMgr.getCountryTask(taskId);
        if (staticCountryTask == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        CtyTask ctyTask = countryManager.getCountryTask(player, taskId);
        if (ctyTask == null) {
            handler.sendErrorMsgToPlayer(GameError.TASK_NOT_FOUND);
            return;
        }

        CountryTaskAwardRs.Builder builder = CountryTaskAwardRs.newBuilder();

        if (ctyTask.getState() != 1) {
            handler.sendErrorMsgToPlayer(GameError.NOT_FINISH_TASK);
            return;
        }

        List<List<Integer>> awardList = staticCountryTask.getAwardList();
        if (awardList == null || awardList.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        for (List<Integer> e : awardList) {
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            int keyId = playerManager.addAward(player, type, id, count, Reason.COUNTRY_TASK);
            builder.addAward(PbHelper.createAward(player, type, id, count, keyId).build());
        }

        // 完成阵营任务获得额外的军功
        List<Integer> extraAward = staticCountryTask.getExtraAward();
        if (extraAward != null && extraAward.size() == 4) {
            Integer minPercent = extraAward.get(3);
            if (null != minPercent) {
                boolean inTheLimits = RandomUtil.isInTheLimits(minPercent, 100);
                if (inTheLimits) {
                    int type = extraAward.get(0);
                    int id = extraAward.get(1);
                    int count = extraAward.get(2);
                    int keyId = playerManager.addAward(player, type, id, count, Reason.COUNTRY_TASK);
                    builder.addAward(PbHelper.createAward(player, type, id, count, keyId).build());

                    chatManager.sendCountryChat(player.getLord().getCountry(), ChatId.COUNTRY_TASK, player.getNick());
                }
            }
        }

        ctyTask.setState(2);
        builder.setCountryTask(ctyTask.wrapPb());

        handler.sendMsgToPlayer(CountryTaskAwardRs.ext, builder.build());
        eventManager.countryTask(player, Lists.newArrayList(ctyTask.getTaskName()));

        achievementService.addAndUpdate(player,AchiType.AT_43,1);
    }

    /**
     * 获取国家武将
     */
    public void getCountryHeroRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        Map<Integer, StaticCountryHero> countryHeros = staticCountryMgr.getHeros();
        if (countryHeros == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        GetCountryHeroRs.Builder builder = GetCountryHeroRs.newBuilder();
        ConcurrentHashMap<Integer, CountryData> countrys = countryManager.getCountrys();
        for (CountryData country : countrys.values()) {
            if (country == null) {
                LogHelper.CONFIG_LOGGER.info("country is null!");
                return;
            }

            Map<Integer, CountryHero> heroMap = country.getCountryHeroMap();
            for (StaticCountryHero config : countryHeros.values()) {
                if (config == null) {
                    continue;
                }

                int heroId = config.getHeroId();
                if (!heroMap.containsKey(heroId)) {
                    continue;
                }

                CountryHero countryHero = heroMap.get(heroId);
                if (countryHero == null) {
                    LogHelper.CONFIG_LOGGER.info("countryHero is null.");
                    continue;
                }
                Player target = playerManager.getPlayer(countryHero.getLordId());
                CommonPb.CountryHero.Builder ch = CommonPb.CountryHero.newBuilder();
                ch.setHeroId(heroId);
                ch.setLevel(countryHero.getHeroLv());
                if (target != null) {
                    ch.setNick(target.getNick());
                }

                ch.setState(countryHero.getState());
                ch.setPos(countryHero.getPos().wrapPb());
                ch.setFightTimes(countryHero.getFightTimes());
                if (target != null) {
                    Hero hero = target.getHero(countryHero.getHeroId());
                    if (hero != null) {
                        ch.setHero(hero.wrapPb());
                    }
                    ch.setCountry(target.getCountry());
                    ch.setEndTime(countryHero.getLoyaltyEndTime());
                } else {
                    ch.setCountry(0);
                }
                builder.addCountryHero(ch);

            }
        }

        handler.sendMsgToPlayer(GetCountryHeroRs.ext, builder.build());
    }

    /**
     * 获取国家荣誉
     */
    public void getCountryGloryRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_DATA_NOT_EXISTS);
            return;
        }

        CtyGlory ctyGlory = country.getGlory();
        if (ctyGlory == null) {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_GLORY_NOT_EXISTS);
            return;
        }
        GetCountryGloryRs.Builder builder = GetCountryGloryRs.newBuilder();
        builder.setBuilds(ctyGlory.getBuilds());
        builder.setCityFight(ctyGlory.getCityFight());
        builder.setStateFight(ctyGlory.getStateFight());
        // 礼包状态, 检测是否完成对应的礼包
        Nation nation = countryManager.getNation(player);
        TreeMap<Integer, Integer> gloryLv = nation.getGloryLv();
        checkGlory(nation, ctyGlory);
        for (Map.Entry<Integer, Integer> entry : gloryLv.entrySet()) {
            if (entry == null) {
                continue;
            }

            CommonPb.GloryStatus.Builder gloryStatus = CommonPb.GloryStatus.newBuilder();
            gloryStatus.setGloryId(entry.getKey());
            gloryStatus.setStatus(entry.getValue());
            builder.addStatus(gloryStatus);
        }

        // 拥有票数
        int vote = player.getLord().getVip() + player.getLord().getTitle() + nation.getVoteExtra();
        vote = vote - nation.getVote() < 0 ? 0 : vote - nation.getVote();
        builder.setVote(vote);

        List<List<CommonPb.CountryRank>> allRanks = new ArrayList<List<CommonPb.CountryRank>>();
        for (int rankType = 1; rankType <= 3; rankType++) {
            List<CtyRank> rankData = countryManager.getRankData(country, rankType);
            allRanks.add(countryManager.wrapCountryRank(rankData, rankType));
        }

        if (allRanks.size() == 3) {
            builder.addAllCityFightRank(allRanks.get(0));
            builder.addAllStateFightRank(allRanks.get(1));
            builder.addAllBuildStateRank(allRanks.get(2));
        }

        handler.sendMsgToPlayer(GetCountryGloryRs.ext, builder.build());
    }

    public void checkGlory(Nation nation, CtyGlory ctyGlory) {
        TreeMap<Integer, Integer> gloryLv = nation.getGloryLv();
        nation.checkGloryLv();
        Map<Integer, StaticCountryGlory> staticCountryGloryMap = staticCountryMgr.getGlorys();
        for (StaticCountryGlory config : staticCountryGloryMap.values()) {
            int status = gloryLv.get(config.getGloryId());
            if (status == 0 && ctyGlory.getBuilds() >= config.getBuilds() && ctyGlory.getCityFight() >= config.getCityFight() && ctyGlory.getStateFight() >= config.getStateFight()) {
                gloryLv.put(config.getGloryId(), 1);
            }
        }
    }

    /**
     * 领取荣誉奖励
     */
    public void countryGloryAwardRq(CountryPb.CountryGloryAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int gloryId = req.getGloryId();
        StaticCountryGlory staticGlory = staticCountryMgr.getCountryGlory(gloryId);
        if (staticGlory == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        Nation nation = countryManager.getNation(player);
        CtyGlory glory = country.getGlory();

        if (glory.getBuilds() < staticGlory.getBuilds() || glory.getCityFight() < staticGlory.getCityFight() || glory.getStateFight() < staticGlory.getStateFight()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_FINISH_GLORY);
            return;
        }

        nation.getGloryLv().put(gloryId, 2);

        CountryGloryAwardRs.Builder builder = CountryGloryAwardRs.newBuilder();
        for (List<Integer> e : staticGlory.getAwardList()) {
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            int keyId = playerManager.addAward(player, type, id, count, Reason.COUNTRY_GLORY);
            builder.addAward(PbHelper.createAward(player, type, id, count, keyId).build());
        }
        builder.setGloryId(gloryId);
        handler.sendMsgToPlayer(CountryGloryAwardRs.ext, builder.build());
    }

    /**
     * 获取荣誉排行(废弃)
     */
    public void getGloryRankRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        GetGloryRankRs.Builder builder = GetGloryRankRs.newBuilder();

        List<List<CommonPb.CountryRank>> allRanks = new ArrayList<List<CommonPb.CountryRank>>();
        for (int rankType = 1; rankType <= 3; rankType++) {
            List<CtyRank> rankData = countryManager.getRankData(country, rankType);
            allRanks.add(countryManager.wrapCountryRank(rankData, rankType));
        }

        if (allRanks.size() == 3) {
            builder.addAllCityFightRank(allRanks.get(0));
            builder.addAllStateFightRank(allRanks.get(1));
            builder.addAllBuildStateRank(allRanks.get(2));
        }
        handler.sendMsgToPlayer(GetGloryRankRs.ext, builder.build());
    }

    /**
     * 国家日志
     *
     * @param handler
     */
    public void getCountryDailyRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        GetCountryDailyRs.Builder builder = GetCountryDailyRs.newBuilder();

        int c = 0;
        for (CtyDaily ctyDaily : country.getDailys()) {
            if (++c > 10) {
                break;
            }
            builder.addCountryDaily(ctyDaily.wrapPb());
        }
        handler.sendMsgToPlayer(GetCountryDailyRs.ext, builder.build());
    }

    public void getGovernRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        GetGovernRs.Builder builder = GetGovernRs.newBuilder();

        // 选举状态
        builder.setState(country.voteState);
        builder.setVoteTime(country.getVoteTime());
        // 自己的选票
        if (country.voteState == CountryConst.VOTE_ING) {
            Nation nation = countryManager.getNation(player);
            // 未投过
            if (nation.getVote() == 0) {
                int totalVote = player.getLord().getVip() + player.getLord().getTitle() + nation.getVoteExtra();
                totalVote = totalVote - nation.getVote() < 0 ? 0 : totalVote - nation.getVote();
                builder.setVotes(totalVote);
            } else {
                builder.setVotes(0);
            }
        }

        if (country.voteState == CountryConst.VOTE_END || country.voteState == CountryConst.VOTE_ING) {
            Iterator<CtyGovern> it = country.getGoverns().values().iterator();
            while (it.hasNext()) {
                CtyGovern ctyGovern = it.next();
                Player target = playerManager.getPlayer(ctyGovern.getLordId());
                if (target == null) {
                    it.remove();
                    continue;
                }
                int area = playerManager.getMapId(target);
                CommonPb.Govern.Builder governBuilder = PbHelper.createGovern(ctyGovern, target);
                if (country.voteState == CountryConst.VOTE_ING) {
                    governBuilder.setVote(ctyGovern.getVote());
                    /*
                     * CtyGovern ctyGovernTemp = country.getTempGoverns().get(ctyGovern.getLordId()); if(null != ctyGovernTemp){ governBuilder.setOffice(ctyGovernTemp.getGovernId()); }
                     */
                } else if (country.voteState == CountryConst.VOTE_END) {
                    governBuilder.setOffice(ctyGovern.getGovernId());
                }
                governBuilder.setFight(target.getMaxScore());
                governBuilder.setTitle(target.getTitle());
                governBuilder.setPortrait(target.getPortrait());

                if (target.isLogin()) {
                    governBuilder.setOnline(1);
                } else {
                    governBuilder.setOnline(0);
                }

                builder.addGovern(governBuilder.build());
            }
        }
        handler.sendMsgToPlayer(GetGovernRs.ext, builder.build());
    }

    /**
     * 投票选举
     */
    public void voteGovernRq(VoteGovernRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        // 选举结束
        if (country.voteState != CountryConst.VOTE_ING) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        Nation nation = countryManager.getNation(player);
        VoteGovernRs.Builder builder = VoteGovernRs.newBuilder();
        long lordId = req.getLordId();

        CtyGovern ctyGovern = country.getGoverns().get(lordId);
        Player target = playerManager.getPlayer(lordId);
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        if (ctyGovern == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        Lord lord = player.getLord();

        int totalVote = lord.getVip() + lord.getTitle() + nation.getVoteExtra();

        // 清理玩家的累计得票数
        nation.setVoteExtra(0);
        com.game.log.LogUser.log(LogTable.glover_log, GloverLog.builder().ticket(-totalVote).vote(nation.getVoteExtra()).lordId(player.roleId).nick(player.getNick()).vip(player.getVip()).lv(player.getLevel()).channel(player.getAccount().getChannel()).build());

        if (nation.getVote() == 0) {
            ctyGovern.setVote(ctyGovern.getVote() + totalVote);
            nation.setVote(totalVote);
        } else {
            int count = req.getCount();
            if (count < 0) {
                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                return;
            }
            int consumeGold = count * 10;
            if (player.getGold() < consumeGold) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            ctyGovern.setVote(ctyGovern.getVote() + count);
            playerManager.subAward(player, AwardType.GOLD, 0, consumeGold, Reason.COUNTRY_VOTE);

        }
        builder.setGold(player.getGold());
        builder.setGovernVote(ctyGovern.getVote());
        handler.sendMsgToPlayer(VoteGovernRs.ext, builder.build());

        // 向在线的玩家同步投票结果
        CountryPb.SynVoteGovernRq.Builder synBuilder = SynVoteGovernRq.newBuilder();
        synBuilder.setLordId(lordId);
        synBuilder.setGovernVote(ctyGovern.getVote());
        CountryPb.SynVoteGovernRq build = synBuilder.build();

        playerManager.getOnlinePlayer().stream().filter(e -> e.getCountry() == player.getCountry()).forEach(e -> {
            SynHelper.synMsgToPlayer(e, SynVoteGovernRq.EXT_FIELD_NUMBER, SynVoteGovernRq.ext, build);
        });
    }

    /**
     * 任命列表
     */
    public void getAppointRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        GetAppointRs.Builder builder = GetAppointRs.newBuilder();

        Map<Long, Integer> had = new HashMap<Long, Integer>();

        int number = 0;

        Iterator<CtyGovern> it1 = country.getGoverns().values().iterator();
        while (it1.hasNext()) {
            CtyGovern next = it1.next();
            Player target = playerManager.getPlayer(next.getLordId());
            if (target == null) {
                continue;
            }

            int office = country.getGovernId(target.roleId);
            if (office >= 1 && office <= 3) {
                continue;
            }

            // 全部元帅
            had.put(next.getLordId(), 1);

            int mapId = playerManager.getMapId(player);
            CommonPb.Govern.Builder governBuilder = CommonPb.Govern.newBuilder();
            governBuilder.setLordId(next.getLordId());
            governBuilder.setLevel(target.getLevel());
            governBuilder.setNick(target.getNick());
            governBuilder.setArea(mapId);

            governBuilder.setOffice(country.getGovernId(target.roleId));
            governBuilder.setTitle(target.getTitle());
            governBuilder.setFight(target.getBattleScore());
            builder.addGovern(governBuilder.build());
            number++;
        }
        rankManager.readLock().lock();
        try {
            Iterator<Lord> it = rankManager.getRankList().iterator();
            while (it.hasNext()) {
                Lord next = it.next();
                if (had.containsKey(next.getLordId())) {
                    continue;
                }
                Player target = playerManager.getPlayer(next.getLordId());
                if (target == null) {
                    continue;
                }

                if (target.getCountry() != player.getCountry()) {
                    continue;
                }

                int office = country.getGovernId(target.roleId);
                if (office >= 1 && office <= 3) {
                    continue;
                }
                int mapId = playerManager.getMapId(player);
                CommonPb.Govern.Builder governBuilder = CommonPb.Govern.newBuilder();
                governBuilder.setLordId(next.getLordId());
                governBuilder.setLevel(next.getLevel());
                governBuilder.setNick(next.getNick());
                governBuilder.setArea(mapId);

                governBuilder.setOffice(country.getGovernId(target.roleId));
                governBuilder.setTitle(target.getTitle());
                governBuilder.setFight(target.getBattleScore());
                builder.addGovern(governBuilder.build());
                if (++number >= 30) {
                    break;
                }
            }
            handler.sendMsgToPlayer(GetAppointRs.ext, builder.build());
        }finally {
            rankManager.readLock().unlock();
        }
    }

    /**
     * 将军任命
     */
    public void appointGeneralRq(AppointGeneralRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        // 选举结束
        if (country.voteState != CountryConst.VOTE_END) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        CtyGovern king = country.getCtyGovernOffer(player.getLord().getLordId());
        if (king == null || king != null && king.getGovernId() < 1 && king.getGovernId() > 4) {
            LogHelper.CONFIG_LOGGER.info("任免异常,king不存在->[{}]", player.getLord().getLordId());
            countryManager.getCountrys().values().forEach(e -> {
                e.getGoverns().values().forEach(g -> {
                    LogHelper.CONFIG_LOGGER.info("任免异常,官员->[{}]", g.toString());
                });
                LogHelper.CONFIG_LOGGER.info("任免异常,---king不存在-------");
                e.getOfferes().values().forEach(k -> {
                    LogHelper.CONFIG_LOGGER.info("任免异常,元帅->[{}]", k.toString());
                });
            });

            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOVERN);
            return;
        }

        // 将军数量已满
        if (country.getGeneralCount() >= CountryConst.GENERAL_MAX) {
            handler.sendErrorMsgToPlayer(GameError.GENERAL_FULL);
            return;
        }

        // 已有职位不可重复任命
        if (country.getGoverns().containsKey(req.getLordId())) {
            handler.sendErrorMsgToPlayer(GameError.GENERAL_FULL);
            return;
        }

        Player target = playerManager.getPlayer(req.getLordId());
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (target.getCountry() != player.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 首次任命免费,其他每次任命需要100金
        int appoint = country.getAppoint() >= 1 ? 1 : 0;
        int cost = appoint * 100;
        if (cost > 0) {
            if (player.getGold() < cost) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }

            playerManager.subAward(player, AwardType.GOLD, 0, cost, Reason.GENEREL);
        }

        CtyGovern general = new CtyGovern(req.getLordId());
        general.setGovernId(CountryConst.GOVERN_GENERAL);
        country.getGoverns().put(general.getLordId(), general);
        country.setAppoint(appoint + 1);
        try {
            // 清除玩家之前召唤次数的数据
            CtyGovern govern = countryManager.getGovern(target);
            if (govern == null || govern.getGovernId() < 1 || govern.getGovernId() > 4) {
                LogHelper.CONFIG_LOGGER.info("任免异常,官员不存在->[{}]", player.getLord().getLordId());
                countryManager.getCountrys().values().forEach(e -> {
                    e.getGoverns().values().forEach(g -> {
                        LogHelper.CONFIG_LOGGER.info("任免异常,官员->[{}]", g.toString());
                    });
                    LogHelper.CONFIG_LOGGER.info("任免异常,---官员不存在-------");
                    e.getOfferes().values().forEach(k -> {
                        LogHelper.CONFIG_LOGGER.info("任免异常,元帅->[{}]", k.toString());
                    });
                });
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOVERN);
                return;
            }

            int callCount = 0;
            StaticCountryGovern staticGovern = staticCountryMgr.getGovern(govern.getGovernId(), 2);
            if (staticGovern != null) {
                callCount = staticGovern.getPerson();
            }

            target.getLord().setCallDay(0);
            target.getLord().setCallTimes(0);
            target.getLord().setCallCount(callCount);
            target.getLord().setCallReply(0);
            target.getLord().setCallEndTime(0);

            int mapId = playerManager.getMapId(target);
            MapInfo mapInfo = worldManager.getMapInfo(mapId);
            Pos pos = target.getPos();
            PlayerCity playerCity = mapInfo.getPlayerCityMap().get(pos);
            playerCity.setCallCount(callCount);
            playerCity.setCallReply(0);
            playerCity.setCallEndTime(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppointGeneralRs.Builder builder = AppointGeneralRs.newBuilder();

        builder.setGold(player.getGold());
        builder.setAppoint(country.getAppoint());

        handler.sendMsgToPlayer(AppointGeneralRs.ext, builder.build());

        synGovernRq(country.getCountryId(), general);

        try {
            // 当选官员之后重新同步城池信息
            int callCount = 0;
            StaticCountryGovern staticGovern = staticCountryMgr.getGovern(CountryConst.GOVERN_GENERAL, 2);
            if (staticGovern != null) {
                callCount = staticGovern.getPerson();
            }
            int mapId = playerManager.getMapId(target);
            MapInfo mapInfo = worldManager.getMapInfo(mapId);
            Pos pos = target.getPos();
            PlayerCity playerCity = mapInfo.getPlayerCityMap().get(pos);
            playerCity.setCallCount(callCount);
            worldManager.SynPlayerCityCallSingleRq(playerCity, mapId, target);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 撤销将军任命
     */
    public void revokeGeneralRq(RevokeGeneralRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        Player target = playerManager.getPlayer(req.getLordId());
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 选举结束
        if (country.voteState != CountryConst.VOTE_END) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        if (countryManager.getOfficeId(player) != CountryConst.GOVERN_KING) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_IS_NOT_KING);
            return;
        }
        // 已有职位不可重复任命
        if (!country.getGoverns().containsKey(req.getLordId())) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        CtyGovern general = country.getGoverns().get(req.getLordId());
        if (general == null || general.getGovernId() != CountryConst.GOVERN_GENERAL) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOVERN);
            return;
        }
        country.getGoverns().remove(req.getLordId());
        general.setGovernId(0);

        try {
            // 清除玩家之前召唤次数的数据
            player.getLord().setCallDay(0);
            player.getLord().setCallTimes(0);
            player.getLord().setCallCount(0);
            player.getLord().setCallReply(0);
            player.getLord().setCallEndTime(0);

            int mapId = playerManager.getMapId(player);
            MapInfo mapInfo = worldManager.getMapInfo(mapId);
            Pos pos = player.getPos();
            PlayerCity playerCity = mapInfo.getPlayerCityMap().get(pos);
            playerCity.setCallCount(0);
            playerCity.setCallReply(0);
            playerCity.setCallEndTime(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RevokeGeneralRs.Builder builder = RevokeGeneralRs.newBuilder();
        handler.sendMsgToPlayer(RevokeGeneralRs.ext, builder.build());
        synGovernRq(country.getCountryId(), general);
    }

    /**
     * 发布公告
     */
    public void doCountryPublishRq(DoCountryPublishRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 国家
        CountryData country = countryManager.getCountry(player.getLord().getCountry());
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        CtyGovern govern = countryManager.getGovern(player);
        if (govern.getGovernId() != CountryConst.GOVERN_KING) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOVERN);
            return;
        }

        String announcement = req.getAnnouncement();
        // 判断是否包含敏感词
        if (staticSensitiveWordMgr.containSensitiveWord(announcement, "privateChatFilter")) {
            handler.sendErrorMsgToPlayer(GameError.SENSITIVE_WORD);
            return;
        }

        announcement = EmojiUtil.emojiChange(announcement);
        country.setAnnouncement(announcement);
        country.setPublisher(player.getNick());
        DoCountryPublishRs.Builder builder = DoCountryPublishRs.newBuilder();
        builder.setAnnouncement(announcement);
        builder.setPublisher(player.getNick());
        handler.sendMsgToPlayer(DoCountryPublishRs.ext, builder.build());
        // 修改阵营公告
        chatManager.sendCountryChat(player.getCountry(), ChatId.COUNTRY_ADDS, String.valueOf(player.getCountry()), player.getNick());
    }

    /**
     * 国家城池
     */
    public void getCountryCityRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        long lordId = player.getLord().getLordId();
        int country = player.getLord().getCountry();
        int mapId = worldManager.getMapId(player);
        GetCountryCityRs.Builder builder = GetCountryCityRs.newBuilder();
        // 玩家自己的城池
        int cityId = player.getCityId();
        if (cityId != 0) {

            City cityOwn = cityManager.getCity(cityId);
            if (cityOwn != null && cityOwn.getLordId() == lordId) {

                int soildier = cityManager.getCitySoldier(cityOwn.getCityId());
                CommonPb.MapCity.Builder myCity = PbHelper.createMapCity(cityOwn);
                myCity.setSoldier(soildier);

                myCity.setOwnId(lordId);
                myCity.setOwn(player.getNick());
                myCity.setOwnEndTime(myCity.getOwnEndTime());
                myCity.setElectionEndTime(cityOwn.getElectionEndTime());
                myCity.setCountry(cityOwn.getCountry());
                myCity.setMaxSoldier(cityManager.getCityMaxSoldier(cityOwn.getCityId()));
                builder.addMapCity(myCity.build());
            }
        }

        Iterator<City> it = cityManager.getCityMap().values().iterator();
        while (it.hasNext()) {
            City next = it.next();
            if (next.getCountry() != country) {
                continue;
            }

            if (next.getCityId() == player.getCityId()) {
                continue;
            }

            // 当前地图的城池
            StaticWorldCity worldCity = worldMgr.getCity(next.getCityId());
            if (worldCity.getMapId() != mapId) {
                continue;
            }

            int soildier = cityManager.getCitySoldier(next.getCityId());
            CommonPb.MapCity.Builder city = PbHelper.createMapCity(next);
            city.setSoldier(soildier);

            long targetId = next.getLordId();
            if (targetId != 0) {
                Player target = playerManager.getPlayer(targetId);
                if (target != null) {
                    city.setOwnId(targetId);
                    city.setOwn(target.getNick());
                    city.setOwnEndTime(city.getOwnEndTime());
                }
            }
            city.setCountry(next.getCountry());
            city.setMaxSoldier(cityManager.getCityMaxSoldier(next.getCityId()));
            city.setElectionEndTime(next.getElectionEndTime());
            builder.addMapCity(city.build());
        }

        handler.sendMsgToPlayer(GetCountryCityRs.ext, builder.build());
    }

    /**
     * 国家战争
     */
    public void getCountryWarRq(ClientHandler handler) {
        try {
            Player player = playerManager.getPlayer(handler.getRoleId());
            if (player == null) {
                LogHelper.CONFIG_LOGGER.info("player is null.");
                handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
                return;
            }
            WarAssemble warInfos = player.getWarInfos();
            List<WarInfo> infos = warInfos.getInfos();
            GetCountryWarRs.Builder builder = GetCountryWarRs.newBuilder();
            for (WarInfo warInfo : infos) {
                if (warInfo.getWarType() == WarType.ATTACK_COUNTRY) {
                    worldManager.handleWarSoldier(warInfo);
                    builder.addWarInfo(warInfo.wrapCountryPb(warInfo.isJoin(player)));
                } else if (warInfo.getWarType() == WarType.ATTACK_QUICK || warInfo.getWarType() == WarType.ATTACK_FAR || warInfo.getWarType() == WarType.Attack_WARFARE) {
                    Player attacker = playerManager.getPlayer(warInfo.getAttackerId());
                    Player defender = playerManager.getPlayer(warInfo.getDefencerId());
                    if (attacker == null || defender == null) {
                        continue;
                    }
                    MapInfo mapInfo = worldManager.getMapInfo(warInfo.getMapId());
                    PlayerCity playerCity = mapInfo.getPlayerCity(warInfo.getDefencerPos());
                    if (playerCity == null) {
                        continue;
                    }
                    worldManager.handleWarSoldier(warInfo);// 统计双方兵力
                    // 战争所在玩家城池
                    CommonPb.PlayerCityWar.Builder playerCityWarPb = CommonPb.PlayerCityWar.newBuilder();
                    CommonPb.WarInfo.Builder warInfoPb = warInfo.wrapCountryPb(warInfo.isJoin(player));
                    warInfoPb.setAttackerLevel(attacker.buildings.getCommandLv());
                    warInfoPb.setDefenceLevel(defender.buildings.getCommandLv());
                    playerCityWarPb.setWarInfo(warInfoPb);
                    playerCityWarPb.setAttackerName(attacker.getNick());
                    playerCityWarPb.setAttackerPos(attacker.getPos().wrapPb());
                    playerCityWarPb.setAttackId(attacker.getLord().getLordId());
                    playerCityWarPb.setWorldEntity(playerCity.wrapPb());
                    builder.addPlayerCityWar(playerCityWarPb.build());
                }
            }
            handler.sendMsgToPlayer(GetCountryWarRs.ext, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 城池战争
     */
    public void getCityWarRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int country = player.getCountry();

        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        CountryPb.GetCityWarRs.Builder builder = CountryPb.GetCityWarRs.newBuilder();

        Iterator<IWar> it = mapInfo.getWarList(e -> e.getWarType() == WarType.Attack_WARFARE || e.getWarType() == WarType.ATTACK_FAR).iterator();
//		Iterator<WarInfo> it = mapInfo.getCityWarMap().values().iterator();
        while (it.hasNext()) {
            WarInfo next = (WarInfo) it.next();
            if (next == null) {
                continue;
            }
            CommonPb.WarInfo.Builder wrapCountryPb = next.wrapCountryPb(next.isJoin(player));
            // 宣战的玩家是该国的
            Player attacker = playerManager.getPlayer(next.getAttackerId());
            if (attacker != null && attacker.getCountry() == country) {
                worldManager.handleWarSoldier(next);
                builder.addWarInfo(wrapCountryPb);
                continue;
            }

            // 城池是本国的
            Player defender = playerManager.getPlayer(next.getDefencerId());
            if (defender != null && defender.getCountry() == country) {
                worldManager.handleWarSoldier(next);
                builder.addWarInfo(wrapCountryPb);
                continue;
            }
        }
        handler.sendMsgToPlayer(CountryPb.GetCityWarRs.ext, builder.build());
    }

    public void synRankData() {
        Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
        // 计算三个国家的排名情况
        Map<Integer, CountryPb.SynGloryRankRq.Builder> msgData = new HashMap<Integer, CountryPb.SynGloryRankRq.Builder>();
        // 国家
        for (int countryId = 1; countryId <= 3; countryId++) {
            CountryData country = countryManager.getCountry(countryId);
            if (country == null) {
                LogHelper.CONFIG_LOGGER.info("country is null!");
                return;
            }

            CountryPb.SynGloryRankRq.Builder builder = CountryPb.SynGloryRankRq.newBuilder();
            List<List<CommonPb.CountryRank>> allRanks = new ArrayList<List<CommonPb.CountryRank>>();
            for (int rankType = 1; rankType <= 3; rankType++) {
                List<CtyRank> rankData = countryManager.getRankData(country, rankType);
                allRanks.add(countryManager.wrapCountryRank(rankData, rankType));
            }
            if (allRanks.size() == 3) {
                builder.addAllCityFightRank(allRanks.get(0));
                builder.addAllStateFightRank(allRanks.get(1));
                builder.addAllBuildStateRank(allRanks.get(2));
                msgData.put(countryId, builder);
            }
        }

        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player == null || !player.isLogin || player.getChannelId() == -1) {
                continue;
            }

            if (player.getCountry() == 0) {
                continue;
            }

            CountryPb.SynGloryRankRq.Builder msg = msgData.get(player.getCountry());
            if (msg != null) {
                SynHelper.synMsgToPlayer(player, CountryPb.SynGloryRankRq.EXT_FIELD_NUMBER, CountryPb.SynGloryRankRq.ext, msg.build());
            }

        }
    }

    public void synGovernRq(int country, CtyGovern ctyGovern) {
        Iterator<Player> iterator = playerManager.getOnlinePlayer().iterator();
        CountryPb.SynGovernRq.Builder builder = CountryPb.SynGovernRq.newBuilder();
        CommonPb.Govern.Builder data = CommonPb.Govern.newBuilder();
        data.setLordId(ctyGovern.getLordId());
        Player target = playerManager.getPlayer(ctyGovern.getLordId());
        if (target == null) {
            LogHelper.CONFIG_LOGGER.info("target is null!");
            return;
        }
        data.setNick(target.getNick());
        data.setLevel(target.getLevel());
        data.setArea(worldManager.getMapId(target));
        data.setOffice(ctyGovern.getGovernId());
        data.setVote(ctyGovern.getVote());
        data.setTitle(target.getTitle());
        data.setFight(target.getBattleScore());
        data.setPortrait(target.getPortrait());
        builder.setGovern(data);
        CountryPb.SynGovernRq msg = builder.build();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player == null) {
                continue;
            }

            if (!player.isLogin) {
                continue;
            }

            if (player.getChannelId() == -1) {
                continue;
            }

            if (player.getCountry() != country) {
                continue;
            }
            SynHelper.synMsgToPlayer(player, CountryPb.SynGovernRq.EXT_FIELD_NUMBER, CountryPb.SynGovernRq.ext, msg);
        }
    }

    class ComparatorVote implements Comparator<CtyGovern> {

        @Override
        public int compare(CtyGovern o1, CtyGovern o2) {
            long d1 = o1.getVote();
            long d2 = o2.getVote();

            if (d1 < d2) {
                return 1;
            }

            if (d1 > d2) {
                return -1;
            }

            if (o1.getFight() < o2.getFight()) {
                return 1;
            }

            if (o1.getFight() > o2.getFight()) {
                return -1;
            }

            return 0;
        }
    }

    // 名将寻访
    public void findCountryHeroRq(CountryPb.FindCountryHeroRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int heroId = req.getHeroId();
        Integer country = staticCountryMgr.getCountryByHeroId(heroId);
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        CountryData countryData = countryManager.getCountry(country);
        if (countryData == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        CountryHero countryHero = countryData.getCountryHero(heroId);
        if (countryHero == null) {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_HERO_NULL);
            return;
        }

        if (countryHero.getState() != HeroState.OPENED) {
            LogHelper.CONFIG_LOGGER.info("country hero state =" + countryHero.getState());
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_HERO_STATE_ERROR);
            return;
        }

        int myCountry = player.getCountry();
        int price = 0;
        if (myCountry == country) {
            price = staticLimitMgr.getNum(138);
        } else {
            price = staticLimitMgr.getNum(139);
        }

        if (player.getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }
        playerManager.subGoldOk(player, price, Reason.FIND_COUNTRY_HERO);
        int fightTimes = staticLimitMgr.getNum(140);
        countryHero.setFightTimes(fightTimes);
        countryHero.setState(HeroState.FOUND);
        FindCountryHeroRs.Builder builder = FindCountryHeroRs.newBuilder();
        builder.setPos(countryHero.getPos().wrapPb());
        builder.setGold(player.getGold());
        builder.setFightTimes(countryHero.getFightTimes());
        builder.setHeroId(countryHero.getHeroId());
        builder.setState(countryHero.getState());
        handler.sendMsgToPlayer(FindCountryHeroRs.ext, builder.build());
        countryManager.sendChatFoundHero(player, heroId, countryHero.getPos());

    }

    public void openCountryHeroRq(CountryPb.OpenCountryHeroRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int heroId = req.getHeroId();
        Hero hero = player.getHero(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        if (hero.isActivated()) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IS_ACTIVATE);
            return;
        }

        Integer country = staticCountryMgr.getCountryByHeroId(heroId);
        if (country == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        CountryData countryData = countryManager.getCountry(country);
        if (countryData == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        CountryHero countryHero = countryData.getCountryHero(heroId);
        if (countryHero == null) {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_HERO_NULL);
            return;
        }

        StaticCountryHero staticCountryHero = staticCountryMgr.getCountryHero(heroId);
        if (staticCountryHero == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        List<List<Integer>> activate = staticCountryHero.getActivate();
        if (activate == null || activate.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        for (List<Integer> resource : activate) {
            if (resource == null) {
                continue;
            }

            if (resource.size() != 3) {
                handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
                return;
            }
            int resType = resource.get(1);
            int resCount = resource.get(2);
            long owned = player.getResource(resType);
            if (owned < resCount) {
                handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
                return;
            }
        }

        // 资源扣除
        for (List<Integer> resource : activate) {
            int awardType = resource.get(0);
            int resType = resource.get(1);
            int resCount = resource.get(2);
            playerManager.subAward(player, awardType, resType, resCount, Reason.COUNTRY_HERO_ACTIVATE);
        }

        hero.setActivate(HeroState.ACTIVATE);
        countryHero.setState(HeroState.ACTIVATE);
        OpenCountryHeroRs.Builder builder = OpenCountryHeroRs.newBuilder();
        builder.setResouce(player.wrapResourcePb());
        handler.sendMsgToPlayer(OpenCountryHeroRs.ext, builder.build());
    }

    // 喂养国家名将
    public void trainCountryHeroRq(CountryPb.TrainCountryHeroRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int heroId = req.getHeroId();
        int itemId = req.getPropId();
        Hero hero = player.getHero(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        StaticCountryHero config = staticCountryMgr.getCountryHero(heroId);
        if (config == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        List<Integer> propId = config.getPropId();
        if (propId == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        if (propId.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        if (!propId.contains(itemId)) {
            handler.sendErrorMsgToPlayer(GameError.NO_PROP_ID);
            return;
        }

        Item item = player.getItem(itemId);
        if (item == null) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        if (item.getItemNum() < config.getPropNum()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        if (hero.getLoyalty() >= staticLimitMgr.getNum(148)) {
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_LOYALTY);
            return;
        }

        int num = staticLimitMgr.getNum(147);
        hero.addLoyalty(num);
        playerManager.subAward(player, AwardType.PROP, itemId, num, Reason.TRAIN_COUNTRY_HERO);
        TrainCountryHeroRs.Builder builder = TrainCountryHeroRs.newBuilder();
        builder.setHeroId(heroId);
        builder.setLoyalty(hero.getLoyalty());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        handler.sendMsgToPlayer(TrainCountryHeroRs.ext, builder.build());
    }

    // 检查国家名将的忠诚度
    public void checkCountryHero() {
        if (staticLimitMgr.isCloseCtyHero()) {
            return;
        }

        long now = System.currentTimeMillis();
        ConcurrentHashMap<Integer, CountryData> countrys = countryManager.getCountrys();
        for (CountryData countryData : countrys.values()) {
            handleHeroLoyalty(countryData, now);
        } // end for
    }

    public void handleHeroLoyalty(CountryData countryData, long now) {
        Map<Integer, CountryHero> countryHeroMap = countryData.getCountryHeroMap();
        for (CountryHero countryHero : countryHeroMap.values()) {
            if (countryHero == null) {
                continue;
            }

            if (countryHero.getLordId() == 0) {
                continue;
            }

            if (countryHero.getState() == HeroState.NO_ACTIVATE) {
                Player player = playerManager.getPlayer(countryHero.getLordId());
                long period = staticLimitMgr.getNum(142) * TimeHelper.HOUR_MS;
                if (countryHero.getLoyaltyEndTime() <= 0) {
                    countryHero.setLoyaltyEndTime(now + period);
                } else if (countryHero.getLoyaltyEndTime() <= now) {
                    if (player != null) {
                        int heroId = countryHero.getHeroId();
                        Hero hero = player.getHero(heroId);
                        if (hero != null) {
                            hero.subLoyalty(staticLimitMgr.getNum(143));
                        }
                    }
                    countryHero.setLoyaltyEndTime(now + period);
                }

                // to delete
                if (player != null) {
                    int heroId = countryHero.getHeroId();
                    Hero hero = player.getHero(heroId);
                    if (hero != null) {
                        hero.subLoyalty(1);
                    }
                }

                if (player != null) {
                    countryManager.checkHeroEscape(player);
                }

            } else if (countryHero.getState() == HeroState.ACTIVATE) {
                Player player = playerManager.getPlayer(countryHero.getLordId());
                long period = staticLimitMgr.getNum(144) * TimeHelper.HOUR_MS;
                if (countryHero.getLoyaltyEndTime() <= 0) {
                    countryHero.setLoyaltyEndTime(now + period);
                } else if (countryHero.getLoyaltyEndTime() <= now) {
                    if (player != null) {
                        int heroId = countryHero.getHeroId();
                        Hero hero = player.getHero(heroId);
                        if (hero != null) {
                            hero.subLoyalty(staticLimitMgr.getNum(150));
                        }
                    }
                    countryHero.setLoyaltyEndTime(now + period);
                }

                // to delete
                if (player != null) {
                    int heroId = countryHero.getHeroId();
                    Hero hero = player.getHero(heroId);
                    if (hero != null) {
                        hero.subLoyalty(1);
                    }
                }

                if (player != null) {
                    countryManager.checkHeroEscape(player);
                }
            } // end if

        } // end for
    }

    /**
     * 更改国家名字
     *
     * @param rq
     * @param handler
     */
    public void modifyCountryName(CountryPb.ModifyCountryNameRq rq, ModifyCountryNameHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (countryManager.getOfficeId(player) != CountryConst.GOVERN_KING) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_IS_NOT_KING);
            return;
        }

        CountryData countryData = countryManager.getCountry(player.getCountry());
        if (countryData.getCheckState() == 1) {
            handler.sendErrorMsgToPlayer(GameError.MODIFY_NAME_IS_CHECKING);
            return;
        }
        int time = countryData.getModifyTime() + 1;
        int cost = (int) Math.floor(1000 * Math.pow(2, time - 1));
        if (player.getGold() < cost) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }
        // 扣除元宝
        playerManager.subAward(player, AwardType.GOLD, 0, cost, Reason.SUB_MODIFT_COUNTRY_NAME);
        countryData.setCheckState(1);
        countryData.setModifyName(rq.getCountryName());
        countryData.setModifyTime(time);
        countryData.setModifyPlayer(player.getLord().getLordId());
        CountryPb.ModifyCountryNameRs.Builder builder = CountryPb.ModifyCountryNameRs.newBuilder();
        builder.setGold(player.getGold());
        handler.sendMsgToPlayer(CountryPb.ModifyCountryNameRs.ext, builder.build());
        checkModifyCountryName(2, countryData.getCountryId());

    }

    /**
     * @param checkState
     * @param country
     */
    public void checkModifyCountryName(int checkState, int country) {
        CountryData countryData = countryManager.getCountry(country);
        if (countryData.getCheckState() != 1) {
            return;
        }
        Player player = playerManager.getPlayer(countryData.getModifyPlayer());
        // 审核失败

        CountryPb.SynModifyCountryNameRq.Builder builder = CountryPb.SynModifyCountryNameRq.newBuilder();
        if (checkState == 3) {
            int cost = (int) Math.floor(1000 * Math.pow(2, countryData.getModifyTime() - 1));
            // 增加元宝
            countryData.setCheckState(3);
            countryData.setModifyName(null);
            List<Award> awards = new ArrayList<Award>();
            Award award = new Award(0, AwardType.GOLD, 0, cost);
            awards.add(award);
            countryData.setModifyTime(countryData.getModifyTime() - 1);
            builder.setModifyTime(countryData.getModifyTime());
            builder.setCheckState(3);
            builder.setCountryName(countryData.getCountryName());
            playerManager.sendAttachMail(player, awards, MailId.COUNTRY_MODIFY_NAME_FAIL);
            SynHelper.synMsgToPlayer(player, CountryPb.SynModifyCountryNameRq.EXT_FIELD_NUMBER, CountryPb.SynModifyCountryNameRq.ext, builder.build());
        }
        if (checkState == 2) {
            countryData.setCountryName(countryData.getModifyName());
            countryData.setModifyName(null);
            countryData.setModifyPlayer(0);
            countryData.setCheckState(2);
            builder.setCheckState(2);
            builder.setModifyTime(countryData.getModifyTime());
            builder.setCountryName(countryData.getCountryName());
            playerManager.sendNormalMail(player, MailId.COUNTRY_MODIFY_NAME_SUCCESS);
            builder.setGold(player.getGold());
            SynHelper.synMsgToPlayer(player, CountryPb.SynModifyCountryNameRq.EXT_FIELD_NUMBER, CountryPb.SynModifyCountryNameRq.ext, builder.build());
            for (Player p : playerManager.getPlayers().values()) {
                synUpdateCountryName(country, p);
            }

        }

    }

    /**
     * 获得国家的名字
     *
     * @param handler
     */
    public void getCountryName(GetCountryNameHandler handler) {
        CountryPb.GetCountryNameRs.Builder countryNameList = CountryPb.GetCountryNameRs.newBuilder();
        for (CountryData country : countryManager.getCountrys().values()) {
            CountryPb.CountryName.Builder countryName = CountryPb.CountryName.newBuilder();
            countryName.setId(country.getCountryId());
            if (country.getCheckState() == 2) {
                countryName.setCountryName(country.getCountryName());
            } else {
                countryName.setCountryName("");
            }
            countryNameList.addCountryName(countryName);

        }
        handler.sendMsgToPlayer(CountryPb.GetCountryNameRs.ext, countryNameList.build());
    }

    public void synUpdateCountryName(int country, Player player) {
        CountryPb.SynUpdateCountryNameRq.Builder builder = CountryPb.SynUpdateCountryNameRq.newBuilder();
        CountryData countryData = countryManager.getCountry(country);
        CountryPb.CountryName.Builder countryName = CountryPb.CountryName.newBuilder();
        countryName.setId(countryData.getCountryId());
        countryName.setCountryName(countryData.getCountryName());
        builder.setCountryName(countryName);
        SynHelper.synMsgToPlayer(player, CountryPb.SynUpdateCountryNameRq.EXT_FIELD_NUMBER, CountryPb.SynUpdateCountryNameRq.ext, builder.build());
    }

    /**
     * 发放军衔未领取奖励 只调用一次
     *
     * @param player
     */
    @Deprecated
    public void sendTitalReward(Player player, String titleStr, String content, String text) {
        int title = player.getTitle() - 1;
        List<Award> list = new ArrayList<>();
        for (int i = title; i >= 0; i--) {
            StaticCountryTitle staticTitle = staticCountryMgr.getCountryTitle(i);
            if (staticTitle == null) {
                break;
            }
            staticTitle.getUpgradeAward().forEach(e -> {
                Optional<Award> optional = list.stream().filter(f -> e.get(0) == f.getType() && e.get(1) == f.getId()).findFirst();
                if (optional.isPresent()) {
                    Award award = optional.get();
                    award.setCount(award.getCount() + e.get(2));
                } else {
                    list.add(new Award(e.get(0), e.get(1), e.get(2)));
                }
            });
        }
        if (list.size() > 0) {
            List<CommonPb.Award> awardList = new ArrayList<>();
            list.forEach(e -> {
                awardList.add(PbHelper.createAward(e.getType(), e.getId(), e.getCount()).build());
            });
            Mail itemslMail = mailManager.addMail(player, MailId.CUSTOMIZE_MAIL, titleStr, content, text, "1");
            if (itemslMail != null) {
                mailManager.addMailAward(itemslMail, awardList);
                if (player.isLogin) {
                    playerManager.synMailToPlayer(player, itemslMail);
                }
            }
        }
    }

    public void loadTitleAwardInfo(CountryPb.TitleAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int type = req.getType();
        CountryPb.TitleAwardRs.Builder builder = CountryPb.TitleAwardRs.newBuilder();
        TitleAward titleAward = player.getTitleAward();
        builder.setType(type);
        List<List<Integer>> dailyAward = null;
        StaticCountryTitle countryTitle;
        int title1 = req.getTitle();
        switch (type) {
            case 1:// 每日
                countryTitle = staticCountryMgr.getCountryTitle(title1);
                if (countryTitle == null) {
                    countryTitle = staticCountryMgr.getCountryTitle(player.getLord().getTitle());
                    if (countryTitle != null) {
                        List<List<Integer>> dailyAward1 = countryTitle.getDailyAward();
                        if (dailyAward1 != null) {
                            dailyAward1.forEach(e -> {
                                builder.addAwards(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
                            });
                        }
                    }
                } else {
                    List<List<Integer>> promotionAward = countryTitle.getPromotionAward();
                    promotionAward.forEach(e -> {
                        builder.addAwards(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
                    });
                }
                break;
            case 2:// 每日
                int title = player.getLord().getTitle();
                countryTitle = staticCountryMgr.getCountryTitle(title);
                if (countryTitle == null) {
                    handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                    return;
                }
                int recv = titleAward.getRecv();
                if (recv == 0) {
                    dailyAward = countryTitle.getDailyAward();
                    titleAward.setRecv(1);
                    titleAward.setRecvTime(TimeHelper.curentTime());
                }
                break;
            case 3:
                // int title1 = req.getTitle();
                countryTitle = staticCountryMgr.getCountryTitle(title1);
                if (countryTitle == null) {
                    handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                    return;
                }
                Map<Integer, Integer> hisRecv = titleAward.getHisRecv();
                Integer integer = hisRecv.get(title1);
                if (integer != null && integer == 0) {
                    dailyAward = countryTitle.getPromotionAward();
                    hisRecv.put(title1, 1);
                }
                break;
        }
        if (dailyAward != null) {
            dailyAward.forEach(e -> {
                playerManager.addAward(player, e.get(0), e.get(1), e.get(2), Reason.UP_TITLE);
                builder.addAwards(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
            });
        }
        builder.setTitleAward(titleAward.encode());
        handler.sendMsgToPlayer(CountryPb.TitleAwardRs.ext, builder.build());
    }
}
