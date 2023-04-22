package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.BattleProperty;
import com.game.domain.p.Property;
import com.game.domain.s.StaticMonster;
import com.game.domain.s.StaticZergMonster;
import com.game.domain.s.StaticZergRound;
import com.game.manager.BattleMgr;
import com.game.util.LogHelper;
import com.google.common.collect.HashBasedTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@LoadData(name = "虫族主宰")
public class StaticZerglMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;
    @Autowired
    private StaticMonsterMgr staticMonsterMgr;
    @Autowired
    private BattleMgr battleMgr;

    /**
     * 类型type,第几波wave,怪StaticZergMonster
     */
    @Getter
    private List<StaticZergMonster> zergMonsters;
    @Getter
    private Map<Integer, StaticZergMonster> zergShowMap = new HashMap<>();

    private HashBasedTable<Integer, Integer, StaticZergMonster> zergMonsterMap = HashBasedTable.create();
    @Getter
    private List<StaticZergRound> zergRounds;


    @Override
    public void load() throws Exception {
        zergMonsters = staticDataDao.loadStaticZergMonster();
        zergRounds = staticDataDao.loadStaticZergRound();
        zergMonsters.forEach(e -> {
            zergMonsterMap.put(e.getType(), e.getWaves(), e);
            zergShowMap.put(e.getShowId(), e);
        });
    }

    @Override
    public void init() throws Exception {

    }


    public StaticMonster getShowMonster(int step, int wave) {
        int monsterId = zergMonsterMap.get(step, wave).getShowId();
        return staticMonsterMgr.getStaticMonster(monsterId);
    }


    public StaticZergMonster getShow(int monsterId) {
        return zergShowMap.get(monsterId);
    }


//    /**
//     * 初始化怪物
//     *
//     * @param type
//     * @param wave
//     * @return
//     */
//    public Team initTeam(int type, int wave) {
//        List<Integer> monsters = zergMonsterMap.get(type, wave).getMonsters();
//        Team team = new Team();
//        for (Integer monsterId : monsters) {
//            BattleEntity battleEntity = createMonster(monsterId, BattleEntityType.BIG_MONSTER);
//            if (battleEntity != null) {
//                team.add(battleEntity);
//            }
//        }
//        return team;
//    }
//
//    /**
//     * @param monsterId
//     * @return
//     */
//    public Team createMonsterTeam(int monsterId) {
//        StaticZergMonster staticZergMonster = zergShowMap.get(monsterId);
//        if (staticZergMonster == null) {
//            return null;
//        }
//        Team team = new Team();
//        List<Integer> monsters = staticZergMonster.getMonsters();
//        for (Integer id : monsters) {
//            BattleEntity battleEntity = createMonster(id, BattleEntityType.BIG_MONSTER);
//            if (battleEntity != null) {
//                team.add(battleEntity);
//            }
//        }
//        return team;
//    }


    public BattleEntity createMonster(int monsterId, int entityType) {
        StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
        if (staticMonster == null) {
            LogHelper.CONFIG_LOGGER.info("staticMonster == null");
            return null;
        }

        Property property = battleMgr.getPveMonsterPro(staticMonster);

        BattleEntity battleEntity = new BattleEntity();
        battleEntity.setName(staticMonster.getName());

        // 二级属性：战斗属性
        BattleProperty battleProperty = battleMgr.getPveMonsterBp(staticMonster);

        battleMgr.initBattleEntity(battleEntity, monsterId, staticMonster.getLevel(), staticMonster.getSoldierLines(), property,
                staticMonster.getSoldierType(), battleProperty, entityType, 0L);

        return battleEntity;

    }

    public StaticZergRound getRound(int id) {
        Optional<StaticZergRound> optional = zergRounds.stream().filter(e -> e.getId() == id).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }


}
