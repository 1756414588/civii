package com.game.domain.p;


import com.game.pb.DataPb;

// 挖图纸信息
public class DigInfo {
    private long lordId;
    private int  itemId;
    public DigInfo() {

    }

    public DigInfo(long lordId, int itemId) {
        setLordId(lordId);
        setItemId(itemId);
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }


    public DataPb.DigInfoData.Builder writeData() {
        DataPb.DigInfoData.Builder builder = DataPb.DigInfoData.newBuilder();
        builder.setLordId(lordId);
        builder.setItemId(itemId);
        return builder;
    }

    public void readData(DataPb.DigInfoData data) {
        lordId = data.getLordId();
        itemId = data.getItemId();
    }
}
