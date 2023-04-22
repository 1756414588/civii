package com.game.uc;

public class CdkeyUniversalKey {
    private Long roleid;

    private Integer areaid;

    private String keychar;

    private int channel;

    public Long getRoleid() {
        return roleid;
    }

    public void setRoleid(Long roleid) {
        this.roleid = roleid;
    }

    public Integer getAreaid() {
        return areaid;
    }

    public void setAreaid(Integer areaid) {
        this.areaid = areaid;
    }

    public String getKeychar() {
        return keychar;
    }

    public void setKeychar(String keychar) {
        this.keychar = keychar == null ? null : keychar.trim();
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}