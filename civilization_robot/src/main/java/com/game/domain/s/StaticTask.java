package com.game.domain.s;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 任务
 */
@Getter
@Setter
public class StaticTask {
    private int taskId;
    private String taskName;
    private int type;
    private int typeChild;
    private int triggerId;
    private int process;
    private int exp;
    private List<List<Long>> awardList;
    private List<List<Integer>> param;
    private List<Integer> openBuildingId;
    private int autoBuildTimes;
    private int newState;
    private int generateMonster;
    private int collectTimes;
    private List<Integer> mulTriggerId;
}
