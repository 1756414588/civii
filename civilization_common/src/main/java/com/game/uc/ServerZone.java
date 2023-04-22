package com.game.uc;

/**
 * @author jyb
 * @date 2020/4/8 10:45
 * @description 游戏分区
 */
public class ServerZone {
    /**
     * 游戏分区
     */
    private  int  id;

    /***
     * 分区名字
     */
    private  String  zoneName;

    /**
     * 分区名称  "|" 隔开
     */
    private String  channel;



    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
