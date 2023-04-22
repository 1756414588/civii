package com.game.manager;

import com.game.dao.s.StaticDataDao;
import com.game.dataMgr.BaseDataMgr;
import com.game.domain.s.StaticRiotAward;
import com.game.domain.s.StaticRiotMonster;
import com.game.domain.s.StaticRoitItemShop;
import com.game.domain.s.StaticRoitScoreShop;
import com.game.domain.s.StaticRoitTime;
import com.game.domain.s.StaticRoitWaveMonster;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author cpz
 * @date 2020/9/24 14:23
 * @description 虫族入侵管理
 */
@Component
public class StaticActRoitManager extends BaseDataMgr {
    @Autowired
    private StaticDataDao dataDao;

    //难度
    private Map<Integer, StaticRoitTime> difficulty;
    //奖励
    private List<StaticRiotAward> riotAwards;
    //信物商店
    private Map<Integer, StaticRoitItemShop> roitItemShops;
    //积分商店
    private Map<Integer, StaticRoitScoreShop> roitScoreShops;
    //普通虫兵配置 难度 列表
    private Map<Integer, StaticRiotMonster> roitMonsters;
    //攻城虫兵配置 难度 列表
    private Map<Integer, StaticRoitWaveMonster> roitWaveMonsters;

    HashBasedTable<Integer, Integer, Integer> monsterFlushNum;

    private boolean isFush;

    private long refushTime;

    @Override
    public void init() throws Exception {
        difficulty = dataDao.selectRoitTime();
        riotAwards = dataDao.selectRiotAward();
        roitItemShops = dataDao.selectItemShop();
        roitScoreShops = dataDao.selectRoitScoreShop();
        roitMonsters = dataDao.selectRoitMonster();
        roitWaveMonsters = dataDao.selectRoitWave();
        refushTime = 0;
    }

    /**
     * 根据难度获取monster
     *
     * @param keyId
     * @return
     */
    public HashBasedTable<Integer, Integer, Integer> getMonster(int keyId) {
        return roitMonsters.get(keyId).getMonster();
    }

    public boolean isFush() {
        return isFush;
    }

    public void setFush(boolean fush) {
        isFush = fush;
    }


    public long getRefushTime() {
        return refushTime;
    }

    public void setRefushTime(long refushTime) {
        this.refushTime = refushTime;
    }

    public int getWaveMonster(int type, int waveIndex) {
        StaticRoitWaveMonster monster = roitWaveMonsters.get(type);
        if (monster != null) {
            if (monster.getMonsters().size() > waveIndex) {
                return monster.getMonsters().get(waveIndex);
            }
        }
        return 0;
    }

}
