package com.game.dataMgr;

import com.game.constant.OpenConditionType;
import com.game.dao.s.StaticDataDao;
import com.game.domain.Player;
import com.game.domain.p.Command;
import com.game.domain.p.Task;
import com.game.domain.s.StaticOpen;
import com.game.manager.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jyb
 * @date 2020/1/16 14:39
 * @description
 */
@Component
public class StaticOpenManger extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticOpen> opens = new HashMap<>();
    @Autowired
    private TaskManager taskManager;

    @Override
    public void init() throws Exception{
        opens = staticDataDao.selectStaticOpen();
    }


    public StaticOpen getOpen(int keyId) {
        return opens.get(keyId);
    }

    /**
     * 是否开启
     *
     * @param keyId
     * @param player
     * @return
     */
    public boolean isOpen(int keyId, Player player) {
        StaticOpen staticOpen = getOpen(keyId);
        if (staticOpen.getType() == OpenConditionType.PLAYER_LEVEL) {
            return player.getLevel() >= staticOpen.getCondition();
        } else if (staticOpen.getType() == OpenConditionType.COMMAND_LEVEL) {
            Command command = player.buildings.getCommand();
            return command.getLv() >= staticOpen.getCondition();
        } else if (staticOpen.getType() == OpenConditionType.TASK) {
            Map<Integer, Task> taskMap = player.getTaskMap();
            for (Task task : taskMap.values()) {
                int taskType = taskManager.getTaskType(task.getTaskId());
                //主线
                if (taskType == 1) {
                    return task.getTaskId() >= staticOpen.getCondition();
                }
            }
        }
        return true;
    }

    /**
     * @param pram 玩家等级  ,任务id  ,主城等级
     * @param type OpenConditionType 1 玩家等级 2 任务id ，3 主城等级
     * @return
     */
    public List<Integer> getBuildOpen(int pram, int type) {
        List<Integer> staticOpens = new ArrayList<>();
        for (Map.Entry<Integer, StaticOpen> entry : opens.entrySet()) {
            StaticOpen staticOpen = entry.getValue();
            if (staticOpen.getFunctionType() != 2) {
                continue;
            }
            if (staticOpen.getType() != type) {
                continue;
            }
//            //无条件
//            if(staticOpen.getType()==3){
//                continue;
//            }

            if (pram == staticOpen.getCondition()) {
                staticOpens.add(staticOpen.getValue());
            }
        }
        return staticOpens;
    }
}
