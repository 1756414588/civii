package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticRiotAward;
import com.game.domain.s.StaticRiotMonster;
import com.game.domain.s.StaticRoitItemShop;
import com.game.domain.s.StaticRoitScoreShop;
import com.game.domain.s.StaticRoitWaveMonster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@LoadData(name = "虫族暴乱")
public class StaticRiotMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticRiotMonster> roitMonsterMap = new HashMap<Integer, StaticRiotMonster>();
    private Map<Integer, StaticRoitItemShop> roitItemShopMap = new HashMap<Integer, StaticRoitItemShop>();
    private Map<Integer, StaticRoitScoreShop> roitScoreShopMap = new HashMap<Integer, StaticRoitScoreShop>();
    private Map<Integer, StaticRoitWaveMonster> roitWaveMap = new HashMap<Integer, StaticRoitWaveMonster>();
    private List<StaticRiotAward> riotAwardList = new ArrayList<StaticRiotAward>();

    @Override
    public void load() throws Exception {
        roitMonsterMap = staticDataDao.selectRoitMonster();
        roitItemShopMap = staticDataDao.selectItemShop();
        roitScoreShopMap = staticDataDao.selectRoitScoreShop();
        roitWaveMap = staticDataDao.selectRoitWave();
        setRiotAwardList(staticDataDao.selectRiotAward());
    }

    @Override
    public void init() throws Exception{

    }

    public StaticRiotMonster getRoitMonster(int keyId) {
        return roitMonsterMap.get(keyId);
    }

    public StaticRoitWaveMonster getWaveMonster(int keyId) {
        return roitWaveMap.get(keyId);
    }

    public List<StaticRiotAward> getRiotAwardList() {
        return riotAwardList;
    }

    public void setRiotAwardList(List<StaticRiotAward> riotAwardList) {
        this.riotAwardList = riotAwardList;
    }

    public Map<Integer, StaticRoitItemShop> getRoitItemShopMap() {
        return roitItemShopMap;
    }

    public void setRoitItemShopMap(Map<Integer, StaticRoitItemShop> roitItemShopMap) {
        this.roitItemShopMap = roitItemShopMap;
    }

    public Map<Integer, StaticRoitScoreShop> getRoitScoreShopMap() {
        return roitScoreShopMap;
    }

    public void setRoitScoreShopMap(Map<Integer, StaticRoitScoreShop> roitScoreShopMap) {
        this.roitScoreShopMap = roitScoreShopMap;
    }

    public int getWaveMonster(int type, int waveIndex) {
        StaticRoitWaveMonster monster = roitWaveMap.get(type);
        if (monster != null) {
            if (monster.getMonsters().size() > waveIndex) {
                return monster.getMonsters().get(waveIndex);
            }
        }
        return 0;
    }

    public StaticRiotAward getRiotAwardByWave(int wave) {
        Optional<StaticRiotAward> award = getRiotAwardList().stream().filter(e -> e.getKeyId() == wave).findFirst();
        if (award.isPresent()) {
            return award.get();
        }
        return null;
    }
}
