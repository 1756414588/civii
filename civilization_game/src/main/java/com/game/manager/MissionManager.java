package com.game.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.game.constant.GameError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.MissionStar;
import com.game.constant.MissionStateType;
import com.game.constant.MissionType;
import com.game.dataMgr.StaticMissionMgr;
import com.game.domain.Player;
import com.game.domain.p.Mission;
import com.game.domain.s.StaticMission;
import com.game.pb.CommonPb;
import com.game.pb.MissionPb;
import com.game.util.LogHelper;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;

@Component
public class MissionManager {

    @Autowired
    private StaticMissionMgr staticMissionMgr;

    public void addMisson(Player player, Mission mission) {
        Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
        if (missions == null) {
            LogHelper.CONFIG_LOGGER.error("player missions is null");
            return;
        }

        if (mission == null) {
            LogHelper.CONFIG_LOGGER.error("mission is null");
            return;
        }

        Map<Integer, Mission> mapInfo = missions.get(mission.getMapId());
        if (mapInfo == null) {
            mapInfo = new HashMap<Integer, Mission>();
            mapInfo.put(mission.getMissionId(), mission);
            missions.put(mission.getMapId(), mapInfo);
        } else {
            mapInfo.put(mission.getMissionId(), mission);
        }
    }

    public boolean hasMission(Player player, int missionId, int missionmap) {
        Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
        if (missions == null) {
            return true;
        }

        Map<Integer, Mission> missionMap = missions.get(missionmap);
        if (missionMap == null) {
            return true;
        }

        Mission mission = missionMap.get(missionId);
        if (mission == null) {
            return false;
        }

        return true;

    }

    public Mission addNextMission(Player player, int missionId, MissionPb.MissionDoneRs.Builder builder) {
        StaticMission config = staticMissionMgr.getStaticMission(missionId);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.error("mission is null, missionId = 1");
            return null;
        }

        Mission missionInfo = new Mission();
        missionInfo.setMissionId(config.getMissionId());
        missionInfo.setMapId(config.getMapId());
        missionInfo.setState(builder != null?MissionStateType.Open:MissionStateType.notOpen);
        missionInfo.setStar(0);
        missionInfo.setType(config.getMissionType());

        // 资源副本信息: 剩余时间、关卡攻打次数、关卡购买次数
        List<Integer> resoureInfo = config.getResource();
        if (resoureInfo != null && resoureInfo.size() == 5) {
            missionInfo.setFightTimes(resoureInfo.get(2));
            missionInfo.setResourceEndTime(TimeHelper.getEndTime(resoureInfo.get(4) * 1000L));
        } else {
            missionInfo.setResourceEndTime(0);
            missionInfo.setFightTimes(0);
        }

        // 资源副本购买次数
        missionInfo.setBuyTimes(0);

        // 装备图纸副本
        missionInfo.setBuyEquipPaperTimes(0);

        missionInfo.setCountryItemNum(0);
        missionInfo.setHeroBought(false);
        missionInfo.setResourceLandNum(0);
        if (builder == null) {
            return missionInfo;
        }
        addMisson(player, missionInfo);
        builder.addNextMission(missionInfo.wrapPb());

