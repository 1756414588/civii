package com.game.uc;

import java.util.Date;

public class Cdkey {
    private Integer id;

    private String keychar;

    private int channel;

    private Integer areaid;

    private Integer keytype;

    private Date starttime;

    private Date endtime;

    private Integer rewardobjectid;

    private Integer isuse;

    private Long roleid;

    private Date createtime;

    private Integer universal;

    private Byte assign;

    private int use_level;

    public int getUse_level() {
        return use_level;
    }

    public void setUse_level(int use_level) {
        this.use_level = use_level;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getAreaid() {
        return areaid;
    }

    public void setAreaid(Integer areaid) {
        this.areaid = areaid;
    }

    public Integer getKeytype() {
        return keytype;
    }

    public void setKeytype(Integer keytype) {
        this.keytype = keytype;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Integer getRewardobjectid() {
        return rewardobjectid;
    }

    public void setRewardobjectid(Integer rewardobjectid) {
        this.rewardobjectid = rewardobjectid;
    }

    public Integer getIsuse() {
        return isuse;
    }

    public void setIsuse(Integer isuse) {
        this.isuse = isuse;
    }

    public Long getRoleid() {
        return roleid;
    }

    public void setRoleid(Long roleid) {
        this.roleid = roleid;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Integer getUniversal() {
        return universal;
    }

    public void setUniversal(Integer universal) {
        this.universal = universal;
    }

    public Byte getAssign() {
        return assign;
    }

    public void setAssign(Byte assign) {
        this.assign = assign;
    }
}