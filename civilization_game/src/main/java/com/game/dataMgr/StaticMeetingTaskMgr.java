package com.game.dataMgr;

import com.game.constant.ArmyEnum;
import com.game.dao.s.StaticDataDao;
import com.game.domain.Player;
import com.game.domain.s.StaticMeetingCommand;
import com.game.domain.s.StaticMeetingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author jyb
 * @date 2019/12/13 11:22
 * @description
 */
@Component
public class StaticMeetingTaskMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;
    @Autowired
    private StaticMeetingCommandMgr staticMeetingCommandMgr;

    private Map<Integer, StaticMeetingTask> meetingTasks = new ConcurrentHashMap<>();

    @Override
    public void init() throws Exception{
        meetingTasks = staticDataDao.selectMeetingTask();
    }


    public StaticMeetingTask getStartMeetingTask() {
        List<StaticMeetingTask> staticMeetingTasks = new ArrayList<>(meetingTasks.values());
        staticMeetingTasks = staticMeetingTasks.stream().sorted(Comparator.comparingInt(StaticMeetingTask::getId)).collect(Collectors.toList());
        return staticMeetingTasks.get(0);
    }

    public StaticMeetingTask getStaticMeetingTask(int taskId) {
        return meetingTasks.get(taskId);
    }

    /**
     * 查看怪物是否是指挥部的任务怪
     *
     * @param monster
     * @return
     */
    public boolean checkTaskMonster(int monster) {
        for (StaticMeetingTask staticMeetingTask : meetingTasks.values()) {
            if (staticMeetingTask.getMonsterId() == monster) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过部队类型拿各部队兵排的加成
     *
     * @return
     */
    public int soldierNumByType(int taskId, ArmyEnum armyEnum) {
        int num = 0;
        for (Map.Entry<Integer, StaticMeetingTask> entry : meetingTasks.entrySet()) {
            StaticMeetingCommand staticMeetingCommand = staticMeetingCommandMgr.getStaticMeetingCommand(entry.getValue().getAward());
			if (staticMeetingCommand == null) {
				continue;
			}
            if (entry.getKey().intValue() < taskId) {
                if (armyEnum.getType() == staticMeetingCommand.getMeetType()) {
                    num = staticMeetingCommand.getEffect() + num;
                }
            }
        }
        return num;
    }

    public int soldierNumByHero(Player player, int heroId) {
        ArmyEnum armyEnum = null;
        List<Integer> embattleList = player.getEmbattleList();
        List<Integer> miningList = player.getMiningList();
        if (embattleList.contains(heroId)) {
            armyEnum = ArmyEnum.ARMY_ONE;
        } else if (miningList.contains(heroId)) {
            armyEnum = ArmyEnum.ARMY_TWO;
        } else if (player.getDefenseArmyList().stream().anyMatch(e -> e.getHeroId() == heroId)) {
            armyEnum = ArmyEnum.ARMY_THREE;
        } else {
            armyEnum = ArmyEnum.ARMY_ONE;
        }
        if (armyEnum == null) {
            return 0;
        }
        //说明一个都没激活
        if (player.getMeetingTask().getId() < 1) {
            return 0;
        }
        return soldierNumByType(player.getMeetingTask().getId(), armyEnum);
    }
}