        return missionInfo;
    }

    // 默认开启第一关
    public void initMission(Player player, int missionId) {
        StaticMission config = staticMissionMgr.getStaticMission(missionId);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.error("mission is null, missionId = 1");
            return;
        }

        Mission missionInfo = new Mission();
        missionInfo.setMissionId(config.getMissionId());
        missionInfo.setMapId(config.getMapId());
        missionInfo.setState(MissionStateType.Open);
        missionInfo.setStar(0);
        missionInfo.setType(config.getMissionType());
        // 资源副本购买次数
        missionInfo.setBuyTimes(0);
        // 装备图纸副本
        missionInfo.setBuyEquipPaperTimes(0);
        missionInfo.setCountryItemNum(0);
        missionInfo.setHeroBought(false);
        missionInfo.setResourceLandNum(0);
        addMisson(player, missionInfo);
    }

    // 一键开启所有关卡
    public void openAllMission(Player player) {
        MissionPb.GetAllMissionRs.Builder builder = MissionPb.GetAllMissionRs.newBuilder();
        Map<Integer, StaticMission> missionMap = staticMissionMgr.getMissionMap();
        for (Map.Entry<Integer, StaticMission> entry : missionMap.entrySet()) {
            StaticMission config = entry.getValue();
            if (config.getMissionType()==MissionType.ResourceLandChip||config.getMapId()==100){
                continue;
            }
            Mission missionInfo = getMission(player, config.getMissionId(), config.getMapId());
            if (missionInfo != null) {
                missionInfo.setState(MissionStateType.Open);
                builder.addMission(missionInfo.wrapPb());
                continue;
            }
            missionInfo = new Mission();
            missionInfo.setMissionId(config.getMissionId());
            missionInfo.setMapId(config.getMapId());
            missionInfo.setState(MissionStateType.Open);
            missionInfo.setStar(0);
            missionInfo.setType(config.getMissionType());
            // 资源副本购买次数
            missionInfo.setBuyTimes(0);
            // 装备图纸副本
            missionInfo.setBuyEquipPaperTimes(0);
            missionInfo.setCountryItemNum(0);
            missionInfo.setHeroBought(false);
            missionInfo.setResourceLandNum(0);
            // 资源副本信息: 剩余时间、关卡攻打次数、关卡购买次数
            List<Integer> resoureInfo = config.getResource();
            if (resoureInfo != null && resoureInfo.size() == 5) {
                missionInfo.setFightTimes(resoureInfo.get(2));
                missionInfo.setResourceEndTime(TimeHelper.getEndTime(resoureInfo.get(4) * 1000L));
            }
            addMisson(player, missionInfo);
            builder.addMission(missionInfo.wrapPb());
        }
        SynHelper.synMsgToPlayer(player, MissionPb.GetAllMissionRs.EXT_FIELD_NUMBER, MissionPb.GetAllMissionRs.ext, builder.build());

    }

    //开启某一个关卡的
    public void openMission(Player player, int mapId) {
        MissionPb.GetAllMissionRs.Builder builder = MissionPb.GetAllMissionRs.newBuilder();
        Map<Integer, StaticMission> missionMap = staticMissionMgr.getMissionMap();
        for (Map.Entry<Integer, StaticMission> entry : missionMap.entrySet()) {
//            if(entry.getValue().getMissionId()==208){
//                System.out.println("============================================");
//            }
            StaticMission config = entry.getValue();
            if (config.getMapId() > mapId) {
                continue;
            }
            Mission missionInfo = getMission(player, config.getMissionId(), config.getMapId());
            if (missionInfo != null) {
                missionInfo.setState(MissionStateType.Open);
                builder.addMission(missionInfo.wrapPb());
                continue;
            }
            missionInfo = new Mission();
            missionInfo.setMissionId(config.getMissionId());
            missionInfo.setMapId(config.getMapId());
            missionInfo.setState(MissionStateType.Open);
            missionInfo.setStar(0);
            missionInfo.setType(config.getMissionType());
            // 资源副本购买次数
            missionInfo.setBuyTimes(0);
            // 装备图纸副本
            missionInfo.setBuyEquipPaperTimes(0);
            missionInfo.setCountryItemNum(0);
            missionInfo.setHeroBought(false);
            missionInfo.setResourceLandNum(0);
            // 资源副本信息: 剩余时间、关卡攻打次数、关卡购买次数
            List<Integer> resoureInfo = config.getResource();
            if (resoureInfo != null && resoureInfo.size() == 5) {
                missionInfo.setFightTimes(resoureInfo.get(2));
                missionInfo.setResourceEndTime(TimeHelper.getEndTime(resoureInfo.get(4) * 1000L));
            }
            addMisson(player, missionInfo);
            builder.addMission(missionInfo.wrapPb());
        }
        SynHelper.synMsgToPlayer(player, MissionPb.GetAllMissionRs.EXT_FIELD_NUMBER, MissionPb.GetAllMissionRs.ext, builder.build());

    }

    // 获取关卡
    public Mission getMission(Player player, int missionId, int mapId) {
        Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
        if (missions == null) {
            return null;
        }

        Map<Integer, Mission> missionMap = missions.get(mapId);
        if (missionMap == null) {
            return null;
        }

        Mission mission = missionMap.get(missionId);
        if (mission == null) {
            return null;
        }

        return mission;
    }

    // finish boss state
    public void doBossMission(Player player, Mission mission, StaticMission staticMission, boolean isOpenMission) {
        if (mission == null) {
            LogHelper.CONFIG_LOGGER.error("mission is null!");
            return;
        }

        if (staticMission.getMissionType() != MissionType.BossMission) {
            return;
        }

        int missionId = mission.getMissionId();
        TreeMap<Integer, TreeMap<Integer, Integer>> missionStar = player.getMissionStar();
        TreeMap<Integer, Integer> stateMap = missionStar.get(missionId);
        if (stateMap == null) {
            stateMap = new TreeMap<Integer, Integer>();
            for (int i = 1; i <= 3; i++) {
                stateMap.put(i, MissionStar.MISSION_STAR_CLOSE);
            }
            missionStar.put(missionId, stateMap);
        }

        int star = mission.getStar();
        boolean isChanged = false;
        for (int i = 1; i <= star; i++) {
            int currentState = stateMap.get(i);
            if (currentState == MissionStar.MISSION_STAR_CLOSE) {
                stateMap.put(i, MissionStar.MISSION_STAR_AWARD_OK);
                isChanged = true;
            }
        }

        if (!isOpenMission && !isChanged) {
            return;
        }

        MissionPb.SynMissionStarRq.Builder builder = MissionPb.SynMissionStarRq.newBuilder();
        CommonPb.MissionStarInfo.Builder starInfo = CommonPb.MissionStarInfo.newBuilder();
        starInfo.setId(missionId);
        for (Integer state : stateMap.values()) {
            starInfo.addState(state);
        }
        builder.setStarInfo(starInfo);
        SynHelper.synMsgToPlayer(player, MissionPb.SynMissionStarRq.EXT_FIELD_NUMBER, MissionPb.SynMissionStarRq.ext, builder.build());
    }

    /**
     * 是否通关该章节
     *
     * @param mapId
     * @return
     */
    public int pssBossMission(Player player, int mapId) {
        int missionId = mapId * 100 + 6;
        StaticMission bossMission = staticMissionMgr.getStaticMission(missionId);
        if (bossMission == null) {
            LogHelper.CONFIG_LOGGER.error("bossMission is null, mapId = " + mapId + ", misionId = " + missionId);
            return 0;
        }

        Map<Integer, Mission> missMap = player.getMissions().get(mapId);
        if (missMap == null || missMap.isEmpty()) {
            return 0;
        }

        Mission boss = missMap.get(bossMission.getMissionId());
        if (boss == null) {
            return 0;
        }
        return boss.getStar() > 0 ? 1 : 0;
    }
}
