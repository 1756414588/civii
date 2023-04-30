package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.Award;
import com.game.domain.s.StaticRebelExchange;
import com.game.domain.s.StaticRebelRankAward;
import com.game.domain.s.StaticRebelZergDrop;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @date 2020/4/28 19:33
 * @description
 */
@Service
@LoadData(name = "伏击叛军")
public class StaticRebelMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;

    /**
     * 兑换集合
     */
    private Map<Integer, StaticRebelExchange> exchanges = new ConcurrentHashMap<>();

    /**
     * 伏击叛军排行榜奖励集合
     */
    private Map<Integer, StaticRebelRankAward> rebelRankAwards = new ConcurrentHashMap<>();


    /**
     * 密电掉落集合
     */
    private Map<Integer, StaticRebelZergDrop> rebelZergDrops = new ConcurrentHashMap<>();

    @Override
    public void load() throws Exception {
        exchanges = staticDataDao.selectStaticRebelExchange();
        rebelRankAwards = staticDataDao.selectStaticRebelRankAward();
        rebelZergDrops = staticDataDao.selectStaticRebelZergDrop();
    }

    @Override
    public void init() throws Exception{

    }


    public Map<Integer, StaticRebelExchange> getExchanges() {
        return exchanges;
    }

    public Map<Integer, StaticRebelRankAward> getRebelRankAwards() {
        return rebelRankAwards;
    }

    public Map<Integer, StaticRebelZergDrop> getRebelZergDrops() {
        return rebelZergDrops;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Award getrRebelZergDropAWard(int lv) {
        StaticRebelZergDrop staticRebelZergDrop = rebelZergDrops.get(lv);
        if(staticRebelZergDrop==null){
            logger.error("getrRebelZergDropAWard lv {} ",lv);
            return null;
        }
        int probability = RandomUtils.nextInt(1, 101);
        if (probability <= staticRebelZergDrop.getProbability()) {
            List<Integer> drop = staticRebelZergDrop.getDrop();
            Award award = new Award(drop.get(0), drop.get(1), drop.get(2));
            return award;
        }
        return null;
    }


    /**
     * 拿到排行奖励
     * @param type  1 个人奖励  2 排行奖励
     * @param rank
     * @return
     */
    public List<Award> getRebelRankAward(int type, int rank) {
        List<Award> awardList = new ArrayList<>();
        for (StaticRebelRankAward s : rebelRankAwards.values()) {
            if (s.getType() != type) {
                continue;
            }
            if (s.getBayssegmenting().get(0) <= rank && rank <= s.getBayssegmenting().get(1)) {
                for (List<Integer> info : s.getAward()) {
                    Award award = new Award(info.get(0), info.get(1), info.get(2));
                    awardList.add(award);
                }
            }
        }
        return awardList;
    }
}
