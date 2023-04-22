package com.game.log.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * 装备秘技
 */
@Builder
@Data
public class EquipDecompoundLog {
    // 角色ID
    private Long roleId;

    // 角色名称
    private String roleName;

    // 角色等级
    private int roleLv;

    // 玩家军衔
    private int title;

    // 玩家阵营
    private int country;

    // vip等级
    private int vip;

    // 装备ID
    private int equipId;

    // 装备品阶 1.金、2.红、3.紫
    private int quality;

    //角色创建时间
    private Date roleCreateTime;

    //账号Key
    private int keyId;

    //渠道
    private int channel;
    //是否分解 0 分解 1产出
    private boolean decompose;
    //产出消耗方式
    private int reason;

    @Tolerate
    public EquipDecompoundLog(){

    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleLv() {
        return roleLv;
    }

    public void setRoleLv(int roleLv) {
        this.roleLv = roleLv;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public Date getRoleCreateTime() {
        return roleCreateTime;
    }

    public void setRoleCreateTime(Date roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isDecompose() {
        return decompose;
    }

    public void setDecompose(boolean decompose) {
        this.decompose = decompose;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }
}
