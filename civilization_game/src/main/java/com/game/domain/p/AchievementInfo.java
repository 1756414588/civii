package com.game.domain.p;

import com.game.dataMgr.StaticAchiMgr;
import com.game.domain.Player;
import com.game.domain.s.StaticAchievement;
import com.game.manager.RankManager;
import com.game.pb.CommonPb;
import com.game.spring.SpringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class AchievementInfo {
    private Player player;
    private int score;//累计积分
    private Map<Integer, Integer> scoreAwardMap = new ConcurrentHashMap<>();//积分奖励记录
    private Map<Integer, Integer> cond = new ConcurrentHashMap<>();//成就累计完成度
    private Map<Integer, Map<Integer, Integer>> show = new ConcurrentHashMap<>();//当前展示得进行中得成就
    private Map<Integer, List<Integer>> rec = new ConcurrentHashMap<>();//领取了奖励得成就
    private Map<Integer, Integer> typeScoreMap = new ConcurrentHashMap<>();//成就累计完成度

    public AchievementInfo() {

    }

    public CommonPb.AchievementPbInfo encode() {
        CommonPb.AchievementPbInfo.Builder builder = CommonPb.AchievementPbInfo.newBuilder();
        builder.setScore(this.score);
        scoreAwardMap.forEach((x, y) -> {
            CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
            builder1.setV1(x);
            builder1.setV2(y);
            builder.addScoreAwardMap(builder1);
        });
        cond.forEach((x, y) -> {
            CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
            builder1.setV1(x);
            builder1.setV2(y);
            builder.addCond(builder1);
        });
        show.values().forEach(x -> x.values().forEach(y -> {
            builder.addShow(y);
        }));
        rec.values().forEach(x -> x.forEach(y -> {
            builder.addRec(y);
        }));
        typeScoreMap.forEach((x, y) -> {
            CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
            builder1.setV1(x);
            builder1.setV2(y);
            builder.addTypeScore(builder1);
        });

        return builder.build();
    }

    public void decode(CommonPb.AchievementPbInfo pbInfo, Player player) {
        this.player = player;
        StaticAchiMgr bean = SpringUtil.getBean(StaticAchiMgr.class);
        if (pbInfo == null || pbInfo.getShowList().isEmpty()) {
            Map<Integer, StaticAchievement> firstAchiMap = bean.getFirstAchiMap();
            firstAchiMap.values().forEach(x -> {
                Map<Integer, Integer> integerIntegerMap = show.computeIfAbsent(x.getType(), y -> new ConcurrentHashMap<>());
                integerIntegerMap.put(x.getGenre(), x.getId());
            });
        } else {
            this.score = pbInfo.getScore();
            List<CommonPb.TwoInt> scoreAwardMapList = pbInfo.getScoreAwardMapList();
            if (scoreAwardMapList != null) {
                scoreAwardMapList.forEach(x -> {
                    scoreAwardMap.put(x.getV1(), x.getV2());
                });
            }

            List<CommonPb.TwoInt> condList = pbInfo.getCondList();
            if (condList != null) {
                condList.forEach(x -> {
                    cond.put(x.getV1(), x.getV2());
                });
            }

            List<Integer> showList = pbInfo.getShowList();

            if (showList != null && !showList.isEmpty()) {
                showList.forEach(x -> {
                    StaticAchievement staticAchievementById = bean.getStaticAchievementById(x);
                    if (staticAchievementById != null) {
                        Map<Integer, Integer> integerIntegerMap = show.computeIfAbsent(staticAchievementById.getType(), y -> new ConcurrentHashMap<>());
                        integerIntegerMap.put(staticAchievementById.getGenre(), staticAchievementById.getId());
                    }
                });
            }
            List<Integer> recList = pbInfo.getRecList();
            if (recList != null) {
                recList.forEach(x -> {
                    StaticAchievement staticAchievementById = bean.getStaticAchievementById(x);
                    if (staticAchievementById != null) {
                        List<Integer> list = rec.computeIfAbsent(staticAchievementById.getType(), y -> new ArrayList<>());
                        list.add(staticAchievementById.getId());
                        SpringUtil.getBean(RankManager.class).statisAchievement(staticAchievementById.getId());
                    }
                });
            }
            List<CommonPb.TwoInt> typeScoreList = pbInfo.getTypeScoreList();
            if (typeScoreList != null) {
                typeScoreList.forEach(x -> {
                    typeScoreMap.put(x.getV1(), x.getV2());
                });
            }
        }
    }

    public void addScore(int score) {
        this.score += score;
        RankManager bean = SpringUtil.getBean(RankManager.class);
        bean.updateAchievementRank(this);
    }
}
