package com.game.domain.p;

import com.game.domain.s.StaticHeroTask;
import com.game.domain.s.StaticPassPortTask;
import com.game.pb.DataPb;
import lombok.Getter;
import lombok.Setter;

/**
 * @author CaoBing
 * @date 2020/10/15 20:51
 */
@Getter
@Setter
public class ActPassPortTask implements Cloneable {
    /**
     * 任务Id
     */
    private int id;
    /**
     * 任务类型
     */
    private int type;
    /**
     * 任务的子类型
     */
    private int taskType;
    /**
     * 任务达成的进度
     */
    private int process;
    /**
     * 是否已经领奖 0.未领奖 1.已经领奖
     */
    private int isAward;

    @Override
    public ActPassPortTask clone() {
        ActPassPortTask actPassPortTask = null;
        try {
            actPassPortTask = (ActPassPortTask) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return actPassPortTask;
    }

    public ActPassPortTask(StaticPassPortTask task) {
        this.id = task.getId();
        this.type = task.getType();
        this.taskType = task.getTaskType();
        this.process = 0;
        this.isAward = 0;
    }

    public ActPassPortTask(StaticHeroTask staticHeroTask) {
        this.id = staticHeroTask.getId();
        this.type = staticHeroTask.getJumpType();
        this.taskType = staticHeroTask.getJumpType();
        this.process = 0;
        this.isAward = 0;
    }

    public ActPassPortTask(DataPb.ActPassPortTaskData pb) {
        this.id = pb.getId();
        this.taskType = pb.getTaskType();
        this.type = pb.getType();
        this.process = pb.getProcess();
        this.isAward = pb.getIsAward();
    }

    @Override
    public String toString() {
        return "ActPassPortTask{" +
                "id=" + id +
                ", type=" + type +
                ", taskType=" + taskType +
                ", process=" + process +
                ", isAward=" + isAward +
                '}';
    }
}
