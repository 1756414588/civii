package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

/**
 * @author CaoBing
 * @date 2021/2/1 9:24
 * 主城皮肤
 */
public class CommandSkin implements Cloneable {
    private int keyId;//皮肤ID
    private int status;//状态 0.未激活 1.已经激活
    private long endTime;//剩余时间
    private int star;//主城皮肤星级

    @Override
    public CommandSkin clone() {
        CommandSkin commandSkin = null;
        try {
            commandSkin = (CommandSkin) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return commandSkin;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public CommandSkin cloneInfo() {
        CommandSkin commandSkin = new CommandSkin();
        commandSkin.keyId = keyId;
        commandSkin.status = status;
        commandSkin.endTime = endTime;
        commandSkin.star = star;
        return commandSkin;
    }

    public CommonPb.CommandSkin.Builder wrapPb() {
        CommonPb.CommandSkin.Builder builder = CommonPb.CommandSkin.newBuilder();
        builder.setKeyId(keyId);
        builder.setStatus(status);
        builder.setEndTime(endTime);
        builder.setStar(star);
        return builder;
    }

    public void unwrapPb(CommonPb.CommandSkin build) {
        keyId = build.getKeyId();
        status = build.getStatus();
        endTime = build.getEndTime();
        star = build.getStar();
    }

    public void copyData(CommandSkin commandSkin) {
        keyId = commandSkin.getKeyId();
        status = commandSkin.getStatus();
        endTime = commandSkin.getEndTime();
        star = commandSkin.getStar();
    }

    public DataPb.SkinData.Builder writeData() {
        DataPb.SkinData.Builder builder = DataPb.SkinData.newBuilder();
        builder.setKeyId(keyId);
        builder.setStatus(status);
        builder.setEndTime(endTime);
        builder.setStar(star);
        return builder;
    }

    public void readData(DataPb.SkinData build) {
        keyId = build.getKeyId();
        status = build.getStatus();
        endTime = build.getEndTime();
        star = build.getStar();
    }

    @Override
    public String toString() {
        return "CommandSkin{" +
                "keyId=" + keyId +
                ", status=" + status +
                ", endTime=" + endTime +
                ", star=" + star +
                '}';
    }
}
