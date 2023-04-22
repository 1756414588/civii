package com.game.domain.p;

import com.game.pb.CommonPb;

import java.util.ArrayList;
import java.util.LinkedList;

public class Soldier implements Cloneable {
    private int soldierType;        //士兵类型 1.火箭兵 2.坦克 3.战车
    private int num;                //士兵数量
    private int capacity;           //兵营等级
    private int largerTimes;        //扩容次数
    private int employeeTimes;      //单次募兵时间扩充次数
    private LinkedList<WorkQue> workQues = new LinkedList<WorkQue>();  // 建造,等待, 需要存盘, 安装顺序来 now+period1, now+period1+period2
    private int soldierIndex;       //兵营索引

    public int getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public CommonPb.Soldier.Builder wrapPb() {
        CommonPb.Soldier.Builder builder = CommonPb.Soldier.newBuilder();
        if (soldierIndex <= 0) {
            soldierIndex = soldierType;
        }
        builder.setSoldierType(getSoldierType());
        builder.setNum(getNum());
        builder.setLargerTimes(getLargerTimes());
        builder.setEmployeeTimes(getEmployeeTimes());
        builder.setSoldierIndex(getSoldierIndex());
        return builder;
    }

    public void unWrapPb(CommonPb.Soldier build) {
        setSoldierType(build.getSoldierType());
        setNum(build.getNum());
        setLargerTimes(build.getLargerTimes());
        setEmployeeTimes(build.getEmployeeTimes());
        setSoldierIndex(build.getSoldierIndex());
    }

    public Soldier() {

    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getLargerTimes() {
        return largerTimes;
    }

    public void setLargerTimes(int largerTimes) {
        this.largerTimes = largerTimes;
    }

    public int getEmployeeTimes() {
        return employeeTimes;
    }

    public void setEmployeeTimes(int employeeTimes) {
        this.employeeTimes = employeeTimes;
    }

    public LinkedList<WorkQue> getWorkQues() {
        return workQues;
    }

    public void setWorkQues(LinkedList<WorkQue> workQues) {
        this.workQues = workQues;
    }

    public boolean isTraining() {
        return !workQues.isEmpty();
    }

    public int getSoldierIndex() {
        return soldierIndex;
    }

    public void setSoldierIndex(int soldierIndex) {
        this.soldierIndex = soldierIndex;
    }

    // 后面的que时间往前面挪
    public void handleMoveTime(LinkedList<WorkQue> workQues) {
        for (int index = 1; index < workQues.size(); index++) {
            WorkQue preElem = workQues.get(index - 1);
            WorkQue curElem = workQues.get(index);
            if (curElem == null)
                continue;
            curElem.setEndTime(preElem.getEndTime() + curElem.getPeriod());
        }
    }

    public void checkWorkQues() {
        long now = System.currentTimeMillis();
        if (!workQues.isEmpty()) {
            WorkQue first = workQues.getFirst();
            if (first.getEndTime() >= now + first.getPeriod()) {
                first.setEndTime(now + first.getPeriod());
            }
            handleMoveTime(workQues);
        }
    }

    @Override
    public Soldier clone() {
        Soldier soldier = null;
        try {
            soldier = (Soldier) super.clone();
            LinkedList<WorkQue> list1 = new LinkedList<>();
            this.workQues.forEach(workQue -> {
                list1.add(workQue.clone());
            });
            soldier.setWorkQues(list1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return soldier;
    }
}
