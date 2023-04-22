package com.game.manager;

import com.game.constant.AddMonsterReason;
import com.game.constant.StaffTaskState;
import com.game.dataMgr.StaticStaffMgr;
import com.game.domain.Player;
import com.game.domain.p.SimpleData;
import com.game.domain.p.StaffTask;
import com.game.domain.s.StaticStaffTask;
import com.game.util.LogHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Monster;
import com.game.worldmap.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StaffManager {

    @Autowired
    private StaticStaffMgr staticStaffMgr;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private CountryManager countryManager;

    public boolean isTaskIdOk(int taskId) {
        Map<Integer, StaticStaffTask> config = staticStaffMgr.getStaffTaskMap();
        if (!config.containsKey(taskId)) {
            return false;
        }
        return true;
    }

    public boolean isPreTaskOk(int taskId, HashMap<Integer, StaffTask> staffTaskMap) {
        int preTaskId = taskId - 1;
        if (preTaskId != 0) {
            // check status
            for (int i = 1; i <= preTaskId; i++) {
                StaffTask preTask = staffTaskMap.get(i);
                if (preTask == null || preTask.isNotActivated()) {
                    return false;
                }
            }
        }
        return true;
    }

    public StaffTask createStaffTask(int taskId) {
        StaffTask task = new StaffTask();
        task.setTaskId(taskId);
        task.setStatus(StaffTaskState.NOT_OPEN);
        return task;
    }

    public void checkTaskMap(HashMap<Integer, StaffTask> staffTaskMap) {
        Map<Integer, StaticStaffTask> config = staticStaffMgr.getStaffTaskMap();
        for (StaticStaffTask elem : config.values()) {
            if (!staffTaskMap.containsKey(elem.getTaskId())) {
                StaffTask task = createStaffTask(elem.getTaskId());
                staffTaskMap.put(task.getTaskId(), task);
            }
        }
    }


    public void flushTaskMonster(Player player, int taskId) {
        StaticStaffTask staticTask = staticStaffMgr.getStaffTask(taskId);
        if (staticTask == null) {
            LogHelper.CONFIG_LOGGER.info("staticTask is null");
            return;
        }

        int monsterLv = staticTask.getMonsterId();
        if (monsterLv <= 0) {
            return;
        }

        int total = 0;
        // 给玩家刷4只野怪
        //int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(player.getLord().getMapId());
        // 表示没有开启世界地图
        if (mapInfo == null) {
            // LogHelper.CONFIG_LOGGER.info("mapInfo is null!");
            return;
        }

        List<Pos> posAround = mapInfo.getPos(player, 3,4);
        List<Entity> list = new ArrayList<>();
        for (Pos pos : posAround) {
            // 判断是否是free的
            //if (!mapInfo.isFreePos(pos)) {
            //    continue;
            //}

            Monster monster = worldManager.addMonster(pos, 1000 + monsterLv, monsterLv, mapInfo, AddMonsterReason.STAFF_MONSTER);
            if (monster == null) {
                continue;
            }
            list.add(monster);
            //++total;
            //if (total >= 4) {
            //    break;
            //}
        }
        worldManager.synEntityAddRq(list);
    }

    public boolean checkCond(Player player, int taskId) {
        StaticStaffTask config = staticStaffMgr.getStaffTask(taskId);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.info("config is null, taskId = " + taskId);
            return false;
        }

        int configSoldier = config.getSoldierNum();
        int roleLv = config.getRoleLv();
        int currentSoldier = countryManager.getSoldierNum(player);
        boolean isSoldierOk = currentSoldier >= configSoldier;
        boolean isRoleLvOk = player.getLevel() >= roleLv;
        return isSoldierOk || isRoleLvOk;
    }

    // 战斗时进行计算
    public int getSoldierLine(Player player) {
        int soldierLine = 0;
        SimpleData simpleData = player.getSimpleData();
        HashMap<Integer, StaffTask> staffTaskMap = simpleData.getStaffTaskMap();
        for (StaffTask task : staffTaskMap.values()) {
            if (task.isActivated()) {
                soldierLine += 1;
            }
        }
        return soldierLine;
    }


}
