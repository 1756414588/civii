package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.SerializePb;

/**
 * @author jyb
 * @date 2020/1/13 15:04
 * @description
 */
public class ResourcePacket implements Cloneable {

    private int resId; //资源id

    private int packetNum;// 一共打包的次数

    private long packetTime;//上一次打包的时间 ，跨天cz

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getPacketNum() {
        return packetNum;
    }

    public void setPacketNum(int packetNum) {
        this.packetNum = packetNum;
    }

    public long getPacketTime() {
        return packetTime;
    }

    public void setPacketTime(long packetTime) {
        this.packetTime = packetTime;
    }

    public SerializePb.ResPacket.Builder wrapPb() {
        SerializePb.ResPacket.Builder builder = SerializePb.ResPacket.newBuilder();
        CommonPb.ResPacketInfo.Builder resPacketInfo = CommonPb.ResPacketInfo.newBuilder();
        resPacketInfo.setResId(resId);
        resPacketInfo.setPacketNum(packetNum);
        builder.setResPacketTime(packetTime);
        builder.setResPacketInfo(resPacketInfo);
        return builder;
    }

    @Override
    public ResourcePacket clone() {
        ResourcePacket resourcePacket = null;
        try {
            resourcePacket = (ResourcePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return resourcePacket;
    }
}
