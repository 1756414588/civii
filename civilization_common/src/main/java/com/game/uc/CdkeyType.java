package com.game.uc;

public class CdkeyType {
    private Integer autoid;

    private String type;

    private String name;

    private Integer giftbagid;

    private Integer channel;

    private Integer universal;

    public Integer getAutoid() {
        return autoid;
    }

    public void setAutoid(Integer autoid) {
        this.autoid = autoid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getGiftbagid() {
        return giftbagid;
    }

    public void setGiftbagid(Integer giftbagid) {
        this.giftbagid = giftbagid;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getUniversal() {
        return universal;
    }

    public void setUniversal(Integer universal) {
        this.universal = universal;
    }
}