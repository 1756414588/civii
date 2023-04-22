package com.game.season;

import com.game.constant.AwardType;
import com.game.constant.GameError;
import com.game.constant.ItemId;
import com.game.constant.Reason;
import com.game.domain.Player;
import com.game.domain.p.Item;
import com.game.manager.HeroManager;
import com.game.manager.PlayerManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.SeasonActivityPb;
import com.game.season.directgift.entity.GiftInfo;
import com.game.season.directgift.entity.StaticSeasonLimitGift;
import com.game.season.grand.entity.GrandInfo;
import com.game.season.grand.entity.GrandType;
import com.game.season.grand.entity.StaticSeasonTreasury;
import com.game.season.journey.entity.*;
import com.game.season.seven.entity.*;
import com.game.season.talent.entity.StaticCompTalent;
import com.game.season.talent.entity.StaticCompTalentType;
import com.game.season.talent.entity.StaticCompTalentUp;
import com.game.season.talent.entity.TalentInfo;
import com.game.season.turn.entity.StaticTurn;
import com.game.season.turn.entity.StaticTurnAward;
import com.game.season.turn.entity.StaticTurnConfig;
import com.game.season.turn.entity.TurnInfo;
import com.game.util.PbHelper;
import com.game.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class SeasonService {

    @Autowired
    SeasonManager seasonManager;

    @Autowired
    PlayerManager playerManager;

    @Autowired
    StaticSeasonMgr staticSeasonMgr;

    @Autowired
    SeasonRankManager seasonRankManager;

    @Autowired
    HeroManager heroManager;

    /**
     * 拉取赛季活动
     *
     * @param handler
     */
    public void loadSeasonAct(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Map<Integer, SeasonActivity> activityMap = seasonManager.getActivityMap();
        SeasonActivityPb.SeasonActivityRs.Builder builder = SeasonActivityPb.SeasonActivityRs.newBuilder();
        builder.setSeason(seasonManager.getSeason());
        activityMap.values().stream().filter(x -> x.getState() != SeasonState.NO_OPEN && x.getState() != SeasonState.CLOSE).forEach(x -> {
            SeasonActivityPb.SeasonActivity.Builder builder1 = SeasonActivityPb.SeasonActivity.newBuilder();
            // builder1.setAcId(x.getId());
            // builder1.setState(x.getState().getState());
            builder1.setActivityId(x.getActId());
            builder1.setState(x.getState().getState());
            builder1.setPreTime(x.getPreheatTime());
            builder1.setBeginTime(x.getOpenTime());
            builder1.setEndTime(x.getEndTime());
            builder1.setDisplayTime(x.getExhibitionTime());
            builder1.setId(x.getId());
            builder1.setAwardId(x.getAwardId());
            // BiFunction<Player, SeasonActivity, Boolean> action = seasonManager.actionMap.get(x.getActId());
            // if (action != null) {
            // Boolean apply = action.apply(player, x);
            // builder1.setTips(apply);
            // }
            builder1.setTips(seasonManager.isRed(player, x));
            builder.addActivity(builder1);
        });
        handler.sendMsgToPlayer(SeasonActivityPb.SeasonActivityRs.ext, builder.build());
    }

    /**
     * 拉取赛季旅程信息
     *
     * @param handler
     */
    public void loadSeasonJourney(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_2.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.END) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        JourneyInfo info = player.getModule(JourneyInfo.class);
        SeasonActivityPb.SeasonJourneyRs.Builder builder = SeasonActivityPb.SeasonJourneyRs.newBuilder();
        builder.setActive(info.getActive());
        Map<Integer, Integer> completeInfoMap = info.getCompleteInfoMap();
        if (completeInfoMap.isEmpty()) {
            List<StaticSeasonJourney> seasonJourneyListByIsInTask = staticSeasonMgr.getSeasonJourneyListByIsInTask(seasonActivity.getAwardId());
            seasonJourneyListByIsInTask.forEach(x -> {
                completeInfoMap.put(x.getType(), x.getId());
            });
        }
        completeInfoMap.values().forEach(x -> {
            StaticSeasonJourney e = staticSeasonMgr.getStaticSeasonJourney(x);
            if (e != null) {
                SeasonActivityPb.SeasonJourneyTask.Builder builder1 = SeasonActivityPb.SeasonJourneyTask.newBuilder();
                builder1.setTaskId(e.getId());
                builder1.setName(e.getDesc());
                builder1.setCond(e.getCond());
                builder1.setTime(info.getNeedTime(e.getId()));
                builder1.setState(info.getTaskState(e.getId()));
                builder.addTask(builder1);
            }
        });
        List<StaticJourneyAward> staticJourneyAwards = staticSeasonMgr.getStaticJourneyAwards(seasonActivity.getAwardId());
        if (staticJourneyAwards != null) {
            staticJourneyAwards.forEach(e -> {
                SeasonActivityPb.ActiveAward.Builder builder1 = SeasonActivityPb.ActiveAward.newBuilder();
                builder1.setId(e.getId());
                builder1.setActive(e.getCond());
                builder1.setState(info.getActiveState(e.getId()));
                builder1.addAllAward(PbHelper.createListAward(e.getAward()));
                builder.addAward(builder1);
            });
        }
        handler.sendMsgToPlayer(SeasonActivityPb.SeasonJourneyRs.ext, builder.build());
    }

    /**
     * 领取赛季旅程
     *
     * @param handler
     * @param rq
     */
    public void journeyTaskComplete(ClientHandler handler, SeasonActivityPb.SeasonJourneyCompleteRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_2.getActId());
        if (seasonActivity == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        int taskId = rq.getTaskId();
        StaticSeasonJourney staticSeasonJourney = staticSeasonMgr.getStaticSeasonJourney(taskId);
        JourneyInfo actRecord = player.getModule(JourneyInfo.class);
        int times = actRecord.getNeedTime(taskId);
        if (times < staticSeasonJourney.getCond()) {
            return;
        }
        int state = actRecord.getTaskState(taskId);
        if (state == 1) {
            return;
        }
        // 设置已完成
        actRecord.addActive();
        actRecord.getTaskState().put(taskId, 1);
        SeasonActivityPb.SeasonJourneyCompleteRs.Builder builder = SeasonActivityPb.SeasonJourneyCompleteRs.newBuilder();
        builder.setActive(actRecord.getActive());
        SeasonActivityPb.SeasonJourneyTask.Builder builder1 = SeasonActivityPb.SeasonJourneyTask.newBuilder();
        builder1.setTaskId(staticSeasonJourney.getId());
        builder1.setName(staticSeasonJourney.getDesc());
        builder1.setCond(staticSeasonJourney.getCond());
        builder1.setTime(times);
        builder1.setState(state);
        List<List<Integer>> award = staticSeasonJourney.getAward();
        award.forEach(x -> {
            builder1.addAward(PbHelper.createAward(x.get(0), x.get(1), x.get(2)));
            if (x.get(0) == AwardType.SEASON_SCORE) {
                actRecord.addScore(x.get(1));
                seasonRankManager.addJourneyScore(player, x.get(1), actRecord);
            }
        });
        builder.setTask(builder1);
        handler.sendMsgToPlayer(SeasonActivityPb.SeasonJourneyCompleteRs.ext, builder.build());
        StaticSeasonJourney staticSeasonJourney1 = staticSeasonMgr.getStaticSeasonJourney(staticSeasonJourney.getNextTask());
        if (staticSeasonJourney1 != null) {
            actRecord.getCompleteInfoMap().put(staticSeasonJourney1.getType(), staticSeasonJourney1.getId());
        }
    }

    /**
     * 领取赛季旅程活跃奖励
     *
     * @param rq
     * @param handler
     */
    public void seasonJourneyActiveAward(SeasonActivityPb.SeasonJourneyActiveAwardRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int activeId = rq.getIndex();
        SeasonActivityPb.SeasonJourneyActiveAwardRs.Builder builder = SeasonActivityPb.SeasonJourneyActiveAwardRs.newBuilder();
        StaticJourneyAward staticJourneyAward = staticSeasonMgr.getStaticJourneyAward(activeId);
        if (staticJourneyAward == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        JourneyInfo actRecord = player.getModule(JourneyInfo.class);

        if (staticJourneyAward.getCond() > actRecord.getActive()) {
            return;
        }
        int activeState = actRecord.getActiveState(activeId);
        if (activeState == 1) {
            return;
        }
        builder.addAllAward(PbHelper.createListAward(staticJourneyAward.getAward()));
        staticJourneyAward.getAward().forEach(award -> {
            playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.DAILY_ACTIVE);
        });
        builder.addActiveState(CommonPb.TwoInt.newBuilder().setV1(staticJourneyAward.getId()).setV2(1).build());

        handler.sendMsgToPlayer(SeasonActivityPb.SeasonJourneyActiveAwardRs.ext, builder.build());
        actRecord.getActiveState().put(activeId, 1);
    }

    /**
     * 赛季旅程排行
     *
     * @param handler
     */
    public void getJourneyRank(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_2.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.END) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        JourneyInfo module = player.getModule(JourneyInfo.class);
        SeasonActivityPb.SeasonJourneyRankRs.Builder builder = SeasonActivityPb.SeasonJourneyRankRs.newBuilder();
        Map<Integer, AtomicInteger> journeyCampRank = seasonRankManager.getJourneyCampRank();
        List<SeasonActivityPb.CountrySort> countrySorts = new ArrayList<>();
        for (Map.Entry<Integer, AtomicInteger> integerAtomicIntegerEntry : journeyCampRank.entrySet()) {
            SeasonActivityPb.CountrySort.Builder builder1 = SeasonActivityPb.CountrySort.newBuilder();
            builder1.setCountry(integerAtomicIntegerEntry.getKey());
            builder1.setScore(integerAtomicIntegerEntry.getValue().get());
            countrySorts.add(builder1.build());
        }
        countrySorts = countrySorts.stream().sorted(Comparator.comparing(SeasonActivityPb.CountrySort::getScore).reversed()).collect(Collectors.toList());
        builder.addAllCampSort(countrySorts);

        Map<Long, JourneyInfo> journeyPesRank = seasonRankManager.getJourneyPesRank();
        List<JourneyInfo> journeyInfos = new ArrayList<>(journeyPesRank.values());
        List<JourneyInfo> collect = journeyInfos.stream().sorted(Comparator.comparing(JourneyInfo::getScore).reversed()).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            JourneyInfo journeyInfo = collect.get(i);
            SeasonActivityPb.JourneyRankInfo.Builder builder1 = SeasonActivityPb.JourneyRankInfo.newBuilder();
            Player player1 = journeyInfo.getPlayer();
            builder1.setCountry(player1.getCountry());
            builder1.setNick(player1.getNick());
            builder1.setLordId(player1.getRoleId());
            builder1.setScore(journeyInfo.getScore());
            builder.addPesoRank(builder1);
            StaticJourneyPerson staticJourneyPerson = staticSeasonMgr.getStaticJourneyPerson(seasonActivity.getAwardId(), i);
            if (staticJourneyPerson != null) {
                builder1.addAllAward(PbHelper.createListAward(staticJourneyPerson.getAward()));
            }
        }
        int i = collect.indexOf(module);
        builder.setRank(i);
        if (i > 0) {
            builder.setRank(i + 1);
        }
        builder.setScore(module.getScore());
        handler.sendMsgToPlayer(SeasonActivityPb.SeasonJourneyRankRs.ext, builder.build());

    }

    /**
     * 赛季转盘
     *
     * @param handler
     */
    public void loadTurn(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_3.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.END) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }

        TurnInfo module = player.getModule(TurnInfo.class);
        boolean sameDay = TimeHelper.isSameDay(module.getTime());
        if (!sameDay) {
            module.setCount(1);
        }
        StaticTurnConfig staticTurnConfig = staticSeasonMgr.getStaticTurnConfig();
        SeasonActivityPb.SeasonLucklyDialRs.Builder builder = SeasonActivityPb.SeasonLucklyDialRs.newBuilder();
        builder.setFree(module.getCount());
        builder.setPrice(staticTurnConfig.getOnePrice());
        builder.setTenPrice(staticTurnConfig.getTenPrice());
        builder.setCount(module.getTotalCount());

        List<StaticTurn> staticTurns = staticSeasonMgr.getStaticTurns(seasonActivity.getAwardId());
        staticTurns.forEach(x -> {
            SeasonActivityPb.SeasonActDial.Builder builder1 = SeasonActivityPb.SeasonActDial.newBuilder();
            builder1.setDialId(x.getId());
            builder1.setPlace(x.getPlace());
            List<List<Integer>> award = x.getAward();
            builder1.addAllAward(PbHelper.createListAward(award));
            builder.addActDial(builder1);
        });
        List<StaticTurnAward> staticTurnAwards = staticSeasonMgr.getStaticTurnAwards(seasonActivity.getAwardId());
        for (StaticTurnAward ex : staticTurnAwards) {
            CommonPb.ActivityCond.Builder activityCond = CommonPb.ActivityCond.newBuilder();
            activityCond.setKeyId(ex.getId());
            activityCond.setCond(ex.getCond());
            activityCond.addAllAward(PbHelper.createListAward(ex.getAward()));
            activityCond.setIsAward(module.getReciveState(ex.getId()));
            builder.addActivityCond(activityCond.build());
        }
        handler.sendMsgToPlayer(SeasonActivityPb.SeasonLucklyDialRs.ext, builder.build());

    }

    /**
     * 转赛季转盘
     *
     * @param req
     * @param handler
     */
    public void doLucklyAward(SeasonActivityPb.DoSeasonLucklyDialRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int count = req.getCount();
        // 检查次数
        if (count != 1 && count != 10) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_3.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.END) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }

        TurnInfo module = player.getModule(TurnInfo.class);
        StaticTurnConfig staticTurnConfig = staticSeasonMgr.getStaticTurnConfig();
        // 单抽，如果免费次数没了，判断金币
        int freeTimes = staticTurnConfig.getFreeTimes();
        int getCount = module.getCount();
        int costGold = 0;
        boolean isfree = false;
        switch (count) {
            case 1:
                if (getCount >= freeTimes) {
                    module.setCount(0);
                    isfree = true;
                    module.setTime(TimeHelper.curentTime());
                } else {
                    int itemNum = player.getItemNum(ItemId.SEASON_DIAL);
                    if (itemNum >= count) {
                        playerManager.subAward(player, AwardType.PROP, ItemId.SEASON_DIAL, count, Reason.SEASON);
                        isfree = true;
                    }
                }
                break;
            case 10:
                int itemNum = player.getItemNum(ItemId.SEASON_DIAL);
                if (itemNum >= count) {
                    playerManager.subAward(player, AwardType.PROP, ItemId.SEASON_DIAL, count, Reason.SEASON);
                    isfree = true;
                }
            default:
                break;
        }

        if (!isfree) {
            if (player.getGold() < (count == 1 ? staticTurnConfig.getOnePrice() : staticTurnConfig.getTenPrice())) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            playerManager.subAward(player, AwardType.GOLD, 0, count == 1 ? staticTurnConfig.getOnePrice() : staticTurnConfig.getTenPrice(), Reason.SEASON);
            costGold = count == 1 ? staticTurnConfig.getOnePrice() : staticTurnConfig.getTenPrice();
        }
        SeasonActivityPb.DoSeasonLucklyDialRs.Builder builder = SeasonActivityPb.DoSeasonLucklyDialRs.newBuilder();
        for (int i = 0; i < count; i++) {
            module.addTotalCount();
            StaticTurn randStaticTurns = staticSeasonMgr.getRandStaticTurns(seasonActivity.getAwardId());
            int key;
            List<List<Integer>> award = randStaticTurns.getAward();
            for (List<Integer> actDial : award) {
                key = playerManager.addAward(player, actDial.get(0), actDial.get(1), actDial.get(2), Reason.SEASON);
                builder.addAward(PbHelper.createAward(player, actDial.get(0), actDial.get(1), actDial.get(2), key));
            }

            // 这里要全局
            // if (actDial.getBeRecorded() == 1) {
            // chatManager.sendWorldChat(ChatId.MY_SOUND, player.getNick(), activityBase.getStaticActivity().getName(), actDial.getItemType() + "", actDial.getItemId() + "");
            // activityData.addRewardRecord(new LuckPoolRewardRecord(player, PbHelper.createAward(actDial.getItemType(), actDial.getItemId(), actDial.getItemCount()).build()));
            // }

            builder.addPlace(randStaticTurns.getPlace());
            // if (actDial.getKeyId() != 0) {
            // actRecord.addRecord(actDial.getKeyId(), 1);
            // }
            // List<List<Integer>> buyAward = dial.getBuyAward();
            // if (buyAward != null) {
            // for (List<Integer> list : buyAward) {
            // builder.addBuyAward(PbHelper.createAward(list.get(0), list.get(1), list.get(2)));
            // playerManager.addAward(player, list.get(0), list.get(1), list.get(2), Reason.LUCK_DIAL);
            // }
            // }
        }
        // 抽取次数累计
        builder.setGold(player.getGold());
        builder.setProp(CommonPb.Prop.newBuilder().setPropId(ItemId.SEASON_DIAL).setPropNum(player.getItemNum(ItemId.SEASON_DIAL)).build());
        handler.sendMsgToPlayer(SeasonActivityPb.DoSeasonLucklyDialRs.ext, builder.build());
    }

    public void turnAward(SeasonActivityPb.DoSeasonAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int id = req.getId();
        StaticTurnAward staticTurnAward = staticSeasonMgr.getStaticTurnAward(id);
        if (staticTurnAward == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        TurnInfo module = player.getModule(TurnInfo.class);
        if (module.getTotalCount() < staticTurnAward.getCond()) {
            return;
        }
        int reciveState = module.getReciveState(id);
        if (reciveState == 1) {
            return;
        }
        module.updateRec(id);
        SeasonActivityPb.DoSeasonAwardRs.Builder builder = SeasonActivityPb.DoSeasonAwardRs.newBuilder();
        List<List<Integer>> award = staticTurnAward.getAward();
        int key;
        for (List<Integer> actDial : award) {
            key = playerManager.addAward(player, actDial.get(0), actDial.get(1), actDial.get(2), Reason.SEASON);
            builder.addAward(PbHelper.createAward(player, actDial.get(0), actDial.get(1), actDial.get(2), key));
        }
        handler.sendMsgToPlayer(SeasonActivityPb.DoSeasonAwardRs.ext, builder.build());
    }

    /**
     * 赛季直升礼包
     *
     * @param handler
     */
    public void loadGift(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_5.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        GiftInfo module = player.getModule(GiftInfo.class);
        Map<Integer, StaticSeasonLimitGift> giftMap = staticSeasonMgr.getGiftMap(seasonActivity.getAwardId());
        SeasonActivityPb.LoadGiftRs.Builder builder1 = SeasonActivityPb.LoadGiftRs.newBuilder();
        giftMap.values().forEach(x -> {
            CommonPb.SuripriseGift.Builder builder = CommonPb.SuripriseGift.newBuilder();
            builder.setKeyId(x.getKeyId());
            builder.setName(x.getName());
            builder.setGold(x.getDisplay());
            builder.setMoney(x.getMoney());
            builder.addAllAward(PbHelper.createListAward(x.getAwardList()));
            builder.setCount(x.getLimit());
            builder.setBuyCount(module.getRecount(x.getKeyId()));
            builder.setAsset(x.getAsset());
            builder.setIcon(x.getIcon());
            builder1.addGift(builder);
        });
        handler.sendMsgToPlayer(SeasonActivityPb.LoadGiftRs.ext, builder1.build());
    }

    // public void loadPayGift(ClientHandler handler) {
    // Player player = playerManager.getPlayer(handler.getRoleId());
    // if (player == null) {
    // handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
    // return;
    // }
    // SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_7.getActId());
    // if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.END) {
    // return;
    // }
    // SeasonGIftInfo module = player.getModule(SeasonGIftInfo.class);
    //
    // Map<Integer, StaticSeasonLimitGift> giftMap = staticSeasonMgr.getGiftMap(seasonActivity.getAwardId());
    // SeasonActivityPb.LoadGiftRs.Builder builder1 = SeasonActivityPb.LoadGiftRs.newBuilder();
    // giftMap.values().forEach(x -> {
    // CommonPb.SuripriseGift.Builder builder = CommonPb.SuripriseGift.newBuilder();
    // builder.setKeyId(x.getKeyId());
    // builder.setName(x.getName());
    // builder.setGold(x.getDisplay());
    // builder.setMoney(x.getMoney());
    // builder.addAllAward(PbHelper.createListAward(x.getAwardList()));
    // builder.setCount(x.getCount());
    // builder.setBuyCount(module.getRecount(x.getKeyId()));
    // builder.setAsset(x.getAsset());
    // builder.setIcon(x.getIcon());
    // builder1.addGift(builder);
    // });
    // handler.sendMsgToPlayer(SeasonActivityPb.LoadGiftRs.ext, builder1.build());
    // }

    /**
     * 赛季七日好礼
     *
     * @param rq
     * @param handler
     */
    public void loadSevenInfo(SeasonActivityPb.LoadSevenRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_4.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        int day = rq.getDay();
        SevenInfo module = player.getModule(SevenInfo.class);
        SeasonActivityPb.LoadSevenRs.Builder builder = SeasonActivityPb.LoadSevenRs.newBuilder();
        int days = TimeHelper.equation(seasonActivity.getOpenTime(), TimeHelper.curentTime()) + 1;// 第几天
        builder.setDay(days);
        List<StaticSeasonSevenAward> sevenAwardList = staticSeasonMgr.getSevenAwardList(seasonActivity.getAwardId(), day);
        if (sevenAwardList != null) {
            sevenAwardList.forEach(x -> {
                SeasonActivityPb.SevenInfo.Builder builder1 = SeasonActivityPb.SevenInfo.newBuilder();
                builder1.setId(x.getId());
                builder1.setScore(module.getDayTotalScore(x.getDay()));
                builder1.setState(module.getState(x.getId()));
                builder1.addAllAward(PbHelper.createListAward(x.getAward()));
                builder1.setDesc(x.getContent());
                builder1.setCond(x.getCond());
                builder.addInfo(builder1);
            });
        }

        handler.sendMsgToPlayer(SeasonActivityPb.LoadSevenRs.ext, builder.build());
    }

    /**
     * 领取赛季七日
     *
     * @param rq
     * @param handler
     */
    public void awardSeven(SeasonActivityPb.AwardSevenRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_4.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        int id = rq.getId();
        StaticSeasonSevenAward sevenAward = staticSeasonMgr.getSevenAward(id);
        SevenInfo module = player.getModule(SevenInfo.class);
        int score = module.getDayTotalScore(sevenAward.getDay());
        if (score < sevenAward.getCond() || module.isGet(id)) {
            return;
        }
        module.updateState(id);
        SeasonActivityPb.AwardSevenRs.Builder builder = SeasonActivityPb.AwardSevenRs.newBuilder();
        SeasonActivityPb.SevenInfo.Builder builder1 = SeasonActivityPb.SevenInfo.newBuilder();
        builder1.setId(id);
        builder1.setScore(module.getDayTotalScore(sevenAward.getDay()));
        builder1.setState(module.getState(id));
        builder1.addAllAward(PbHelper.createListAward(sevenAward.getAward()));
        builder1.setDesc(sevenAward.getContent());
        List<List<Integer>> award = sevenAward.getAward();
        for (List<Integer> actDial : award) {
            int key = playerManager.addAward(player, actDial.get(0), actDial.get(1), actDial.get(2), Reason.LUCK_DIAL);
            builder1.addAward(PbHelper.createAward(player, actDial.get(0), actDial.get(1), actDial.get(2), key));
        }
        builder.setInfo(builder1);
        handler.sendMsgToPlayer(SeasonActivityPb.AwardSevenRs.ext, builder.build());
    }

    /**
     * 赛季七日排行
     *
     * @param req
     * @param handler
     */
    public void loadSevenRank(SeasonActivityPb.LoadSevenRankRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_4.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        List<DayScore> list = new ArrayList<>();
        int type = req.getType();
        int day = req.getDay();
        SeasonActivityPb.LoadSevenRankRs.Builder builder = SeasonActivityPb.LoadSevenRankRs.newBuilder();
        builder.setType(type);
        builder.setDay(day);
        SevenInfo module = player.getModule(SevenInfo.class);
        List<DayScore> newList = new ArrayList<>();
        int size = 10;
        int page = req.getPage();// 当前页
        int begin = (page - 1) * size;
        int end = page * size;
        int totalPage = 0;
        switch (type) {
            case 1:
                Map<Long, DayScore> dayRank = seasonRankManager.getDayRank(day);
                list.addAll(dayRank.values());
                List<DayScore> collect = list.stream().filter(x -> x.getScore() >= 2000).sorted(Comparator.comparing(DayScore::getScore).reversed()).collect(Collectors.toList());
                int index = collect.indexOf(player);
                if (index >= 0) {
                    builder.setIndex(index + 1);
                }
                int dayTotalScore = module.getDayTotalScore(day);
                builder.setScore(dayTotalScore);
                if (collect.size() >= end) {
                    newList = collect.subList(begin, end);
                }
                if (collect.size() < end && collect.size() > begin) {
                    newList = collect.subList(begin, collect.size());
                }
                totalPage = collect.size() / 10 + 1;
                for (int i = 0; i < newList.size(); i++) {
                    DayScore dayScore = newList.get(i);
                    Player player1 = dayScore.getPlayer();
                    SeasonActivityPb.SevenRank.Builder builder1 = SeasonActivityPb.SevenRank.newBuilder();
                    builder1.setCountry(player1.getCountry());
                    builder1.setLordId(player1.getRoleId());
                    builder1.setNick(player1.getNick());
                    builder1.setRank(i + 1);
                    builder1.setScore(dayScore.getScore());
                    builder.addRank(builder1);
                }
                break;
            case 2:
                Map<Long, DayScore> dayRank1 = seasonRankManager.getDayRank();// 总排行
                list.addAll(dayRank1.values());
                List<DayScore> collect1 = list.stream().filter(x -> x.getScore() >= 5000).sorted(Comparator.comparing(DayScore::getScore).reversed()).collect(Collectors.toList());
                int index1 = collect1.indexOf(player);
                if (index1 >= 0) {
                    builder.setIndex(index1 + 1);
                }
                builder.setScore(module.getTotalScore());
                if (collect1.size() >= end) {
                    newList = collect1.subList(begin, end);
                }
                if (collect1.size() < end && collect1.size() > begin) {
                    newList = collect1.subList(begin, collect1.size());
                }
                totalPage = collect1.size() / 10 + 1;
                for (int i = 0; i < newList.size(); i++) {
                    DayScore dayScore = newList.get(i);
                    Player player1 = dayScore.getPlayer();
                    SeasonActivityPb.SevenRank.Builder builder1 = SeasonActivityPb.SevenRank.newBuilder();
                    builder1.setCountry(player1.getCountry());
                    builder1.setLordId(player1.getRoleId());
                    builder1.setNick(player1.getNick());
                    builder1.setRank(i + 1);
                    builder1.setScore(dayScore.getScore());
                    builder.addRank(builder1);
                }
                break;
        }
        builder.setTotalPage(totalPage);
        handler.sendMsgToPlayer(SeasonActivityPb.LoadSevenRankRs.ext, builder.build());
    }

    /**
     * 赛季宏伟宝库
     *
     * @param handler
     */
    public void loadTreasury(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_1.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        Map<Integer, StaticSeasonTreasury> staticSeasonTreasury = staticSeasonMgr.getStaticSeasonTreasury(seasonActivity.getAwardId());
        GrandInfo module = player.getModule(GrandInfo.class);
        // if (module.getNextTime() == 0) {
        // long milTime = TimeHelper.getMilTime(System.currentTimeMillis(), 0, 7, 23, 59, 59);
        // module.setNextTime(milTime);
        // }
        if (TimeHelper.curentTime() > module.getNextTime() && module.getState() == 1) {
            module.setState(2);
        }
        SeasonActivityPb.LoadTreasuryRs.Builder builder = SeasonActivityPb.LoadTreasuryRs.newBuilder();
        builder.setState(module.getState());
        builder.setNextTime(module.getNextTime());
        staticSeasonTreasury.values().forEach(x -> {
            SeasonActivityPb.TreasuryInfo.Builder builder1 = SeasonActivityPb.TreasuryInfo.newBuilder();
            builder1.setId(x.getId());
            builder1.setCond(x.getCond());
            builder1.setDesc(x.getDesc());
            builder1.setType(x.getType());
            builder1.setScore(module.getScore(x.getType(), x.getId()));
            CommonPb.Award award = module.getAward(x.getId());
            if (award != null) {
                builder1.setAward(award);
            }
            builder.addInfo(builder1);
        });
        handler.sendMsgToPlayer(SeasonActivityPb.LoadTreasuryRs.ext, builder.build());
    }

    /**
     * 生成赛季宏伟宝库
     *
     * @param handler
     */
    public void loadTreasuryAward(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_1.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        GrandInfo module = player.getModule(GrandInfo.class);
        if (module.getState() != 2) {
            return;
        }
        Map<Integer, StaticSeasonTreasury> staticSeasonTreasury = staticSeasonMgr.getStaticSeasonTreasury(seasonActivity.getAwardId());
        module.setState(3);
        SeasonActivityPb.LoadTreasuryAwardRs.Builder builder = SeasonActivityPb.LoadTreasuryAwardRs.newBuilder();
        builder.setState(module.getState());
        staticSeasonTreasury.values().forEach(x -> {
            SeasonActivityPb.TreasuryInfo.Builder builder1 = SeasonActivityPb.TreasuryInfo.newBuilder();
            builder1.setId(x.getId());
            builder1.setCond(x.getCond());
            builder1.setDesc(x.getDesc());
            builder1.setType(x.getType());
            int score = module.getScore(x.getType(), x.getId());
            builder1.setScore(score);
            if (score >= x.getCond()) {
                // 生成奖励
                List<List<Integer>> award = x.getAward();
                List<Integer> list = award.get(new Random().nextInt(award.size()));
                CommonPb.Award.Builder award1 = PbHelper.createAward(list.get(0), list.get(1), list.get(2));
                builder1.setAward(award1);
                module.getAward().put(x.getId(), award1.build());
            }
            builder.addInfo(builder1);
        });
        handler.sendMsgToPlayer(SeasonActivityPb.LoadTreasuryAwardRs.ext, builder.build());
    }

    /**
     * 领取赛季宏伟宝库
     *
     * @param handler
     */
    public void treasuryAward(SeasonActivityPb.TreasuryAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_1.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        GrandInfo module = player.getModule(GrandInfo.class);
        if (module.getState() != 3) {
            return;
        }
        // module.setState(1);
        // long milTime = TimeHelper.getMilTime(System.currentTimeMillis(), 0, 7, 23, 59, 59);
        // module.setNextTime(milTime);
        List<Integer> idsList = req.getIdsList();
        // 时间设置成本周天
        SeasonActivityPb.TreasuryAwardRs.Builder builder = SeasonActivityPb.TreasuryAwardRs.newBuilder();
        builder.setState(module.getState());
        builder.setNextTime(module.getNextTime());
        idsList.forEach(x -> {
            CommonPb.Award award = module.getAward(x);
            if (award != null) {
                builder.addAward(award);
                playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.SEASON);
            }
        });
        Map<Integer, StaticSeasonTreasury> staticSeasonTreasury = staticSeasonMgr.getStaticSeasonTreasury(seasonActivity.getAwardId());
        staticSeasonTreasury.values().forEach(x -> {
            SeasonActivityPb.TreasuryInfo.Builder builder1 = SeasonActivityPb.TreasuryInfo.newBuilder();
            builder1.setId(x.getId());
            builder1.setCond(x.getCond());
            builder1.setDesc(x.getDesc());
            builder1.setType(x.getType());
            builder1.setScore(0);
            builder.addInfo(builder1);
        });
        handler.sendMsgToPlayer(SeasonActivityPb.TreasuryAwardRs.ext, builder.build());
        module.clean();

        addJourney(player, JourneyType.AWARD, 1, 1);
    }

    /**
     * 记录赛季旅程
     *
     * @param player
     * @param type
     * @param count
     */
    public void addJourney(Player player, JourneyType type, int count, int heroId) {
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_2.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            return;
        }
        List<StaticSeasonJourney> seasonJourneyList = staticSeasonMgr.getSeasonJourneyList(seasonActivity.getAwardId());
        if (seasonJourneyList == null) {
            return;
        }
        JourneyInfo info = player.getModule(JourneyInfo.class);
        info.update(seasonJourneyList, count, type, heroId);
    }

    /**
     * 记录宏伟宝库
     *
     * @param player
     * @param type
     * @param taskType
     * @param count
     */
    public void addTreasuryScore(Player player, GrandType type, int taskType, int count) {
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_1.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            return;
        }
        GrandInfo module = player.getModule(GrandInfo.class);
        if (module.getState() != 1) {
            return;
        }
        Map<Integer, StaticSeasonTreasury> staticSeasonTreasury = staticSeasonMgr.getStaticSeasonTreasury(seasonActivity.getAwardId());
        staticSeasonTreasury.values().forEach(x -> {
            if (x.getType() == type.get() && x.getTaskType() == taskType) {
                int i = module.addScore(type.get(), x.getId(), count);
                // 完成宏伟宝库任务
                if (i >= x.getCond()) {
                    this.addJourney(player, JourneyType.COMP, 1, 1);
                }
            }
        });
    }

    /**
     * 记录赛季七日任务
     *
     * @param sevenType
     * @param cond
     * @param player
     * @param sec
     */
    public void addSevenScore(SevenType sevenType, int cond, Player player, int sec) {
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_4.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            return;
        }
        int day = TimeHelper.equation(seasonActivity.getOpenTime(), TimeHelper.curentTime()) + 1;// 第几天
        Map<Integer, StaticSeasonSeven> staticSeasonSeven = staticSeasonMgr.getStaticSeasonSeven(seasonActivity.getAwardId(), day);
        if (staticSeasonSeven != null) {
            SevenInfo module = player.getModule(SevenInfo.class);
            for (StaticSeasonSeven value : staticSeasonSeven.values()) {
                StaticSeasonSevenType staticSeasonSevenType = staticSeasonMgr.getStaticSeasonSevenType(value.getTaskType());
                if (staticSeasonSevenType == null) {
                    continue;
                }
                int addSc = 0;
                if (staticSeasonSevenType.getType() == sevenType.get()) {
                    int score = module.getScore(day, staticSeasonSevenType.getTaskType());
                    switch (sevenType.get()) {
                        // case 1:
                        // case 2:
                        //
                        // case 6:
                        // case 10:
                        // case 11:
                        // case 13:
                        // case 14:
                        case 3:
                            addSc = staticSeasonSevenType.getGetScore() * cond;//1钻石10积分
                            break;
                        case 4:
                        case 5:
                            List<Integer> section = staticSeasonSevenType.getSection();
                            if (sec >= section.get(0) && sec <= section.get(1) && score < staticSeasonSevenType.getLimitTime()) {
                                addSc = staticSeasonSevenType.getGetScore();
                            }
                            break;
                        case 7:
                        case 8:
                        case 9:
                            if (score == 0 && cond >= staticSeasonSevenType.getCond()) {
                                addSc = staticSeasonSevenType.getGetScore();
                            }
                            break;
                        case 12:// 资源采集
                            if (sec == staticSeasonSevenType.getCondType()) {
                                int i = cond / staticSeasonSevenType.getTime();
                                int i2 = staticSeasonSevenType.getLimitTime() - score;
                                if (i2 > 0) {
                                    int i3 = i > i2 ? i2 : i;
                                    addSc = i3;
                                }
                            }
                            break;
                        case 15:
                            // 参加战斗
                            if (sec == staticSeasonSevenType.getCondType()) {
                                addSc = staticSeasonSevenType.getGetScore();
                            }
                            break;
                        case 16:
                        case 17:
                            int i1 = cond / staticSeasonSevenType.getTime();
                            addSc = i1;
                            break;

                        default:
                            addSc = staticSeasonSevenType.getGetScore();
                            break;
                    }
                    module.addScore(player, day, value.getTaskType(), addSc);
                }
            }
        }
    }

    /**
     * 打开赛季天赋界面
     *
     * @param handler
     */
    public void loadSeasonTalent(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_10.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        TalentInfo module = player.getModule(TalentInfo.class);
        SeasonActivityPb.LoadSeasonTalentRs.Builder builder = SeasonActivityPb.LoadSeasonTalentRs.newBuilder();
        builder.setProgress(module.getProgress());
        builder.setType(module.getTypeId());
        builder.setState(module.getState());
        builder.setFreeTime(module.getFreeTime());

        module.getMap().values().forEach(x -> {
            x.values().forEach(y -> {
                StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(y);
                if (staticCompTalentUp != null) {
                    builder.addTalentId(CommonPb.TwoInt.newBuilder().setV1(y).setV2(staticCompTalentUp.getTypeId()).build());
                }

            });
        });
        handler.sendMsgToPlayer(SeasonActivityPb.LoadSeasonTalentRs.ext, builder.build());

    }

    public void openSeasonTalent(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_10.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        TalentInfo module = player.getModule(TalentInfo.class);
        if (module.getState() == 2) {
            return;
        }
        StaticCompTalent staticCompTalent = staticSeasonMgr.getStaticCompTalent();
        if (staticCompTalent == null) {
            return;
        }
        if (module.getFreeTime() > 0) {
            module.setFreeTime(0);
        } else {
            boolean b = playerManager.checkAndSubItem(player, staticCompTalent.getCost(), Reason.SEASON);
            if (!b) {
                handler.sendErrorMsgToPlayer(GameError.PROP_NOT_ENOUGH);
                return;
            }
        }
        int i = new Random().nextInt(10000);
        if (i < staticCompTalent.getProb()) {
            module.setState(2);
        } else {
            module.addPro();
        }
        if (module.getProgress() >= staticCompTalent.getTotal()) {
            module.setState(2);
        }
        SeasonActivityPb.OpenSeasonTalentRs.Builder builder = SeasonActivityPb.OpenSeasonTalentRs.newBuilder();
        builder.setProgress(module.getProgress());
        builder.setState(module.getState());
        builder.setFreeTime(module.getFreeTime());
        List<List<Integer>> cost = staticCompTalent.getCost();
        if (cost != null) {
            cost.forEach(x -> {
                Item item = player.getItem(x.get(1));
                builder.setProp(PbHelper.createItemPb(item.getItemId(), item.getItemNum()));
            });
        }
        handler.sendMsgToPlayer(SeasonActivityPb.OpenSeasonTalentRs.ext, builder.build());
    }

    public void selectSeasonTalent(SeasonActivityPb.SelectSeasonTalentRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_10.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        TalentInfo module = player.getModule(TalentInfo.class);
        if (module.getState() != 2) {
            return;
        }
        int type = req.getType();
        if (module.getTypeId() != 0) {
            if (type != module.getTypeId()) {
                Map<Integer, Map<Integer, Integer>> map = module.getMap();
                map.values().forEach(x -> x.values().forEach(y -> {
                    StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(y);
                    if (staticCompTalentUp.getTotalCost() != null) {
                        List<List<Integer>> total = staticCompTalentUp.getTotalCost();
                        total.forEach(totalCost -> {
                            playerManager.addAward(player, totalCost.get(0), totalCost.get(1), totalCost.get(2), Reason.SEASON);
                        });
                    }
                }));
            }
        }
        Map<Integer, Map<Integer, Integer>> map = module.getMap();
        map.clear();
        StaticCompTalentType staticCompTalentType = staticSeasonMgr.getStaticCompTalentType(type);
        if (staticCompTalentType != null) {
            module.setTypeId(type);
            StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(staticCompTalentType.getBeginId());
            if (staticCompTalentUp != null) {
                module.updateTalentId(staticCompTalentUp);
            }
        }
        SeasonActivityPb.SelectSeasonTalentRs.Builder builder = SeasonActivityPb.SelectSeasonTalentRs.newBuilder();
        map.values().forEach(x -> {
            x.values().forEach(y -> {

                StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(y);
                if (staticCompTalentUp != null) {
                    builder.addTalentId(CommonPb.TwoInt.newBuilder().setV1(y).setV2(staticCompTalentUp.getTypeId()).build());
                }

                // builder.addTalentId(y);
            });
        });
        handler.sendMsgToPlayer(SeasonActivityPb.SelectSeasonTalentRs.ext, builder.build());
    }

    public void upSeasonTalent(SeasonActivityPb.UpSeasonTalentRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        SeasonActivity seasonActivity = seasonManager.getSeasonActivity(SeasonAct.ACT_10.getActId());
        if (seasonActivity == null || seasonActivity.getState() == SeasonState.NO_OPEN || seasonActivity.getState() == SeasonState.CLOSE) {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        TalentInfo module = player.getModule(TalentInfo.class);
        int talentId = req.getTalentId();
        StaticCompTalentUp staticCompTalentUp = staticSeasonMgr.getStaticCompTalentUp(talentId);
        if (staticCompTalentUp == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        if (staticCompTalentUp.getChildType() == 1) {
            int talentId1 = module.getTalentId(staticCompTalentUp.getChildType(), staticCompTalentUp.getTypeId());
            if (talentId1 != talentId) {
                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                return;
            }
            StaticCompTalentUp staticCompTalentUp1 = staticSeasonMgr.getStaticCompTalentUp(staticCompTalentUp.getNextId());
            if (staticCompTalentUp1 != null) {
                boolean b = playerManager.checkAndSubItem(player, staticCompTalentUp1.getCost(), Reason.SEASON);
                if (!b) {
                    handler.sendErrorMsgToPlayer(GameError.PROP_NOT_ENOUGH);
                    return;
                }
                module.updateTalentId(staticCompTalentUp1);
                List<Integer> preTypeId = staticCompTalentUp1.getPreTypeId();
                if (preTypeId != null) {
                    preTypeId.forEach(x -> {
                        StaticCompTalentUp staticCompTalentUp2 = staticSeasonMgr.getStaticCompTalentUp(x);
                        if (staticCompTalentUp2 != null) {
                            module.updateTalentId(staticCompTalentUp2);
                        }
                    });
                }
            }
        } else {
            List<StaticCompTalentUp> staticCompTalentUps = staticSeasonMgr.getStaticCompTalentUps(module.getTypeId());
            if (staticCompTalentUps != null) {
                for (StaticCompTalentUp compTalentUp : staticCompTalentUps) {
                    int talentId1 = module.getTalentId(compTalentUp.getChildType(), compTalentUp.getTypeId());
                    if (talentId1 != compTalentUp.getKeyId()) {
                        // 没用升级完成 不让解锁下级技能
                        handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                        return;
                    }
                }
            }
            StaticCompTalentUp staticCompTalentUp1 = staticSeasonMgr.getStaticCompTalentUp(staticCompTalentUp.getNextId());
            if (staticCompTalentUp1 != null) {
                Map<Integer, Integer> integerIntegerMap = module.getMap().computeIfAbsent(staticCompTalentUp.getChildType(), x -> new ConcurrentHashMap<>());
                integerIntegerMap.clear();
                integerIntegerMap.put(staticCompTalentUp1.getTypeId(), staticCompTalentUp1.getKeyId());
            }
        }
        Map<Integer, Map<Integer, Integer>> map = module.getMap();
        SeasonActivityPb.UpSeasonTalentRs.Builder builder = SeasonActivityPb.UpSeasonTalentRs.newBuilder();
        map.values().forEach(x -> {
            x.values().forEach(y -> {
                // builder.addTalentId(y);
                StaticCompTalentUp staticCompTalentUp3 = staticSeasonMgr.getStaticCompTalentUp(y);
                if (staticCompTalentUp3 != null) {
                    builder.addTalentId(CommonPb.TwoInt.newBuilder().setV1(y).setV2(staticCompTalentUp3.getTypeId()).build());
                }
            });
        });
        handler.sendMsgToPlayer(SeasonActivityPb.UpSeasonTalentRs.ext, builder.build());
        heroManager.caculateBattleScore(player);
    }

}
