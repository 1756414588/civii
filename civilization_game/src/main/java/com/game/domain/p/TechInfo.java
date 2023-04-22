package com.game.domain.p;

import com.game.pb.DataPb;


// 科技等级信息
public class TechInfo {
    private int techType;       // 科技type
    private int level;         // 当前科技等级
    private int process;       // 当前科技的进度
    private int speed;          //加速次数

    public TechInfo() {
    }

    public TechInfo(int techType, int level, int process) {
        this.techType = techType;
        this.level = level;
        this.process = process;

    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }


    public DataPb.TechData.Builder wrapData() {
        DataPb.TechData.Builder builder = DataPb.TechData.newBuilder();
        builder.setTechType(techType);
        builder.setTechLevel(level);
        builder.setProcess(process);
        builder.setSpeed(speed);
        return builder;
    }

    public void unwrapData(DataPb.TechData builder) {
        if (builder != null) {
            level = builder.getTechLevel();
            process = builder.getProcess();
            techType = builder.getTechType();
            this.speed = builder.getSpeed();
        }
    }

    public int getTechType() {
        return techType;
    }

    public void setTechType(int techType) {
        this.techType = techType;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void addSpeed(int speed) {
        this.speed += speed;
    }
}
