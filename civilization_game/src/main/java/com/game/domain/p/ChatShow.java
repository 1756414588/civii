package com.game.domain.p;

import com.game.pb.DataPb;

import java.util.HashSet;

// 全服通用
public class ChatShow {
    private int keyId;
    private int number;
    private HashSet<Long> lordIds = new HashSet<Long>();

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void readData(DataPb.ChatShowData data) {
        keyId = data.getKeyId();
        number = data.getNumber();
        for (Long lordId : data.getLordIdList()) {
            if (lordId != null) {
                lordIds.add(lordId);
            }
        }
    }

    public DataPb.ChatShowData.Builder writeData() {
        DataPb.ChatShowData.Builder builder = DataPb.ChatShowData.newBuilder();
        builder.setKeyId(keyId);
        builder.setNumber(number);
        if (!lordIds.isEmpty()) {
            builder.addAllLordId(lordIds);
        }

        return builder;
    }

    public ChatShow() {

    }

    public ChatShow(int keyId, int number) {
        setKeyId(keyId);
        setNumber(number);
    }

    public HashSet<Long> getLordIds() {
        return lordIds;
    }

    public void setLordIds(HashSet<Long> lordIds) {
        this.lordIds = lordIds;
    }

    public boolean hasLord(long lordId) {
        return lordIds.contains(lordId);
    }

    public void addLord(long lordId) {
        lordIds.add(lordId);
    }
}
