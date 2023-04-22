package com.game.manager;

import com.game.constant.MarchState;
import com.game.constant.WarType;
import com.game.constant.WorldActivityConsts;
import com.game.define.Fight;
import com.game.define.WorldActCmd;
import com.game.domain.WorldData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.p.WorldMap;
import com.game.spring.SpringUtil;
import com.game.util.ClassUtil;
import com.game.util.LogHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.process.FightProcess;
import com.game.worldmap.fight.IFightProcess;
import com.game.worldmap.fight.IWar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import jdk.nashorn.internal.ir.IfNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BattleManager {

    // 战斗处理
    private Map<Integer, IFightProcess> fightProcessHashMap = new HashMap<>();
    // 行军处理
    private Map<Integer, IFightProcess> marchProcessHashMap = new HashMap<>();
    // 世界活动处理
    private Map<Integer, IFightProcess> worldActPlanHashMap = new HashMap<>();
    // 处理器列表
    private List<IFightProcess> fightProcessList = new ArrayList<>();

    @Autowired
    private WorldManager worldManager;

    public void init() {
        Map<String, IFightProcess> beansOfType = SpringUtil.getApplicationContext().getBeansOfType(IFightProcess.class);
        beansOfType.values().forEach(process -> {
            Fight fight = process.getClass().getAnnotation(Fight.class);
            if (fight != null) {
                process.init(fight.warType(), fight.marthes());
                for (int warType : fight.warType()) {
                    fightProcessHashMap.put(warType, process);
                }
                for (int marchType : fight.marthes()) {
                    marchProcessHashMap.put(marchType, process);
                }
                // 处理器列表
                fightProcessList.add(process);
            }

            WorldActCmd actCmd = process.getClass().getAnnotation(WorldActCmd.class);
            if (actCmd != null) {
                worldActPlanHashMap.put(actCmd.actId(), process);
            }
        });
    }


    public void battleTimer() {
        ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
        Iterator<MapInfo> it = worldMapInfo.values().iterator();
        while (it.hasNext()) {
            MapInfo mapInfo = it.next();
            processWar(mapInfo);
            processRebelWar(mapInfo);
        }

        // 定时处理世界活动
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            return;
        }
        Iterator<Map.Entry<Integer, IFightProcess>> iterator = worldActPlanHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, IFightProcess> next = iterator.next();
            int actId = next.getKey();
            WorldActPlan worldActPlan = worldData.getWorldActPlans().get(actId);
            if (worldActPlan != null) {
                IFightProcess fightProcess = next.getValue();
                fightProcess.doWorldActPlan(worldActPlan);
            }
        }
    }

    /**
     * 行军定时器，后续可根据类型进行分线程处理
     */
    public void marchTimer() {
        ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
        Iterator<MapInfo> it = worldMapInfo.values().iterator();
        while (it.hasNext()) {
            MapInfo mapInfo = it.next();
            processMarch(mapInfo);
        }
    }

    /**
     * 加载战斗
     *
     * @param worldMap
     * @param mapInfo
     */
    public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
        // 闪电战
        if (worldMap.getQuickWarData() != null) {
            fightProcessHashMap.get(WarType.ATTACK_QUICK).loadWar(worldMap, mapInfo);
        }
        // 远征
        if (worldMap.getFarWarData() != null) {
            fightProcessHashMap.get(WarType.ATTACK_FAR).loadWar(worldMap, mapInfo);
        }
        // 国家城池战
        if (worldMap.getCountryWarData() != null) {
            fightProcessHashMap.get(WarType.ATTACK_COUNTRY).loadWar(worldMap, mapInfo);
        }
        // 巨型虫族
        if (worldMap.getBigMonsterWarData() != null) {
            fightProcessHashMap.get(WarType.BIGMONSTER_WAR).loadWar(worldMap, mapInfo);
        }
        //
        if (worldMap.getZergWarData() != null) {
            fightProcessHashMap.get(WarType.ATTACK_ZERG).loadWar(worldMap, mapInfo);
        }
    }

    private void processWar(MapInfo mapInfo) {
        Iterator<IWar> it = mapInfo.getWarMap().values().iterator();
        while (it.hasNext()) {
            try {
                IWar war = it.next();
                IFightProcess process = fightProcessHashMap.get(war.getWarType());
                process.process(mapInfo, war);
                if (war.isEnd()) {
                    it.remove();
                }
            } catch (Exception e) {
                LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void processRebelWar(MapInfo mapInfo) {
        Map<Long, WarInfo> rebelWarMap = mapInfo.getRebelWarMap();
        if (rebelWarMap.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<WarInfo> it = rebelWarMap.values().iterator();
        while (it.hasNext()) {
            WarInfo war = it.next();
            try {
                IFightProcess process = fightProcessHashMap.get(war.getWarType());
                process.process(mapInfo, war);
                if (war.isEnd()) {
                    it.remove();
                }
            } catch (Exception e) {
                LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 处理行军
     *
     * @param mapInfo
     */
    private void processMarch(MapInfo mapInfo) {
        if (mapInfo == null || mapInfo.getMarchMap() == null || mapInfo.getMarchMap().isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<March> it = mapInfo.getMarchMap().values().iterator();
        while (it.hasNext()) {// 单独行军报错不影响整体运行
            March march = it.next();

            try {
                if (march.getEndTime() > now) {// 行军中
                    continue;
                }

                IFightProcess process = marchProcessHashMap.get(march.getMarchType());
                if (process == null) {
                    LogHelper.ERROR_LOGGER.info("process is Null playerId:{} marchType:{}", march.getLordId(), march.getMarchType());
                    continue;
                }

                process.doMarch(mapInfo, march);

                // 行军完成则删除
                if (march.getState() == MarchState.Done) {
                    it.remove();
                }
            } catch (Exception e) {
                LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public IFightProcess getWarProcess(int warType) {
        return fightProcessHashMap.get(warType);
    }

    public IFightProcess getMarchProcess(int marchType) {
        return marchProcessHashMap.get(marchType);
    }

}
