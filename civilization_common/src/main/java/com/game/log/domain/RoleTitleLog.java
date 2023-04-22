package com.game.log.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * @author cpz
 * @date 2020/9/22 16:18
 * @description
 */
@Builder
@Data
public class RoleTitleLog {
    private long roleId;
    private Date roleCreateTime;
    private int rolelv;
    private int viplv;
    private int title;
    private long honor;
    private long decreHonor;
    private int country;
    private int channel;
    @Tolerate
    public RoleTitleLog(){}
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public Date getRoleCreateTime() {
        return roleCreateTime;
    }

    public void setRoleCreateTime(Date roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
    }

    public int getRolelv() {
        return rolelv;
    }

    public void setRolelv(int rolelv) {
        this.rolelv = rolelv;
    }

    public int getViplv() {
        return viplv;
    }

    public void setViplv(int viplv) {
        this.viplv = viplv;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public long getHonor() {
        return honor;
    }

    public void setHonor(long honor) {
        this.honor = honor;
    }

    public long getDecreHonor() {
        return decreHonor;
    }

    public void setDecreHonor(long decreHonor) {
        this.decreHonor = decreHonor;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
