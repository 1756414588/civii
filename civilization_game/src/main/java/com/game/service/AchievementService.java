package com.game.service;

import com.game.constant.AchiType;
import com.game.constant.GameError;
import com.game.constant.Reason;
import com.game.dataMgr.StaticAchiMgr;
import com.game.domain.Player;
import com.game.domain.p.AchievementInfo;
import com.game.domain.s.StaticAchiAwardBox;
import com.game.domain.s.StaticAchiInfo;
import com.game.domain.s.StaticAchievement;
import com.game.manager.PlayerManager;
import com.game.manager.RankManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.pb.CommonPb;
import com.game.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AchievementService {
    @Autowired
    PlayerManager playerManager;
    @Autowired
    StaticAchiMgr staticAchiMgr;
    @Autowired
    RankManager rankManager;

    /**
     * 拉取成就奖励
     *
     * @param handler
     */
    public void achievementInfo(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        AchievementInfo achievementInfo = player.getAchievementInfo();
        ActivityPb.AchievementRs.Builder builder = ActivityPb.AchievementRs.newBuilder();
        builder.setScore(achievementInfo.getScore());
        Map<Integer, StaticAchiInfo> achiInfoHashMap = staticAchiMgr.getAchiInfoHashMap();
        achiInfoHashMap.values().forEach(x -> {
            CommonPb.ThreeInt.Builder builder1 = CommonPb.ThreeInt.newBuilder();
            builder1.setV1(x.getType());
            List<Integer> list = achievementInfo.getRec().computeIfAbsent(x.getType(), y -> new ArrayList<>());
            builder1.setV2(list.size());
            List<StaticAchievement> staticAchievementByType = staticAchiMgr.getStaticAchievementByType(x.getType());
            if (staticAchievementByType != null) {
                builder1.setV3(staticAchievementByType.size());
            }
            builder.addInfo(builder1);
        });

        List<StaticAchiAwardBox> achiAwardBoxHashMap = staticAchiMgr.getStaticAchiAwardBoxList(1, 0);
        achiAwardBoxHashMap.forEach(x -> {
            CommonPb.ActivityCond.Builder builder1 = CommonPb.ActivityCond.newBuilder();
            builder1.setKeyId(x.getId());
            builder1.setCond(x.getCond());
            Map<Integer, Integer> scoreAwardMap = achievementInfo.getScoreAwardMap();
            builder1.setIsAward(scoreAwardMap.containsKey(x.getId()) ? 1 : 0);
            List<List<Integer>> award = x.getAward();
            builder1.addAllAward(PbHelper.createListAward(award));
            builder.addCond(builder1);
        });
        handler.sendMsgToPlayer(ActivityPb.AchievementRs.ext, builder.build());
    }

    /**
     * 查询详细完成度
     *
     * @param req
     * @param handler
     */
    public void loadAchievementInfo(ActivityPb.AchievementInfoRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int type = req.getType();
        AchievementInfo achievementInfo = player.getAchievementInfo();
        Map<Integer, Integer> integerIntegerMap = achievementInfo.getShow().computeIfAbsent(type, x -> new ConcurrentHashMap<>());
        ActivityPb.AchievementInfoRs.Builder builder = ActivityPb.AchievementInfoRs.newBuilder();
        builder.setType(type);
        int size = playerManager.getAllPlayer().size();
        integerIntegerMap.values().forEach(x -> {
            StaticAchievement staticAchievementById = staticAchiMgr.getStaticAchievementById(x);
            if (staticAchievementById != null) {
                CommonPb.ThreeInt.Builder builder1 = CommonPb.ThreeInt.newBuilder();
                builder1.setV1(x);
                int cond = achievementInfo.getCond().getOrDefault(x, 0);
                builder1.setV2(cond >= staticAchievementById.getTarget() ? 1 : 0);
                int achievementCount = rankManager.getAchievementCount(x);
                int ceil = (int) ((double) achievementCount / size * 100);
                builder1.setV3(ceil);
                builder.addInfo(builder1);
            }
        });
        List<StaticAchiAwardBox> staticAchiAwardBoxList = staticAchiMgr.getStaticAchiAwardBoxList(2, type);
        staticAchiAwardBoxList.forEach(x -> {
            CommonPb.ActivityCond.Builder builder1 = CommonPb.ActivityCond.newBuilder();
            builder1.setKeyId(x.getId());
            builder1.setCond(x.getCond());
            Map<Integer, Integer> scoreAwardMap = achievementInfo.getScoreAwardMap();
            builder1.setIsAward(scoreAwardMap.containsKey(x.getId()) ? 1 : 0);
            List<List<Integer>> award = x.getAward();
            builder1.addAllAward(PbHelper.createListAward(award));
            builder.addCond(builder1);
        });
        builder.setScore(achievementInfo.getTypeScoreMap().getOrDefault(type, 0));
        handler.sendMsgToPlayer(ActivityPb.AchievementInfoRs.ext, builder.build());
    }

    public void recAchiBoxAward(ActivityPb.AchievementBoxAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int id = req.getId();
        StaticAchiAwardBox staticAchiAwardBoxById = staticAchiMgr.getStaticAchiAwardBoxById(id);
        if (staticAchiAwardBoxById == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        AchievementInfo achievementInfo = player.getAchievementInfo();
        if (achievementInfo.getScoreAwardMap().containsKey(id)) {
            handler.sendErrorMsgToPlayer(GameError.TARGET_AWARD_IS_AWARD);
            return;
        }
        boolean flag = false;
        if (staticAchiAwardBoxById.getChildType() == 0) {
            if (achievementInfo.getScore() >= staticAchiAwardBoxById.getCond()) {
                flag = true;
            }
        } else {
            int orDefault = achievementInfo.getTypeScoreMap().getOrDefault(staticAchiAwardBoxById.getType(), 0);
            if (orDefault >= staticAchiAwardBoxById.getCond()) {
                flag = true;
            }
        }
        if (!flag) {
            handler.sendErrorMsgToPlayer(GameError.SCORE_NOT_ENOUGH);
            return;
        }
        achievementInfo.getScoreAwardMap().put(id, 1);
        List<List<Integer>> award = staticAchiAwardBoxById.getAward();
        ActivityPb.AchievementBoxAwardRs.Builder builder = ActivityPb.AchievementBoxAwardRs.newBuilder();
        if (award != null) {
            award.forEach(x -> {
                playerManager.addAward(player, x.get(0), x.get(1), x.get(2), Reason.ACHI);
                builder.addAward(PbHelper.createAward(x.get(0), x.get(1), x.get(2)));
            });
        }
        handler.sendMsgToPlayer(ActivityPb.AchievementBoxAwardRs.ext, builder.build());
    }

    public void recAchiInfoAward(ActivityPb.AchievementInfoAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int id = req.getId();
        StaticAchievement staticAchievementById = staticAchiMgr.getStaticAchievementById(id);
        if (staticAchievementById == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        AchievementInfo achievementInfo = player.getAchievementInfo();
        Integer cond = achievementInfo.getCond().get(id);
        ActivityPb.AchievementInfoAwardRs.Builder builder = ActivityPb.AchievementInfoAwardRs.newBuilder();
        List<Integer> list = achievementInfo.getRec().computeIfAbsent(staticAchievementById.getType(), x -> new ArrayList<>());
        if (cond >= staticAchievementById.getTarget() && !list.contains(id)) {
            list.add(id);
            StaticAchievement staticAchievementById1 = staticAchiMgr.getStaticAchievementById(staticAchievementById.getNextId());
            if (staticAchievementById1 != null) {
                Map<Integer, Map<Integer, Integer>> show = achievementInfo.getShow();
                Map<Integer, Integer> integerIntegerMap = show.get(staticAchievementById1.getType());
                integerIntegerMap.put(staticAchievementById1.getGenre(), staticAchievementById1.getId());
            }
            List<Integer> x = staticAchievementById.getAward();
            if (x != null) {
                playerManager.addAward(player, x.get(0), x.get(1), x.get(2), Reason.ACHI);
                builder.addAward(PbHelper.createAward(x.get(0), x.get(1), x.get(2)));
            }
            achievementInfo.addScore(staticAchievementById.getScore());
            achievementInfo.getTypeScoreMap().merge(staticAchievementById.getType(), staticAchievementById.getScore(), (a, b) -> a + b);
            rankManager.statisAchievement(id);

        }
        Map<Integer, Integer> integerIntegerMap = achievementInfo.getShow().get(staticAchievementById.getType());
        integerIntegerMap.values().forEach(y -> {
            StaticAchievement staticAchievementById2 = staticAchiMgr.getStaticAchievementById(y);
            if (staticAchievementById2 != null) {
                CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
                builder1.setV1(y);
                int cond1 = achievementInfo.getCond().getOrDefault(y,0);
                builder1.setV2(cond1 >= staticAchievementById2.getTarget() ? 1 : 0);
                if (list.contains(y)) {
                    builder1.setV2(2);
                }
                builder.addInfo(builder1);
            }
        });
        handler.sendMsgToPlayer(ActivityPb.AchievementInfoAwardRs.ext, builder.build());
    }


    /**
     * 统计成就
     *
     * @param player
     * @param type
     * @param count
     */
    public void addAndUpdate(Player player, AchiType type, int count) {
        AchievementInfo achievementInfo = player.getAchievementInfo();
        Map<Integer, Integer> cond = achievementInfo.getCond();
        List<StaticAchievement> staticAchievementByType = staticAchiMgr.getStaticAchievementByType(type.getType());
        if (staticAchievementByType != null) {
            staticAchievementByType.forEach(x -> {
                if (type.getSet() == 1) {
                    Integer orDefault = cond.getOrDefault(x.getId(), 0);
                    if (count > orDefault) {
                        cond.put(x.getId(), count);
                    }
                } else {
                    cond.merge(x.getId(), count, (a, b) -> a + b);
                }
            });
        }
    }
}
