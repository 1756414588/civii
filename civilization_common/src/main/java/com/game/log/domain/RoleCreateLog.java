package com.game.log.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * @author cpz
 * @date 2020/9/22 16:08
 * @description
 */
@Builder
@Data
public class RoleCreateLog {
    private String channel;
    private int serverId;
    private long roleId;
    private int professionType;
    private Date operationTime;
    private String roleName;
    private String account;
    private int isRand;
    private String model;
    private String mei;
    private String mac;
    private String idfa;

    @Tolerate
    public RoleCreateLog(){

    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getProfessionType() {
        return professionType;
    }

    public void setProfessionType(int professionType) {
        this.professionType = professionType;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getIsRand() {
        return isRand;
    }

    public void setIsRand(int isRand) {
        this.isRand = isRand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMei() {
        return mei;
    }

    public void setMei(String mei) {
        this.mei = mei;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }
}
