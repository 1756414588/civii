package com.game.rank;

import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.domain.Player;
import com.game.domain.p.WorldActPlan;
import com.game.manager.PlayerManager;
import com.game.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jyb
 * @date 2020/5/6 19:08
 * @description
 */
@Service
public class RebelScoreRankMgr extends AbstractRankMgr<RankInfo> {


    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private WorldManager worldManager;


    private Map<Integer, CountryScore> countryScores = new ConcurrentHashMap<>();

    @Override
    public void send(Player player, int startIndex, int length) {

    }

    @Override
    public boolean update(Player player, Object... data) {
        return false;
    }

    @Override
    public boolean update(RankInfo info) {
        lock.lock();
        int score = 0;
        try {
            int index = indexOf(info.getKey());
            if (index == -1) {
                score = info.getValue();
                int size = rankList.size();
                if (size < getCapacity()) {
                    rankList.add(info);
                    sort();
                    return true;
                } else {
                    RankInfo bottom = rankList.get(size - 1);
                    if (compare(bottom, info)) {
                        return false;
                    }
                    rankList.set(size - 1, info);
                    sort();
                    return true;
                }

            } else {
                //如果在排行榜里面 要减去上一次的增加量
                score = info.getValue() - rankList.get(index).getValue();
                rankList.set(index, info);
                sort();
                return true;
            }

        } finally {
            //更新国家score
            try {
                Player player = playerManager.getPlayer(info.getKey());
                CountryScore countryScore = countryScores.get(player.getCountry());
                if (countryScore == null) {
                    countryScore = new CountryScore(player.getCountry(), 0);
                    countryScores.put(countryScore.getCountryId(),countryScore);
                }
                countryScore.setScore(countryScore.getScore() + score);
            } catch (Exception e) {
                logger.error("DefaultRankMgr update error {}", e);
            }

            lock.unlock();
        }

    }

    public void init() {
        WorldActPlan worldActPlan = worldManager.getWorldActPlan(WorldActivityConsts.ACTIVITY_2);
        if (worldActPlan == null || worldActPlan.getState() != WorldActPlanConsts.OPEN) {
            return;
        }

        for (Player player : playerManager.getPlayers().values()) {
            int rebelScore = player.getSimpleData().getRebelScore();
            if (player.getSimpleData().getRebelScore() != 0) {
                RankInfo rankInfo = new RankInfo(player.getLord().getLordId(), rebelScore);
                update(rankInfo);
            }
        }
    }

    @Override
    public void online(Player player) {

    }


    public List<CountryScore> getCountryScores() {
        List<CountryScore> scores = new ArrayList<>(countryScores.values());
        Collections.sort(scores);
        return scores;
    }


    /**
     * 拿到阵营排行
     * @param country
     * @return
     */
    public int getCountryRank(int country) {
        int rank = 1;
        for (CountryScore c : getCountryScores()) {
            if (c.getCountryId() == country) {
                return rank;
            }
            rank++;
        }
        return -1;
    }
}
