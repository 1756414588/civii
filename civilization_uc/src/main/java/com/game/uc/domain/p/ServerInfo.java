package com.game.uc.domain.p;

import com.game.pay.channel.ChannelConsts;
import com.game.pay.channel.PlayerExist;
import com.game.pb.AccountLoginPb;
import com.game.uc.Server;

import java.util.Date;
import java.util.List;

/**
 *
 * @date 2020/4/8 14:48
 * @description
 */
public class ServerInfo {
    /**
     * 服务Id
     */
    private int serverId;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 服务器ip
     */
    private String ip;

    /**
     * tcp端口
     */
    private int port;


    /**
     * 服务器标签1新服 2推荐 3拥挤 0无标记
     */
    private int label;

    /**
     * 开服时间
     */
    private Date startTime;


    private int status;

    /**
     * 是否能进入
     */
    private boolean join;

    /**
     * 当前人数最小的国家1,2,3
     */
    private int minCountry;

    /**
     * 当前服务器玩家是否存在角色
     */
    private boolean isExistRole;

    /**
     * 当前服务器合服后阵营映射的ip
     */
    private List<ServerCountryMapping> serverCountryMappingList;

    /**
     * 服务器结束时间
     */
    private Date repairEndTime;
    /**
     * 玩家阵营
     */
    private int country;
    /**
     * 玩家昵称
     */
    private String nick;
    /**
     * 玩家等级
     */
    private int level;
    /**
     * 玩家头像
     */
    private int portrait;

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPortrait() {
        return portrait;
    }

    public void setPortrait(int portrait) {
        this.portrait = portrait;
    }

    public Date getRepairEndTime() {
        return repairEndTime;
    }

    public void setRepairEndTime(Date repairEndTime) {
        this.repairEndTime = repairEndTime;
    }

    public boolean isJoin() {
        return join;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }


    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public ServerInfo() {
    }

    public int getMinCountry() {
        return minCountry;
    }

    public void setMinCountry(int minCountry) {
        this.minCountry = minCountry;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isExistRole() {
        return isExistRole;
    }

    public void setExistRole(boolean existRole) {
        isExistRole = existRole;
    }

    public List<ServerCountryMapping> getServerCountryMappingList() {
        return serverCountryMappingList;
    }

    public void setServerCountryMappingList(List<ServerCountryMapping> serverCountryMappingList) {
        this.serverCountryMappingList = serverCountryMappingList;
    }

    public ServerInfo(Server server, String serverName) {
        this.serverId = server.getServerId();
        this.serverName = serverName;
        this.ip = server.getIp();
        this.label = server.getLabel();
        this.port = server.getPort();
        this.startTime = server.getOpenTime();
        this.status = server.getState();
        this.repairEndTime = server.getRepairEndTime();
    }

    public void setPlayerInfo(PlayerExist playerInfo, int channel) {
        if (channel == ChannelConsts.KUAI_YOU_ID) {
            return;
        }
        this.nick = playerInfo.getNick();
        this.portrait = playerInfo.getPortrait();
        this.level = playerInfo.getLevel();
        if (playerInfo.getCountry() == 0 && playerInfo.getNick() != null && !playerInfo.getNick().isEmpty()) {
            this.country = 1;
        } else {
            this.country = playerInfo.getCountry();
        }
    }

    public AccountLoginPb.ServerInfo encode() {
        AccountLoginPb.ServerInfo.Builder builder = AccountLoginPb.ServerInfo.newBuilder();
        builder.setServerId(this.serverId);
        builder.setServerName(this.serverName);
        builder.setIp(this.ip);
        builder.setPort(this.port);
        builder.setLabel(this.label);
        builder.setStartTime(this.startTime.getTime());
        builder.setStatus(this.status);
        builder.setJoin(this.join);
        builder.setMinCountry(this.minCountry);
        builder.setExistRole(this.isExistRole);
        builder.setCountry(this.country);
        builder.setNick(this.nick == null ? "" : this.nick);
        builder.setLevel(this.level);
        builder.setPortrait(this.portrait);
        for (ServerCountryMapping serverCountryMapping : serverCountryMappingList) {
            AccountLoginPb.ServerCountryMapping.Builder builder1 = AccountLoginPb.ServerCountryMapping.newBuilder();
            builder1.setCountyId(serverCountryMapping.getCountyId());
            builder1.setIp(serverCountryMapping.getIp());
            builder1.setPort(serverCountryMapping.getPort());
            builder.addServerCountryMappingList(builder1);
        }
        return builder.build();
    }
}
