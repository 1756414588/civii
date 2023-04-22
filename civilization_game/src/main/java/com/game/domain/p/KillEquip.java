package com.game.domain.p;


import com.game.pb.CommonPb;
import com.game.pb.DataPb;

public class KillEquip implements Cloneable {
    private int equipId;
    private int level;
    private int process;   // 当前进度 0~100, 一次增加10点
    private int criti;     // 暴击倍率
    private int isOpen;   //是否解锁  0.表示未解锁 1.表示已经解锁

    @Override
    public KillEquip clone() {
        KillEquip killEquip = null;
        try {
            killEquip = (KillEquip) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return killEquip;
    }

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public CommonPb.KillEquip.Builder wrapPb() {
        CommonPb.KillEquip.Builder builder = CommonPb.KillEquip.newBuilder();
        builder.setEquipId(equipId);
        builder.setLevel(level);
        builder.setProcess(process);
        builder.setCirti(criti);
        builder.setIsOpen(isOpen);
        return builder;
    }

    public void unwrapPb(CommonPb.KillEquip builder) {
        equipId = builder.getEquipId();
        level = builder.getLevel();
        process = builder.getProcess();
        criti = builder.getCirti();
        isOpen = builder.getIsOpen();
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public int getCriti() {
        return criti;
    }

    public void setCriti(int criti) {
        this.criti = criti;
    }

    // 读数据库
    public void readData(DataPb.KillEquipData data) {
        if (data == null) {
            return;
        }

        equipId = data.getEquipId();
        level = data.getLevel();
        process = data.getProcess();
        criti = data.getCirti();
        isOpen = data.getIsOpen();

    }

    // 写数据库
    public DataPb.KillEquipData.Builder writeData() {
        DataPb.KillEquipData.Builder builder = DataPb.KillEquipData.newBuilder();
        builder.setEquipId(equipId);
        builder.setLevel(level);
        builder.setProcess(process);
        builder.setCirti(criti);
        builder.setIsOpen(isOpen);

        return builder;
    }

    public int getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }
}
